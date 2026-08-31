package principal.entes.objetos.especial;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.utilidades.Render2D;

public class CuadradoInvisible extends Complemento {
	private final int CODIGO_MODELO;

	public CuadradoInvisible(final int x, final int y, final int codModelo) {
		super(x, y, 0);
		this.CODIGO_MODELO = codModelo;
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public void pintarFijo(final Graphics2D g) {
		final int lado = this.getAncho();
		Render2D.dibujarRectanguloRelleno(g, this.getPosicionXInt(), this.getPosicionYInt(), lado, lado,
				ListaObjetosEspeciales.getModeloCuadrado(this.CODIGO_MODELO).getColor());
	}

	private static final long serialVersionUID = -8572826474094657688L;

	@Override
	public int getCodigoModelo() {
		return this.CODIGO_MODELO;
	}

	@Override
	public int getAncho() {
		return ListaObjetosEspeciales.getModeloCuadrado(this.CODIGO_MODELO).getLado();
	}

	@Override
	public int getAlto() {
		return ListaObjetosEspeciales.getModeloCuadrado(this.CODIGO_MODELO).getLado();
	}

	@Override
	public BufferedImage getTextura() {
		return ListaObjetosEspeciales.getModeloCuadrado(this.CODIGO_MODELO).getImagen();
	}

	@Override
	public boolean esSolido() {
		return ListaObjetosEspeciales.getModeloCuadrado(this.CODIGO_MODELO).esSolido();
	}

	@Override
	public Objeto copiar() {
		return new CuadradoInvisible(this.getPosicionXInt(), this.getPosicionYInt(), this.CODIGO_MODELO);
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

}
