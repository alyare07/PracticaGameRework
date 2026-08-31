package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.animaciones.Animacion;
import principal.animaciones.criaturas.AnimacionesBandido;
import principal.mapa.Mundo;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Variación del enemigo Bandido especializado en combate cuerpo a cuerpo con
 * garrote.
 * <p>
 * <b>CARACTERÍSTICAS TÉCNICAS (v2.5):</b>
 * <ul>
 * <li><b>Hit-Frame Driven Combat:</b> El cálculo de impacto se sincroniza con
 * el fotograma exacto del sprite (Frame 2), eliminando la latencia y los
 * proyectiles temporales.</li>
 * <li><b>Orientación Reactiva:</b> Ajusta la dirección de ataque hacia el
 * objetivo fijado.</li>
 * <li><b>Zero-GC:</b> Reutilización de cajas de colisión y llamadas directas de
 * daño.</li>
 * </ul>
 * </p>
 * 
 * @version 2.5 (Java 8 Compatible - Zero-GC Architecture)
 */
public class BandidoGarrote extends Bandido {

	private boolean pintarAtaque;
	private boolean impactoRealizadoEnCiclo;

	/**
	 * Fotograma de la animación en el que se produce el contacto físico del
	 * garrote.
	 */
	private static final int FOTOGRAMA_IMPACTO = 2;

	public BandidoGarrote(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.ataque = 15.0; // Daño base cuerpo a cuerpo calibrado
	}

	@Override
	public void actualizar() {
		super.actualizar();

		// Inicio de la secuencia de golpe cargado
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

		// Sincronización del fotograma de impacto y fin de la animación
		if (this.pintarAtaque) {
			final Animacion anim = this.ANIMACION.getAnimacion(AnimacionesBandido.GARROTE_ATACANDO, this.direccion);
			if (anim != null) {
				// DISPARO DE IMPACTO EN EL FRAME EXACTO DEL SPRITE
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

	/**
	 * Evalúa el área frontal de contacto y aplica el daño directamente sobre el
	 * objetivo fijado.
	 */
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
		return this.getPosicionX() + (this.ANCHO / 2.0);
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return this.getPosicionY() + (this.ALTO / 2.0);
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