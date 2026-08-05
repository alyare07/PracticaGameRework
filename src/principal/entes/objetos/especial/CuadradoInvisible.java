package principal.entes.objetos.especial;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class CuadradoInvisible extends Complemento {
	private final int CODIGO_MODELO;

	public CuadradoInvisible(int x, int y, final int codModelo) {
		super(x, y, 0);
		this.CODIGO_MODELO = codModelo;
	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagenRefCamara(g, getTextura(), this.x, this.y);
	}

	@Override
	public void pintarFijo(final Graphics2D g) {
		final int lado = getAncho();
		DibujoDebug.dibujarRectanguloRelleno(g, getPosicionXInt(), getPosicionYInt(), lado, lado,
				ListaObjetosEspeciales.getModeloCuadrado(CODIGO_MODELO).getColor());
	}

	private static final long serialVersionUID = -8572826474094657688L;

	@Override
	public int getCodigoModelo() {
		return this.CODIGO_MODELO;
	}

	@Override
	public int getAncho() {
		return ListaObjetosEspeciales.getModeloCuadrado(CODIGO_MODELO).getLado();
	}

	@Override
	public int getAlto() {
		return ListaObjetosEspeciales.getModeloCuadrado(CODIGO_MODELO).getLado();
	}

	@Override
	public BufferedImage getTextura() {
		return ListaObjetosEspeciales.getModeloCuadrado(CODIGO_MODELO).getImagen();
	}

	@Override
	public boolean esSolido() {
		return ListaObjetosEspeciales.getModeloCuadrado(CODIGO_MODELO).esSolido();
	}

	@Override
	public Objeto copiar() {
		return new CuadradoInvisible(this.x, this.y, CODIGO_MODELO);
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	@Override
	public void modificarPosicionX(double desplazamientoX) {
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

}
