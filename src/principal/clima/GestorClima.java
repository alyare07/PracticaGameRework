package principal.clima;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import principal.iluminacion.IntensidadNiebla;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestor maestro del subsistema meteorológico y atmosférico del motor 2D
 * (Zero-GC / O(1)).
 * 
 * @version 12.0
 */
public class GestorClima {

	private static final int MAX_PARTICULAS = 400;
	private static final int MAX_ESTRELLAS_FUGAZ = 6;

	private static final int RESOLUCION_NUBES = 512;
	private static final int RESOLUCION_NIEBLA = 256;
	private static final int ANCHO_AURORA_HD = 640;
	private static final int ALTO_AURORA_HD = 120;

	private static final Color COLOR_LLUVIA = new Color(185, 215, 245, 175);
	private static final Color COLOR_NIEVE = new Color(245, 250, 255, 210);
	private static final Color COLOR_ARENA = new Color(215, 170, 95, 190);
	private static final Color COLOR_HOJAS_VIENTO = new Color(135, 190, 60, 220);
	private static final Color COLOR_CENIZA = new Color(75, 70, 75, 200);
	private static final Color COLOR_BRASA = new Color(255, 125, 30, 235);
	private static final Color COLOR_ESPORAS = new Color(110, 235, 255, 210);
	private static final Color COLOR_PETALOS = new Color(255, 175, 205, 220);
	private static final Color COLOR_LLUVIA_ACIDA = new Color(135, 240, 90, 185);
	private static final Color COLOR_AURORA_POLVO = new Color(100, 255, 215, 220);
	private static final Color COLOR_ESTRELLA_PARTICULA = new Color(255, 235, 150, 230);
	private static final Color COLOR_ESTRELLA_TRAIL = new Color(255, 255, 220, 240);

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
	private final BufferedImage texturaAurora;

	private boolean cicloAutomaticoHabilitado = true;
	private PerfilClima perfilBiomaActual = PerfilClima.TEMPLADO_BOSQUE;
	private TipoClima climaActual = TipoClima.DESPEJADO;
	private TipoClima climaPronosticado = TipoClima.LLUVIA_LEVE;

	private double duracionEstadoClimaSegundos = 360.0;
	private double tiempoRestanteEstadoClima = 360.0;
	private double duracionTransicionClima = 5.0;
	private boolean modoPruebaRapida = false;

	private double temperaturaActualCelsius = 20.0;
	private double humedadActual = 0.50;
	private double presionBarometricaHPa = 1013.25;

	private double anguloVientoRadianes = Math.toRadians(45.0);
	private double fuerzaViento = 1.0;
	private double tiempoRafaga = 0.0;
	private double vectorVientoX = 0.0;
	private double vectorVientoY = 0.0;

	private final ParticulaClima[] particulas = new ParticulaClima[MAX_PARTICULAS];
	private int cantidadParticulasActivas = 0;

	private final double[] estrellaX = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaY = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaVelX = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaVelY = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaVida = new double[MAX_ESTRELLAS_FUGAZ];
	private final double[] estrellaLongitud = new double[MAX_ESTRELLAS_FUGAZ];
	private final boolean[] estrellaActiva = new boolean[MAX_ESTRELLAS_FUGAZ];
	private double temporizadorSpawnEstrella = 0.0;

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

	private double faseOndaAurora = 0.0;

	public GestorClima() {
		this.texturaSombrasNubes = this.hornearTexturaSombrasNubes();
		this.texturaNiebla = this.hornearTexturaNiebla();
		this.texturaAurora = this.hornearTexturaAurora();

		for (int i = 0; i < MAX_PARTICULAS; i++) {
			this.particulas[i] = new ParticulaClima();
			this.particulas[i].inicializarAleatorio();
		}

		for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
			this.estrellaActiva[i] = false;
		}

