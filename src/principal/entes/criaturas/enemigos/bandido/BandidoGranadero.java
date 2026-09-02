package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.items.arrojadizos.granadas.Granada;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.iluminacion.CalculadorSigilo;
import principal.mapa.Mundo;

public class BandidoGranadero extends Bandido {
	private static final String NOMBRE = "Bandido Granadero";
	private final Granada granada;

	public BandidoGranadero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.granada = new GranadaT1(100);
		this.areaDeteccionAncho = 300;
		this.areaDeteccionAlto = 300;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		if (this.granada.getCantidad() <= 1) {
			this.granada.establecerCantidad(100);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);
	}

	private void pintarSprite(final Graphics2D g) {
		final boolean flash = this.estaEnFlashDanio();

		if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.ESTANDAR, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.CAMINANDO, this.atrasDeComplemento, true, flash);
		}
	}

	@Override
	protected void actualizarAtaque() {
		if (this.objetivoActual == null) {
			this.desactivarModoAgresivo();
			return;
		}

		// --- FASE 1: Lanzamiento al culminar carga ---
		if (this.realizandoAtaque) {
			if (this.GT_CARGA_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
				this.enAccion = false;

				final int targetX = this.objetivoActual.getCentroX();
				final int targetY = this.objetivoActual.getCentroY();

				this.setDireccionMirandoCriatura(this.objetivoActual);
				this.granada.arrojar(targetX, targetY, this.direccion, this.mundo, this);

				this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
				this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();
				this.realizandoAtaque = false;
				this.removerEstado(Estado.ATACANDO);
				this.removerEstado(Estado.ARROJANDO);
				this.meterEstado(Estado.PERSIGUIENDO);
			}
			return;
		}

		// Cooldown tras lanzamiento
		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			final double rangoVision = this.areaDeteccionAncho / 2.0;
			if (!CalculadorSigilo.puedeDetectar(this, this.objetivoActual, rangoVision)) {
				this.reposicionarseHaciaObjetivo();
			}
			return;
		}

		// --- FASE 2: Detección y aproximación ---
		final double rangoVision = this.areaDeteccionAncho / 2.0;
		final boolean objetivoEnVision = CalculadorSigilo.puedeDetectar(this, this.objetivoActual, rangoVision);
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (objetivoEnVision) {
			this.meterEstado(Estado.ATACANDO);
			this.meterEstado(Estado.ARROJANDO);
			this.removerEstado(Estado.CAMINANDO);
			this.removerEstado(Estado.PERSIGUIENDO);

			this.setDireccionMirandoCriatura(this.objetivoActual);

			if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
				if (!this.realizandoAtaque) {
					this.realizandoAtaque = true;
					this.GT_CARGA_ATAQUE.establecerReferenciaTiempoActual();
				}
			}
			this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();

		} else if (dentroTiempoBusqueda) {
			this.reposicionarseHaciaObjetivo();
		} else {
			this.desactivarModoAgresivo();
		}
	}

	private void reposicionarseHaciaObjetivo() {
		this.removerEstado(Estado.ATACANDO);
		this.removerEstado(Estado.ARROJANDO);
		this.meterEstado(Estado.PERSIGUIENDO);

		if (this.objetivoActual instanceof Jugador) {
			this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
		} else {
			if (this.GT_ACTUALIZACION_A_ESTRELLA.transcurrioMiliSegundos(500)
					|| ((this.nodoADestino == null) && this.recorridoA.isEmpty())) {
				this.calcularRutaAEstrella(this.objetivoActual.getCentroX(), this.objetivoActual.getCentroY());
				this.GT_ACTUALIZACION_A_ESTRELLA.establecerReferenciaTiempoActual();
			}
			this.moverANodoADestino();
		}
		this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
	}

	@Override
	public String exportarSubtipoBandido() {
		return "Granadero";
	}

	@Override
	protected double getXRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getAlcanceRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getGrosorRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected int getTiempoMsEsperaAtaqueInicial() {
		return 800;
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 1800;
	}

	@Override
	public String getNombre() {
		return NOMBRE;
	}
}