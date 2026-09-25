package principal.mapa.escenario;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.iluminacion.IntensidadNiebla;
import principal.iluminacion.TipoLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.PuertaMundo;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.persistencia.json.LectorJSON;
import principal.persistencia.json.RegistroEntidades;
import principal.utilidades.Globales;

/**
 * Representa la definición serializada completa de un mapa del juego. Opera
 * mediante estructuras nativas JSONArray y delega la instanciación polimórfica
 * en RegistroEntidades (Zero-String-Reparse / Java 8).
 */
public class Escenario implements Serializable {

	private static final long serialVersionUID = 3351562178857131172L;

	protected final Terreno TERRENO;
	protected final JSONArray LISTA_CRIATURAS;
	protected final JSONArray LISTA_ITEMS;
	protected final JSONArray LISTA_COMPLEMENTOS;
	protected final JSONArray LISTA_OBJETOS;
	protected final JSONArray LISTA_SPAWNS;
	protected final JSONArray LISTA_TRIGGERS;
	protected final JSONArray LISTA_ZONAS_AMBIENTE;
	protected final JSONArray LISTA_LUCES;
	protected final MetadatosEscenario METADATOS;

	public Escenario(final Terreno terreno, final JSONArray criaturas, final JSONArray items,
			final JSONArray complementos, final JSONArray objetos, final JSONArray spawns, final JSONArray triggers,
			final JSONArray zonasAmbiente, final JSONArray luces, final MetadatosEscenario metadatos) {
		this.TERRENO = terreno;
		this.LISTA_CRIATURAS = (criaturas != null) ? criaturas : new JSONArray();
		this.LISTA_ITEMS = (items != null) ? items : new JSONArray();
		this.LISTA_COMPLEMENTOS = (complementos != null) ? complementos : new JSONArray();
		this.LISTA_OBJETOS = (objetos != null) ? objetos : new JSONArray();
		this.LISTA_SPAWNS = (spawns != null) ? spawns : new JSONArray();
		this.LISTA_TRIGGERS = (triggers != null) ? triggers : new JSONArray();
		this.LISTA_ZONAS_AMBIENTE = (zonasAmbiente != null) ? zonasAmbiente : new JSONArray();
		this.LISTA_LUCES = (luces != null) ? luces : new JSONArray();
		this.METADATOS = (metadatos != null) ? metadatos : new MetadatosEscenario();
	}

	public ArrayList<Spawn> generarSpawns() {
		final ArrayList<Spawn> spawns = new ArrayList<Spawn>();
		for (final Object obj : this.LISTA_SPAWNS) {
			if (obj instanceof JSONObject) {
				final Spawn s = Spawn.crearDesdeJson((JSONObject) obj);
				if (s != null) {
					spawns.add(s);
				}
			}
		}
		return spawns;
	}

	public ArrayList<Criatura> generarListaCriaturas(final Mundo mundo) {
		final ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		for (final Object obj : this.LISTA_CRIATURAS) {
			if (obj instanceof JSONObject) {
				final Ente e = RegistroEntidades.importar((JSONObject) obj, mundo);
				if (e instanceof Criatura) {
					criaturas.add((Criatura) e);
				}
			}
		}
		return criaturas;
	}

	public void generarListaComplementos(final Mundo mundo) {
		final ArrayList<Complemento> complementos = new ArrayList<Complemento>();
		for (final Object obj : this.LISTA_COMPLEMENTOS) {
			if (obj instanceof JSONObject) {
				final Ente e = RegistroEntidades.importar((JSONObject) obj, mundo);
				if (e instanceof Complemento) {
					complementos.add((Complemento) e);
				}
			}
		}

		complementos.sort((c1, c2) -> Integer.compare(c1.getPosicionYBase(), c2.getPosicionYBase()));
		for (int i = 0; i < complementos.size(); i++) {
			mundo.meterEntidad(complementos.get(i));
		}
	}

	public ArrayList<Item> generarItemsEnTerreno() {
		final ArrayList<Item> items = new ArrayList<Item>();
		for (final Object obj : this.LISTA_ITEMS) {
			if (obj instanceof JSONObject) {
				final Ente e = RegistroEntidades.importar((JSONObject) obj, null);
				if ((e instanceof Item) && this.TERRENO.areaDentroDelTerreno(e.getArea())) {
					items.add((Item) e);
				}
			}
		}
		return items;
	}

	public int generarObjetosEnTerreno(final Mundo mundo) {
		int cant = 0;
		for (final Object obj : this.LISTA_OBJETOS) {
			if (obj instanceof JSONObject) {
				final Ente e = RegistroEntidades.importar((JSONObject) obj, mundo);
				if (e instanceof Objeto) {
					if (mundo.meterEntidad(e)) {
						cant++;
					}
				}
			}
		}
		return cant;
	}

