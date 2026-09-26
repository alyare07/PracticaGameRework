package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.recursos.SetTerreno;
import principal.recursos.TipoTerreno;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Asistente para la creación de nuevos proyectos de mapa. Genera la estructura
 * en disco: mapas/<idMapa>/mapa.mp y su primer exterior.wld.
 */
public class MenuEditorNuevo extends Menu {

	private static final int PANEL_ANCHO = 340;
	private static final int PANEL_ALTO = 195;

	public static final int MIN_TILES = 15;
	public static final int MAX_TILES = 2000;
	private static final long LIMITE_ALERTA_RAM = 1000000L;

	private CajaTextoPixel ctNombre;
	private CajaTextoPixel ctAncho;
	private CajaTextoPixel ctAlto;

	private int indiceTerreno = 0;
	private final Rectangle areaSelectorTerreno = new Rectangle();

	private BotonPixel botonCrear;
	private BotonPixel botonVolver;

	public MenuEditorNuevo(final GestorEstados ge) {
		super(ge, "CREAR NUEVO PROYECTO");
		this.subtituloMenu = "- ASISTENTE DE PROYECTO -";
		this.inicializarMenu();
	}

	@SuppressWarnings("unchecked")
	private void crearNuevoProyecto() {
		String idRaw = this.ctNombre.getTexto().trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
		if (idRaw.isEmpty()) {
			idRaw = "mapa_nuevo";
		}

		final int rawAncho = this.ctAncho.getNumeroEntero(50);
		final int rawAlto = this.ctAlto.getNumeroEntero(50);
		final int ancho = Math.max(MIN_TILES, Math.min(MAX_TILES, rawAncho));
		final int alto = Math.max(MIN_TILES, Math.min(MAX_TILES, rawAlto));
		final TipoTerreno tipoInicial = TipoTerreno.values()[this.indiceTerreno];

		final File dirProyecto = new File("mapas", idRaw);
		final File dirSubmundos = new File(dirProyecto, "submundos");

		if (dirProyecto.exists()) {
			JOptionPane.showMessageDialog(null, "Ya existe un proyecto con el identificador: " + idRaw,
					"Proyecto Existente", JOptionPane.WARNING_MESSAGE);
			return;
		}

		dirSubmundos.mkdirs();

		// 1. Generar mapa.mp
		final JSONObject jsonMp = new JSONObject();
		jsonMp.put("idMapa", idRaw);
		jsonMp.put("nombreVisible", this.ctNombre.getTexto().trim());
		jsonMp.put("mundoComienzo", "exterior");
		jsonMp.put("spawnComienzo", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);

		final JSONArray submundosArr = new JSONArray();
		final JSONObject subExterior = new JSONObject();
		subExterior.put("id", "exterior");
		subExterior.put("archivo", "submundos/exterior.wld");
		subExterior.put("tipoAmbiente", "EXTERIOR");
		subExterior.put("modoCarga", "PESADO");
		submundosArr.add(subExterior);

		jsonMp.put("submundos", submundosArr);

		try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(dirProyecto, "mapa.mp")), StandardCharsets.UTF_8))) {
			writer.write(jsonMp.toJSONString());
			writer.flush();
		} catch (final Exception e) {
			e.printStackTrace();
			return;
		}

		// 2. Generar primer exterior.wld con Spawn en el centro
		final Terreno terreno = new Terreno(ancho, alto, Constantes.LADO_TILE, tipoInicial);
		final JSONArray arrSpawns = new JSONArray();
		final int spawnX = (ancho / 2) * Constantes.LADO_TILE;
		final int spawnY = (alto / 2) * Constantes.LADO_TILE;
		arrSpawns.add(new Spawn(spawnX, spawnY, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO).exportarParaJSON());

		final Escenario escInicial = new Escenario(terreno, null, null, null, null, arrSpawns, null, null, null,
				new MetadatosEscenario());
		EscenarioLoader.exportarEscenario(escInicial, new File(dirSubmundos, "exterior.wld"));

		// 3. Abrir editor conectado al nuevo proyecto
		this.GE.editorMapa(dirProyecto, "exterior");
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		final int panelY = Constantes.CENTROY - (PANEL_ALTO / 2) - 10;

		// 1. Campos de entrada
		this.ctNombre = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 150, panelY + 20, 130, 16),
				"reino_nuevo", 16, false, true);
		this.ctNombre.setPermitirEspacios(false);
		this.ctAncho = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 45, 55, 16), "50", 4,
				true);
		this.ctAlto = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 70, 55, 16), "50", 4,
				true);

		this.areaSelectorTerreno.setBounds((panelX + PANEL_ANCHO) - 130, panelY + 95, 110, 18);

		this.componentes.add(this.ctNombre);
		this.componentes.add(this.ctAncho);
		this.componentes.add(this.ctAlto);

		// Foco inicial automático en el nombre y deseleccionar botones
		this.ctNombre.setActivo(true);
		this.limpiarFoco();

		// 2. Botones de acción
		final int yBotones = panelY + PANEL_ALTO + 12;
		this.botonCrear = new BotonPixel("Crear", new Rectangle(Constantes.CENTROX - 105, yBotones, 100, 18), () -> {
			this.crearNuevoProyecto();
		});

		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX + 5, yBotones, 100, 18), () -> {
			this.alPresionarEscape();
		});

		this.componentes.add(this.botonCrear);
		this.componentes.add(this.botonVolver);
		this.botones.add(this.botonCrear);
		this.botones.add(this.botonVolver);
	}

	@Override
	public void actualizar() {
		// super.actualizar() ya actualiza TODOS los componentes (ctNombre, ctAncho,
		// ctAlto, botones)
		super.actualizar();

		// Clic en el selector de tipo de terreno
		if (Globales.RATON.presionadoClickIzqUnicaAct()) {
			final Point p = Globales.RATON.getPuntoPosicionEscalado();
			if (this.areaSelectorTerreno.contains(p)) {
				this.indiceTerreno = (this.indiceTerreno + 1) % TipoTerreno.values().length;
				GestorSonido.reproducir(IDSonido.SELECT_MENU);
			}
		}
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.editorMapaSeleccion();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		final int panelY = Constantes.CENTROY - (PANEL_ALTO / 2) - 10;

		Render2D.dibujarRectanguloRelleno(g, panelX, panelY, PANEL_ANCHO, PANEL_ALTO, new Color(16, 20, 26, 235));
		Render2D.dibujarRectanguloContorno(g, panelX, panelY, PANEL_ANCHO, PANEL_ALTO, new Color(55, 60, 75));

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 15f));

		Render2D.dibujarStringConSombra(g, "Nombre del Proyecto:", panelX + 16, panelY + 32, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Ancho del Exterior (Tiles):", panelX + 16, panelY + 58, Color.WHITE,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Alto del Exterior (Tiles):", panelX + 16, panelY + 83, Color.WHITE,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Suelo Inicial:", panelX + 16, panelY + 108, Color.WHITE, Color.BLACK);

		g.setFont(fontPrevia);

		final TipoTerreno tipoActual = TipoTerreno.values()[this.indiceTerreno];
		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(tipoActual);

		Render2D.dibujarRectanguloRelleno(g, this.areaSelectorTerreno, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, this.areaSelectorTerreno, new Color(220, 180, 50));

		if ((set != null) && (set.getSpriteBase() != null)) {
			Render2D.dibujarImagen(g, set.getSpriteBase(), this.areaSelectorTerreno.x + 2,
					this.areaSelectorTerreno.y + 1);
		}

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
		Render2D.dibujarStringConSombra(g, tipoActual.getNombre(), this.areaSelectorTerreno.x + 22,
				this.areaSelectorTerreno.y + 13, Color.WHITE, Color.BLACK);

		// Telemetría de RAM
		final int curW = this.ctAncho.getNumeroEntero(50);
		final int curH = this.ctAlto.getNumeroEntero(50);
		final long totalTiles = (long) curW * (long) curH;

		String infoMemoria = "Total: " + String.format("%,d", totalTiles) + " celdas (Carga Normal)";
		Color colorMemoria = new Color(130, 220, 120);

		if (totalTiles > LIMITE_ALERTA_RAM) {
			infoMemoria = "[!] " + String.format("%,d", totalTiles) + " celdas (Requiere RAM)";
			colorMemoria = new Color(255, 180, 40);
		}

		Render2D.dibujarRectanguloRelleno(g, panelX + 16, panelY + 130, PANEL_ANCHO - 32, 16,
				new Color(10, 12, 16, 200));
		Render2D.dibujarRectanguloContorno(g, panelX + 16, panelY + 130, PANEL_ANCHO - 32, 16, new Color(40, 45, 55));
		Render2D.dibujarStringConSombra(g, infoMemoria, panelX + 22, panelY + 142, colorMemoria, Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 11f));
		Render2D.dibujarStringConSombra(g, "Rango seguro: " + MIN_TILES + " a " + MAX_TILES + " tiles por eje.",
				panelX + 16, panelY + 162, Color.GRAY, Color.BLACK);

		g.setFont(fontPrevia);

		this.ctNombre.pintar(g);
		this.ctAncho.pintar(g);
		this.ctAlto.pintar(g);
		this.botonCrear.pintar(g);
		this.botonVolver.pintar(g);
		this.pintarGuiaControles(g);
	}
}