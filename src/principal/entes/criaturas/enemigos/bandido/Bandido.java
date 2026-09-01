package principal.entes.criaturas.enemigos.bandido;

import org.json.simple.JSONObject;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.entes.facciones.GestorFacciones;
import principal.mapa.Mundo;

public abstract class Bandido extends Enemigo {

	protected final AnimacionesBandido ANIMACION;

	public Bandido(final double x, final double y, final double vida, final double vidaMaxima, final Mundo mundo) {
		super(x, y, 12, 20, vida, vidaMaxima, mundo);

		this.setFaccion(GestorFacciones.FACCION_BANDIDOS);
		this.ANIMACION = new AnimacionesBandido();
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.actualizarAnimacion();
	}

	protected void actualizarAnimacion() {
		final String tipo = this.obtenerClaveAnimacionActiva();
		this.ANIMACION.actualizar(this.direccion, tipo);
	}

	protected String obtenerClaveAnimacionActiva() {
		return this.estaEstadoCaminando() ? AnimacionesBandido.CAMINANDO : AnimacionesBandido.ESTANDAR;
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("vida", this.vida);
		json.put("vidaMaxima", this.vidaMaxima);
		json.put("subtipo", this.exportarSubtipoBandido());
		return json;
	}

	public abstract String exportarSubtipoBandido();

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