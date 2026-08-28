package principal.iluminacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Gestor maestro del subsistema de iluminación dinámica 2D, sombreado acelerado
 * en VRAM, ciclo solar de 24 horas y consultas de sigilo para la Inteligencia
 * Artificial.
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Máscara en Espacio de Pantalla (Screen-Space Lightmap):</b> Renderiza
 * la oscuridad y los agujeros de luz sobre una {@link VolatileImage} acelerada
 * en VRAM a escala 1:1 ($640 \times 360$). Esto garantiza cobertura total de
 * esquina a esquina sin recuadros negros ni líneas de corte en ningún nivel de
 * zoom.</li>
 * <li><b>Pipeline Dual-Pass:</b> Perfora sombras restando opacidad mediante
 * {@link AlphaComposite#DST_OUT} y estampa el tinte cálido/arcano con
 * {@link AlphaComposite#SRC_OVER}.</li>
 * <li><b>Atenuación Diurna (Sunlight Washout):</b> Desvanece automáticamente el
 * tinte de las linternas y auras bajo la luz solar para evitar manchas
 * artificiales de luz sobre el pasto diurno.</li>
 * <li><b>Doble Frustum Culling O(1):</b> Descarta luces tanto en el renderizado
 * (Render Culling) como en la física de parpadeo de fuego en la lógica (Update
 * Culling), compensando zoom final, rotación ($\theta$) y temblores ($X,
 * Y$).</li>
 * <li><b>Pool de 256 Luces con Free-List Stack (Zero-GC):</b> Permite encender,
 * apagar y reutilizar fuentes de luz en tiempo constante sin instanciar objetos
 * en el Heap durante el juego.</li>
 * </ul>
 * </p>
 * 
 * @version 10.0
 */
public class GestorLuz {

	// =========================================================================
	// === 1. CAPACIDAD Y TEXTURIZADO
	// =========================================================================

	/** Capacidad máxima de fuentes de luz simultáneas activas en memoria. */
	private static final int CAPACIDAD_LUCES = 256;

	/**
	 * Dimensión en píxeles de las texturas cuadradas de halos pre-horneados en HD.
	 */
	private static final int RESOLUCION_HALO_HD = 256;

	/**
	 * Margen perimetral de holgura en píxeles de mundo para el Update Frustum
	 * Culling.
	 */
	private static final int MARGEN_CULLING_MUNDO = 96;

	// Composites estándar pre-instanciados (Zero-GC)
	private static final AlphaComposite COMPOSITE_LIMPIEZA = AlphaComposite.getInstance(AlphaComposite.CLEAR);
	private static final AlphaComposite COMPOSITE_NORMAL = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	private static final AlphaComposite COMPOSITE_PERFORAR = AlphaComposite.getInstance(AlphaComposite.DST_OUT);

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: POOL DE COMPOSITES PARA ATENUACIÓN DIURNA (ZERO-GC)
	 * ------------------------------------------------------------------------- En
	 * Java 2D, llamar a 'AlphaComposite.getInstance(SRC_OVER, float)' dentro del
	 * bucle de renderizado puede generar basura en memoria si el valor float
	 * cambia.
	 *
	 * Para que el tinte de color de las linternas se desvanezca suavemente al salir
	 * el sol sin crear ningún objeto nuevo en el Heap:
	 *
	 * Pre-instanciamos 11 niveles fijos de opacidad (del 0% al 100% de fuerza del
	 * tinte). Al dibujar, simplemente seleccionamos el índice '[0..10]'
	 * correspondiente en O(1).
	 * =========================================================================
	 */
	private static final AlphaComposite[] COMPOSITES_TINTE_ATENUADO = new AlphaComposite[11];
	static {
		for (int i = 0; i <= 10; i++) {
			final float opacidad = (i / 10.0f) * 0.40f; // Máximo 40% de opacidad de tinte en noche cerrada
			COMPOSITES_TINTE_ATENUADO[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad);
		}
	}

	// =========================================================================
	// === 2. POOL DE MEMORIA Y FREE-LIST STACK (ZERO-GC)
	// =========================================================================

	/** Pool maestro de instancias pre-asignadas en memoria estática. */
	private final FuenteLuz[] pool;

	/**
	 * Pila de índices libres para asignación y liberación en tiempo constante O(1).
	 */
	private final int[] indicesLibres;

	/** Puntero al tope de la pila de ranuras libres. */
	private int topePila;

	/**
	 * Arreglo contiguo de luces actualmente encendidas para iteración rápida de
	 * CPU.
	 */
	private final FuenteLuz[] activas;

	/** Cantidad de luces encendidas y procesándose en el fotograma actual. */
	private int cantidadActivas;

	/**
	 * Controlador del reloj solar de 24 horas y transiciones de color ambiental.
	 */
	private final CicloDiaNoche ciclo;

	/**
	 * Framebuffer secundario acelerado en GPU donde se compone la máscara de
	 * sombras.
	 */
	private VolatileImage lightmap;

	// Texturas de halo circular y cónico pre-generadas al arrancar el motor
	private final BufferedImage texturaMascaraAlphaHD;
	private final BufferedImage texturaMascaraConoHD;
	private final BufferedImage[] texturasHaloColor;
	private final BufferedImage[] texturasHaloColorCono;

	// =========================================================================
	// === 3. ESTADOS DE AMBIENTE, CUEVAS Y BIOMAS
	// =========================================================================

	private boolean iluminacionHabilitada = true;
	private boolean modoAmbienteFijo = false;
	private Color colorAmbienteFijo = new Color(0, 0, 0, 255);

	// Transición suave al entrar a cuevas/mazmorras
	private boolean transicionActiva = false;
	private Color colorTransicionOrigen;
	private Color colorTransicionDestino;
	private double tiempoTransicionTotal;
	private double tiempoTransicionActual;

	// Tinte ambiental recibido desde GestorZonasAmbiente
	private Color colorTinteBioma = null;
	private double factorInmersionBioma = 0.0;

	// Flashes de explosiones y relámpagos
	private boolean flashGlobalActivo = false;
	private double duracionFlashGlobal = 0.0;
	private double tiempoFlashGlobalRestante = 0.0;
	private boolean flashGlobalRelampago = false;

	// Dirty-Flags de Color para evitar 'new Color()' en cada tick (Zero-GC)
	private int lastBaseR = -1, lastBaseG = -1, lastBaseB = -1, lastBaseA = -1;
	private Color colorAmbienteCalculado = new Color(0, 0, 0, 0);

	// =========================================================================
	// === CONSTRUCTOR: INICIALIZACIÓN Y PRE-HORNEADO
	// =========================================================================

	/**
	 * Inicializa el pool de 256 luces, la pila Free-List, el reloj solar y hornea
	 * los gradientes radiales y cónicos en texturas de memoria estática.
	 */
	public GestorLuz() {
		this.pool = new FuenteLuz[CAPACIDAD_LUCES];
		this.indicesLibres = new int[CAPACIDAD_LUCES];
		this.activas = new FuenteLuz[CAPACIDAD_LUCES];
		this.cantidadActivas = 0;
		this.topePila = CAPACIDAD_LUCES;

		for (int i = 0; i < CAPACIDAD_LUCES; i++) {
			this.pool[i] = new FuenteLuz(i);
			this.indicesLibres[i] = i;
		}

		this.ciclo = new CicloDiaNoche();

		// 1. Horneado de máscaras de perforación alfa en HD
		this.texturaMascaraAlphaHD = this.hornearTexturaMascaraHD();
		this.texturaMascaraConoHD = this.hornearTexturaMascaraConoHD(85.0);

		// 2. Horneado de halos coloreados por tipo de luz
		final int totalTipos = TipoLuz.values().length;
		this.texturasHaloColor = new BufferedImage[totalTipos];
		this.texturasHaloColorCono = new BufferedImage[totalTipos];

		for (final TipoLuz tipo : TipoLuz.values()) {
			this.texturasHaloColor[tipo.ordinal()] = this.hornearTexturaColorHD(tipo);
			this.texturasHaloColorCono[tipo.ordinal()] = this.hornearTexturaColorConoHD(tipo);
		}
	}

	// =========================================================================
	// === PRE-HORNEADO PROCEDURAL DE GRADIENTES
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ PRE-HORNEAR LOS HALOS DE LUZ?
	 * -------------------------------------------------------------------------
	 * Calcular 'new RadialGradientPaint()' en cada frame para 30 antorchas obliga a
	 * la CPU a calcular raíces cuadradas y miles de colores por segundo, generando
	 * lag masivo y toneladas de basura en el Garbage Collector.
	 *
	 * Al "hornear" (dibujar una sola vez en el inicio) las texturas cuadradas
	 * suaves: 1. Durante el juego la GPU solo hace 'gLight.drawImage(...)', lo cual
	 * es ultra-rápido. 2. Cero consumo de CPU en cálculo de degradados y 0 bytes de
	 * basura en memoria.
	 * =========================================================================
	 */
	private BufferedImage hornearTexturaMascaraHD() {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final float[] fracciones = { 0.0f, 0.50f, 1.0f };
		final Color[] colores = { new Color(255, 255, 255, 255), // Centro: 100% perforación de penumbra
				new Color(255, 255, 255, 160), // Penumbra suave intermedia
				new Color(255, 255, 255, 0) // Borde exterior
		};

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		g2d.fillOval(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD);
		g2d.dispose();
		return img;
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: CONO DIRECCIONAL PURO (SIN CÍRCULO DETRÁS)
	 * -------------------------------------------------------------------------
	 * Usamos 'Arc2D.PIE' centrado en (128, 128) con un ángulo de apertura de 85°.
	 * El gradiente radial hace que el vértice que nace en el pecho del jugador
	 * tenga 100% de luz y se difumine suavemente hacia el borde frontal.
	 * =========================================================================
	 */
	private BufferedImage hornearTexturaMascaraConoHD(final double anguloApertura) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final float[] fracciones = { 0.0f, 0.55f, 1.0f };
		final Color[] colores = { new Color(255, 255, 255, 255), // Vértice en el personaje (máxima claridad)
				new Color(255, 255, 255, 150), // Haz central
				new Color(255, 255, 255, 0) // Borde del haz
		};

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		final double inicioAngulo = -(anguloApertura / 2.0);
		g2d.fill(new Arc2D.Double(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD, inicioAngulo, anguloApertura,
				Arc2D.PIE));
		g2d.dispose();
		return img;
	}

	private BufferedImage hornearTexturaColorHD(final TipoLuz tipo) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final Color base = tipo.getColorLuz();
		final int aMax = (int) (tipo.getIntensidad() * 255.0f);
		final float[] fracciones = { 0.0f, 0.40f, 1.0f };
		final Color[] colores = { new Color(base.getRed(), base.getGreen(), base.getBlue(), aMax),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), (int) (aMax * 0.45f)),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), 0) };

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		g2d.fillOval(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD);
		g2d.dispose();
		return img;
	}

	private BufferedImage hornearTexturaColorConoHD(final TipoLuz tipo) {
		final BufferedImage img = new BufferedImage(RESOLUCION_HALO_HD, RESOLUCION_HALO_HD,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = RESOLUCION_HALO_HD / 2.0f;
		final Color base = tipo.getColorLuz();
		final int aMax = (int) (tipo.getIntensidad() * 255.0f);
		final float[] fracciones = { 0.0f, 0.45f, 1.0f };
		final Color[] colores = { new Color(base.getRed(), base.getGreen(), base.getBlue(), aMax),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), (int) (aMax * 0.40f)),
				new Color(base.getRed(), base.getGreen(), base.getBlue(), 0) };

		g2d.setPaint(new RadialGradientPaint(centro, centro, centro, fracciones, colores));
		final double inicioAngulo = -(tipo.getAnguloAperturaGrados() / 2.0);
		g2d.fill(new Arc2D.Double(0, 0, RESOLUCION_HALO_HD, RESOLUCION_HALO_HD, inicioAngulo,
				tipo.getAnguloAperturaGrados(), Arc2D.PIE));
		g2d.dispose();
		return img;
	}

	// =========================================================================
	// === POOL Y GESTIÓN DE LUCES (ZERO-GC)
	// =========================================================================

	public FuenteLuz agregarLuzEstatica(final double x, final double y, final TipoLuz tipo) {
		return this.agregarLuzEstatica(x, y, tipo, tipo.getRadioBase());
	}

	public FuenteLuz agregarLuzEstatica(final double x, final double y, final TipoLuz tipo, final double radio) {
		if (this.topePila == 0) {
			return null;
		}
		final int indice = this.indicesLibres[--this.topePila];
		final FuenteLuz luz = this.pool[indice];
		luz.spawnFija(x, y, tipo, radio);
		this.activas[this.cantidadActivas++] = luz;
		return luz;
	}

	public FuenteLuz agregarLuzAnclada(final Ente ente, final TipoLuz tipo) {
		return this.agregarLuzAnclada(ente, tipo, (tipo != null) ? tipo.getRadioBase() : 100.0);
	}

	/**
	 * Ancla una fuente de luz a una entidad móvil con deduplicación automática y
	 * sincronización bidireccional.
	 *
	 * @param ente  Entidad a seguir.
	 * @param tipo  Preset de iluminación.
	 * @param radio Radio en píxeles deseado.
	 * @return Referencia a la {@link FuenteLuz} asignada.
	 */
	public FuenteLuz agregarLuzAnclada(final Ente ente, final TipoLuz tipo, final double radio) {
		if (ente == null) {
			return null;
		}

		// 1. Si la entidad ya poseía una luz activa, se actualiza in-situ
		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz l = this.activas[i];
			if (l.getEnteAnclado() == ente) {
				l.spawnAnclada(ente, tipo, radio);
				return l;
			}
		}

		// 2. Si no tenía luz, se extrae una ranura del pool
		if (this.topePila == 0) {
			return null;
		}
		final int indice = this.indicesLibres[--this.topePila];
		final FuenteLuz luz = this.pool[indice];
		luz.spawnAnclada(ente, tipo, radio);
		this.activas[this.cantidadActivas++] = luz;
		return luz;
	}

	public FuenteLuz dispararFlashPosicional(final double x, final double y, final double radio,
			final double duracion) {
		return this.dispararFlashPosicional(x, y, TipoLuz.DESTELLO_EXPLOSION, radio, duracion);
	}

	public FuenteLuz dispararFlashPosicional(final double x, final double y, final TipoLuz tipo, final double radio,
			final double duracion) {
		if (this.topePila == 0) {
			return null;
		}
		final int indice = this.indicesLibres[--this.topePila];
		final FuenteLuz luz = this.pool[indice];
		luz.spawnTemporal(x, y, tipo, radio, duracion);
		this.activas[this.cantidadActivas++] = luz;
		return luz;
	}

	public void dispararFlashGlobal(final double duracionSegundos, final boolean esRelampago) {
		this.flashGlobalActivo = true;
		this.duracionFlashGlobal = Math.max(0.05, duracionSegundos);
		this.tiempoFlashGlobalRestante = this.duracionFlashGlobal;
		this.flashGlobalRelampago = esRelampago;
	}

	public void removerLuzDe(final Ente ente) {
		if (ente == null) {
			return;
		}
		for (int i = 0; i < this.cantidadActivas; i++) {
			if (this.activas[i].getEnteAnclado() == ente) {
				this.activas[i].apagar();
				break;
			}
		}
	}

	/**
	 * Apaga todas las luces activas y reintegra los índices al pool (usado al
	 * cambiar de mapa).
	 */
	public void apagarTodasLasLuces() {
		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luz = this.activas[i];
			luz.apagar();
			this.indicesLibres[this.topePila++] = luz.getIndicePool();
			this.activas[i] = null;
		}
		this.cantidadActivas = 0;
	}

	// =========================================================================
	// === CONSULTAS DE SIGILO E INTELIGENCIA ARTIFICIAL (IA QUERY O(1))
	// =========================================================================

	/**
	 * Evalúa si una coordenada puntual del mundo se encuentra iluminada por el sol
	 * o por una luz cercana.
	 *
	 * @param mundoX Coordenada X absoluta de mundo.
	 * @param mundoY Coordenada Y absoluta de mundo.
	 * @return {@code true} si el punto recibe luz visible.
	 */
	public boolean isPosicionIluminada(final double mundoX, final double mundoY) {
		if (!this.modoAmbienteFijo && (this.ciclo.getColorAmbienteActual().getAlpha() < 50)) {
			return true;
		}
		if (this.flashGlobalActivo) {
			return true;
		}

		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luz = this.activas[i];
			final double dx = mundoX - luz.getPosX();
			final double dy = mundoY - luz.getPosY();
			final double distSq = (dx * dx) + (dy * dy);
			final double radio = luz.getRadioActual();

			if (distSq <= (radio * radio)) {
				if (!luz.getTipo().isEsCono()) {
					return true;
				}
				final double anguloPunto = Math.atan2(dy, dx);
				double diff = Math.abs(anguloPunto - luz.getAnguloRotacion());
				while (diff > Math.PI) {
					diff = Math.abs(diff - (Math.PI * 2.0));
				}

				final double semiApertura = Math.toRadians(luz.getTipo().getAnguloAperturaGrados() / 2.0);
				if (diff <= semiApertura) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Retorna el nivel relativo de claridad en un punto (0.0 = oscuridad pura, 1.0
	 * = claridad plena).
	 */
	public float getNivelLuzEn(final double mundoX, final double mundoY) {
		if (this.isPosicionIluminada(mundoX, mundoY)) {
			return 1.0f;
		}
		final Color c = this.modoAmbienteFijo ? this.colorAmbienteFijo : this.ciclo.getColorAmbienteActual();
		return 1.0f - (c.getAlpha() / 255.0f);
	}

	public int getAlphaOscuridadActual() {
		if (this.modoAmbienteFijo) {
			return this.colorAmbienteFijo.getAlpha();
		}
		return this.ciclo.getColorAmbienteActual().getAlpha();
	}

	// =========================================================================
	// === AMBIENTES Y TRANSICIONES
	// =========================================================================

	public void establecerModoCueva(final boolean total) {
		this.establecerAmbienteTransicion(total ? new Color(0, 0, 0, 255) : new Color(10, 15, 30, 235), 1.2);
	}

	public void restablecerModoExterior() {
		this.iluminacionHabilitada = true;
		this.modoAmbienteFijo = false;
		this.transicionActiva = false;
		this.colorTinteBioma = null;
		this.factorInmersionBioma = 0.0;
	}

	public void establecerAmbienteTransicion(final Color destino, final double duracion) {
		this.iluminacionHabilitada = true;
		this.colorTransicionOrigen = this.modoAmbienteFijo ? this.colorAmbienteFijo
				: this.ciclo.getColorAmbienteActual();
		this.colorTransicionDestino = (destino != null) ? destino : new Color(0, 0, 0, 255);
		this.modoAmbienteFijo = true;
		this.tiempoTransicionTotal = Math.max(0.1, duracion);
		this.tiempoTransicionActual = 0.0;
		this.transicionActiva = true;
	}

	public void setTinteBiomaExterior(final Color tinte, final double factorInmersion) {
		this.colorTinteBioma = tinte;
		this.factorInmersionBioma = factorInmersion;
	}

	// =========================================================================
	// === CICLO LÓGICO CON UPDATE FRUSTUM CULLING (60 APS)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: UPDATE FRUSTUM CULLING EN COORDENADAS DE MUNDO
	 * ------------------------------------------------------------------------- Si
	 * tienes 256 antorchas en el mapa, calcular 'Math.sin()' y parpadeo para todas
	 * en cada tick consume CPU innecesariamente.
	 *
	 * 1. Calculamos el área rectangular del mundo que la cámara está viendo
	 * (compensando zoom final, rotación, temblor y un margen de seguridad).
	 *
	 * 2. Si una luz está fuera de este rectángulo: - Si está anclada a un ente,
	 * actualizamos su posición para que nunca quede desfasada. - Si el ente murió
	 * fuera de pantalla, apagamos la luz de inmediato. - Omitimos los cálculos
	 * trigonométricos de la llama, ahorrando CPU al 100%.
	 * =========================================================================
	 */
	public void actualizar() {
		if (!this.iluminacionHabilitada) {
			return;
		}

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		if (!this.modoAmbienteFijo) {
			this.ciclo.actualizar(dt);
		} else if (this.transicionActiva) {
			this.tiempoTransicionActual += dt;
			final double factor = Math.min(1.0, this.tiempoTransicionActual / this.tiempoTransicionTotal);

			final int r = (int) (this.colorTransicionOrigen.getRed()
					+ ((this.colorTransicionDestino.getRed() - this.colorTransicionOrigen.getRed()) * factor));
			final int g = (int) (this.colorTransicionOrigen.getGreen()
					+ ((this.colorTransicionDestino.getGreen() - this.colorTransicionOrigen.getGreen()) * factor));
			final int b = (int) (this.colorTransicionOrigen.getBlue()
					+ ((this.colorTransicionDestino.getBlue() - this.colorTransicionOrigen.getBlue()) * factor));
			final int a = (int) (this.colorTransicionOrigen.getAlpha()
					+ ((this.colorTransicionDestino.getAlpha() - this.colorTransicionOrigen.getAlpha()) * factor));

			this.colorAmbienteFijo = new Color(r, g, b, a);
			if (factor >= 1.0) {
				this.transicionActiva = false;
			}
		}

		// Cálculo del cuadro envolvente de visión de la cámara en píxeles de mundo
		final double zoomActivo = (Globales.CAMARA != null) ? Math.max(0.2, Globales.CAMARA.getZoomFinal()) : 1.0;
		final double rotAbs = (Globales.CAMARA != null)
				? Math.abs(Globales.CAMARA.getGestorEfectos().getAnguloRotacion())
				: 0.0;
		final double shakeX = (Globales.CAMARA != null) ? Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetX())
				: 0.0;
		final double shakeY = (Globales.CAMARA != null) ? Math.abs(Globales.CAMARA.getGestorEfectos().getOffsetY())
				: 0.0;

		final double cos = Math.cos(rotAbs);
		final double sin = Math.sin(rotAbs);

		final int radioVisibleX = (int) Math
				.ceil(((Constantes.CENTROX * cos) + (Constantes.CENTROY * sin)) / zoomActivo) + (int) shakeX
				+ MARGEN_CULLING_MUNDO;
		final int radioVisibleY = (int) Math
				.ceil(((Constantes.CENTROX * sin) + (Constantes.CENTROY * cos)) / zoomActivo) + (int) shakeY
				+ MARGEN_CULLING_MUNDO;

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		final int minMundoX = camX - radioVisibleX;
		final int maxMundoX = camX + radioVisibleX;
		final int minMundoY = camY - radioVisibleY;
		final int maxMundoY = camY + radioVisibleY;

		// Bucle de actualización con Update Frustum Culling
		int i = 0;
		while (i < this.cantidadActivas) {
			final FuenteLuz luz = this.activas[i];

			/*
			 * Sincronización continua de entidades: Si la luz sigue a un Ente, actualizamos
			 * su posición y validamos si murió, garantizando que nunca quede atrapada en
			 * coordenadas viejas fuera de pantalla.
			 */
			if (luz.getEnteAnclado() != null) {
				if (luz.getEnteAnclado().estaEliminado()) {
					luz.apagar();
					this.indicesLibres[this.topePila++] = luz.getIndicePool();
					this.activas[i] = this.activas[this.cantidadActivas - 1];
					this.activas[this.cantidadActivas - 1] = null;
					this.cantidadActivas--;
					continue;
				}
				luz.actualizarPosicionEnte();
			}

			// Descarte temprano de físicas pesadas para luces fuera de pantalla
			final double rLuz = luz.getRadioActual();
			if (((luz.getPosX() + rLuz) < minMundoX) || ((luz.getPosX() - rLuz) > maxMundoX)
					|| ((luz.getPosY() + rLuz) < minMundoY) || ((luz.getPosY() - rLuz) > maxMundoY)) {
				i++;
				continue;
			}

			luz.actualizar(dt);

			if (luz.isActiva()) {
				i++;
			} else {
				// Eliminación Swap-and-Pop en tiempo O(1)
				this.indicesLibres[this.topePila++] = luz.getIndicePool();
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
			}
		}

		if (this.flashGlobalActivo) {
			this.tiempoFlashGlobalRestante -= dt;
			if (this.tiempoFlashGlobalRestante <= 0.0) {
				this.flashGlobalActivo = false;
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO EN VRAM (LIGHTMAP + CONOS)
	// =========================================================================

	private void verificarLightmap(final Graphics2D g) {
		if ((this.lightmap == null) || (this.lightmap.getWidth() != Constantes.ANCHO_JUEGO)
				|| (this.lightmap.getHeight() != Constantes.ALTO_JUEGO)
				|| (this.lightmap.validate(g.getDeviceConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE)) {

			if (this.lightmap != null) {
				this.lightmap.flush();
			}
			this.lightmap = g.getDeviceConfiguration().createCompatibleVolatileImage(Constantes.ANCHO_JUEGO,
					Constantes.ALTO_JUEGO, Transparency.TRANSLUCENT);
		}
	}

	/**
	 * Renderiza la máscara de sombras y perforaciones de luz adaptadas a la cámara
	 * activa.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.iluminacionHabilitada) {
			return;
		}

		int rBase, gBase, bBase, aBase;
		if (this.modoAmbienteFijo) {
			rBase = this.colorAmbienteFijo.getRed();
			gBase = this.colorAmbienteFijo.getGreen();
			bBase = this.colorAmbienteFijo.getBlue();
			aBase = this.colorAmbienteFijo.getAlpha();
		} else if ((this.colorTinteBioma != null) && (this.factorInmersionBioma > 0.0)) {
			final Color solar = this.ciclo.getColorAmbienteActual();
			final double f = this.factorInmersionBioma;
			rBase = (int) (solar.getRed() + ((this.colorTinteBioma.getRed() - solar.getRed()) * f * 0.6));
			gBase = (int) (solar.getGreen() + ((this.colorTinteBioma.getGreen() - solar.getGreen()) * f * 0.6));
			bBase = (int) (solar.getBlue() + ((this.colorTinteBioma.getBlue() - solar.getBlue()) * f * 0.6));
			aBase = Math.max(solar.getAlpha(), (int) (this.colorTinteBioma.getAlpha() * f));
		} else {
			final Color solar = this.ciclo.getColorAmbienteActual();
			rBase = solar.getRed();
			gBase = solar.getGreen();
			bBase = solar.getBlue();
			aBase = solar.getAlpha();
		}

		final double factorFlash = (this.flashGlobalActivo && (this.duracionFlashGlobal > 0.0))
				? (this.tiempoFlashGlobalRestante / this.duracionFlashGlobal)
				: 0.0;

		final int alphaSombra = (int) Math.round(aBase * (1.0 - factorFlash));

		// Descarte temprano si no hay sombras ni relámpagos que dibujar (0% GPU)
		if ((alphaSombra <= 0) && (!this.flashGlobalActivo || !this.flashGlobalRelampago)) {
			return;
		}

		this.verificarLightmap(g);

		do {
			final int val = this.lightmap.validate(g.getDeviceConfiguration());
			if (val == VolatileImage.IMAGE_INCOMPATIBLE) {
				this.verificarLightmap(g);
			}

			final Graphics2D gLight = this.lightmap.createGraphics();
			try {
				// 1. Vaciado total de VRAM
				gLight.setComposite(COMPOSITE_LIMPIEZA);
				gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);

				// 2. Estampar la oscuridad ambiental con Dirty-Flag (Zero-GC)
				if (alphaSombra > 0) {
					if ((rBase != this.lastBaseR) || (gBase != this.lastBaseG) || (bBase != this.lastBaseB)
							|| (alphaSombra != this.lastBaseA)) {
						this.lastBaseR = rBase;
						this.lastBaseG = gBase;
						this.lastBaseB = bBase;
						this.lastBaseA = alphaSombra;
						this.colorAmbienteCalculado = new Color(rBase, gBase, bBase, alphaSombra);
					}
					gLight.setComposite(COMPOSITE_NORMAL);
					gLight.setColor(this.colorAmbienteCalculado);
					gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
				}

				// 3. Renderizado de Luces
				if (this.cantidadActivas > 0) {
					this.pintarLuces(gLight, alphaSombra);
				}

				// 4. Relámpago Blanco
				if (this.flashGlobalActivo && this.flashGlobalRelampago && (factorFlash > 0.5)) {
					final float aRel = (float) Math.min(0.80, (factorFlash - 0.5) / 0.5);
					gLight.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, aRel));
					gLight.setColor(Color.WHITE);
					gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
				}

			} finally {
				gLight.dispose();
			}

			// Estampar el Lightmap resultante en espacio de pantalla 1:1
			DibujoDebug.dibujarImagen(g, this.lightmap, 0, 0);

		} while (this.lightmap.contentsLost());
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: PROYECCIÓN ALINEADA CON EL CENTRO FOCAL DE CÁMARA
	 * ------------------------------------------------------------------------- En
	 * este motor, 'camX' es la esquina superior izquierda de la entidad enfocada.
	 * Para que el punto de luz coincida exactamente con el sprite dibujado en
	 * pantalla:
	 *
	 * Calculamos el centro focal en mundo sumando medio ancho y alto del personaje:
	 * centroMundoCamX = camX + (enteAncho / 2)
	 *
	 * Al restar 'luz.posX - centroMundoCamX', el desplazamiento relativo 'dx' vale
	 * 0 cuando la luz está en el pecho del jugador, proyectándose exactamente en
	 * CENTROX (320).
	 * =========================================================================
	 */
	private void pintarLuces(final Graphics2D gLight, final int alphaSombra) {
		final double z = (Globales.CAMARA != null) ? Globales.CAMARA.getZoomFinal() : 1.0;
		final double shakeX = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetX() : 0.0;
		final double shakeY = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetY() : 0.0;
		final double rotCam = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getAnguloRotacion() : 0.0;

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		// Dimensiones de la entidad enfocada para alinear el punto cero focal
		final int enteAncho = ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null))
				? Globales.CAMARA.getEntidadEnfocada().getAncho()
				: 0;
		final int enteAlto = ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null))
				? Globales.CAMARA.getEntidadEnfocada().getAlto()
				: 0;

		final double centroMundoCamX = camX + (enteAncho / 2.0);
		final double centroMundoCamY = camY + (enteAlto / 2.0);

		final double cos = Math.cos(rotCam);
		final double sin = Math.sin(rotCam);

		// =====================================================================
		// PASE A: PERFORACIÓN DE PENUMBRA (DST_OUT)
		// =====================================================================
		gLight.setComposite(COMPOSITE_PERFORAR);
		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luz = this.activas[i];
			final int radioPantalla = (int) Math.round(luz.getRadioActual() * z);
			final int diametro = radioPantalla * 2;

			final double dx = (luz.getPosX() - centroMundoCamX) * z;
			final double dy = (luz.getPosY() - centroMundoCamY) * z;
			final double rx = (dx * cos) - (dy * sin);
			final double ry = (dx * sin) + (dy * cos);

			final int screenX = (int) Math.round(Constantes.CENTROX + shakeX + rx) - radioPantalla;
			final int screenY = (int) Math.round(Constantes.CENTROY + shakeY + ry) - radioPantalla;

			// Frustum Culling en Pantalla
			if (((screenX + diametro) < 0) || (screenX > Constantes.ANCHO_JUEGO) || ((screenY + diametro) < 0)
					|| (screenY > Constantes.ALTO_JUEGO)) {
				continue;
			}

			if (luz.getTipo().isEsCono()) {
				final double rotTotal = luz.getAnguloRotacion() + rotCam;
				final int cx = screenX + radioPantalla;
				final int cy = screenY + radioPantalla;
				gLight.rotate(rotTotal, cx, cy);
				gLight.drawImage(this.texturaMascaraConoHD, screenX, screenY, diametro, diametro, null);
				gLight.rotate(-rotTotal, cx, cy);
			} else {
				gLight.drawImage(this.texturaMascaraAlphaHD, screenX, screenY, diametro, diametro, null);
			}
		}

		// =====================================================================
		// PASE B: TINTE DE COLOR CON ATENUACIÓN DIURNA (ZERO-GC)
		// =========================================================================
		final int indiceCompositeTinte = Math.max(0, Math.min(10, (int) Math.round((alphaSombra / 200.0) * 10.0)));

		if (indiceCompositeTinte > 0) {
			gLight.setComposite(COMPOSITES_TINTE_ATENUADO[indiceCompositeTinte]);

			for (int i = 0; i < this.cantidadActivas; i++) {
				final FuenteLuz luz = this.activas[i];
				final int radioPantalla = (int) Math.round(luz.getRadioActual() * z);
				final int diametro = radioPantalla * 2;

				final double dx = (luz.getPosX() - centroMundoCamX) * z;
				final double dy = (luz.getPosY() - centroMundoCamY) * z;
				final double rx = (dx * cos) - (dy * sin);
				final double ry = (dx * sin) + (dy * cos);

				final int screenX = (int) Math.round(Constantes.CENTROX + shakeX + rx) - radioPantalla;
				final int screenY = (int) Math.round(Constantes.CENTROY + shakeY + ry) - radioPantalla;

				if (((screenX + diametro) < 0) || (screenX > Constantes.ANCHO_JUEGO) || ((screenY + diametro) < 0)
						|| (screenY > Constantes.ALTO_JUEGO)) {
					continue;
				}

				if (luz.getTipo().isEsCono()) {
					final double rotTotal = luz.getAnguloRotacion() + rotCam;
					final int cx = screenX + radioPantalla;
					final int cy = screenY + radioPantalla;
					gLight.rotate(rotTotal, cx, cy);
					gLight.drawImage(this.texturasHaloColorCono[luz.getTipo().ordinal()], screenX, screenY, diametro,
							diametro, null);
					gLight.rotate(-rotTotal, cx, cy);
				} else {
					gLight.drawImage(this.texturasHaloColor[luz.getTipo().ordinal()], screenX, screenY, diametro,
							diametro, null);
				}
			}
		}
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public CicloDiaNoche getCiclo() {
		return this.ciclo;
	}

	public int getCantidadActivas() {
		return this.cantidadActivas;
	}

	public void setIluminacionHabilitada(final boolean h) {
		this.iluminacionHabilitada = h;
	}

	public boolean isIluminacionHabilitada() {
		return this.iluminacionHabilitada;
	}

	public boolean isModoAmbienteFijo() {
		return this.modoAmbienteFijo;
	}

	public Color getColorAmbienteFijo() {
		return this.colorAmbienteFijo;
	}
}