package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.mapa.Mundo;

public abstract class Bandido extends Enemigo{
    protected final AnimacionesBandido ANIMACION;

    public Bandido(final double x, final double y, final double vida, final double vidaMaxima, final Mundo mundo) {
	super(x, y, 12, 20, vida, vidaMaxima, mundo);
	this.ANIMACION = new AnimacionesBandido();
    }

    @Override
    public void pintar(final Graphics2D g) {
	super.pintar(g);

    }

    @Override
    public void modificarPosicionX(final double desplazamientoX) {
	super.modificarPosicionX(desplazamientoX);
    }

    @Override
    public void modificarPosicionY(final double desplazamientoY) {
	super.modificarPosicionY(desplazamientoY);
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
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String exportarTipoCriatura() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected int getTiempoMsEsperaRegenVida() {
	// TODO Auto-generated method stub
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
