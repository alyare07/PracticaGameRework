package principal.persistencia.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Fachada utilitaria de extracción segura de tipos primitivos y estructuras
 * desde org.json.simple. Evita ClassCastException y NullPointerException
 * mediante valores de reserva por defecto (Zero-Crash / Java 8).
 */
public final class LectorJSON {

	private LectorJSON() {
	}

	public static int getInt(final JSONObject json, final String clave, final int valorPorDefecto) {
		if (json == null || clave == null) {
			return valorPorDefecto;
		}
		final Object val = json.get(clave);
		if (val instanceof Number) {
			return ((Number) val).intValue();
		}
		if (val != null) {
			try {
				return Integer.parseInt(val.toString().trim());
			} catch (final NumberFormatException ignored) {
			}
		}
		return valorPorDefecto;
	}

	public static double getDouble(final JSONObject json, final String clave, final double valorPorDefecto) {
		if (json == null || clave == null) {
			return valorPorDefecto;
		}
		final Object val = json.get(clave);
		if (val instanceof Number) {
			return ((Number) val).doubleValue();
		}
		if (val != null) {
			try {
				return Double.parseDouble(val.toString().trim());
			} catch (final NumberFormatException ignored) {
			}
		}
		return valorPorDefecto;
	}

	public static long getLong(final JSONObject json, final String clave, final long valorPorDefecto) {
		if (json == null || clave == null) {
			return valorPorDefecto;
		}
		final Object val = json.get(clave);
		if (val instanceof Number) {
			return ((Number) val).longValue();
		}
		if (val != null) {
			try {
				return Long.parseLong(val.toString().trim());
			} catch (final NumberFormatException ignored) {
			}
		}
		return valorPorDefecto;
	}

	public static boolean getBoolean(final JSONObject json, final String clave, final boolean valorPorDefecto) {
		if (json == null || clave == null) {
			return valorPorDefecto;
		}
		final Object val = json.get(clave);
		if (val instanceof Boolean) {
			return ((Boolean) val).booleanValue();
		}
		if (val != null) {
			return Boolean.parseBoolean(val.toString().trim());
		}
		return valorPorDefecto;
	}

	public static String getString(final JSONObject json, final String clave, final String valorPorDefecto) {
		if (json == null || clave == null) {
			return valorPorDefecto;
		}
		final Object val = json.get(clave);
		return (val != null) ? val.toString() : valorPorDefecto;
	}

	public static <E extends Enum<E>> E getEnum(final JSONObject json, final String clave, final E valorPorDefecto,
			final Class<E> enumClass) {
		if (json == null || clave == null || enumClass == null) {
			return valorPorDefecto;
		}
		final Object val = json.get(clave);
		if (val != null) {
			try {
				return Enum.valueOf(enumClass, val.toString().trim());
			} catch (final IllegalArgumentException ignored) {
			}
		}
		return valorPorDefecto;
	}

	public static JSONObject getObjeto(final JSONObject json, final String clave) {
		if (json == null || clave == null) {
			return null;
		}
		final Object val = json.get(clave);
		return (val instanceof JSONObject) ? (JSONObject) val : null;
	}

	public static JSONArray getArray(final JSONObject json, final String clave) {
		if (json == null || clave == null) {
			return null;
		}
		final Object val = json.get(clave);
		return (val instanceof JSONArray) ? (JSONArray) val : null;
	}
}