package principal.mapa.escenario.tps;

import java.awt.Rectangle;

import org.json.simple.JSONObject;

import principal.persistencia.json.LectorJSON;

/**
 * Serializador polimórfico universal para la jerarquía de PuertaTP y condiciones de acceso.
 */
public final class SerializadorPuertas {

	private SerializadorPuertas() {
	}

	@SuppressWarnings("unchecked")
	public static JSONObject serializarPuerta(final PuertaTP puerta) {
		if (puerta == null) {
			return null;
		}
		final JSONObject json = new JSONObject();

		if (puerta instanceof PuertaMundo) {
			final PuertaMundo pm = (PuertaMundo) puerta;
			json.put("tipo", "PuertaMundo");
			json.put("mundo", pm.getNombreMundoDestino());
			json.put("spawn", pm.getNombreSpawnDestino());

		} else if (puerta instanceof PuertaMapa) {
			final PuertaMapa pmapa = (PuertaMapa) puerta;
			json.put("tipo", "PuertaMapa");
			json.put("mapa", pmapa.getRutaMapaDestino());
			json.put("mundo", pmapa.getNombreMundoDestino());
			json.put("spawn", pmapa.getNombreSpawnDelMundoDestino());

		} else if (puerta instanceof PuertaArea) {
			final PuertaArea pa = (PuertaArea) puerta;
			json.put("tipo", "PuertaArea");
			json.put("destX", Integer.valueOf(pa.getXDestino()));
			json.put("destY", Integer.valueOf(pa.getYDestino()));
			json.put("destW", Integer.valueOf(pa.getWDestino()));
			json.put("destH", Integer.valueOf(pa.getHDestino()));

		} else if (puerta instanceof PuertaSalidaCueva) {
			json.put("tipo", "PuertaSalidaCueva");
		}

		return json;
	}

	public static PuertaTP deserializarPuerta(final JSONObject json) {
		if (json == null) {
			return null;
		}

		final String tipo = LectorJSON.getString(json, "tipo", "");

		switch (tipo) {
		case "PuertaMundo": {
			final String mundo = LectorJSON.getString(json, "mundo", "exterior");
			final String spawn = LectorJSON.getString(json, "spawn", "Comienzo");
			return new PuertaMundo(mundo, spawn);
		}
		case "PuertaMapa": {
			final String ruta = LectorJSON.getString(json, "mapa", "Mapa1");
			final String mundo = LectorJSON.getString(json, "mundo", "exterior");
			final String spawn = LectorJSON.getString(json, "spawn", "Comienzo");
			return new PuertaMapa(ruta, mundo, spawn, false, null);
		}
		case "PuertaArea": {
			final int dx = LectorJSON.getInt(json, "destX", 0);
			final int dy = LectorJSON.getInt(json, "destY", 0);
			final int dw = LectorJSON.getInt(json, "destW", 16);
			final int dh = LectorJSON.getInt(json, "destH", 16);
			return new PuertaArea(new Rectangle(dx, dy, dw, dh));
		}
		case "PuertaSalidaCueva":
			return new PuertaSalidaCueva();

		default:
			return null;
		}
	}
}