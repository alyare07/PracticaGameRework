package principal.clima;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.iluminacion.IntensidadNiebla;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Gestor maestro del subsistema meteorológico y atmosférico del motor 2D.
 * <p>
 * <b>Responsabilidades del Sistema:</b>
 * <ul>
 * <li><b>Simulación Meteorológica Autónoma:</b> Transiciones continuas y
 * orgánicas entre estados climáticos basadas en cadenas probabilísticas de
 * Markov según el bioma.</li>
 * <li><b>Termodinámica y Barometría:</b> Cálculo en tiempo real de temperatura
 * ambiental ($^\circ\text{C}$), humedad relativa y presión atmosférica
 * ($\text{hPa}$) vinculadas al ciclo solar.</li>
 * <li><b>Control Dinámico de Viento:</b> Simulación de ráfagas armónicas y
 * empuje vectorial para nubes, niebla y partículas.</li>
 * <li><b>Partículas en Espacio de Pantalla (Screen-Space Wrapped Pool):</b>
 * Renderizado de lluvia, nieve, ventisca, hojas, ceniza volcánica, esporas y
 * arena con <b>Cero Asignación en Memoria (Zero-GC)</b>.</li>
 * <li><b>Sombras de Nubes y Niebla Procedural:</b> Generación matemática de
 * ruido armónico en texturas de VRAM de alto rendimiento.</li>
 * <li><b>Simulador de Tormentas:</b> Relámpagos con destello óptico en pantalla
 * y retardo acústico físico de trueno según la distancia calculada.</li>
 * </ul>
 * </p>
 * 
 * @version 6.5
 */
public class GestorClima {

	// =========================================================================
	// === 1. CONSTANTES DE RENDIMIENTO Y TEXTURIZADO
	// =========================================================================

	/** Límite de partículas simultáneas activas en pantalla. */
	private static final int MAX_PARTICULAS = 400;

	/** Dimensión en píxeles de la textura cuadrada de sombras de nubes. */
	private static final int RESOLUCION_NUBES = 512;

	/** Dimensión en píxeles de la textura cuadrada de niebla procedural. */
	private static final int RESOLUCION_NIEBLA = 256;

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: COLORES ESTÁTICOS PRE-ASIGNADOS (ZERO-GC)
	 * ------------------------------------------------------------------------- En
	 * Java, escribir 'new Color(r, g, b, a)' dentro de un método de dibujo (que se
	 * ejecuta 60 veces por segundo para 400 partículas) crearía más de 24.000
	 * objetos por segundo en la memoria Heap. Esto obligaría al recolector de
	 * basura (Garbage Collector) a pausar el juego periódicamente para limpiar,
	 * causando micro-tirones o caídas de FPS.
	 *
	 * Al instanciarlos una sola vez como constantes estáticas al cargar la clase,
	 * el costo de recolección de basura es exactamente 0.
	 * =========================================================================
	 */
	private static final Color COLOR_LLUVIA = new Color(185, 215, 245, 175);
	private static final Color COLOR_NIEVE = new Color(245, 250, 255, 210);
	private static final Color COLOR_ARENA = new Color(215, 170, 95, 190);
	private static final Color COLOR_HOJAS_VIENTO = new Color(135, 190, 60, 220);
	private static final Color COLOR_CENIZA = new Color(75, 70, 75, 200);
	private static final Color COLOR_BRASA = new Color(255, 125, 30, 235);
	private static final Color COLOR_ESPORAS = new Color(110, 235, 255, 210);
	private static final Color COLOR_PETALOS = new Color(255, 175, 205, 220);
	private static final Color COLOR_LLUVIA_ACIDA = new Color(135, 240, 90, 185);

	/** Textura pre-horneada de nubes oscuras con claros de sol transparentes. */
	private final BufferedImage texturaSombrasNubes;

	/** Textura pre-horneada de bruma continua para niebla y vapor ambiental. */
	private final BufferedImage texturaNiebla;

	// =========================================================================
	// === 2. SIMULADOR METEOROLÓGICO Y PRONÓSTICO
	// =========================================================================

	/**
	 * Interruptor para el cambio automático del clima según probabilidades del
	 * bioma.
	 */
	private boolean cicloAutomaticoHabilitado = true;

	/**
	 * Perfil meteorológico del bioma actual (determina probabilidades de lluvia,
	 * nieve, etc.).
	 */
	private PerfilClima perfilBiomaActual = PerfilClima.TEMPLADO_BOSQUE;

	/** Estado climático activo en el fotograma actual. */
	private TipoClima climaActual = TipoClima.DESPEJADO;