	public void generarTriggers(final Mundo mundo) {
		if (mundo == null) {
			return;
		}

		for (final Object obj : this.LISTA_TRIGGERS) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final int x = LectorJSON.getInt(json, "x", 0);
				final int y = LectorJSON.getInt(json, "y", 0);
				final int w = LectorJSON.getInt(json, "w", 16);
				final int h = LectorJSON.getInt(json, "h", 16);
				final String tipoPuerta = LectorJSON.getString(json, "tipo", "");
				final Rectangle areaTP = new Rectangle(x, y, w, h);
				ZonaTP zonaTP = null;

				if (tipoPuerta.equals("PuertaMapa")) {
					final String ruta = LectorJSON.getString(json, "mapa", "Mapa1");
					final String mDest = LectorJSON.getString(json, "mundo", "Exterior");
					final String sp = LectorJSON.getString(json, "spawn", "Comienzo");
					zonaTP = new ZonaTP(areaTP, new PuertaMapa(ruta, mDest, sp, false, null));
				} else if (tipoPuerta.equals("PuertaArea")) {
					final int dx = LectorJSON.getInt(json, "destX", 0);
					final int dy = LectorJSON.getInt(json, "destY", 0);
					zonaTP = new ZonaTP(areaTP, new PuertaArea(new Rectangle(dx, dy, 16, 16)));
				} else if (tipoPuerta.equals("PuertaMundo")) {
					final String mDest = LectorJSON.getString(json, "mundo", "Exterior");
					final String sp = LectorJSON.getString(json, "spawn", "Comienzo");
					zonaTP = new ZonaTP(areaTP, new PuertaMundo(mDest, sp));
				} else if (tipoPuerta.equals("PuertaSalidaCueva")) {
					zonaTP = new ZonaTP(areaTP, new principal.mapa.escenario.tps.PuertaSalidaCueva());
				}

				if (zonaTP != null) {
					mundo.meterEntidad(zonaTP);
				}
			}
		}
	}

	public void generarZonasAmbiente() {
		if (Globales.GESTOR_ZONAS_AMBIENTE == null) {
			return;
		}

		for (final Object obj : this.LISTA_ZONAS_AMBIENTE) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final int x = LectorJSON.getInt(json, "x", 0);
				final int y = LectorJSON.getInt(json, "y", 0);
				final int w = LectorJSON.getInt(json, "w", 64);
				final int h = LectorJSON.getInt(json, "h", 64);
				final String nombre = LectorJSON.getString(json, "nombre", "Zona");
				final boolean esInterior = LectorJSON.getBoolean(json, "interior", false);

				final int r = LectorJSON.getInt(json, "r", 255);
				final int g = LectorJSON.getInt(json, "g", 255);
				final int b = LectorJSON.getInt(json, "b", 255);
				final int a = LectorJSON.getInt(json, "a", 50);

				final IntensidadNiebla niebla = LectorJSON.getEnum(json, "niebla", IntensidadNiebla.DESACTIVADA,
						IntensidadNiebla.class);

				Globales.GESTOR_ZONAS_AMBIENTE
						.registrarZona(new ZonaAmbiente(x, y, w, h, new Color(r, g, b, a), niebla, nombre, esInterior));
			}
		}
	}

	public void generarLucesEstaticas() {
		if (Globales.GESTOR_LUZ == null) {
			return;
		}

		for (final Object obj : this.LISTA_LUCES) {
			if (obj instanceof JSONObject) {
				final JSONObject json = (JSONObject) obj;
				final double x = LectorJSON.getDouble(json, "x", 0.0);
				final double y = LectorJSON.getDouble(json, "y", 0.0);
				final double radio = LectorJSON.getDouble(json, "radio", 80.0);
				final TipoLuz tipo = LectorJSON.getEnum(json, "tipo", TipoLuz.ANTORCHA, TipoLuz.class);

				Globales.GESTOR_LUZ.agregarLuzEstatica(x, y, tipo, radio);
			}
		}
	}

	public Terreno getTerreno() {
		return this.TERRENO;
	}

	public MetadatosEscenario getMetadatos() {
		return this.METADATOS;
	}

	public JSONArray getListaCriaturas() {
		return this.LISTA_CRIATURAS;
	}

	public JSONArray getListaItems() {
		return this.LISTA_ITEMS;
	}

	public JSONArray getListaComplementos() {
		return this.LISTA_COMPLEMENTOS;
	}

	public JSONArray getListaObjetos() {
		return this.LISTA_OBJETOS;
	}

	public JSONArray getListaSpawns() {
		return this.LISTA_SPAWNS;
	}

	public JSONArray getListaTriggers() {
		return this.LISTA_TRIGGERS;
	}

	public JSONArray getListaZonasAmbiente() {
		return this.LISTA_ZONAS_AMBIENTE;
	}

	public JSONArray getListaLuces() {
		return this.LISTA_LUCES;
	}
}