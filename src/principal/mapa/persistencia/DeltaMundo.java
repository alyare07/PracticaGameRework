package principal.mapa.persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DeltaMundo {

	private final String nombreMundo;
	private final HashSet<String> entidadesDestruidas = new HashSet<String>();
	private final ArrayList<JSONObject> estructurasConstruidas = new ArrayList<JSONObject>();
	private final HashMap<String, JSONArray> cofresModificados = new HashMap<String, JSONArray>();
	private final ArrayList<JSONObject> itemsEnSuelo = new ArrayList<JSONObject>();

	private int diaGuardado = 1;
	private int diasParaRegenerar = 0; // 0 = Nunca regenera (Permanente, ej: Exterior)

	public DeltaMundo(final String nombreMundo, final int diasParaRegenerar) {
		this.nombreMundo = nombreMundo;
		this.diasParaRegenerar = Math.max(0, diasParaRegenerar);
	}

	public void registrarDestruccion(final int x, final int y) {
		this.entidadesDestruidas.add(IdentificadorEspacial.generarClave(x, y));
	}

	public boolean isEntidadDestruida(final int x, final int y) {
		return this.entidadesDestruidas.contains(IdentificadorEspacial.generarClave(x, y));
	}

	public boolean haExpirado(final int diaActual) {
		if (this.diasParaRegenerar <= 0) {
			return false;
		}
		return (diaActual - this.diaGuardado) >= this.diasParaRegenerar;
	}

	public void limpiar() {
		this.entidadesDestruidas.clear();
		this.estructurasConstruidas.clear();
		this.cofresModificados.clear();
		this.itemsEnSuelo.clear();
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("nombreMundo", this.nombreMundo);
		json.put("diaGuardado", this.diaGuardado);
		json.put("diasParaRegenerar", this.diasParaRegenerar);

		final JSONArray listaDestruidas = new JSONArray();
		listaDestruidas.addAll(this.entidadesDestruidas);
		json.put("destruidas", listaDestruidas);

		final JSONArray listaEstructuras = new JSONArray();
		listaEstructuras.addAll(this.estructurasConstruidas);
		json.put("estructuras", listaEstructuras);

		final JSONObject jsonCofres = new JSONObject();
		for (final Map.Entry<String, JSONArray> entry : this.cofresModificados.entrySet()) {
			jsonCofres.put(entry.getKey(), entry.getValue());
		}
		json.put("cofres", jsonCofres);

		final JSONArray listaItems = new JSONArray();
		listaItems.addAll(this.itemsEnSuelo);
		json.put("items", listaItems);

		return json;
	}

	@SuppressWarnings("unchecked")
	public void importarJSON(final JSONObject json) {
		if (json == null) {
			return;
		}

		this.limpiar();

		if (json.get("diaGuardado") != null) {
			this.diaGuardado = ((Number) json.get("diaGuardado")).intValue();
		}
		if (json.get("diasParaRegenerar") != null) {
			this.diasParaRegenerar = ((Number) json.get("diasParaRegenerar")).intValue();
		}

		final JSONArray listaDestruidas = (JSONArray) json.get("destruidas");
		if (listaDestruidas != null) {
			for (final Object obj : listaDestruidas) {
				this.entidadesDestruidas.add(obj.toString());
			}
		}

		final JSONArray listaEstructuras = (JSONArray) json.get("estructuras");
		if (listaEstructuras != null) {
			for (final Object obj : listaEstructuras) {
				if (obj instanceof JSONObject) {
					this.estructurasConstruidas.add((JSONObject) obj);
				}
			}
		}

		final JSONObject jsonCofres = (JSONObject) json.get("cofres");
		if (jsonCofres != null) {
			for (final Object key : jsonCofres.keySet()) {
				final Object val = jsonCofres.get(key);
				if (val instanceof JSONArray) {
					this.cofresModificados.put(key.toString(), (JSONArray) val);
				}
			}
		}

		final JSONArray listaItems = (JSONArray) json.get("items");
		if (listaItems != null) {
			for (final Object obj : listaItems) {
				if (obj instanceof JSONObject) {
					this.itemsEnSuelo.add((JSONObject) obj);
				}
			}
		}
	}

	public String getNombreMundo() {
		return this.nombreMundo;
	}

	public HashSet<String> getEntidadesDestruidas() {
		return this.entidadesDestruidas;
	}

	public ArrayList<JSONObject> getEstructurasConstruidas() {
		return this.estructurasConstruidas;
	}

	public HashMap<String, JSONArray> getCofresModificados() {
		return this.cofresModificados;
	}

	public ArrayList<JSONObject> getItemsEnSuelo() {
		return this.itemsEnSuelo;
	}

	public int getDiaGuardado() {
		return this.diaGuardado;
	}

	public void setDiaGuardado(final int diaGuardado) {
		this.diaGuardado = diaGuardado;
	}

	public int getDiasParaRegenerar() {
		return this.diasParaRegenerar;
	}

	public void setDiasParaRegenerar(final int diasParaRegenerar) {
		this.diasParaRegenerar = diasParaRegenerar;
	}
}