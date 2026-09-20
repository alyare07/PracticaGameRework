package principal.clima;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import principal.configuracion.ConfiguracionGrafica;
import principal.iluminacion.IntensidadNiebla;
import principal.iluminacion.ZonaAmbiente;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.musica.IDMusica;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Motor meteorológico acelerado en VRAM (Overdraw reducido 512x512) y despacho
 * de primitivas en lote. Conmuta el EstadoClima del mundo activo sin fugas
 * (Zero-GC / O(1)).
 * 
 * @version 16.0 (Vanilla Java 8 - Dedicated World Climate Engine)
 */
public class GestorClima {

	private static final int MAX_PARTICULAS = 200;
	private static final int RESOLUCION_NUBES = 512;
	private static final int RESOLUCION_NIEBLA = 512;

	private static final Color COLOR_LLUVIA = new Color(185, 215, 245, 175);
	private static final Color COLOR_NIEVE = new Color(245, 250, 255, 210);
	private static final Color COLOR_ARENA = new Color(215, 170, 95, 190);
	private static final Color COLOR_HOJAS_VIENTO = new Color(135, 190, 60, 220);
	private static final Color COLOR_CENIZA = new Color(75, 70, 75, 200);
	private static final Color COLOR_BRASA = new Color(255, 125, 30, 235);
	private static final Color COLOR_ESPORAS = new Color(110, 235, 255, 210);
	private static final Color COLOR_PETALOS = new Color(255, 175, 205, 220);
	private static final Color COLOR_LLUVIA_ACIDA = new Color(135, 240, 90, 185);

	private static final Color COLOR_TINTE_TORMENTA = new Color(20, 30, 48, 175);
	private static final Color COLOR_TINTE_VENTISCA = new Color(55, 75, 105, 165);
	private static final Color COLOR_TINTE_ARENA = new Color(145, 95, 35, 165);
	private static final AlphaComposite COMPOSITE_OPACO = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

	private static final AlphaComposite[] COMPOSITES_OPACIDAD = new AlphaComposite[101];
	static {
		for (int i = 0; i <= 100; i++) {
			COMPOSITES_OPACIDAD[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i / 100.0f);
		}
	}

	private static AlphaComposite obtenerComposite(final float opacidad) {
		final int indice = Math.max(0, Math.min(100, Math.round(opacidad * 100.0f)));
		return COMPOSITES_OPACIDAD[indice];
	}

	private final BufferedImage texturaSombrasNubes;
	private final BufferedImage texturaNiebla;

	// Estado climático del mundo actualmente vinculado
	private EstadoClima estadoActivo;
	private PerfilClima perfilBiomaActual = PerfilClima.TEMPLADO_BOSQUE;
	private boolean cicloAutomaticoHabilitado = true;
	private boolean modoPruebaRapida = false;

	private double tiempoRafaga = 0.0;
	private final ParticulaClima[] particulas = new ParticulaClima[MAX_PARTICULAS];
	private int cantidadParticulasActivas = 0;

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

	private float opacidadNieblaBioma = 0.0f;
	private double factorInmersionBioma = 0.0;
	private double scrollNieblaX = 0.0;
	private double scrollNieblaY = 0.0;

	private boolean tormentaActiva = false;
	private double temporizadorProximoRayo = 5.0;
	private boolean truenoPendiente = false;
	private double tiempoParaSonidoTrueno = 0.0;
	private float volumenTruenoProporcional = 1.0f;

	public GestorClima() {
		this.texturaSombrasNubes = this.hornearTexturaSombrasNubes();
		this.texturaNiebla = this.hornearTexturaNiebla();

		for (int i = 0; i < MAX_PARTICULAS; i++) {
			this.particulas[i] = new ParticulaClima();
			this.particulas[i].inicializarAleatorio();
		}

		// Estado inicial por defecto
		this.estadoActivo = new EstadoClima(this.perfilBiomaActual, TipoClima.DESPEJADO);
		this.setClima(this.estadoActivo.getClimaActual(), 0.0);
	}