	/** Próximo estado meteorológico que entrará tras agotar el temporizador. */
	private TipoClima climaPronosticado = TipoClima.LLUVIA_LEVE;

	/** Duración total del estado climático en curso en segundos reales. */
	private double duracionEstadoClimaSegundos = 360.0; // 6 minutos por defecto

	/** Tiempo restante en segundos antes de la siguiente transición climática. */
	private double tiempoRestanteEstadoClima = 360.0;

	/**
	 * Tiempo en segundos que dura el fundido suave entre un clima y el siguiente.
	 */
	private double duracionTransicionClima = 5.0;

	/** Bandera de control para pruebas aceleradas en tiempo de desarrollo. */
	private boolean modoPruebaRapida = false;

	/** Temperatura ambiental actual en grados Celsius (°C). */
	private double temperaturaActualCelsius = 20.0;

	/** Humedad relativa del aire (0.0 = seco absoluto, 1.0 = saturación/lluvia). */
	private double humedadActual = 0.50;

	/** Presión atmosférica ambiental en hectopascales (hPa / milibares). */
	private double presionBarometricaHPa = 1013.25;

	// =========================================================================
	// === 3. CONTROLADOR DE VIENTO Y RÁFAGAS
	// =========================================================================

	/**
	 * Dirección angular del viento expresada en radianes (0 = derecha, PI/2 =
	 * abajo).
	 */
	private double anguloVientoRadianes = Math.toRadians(45.0);

	/**
	 * Multiplicador escalar de intensidad del viento (0.0 = calma, 3.0+ =
	 * temporal).
	 */
	private double fuerzaViento = 1.0;

	/**
	 * Acumulador de tiempo individual para modular ráfagas orgánicas sinusoidales.
	 */
	private double tiempoRafaga = 0.0;

	/** Componente vectorial horizontal del viento. */
	private double vectorVientoX = 0.0;

	/** Componente vectorial vertical del viento. */
	private double vectorVientoY = 0.0;

	// =========================================================================
	// === 4. POOL DE PARTÍCULAS ATMOSFÉRICAS (ZERO-GC)
	// =========================================================================

	/** Arreglo contiguo de instancias de partículas pre-creadas en memoria. */
	private final ParticulaClima[] particulas = new ParticulaClima[MAX_PARTICULAS];

	/** Cantidad de partículas activas en pantalla para el clima en curso. */
	private int cantidadParticulasActivas = 0;

	// =========================================================================
	// === 5. NUBES, NIEBLA Y TORMENTAS
	// =========================================================================

	private boolean sombrasNubesHabilitadas = true;
	private float opacidadSombraNubes = 0.32f;
	private double scrollNubesX = 0.0;
	private double scrollNubesY = 0.0;

	private IntensidadNiebla nivelNieblaGlobal = IntensidadNiebla.DESACTIVADA;
	private Color colorNiebla = new Color(200, 215, 230);
	private float opacidadNieblaActual = 0.0f;
	private float opacidadNieblaOrigen = 0.0f;
	private float opacidadNieblaDestino = 0.0f;
	private boolean transicionNieblaActiva = false;
	private double tiempoTransicionNieblaTotal = 0.0;
	private double tiempoTransicionNieblaActual = 0.0;

	// Inmersión espacial recibida desde GestorZonasAmbiente
	private float opacidadNieblaBioma = 0.0f;
	private double factorInmersionBioma = 0.0;
	private double scrollNieblaX = 0.0;
	private double scrollNieblaY = 0.0;

	// Controlador acústico y lumínico de tormentas
	private boolean tormentaActiva = false;
	private double temporizadorProximoRayo = 5.0;
	private boolean truenoPendiente = false;
	private double tiempoParaSonidoTrueno = 0.0;
	private float volumenTruenoProporcional = 1.0f;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Inicializa el subsistema meteorológico, pre-hornea las texturas en VRAM,
	 * puebla el pool de partículas estáticas y sincroniza el primer pronóstico.
	 */
	public GestorClima() {
		this.texturaSombrasNubes = this.hornearTexturaSombrasNubes();
		this.texturaNiebla = this.hornearTexturaNiebla();

		// Inicialización del pool de partículas fijas (Zero-GC)
		for (int i = 0; i < MAX_PARTICULAS; i++) {
			this.particulas[i] = new ParticulaClima();
			this.particulas[i].inicializarAleatorio();
		}

		this.actualizarVectoresViento();
		this.climaPronosticado = this.perfilBiomaActual.calcularSiguienteClima(this.climaActual);
	}