		this.actualizarVectoresViento();
		this.climaPronosticado = this.perfilBiomaActual.calcularSiguienteClima(this.climaActual);
	}

	private BufferedImage hornearTexturaSombrasNubes() {
		final int size = RESOLUCION_NUBES;
		final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

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
				final int rgba = (alpha << 24) | (0 << 16) | (0 << 8) | 0;
				img.setRGB(x, y, rgba);
			}
		}
		return img;
	}

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

	private BufferedImage hornearTexturaAurora() {
		final BufferedImage img = new BufferedImage(ANCHO_AURORA_HD, ALTO_AURORA_HD, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final GradientPaint grad = new GradientPaint(0, 0, new Color(40, 240, 180, 0), 0, ALTO_AURORA_HD / 2,
				new Color(70, 255, 190, 160));
		g.setPaint(grad);
		g.fillRect(0, 0, ANCHO_AURORA_HD, ALTO_AURORA_HD / 2);

		final GradientPaint grad2 = new GradientPaint(0, ALTO_AURORA_HD / 2, new Color(130, 80, 255, 140), 0,
				ALTO_AURORA_HD, new Color(130, 80, 255, 0));
		g.setPaint(grad2);
		g.fillRect(0, ALTO_AURORA_HD / 2, ANCHO_AURORA_HD, ALTO_AURORA_HD / 2);

		g.dispose();
		return img;
	}

	public double getFactorBalanceoVegetacion(final double mundoX, final double mundoY) {
		final double t = this.tiempoRafaga;
		final double desfase = (mundoX * 0.04) + (mundoY * 0.02);
		final double onda = Math.sin(t + desfase) + (0.3 * Math.sin((t * 2.3) + desfase));
		return onda * 0.05 * this.fuerzaViento * Math.cos(this.anguloVientoRadianes);
	}

	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		if (this.cicloAutomaticoHabilitado) {
			this.actualizarSimuladorMeteorologico(dt);
		}

		this.actualizarTermodinamica(dt);

		this.tiempoRafaga += dt * 1.5;
		final double rafaga = 1.0 + (Math.sin(this.tiempoRafaga) * 0.25);
		final double vxViento = this.vectorVientoX * rafaga;
		final double vyViento = this.vectorVientoY * rafaga;

		final double velNubes = (this.climaActual == TipoClima.TORMENTA_ARENA) ? 45.0 : 18.0;
		final double velNiebla = (this.climaActual == TipoClima.TORMENTA_ARENA) ? 55.0 : 12.0;

		this.scrollNubesX = (this.scrollNubesX + (vxViento * velNubes * dt)) % RESOLUCION_NUBES;
		this.scrollNubesY = (this.scrollNubesY + (vyViento * velNubes * dt)) % RESOLUCION_NUBES;

		this.scrollNieblaX = (this.scrollNieblaX + (vxViento * velNiebla * dt)) % RESOLUCION_NIEBLA;
		this.scrollNieblaY = (this.scrollNieblaY + (vyViento * velNiebla * dt)) % RESOLUCION_NIEBLA;

		if (this.climaActual == TipoClima.AURORA_BOREAL) {
			this.faseOndaAurora += dt * 0.8;
		}

		this.actualizarParticulas(vxViento, vyViento, dt);
		this.actualizarEstrellasFugaces(dt);

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

	private void actualizarEstrellasFugaces(final double dt) {
		if (this.climaActual == TipoClima.LLUVIA_ESTRELLAS) {
			this.temporizadorSpawnEstrella += dt;
			if (this.temporizadorSpawnEstrella >= 0.45) {
				this.temporizadorSpawnEstrella = 0.0;

				for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
					if (!this.estrellaActiva[i]) {
						this.estrellaActiva[i] = true;
						this.estrellaX[i] = 100.0 + (Math.random() * (Constantes.ANCHO_JUEGO + 150));
						this.estrellaY[i] = -20.0 + (Math.random() * 80.0);
						this.estrellaVelX[i] = -(420.0 + (Math.random() * 200.0));
						this.estrellaVelY[i] = 280.0 + (Math.random() * 150.0);
						this.estrellaVida[i] = 0.55 + (Math.random() * 0.35);
						this.estrellaLongitud[i] = 25.0 + (Math.random() * 30.0);
						break;
					}
				}
			}
		}

		for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
			if (this.estrellaActiva[i]) {
				this.estrellaX[i] += this.estrellaVelX[i] * dt;
				this.estrellaY[i] += this.estrellaVelY[i] * dt;
				this.estrellaVida[i] -= dt;

				if ((this.estrellaVida[i] <= 0.0) || (this.estrellaX[i] < -100)
						|| (this.estrellaY[i] > (Constantes.ALTO_JUEGO + 50))) {
					this.estrellaActiva[i] = false;
				}
			}
		}
	}

	private void actualizarSimuladorMeteorologico(final double dt) {
		this.tiempoRestanteEstadoClima -= dt;

		if (this.tiempoRestanteEstadoClima <= 0.0) {
			this.setClima(this.climaPronosticado, this.duracionTransicionClima);

			if (!this.modoPruebaRapida) {
				this.duracionEstadoClimaSegundos = 240.0 + (Math.random() * 240.0);
			}
			this.tiempoRestanteEstadoClima = this.duracionEstadoClimaSegundos;
			this.climaPronosticado = this.perfilBiomaActual.calcularSiguienteClima(this.climaActual);
		}
	}

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
		case ECLIPSE_SOLAR:
			tempObjetivo -= 6.0;
			humObjetivo = 0.40;
			presObjetivo = 1020.0;
			break;
		case AURORA_BOREAL:
			tempObjetivo -= 3.0;
			humObjetivo = 0.60;
			presObjetivo = 1015.0;
			break;
		default:
			break;
		}

		this.temperaturaActualCelsius += (tempObjetivo - this.temperaturaActualCelsius) * (dt * 0.1);
		this.humedadActual += (humObjetivo - this.humedadActual) * (dt * 0.1);
		this.presionBarometricaHPa += (presObjetivo - this.presionBarometricaHPa) * (dt * 0.1);
	}

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
				p.faseOscilacion += dt * 2.0;
				vx = ((Math.cos(p.faseOscilacion) * 18.0) + (vxViento * 15.0)) * p.velocidadBase;
				vy = (-25.0 + (Math.sin(p.faseOscilacion) * 12.0)) * p.velocidadBase;
				break;

			case AURORA_BOREAL:
				p.faseOscilacion += dt * 1.5;
				vx = (Math.sin(p.faseOscilacion) * 15.0) * p.velocidadBase;
				vy = (-15.0 + (Math.cos(p.faseOscilacion) * 10.0)) * p.velocidadBase;
				break;

			case LLUVIA_ESTRELLAS:
				p.faseOscilacion += dt * 4.0;
				vx = (-80.0 + (Math.sin(p.faseOscilacion) * 20.0)) * p.velocidadBase;
				vy = (90.0 + (Math.cos(p.faseOscilacion) * 20.0)) * p.velocidadBase;
				break;

			default:
				break;
			}

			p.actualizar(vx, vy, dt);
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
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO ATMOSFÉRICO EN PANTALLA (ZERO-GC)
	// =========================================================================

	public void pintar(final Graphics2D g) {
		final int oscuridad = (Globales.GESTOR_LUZ != null) ? Globales.GESTOR_LUZ.getAlphaOscuridadActual() : 0;

		final int camX = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionXInt() : 0;
		final int camY = (Globales.CAMARA != null) ? Globales.CAMARA.getPosicionYInt() : 0;

		// 1. Cintas de Aurora Boreal
		if (this.climaActual == TipoClima.AURORA_BOREAL) {
			this.pintarAuroraBoreal(g, camX);
		}

		// 2. Sombras de nubes
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

		// 3. Capa de niebla dinámica con Paralaje
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

		// 4. Estrellas Fugaces / Meteoros
		if (this.climaActual == TipoClima.LLUVIA_ESTRELLAS) {
			this.pintarEstrellasFugaces(g);
		}

		// 5. Partículas atmosféricas
		if (this.cantidadParticulasActivas > 0) {
			this.pintarParticulas(g);
		}

		g.setComposite(COMPOSITE_OPACO);
	}

	private void pintarAuroraBoreal(final Graphics2D g, final int camX) {
		g.setComposite(obtenerComposite(0.55f));
		final double onda = Math.sin(this.faseOndaAurora) * 20.0;
		final int ox = Math.floorMod((int) Math.round((this.faseOndaAurora * 15.0) - (camX * 0.2)), ANCHO_AURORA_HD);

		for (int x = -ANCHO_AURORA_HD + ox; x < Constantes.ANCHO_JUEGO; x += ANCHO_AURORA_HD) {
			final int y = (int) Math.round(-15.0 + onda);
			Render2D.dibujarImagen(g, this.texturaAurora, x, y);
			Render2D.dibujarImagen(g, this.texturaAurora, x + 120, y + 25);
		}
	}

	private void pintarEstrellasFugaces(final Graphics2D g) {
		g.setComposite(COMPOSITE_OPACO);
		for (int i = 0; i < MAX_ESTRELLAS_FUGAZ; i++) {
			if (this.estrellaActiva[i]) {
				final int x1 = (int) Math.round(this.estrellaX[i]);
				final int y1 = (int) Math.round(this.estrellaY[i]);
				final int x2 = (int) Math.round(this.estrellaX[i] + (this.estrellaLongitud[i] * 0.8));
				final int y2 = (int) Math.round(this.estrellaY[i] - (this.estrellaLongitud[i] * 0.5));

				Render2D.dibujarLinea(g, x1, y1, x2, y2, COLOR_ESTRELLA_TRAIL);
				Render2D.dibujarRectanguloRelleno(g, x1 - 1, y1 - 1, 3, 3, Color.WHITE);
			}
		}
	}

	private void pintarParticulas(final Graphics2D g) {
		g.setComposite(COMPOSITE_OPACO);

		switch (this.climaActual) {
		case LLUVIA_LEVE:
		case LLUVIA_TORMENTA:
			final double dxLluvia = this.vectorVientoX * 3.5;
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				Render2D.dibujarLinea(g, (int) p.x, (int) p.y, (int) (p.x - dxLluvia), (int) (p.y - p.longitudTrazo),
						COLOR_LLUVIA);
			}
			break;

		case LLUVIA_ACIDA:
			final double dxAcido = this.vectorVientoX * 3.2;
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				Render2D.dibujarLinea(g, (int) p.x, (int) p.y, (int) (p.x - dxAcido),
						(int) (p.y - (p.longitudTrazo * 1.1)), COLOR_LLUVIA_ACIDA);
			}
			break;

		case NIEVE:
		case VENTISCA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) p.tamano;
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, s, s, COLOR_NIEVE);
			}
			break;

		case VENTOSO:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, (int) p.tamano + 2, (int) p.tamano + 1,
						COLOR_HOJAS_VIENTO);
			}
			break;

		case PETALOS_CEREZO:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, (int) p.tamano + 1, (int) p.tamano + 2,
						COLOR_PETALOS);
			}
			break;

		case TORMENTA_ARENA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, (int) (p.tamano * 2.5), (int) p.tamano,
						COLOR_ARENA);
			}
			break;

		case CENIZA_VOLCANICA:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final Color color = ((i % 4) == 0) ? COLOR_BRASA : COLOR_CENIZA;
				final int s = (int) Math.max(1, p.tamano);
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, s, s, color);
			}
			break;

		case ESPORAS_MAGICAS:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) Math.max(1, p.tamano);
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, s, s, COLOR_ESPORAS);
			}
			break;

		case AURORA_BOREAL:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) Math.max(1, p.tamano);
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, s, s, COLOR_AURORA_POLVO);
			}
			break;

		case LLUVIA_ESTRELLAS:
			for (int i = 0; i < this.cantidadParticulasActivas; i++) {
				final ParticulaClima p = this.particulas[i];
				final int s = (int) Math.max(1, p.tamano);
				Render2D.dibujarRectanguloRelleno(g, (int) p.x, (int) p.y, s + 1, s + 1, COLOR_ESTRELLA_PARTICULA);
			}
			break;

		default:
			break;
		}
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public String getNombreClimaActual() {
		return (this.climaActual != null) ? this.climaActual.getNombre() : "Despejado";
	}

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
		case AURORA_BOREAL:
			return "El cielo nocturno se iluminara con una mistica aurora boreal en " + minutosRestantes + " min.";
		case ECLIPSE_SOLAR:
			return "¡Advertencia! Se avecina un eclipse solar que sumira la tierra en sombras en " + minutosRestantes
					+ " min.";
		case LLUVIA_ESTRELLAS:
			return "El cielo se prepara para una lluvia cosmica de estrellas fugaces en " + minutosRestantes + " min.";
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