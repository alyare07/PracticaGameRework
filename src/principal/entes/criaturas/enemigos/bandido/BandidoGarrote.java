package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.mapa.Mundo;

public class BandidoGarrote extends Bandido {
	private boolean pintarAtaque;

	public BandidoGarrote(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.ataque = 0.5;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		if (this.realizandoAtaque && !this.pintarAtaque) {
			this.pintarAtaque = true;
		}
		if (this.pintarAtaque && this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion)
				.animacionFinalizada()) {
			this.pintarAtaque = false;
			this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion).reiniciarAnimacion();
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
		} else if (this.estaEstadoPersiguiendo() || this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_CAMINANDO, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_ESTANDAR, this.atrasDeComplemento, true, flash);
		}
	}

	@Override
	protected double getXRangoAtaqueMele() {
		return this.getPosicionX() + (this.ANCHO / 2);
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return this.getPosicionY() + (this.ALTO / 2);
	}

	@Override
	protected double getAlcanceRangoAtaqueMele() {
		return 12;
	}

	@Override
	protected double getGrosorRangoAtaqueMele() {
		return 6;
	}
}