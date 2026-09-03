package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.animaciones.Animacion;
import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.Jugador;
import principal.iluminacion.CalculadorSigilo;
import principal.mapa.Mundo;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class BandidoGarrote extends Bandido {

	private static final String NOMBRE = "Bandido Con Garrote";
	private static final int FOTOGRAMA_IMPACTO = 2;

	private boolean pintarAtaque;
	private boolean impactoRealizadoEnCiclo;

	public BandidoGarrote(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.ataque = 15.0;
	}

	@Override
	public void actualizar() {
		super.actualizar();

		// Actualización y sincronización visual del fotograma de impacto
		if (this.pintarAtaque) {
			final Animacion anim = this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion);
			if (anim != null) {
				anim.actualizar();

				// Aplica el daño exactamente UNA vez al alcanzar el fotograma clave
				if (!this.impactoRealizadoEnCiclo && (anim.getSpritePosicion() >= FOTOGRAMA_IMPACTO)) {
					this.impactoRealizadoEnCiclo = true;
					this.ejecutarGolpeFisico();
				}

				// Finalización del ataque al terminar la animación
				if (anim.animacionFinalizada()) {
					this.pintarAtaque = false;
					this.realizandoAtaque = false;
					anim.reiniciarAnimacion();

					this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
					this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();

					this.removerEstado(Estado.ATACANDO);
					this.meterEstado(Estado.PERSIGUIENDO);
				}
			}
		}
	}

	@Override
	protected void actualizarAtaque() {
		if (this.objetivoActual == null) {
			this.desactivarModoAgresivo();
			return;
		}

		// Si ya está en plena animación de golpe, esperamos a que termine en
		// actualizar()
		if (this.realizandoAtaque) {
			return;
		}

		// Respetar el tiempo de enfriamiento entre golpes
		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		final double rangoVision = this.areaDeteccionAncho / 2.0;
		final boolean objetivoVisible = CalculadorSigilo.puedeDetectar(this, this.objetivoActual, rangoVision);
		final boolean dentroTiempoBusqueda = !this.GE_FUERA_DE_RANGO
				.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango());

		if (objetivoVisible || dentroTiempoBusqueda) {
			this.rangoAtaqueMele = this.obtenerRangoAtaqueMeleValido();

			if (this.rangoAtaqueMele != null) {
				// En rango Melee -> Iniciar animación y bloqueo de ataque
				this.meterEstado(Estado.ATACANDO);
				this.removerEstado(Estado.CAMINANDO);
				this.removerEstado(Estado.PERSIGUIENDO);
				this.setDireccionMirandoCriatura(this.objetivoActual);

				if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
					this.realizandoAtaque = true;
					this.pintarAtaque = true;
					this.impactoRealizadoEnCiclo = false;

					final Animacion anim = this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO,
							this.direccion);
					if (anim != null) {
						anim.reiniciarAnimacion();
					}
				}
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();

			} else {
				// Fuera de rango Melee -> Persecución táctica
				this.removerEstado(Estado.ATACANDO);
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
			}
		} else {
			this.desactivarModoAgresivo();
		}
	}

	private void ejecutarGolpeFisico() {
		final Rectangle rangoMele = this.obtenerRangoAtaqueMeleValido();

		if ((rangoMele != null) && (this.objetivoActual != null) && !this.objetivoActual.estaEliminado()) {
			if (rangoMele.intersects(this.objetivoActual.getArea())) {
				this.objetivoActual.recibirAtaque(this.ataque, this);
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);
	}

	private void pintarSprite(final Graphics2D g) {
		final boolean flash = this.estaEnFlashDanio();

		if (this.pintarAtaque) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_ATACANDO, this.atrasDeComplemento, true, flash);
		} else if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_ESTANDAR, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_CAMINANDO, this.atrasDeComplemento, true, flash);
		}
	}

	@Override
	public String exportarSubtipoBandido() {
		return "Garrote";
	}

	@Override
	protected int obtenerClaveAnimacionActiva() {
		if (this.pintarAtaque) {
			return AnimacionesBandido.GARROTE_ATACANDO;
		}
		return this.estaEstadoCaminando() ? AnimacionesBandido.GARROTE_CAMINANDO : AnimacionesBandido.GARROTE_ESTANDAR;
	}

	@Override
	protected double getXRangoAtaqueMele() {
		return this.getPosicionX() + (this.ANCHO / 2.0);
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return this.getPosicionY() + (this.ALTO / 2.0);
	}

	@Override
	protected double getAlcanceRangoAtaqueMele() {
		return 12.0;
	}

	@Override
	protected double getGrosorRangoAtaqueMele() {
		return 6.0;
	}

	@Override
	public String getNombre() {
		return NOMBRE;
	}
}