	private BufferedImage hornearTexturaSombrasNubes() {
		final int size = RESOLUCION_NUBES;
		final BufferedImage img = Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(size, size,
				Transparency.TRANSLUCENT);

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				final double u = (x / (double) size) * Math.PI * 2.0;
				final double v = (y / (double) size) * Math.PI * 2.0;

				final double n1 = Math.sin(u) + Math.cos(v);
				final double n2 = 0.5 * (Math.sin((u * 2.0) + v) + Math.cos(u - (v * 2.0)));
				final double n3 = 0.25 * (Math.sin((u * 4.0) - (v * 2.0)) + Math.cos((u * 2.0) + (v * 4.0)));

				final double valorRuido = (n1 + n2 + n3) / 1.75;
				final double normalizado = (valorRuido + 1.0) / 2.0;

				double factorSombra = 0.0;
				if (normalizado > 0.45) {
					final double t = (normalizado - 0.45) / 0.55;
					factorSombra = t * t * (3.0 - (2.0 * t));
				}

				final int alpha = (int) (factorSombra * 255.0);
				final int rgba = (alpha << 24) | 0;
				img.setRGB(x, y, rgba);
			}
		}
		return img;
	}

	private BufferedImage hornearTexturaNiebla() {
		final int size = RESOLUCION_NIEBLA;
		final BufferedImage img = Globales.FUNCIONES.TEXTURAS_TOOLS.crearImagenVRAM(size, size,
				Transparency.TRANSLUCENT);

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

	/**
	 * Conmuta en O(1) el simulador hacia el nuevo mundo. Sella la hora del mundo
	 * saliente y pone al día determinísticamente el clima del mundo entrante si
	 * estuvo inactivo.
	 */
	public void conmutarMundo(final Mundo nuevoMundo) {
		if ((nuevoMundo == null) || (nuevoMundo.getEstadoClima() == null)) {
			return;
		}

		final double ahoraHoras = (Globales.GESTOR_ASTRONOMICO != null)
				? Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego()
				: 0.0;

		// 1. Sellar la hora de salida en el mundo que estamos abandonando
		if ((this.estadoActivo != null) && (ahoraHoras > 0.0)) {
			this.estadoActivo.setTimestampUltimaVisitaHoras(ahoraHoras);
		}

		// 2. Vincularnos al estado del nuevo mundo
		this.estadoActivo = nuevoMundo.getEstadoClima();

		if ((nuevoMundo.getEscenario() != null) && (nuevoMundo.getEscenario().getMetadatos() != null)) {
			this.perfilBiomaActual = nuevoMundo.getEscenario().getMetadatos().getPerfilBioma();

			// Si el nuevo mundo es interior o cueva:
			if (nuevoMundo.getEscenario().getMetadatos().esEspacioInterior()) {
				this.cantidadParticulasActivas = 0;
				this.setTormentaActiva(false);
				return;
			}
		}

		// 3. Si es un mundo exterior, verificar si estuvo dormido y ponerlo al día
		// (Catch-Up)
		if ((this.estadoActivo.getTimestampUltimaVisitaHoras() > 0.0) && (ahoraHoras > 0.0)) {
			final double deltaHoras = ahoraHoras - this.estadoActivo.getTimestampUltimaVisitaHoras();
			if (deltaHoras > 0.01) { // Si pasaron más de ~30 segundos in-game
				this.proyectarClimaPorTiempoTranscurrido(deltaHoras);
			}
		}

		// 4. Actualizar el timestamp del mundo recién despertado
		this.estadoActivo.setTimestampUltimaVisitaHoras(ahoraHoras);

		// 5. Aplicar física y niebla del nuevo clima
		this.setClima(this.estadoActivo.getClimaActual(), 1.0);
	}

	public double getFactorBalanceoVegetacion(final double mundoX, final double mundoY) {
		final double t = this.tiempoRafaga;
		final double desfase = (mundoX * 0.04) + (mundoY * 0.02);
		final double onda = Math.sin(t + desfase) + (0.3 * Math.sin((t * 2.3) + desfase));
		return onda * 0.05 * this.estadoActivo.getFuerzaViento()
				* Math.cos(this.estadoActivo.getAnguloVientoRadianes());
	}

	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		if (this.cicloAutomaticoHabilitado) {
			this.actualizarSimuladorMeteorologico(dt);
		}

		if (this.tieneTinteClimaticoEspecial()) {
			this.actualizarTinteAtmosferico();
		}

		this.actualizarTermodinamica(dt);

		this.tiempoRafaga += dt * 1.5;
		final double rafaga = 1.0 + (Math.sin(this.tiempoRafaga) * 0.25);
		final double vxViento = this.estadoActivo.getVectorVientoX() * rafaga;
		final double vyViento = this.estadoActivo.getVectorVientoY() * rafaga;

		final double velNubes = (this.estadoActivo.getClimaActual() == TipoClima.TORMENTA_ARENA) ? 45.0 : 18.0;
		final double velNiebla = ((this.estadoActivo.getClimaActual() == TipoClima.TORMENTA_ARENA)
				|| (this.estadoActivo.getClimaActual() == TipoClima.LLUVIA_TORMENTA)
				|| (this.estadoActivo.getClimaActual() == TipoClima.VENTISCA)) ? 55.0 : 12.0;

		this.scrollNubesX = (this.scrollNubesX + (vxViento * velNubes * dt)) % RESOLUCION_NUBES;
		this.scrollNubesY = (this.scrollNubesY + (vyViento * velNubes * dt)) % RESOLUCION_NUBES;

		this.scrollNieblaX = (this.scrollNieblaX + (vxViento * velNiebla * dt)) % RESOLUCION_NIEBLA;
		this.scrollNieblaY = (this.scrollNieblaY + (vyViento * velNiebla * dt)) % RESOLUCION_NIEBLA;

		this.actualizarParticulas(vxViento, vyViento, dt);

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

		this.actualizarTormenta(dt);
	}

	private void actualizarSimuladorMeteorologico(final double dt) {
		final double tRestante = this.estadoActivo.getTiempoRestanteEstadoClima() - dt;
		this.estadoActivo.setTiempoRestanteEstadoClima(tRestante);

		if (tRestante <= 0.0) {
			this.setClima(this.estadoActivo.getClimaPronosticado(), this.estadoActivo.getDuracionTransicionClima());

			if (!this.modoPruebaRapida) {
				this.estadoActivo.setDuracionEstadoClimaSegundos(240.0 + (Math.random() * 240.0));
			}
			this.estadoActivo.setTiempoRestanteEstadoClima(this.estadoActivo.getDuracionEstadoClimaSegundos());
			this.estadoActivo.setClimaPronosticado(
					this.perfilBiomaActual.calcularSiguienteClima(this.estadoActivo.getClimaActual()));

			final double temp = this.estadoActivo.getTemperaturaCelsius();
			final TipoClima pronostico = this.estadoActivo.getClimaPronosticado();

			if (temp <= 2.0) {
				if (pronostico == TipoClima.LLUVIA_LEVE) {
					this.estadoActivo.setClimaPronosticado(TipoClima.NIEVE);
				} else if (pronostico == TipoClima.LLUVIA_TORMENTA) {
					this.estadoActivo.setClimaPronosticado(TipoClima.VENTISCA);
				}
			} else if (temp > 5.0) {
				if (pronostico == TipoClima.NIEVE) {
					this.estadoActivo.setClimaPronosticado(TipoClima.LLUVIA_LEVE);
				} else if (pronostico == TipoClima.VENTISCA) {
					this.estadoActivo.setClimaPronosticado(TipoClima.LLUVIA_TORMENTA);
				}
			}
		}
	}

	public void sincronizarConPerfilRendimiento() {
		if (this.estadoActivo != null) {
			this.setClima(this.estadoActivo.getClimaActual(), 0.5);
		}
	}

	private void actualizarTermodinamica(final double dt) {
		double hora = 12.0;
		double deltaEstacional = 0.0;

		if (Globales.GESTOR_ASTRONOMICO != null) {
			hora = Globales.GESTOR_ASTRONOMICO.getHoraActual();
			final int diaAnio = Globales.GESTOR_ASTRONOMICO.getDiaDelAnio() - 1;
			final double factorSolar = Math.sin(((diaAnio - 14.0) / 112.0) * (Math.PI * 2.0));
			deltaEstacional = (factorSolar >= 0.0) ? (factorSolar * 7.5) : (factorSolar * 9.5);
		}

		final double cicloSolarTermico = Math.sin(((hora - 8.0) / 24.0) * Math.PI * 2.0) * 4.5;
		double tempObjetivo = this.perfilBiomaActual.getTemperaturaBase() + cicloSolarTermico + deltaEstacional;
		double humObjetivo = this.perfilBiomaActual.getHumedadBase();
		double presObjetivo = 1013.25;

		switch (this.estadoActivo.getClimaActual()) {
		case LLUVIA_LEVE:
			tempObjetivo -= 2.5;
			humObjetivo = 0.85;
			presObjetivo = 1005.0;
			break;
		case LLUVIA_TORMENTA:
		case LLUVIA_ACIDA:
			tempObjetivo -= 4.5;
			humObjetivo = 0.95;
			presObjetivo = 992.0;
			break;
		case NIEVE:
			tempObjetivo -= 8.5;
			humObjetivo = 0.75;
			presObjetivo = 1002.0;
			break;
		case VENTISCA:
			tempObjetivo -= 14.0;
			humObjetivo = 0.90;
			presObjetivo = 985.0;
			break;
		case TORMENTA_ARENA:
			tempObjetivo += 6.0;
			humObjetivo = 0.10;
			presObjetivo = 998.0;
			break;
		default:
			break;
		}

		final double tempAct = this.estadoActivo.getTemperaturaCelsius()
				+ ((tempObjetivo - this.estadoActivo.getTemperaturaCelsius()) * (dt * 0.1));
		final double humAct = this.estadoActivo.getHumedadRelativa()
				+ ((humObjetivo - this.estadoActivo.getHumedadRelativa()) * (dt * 0.1));
		final double presAct = this.estadoActivo.getPresionBarometricaHPa()
				+ ((presObjetivo - this.estadoActivo.getPresionBarometricaHPa()) * (dt * 0.1));

		this.estadoActivo.setTemperaturaCelsius(tempAct);
		this.estadoActivo.setHumedadRelativa(humAct);
		this.estadoActivo.setPresionBarometricaHPa(presAct);
	}

	private void actualizarParticulas(final double vxViento, final double vyViento, final double dt) {
		if (this.cantidadParticulasActivas <= 0) {
			return;
		}

		switch (this.estadoActivo.getClimaActual()) {
		case LLUVIA_LEVE:
		case LLUVIA_TORMENTA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final double vx = (vxViento * 80.0) * p.velocidadBase;
				final double vy = (320.0 + (vyViento * 60.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case LLUVIA_ACIDA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final double vx = (vxViento * 70.0) * p.velocidadBase;
				final double vy = (340.0 + (vyViento * 50.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case NIEVE:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				p.faseOscilacion += dt * 3.0;
				final double oscilacionNieve = Math.sin(p.faseOscilacion) * 25.0;
				final double vx = ((vxViento * 40.0) + oscilacionNieve) * p.velocidadBase;
				final double vy = (65.0 + (vyViento * 20.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case VENTISCA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final double vx = (vxViento * 110.0) * p.velocidadBase;
				final double vy = (90.0 + (vyViento * 30.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case VENTOSO:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				p.faseOscilacion += dt * 5.0;
				final double aleteoHoja = Math.sin(p.faseOscilacion) * 35.0;
				final double vx = (140.0 + (vxViento * 80.0)) * p.velocidadBase;
				final double vy = (45.0 + (vyViento * 40.0) + aleteoHoja) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case PETALOS_CEREZO:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				p.faseOscilacion += dt * 4.0;
				final double balanceoPetalo = Math.sin(p.faseOscilacion) * 28.0;
				final double vx = (65.0 + (vxViento * 45.0) + balanceoPetalo) * p.velocidadBase;
				final double vy = (55.0 + (vyViento * 25.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case TORMENTA_ARENA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				p.faseOscilacion += dt * 4.0;
				final double vx = (260.0 + (vxViento * 110.0)) * p.velocidadBase;
				final double vy = (30.0 + (Math.sin(p.faseOscilacion) * 15.0) + (vyViento * 20.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case CENIZA_VOLCANICA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				p.faseOscilacion += dt * 2.5;
				final double oscilacionCeniza = Math.sin(p.faseOscilacion) * 15.0;
				final double vx = (25.0 + (vxViento * 30.0) + oscilacionCeniza) * p.velocidadBase;
				final double vy = (40.0 + (vyViento * 15.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		case ESPORAS_MAGICAS:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				p.faseOscilacion += dt * 2.0;
				final double vx = ((Math.cos(p.faseOscilacion) * 18.0) + (vxViento * 15.0)) * p.velocidadBase;
				final double vy = (-25.0 + (Math.sin(p.faseOscilacion) * 12.0)) * p.velocidadBase;
				p.actualizar(vx, vy, dt);
			}
			break;

		default:
			break;
		}
	}

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
				final double factorAmbiente = GestorMusica.getFactorAtenuacionAmbiente();
				GestorSonido.reproducirConFactor(IDSonido.TRUENO, this.volumenTruenoProporcional * factorAmbiente);

				if ((this.volumenTruenoProporcional > 0.6f) && (Globales.CAMARA != null) && (factorAmbiente > 0.0)) {
					Globales.CAMARA.aplicarTemblor(350, this.volumenTruenoProporcional * 2.5 * factorAmbiente);
				}
			}
		}
	}

	public void actualizarTinteAtmosferico() {
		if (Globales.GESTOR_LUZ == null) {
			return;
		}

		final TipoClima actual = this.estadoActivo.getClimaActual();

		if (actual == TipoClima.LLUVIA_TORMENTA) {
			Globales.GESTOR_LUZ.setTinteBiomaExterior(COLOR_TINTE_TORMENTA, 0.85);
			return;
		}
		if (actual == TipoClima.VENTISCA) {
			Globales.GESTOR_LUZ.setTinteBiomaExterior(COLOR_TINTE_VENTISCA, 0.85);
			return;
		}
		if (actual == TipoClima.TORMENTA_ARENA) {
			Globales.GESTOR_LUZ.setTinteBiomaExterior(COLOR_TINTE_ARENA, 0.85);
			return;
		}

		if ((Globales.GESTOR_ZONAS_AMBIENTE != null) && (Globales.GESTOR_ZONAS_AMBIENTE.getZonaActual() != null)) {
			final ZonaAmbiente z = Globales.GESTOR_ZONAS_AMBIENTE.getZonaActual();
			if (!z.isEsInterior()) {
				Globales.GESTOR_LUZ.setTinteBiomaExterior(z.getColorAmbiente(), z.getFactorInmersion());
				return;
			}
		}

		Globales.GESTOR_LUZ.setTinteBiomaExterior(null, 0.0);
	}

	public boolean tieneTinteClimaticoEspecial() {
		final TipoClima actual = this.estadoActivo.getClimaActual();
		return (actual == TipoClima.LLUVIA_TORMENTA) || (actual == TipoClima.VENTISCA)
				|| (actual == TipoClima.TORMENTA_ARENA);
	}

	public void pintar(final Graphics2D g) {
		final boolean esInterior = (Globales.JUGADOR != null) && (Globales.JUGADOR.getMundo() != null)
				&& (Globales.JUGADOR.getMundo().getEscenario() != null)
				&& (Globales.JUGADOR.getMundo().getEscenario().getMetadatos() != null)
				&& Globales.JUGADOR.getMundo().getEscenario().getMetadatos().esEspacioInterior();

		if (esInterior) {
			return;
		}

		final int oscuridad = (Globales.GESTOR_LUZ != null) ? Globales.GESTOR_LUZ.getAlphaOscuridadActual() : 0;
		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		// 1. Sombras de nubes diurnas
		if (this.sombrasNubesHabilitadas && (oscuridad < 130)) {
			final float factorDia = 1.0f - (oscuridad / 130.0f);
			final float opacidadEfectivaNubes = this.opacidadSombraNubes * factorDia;
			g.setComposite(obtenerComposite(opacidadEfectivaNubes));

			final int ox = Math.floorMod((int) Math.round(this.scrollNubesX - camX), RESOLUCION_NUBES);
			final int oy = Math.floorMod((int) Math.round(this.scrollNubesY - camY), RESOLUCION_NUBES);

			for (int y = -RESOLUCION_NUBES + oy; y < Constantes.ALTO_JUEGO; y += RESOLUCION_NUBES) {
				for (int x = -RESOLUCION_NUBES + ox; x < Constantes.ANCHO_JUEGO; x += RESOLUCION_NUBES) {
					Render2D.dibujarImagen(g, this.texturaSombrasNubes, x, y);
				}
			}
		}

		// 2. Niebla ambiental continua
		float opacidadEfectivaNiebla = this.opacidadNieblaActual;
		if (this.factorInmersionBioma > 0.0) {
			opacidadEfectivaNiebla = (float) (opacidadEfectivaNiebla
					+ ((this.opacidadNieblaBioma - opacidadEfectivaNiebla) * this.factorInmersionBioma));
		}

		if (opacidadEfectivaNiebla > 0.0f) {
			g.setComposite(obtenerComposite(opacidadEfectivaNiebla));
			final int ox = Math.floorMod((int) Math.round(this.scrollNieblaX - (camX * 0.5)), RESOLUCION_NIEBLA);
			final int oy = Math.floorMod((int) Math.round(this.scrollNieblaY - (camY * 0.5)), RESOLUCION_NIEBLA);

			for (int y = -RESOLUCION_NIEBLA + oy; y < Constantes.ALTO_JUEGO; y += RESOLUCION_NIEBLA) {
				for (int x = -RESOLUCION_NIEBLA + ox; x < Constantes.ANCHO_JUEGO; x += RESOLUCION_NIEBLA) {
					Render2D.dibujarImagen(g, this.texturaNiebla, x, y);
				}
			}
		}

		// 3. Partículas de suelo en lote nativo
		if (this.cantidadParticulasActivas > 0) {
			this.pintarParticulas(g);
		}

		g.setComposite(COMPOSITE_OPACO);
	}

	private void pintarParticulas(final Graphics2D g) {
		g.setComposite(COMPOSITE_OPACO);
		Render2D.registrarLlamadas(this.cantidadParticulasActivas);

		switch (this.estadoActivo.getClimaActual()) {
		case LLUVIA_LEVE:
		case LLUVIA_TORMENTA: {
			final double dxLluvia = this.estadoActivo.getVectorVientoX() * 3.5;
			g.setColor(COLOR_LLUVIA);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.drawLine((int) p.x, (int) p.y, (int) (p.x - dxLluvia), (int) (p.y - p.longitudTrazo));
			}
			break;
		}

		case LLUVIA_ACIDA: {
			final double dxAcido = this.estadoActivo.getVectorVientoX() * 3.2;
			g.setColor(COLOR_LLUVIA_ACIDA);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.drawLine((int) p.x, (int) p.y, (int) (p.x - dxAcido), (int) (p.y - (p.longitudTrazo * 1.1)));
			}
			break;
		}

		case NIEVE: {
			g.setColor(COLOR_NIEVE);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) p.tamano;
				g.fillRect((int) p.x, (int) p.y, s, s);
			}
			break;
		}

		case VENTISCA: {
			final double dxNieve = this.estadoActivo.getVectorVientoX() * 4.5;
			g.setColor(COLOR_NIEVE);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.drawLine((int) p.x, (int) p.y, (int) (p.x - dxNieve), (int) (p.y - 3));
			}
			break;
		}

		case VENTOSO: {
			g.setColor(COLOR_HOJAS_VIENTO);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.fillRect((int) p.x, (int) p.y, (int) p.tamano + 2, (int) p.tamano + 1);
			}
			break;
		}

		case PETALOS_CEREZO: {
			g.setColor(COLOR_PETALOS);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.fillRect((int) p.x, (int) p.y, (int) p.tamano + 1, (int) p.tamano + 2);
			}
			break;
		}

		case TORMENTA_ARENA: {
			g.setColor(COLOR_ARENA);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				g.fillRect((int) p.x, (int) p.y, (int) (p.tamano * 2.5), (int) p.tamano);
			}
			break;
		}

		case CENIZA_VOLCANICA: {
			g.setColor(COLOR_CENIZA);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				if ((i % 4) != 0) {
					final ParticulaClima p = this.particulas[i];
					final int s = (int) Math.max(1, p.tamano);
					g.fillRect((int) p.x, (int) p.y, s, s);
				}
			}
			g.setColor(COLOR_BRASA);
			for (int i = 0; i < this.cantidadParticulasActivas; i += 4) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) Math.max(1, p.tamano);
				g.fillRect((int) p.x, (int) p.y, s, s);
			}
			break;
		}

		case ESPORAS_MAGICAS: {
			g.setColor(COLOR_ESPORAS);
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) Math.max(1, p.tamano);
				g.fillRect((int) p.x, (int) p.y, s, s);
			}
			break;
		}

		default:
			break;
		}
	}

	public String getNombreClimaActual() {
		return (this.estadoActivo != null) ? this.estadoActivo.getClimaActual().getNombre() : "Despejado";
	}

	public String getReporteMeteorologico() {
		final int minutosRestantes = (int) Math.ceil(this.estadoActivo.getTiempoRestanteEstadoClima() / 60.0);
		switch (this.estadoActivo.getClimaPronosticado()) {
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

	public EstadoClima getEstadoActivo() {
		return this.estadoActivo;
	}

	public TipoClima getClimaActual() {
		return this.estadoActivo.getClimaActual();
	}

	public TipoClima getClimaPronosticado() {
		return this.estadoActivo.getClimaPronosticado();
	}

	public double getTemperaturaCelsius() {
		return this.estadoActivo.getTemperaturaCelsius();
	}

	public double getHumedadRelativa() {
		return this.estadoActivo.getHumedadRelativa();
	}

	public double getPresionHPa() {
		return this.estadoActivo.getPresionBarometricaHPa();
	}

	public PerfilClima getPerfilBiomaActual() {
		return this.perfilBiomaActual;
	}

	public void setPerfilBioma(final PerfilClima nuevoPerfil) {
		if ((nuevoPerfil == null) || (nuevoPerfil == this.perfilBiomaActual)) {
			return;
		}
		this.perfilBiomaActual = nuevoPerfil;
		this.estadoActivo.setClimaPronosticado(
				this.perfilBiomaActual.calcularSiguienteClima(this.estadoActivo.getClimaActual()));
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
		this.estadoActivo.setClimaActual(nuevoClima);
		this.setNivelNiebla(nuevoClima.getNivelNiebla(), duracionTransicionSegundos);
		this.setColorNiebla(nuevoClima.getColorNiebla());

		if (ConfiguracionGrafica.getPerfil() == principal.configuracion.PerfilRendimiento.POTATO) {
			this.setSombrasNubesHabilitadas(false);
		} else {
			this.setSombrasNubesHabilitadas(nuevoClima.isTieneNubes());
		}

		this.setOpacidadSombraNubes(nuevoClima.getOpacidadNubes());
		this.setTormentaActiva(nuevoClima.isTieneTormentaRayos());
		this.estadoActivo.setViento(nuevoClima.getAnguloVientoGrados(), nuevoClima.getFuerzaViento());

		int baseParticulas = nuevoClima.getCantidadParticulas();
		if (ConfiguracionGrafica.getPerfil() != null) {
			switch (ConfiguracionGrafica.getPerfil()) {
			case POTATO:
				baseParticulas = (int) Math.round(baseParticulas * 0.40);
				break;
			case BASICO:
				baseParticulas = (int) Math.round(baseParticulas * 0.65);
				break;
			case MEDIO:
				baseParticulas = (int) Math.round(baseParticulas * 0.85);
				break;
			case ALTO:
			default:
				break;
			}
		}

		this.cantidadParticulasActivas = Math.min(MAX_PARTICULAS, baseParticulas);
		this.actualizarTinteAtmosferico();

		switch (nuevoClima) {
		case LLUVIA_LEVE:
		case LLUVIA_ACIDA:
			GestorMusica.reproducirAmbienteClima(IDMusica.AMBIENTE_LLUVIA);
			break;
		case LLUVIA_TORMENTA:
			GestorMusica.reproducirAmbienteClima(IDMusica.AMBIENTE_TORMENTA);
			break;
		case VENTOSO:
		case VENTISCA:
		case TORMENTA_ARENA:
			GestorMusica.reproducirAmbienteClima(IDMusica.AMBIENTE_VENTOSO);
			break;
		default:
			GestorMusica.detenerAmbienteClima();
			break;
		}
	}

	/**
	 * Proyecta en O(1) la atmósfera del mundo cuando el jugador regresa tras una
	 * ausencia, resolviendo si la tormenta cesó o si el mundo transitó de estación
	 * (Zero-GC).
	 */
	private void proyectarClimaPorTiempoTranscurrido(final double deltaHoras) {
		// Conversión: ¿cuántos segundos de simulación equivalen a esas horas in-game?
		final double segsPorHora = (Globales.GESTOR_ASTRONOMICO != null)
				? (Globales.GESTOR_ASTRONOMICO.getDuracionDiaSegundos() / 24.0)
				: 75.0; // 1800s / 24h = 75 segundos por hora por defecto

		final double segundosPasados = deltaHoras * segsPorHora;

		// ESCALA 1: Ausencia muy breve (la tormenta aún no terminaba)
		if (segundosPasados < this.estadoActivo.getTiempoRestanteEstadoClima()) {
			final double tRestante = this.estadoActivo.getTiempoRestanteEstadoClima() - segundosPasados;
			this.estadoActivo.setTiempoRestanteEstadoClima(tRestante);
			return;
		}

		// ESCALA 2: Pasaron varias horas (la tormenta terminó mientras estabas fuera,
		// mismo día)
		if (deltaHoras < 24.0) {
			final TipoClima nuevoClima = this.estadoActivo.getClimaPronosticado();
			this.estadoActivo.setClimaActual(nuevoClima);
			this.estadoActivo.setClimaPronosticado(this.perfilBiomaActual.calcularSiguienteClima(nuevoClima));

			final double nuevaDuracion = 240.0 + (Math.random() * 240.0);
			this.estadoActivo.setDuracionEstadoClimaSegundos(nuevaDuracion);

			final double sobrante = segundosPasados - this.estadoActivo.getTiempoRestanteEstadoClima();
			this.estadoActivo.setTiempoRestanteEstadoClima(Math.max(30.0, nuevaDuracion - (sobrante % nuevaDuracion)));
			return;
		}

		// ESCALA 3: Pasaron días, semanas o meses (la atmósfera cicló por completo)
		final principal.astronomia.Estacion estActual = (Globales.GESTOR_ASTRONOMICO != null)
				? Globales.GESTOR_ASTRONOMICO.getEstacionActual()
				: principal.astronomia.Estacion.PRIMAVERA;

		final TipoClima climaSorteado = this.perfilBiomaActual.calcularSiguienteClima(TipoClima.DESPEJADO, estActual);
		this.estadoActivo.setClimaActual(climaSorteado);
		this.estadoActivo.setClimaPronosticado(this.perfilBiomaActual.calcularSiguienteClima(climaSorteado, estActual));

		final double nuevaDuracion = 240.0 + (Math.random() * 240.0);
		this.estadoActivo.setDuracionEstadoClimaSegundos(nuevaDuracion);
		this.estadoActivo.setTiempoRestanteEstadoClima(nuevaDuracion * (0.3 + (Math.random() * 0.7)));
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
		return this.estadoActivo.getFuerzaViento();
	}

	public void forzarSiguienteClima() {
		this.estadoActivo.setTiempoRestanteEstadoClima(0.0);
	}
	// =========================================================================
	// MÉTODOS DELEGADOS HACIA EL ESTADO CLIMÁTICO ACTIVO (FACHADA ZERO-GC)
	// =========================================================================

	public double getTiempoRestanteEstadoClima() {
		return (this.estadoActivo != null) ? Math.max(0.0, this.estadoActivo.getTiempoRestanteEstadoClima()) : 0.0;
	}

	public double getDuracionEstadoClimaSegundos() {
		return (this.estadoActivo != null) ? this.estadoActivo.getDuracionEstadoClimaSegundos() : 360.0;
	}

	public void setViento(final double gradosDireccion, final double fuerza) {
		if (this.estadoActivo != null) {
			this.estadoActivo.setViento(gradosDireccion, fuerza);
		}
	}

	public void setFuerzaViento(final double fuerza) {
		if (this.estadoActivo != null) {
			this.estadoActivo.setViento(Math.toDegrees(this.estadoActivo.getAnguloVientoRadianes()), fuerza);
		}
	}

	public void setDireccionViento(final double gradosDireccion) {
		if (this.estadoActivo != null) {
			this.estadoActivo.setViento(gradosDireccion, this.estadoActivo.getFuerzaViento());
		}
	}

	public void activarModoPruebaRapida(final double segundosPorClima, final double segundosTransicion) {
		this.modoPruebaRapida = true;
		this.cicloAutomaticoHabilitado = true;
		if (this.estadoActivo != null) {
			this.estadoActivo.setDuracionEstadoClimaSegundos(Math.max(1.0, segundosPorClima));
			this.estadoActivo.setTiempoRestanteEstadoClima(this.estadoActivo.getDuracionEstadoClimaSegundos());
			this.estadoActivo.setDuracionTransicionClima(Math.max(0.5, segundosTransicion));
		}
	}
}