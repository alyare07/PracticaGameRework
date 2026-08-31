package principal.entes.criaturas.enemigos.bandido;

import org.json.simple.JSONObject;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.entes.facciones.GestorFacciones;
import principal.mapa.Mundo;

/**
 * Representa la base genérica de enemigos tipo Bandido.
 * <p>
 * Configura la identidad de facción {@link GestorFacciones#FACCION_BANDIDOS},
 * márgenes de recorte de sprite y constantes temporales de IA y regeneración.
 * </p>
 * 
 * @version 2.5 (Java 8 Compatible - Zero-GC Architecture)
 */
public abstract class Bandido extends Enemigo {

	protected final AnimacionesBandido ANIMACION;

	public Bandido(final double x, final double y, final double vida, final double vidaMaxima, final Mundo mundo) {
		super(x, y, 12, 20, vida, vidaMaxima, mundo);

		// Asignación de facción oficial de Bandidos
		this.setFaccion(GestorFacciones.FACCION_BANDIDOS);

		this.ANIMACION = new AnimacionesBandido();
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@Override
	protected JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Bandido";
	}

	@Override
	protected int getTiempoMsEsperaRegenVida() {
		return 10000;
	}

	@Override
	protected int getTiempoMsEsperaAtacado() {
		return 7000;
	}

	@Override
	protected int getTiempoMsBusquedaFueraRango() {
		return 8000;
	}

	@Override
	protected int getTiempoMsEsperaAtaqueInicial() {
		return 1000;
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 750;
	}
}