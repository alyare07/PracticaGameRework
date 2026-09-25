package principal.entes.objetos.cofres;

import java.awt.image.BufferedImage;

import principal.entes.objetos.Objeto;
import principal.inventario.vault.InventarioVault.EstadoInventario;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class CofrePequeño extends Cofre {

	private static final long serialVersionUID = 4592661837024054777L;
	private final int ANCHO = 16;
	private final int ALTO = 16;

	public CofrePequeño(final int x, final int y) {
		super(x, y, 3, 3, "Cofre Pequeño");
	}

	@Override
	public BufferedImage getTextura() {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.COFRES_16);
		if (hoja == null) {
			return Globales.GESTOR_TEXTURAS.getTexturaError();
		}

		if (this.getInventario().getEstadoInventario() == EstadoInventario.CERRADO) {
			return hoja.getSprite(1);
		}
		return hoja.getSprite(0);
	}

	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
	}

	@Override
	public Objeto copiar() {
		return new CofrePequeño(this.getPosicionXInt(), this.getPosicionYInt());
	}

	@Override
	public boolean esSolido() {
		return true;
	}
}