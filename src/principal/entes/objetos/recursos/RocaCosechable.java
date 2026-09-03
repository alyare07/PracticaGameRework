package principal.entes.objetos.recursos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class RocaCosechable extends RecursoCosechable {

	private static final long serialVersionUID = 1L;

	private final ClaveHoja hoja;
	private final int spriteIndex;

	public RocaCosechable(final int x, final int y, final ClaveHoja hoja, final int spriteIndex) {
		super(x, y, 120.0, TipoHerramienta.PICO);
		this.hoja = (hoja != null) ? hoja : ClaveHoja.DUNGEON_16;
		this.spriteIndex = Math.max(0, spriteIndex);
	}

	public RocaCosechable(final int x, final int y) {
		this(x, y, ClaveHoja.DUNGEON_16, 813);
	}

	// Sobrecarga de compatibilidad transitoria
	@Deprecated
	public RocaCosechable(final int x, final int y, final int codViejo) {
		this(x, y, ClaveHoja.DUNGEON_16, 813);
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt() + 4, this.getPosicionYInt() + 8, 24, 20);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}

		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (this.getAlto() / 2);

		this.mundo.meterEntidad(RecursoMaterial.crearPiedra(dropX, dropY, 4));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 20);
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 8);
	}

	@Override
	public BufferedImage getTextura() {
		final HojaSprite h = Globales.GESTOR_TEXTURAS.getHoja(this.hoja);
		return (h != null) ? h.getSprite(this.spriteIndex) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public int getAncho() {
		return 32;
	}

	@Override
	public int getAlto() {
		return 32;
	}

	@Override
	public Objeto copiar() {
		return new RocaCosechable(this.getPosicionXInt(), this.getPosicionYInt(), this.hoja, this.spriteIndex);
	}
}