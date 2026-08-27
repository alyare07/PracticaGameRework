package principal.iluminacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

/**
 * Gestor maestro de iluminación dinámica 2D, ciclo día/noche y máscaras de
 * sombra aceleradas por hardware en GPU (Lightmaps en VRAM).
 * <p>
 * <b>Pilares de Arquitectura y Rendimiento:</b>
 * <ul>
 * <li><b>Máscara en Espacio de Pantalla (Screen-Space Lightmap):</b> La
 * oscuridad cubre exactamente la resolución lógica ($640 \times 360$) en una
 * {@link VolatileImage} transparente. Al proyectarse en espacio de pantalla
 * (1:1), cubre de esquina a esquina el monitor sin dejar franjas claras ni
 * recuadros negros en ningún nivel de zoom.</li>
 * <li><b>Gradientes Radiales Pre-Horneados:</b> Genera texturas circulares
 * difuminadas <i>una sola vez</i> al iniciar el juego en
 * {@link #texturasHaloPrehorneadas}, eliminando la costosa rasterización de
 * {@link RadialGradientPaint} en el Game Loop.</li>
 * <li><b>Perforación Óptica por {@code AlphaComposite.DST_OUT}:</b> Los halos
 * de luz restan la opacidad de la máscara de sombra en VRAM, revelando el mundo
 * subyacente con bordes suaves de penumbra sin alterar los colores del
 * terreno.</li>
 * <li><b>Proyección Angular y Zoom O(1):</b> Modula la posición y radio de las
 * luces en pantalla proyectando el temblor, zoom y rotación ($\theta$) con
 * fórmulas primitivas directas.</li>
 * <li><b>Deduplicación Automática de Entidades:</b> Si se vuelve a anclar una
 * luz a un {@link Ente} que ya tenía una, actualiza sus propiedades en el lugar
 * sin duplicar ranuras.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 3.5
 */
public class GestorLuz {

	// =========================================================================
	// === 1. CAPACIDAD Y CONSTANTES DE COMPOSICIÓN GRÁFICA (ZERO-GC)
	// =========================================================================

	/** Capacidad máxima de fuentes de luz simultáneas activas en el mundo. */
	private static final int CAPACIDAD_LUCES = 128;

	/** Composite pre-instanciado para vaciar la VRAM a transparencia pura. */
	private static final AlphaComposite COMPOSITE_LIMPIEZA = AlphaComposite.getInstance(AlphaComposite.CLEAR);

	/** Composite estándar para estampar la sombra o tinte ambiental. */
	private static final AlphaComposite COMPOSITE_NORMAL = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

	/**
	 * Composite DST_OUT: El canal Alpha de la luz resta el canal Alpha de la
	 * sombra, "perforando" un agujero transparente que deja ver el mapa iluminado
	 * debajo.
	 */
	private static final AlphaComposite COMPOSITE_PERFORAR = AlphaComposite.getInstance(AlphaComposite.DST_OUT);

	// =========================================================================
	// === 2. POOL DE LUCES Y CONTROLADORES
	// =========================================================================

	/** Pool maestro de instancias pre-asignadas para cero recolección de basura. */
	private final FuenteLuz[] pool;

	/** Arreglo contiguo de luces activas para iteración rápida de CPU. */
	private final FuenteLuz[] activas;

	/** Cantidad de luces actualmente encendidas y procesándose en el frame. */
	private int cantidadActivas;

	/** Puntero circular para reciclaje instantáneo en O(1). */
	private int punteroCircular;

	/**
	 * Controlador del reloj solar de 24 horas y transiciones de color ambiental.
	 */
	private final CicloDiaNoche ciclo;

	/**
	 * Framebuffer secundario en VRAM donde se dibuja la máscara de sombras y luces.
	 */
	private VolatileImage lightmap;

	/**
	 * Texturas de halo circular pre-generadas en memoria indexadas por
	 * {@link TipoLuz#ordinal()}.
	 */
	private final BufferedImage[] texturasHaloPrehorneadas;

	// =========================================================================
	// === 3. MODOS DE MAPA Y CONFIGURACIÓN AMBIENTAL
	// =========================================================================

