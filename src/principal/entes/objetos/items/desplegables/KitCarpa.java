package principal.entes.objetos.items.desplegables;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.construccion.AccionColocacion;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.fabricables.Carpa;
import principal.entes.objetos.items.Consumible;
import principal.igu.textos.TipoTextoFlotante;
import principal.mapa.Mundo;
import principal.recursos.TexturaItem;
import principal.utilidades.Globales;

/**
 * Ítem consumible que permite al pionero plantar una Carpa de supervivencia en
 * el mundo mediante posicionamiento con el ratón (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8 - Tent Deployment Item)
 */
public class KitCarpa extends Consumible {

	private static final long serialVersionUID = 1L;

	public static final String COD_KIT_CARPA = "Kit de Carpa";
	private static final int LIMITE_PILA = 1;

	private final AccionColocacion accionDespliegue = new AccionColocacion() {
		@Override
		public void colocar(final int x, final int y, final Mundo mundo) {
			final Carpa nuevaCarpa = new Carpa(x, y);
			if (mundo.meterEntidad(nuevaCarpa)) {
				mundo.notificarModificacionEstructura();
				Globales.GESTOR_PARTICULAS.emitirPolvo(x + 16, y + 16, 10);
				Globales.GESTOR_TEXTOS.agregarTexto("¡Carpa montada!", x + 16, y - 8, TipoTextoFlotante.CRAFTEO_EXITO);
			}
		}
	};

	public KitCarpa(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, COD_KIT_CARPA, COD_KIT_CARPA, TexturaItem.KIT_CARPA_INV, TexturaItem.KIT_CARPA_MAPA,
				LIMITE_PILA);
		this.precioBasePlata = 45L;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public KitCarpa(final int cantidad) {
		this(0, 0, cantidad);
	}

	@Override
	public void consumir(final Criatura c) {
		if (Globales.GESTOR_CONSTRUCCION == null) {
			return;
		}
		Globales.GESTOR_CONSTRUCCION.iniciarDespliegueItem(this, this.getTexturaInventario(), Carpa.LADO_CARPA,
				Carpa.LADO_CARPA, this.accionDespliegue);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Refugio portatil de lona reforzada.");
		listaInfo.add("Protege de la intemperie y permite descansar.");
		listaInfo.add("Shift + [E] sobre la carpa para desarmarla.");
	}

	@Override
	public Objeto copiar() {
		final KitCarpa copia = new KitCarpa(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad());
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

	public static KitCarpa crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new KitCarpa(1);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final int cant = (json.get("cant") != null) ? ((Number) json.get("cant")).intValue() : 1;

		final KitCarpa kit = new KitCarpa(x, y, cant);
		if (json.get("precio") != null) {
			kit.setPrecioBasePlata(((Number) json.get("precio")).longValue());
		}
		return kit;
	}

	@Override
	public String exportarTipoItem() {
		return "KitCarpa";
	}
}