package principal.entes.objetos.items.desplegables;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.construccion.AccionColocacion;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.fabricables.Cama;
import principal.entes.objetos.items.Consumible;
import principal.igu.textos.TipoTextoFlotante;
import principal.mapa.Mundo;
import principal.recursos.TexturaItem;
import principal.utilidades.Globales;

/**
 * Ítem consumible que permite al jugador colocar una Cama de descanso en
 * interiores mediante posicionamiento con el ratón (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8 - Bed Deployment Item)
 */
public class KitCama extends Consumible {

	private static final long serialVersionUID = 1L;

	public static final String COD_KIT_CAMA = "Kit de Cama";
	private static final int LIMITE_PILA = 1;

	private final AccionColocacion accionDespliegue = new AccionColocacion() {
		@Override
		public void colocar(final int x, final int y, final Mundo mundo) {
			final Cama nuevaCama = new Cama(x, y);
			if (mundo.meterEntidad(nuevaCama)) {
				mundo.notificarModificacionEstructura();
				Globales.GESTOR_PARTICULAS.emitirPolvo(x + 8, y + 16, 8);
				Globales.GESTOR_TEXTOS.agregarTexto("¡Cama instalada!", x + 8, y - 8, TipoTextoFlotante.CRAFTEO_EXITO);
			}
		}
	};

	public KitCama(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, COD_KIT_CAMA, COD_KIT_CAMA, TexturaItem.KIT_CAMA_INV, TexturaItem.KIT_CAMA_MAPA,
				LIMITE_PILA);
		this.precioBasePlata = 60L;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public KitCama(final int cantidad) {
		this(0, 0, cantidad);
	}

	@Override
	public void consumir(final Criatura c) {
		if (Globales.GESTOR_CONSTRUCCION == null) {
			return;
		}
		// Despliegue holográfico de 16x32 px (1x2 tiles)
		Globales.GESTOR_CONSTRUCCION.iniciarDespliegueItem(this, this.getTexturaInventario(), Cama.ANCHO_CAMA,
				Cama.ALTO_CAMA, this.accionDespliegue);
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Mueble de descanso para interiores.");
		listaInfo.add("Restaura salud, energia y temperatura al 100%.");
		listaInfo.add("Usar para colocar en el suelo con el raton.");
	}

	@Override
	public Objeto copiar() {
		final KitCama copia = new KitCama(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad());
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

	public static KitCama crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new KitCama(1);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final int cant = (json.get("cant") != null) ? ((Number) json.get("cant")).intValue() : 1;

		final KitCama kit = new KitCama(x, y, cant);
		if (json.get("precio") != null) {
			kit.setPrecioBasePlata(((Number) json.get("precio")).longValue());
		}
		return kit;
	}

	@Override
	public String exportarTipoItem() {
		return "KitCama";
	}
}