	// =========================================================================
	// === PRE-HORNEADO PROCEDURAL (UNA SOLA VEZ EN EL ARRANQUE)
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: GENERACIÓN DE NUBES MEDIANTE RUIDO ARMÓNICO
	 * -------------------------------------------------------------------------
	 * Para crear nubes esponjosas sin cargar archivos PNG externos:
	 *
	 * 1. MULTI-OCTAVA ARMÓNICA: Sumamos 3 ondas senoidales periódicas con distintas
	 * frecuencias (1x, 2x, 4x). Al multiplicarse por '2 * PI', el borde izquierdo
	 * de la imagen encaja exactamente con el derecho (Tiling perfecto sin cortes
	 * visibles).
	 *
	 * 2. UMBRAL (THRESHOLD) Y CURVA SUAVE (SMOOTHSTEP): Si el valor de la onda es
	 * menor a 0.45, el canal Alpha es 0 (cielo soleado). Si es mayor, aplicamos la
	 * fórmula cúbica 't * t * (3 - 2t)', logrando que los bordes de la sombra sean
	 * suaves y difuminados como la penumbra real de una nube.
	 *
	 * 3. COLOR NEGRO PURO (RGB: 0, 0, 0): Al dibujarse con AlphaComposite, el negro
	 * resta luminosidad al suelo en lugar de actuar como una niebla blanca que
	 * aclara el terreno.
	 * =========================================================================
	 */
	private BufferedImage hornearTexturaSombrasNubes() {
		final int size = RESOLUCION_NUBES;
		final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				final double u = (x / (double) size) * Math.PI * 2.0;
				final double v = (y / (double) size) * Math.PI * 2.0;

				// Superposición de armónicos
				final double n1 = Math.sin(u) + Math.cos(v);
				final double n2 = 0.5 * (Math.sin((u * 2.0) + v) + Math.cos(u - (v * 2.0)));
				final double n3 = 0.25 * (Math.sin((u * 4.0) - (v * 2.0)) + Math.cos((u * 2.0) + (v * 4.0)));

				final double valorRuido = (n1 + n2 + n3) / 1.75;
				final double normalizado = (valorRuido + 1.0) / 2.0;

				// Thresholding y Smoothstep
				double factorSombra = 0.0;
				if (normalizado > 0.45) {
					final double t = (normalizado - 0.45) / 0.55;
					factorSombra = t * t * (3.0 - (2.0 * t));
				}

				final int alpha = (int) (factorSombra * 255.0);
				final int rgba = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
				img.setRGB(x, y, rgba);
			}
		}
		return img;
	}

	/**
	 * Hornea una textura continua y suave de 256x256 para niebla, bruma o humo
	 * ambiental.
	 */
	private BufferedImage hornearTexturaNiebla() {
		final int size = RESOLUCION_NIEBLA;
		final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				final double nx = (x / (double) size) * Math.PI * 2.0;
				final double ny = (y / (double) size) * Math.PI * 2.0;
				final double v = (Math.sin(nx) + Math.sin(ny) + (0.5 * Math.sin((nx * 2.0) + ny))
						+ (0.5 * Math.cos(nx - (ny * 2.0)))) / 3.0;
				final int alpha = (int) Math.max(0.0, Math.min(255.0, ((v + 1.0) / 2.0) * 255.0));
				final int rgba = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
				img.setRGB(x, y, rgba);
			}
		}
		return img;
	}

	// =========================================================================
	// === CICLO LÓGICO DE ACTUALIZACIÓN (60 APS / TICKS)
	// =========================================================================

	/**
	 * Procesa la simulación meteorológica, el desplazamiento por viento, la
	 * termodinámica y la física de partículas en el fotograma actual.
	 */
	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		// 1. Simulación meteorológica y pronóstico
		if (this.cicloAutomaticoHabilitado) {
			this.actualizarSimuladorMeteorologico(dt);
		}

		// 2. Cálculo termodinámico y barométrico
		this.actualizarTermodinamica(dt);

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: FÍSICA DE RÁFAGAS DE VIENTO
		 * --------------------------------------------------------------------- El
		 * viento real nunca sopla a una velocidad estática y matemática. Para crear la
		 * sensación de aire vivo:
		 *
		 * 1. Multiplicamos la fuerza base por '(1.0 + sin(tiempo) * 0.25)'. 2. Esto
		 * hace que el viento acelere y desacelere suavemente un +/- 25% de forma
		 * continua, haciendo que las hojas, nubes y gotas bailen de manera orgánica en
		 * pantalla.
		 * =====================================================================
		 */
		this.tiempoRafaga += dt * 1.5;
		final double rafaga = 1.0 + (Math.sin(this.tiempoRafaga) * 0.25);
		final double vxViento = this.vectorVientoX * rafaga;
		final double vyViento = this.vectorVientoY * rafaga;

		// 3. Desplazamiento UV de texturas
		final double velNubes = (this.climaActual == TipoClima.TORMENTA_ARENA) ? 45.0 : 18.0;
		final double velNiebla = (this.climaActual == TipoClima.TORMENTA_ARENA) ? 55.0 : 12.0;

		this.scrollNubesX = (this.scrollNubesX + (vxViento * velNubes * dt)) % RESOLUCION_NUBES;
		this.scrollNubesY = (this.scrollNubesY + (vyViento * velNubes * dt)) % RESOLUCION_NUBES;

		this.scrollNieblaX = (this.scrollNieblaX + (vxViento * velNiebla * dt)) % RESOLUCION_NIEBLA;
		this.scrollNieblaY = (this.scrollNieblaY + (vyViento * velNiebla * dt)) % RESOLUCION_NIEBLA;

		// 4. Actualización física de partículas en pantalla
		this.actualizarParticulas(vxViento, vyViento, dt);

		// 5. Interpolación suave de niebla
		if (this.transicionNieblaActiva) {
			this.tiempoTransicionNieblaActual += dt;
			final double factor = Math.min(1.0, this.tiempoTransicionNieblaActual / this.tiempoTransicionNieblaTotal);
			this.opacidadNieblaActual = (float) (this.opacidadNieblaOrigen
					+ ((this.opacidadNieblaDestino - this.opacidadNieblaOrigen) * factor));

			if (factor >= 1.0) {
				this.transicionNieblaActiva = false;
				this.opacidadNieblaActual = this.opacidadNieblaDestino;
			}
		}

		// 6. Proceso de tormentas y rayos
		this.actualizarTormenta(dt);
	}

	/**
	 * Controla el reloj de cambio de clima y calcula el siguiente pronóstico en la
	 * cadena.
	 */
	private void actualizarSimuladorMeteorologico(final double dt) {
		this.tiempoRestanteEstadoClima -= dt;

		if (this.tiempoRestanteEstadoClima <= 0.0) {
			// Aplica el clima que estaba previsto en el pronóstico
			this.setClima(this.climaPronosticado, this.duracionTransicionClima);

			// Duración aleatoria del nuevo clima (entre 4 y 8 minutos reales)
			if (!this.modoPruebaRapida) {
				this.duracionEstadoClimaSegundos = 240.0 + (Math.random() * 240.0);
			}
			this.tiempoRestanteEstadoClima = this.duracionEstadoClimaSegundos;

			// Genera el próximo pronóstico del bioma actual
			this.climaPronosticado = this.perfilBiomaActual.calcularSiguienteClima(this.climaActual);
		}
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: CÁLCULO TERMODINÁMICO Y PRESIÓN BAROMÉTRICA
	 * -------------------------------------------------------------------------
	 * Vincula la temperatura y la presión al ciclo de 24 horas y al clima:
	 *
	 * 1. CICLO SOLAR: La temperatura sube al máximo cerca de las 14:00 (+4.5°C) y
	 * baja al mínimo antes del amanecer a las 04:30 (-4.5°C).
	 *
	 * 2. MODIFICADORES CLIMÁTICOS: La lluvia enfría el ambiente (-2.5°C), las
	 * tormentas desploman la presión (< 1000 hPa) y la nieve congela el aire.
	 *
	 * 3. INTERPOLACIÓN LINEAL (LERP): Usamos '(target - actual) * (dt * 0.1)' para
	 * que el termómetro varíe suavemente y sin dar saltos bruscos.
	 * =========================================================================
	 */
	private void actualizarTermodinamica(final double dt) {
		double hora = 12.0;
		if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
			hora = Globales.GESTOR_LUZ.getCiclo().getHoraActual();
		}

		final double cicloSolarTermico = Math.sin(((hora - 8.0) / 24.0) * Math.PI * 2.0) * 4.5;
		double tempObjetivo = this.perfilBiomaActual.getTemperaturaBase() + cicloSolarTermico;
		double humObjetivo = this.perfilBiomaActual.getHumedadBase();
		double presObjetivo = 1013.25;

		switch (this.climaActual) {
		case LLUVIA_LEVE:
			tempObjetivo -= 2.5;
			humObjetivo = 0.85;
			presObjetivo = 1005.0;
			break;
		case LLUVIA_TORMENTA:
		case LLUVIA_ACIDA:
			tempObjetivo -= 4.0;
			humObjetivo = 0.95;
			presObjetivo = 992.0;
			break;
		case NIEVE:
		case VENTISCA:
			tempObjetivo -= 8.0;
			humObjetivo = 0.75;
			presObjetivo = 1002.0;
			break;
		case TORMENTA_ARENA:
			tempObjetivo += 6.0;
			humObjetivo = 0.10;
			presObjetivo = 998.0;
			break;
		default:
			break;
		}

		this.temperaturaActualCelsius += (tempObjetivo - this.temperaturaActualCelsius) * (dt * 0.1);
		this.humedadActual += (humObjetivo - this.humedadActual) * (dt * 0.1);
		this.presionBarometricaHPa += (presObjetivo - this.presionBarometricaHPa) * (dt * 0.1);
	}

	/**
	 * Actualiza las trayectorias de las partículas activas según la física de cada
	 * clima.
	 */
	private void actualizarParticulas(final double vxViento, final double vyViento, final double dt) {
		if (this.cantidadParticulasActivas <= 0) {
			return;
		}

		for (int i = 0; i < this.cantidadParticulasActivas; i++) {
			final ParticulaClima p = this.particulas[i];
			double vx = 0.0;
			double vy = 0.0;

			switch (this.climaActual) {
			case LLUVIA_LEVE:
			case LLUVIA_TORMENTA:
				vx = (vxViento * 80.0) * p.velocidadBase;
				vy = (320.0 + (vyViento * 60.0)) * p.velocidadBase;
				break;

			case LLUVIA_ACIDA:
				vx = (vxViento * 70.0) * p.velocidadBase;
				vy = (340.0 + (vyViento * 50.0)) * p.velocidadBase;
				break;

			case NIEVE:
			case VENTISCA:
				p.faseOscilacion += dt * 3.0;
				final double oscilacionNieve = Math.sin(p.faseOscilacion) * 25.0;
				vx = ((vxViento * 40.0) + oscilacionNieve) * p.velocidadBase;
				vy = (65.0 + (vyViento * 20.0)) * p.velocidadBase;
				break;

			case VENTOSO:
				p.faseOscilacion += dt * 5.0;
				final double aleteoHoja = Math.sin(p.faseOscilacion) * 35.0;
				vx = (140.0 + (vxViento * 80.0)) * p.velocidadBase;
				vy = (45.0 + (vyViento * 40.0) + aleteoHoja) * p.velocidadBase;
				break;

			case PETALOS_CEREZO:
				p.faseOscilacion += dt * 4.0;
				final double balanceoPetalo = Math.sin(p.faseOscilacion) * 28.0;
				vx = (65.0 + (vxViento * 45.0) + balanceoPetalo) * p.velocidadBase;
				vy = (55.0 + (vyViento * 25.0)) * p.velocidadBase;
				break;

			case TORMENTA_ARENA:
				p.faseOscilacion += dt * 4.0;
				vx = (260.0 + (vxViento * 110.0)) * p.velocidadBase;
				vy = (30.0 + (Math.sin(p.faseOscilacion) * 15.0) + (vyViento * 20.0)) * p.velocidadBase;
				break;

			case CENIZA_VOLCANICA:
				p.faseOscilacion += dt * 2.5;
				final double oscilacionCeniza = Math.sin(p.faseOscilacion) * 15.0;
				vx = (25.0 + (vxViento * 30.0) + oscilacionCeniza) * p.velocidadBase;
				vy = (40.0 + (vyViento * 15.0)) * p.velocidadBase;
				break;

			case ESPORAS_MAGICAS:
				// Las esporas flotan y ascienden suavemente contra la gravedad
				p.faseOscilacion += dt * 2.0;
				vx = ((Math.cos(p.faseOscilacion) * 18.0) + (vxViento * 15.0)) * p.velocidadBase;
				vy = (-25.0 + (Math.sin(p.faseOscilacion) * 12.0)) * p.velocidadBase;
				break;

			default:
				break;
			}

			p.actualizar(vx, vy, dt);
		}
	}

	/*
	 * =========================================================================
	 * EXPLICACIÓN DIDÁCTICA: FÍSICA ACÚSTICA DEL TRUENO
	 * -------------------------------------------------------------------------
	 * Como la luz viaja casi instantáneamente comparada con el sonido:
	 *
	 * 1. Primero se dispara el fogonazo de luz blanca en GestorLuz. 2. Se calcula
	 * una distancia virtual del rayo (0.4 a 2.9 km). 3. El tiempo de retraso para
	 * reproducir el audio es 'distancia * 0.75s'. 4. El volumen se atenúa con la
	 * distancia (rayos lejanos suenan más suaves).
	 * =========================================================================
	 */
	private void actualizarTormenta(final double dt) {
		if (!this.tormentaActiva) {
			return;
		}

		this.temporizadorProximoRayo -= dt;
		if (this.temporizadorProximoRayo <= 0.0) {
			final double duracion = 0.20 + (Math.random() * 0.15);
			if (Globales.GESTOR_LUZ != null) {
				Globales.GESTOR_LUZ.dispararFlashGlobal(duracion, true);
			}

			final double distanciaKm = 0.4 + (Math.random() * 2.5);
			this.tiempoParaSonidoTrueno = distanciaKm * 0.75;
			this.volumenTruenoProporcional = (float) Math.max(0.2, 1.0 - (distanciaKm / 3.0));
			this.truenoPendiente = true;

			this.temporizadorProximoRayo = 4.0 + (Math.random() * 8.0);
		}

		if (this.truenoPendiente) {
			this.tiempoParaSonidoTrueno -= dt;
			if (this.tiempoParaSonidoTrueno <= 0.0) {
				this.truenoPendiente = false;
				// TODO: Reproducir sonido de trueno grave aquí (volumen:
				// this.volumenTruenoProporcional)
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO ATMOSFÉRICO (SOBRE EL MUNDO Y BAJO LA SOMBRA)
	// =========================================================================

	/**
	 * Dibuja las sombras de nubes, la niebla ambiental y las partículas de clima.
	 *
	 * @param g Contexto gráfico {@link Graphics2D}.
	 */
	public void pintar(final Graphics2D g) {
		final int oscuridad = (Globales.GESTOR_LUZ != null) ? Globales.GESTOR_LUZ.getAlphaOscuridadActual() : 0;

		// 1. Sombras de nubes diurnas (Se apagan solas durante la noche)
		if (this.sombrasNubesHabilitadas && (oscuridad < 130)) {
			final float factorDia = 1.0f - (oscuridad / 130.0f);
			final float opacidadEfectivaNubes = this.opacidadSombraNubes * factorDia;

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadEfectivaNubes));
			final int ox = (int) Math.round(this.scrollNubesX);
			final int oy = (int) Math.round(this.scrollNubesY);

			for (int y = -RESOLUCION_NUBES + oy; y < Constantes.ALTO_JUEGO; y += RESOLUCION_NUBES) {
				for (int x = -RESOLUCION_NUBES + ox; x < Constantes.ANCHO_JUEGO; x += RESOLUCION_NUBES) {
					g.drawImage(this.texturaSombrasNubes, x, y, null);
				}
			}
		}

		// 2. Capa de niebla dinámica (Modulada por el bioma local)
		float opacidadEfectivaNiebla = this.opacidadNieblaActual;
		if (this.factorInmersionBioma > 0.0) {
			opacidadEfectivaNiebla = (float) (opacidadEfectivaNiebla
					+ ((this.opacidadNieblaBioma - opacidadEfectivaNiebla) * this.factorInmersionBioma));
		}

		if (opacidadEfectivaNiebla > 0.0f) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadEfectivaNiebla));
			final int ox = (int) Math.round(this.scrollNieblaX);
			final int oy = (int) Math.round(this.scrollNieblaY);

			for (int y = -RESOLUCION_NIEBLA + oy; y < Constantes.ALTO_JUEGO; y += RESOLUCION_NIEBLA) {
				for (int x = -RESOLUCION_NIEBLA + ox; x < Constantes.ANCHO_JUEGO; x += RESOLUCION_NIEBLA) {
					g.drawImage(this.texturaNiebla, x, y, null);
				}
			}
		}

		// 3. Partículas atmosféricas
		if (this.cantidadParticulasActivas > 0) {
			this.pintarParticulas(g);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	}

	/**
	 * Dibuja los trazos, copos, hojas o cenizas en pantalla con operaciones de
	 * dibujo directas.
	 */
	private void pintarParticulas(final Graphics2D g) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		switch (this.climaActual) {
		case LLUVIA_LEVE:
		case LLUVIA_TORMENTA:
			g.setColor(COLOR_LLUVIA);
			final double dxLluvia = this.vectorVientoX * 3.5;
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.drawLine((int) p.x, (int) p.y, (int) (p.x - dxLluvia), (int) (p.y - p.longitudTrazo));
			}
			break;

		case LLUVIA_ACIDA:
			g.setColor(COLOR_LLUVIA_ACIDA);
			final double dxAcido = this.vectorVientoX * 3.2;
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.drawLine((int) p.x, (int) p.y, (int) (p.x - dxAcido), (int) (p.y - (p.longitudTrazo * 1.1)));
			}
			break;

		case NIEVE:
		case VENTISCA:
			g.setColor(COLOR_NIEVE);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) p.tamano;
				g.fillRect((int) p.x, (int) p.y, s, s);
			}
			break;

		case VENTOSO:
			g.setColor(COLOR_HOJAS_VIENTO);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.fillRect((int) p.x, (int) p.y, (int) p.tamano + 2, (int) p.tamano + 1);
			}
			break;

		case PETALOS_CEREZO:
			g.setColor(COLOR_PETALOS);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.fillRect((int) p.x, (int) p.y, (int) p.tamano + 1, (int) p.tamano + 2);
			}
			break;

		case TORMENTA_ARENA:
			g.setColor(COLOR_ARENA);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.fillRect((int) p.x, (int) p.y, (int) (p.tamano * 2.5), (int) p.tamano);
			}
			break;

		case CENIZA_VOLCANICA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				// 1 de cada 4 partículas es una brasa incandescente naranja
				g.setColor(((i % 4) == 0) ? COLOR_BRASA : COLOR_CENIZA);
				final int s = (int) Math.max(1, p.tamano);
				g.fillRect((int) p.x, (int) p.y, s, s);
			}
			break;

		case ESPORAS_MAGICAS:
			g.setColor(COLOR_ESPORAS);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) Math.max(1, p.tamano);
				g.fillRect((int) p.x, (int) p.y, s, s);
			}
			break;

		default:
			break;
		}
	}

	// =========================================================================
	// === MÉTODOS DE CONSULTA Y GETTERS (API PÚBLICA)
	// =========================================================================

	/**
	 * Retorna el nombre descriptivo y legible del clima activo actual (Zero-GC).
	 *
	 * @return Cadena inmutable con el nombre (ej: {@code "Tormenta Eléctrica"},
	 *         {@code "Despejado"}).
	 */
	public String getNombreClimaActual() {
		return (this.climaActual != null) ? this.climaActual.getNombre() : "Despejado";
	}

	/**
	 * Retorna una descripción narrativa del clima venidero para el HUD o NPCs.
	 *
	 * @return Cadena con el reporte meteorológico formateado.
	 */
	public String getReporteMeteorologico() {
		final int minutosRestantes = (int) Math.ceil(this.tiempoRestanteEstadoClima / 60.0);
		switch (this.climaPronosticado) {
		case LLUVIA_LEVE:
			return "El cielo se encapotara pronto. Se espera llovizna en " + minutosRestantes + " min.";
		case LLUVIA_TORMENTA:
			return "¡Alerta de tormenta electrica y fuertes vientos en " + minutosRestantes + " min!";
		case NIEVE:
			return "Las temperaturas descenderan. Se aproxima nevada en " + minutosRestantes + " min.";
		case VENTISCA:
			return "¡Peligro de ventisca helada! Se recomienda buscar refugio en " + minutosRestantes + " min.";
		case TORMENTA_ARENA:
			return "Vientos huracanados levantaran arena del desierto en " + minutosRestantes + " min.";
		case VENTOSO:
			return "El viento aumentara su fuerza en los proximos " + minutosRestantes + " min.";
		default:
			return "El clima se mantendra despejado y estable durante los proximos minutos.";
		}
	}

	public TipoClima getClimaActual() {
		return this.climaActual;
	}

	public TipoClima getClimaPronosticado() {
		return this.climaPronosticado;
	}

	public double getTemperaturaCelsius() {
		return this.temperaturaActualCelsius;
	}

	public double getHumedadRelativa() {
		return this.humedadActual;
	}

	public double getPresionHPa() {
		return this.presionBarometricaHPa;
	}

	public PerfilClima getPerfilBiomaActual() {
		return this.perfilBiomaActual;
	}

	public void setPerfilBioma(final PerfilClima nuevoPerfil) {
		if (nuevoPerfil != null) {
			this.perfilBiomaActual = nuevoPerfil;
			this.climaPronosticado = this.perfilBiomaActual.calcularSiguienteClima(this.climaActual);
		}
	}

	public void setCicloAutomaticoHabilitado(final boolean habilitado) {
		this.cicloAutomaticoHabilitado = habilitado;
	}

	public boolean isCicloAutomaticoHabilitado() {
		return this.cicloAutomaticoHabilitado;
	}

	public void setClima(final TipoClima nuevoClima) {
		this.setClima(nuevoClima, 3.0);
	}

	public void setClima(final TipoClima nuevoClima, final double duracionTransicionSegundos) {
		if (nuevoClima == null) {
			return;
		}
		this.climaActual = nuevoClima;

		this.setNivelNiebla(nuevoClima.getNivelNiebla(), duracionTransicionSegundos);
		this.setColorNiebla(nuevoClima.getColorNiebla());
		this.setSombrasNubesHabilitadas(nuevoClima.isTieneNubes());
		this.setOpacidadSombraNubes(nuevoClima.getOpacidadNubes());
		this.setTormentaActiva(nuevoClima.isTieneTormentaRayos());
		this.setViento(nuevoClima.getAnguloVientoGrados(), nuevoClima.getFuerzaViento());

		this.cantidadParticulasActivas = Math.min(MAX_PARTICULAS, nuevoClima.getCantidadParticulas());
	}

	public void setViento(final double gradosDireccion, final double fuerza) {
		this.anguloVientoRadianes = Math.toRadians(gradosDireccion);
		this.fuerzaViento = Math.max(0.0, fuerza);
		this.actualizarVectoresViento();
	}

	public void setDireccionViento(final double gradosDireccion) {
		this.anguloVientoRadianes = Math.toRadians(gradosDireccion);
		this.actualizarVectoresViento();
	}

	public void setFuerzaViento(final double fuerza) {
		this.fuerzaViento = Math.max(0.0, fuerza);
		this.actualizarVectoresViento();
	}

	private void actualizarVectoresViento() {
		this.vectorVientoX = Math.cos(this.anguloVientoRadianes) * this.fuerzaViento;
		this.vectorVientoY = Math.sin(this.anguloVientoRadianes) * this.fuerzaViento;
	}

	public void setNivelNiebla(final IntensidadNiebla nivel) {
		this.setNivelNiebla(nivel, 0.0);
	}

	public void setNivelNiebla(final IntensidadNiebla nivel, final double duracionSegundos) {
		this.nivelNieblaGlobal = (nivel != null) ? nivel : IntensidadNiebla.DESACTIVADA;
		final float destino = this.nivelNieblaGlobal.getOpacidad();

		if (duracionSegundos <= 0.0) {
			this.opacidadNieblaActual = destino;
			this.transicionNieblaActiva = false;
		} else {
			this.opacidadNieblaOrigen = this.opacidadNieblaActual;
			this.opacidadNieblaDestino = destino;
			this.tiempoTransicionNieblaTotal = duracionSegundos;
			this.tiempoTransicionNieblaActual = 0.0;
			this.transicionNieblaActiva = true;
		}
	}

	public void setNieblaBiomaLocal(final IntensidadNiebla nivel, final double factorInmersion) {
		this.opacidadNieblaBioma = (nivel != null) ? nivel.getOpacidad() : 0.0f;
		this.factorInmersionBioma = factorInmersion;
	}

	public void setSombrasNubesHabilitadas(final boolean habilitadas) {
		this.sombrasNubesHabilitadas = habilitadas;
	}

	public boolean isSombrasNubesHabilitadas() {
		return this.sombrasNubesHabilitadas;
	}

	public void setOpacidadSombraNubes(final float opacidad) {
		this.opacidadSombraNubes = Math.max(0.0f, Math.min(1.0f, opacidad));
	}

	public void setColorNiebla(final Color color) {
		this.colorNiebla = (color != null) ? color : new Color(200, 215, 230);
	}

	public void setTormentaActiva(final boolean activa) {
		this.tormentaActiva = activa;
		this.temporizadorProximoRayo = 3.0;
	}

	public boolean isTormentaActiva() {
		return this.tormentaActiva;
	}

	public double getFuerzaViento() {
		return this.fuerzaViento;
	}

	// =========================================================================
	// === MÉTODOS DE PRUEBA Y DEBUG
	// =========================================================================

	/**
	 * MODO TEST TEMPORAL: Acelera el ciclo meteorológico para ver los cambios en
	 * pocos segundos.
	 *
	 * @param segundosPorClima   Tiempo que dura cada clima (ej: 8.0 segundos).
	 * @param segundosTransicion Tiempo que tarda el fundido suave (ej: 2.0
	 *                           segundos).
	 */
	public void activarModoPruebaRapida(final double segundosPorClima, final double segundosTransicion) {
		this.modoPruebaRapida = true;
		this.cicloAutomaticoHabilitado = true;
		this.duracionEstadoClimaSegundos = Math.max(1.0, segundosPorClima);
		this.tiempoRestanteEstadoClima = this.duracionEstadoClimaSegundos;
		this.duracionTransicionClima = Math.max(0.5, segundosTransicion);
	}

	public double getTiempoRestanteEstadoClima() {
		return Math.max(0.0, this.tiempoRestanteEstadoClima);
	}

	public void forzarSiguienteClima() {
		this.tiempoRestanteEstadoClima = 0.0;
	}
}