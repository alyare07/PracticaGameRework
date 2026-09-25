package principal.entes.objetos.recursos.minerales;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.recursos.RecursoCosechable;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Base abstracta para todas las formaciones minerales y vetas minables con pico
 * (Zero-GC / O(1)). Estandariza la huella física en 16x16 px y resuelve la
 * textura perezosamente desde la hoja maestra.
 */
public abstract class MineralCosechable extends RecursoCosechable {

	private static final long serialVersionUID = 1L;

	public static final int LADO = 16;
	protected final int spriteIndex;

	public MineralCosechable(final int x, final int y, final double durabilidadMaxima, final int spriteIndex) {
		super(x, y, durabilidadMaxima, TipoHerramienta.PICO);
		this.spriteIndex = Math.max(0, spriteIndex);
	}

	@Override
	public BufferedImage getTextura() {
		if (Globales.GESTOR_TEXTURAS == null) {
			return null;
		}
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.MINERALES_COSECHABLES_16);
		return (hoja != null) ? hoja.getSprite(this.spriteIndex) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), LADO, LADO);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public int getAncho() {
		return LADO;
	}

	@Override
	public int getAlto() {
		return LADO;
	}

	public int getSpriteIndex() {
		return this.spriteIndex;
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 6);
	}
}