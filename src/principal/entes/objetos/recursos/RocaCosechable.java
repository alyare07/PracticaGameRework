package principal.entes.objetos.recursos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

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

	@Deprecated
	public RocaCosechable(final int x, final int y, final int codViejo) {
		this(x, y, ClaveHoja.DUNGEON_16, 813);
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), 16, 16);
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

	public ClaveHoja getHoja() {
		return this.hoja;
	}

	public int getSpriteIndex() {
		return this.spriteIndex;
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("hoja", this.hoja.name());
		json.put("spriteIndex", Integer.valueOf(this.spriteIndex));
		return json;
	}

	public static RocaCosechable crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new RocaCosechable(0, 0, ClaveHoja.DUNGEON_16, 813);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;

		ClaveHoja hoja = ClaveHoja.DUNGEON_16;
		if (json.get("hoja") != null) {
			try {
				hoja = ClaveHoja.valueOf(json.get("hoja").toString());
			} catch (final Exception ignored) {
			}
		}

		final int spriteIndex = (json.get("spriteIndex") != null) ? ((Number) json.get("spriteIndex")).intValue() : 813;
		return new RocaCosechable(x, y, hoja, spriteIndex);
	}
}