package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import principal.controles.Raton;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.PuertaMundo;
import principal.mapa.escenario.tps.PuertaSalidaCueva;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.ManifiestoMapa;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.persistencia.json.LectorJSON;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Inspector modal interactivo para configurar Triggers (ZonaTP). Incorpora
 * Cascada Inteligente de 3 Niveles: [Mapa Destino] -> [Submundo Destino] ->
 * [Spawn Real Destino] (Cero Typos).
 */
public class VentanaModalTrigger extends ComponenteMenu {

	private static final int ANCHO_MODAL = 360;
	private static final int ALTO_MODAL = 205;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(255, 60, 60);

	private static final String[] TIPOS_PUERTA = { "A Otro Mapa (.mp)", "Entre Mundos (Mismo Mapa)",
			"Local (Coordenadas X, Y)", "Salida de Cueva (Dinamica)" };
	private int idxTipoPuerta = 0; // 0 = PuertaMapa, 1 = PuertaMundo, 2 = PuertaArea, 3 = SalidaCueva

	private final EditorMapa editor;
	private ZonaTP triggerSeleccionado;
	private boolean abierta = false;

	// Áreas interactivas de selectores
	private final Rectangle areaBtnTipo = new Rectangle();
	private final Rectangle areaBtnMapaDestino = new Rectangle();
	private final Rectangle areaBtnSubmundoDestino = new Rectangle();
	private final Rectangle areaBtnSpawnDestino = new Rectangle();

	// Campos numéricos para PuertaArea (Local X, Y)
	private CajaTextoPixel ctParametro2;
	private CajaTextoPixel ctParametro3;

	private BotonPixel btnAplicar;
	private BotonPixel btnCerrar;

	// Listas dinámicas sincronizadas en cascada
	private final List<String> mapasDisponibles = new ArrayList<String>();
	private int idxMapaSeleccionado = 0;

	private final List<String> submundosDisponibles = new ArrayList<String>();
	private int idxSubmundoSeleccionado = 0;

	private final List<String> spawnsDisponibles = new ArrayList<String>();
	private int idxSpawnSeleccionado = 0;

