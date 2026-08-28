package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.entes.Ente;
import principal.inventario.Contenedor;
import principal.inventario.vault.InventarioVault;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

public class ArbolCofre extends Objeto implements Contenedor {
	private static final long serialVersionUID = 651599209121613328L;
	private final InventarioVault INVENTARIO;

	public ArbolCofre(final int x, final int y) {
		super(x, y);
		this.INVENTARIO = new InventarioVault(this, 20, 5, "Inventario arbol secreto");
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.INVENTARIO.actualizarEstadoCofre();
	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt() - 14,
				this.getPosicionYInt() - 18);
		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	@Override
	public String getNombreContenedor() {
		return this.INVENTARIO.getNombre();
	}

	@Override
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}

	@Override
	public Ente getEntePropietario() {
		return this;
	}

	@Override
	public int getAncho() {
		return 5;
	}

	@Override
	public int getAlto() {
		// TODO Auto-generated method stub
		return 12;
	}

	@Override
	public BufferedImage getTextura() {
		return Textura.getTextura(Textura.TEXTURA_x32_ARBOL_2);
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	public Objeto copiar() {
		return new ArbolCofre(this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public double getPosicionX() {
		return this.getPosicionXInt();
	}

	@Override
	public double getPosicionY() {
		return this.getPosicionYInt();
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

}
