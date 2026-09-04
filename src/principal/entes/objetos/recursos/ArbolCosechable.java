package principal.entes.objetos.recursos;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

public class ArbolCosechable extends RecursoCosechable {

	private static final long serialVersionUID = 1L;

	private boolean esTocon = false;
	private final ClaveHoja hoja;
	private final int spriteIndex;

	public ArbolCosechable(final int x, final int y, final ClaveHoja hoja, final int spriteIndex) {
		super(x, y, 100.0, TipoHerramienta.HACHA);
		this.hoja = (hoja != null) ? hoja : ClaveHoja.ARBOLES_32;
		this.spriteIndex = Math.max(0, spriteIndex);
	}

	public ArbolCosechable(final int x, final int y) {
		this(x, y, ClaveHoja.ARBOLES_32, 0);
	}

	@Deprecated
	public ArbolCosechable(final int x, final int y, final int codViejo) {
		this(x, y, ClaveHoja.ARBOLES_32, 0);
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt() + 10, this.getPosicionYInt() + 18, 12, 14);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public void destruir(final Ente causante) {
		if (!this.esTocon) {
			this.soltarBotin();
			this.esTocon = true;
			this.durabilidadMaxima = 40.0;
			this.durabilidad = this.durabilidadMaxima;
			this.activarShake();
		} else {
			this.soltarBotin();
			super.destruir(causante);
		}
	}

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}

		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (this.getAlto() / 2);

		final int cantidadMadera = this.esTocon ? 2 : 5;
		this.mundo.meterEntidad(RecursoMaterial.crearMadera(dropX, dropY, cantidadMadera));
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 18);
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 6);
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
		return new ArbolCosechable(this.getPosicionXInt(), this.getPosicionYInt(), this.hoja, this.spriteIndex);
	}

	public boolean isEsTocon() {
		return this.esTocon;
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

	public static ArbolCosechable crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new ArbolCosechable(0, 0, ClaveHoja.ARBOLES_32, 0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;

		ClaveHoja hoja = ClaveHoja.ARBOLES_32;
		if (json.get("hoja") != null) {
			try {
				hoja = ClaveHoja.valueOf(json.get("hoja").toString());
			} catch (final Exception ignored) {
			}
		}

		final int spriteIndex = (json.get("spriteIndex") != null) ? ((Number) json.get("spriteIndex")).intValue() : 0;
		return new ArbolCosechable(x, y, hoja, spriteIndex);
	}
}