	public VentanaModalTrigger(final EditorMapa editor) {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.editor = editor;
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.areaBtnTipo.setBounds(x + 135, y + 36, 205, 18);
		this.areaBtnMapaDestino.setBounds(x + 135, y + 64, 205, 18);
		this.areaBtnSubmundoDestino.setBounds(x + 135, y + 92, 205, 18);
		this.areaBtnSpawnDestino.setBounds(x + 135, y + 120, 205, 18);

		this.ctParametro2 = new CajaTextoPixel(new Rectangle(x + 135, y + 92, 205, 16), "500", 6, true);
		this.ctParametro3 = new CajaTextoPixel(new Rectangle(x + 135, y + 120, 205, 16), "350", 6, true);

		this.btnAplicar = new BotonPixel("Guardar", new Rectangle(x + 45, (y + ALTO_MODAL) - 28, 110, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cerrar", new Rectangle(x + 205, (y + ALTO_MODAL) - 28, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final ZonaTP trigger) {
		if (trigger == null) {
			return;
		}
		this.triggerSeleccionado = trigger;
		this.abierta = true;
		this.visible = true;

		this.poblarMapasDisponibles();

		final Object puerta = trigger.getPuertaTP();

		if (puerta instanceof PuertaSalidaCueva) {
			this.idxTipoPuerta = 3;
			this.ocultarCamposTexto();
		} else if (puerta instanceof PuertaMundo) {
			this.idxTipoPuerta = 1;
			this.ocultarCamposTexto();
			final PuertaMundo pm = (PuertaMundo) puerta;
			this.sincronizarCascadaPuertaMundo(pm.getNombreMundoDestino(), pm.getNombreSpawnDestino());
		} else if (puerta instanceof PuertaArea) {
			this.idxTipoPuerta = 2;
			this.ctParametro2.setVisible(true);
			this.ctParametro3.setVisible(true);
			final PuertaArea pa = (PuertaArea) puerta;
			this.ctParametro2.setTexto(String.valueOf(pa.getXDestino()));
			this.ctParametro3.setTexto(String.valueOf(pa.getYDestino()));
		} else if (puerta instanceof PuertaMapa) {
			this.idxTipoPuerta = 0;
			this.ocultarCamposTexto();
			final PuertaMapa pmap = (PuertaMapa) puerta;
			this.sincronizarCascadaPuertaMapa(pmap.getRutaMapaDestino(), pmap.getNombreMundoDestino(),
					pmap.getNombreSpawnDelMundoDestino());
		} else {
			this.idxTipoPuerta = 1;
			this.sincronizarCascadaPuertaMundo("exterior", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
		}

		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	// =========================================================================
	// ESCANEO Y CASCADA DE PROYECTOS / SUBMUNDOS / SPAWNS
	// =========================================================================

	private void poblarMapasDisponibles() {
		this.mapasDisponibles.clear();
		File dirMapas = new File("mapas");
		if (!dirMapas.exists() || !dirMapas.isDirectory()) {
			dirMapas = new File("mundos");
		}

		if (dirMapas.exists() && dirMapas.isDirectory()) {
			final File[] carpetas = dirMapas.listFiles();
			if (carpetas != null) {
				for (final File f : carpetas) {
					if (f.isDirectory()) {
						final File man = new File(f, ManifiestoMapa.NOMBRE_MANIFIESTO);
						final File manJson = new File(f, ManifiestoMapa.NOMBRE_MANIFIESTO_FALLBACK);
						if (man.exists() || manJson.exists()) {
							this.mapasDisponibles.add(f.getName());
						}
					}
				}
			}
		}

		if (this.mapasDisponibles.isEmpty()) {
			this.mapasDisponibles.add("mapa1");
		}
	}

	private void sincronizarCascadaPuertaMapa(final String idMapa, final String idMundo, final String idSpawn) {
		this.idxMapaSeleccionado = 0;
		if (idMapa != null) {
			final String limpia = idMapa.replace('\\', '/').replaceAll(".*/", "").replace(".mp", "").replace(".json",
					"");
			for (int i = 0; i < this.mapasDisponibles.size(); i++) {
				if (this.mapasDisponibles.get(i).equalsIgnoreCase(limpia)) {
					this.idxMapaSeleccionado = i;
					break;
				}
			}
		}

		this.refrescarSubmundosDeMapaSeleccionado();

		this.idxSubmundoSeleccionado = 0;
		if (idMundo != null) {
			for (int i = 0; i < this.submundosDisponibles.size(); i++) {
				if (this.submundosDisponibles.get(i).equalsIgnoreCase(idMundo)) {
					this.idxSubmundoSeleccionado = i;
					break;
				}
			}
		}

		this.refrescarSpawnsDeSubmundoSeleccionado();

		this.idxSpawnSeleccionado = 0;
		if (idSpawn != null) {
			for (int i = 0; i < this.spawnsDisponibles.size(); i++) {
				if (this.spawnsDisponibles.get(i).equalsIgnoreCase(idSpawn)) {
					this.idxSpawnSeleccionado = i;
					break;
				}
			}
		}
	}

	private void sincronizarCascadaPuertaMundo(final String idMundo, final String idSpawn) {
		// En PuertaMundo, el mapa siempre es el proyecto actual del editor
		this.submundosDisponibles.clear();
		if ((this.editor != null) && (this.editor.getManifiesto() != null)) {
			this.submundosDisponibles.addAll(this.editor.getManifiesto().getListaIdsSubmundos());
		}
		if (this.submundosDisponibles.isEmpty()) {
			this.submundosDisponibles.add("exterior");
		}

		this.idxSubmundoSeleccionado = 0;
		if (idMundo != null) {
			for (int i = 0; i < this.submundosDisponibles.size(); i++) {
				if (this.submundosDisponibles.get(i).equalsIgnoreCase(idMundo)) {
					this.idxSubmundoSeleccionado = i;
					break;
				}
			}
		}

		this.refrescarSpawnsDeSubmundoSeleccionado();

		this.idxSpawnSeleccionado = 0;
		if (idSpawn != null) {
			for (int i = 0; i < this.spawnsDisponibles.size(); i++) {
				if (this.spawnsDisponibles.get(i).equalsIgnoreCase(idSpawn)) {
					this.idxSpawnSeleccionado = i;
					break;
				}
			}
		}
	}

	private File resolverDirectorioMapaSeleccionado() {
		if (this.idxTipoPuerta == 1) {
			// PuertaMundo: usa el proyecto activo del editor
			return this.editor != null ? this.editor.getDirectorioProyecto() : new File("mapas", "mapa1");
		}
		// PuertaMapa: usa la carpeta del mapa elegido en el carrusel
		final String idMapa = this.mapasDisponibles.get(this.idxMapaSeleccionado);
		File dir = new File("mapas", idMapa);
		if (!dir.exists()) {
			dir = new File("mundos", idMapa);
		}
		return dir;
	}

	private void refrescarSubmundosDeMapaSeleccionado() {
		this.submundosDisponibles.clear();
		final File dirMapa = this.resolverDirectorioMapaSeleccionado();
		final ManifiestoMapa man = ManifiestoMapa.cargarDesdeDirectorio(dirMapa);

		if (man != null) {
			this.submundosDisponibles.addAll(man.getListaIdsSubmundos());
		}
		if (this.submundosDisponibles.isEmpty()) {
			this.submundosDisponibles.add("exterior");
		}
	}

	private void refrescarSpawnsDeSubmundoSeleccionado() {
		this.spawnsDisponibles.clear();
		final File dirMapa = this.resolverDirectorioMapaSeleccionado();
		final ManifiestoMapa man = ManifiestoMapa.cargarDesdeDirectorio(dirMapa);

		if ((man != null) && !this.submundosDisponibles.isEmpty()) {
			final String idSub = this.submundosDisponibles.get(this.idxSubmundoSeleccionado);
			final File archivoSub = man.resolverArchivoSubmundo(dirMapa, idSub);

			if ((archivoSub != null) && archivoSub.exists()) {
				try {
					final StringBuilder sb = new StringBuilder();
					try (final BufferedReader reader = new BufferedReader(
							new InputStreamReader(new FileInputStream(archivoSub), StandardCharsets.UTF_8))) {
						String linea;
						while ((linea = reader.readLine()) != null) {
							sb.append(linea);
						}
					}

					String contenido = sb.toString().trim();
					if (!contenido.startsWith("{")) {
						contenido = Globales.FUNCIONES.ENCRIPTADOR_STRING.desencriptar(contenido);
					}

					final JSONObject json = (JSONObject) new JSONParser().parse(contenido);
					final JSONArray arrSpawns = LectorJSON.getArray(json, "spawns");
					if (arrSpawns != null) {
						for (final Object obj : arrSpawns) {
							if (obj instanceof JSONObject) {
								final String nom = LectorJSON.getString((JSONObject) obj, "nombre", "");
								if (!nom.isEmpty()) {
									this.spawnsDisponibles.add(nom);
								}
							}
						}
					}
				} catch (final Exception ignored) {
				}
			}
		}

		if (this.spawnsDisponibles.isEmpty()) {
			this.spawnsDisponibles.add(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
		}
	}

	private void ocultarCamposTexto() {
		this.ctParametro2.setVisible(false);
		this.ctParametro3.setVisible(false);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
		this.triggerSeleccionado = null;
	}

	private void guardarCambios() {
		if (this.triggerSeleccionado == null) {
			return;
		}

		switch (this.idxTipoPuerta) {
		case 0: // PuertaMapa (Cascada completa)
			final String mapDest = this.mapasDisponibles.get(this.idxMapaSeleccionado);
			final String subDestMapa = this.submundosDisponibles.get(this.idxSubmundoSeleccionado);
			final String spDestMapa = this.spawnsDisponibles.get(this.idxSpawnSeleccionado);
			this.triggerSeleccionado.setPuertaTP(new PuertaMapa(mapDest, subDestMapa, spDestMapa, false, null));
			break;
		case 1: // PuertaMundo (Intra-mapa asistido)
			final String mDest = this.submundosDisponibles.get(this.idxSubmundoSeleccionado);
			final String sDest = this.spawnsDisponibles.get(this.idxSpawnSeleccionado);
			this.triggerSeleccionado.setPuertaTP(new PuertaMundo(mDest, sDest));
			break;
		case 2: // PuertaArea (Local X, Y)
			final int dx = this.ctParametro2.getNumeroEntero(0);
			final int dy = this.ctParametro3.getNumeroEntero(0);
			this.triggerSeleccionado.setPuertaTP(new PuertaArea(new Rectangle(dx, dy, 16, 16)));
			break;
		case 3: // PuertaSalidaCueva
			this.triggerSeleccionado.setPuertaTP(new PuertaSalidaCueva());
			break;
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();

			// 1. Selector Tipo de Puerta
			if (this.areaBtnTipo.contains(p)) {
				this.idxTipoPuerta = (this.idxTipoPuerta + 1) % TIPOS_PUERTA.length;
				this.actualizarModoPuerta();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
			// 2. Cascada PuertaMapa (0)
			else if (this.idxTipoPuerta == 0) {
				if (this.areaBtnMapaDestino.contains(p) && !this.mapasDisponibles.isEmpty()) {
					this.idxMapaSeleccionado = (this.idxMapaSeleccionado + 1) % this.mapasDisponibles.size();
					this.refrescarSubmundosDeMapaSeleccionado();
					this.idxSubmundoSeleccionado = 0;
					this.refrescarSpawnsDeSubmundoSeleccionado();
					this.idxSpawnSeleccionado = 0;
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				} else if (this.areaBtnSubmundoDestino.contains(p) && !this.submundosDisponibles.isEmpty()) {
					this.idxSubmundoSeleccionado = (this.idxSubmundoSeleccionado + 1)
							% this.submundosDisponibles.size();
					this.refrescarSpawnsDeSubmundoSeleccionado();
					this.idxSpawnSeleccionado = 0;
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				} else if (this.areaBtnSpawnDestino.contains(p) && !this.spawnsDisponibles.isEmpty()) {
					this.idxSpawnSeleccionado = (this.idxSpawnSeleccionado + 1) % this.spawnsDisponibles.size();
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
			// 3. Cascada PuertaMundo (1)
			else if (this.idxTipoPuerta == 1) {
				if (this.areaBtnSubmundoDestino.contains(p) && !this.submundosDisponibles.isEmpty()) {
					this.idxSubmundoSeleccionado = (this.idxSubmundoSeleccionado + 1)
							% this.submundosDisponibles.size();
					this.refrescarSpawnsDeSubmundoSeleccionado();
					this.idxSpawnSeleccionado = 0;
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				} else if (this.areaBtnSpawnDestino.contains(p) && !this.spawnsDisponibles.isEmpty()) {
					this.idxSpawnSeleccionado = (this.idxSpawnSeleccionado + 1) % this.spawnsDisponibles.size();
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
		}

		if (this.idxTipoPuerta == 2) {
			this.ctParametro2.actualizar(raton);
			this.ctParametro3.actualizar(raton);
		}

		this.btnAplicar.actualizar(raton);
		this.btnCerrar.actualizar(raton);
	}

	private void actualizarModoPuerta() {
		switch (this.idxTipoPuerta) {
		case 0: // PuertaMapa
			this.ocultarCamposTexto();
			this.sincronizarCascadaPuertaMapa("mapa1", "exterior", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
			break;
		case 1: // PuertaMundo
			this.ocultarCamposTexto();
			this.sincronizarCascadaPuertaMundo("exterior", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
			break;
		case 2: // PuertaArea
			this.ctParametro2.setVisible(true);
			this.ctParametro3.setVisible(true);
			this.ctParametro2.setTexto("500");
			this.ctParametro3.setTexto("350");
			break;
		case 3: // PuertaSalidaCueva
			this.ocultarCamposTexto();
			break;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.abierta) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y;
		final int w = this.area.width;
		final int h = this.area.height;

		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO,
				new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "CONFIGURACION DE TRIGGER (TP)";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 22, new Color(255, 100, 100),
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
		Render2D.dibujarStringConSombra(g, "Tipo de Puerta:", x + 16, y + 50, Color.WHITE, Color.BLACK);

		// PUERTA MAPA (CASCADA COMPLETA 3 NIVELES)
		if (this.idxTipoPuerta == 0) {
			Render2D.dibujarStringConSombra(g, "Mapa Destino:", x + 16, y + 78, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Mundo Destino:", x + 16, y + 106, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Spawn Destino:", x + 16, y + 134, Color.WHITE, Color.BLACK);

			final String mapTxt = !this.mapasDisponibles.isEmpty() ? this.mapasDisponibles.get(this.idxMapaSeleccionado)
					: "none";
			final String subTxt = !this.submundosDisponibles.isEmpty()
					? this.submundosDisponibles.get(this.idxSubmundoSeleccionado)
					: "exterior";
			final String spTxt = !this.spawnsDisponibles.isEmpty()
					? this.spawnsDisponibles.get(this.idxSpawnSeleccionado)
					: "comienzo";

			this.pintarSelectorVisual(g, this.areaBtnMapaDestino, mapTxt, new Color(130, 220, 255));
			this.pintarSelectorVisual(g, this.areaBtnSubmundoDestino, subTxt, new Color(100, 240, 120));
			this.pintarSelectorVisual(g, this.areaBtnSpawnDestino, spTxt, new Color(255, 215, 0));
		}
		// PUERTA MUNDO (CASCADA 2 NIVELES)
		else if (this.idxTipoPuerta == 1) {
			Render2D.dibujarStringConSombra(g, "Mundo Destino:", x + 16, y + 106, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Spawn Destino:", x + 16, y + 134, Color.WHITE, Color.BLACK);

			final String subTxt = !this.submundosDisponibles.isEmpty()
					? this.submundosDisponibles.get(this.idxSubmundoSeleccionado)
					: "exterior";
			final String spTxt = !this.spawnsDisponibles.isEmpty()
					? this.spawnsDisponibles.get(this.idxSpawnSeleccionado)
					: "comienzo";

			this.pintarSelectorVisual(g, this.areaBtnSubmundoDestino, subTxt, new Color(100, 240, 120));
			this.pintarSelectorVisual(g, this.areaBtnSpawnDestino, spTxt, new Color(255, 215, 0));
		}
		// PUERTA ÁREA (COORDENADAS LOCALES)
		else if (this.idxTipoPuerta == 2) {
			Render2D.dibujarStringConSombra(g, "Destino X (Px):", x + 16, y + 106, Color.WHITE, Color.BLACK);
			Render2D.dibujarStringConSombra(g, "Destino Y (Px):", x + 16, y + 134, Color.WHITE, Color.BLACK);
			this.ctParametro2.pintar(g);
			this.ctParametro3.pintar(g);
		}
		// PUERTA SALIDA CUEVA
		else if (this.idxTipoPuerta == 3) {
			final String info = "Retorno automatico al mapa exterior y al\nspawn exacto desde donde entro el jugador.";
			Render2D.dibujarStringConSombra(g, info, x + 35, y + 100, new Color(130, 220, 255), Color.BLACK);
		}

		// Selector de Tipo
		this.pintarSelectorVisual(g, this.areaBtnTipo, TIPOS_PUERTA[this.idxTipoPuerta], new Color(255, 200, 60));

		this.btnAplicar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	private void pintarSelectorVisual(final Graphics2D g, final Rectangle r, final String texto, final Color cTexto) {
		Render2D.dibujarRectanguloRelleno(g, r, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, r, new Color(75, 80, 95));
		final String t = "< " + texto + " >";
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, t);
		Render2D.dibujarStringConSombra(g, t, r.x + ((r.width - ancho) / 2), r.y + 13, cTexto, Color.BLACK);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}