	/**
	 * Interruptor maestro: si es false, no se procesa ni se dibuja ninguna sombra.
	 */
	private boolean iluminacionHabilitada = true;

	/** Indica si el mapa actual tiene un ambiente estático (ej: cueva sin sol). */
	private boolean modoAmbienteFijo = false;

	/** Color ambiental fijo utilizado en cuevas, interiores o biomas especiales. */
	private Color colorAmbienteFijo = new Color(0, 0, 0, 255);

	// =========================================================================
	// === CONSTRUCTOR: INICIALIZACIÓN Y PRE-HORNEADO DE GRADIENTES
	// =========================================================================

	/**
	 * Inicializa el pool de luces, el cronómetro solar y pre-calcula los gradientes
	 * radiales en texturas de memoria estática.
	 */
	public GestorLuz() {
		this.pool = new FuenteLuz[CAPACIDAD_LUCES];
		this.activas = new FuenteLuz[CAPACIDAD_LUCES];
		this.cantidadActivas = 0;
		this.punteroCircular = 0;
		this.ciclo = new CicloDiaNoche();

		// 1. Pre-instanciación de todas las ranuras del pool
		for (int i = 0; i < CAPACIDAD_LUCES; i++) {
			this.pool[i] = new FuenteLuz();
		}

		// 2. Pre-horneado de gradientes circulares para cada tipo de luz
		final int totalTipos = TipoLuz.values().length;
		this.texturasHaloPrehorneadas = new BufferedImage[totalTipos];

		for (final TipoLuz tipo : TipoLuz.values()) {
			this.texturasHaloPrehorneadas[tipo.ordinal()] = this.hornearTexturaHalo(tipo);
		}
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: ¿POR QUÉ PRE-HORNEAR LOS GRADIENTES RADIALES?
	 * ------------------------------------------------------------------------- En
	 * Java 2D, calcular 'new RadialGradientPaint()' en cada fotograma para 20
	 * antorchas obliga a la CPU a calcular raíces cuadradas y gradientes de color
	 * miles de veces por segundo, generando lag y miles de objetos en el Heap.
	 * 
	 * Al "hornear" (dibujar una sola vez en el arranque) una imagen cuadrada suave
	 * con el círculo difuminado: 1. Durante el juego solo hacemos
	 * 'g.drawImage(texturaPrehorneada, ...)'. 2. La GPU dibuja y escala esa imagen
	 * a la velocidad de la luz en VRAM. 3. 0 cálculos pesados y 0 basura en
	 * memoria.
	 * =========================================================================
	 */
	/**
	 * Dibuja una textura cuadrada con un gradiente radial perfecto de blanco a
	 * transparente.
	 *
	 * @param tipo Preset de luz a hornear.
	 * @return {@link BufferedImage} pre-calculada en memoria.
	 */
	private BufferedImage hornearTexturaHalo(final TipoLuz tipo) {
		final int diametro = (tipo.getRadioBase() + (int) tipo.getAmplitudParpadeo() + 10) * 2;
		final BufferedImage img = new BufferedImage(diametro, diametro, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = img.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float centro = diametro / 2.0f;
		final float[] fracciones = { 0.0f, 0.45f, 1.0f };
		final Color[] colores = { new Color(255, 255, 255, 255), // Centro: 100% de borrado de oscuridad (luz plena)
				new Color(255, 255, 255, 170), // Penumbra suave intermedia
				new Color(255, 255, 255, 0) // Borde exterior: no borra nada de sombra
		};

		final RadialGradientPaint gradiente = new RadialGradientPaint(centro, centro, centro, fracciones, colores);
		g2d.setPaint(gradiente);
		g2d.fillOval(0, 0, diametro, diametro);
		g2d.dispose();

		return img;
	}

	// =========================================================================
	// === MÉTODOS DE CONFIGURACIÓN POR MAPAS (API PÚBLICA)
	// =========================================================================

	/**
	 * Habilita o deshabilita por completo la capa de iluminación.
	 * <p>
	 * <b>Uso:</b> Tiendas, menús o interiores iluminados uniformemente.
	 * </p>
	 *
	 * @param habilitada {@code true} para activar, {@code false} para apagar toda
	 *                   sombra.
	 */
	public void setIluminacionHabilitada(final boolean habilitada) {
		this.iluminacionHabilitada = habilitada;
	}

	public boolean isIluminacionHabilitada() {
		return this.iluminacionHabilitada;
	}

	/**
	 * Configura el mapa como Cueva o Mazmorra subterránea:
	 * <ul>
	 * <li>Fija la oscuridad ambiental a negro permanente (o penumbra tenue).</li>
	 * <li>Desconecta el ciclo solar (el sol exterior no altera la cueva).</li>
	 * </ul>
	 *
	 * @param oscuridadTotal {@code true} para negro azabache puro (255),
	 *                       {@code false} para penumbra azulada.
	 */
	public void establecerModoCueva(final boolean oscuridadTotal) {
		this.iluminacionHabilitada = true;
		this.modoAmbienteFijo = true;
		this.colorAmbienteFijo = oscuridadTotal ? new Color(0, 0, 0, 255) : new Color(10, 15, 30, 235);
	}

	/**
	 * Fija un color ambiental personalizado independiente de la hora.
	 * <p>
	 * <b>Uso:</b> Pantano con niebla verde, bioma volcánico con resplandor rojo.
	 * </p>
	 *
	 * @param color Color con canal Alpha para la niebla/oscuridad del mapa.
	 */
	public void establecerAmbienteFijo(final Color color) {
		this.iluminacionHabilitada = true;
		this.modoAmbienteFijo = true;
		this.colorAmbienteFijo = (color != null) ? color : new Color(0, 0, 0, 255);
	}

	/**
	 * Restaura el comportamiento natural de exterior (el sol y el reloj de 24h
	 * controlan la luz).
	 */
	public void restablecerModoExterior() {
		this.iluminacionHabilitada = true;
		this.modoAmbienteFijo = false;
	}

	/**
	 * Conmuta entre noche con tinte azulado atmosférico o noche 100% negra
	 * (Blackout).
	 *
	 * @param blackout {@code true} para noche negra total, {@code false} para noche
	 *                 estándar.
	 */
	public void setOscuridadTotalNoche(final boolean blackout) {
		this.ciclo.setModoOscuridadTotal(blackout);
	}

	// =========================================================================
	// === MÉTODOS DE DISPARO Y GESTIÓN DE LUCES
	// =========================================================================

	public FuenteLuz agregarLuzEstatica(final double x, final double y, final TipoLuz tipo) {
		return this.agregarLuzEstatica(x, y, tipo, tipo.getRadioBase());
	}

	public FuenteLuz agregarLuzEstatica(final double x, final double y, final TipoLuz tipo,
			final double radioPersonalizado) {
		final FuenteLuz luz = this.pool[this.punteroCircular];
		this.punteroCircular = (this.punteroCircular + 1) % CAPACIDAD_LUCES;

		final boolean yaEstaba = luz.isActiva();
		luz.spawnFija(x, y, tipo, radioPersonalizado);

		if (!yaEstaba && (this.cantidadActivas < CAPACIDAD_LUCES)) {
			this.activas[this.cantidadActivas] = luz;
			this.cantidadActivas++;
		}
		return luz;
	}

	public FuenteLuz agregarLuzAnclada(final Ente ente, final TipoLuz tipo) {
		return this.agregarLuzAnclada(ente, tipo, (tipo != null) ? tipo.getRadioBase() : 100.0);
	}

	/**
	 * Ancla una luz a una entidad móvil con radio personalizado y deduplicación
	 * automática.
	 *
	 * @param ente               Entidad a la que se vinculará la luz.
	 * @param tipo               Preset de iluminación.
	 * @param radioPersonalizado Radio en píxeles deseado.
	 * @return Referencia a la {@link FuenteLuz} activa.
	 */
	public FuenteLuz agregarLuzAnclada(final Ente ente, final TipoLuz tipo, final double radioPersonalizado) {
		if (ente == null) {
			return null;
		}

		// 1. Verificamos si este Ente ya posee una luz activa para actualizarla sin
		// duplicar
		for (int i = 0; i < this.cantidadActivas; i++) {
			final FuenteLuz luzExistente = this.activas[i];
			if (luzExistente.getEnteAnclado() == ente) {
				luzExistente.spawnAnclada(ente, tipo, radioPersonalizado);
				return luzExistente;
			}
		}

		// 2. Si no tenía luz previa, tomamos un nuevo slot del pool circular
		final FuenteLuz luz = this.pool[this.punteroCircular];
		this.punteroCircular = (this.punteroCircular + 1) % CAPACIDAD_LUCES;

		final boolean yaEstaba = luz.isActiva();
		luz.spawnAnclada(ente, tipo, radioPersonalizado);

		if (!yaEstaba && (this.cantidadActivas < CAPACIDAD_LUCES)) {
			this.activas[this.cantidadActivas] = luz;
			this.cantidadActivas++;
		}
		return luz;
	}

	/**
	 * Apaga y desvincula inmediatamente la luz de una entidad específica (ej: al
	 * morir).
	 *
	 * @param ente Entidad cuya luz se desea remover.
	 */
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
	 * Apaga todas las luces activas y vacía la lista contigua (ej: al cambiar de
	 * mapa).
	 */
	public void apagarTodasLasLuces() {
		for (int i = 0; i < this.cantidadActivas; i++) {
			this.activas[i].apagar();
			this.activas[i] = null;
		}
		this.cantidadActivas = 0;
	}

	// =========================================================================
	// === CICLO DE VIDA Y RENDERIZADO DEL LIGHTMAP (60 APS)
	// =========================================================================

	/**
	 * Actualiza el reloj solar y el parpadeo físico de todas las luces activas.
	 */
	public void actualizar() {
		if (!this.iluminacionHabilitada) {
			return;
		}

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		// Si estamos en exteriores, avanza el reloj solar de 24h
		if (!this.modoAmbienteFijo) {
			this.ciclo.actualizar(dt);
		}

		int i = 0;
		while (i < this.cantidadActivas) {
			final FuenteLuz luz = this.activas[i];
			luz.actualizar(dt);

			if (luz.isActiva()) {
				i++;
			} else {
				// Eliminación Swap-and-Pop O(1)
				this.activas[i] = this.activas[this.cantidadActivas - 1];
				this.activas[this.cantidadActivas - 1] = null;
				this.cantidadActivas--;
			}
		}
	}

	/**
	 * Valida la existencia del Lightmap en memoria de video (VRAM).
	 */
	private void verificarLightmap(final Graphics2D g) {
		if ((this.lightmap == null) || (this.lightmap.getWidth() != Constantes.ANCHO_JUEGO)
				|| (this.lightmap.getHeight() != Constantes.ALTO_JUEGO)
				|| (this.lightmap.validate(g.getDeviceConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE)) {

			if (this.lightmap != null) {
				this.lightmap.flush();
			}

			// Creamos la textura en VRAM con soporte para canal Alpha transparente
			this.lightmap = g.getDeviceConfiguration().createCompatibleVolatileImage(Constantes.ANCHO_JUEGO,
					Constantes.ALTO_JUEGO, Transparency.TRANSLUCENT);
		}
	}

	/**
	 * Dibuja la máscara de iluminación cubriendo el 100% de la pantalla lógica,
	 * adaptando automáticamente la posición y tamaño de las luces a cualquier nivel
	 * de zoom.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.iluminacionHabilitada) {
			return;
		}

		// Seleccionamos el color ambiental (Fijo para cuevas o Dinámico para
		// exteriores)
		final Color colorAmbiente = this.modoAmbienteFijo ? this.colorAmbienteFijo
				: this.ciclo.getColorAmbienteActual();

		// Si es pleno día soleado (Alpha = 0), no hay sombras que dibujar (0% de uso de
		// GPU)
		if (colorAmbiente.getAlpha() <= 0) {
			return;
		}

		this.verificarLightmap(g);

		// Obtenemos los parámetros de cámara activos
		final double z = (Globales.CAMARA != null) ? Globales.CAMARA.getZoomFinal() : 1.0;
		final double shakeX = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetX() : 0.0;
		final double shakeY = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getOffsetY() : 0.0;
		final double rot = (Globales.CAMARA != null) ? Globales.CAMARA.getGestorEfectos().getAnguloRotacion() : 0.0;

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		final double cos = Math.cos(rot);
		final double sin = Math.sin(rot);

		final Graphics2D gLight = this.lightmap.createGraphics();
		try {
			// =====================================================================
			// 1. VACIADO TOTAL DE VRAM A TRANSPARENCIA PURA
			// =====================================================================
			gLight.setComposite(COMPOSITE_LIMPIEZA);
			gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);

			// =====================================================================
			// 2. ESTAMPAR LA OSCURIDAD AMBIENTAL A PANTALLA COMPLETA
			// =====================================================================
			gLight.setComposite(COMPOSITE_NORMAL);
			gLight.setColor(colorAmbiente);
			gLight.fillRect(0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);

			// =====================================================================
			// 3. PERFORAR AGUJEROS DE LUZ ADAPTADOS A ZOOM Y ROTACIÓN
			// =====================================================================
			/*
			 * =====================================================================
			 * EXPLICACIÓN DIDÁCTICA: ¿CÓMO SE PROYECTA LA LUZ EN LA PANTALLA?
			 * --------------------------------------------------------------------- 1.
			 * ESCALADO DE RADIO POR ZOOM: Si la cámara hace zoom out a 0.5x, los personajes
			 * se ven más chicos; por lo tanto, el círculo de luz debe achicarse a la mitad:
			 * radioPantalla = radioLuz * zoom
			 * 
			 * 2. ROTACIÓN Y CENTRADO ANGULAR: Calculamos la distancia relativa respecto a
			 * la cámara (dx, dy), la multiplicamos por el zoom y la rotamos mediante la
			 * matriz estándar: rx = (dx * cos) - (dy * sin) ry = (dx * sin) + (dy * cos)
			 * 
			 * 3. POSICIÓN FINAL EN PANTALLA: Sumamos el centro de la pantalla (CENTROX,
			 * CENTROY) y la vibración del temblor (shakeX, shakeY).
			 * 
			 * Resultado: El agujero de luz sigue exactamente al jugador sin desfasarse
			 * jamás, incluso mientras la pantalla tiembla violentamente o gira en Modo
			 * Borracho.
			 * =====================================================================
			 */
			if (this.cantidadActivas > 0) {
				gLight.setComposite(COMPOSITE_PERFORAR);

				for (int i = 0; i < this.cantidadActivas; i++) {
					final FuenteLuz luz = this.activas[i];
					final BufferedImage halo = this.texturasHaloPrehorneadas[luz.getTipo().ordinal()];

					final int radioPantalla = (int) Math.round(luz.getRadioActual() * z);
					final int diametroPantalla = radioPantalla * 2;

					// Distancia relativa del punto de luz respecto a la cámara
					final double dx = (luz.getPosX() - camX) * z;
					final double dy = (luz.getPosY() - camY) * z;

					// Rotación 2D sobre el plano de pantalla
					final double rx = (dx * cos) - (dy * sin);
					final double ry = (dx * sin) + (dy * cos);

					final int screenX = (int) Math.round(Constantes.CENTROX + shakeX + rx) - radioPantalla;
					final int screenY = (int) Math.round(Constantes.CENTROY + shakeY + ry) - radioPantalla;

					gLight.drawImage(halo, screenX, screenY, diametroPantalla, diametroPantalla, null);
				}
			}

		} finally {
			gLight.dispose();
		}

		// 4. Dibujamos el Lightmap resultante en coordenadas fijas de pantalla 1:1
		DibujoDebug.dibujarImagen(g, this.lightmap, 0, 0);
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public CicloDiaNoche getCiclo() {
		return this.ciclo;
	}

	public boolean isModoAmbienteFijo() {
		return this.modoAmbienteFijo;
	}

	public Color getColorAmbienteFijo() {
		return this.colorAmbienteFijo;
	}

	public int getCantidadActivas() {
		return this.cantidadActivas;
	}
}