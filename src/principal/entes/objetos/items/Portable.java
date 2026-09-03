package principal.entes.objetos.items;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.recursos.TexturaItem;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public abstract class Portable extends Item {

	private static final long serialVersionUID = 4861089138825196600L;

	protected final String nombre;
	protected final String codigoModelo;
	protected final TexturaItem texturaInv;
	protected final TexturaItem texturaMapa;

	public Portable(final int x, final int y, final String codModelo, final String nombre, final TexturaItem texturaInv,
			final TexturaItem texturaMapa) {
		super(x, y);
		this.codigoModelo = (codModelo != null) ? codModelo : "";
		this.nombre = (nombre != null) ? nombre : this.codigoModelo;
		this.texturaInv = texturaInv;
		this.texturaMapa = texturaMapa;
	}

	public Portable(final String codModelo, final String nombre, final TexturaItem texturaInv,
			final TexturaItem texturaMapa) {
		this(0, 0, codModelo, nombre, texturaInv, texturaMapa);
	}

	// Sobrecarga de compatibilidad transitoria para armas y equipo
	public Portable(final int x, final int y, final String codModelo) {
		this(x, y, codModelo, codModelo, resolverTexturaInvDefecto(codModelo), resolverTexturaMapaDefecto(codModelo));
	}

	public Portable(final String codModelo) {
		this(0, 0, codModelo);
	}

	public String getCodigoModelo() {
		return this.codigoModelo;
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public BufferedImage getTexturaInventario() {
		return (this.texturaInv != null) ? Globales.GESTOR_TEXTURAS.get(this.texturaInv)
				: Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public void pintarInventario(final Graphics2D g, final int x, final int y) {
		Render2D.dibujarImagen(g, this.getTexturaInventario(), x, y);
	}

	@Override
	public int getTipoItem() {
		return Item.COD_ITEM_PORTABLE;
	}

	@Override
	public int getAncho() {
		return 16;
	}

	@Override
	public int getAlto() {
		return 16;
	}

	@Override
	public BufferedImage getTextura() {
		return (this.texturaMapa != null) ? Globales.GESTOR_TEXTURAS.get(this.texturaMapa)
				: Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public boolean esSolido() {
		return false;
	}

	@Override
	public String getNombre() {
		return this.nombre;
	}

	@Override
	protected JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public String exportarTipoItem() {
		return "Portable";
	}

	private static TexturaItem resolverTexturaInvDefecto(final String cod) {
		if (cod == null) {
			return TexturaItem.PISTOLA_INV;
		}
		if (cod.contains("Pistola")) {
			return TexturaItem.PISTOLA_INV;
		}
		if (cod.contains("Recortada")) {
			return TexturaItem.ESCOPETA_RECORTADA_INV;
		}
		if (cod.contains("Tactica")) {
			return TexturaItem.ESCOPETA_TACTICA_INV;
		}
		if (cod.contains("Automatica")) {
			return TexturaItem.ESCOPETA_AUTOMATICA_INV;
		}
		if (cod.contains("Subfusil")) {
			return TexturaItem.SUBFUSIL_LIGERO_INV;
		}
		if (cod.contains("Rifle") || cod.contains("Asalto")) {
			return TexturaItem.RIFLE_ASALTO_INV;
		}
		if (cod.contains("Ametralladora")) {
			return TexturaItem.AMETRALLADORA_PESADA_INV;
		}
		if (cod.contains("Botas")) {
			return TexturaItem.BOTAS_CUERO_INV;
		}
		if (cod.contains("Casco")) {
			return TexturaItem.CASCO_BASE_INV;
		}
		if (cod.contains("Armadura")) {
			return TexturaItem.ARMADURA_BASE_INV;
		}
		if (cod.contains("Oro")) {
			return TexturaItem.ANILLO_ORO_INV;
		}
		if (cod.contains("Plata")) {
			return TexturaItem.ANILLO_PLATA_INV;
		}
		if (cod.contains("Hacha")) {
			return TexturaItem.ESMERALDA_INV;
		}
		if (cod.contains("Pico")) {
			return TexturaItem.ANILLO_ORO_INV;
		}
		return TexturaItem.PISTOLA_INV;
	}

	private static TexturaItem resolverTexturaMapaDefecto(final String cod) {
		if (cod == null) {
			return TexturaItem.PISTOLA_MAPA;
		}
		if (cod.contains("Pistola")) {
			return TexturaItem.PISTOLA_MAPA;
		}
		if (cod.contains("Recortada")) {
			return TexturaItem.ESCOPETA_RECORTADA_MAPA;
		}
		if (cod.contains("Tactica")) {
			return TexturaItem.ESCOPETA_TACTICA_MAPA;
		}
		if (cod.contains("Automatica")) {
			return TexturaItem.ESCOPETA_AUTOMATICA_MAPA;
		}
		if (cod.contains("Subfusil")) {
			return TexturaItem.SUBFUSIL_LIGERO_MAPA;
		}
		if (cod.contains("Rifle") || cod.contains("Asalto")) {
			return TexturaItem.RIFLE_ASALTO_MAPA;
		}
		if (cod.contains("Ametralladora")) {
			return TexturaItem.AMETRALLADORA_PESADA_MAPA;
		}
		if (cod.contains("Botas")) {
			return TexturaItem.BOTAS_CUERO_MAPA;
		}
		if (cod.contains("Casco")) {
			return TexturaItem.CASCO_BASE_MAPA;
		}
		if (cod.contains("Armadura")) {
			return TexturaItem.ARMADURA_BASE_MAPA;
		}
		if (cod.contains("Oro")) {
			return TexturaItem.ANILLO_ORO_MAPA;
		}
		if (cod.contains("Plata")) {
			return TexturaItem.ANILLO_PLATA_MAPA;
		}
		return TexturaItem.PISTOLA_MAPA;
	}
}