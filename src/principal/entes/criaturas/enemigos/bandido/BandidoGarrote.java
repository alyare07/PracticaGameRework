package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.animaciones.Animacion;
import principal.animaciones.criaturas.AnimacionesBandido;
import principal.mapa.Mundo;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class BandidoGarrote extends Bandido {
	private static final String NOMBRE = "Bandido Con Garrote";
	private boolean pintarAtaque;
	private boolean impactoRealizadoEnCiclo;
	private static final int FOTOGRAMA_IMPACTO = 2;

	public BandidoGarrote(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.ataque = 15.0;
	}

	@Override
	public void actualizar() {
		super.actualizar();

		if (this.realizandoAtaque && !this.pintarAtaque) {
			this.pintarAtaque = true;
			this.impactoRealizadoEnCiclo = false;

			if (this.objetivoActual != null) {
				this.setDireccionMirandoCriatura(this.objetivoActual);
			}

			final Animacion anim = this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion);
			if (anim != null) {
				anim.reiniciarAnimacion();
			}
		}

		if (this.pintarAtaque) {
			final Animacion anim = this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion);
			if (anim != null) {
				anim.actualizar();

				if (!this.impactoRealizadoEnCiclo && (anim.getSpritePosicion() >= FOTOGRAMA_IMPACTO)) {
					this.impactoRealizadoEnCiclo = true;
					this.ejecutarGolpeFisico();
				}

				if (anim.animacionFinalizada()) {
					this.pintarAtaque = false;
					anim.reiniciarAnimacion();
				}
			}
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