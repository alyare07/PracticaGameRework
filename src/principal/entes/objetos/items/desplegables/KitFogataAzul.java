package principal.entes.objetos.items.desplegables;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.construccion.AccionColocacion;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Fogata;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.igu.textos.TipoTextoFlotante;
import principal.mapa.Mundo;
import principal.recursos.ClaveHoja;
import principal.recursos.TexturaItem;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;

/**
 * Ítem consumible de inventario que despliega una Fogata Mística de Fuego Azul
 * inmune al clima y con aura de regeneración de salud (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class KitFogataAzul extends Consumible {

	private static final long serialVersionUID = 1L;

	public static final String COD_KIT_FOGATA_AZUL = "Kit de Fogata Mística";
	private static final int LIMITE_PILA = 10;

	// Callback preasignado Zero-GC: instancia con fuegoAzul = true
	private final AccionColocacion accionDespliegue = new AccionColocacion() {
		@Override
		public void colocar(final int x, final int y, final Mundo mundo) {
			final Fogata nuevaFogata = new Fogata(x, y, 4, true, true);
			mundo.meterEntidad(nuevaFogata);
			Globales.GESTOR_PARTICULAS.emitirMagia(x + 8, y + 8, 12);
			Globales.GESTOR_TEXTOS.agregarTexto("¡Fogata Mística instalada!", x + 8, y - 8,
					TipoTextoFlotante.ORO_EXP);
		}
	};

	public KitFogataAzul(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, COD_KIT_FOGATA_AZUL, COD_KIT_FOGATA_AZUL, TexturaItem.MADERA_INV,
				TexturaItem.MADERA_MAPA, LIMITE_PILA);
		this.precioBasePlata = 45L;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public KitFogataAzul(final int cantidad) {
		this(0, 0, cantidad);
	}

	@Override
	public void consumir(final Criatura c) {
		if (Globales.GESTOR_CONSTRUCCION == null) {
			return;
		}

		Globales.GESTOR_CONSTRUCCION.iniciarDespliegueItem(this, this.getTexturaInventario(), Constantes.LADO_TILE,
				Constantes.LADO_TILE, this.accionDespliegue);
	}

	@Override
	public BufferedImage getTexturaInventario() {
		// Fila 1, Frame 0 (Índice 4 en spritesheet de 4 columnas) = Fuego Azul
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.FOGATA);
		return (hoja != null) ? hoja.getSprite(4) : super.getTexturaInventario();
	}

	@Override
	public BufferedImage getTextura() {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.FOGATA);
		return (hoja != null) ? hoja.getSprite(4) : super.getTextura();
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Fogata mística impregnada con energía arcana.");
		listaInfo.add("Inmune a la lluvia, tormentas y ventiscas.");
		listaInfo.add("Regenera la salud de los aliados cercanos.");
		listaInfo.add("Usar para apuntar y colocar con el ratón.");
	}

	@Override
	public Objeto copiar() {
		final KitFogataAzul copia = new KitFogataAzul(this.getPosicionXInt(), this.getPosicionYInt(),
				this.getCantidad());
		copia.setPrecioBasePlata(this.precioBasePlata);
		return copia;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.getCodigoModelo());
		json.put("cant", Integer.valueOf(this.getCantidad()));
		json.put("precio", Long.valueOf(this.precioBasePlata));
		return json;
	}

	public static KitFogataAzul crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new KitFogataAzul(1);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final int cant = (json.get("cant") != null) ? ((Number) json.get("cant")).intValue() : 1;

		final KitFogataAzul kit = new KitFogataAzul(x, y, cant);
		if (json.get("precio") != null) {
			kit.setPrecioBasePlata(((Number) json.get("precio")).longValue());
		}
		return kit;
	}

	@Override
	public String exportarTipoItem() {
		return "KitFogataAzul";
	}
}