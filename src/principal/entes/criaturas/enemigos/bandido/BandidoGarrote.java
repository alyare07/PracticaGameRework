package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;

import principal.animaciones.Animacion;
import principal.animaciones.criaturas.AnimacionesBandido;
import principal.ia.arbol.FabricaArbolesIA;
import principal.mapa.Mundo;

public class BandidoGarrote extends Bandido {

	private static final String NOMBRE = "Bandido Con Garrote";
	private static final int FOTOGRAMA_IMPACTO = 2;

	private boolean impactoRealizadoEnCiclo;

	public BandidoGarrote(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.ataque = 15.0;
		this.configurarFootprint(8, 8, 0);
		this.arbolComportamiento = FabricaArbolesIA.ARBOL_BANDIDO_MELE;
	}

	@Override
	public void actualizar() {
		super.actualizar();

		if (this.estaEstadoAtacando()) {
			final Animacion anim = this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion);
			if (anim != null) {
				anim.actualizar();

				if (!this.impactoRealizadoEnCiclo && (anim.getSpritePosicion() >= FOTOGRAMA_IMPACTO)) {
					this.impactoRealizadoEnCiclo = true;
				}

				if (anim.animacionFinalizada()) {
					anim.reiniciarAnimacion();
					this.impactoRealizadoEnCiclo = false;
					this.removerEstado(Estado.ATACANDO);
				}
			}
		}
	}

	@Override
	protected int obtenerClaveAnimacionActiva() {
		if (this.estaEstadoAtacando()) {
			return AnimacionesBandido.GARROTE_ATACANDO;
		}
		return this.estaEnMovimientoFisico() ? AnimacionesBandido.GARROTE_CAMINANDO
				: AnimacionesBandido.GARROTE_ESTANDAR;
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);
	}

	private void pintarSprite(final Graphics2D g) {
		final boolean flash = this.estaEnFlashDanio();

		if (this.estaEstadoAtacando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_ATACANDO, this.atrasDeComplemento, true, flash);
		} else if (this.estaEnMovimientoFisico()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_CAMINANDO, this.atrasDeComplemento, true, flash);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.GARROTE_ESTANDAR, this.atrasDeComplemento, true, flash);
		}
	}

	@Override
	public String exportarSubtipoBandido() {
		return "Garrote";
	}

	@Override
	public String getNombre() {
		return NOMBRE;
	}
}