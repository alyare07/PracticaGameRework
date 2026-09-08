package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import principal.clima.TipoClima;
import principal.controles.Raton;
import principal.entes.AsistenteCamara;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.graficos.SuperficieDibujo;
import principal.iluminacion.CicloDiaNoche.FaseDia;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.IntensidadNiebla;
import principal.iluminacion.TipoLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.inventario.Contenedor;
import principal.inventario.slot.Slot;
import principal.inventario.vault.InventarioVault;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.Spawn;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.editor.herramientas.FloodFillTerreno;
import principal.maquinaestado.estados.editor.herramientas.TipoHerramientaDibujo;
import principal.maquinaestado.estados.editor.historial.AccionHistorialEntidad;
import principal.maquinaestado.estados.editor.historial.AccionHistorialMover;
import principal.maquinaestado.estados.editor.historial.AccionHistorialSpawn;
import principal.maquinaestado.estados.editor.historial.AccionHistorialTerreno;
import principal.maquinaestado.estados.editor.historial.AccionHistorialTrigger;
import principal.maquinaestado.estados.editor.historial.HistorialEditor;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.editor.modal.VentanaModalAmbiente;
import principal.maquinaestado.estados.editor.modal.VentanaModalConfirmarSalir;
import principal.maquinaestado.estados.editor.modal.VentanaModalLuz;
import principal.maquinaestado.estados.editor.modal.VentanaModalMundo;
import principal.maquinaestado.estados.editor.modal.VentanaModalSpawn;
import principal.maquinaestado.estados.editor.modal.VentanaModalTrigger;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.BotonTogglePixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.recursos.TipoTerreno;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Editor maestro de mapas con Studio Layout Pro ($640 \times 360$). Incluye
 * barra de herramientas extensible, barra de estado inferior, inspectores
 * modales, pipeta, snap semántico y heatmap de navegación IA.
 * 
 * @version 11.0 (Vanilla Java 8 - Studio Layout Architecture)
 */
public class EditorMapa implements EstadoJuego {

	private final GestorEstados GE;
	private final int LADO_TILE;
	private final int ANCHO;
	private final int ALTO;
	private final Terreno TERRENO;

	private int x;
	private int y;
	private final AsistenteCamara asistenteCamara;

	private final Raton RATON = SuperficieDibujo.obtenerSuperficieDibujo().RATON;
	private final Rectangle areaTileSelected = new Rectangle();
	private boolean tileApuntadoValido = false;

	// Geometría del Studio Layout
	private static final int ALTO_BARRA_SUP = 18;
	private static final int ALTO_BARRA_INF = 14;
	private static final int ANCHO_PALETA_LATERAL = 160;

	private final Rectangle AREA_BARRA_SUPERIOR;
	private final Rectangle AREA_BARRA_INFERIOR;
	private final Rectangle PALETA_MAPA;
	private final GrupoPaleta PALETAS;

	private final HistorialEditor HISTORIAL = new HistorialEditor();
	private final FloodFillTerreno FLOOD_FILL = new FloodFillTerreno();

	// Modales interactivos
	private final VentanaModalMundo modalMundo = new VentanaModalMundo();
	private final VentanaModalTrigger modalTrigger = new VentanaModalTrigger();
	private final VentanaModalAmbiente modalAmbiente = new VentanaModalAmbiente();
	private final VentanaModalLuz modalLuz = new VentanaModalLuz();
	private final VentanaModalSpawn modalSpawn;
	private final VentanaModalConfirmarSalir modalConfirmarSalir;

	// Botonera de la Barra Superior Extensible
	private final ArrayList<ComponenteMenu> barraSuperior = new ArrayList<ComponenteMenu>();

	// Captura de trazo para Undo/Redo
	private final Map<Integer, TipoTerreno> trazoTilesPrevios = new HashMap<Integer, TipoTerreno>();
	private boolean grabandoTrazo = false;

	private int tamanoPincel = 1;
	private boolean pincelCircular = false;
	private boolean mostrarGrid = false;
	private boolean modoPreviewLuz = false;

	// Snap Semántico a Grilla vs Modo Libre
	private boolean modoSnapGrilla = true;

	// Capas de renderizado y Overlays
	private boolean verCapaTerreno = true;
	private boolean verCapaEntidades = true;
	private boolean verCapaTriggers = true;
	private boolean mostrarOverlayIA = false;

	// Herramienta de Traslado (Shift + Arrastre)
	private boolean arrastrandoElemento = false;
	private Object elementoArrastrado = null;
	private int dragInicioX = 0;
	private int dragInicioY = 0;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;

	// Herramienta Regla de Medición (R)
	private boolean modoRegla = false;
	private int startReglaX = 0;
	private int startReglaY = 0;

	// Trazado de rectángulos
	private boolean arrastrandoRectangulo = false;
	private int startRectTileX = 0;
	private int startRectTileY = 0;

	// Clima y Hora
	private int idxClimaTest = 0;
	private int idxHoraTest = 0;

	private final GestorTiempo GT_COLOCACION = new GestorTiempo();
	private static final int TIEMPO_ESPERA_MS_COLOCACION = 180;

	private final MundoEditor MUNDO_EDITOR;
	private MetadatosEscenario metadatos = new MetadatosEscenario();

	// Estructuras Zero-GC preasignadas
	private final Rectangle AREA_MOUSE_APUNTADO = new Rectangle(-1, -1, 1, 1);
	private final Rectangle AREA_BORRADO_AUX = new Rectangle();
	private final Rectangle AREA_CURSOR_INSPECCION = new Rectangle();
	private final Rectangle AREA_CUENTAGOTAS = new Rectangle();
	private final ArrayList<Ente> listaEntesABorrar = new ArrayList<Ente>(8);
	private Ente enteMuestreado = null;

	// Gestión de Contenedores
	private InventarioVault cofreAbierto = null;
	private final ItemPuntero itemPuntero = new ItemPuntero();
	private Ente contenedorEncontrado = null;

	// Fuentes y Colores Constantes
	private static final Font FUENTE_INFO = new Font(Font.SANS_SERIF, Font.PLAIN, 6);
	private static final Font FUENTE_SPAWN = new Font(Font.SANS_SERIF, Font.BOLD, 7);
	private static final Font FUENTE_RULER = new Font(Font.SANS_SERIF, Font.BOLD, 8);

	private static final Color COLOR_PREVIEW_SPAWN = new Color(255, 215, 0, 75);
	private static final Color COLOR_PREVIEW_TP = new Color(255, 60, 60, 75);
	private static final Color COLOR_IA_SOLIDO = new Color(255, 40, 40, 85);
	private static final Color COLOR_IA_ESTRECHO = new Color(255, 220, 0, 65);
	private static final Color COLOR_IA_LIBRE = new Color(40, 240, 80, 40);
	private static final Color COLOR_RULER_LINE = new Color(0, 255, 255, 220);

	private static final Color COLOR_BARRA_BG = new Color(20, 24, 32);
	private static final Color COLOR_BARRA_BORDE = new Color(45, 50, 65);

	private VolatileImage bufferEditor;

	public EditorMapa(final int ladoTile, final int anchoTiles, final int altoTiles, final TipoTerreno tipoInicial,
			final GestorEstados ge) {
		this.GE = ge;
		this.LADO_TILE = ladoTile;
		this.ANCHO = anchoTiles * ladoTile;
		this.ALTO = altoTiles * ladoTile;
		this.TERRENO = new Terreno(anchoTiles, altoTiles, this.LADO_TILE, tipoInicial);

		this.AREA_BARRA_SUPERIOR = new Rectangle(0, 0, Constantes.ANCHO_JUEGO, ALTO_BARRA_SUP);
		this.AREA_BARRA_INFERIOR = new Rectangle(0, Constantes.ALTO_JUEGO - ALTO_BARRA_INF,
				Constantes.ANCHO_JUEGO - ANCHO_PALETA_LATERAL, ALTO_BARRA_INF);

		final int viewportW = Constantes.ANCHO_JUEGO - ANCHO_PALETA_LATERAL;
		final int viewportH = Constantes.ALTO_JUEGO - ALTO_BARRA_SUP - ALTO_BARRA_INF;
		this.PALETA_MAPA = new Rectangle(0, ALTO_BARRA_SUP, viewportW, viewportH);

		this.PALETAS = new GrupoPaleta(viewportW, ALTO_BARRA_SUP, ANCHO_PALETA_LATERAL,
				Constantes.ALTO_JUEGO - ALTO_BARRA_SUP, this);
		this.MUNDO_EDITOR = new MundoEditor(this.TERRENO);
		this.modalSpawn = new VentanaModalSpawn(this.MUNDO_EDITOR);
		this.asistenteCamara = new AsistenteCamara(0, 0, 0, 0);

		this.modalConfirmarSalir = new VentanaModalConfirmarSalir(() -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			this.salirAlMenu();
		}, () -> this.salirAlMenu());

		this.inicializarBarraSuperior();
		this.inicializarCamara();
	}

	public EditorMapa(final Escenario esc, final GestorEstados ge) {
		this.GE = ge;
		this.TERRENO = (esc != null) ? esc.getTerreno() : new Terreno(50, 50, Constantes.LADO_TILE, TipoTerreno.TIERRA);
		this.ANCHO = this.TERRENO.getAncho();
		this.ALTO = this.TERRENO.getAlto();
		this.LADO_TILE = this.TERRENO.ladoTile();
		this.metadatos = (esc != null) ? esc.getMetadatos() : new MetadatosEscenario();

		this.AREA_BARRA_SUPERIOR = new Rectangle(0, 0, Constantes.ANCHO_JUEGO, ALTO_BARRA_SUP);
		this.AREA_BARRA_INFERIOR = new Rectangle(0, Constantes.ALTO_JUEGO - ALTO_BARRA_INF,
				Constantes.ANCHO_JUEGO - ANCHO_PALETA_LATERAL, ALTO_BARRA_INF);

		final int viewportW = Constantes.ANCHO_JUEGO - ANCHO_PALETA_LATERAL;
		final int viewportH = Constantes.ALTO_JUEGO - ALTO_BARRA_SUP - ALTO_BARRA_INF;
		this.PALETA_MAPA = new Rectangle(0, ALTO_BARRA_SUP, viewportW, viewportH);

		this.PALETAS = new GrupoPaleta(viewportW, ALTO_BARRA_SUP, ANCHO_PALETA_LATERAL,
				Constantes.ALTO_JUEGO - ALTO_BARRA_SUP, this);
		this.MUNDO_EDITOR = (esc != null) ? new MundoEditor(esc) : new MundoEditor(this.TERRENO);
		this.modalSpawn = new VentanaModalSpawn(this.MUNDO_EDITOR);
		this.asistenteCamara = new AsistenteCamara(0, 0, 0, 0);

		this.modalConfirmarSalir = new VentanaModalConfirmarSalir(() -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			this.salirAlMenu();
		}, () -> this.salirAlMenu());

		this.inicializarBarraSuperior();
		this.inicializarCamara();
	}

	public EditorMapa(final Terreno terreno, final GestorEstados ge) {
		this(new Escenario(terreno, "[]", "[]", "[]", "[]", "[]", "[]", "[]", "[]", new MetadatosEscenario()), ge);
	}

	public EditorMapa(final String rutaMapa, final GestorEstados ge) {
		this(EscenarioLoader.importarEscenario(new File(rutaMapa)), ge);
	}

	private void inicializarBarraSuperior() {
		this.barraSuperior.clear();

		// 1. GESTIÓN Y CONFIGURACIÓN
		this.barraSuperior
				.add(new BotonPixel("Mundo", new Rectangle(2, 2, 34, 14), () -> this.modalMundo.abrir(this.metadatos)));
		this.barraSuperior.add(new BotonPixel("Guardar", new Rectangle(38, 2, 42, 14), () -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}));
		this.barraSuperior
				.add(new BotonPixel("Salir", new Rectangle(82, 2, 28, 14), () -> this.modalConfirmarSalir.abrir()));

		// 2. HERRAMIENTAS Y MODOS
		this.barraSuperior.add(new BotonTogglePixel("GRD", new Rectangle(114, 2, 22, 14), () -> this.mostrarGrid,
				() -> this.mostrarGrid = !this.mostrarGrid));
		this.barraSuperior.add(new BotonTogglePixel("LUZ", new Rectangle(138, 2, 22, 14), () -> this.modoPreviewLuz,
				() -> this.modoPreviewLuz = !this.modoPreviewLuz));
		this.barraSuperior.add(new BotonTogglePixel("IA", new Rectangle(162, 2, 20, 14), () -> this.mostrarOverlayIA,
				() -> this.mostrarOverlayIA = !this.mostrarOverlayIA));
		this.barraSuperior.add(new BotonTogglePixel("SNP", new Rectangle(184, 2, 22, 14), () -> this.modoSnapGrilla,
				() -> this.modoSnapGrilla = !this.modoSnapGrilla));
		this.barraSuperior.add(new BotonTogglePixel("RGL", new Rectangle(208, 2, 22, 14), () -> this.modoRegla, () -> {
			this.modoRegla = !this.modoRegla;
			if (this.modoRegla && this.tileApuntadoValido) {
				this.startReglaX = this.AREA_MOUSE_APUNTADO.x;
				this.startReglaY = this.AREA_MOUSE_APUNTADO.y;
			}
		}));

		// 3. CAPAS DE RENDER
		this.barraSuperior.add(new BotonTogglePixel("TER", new Rectangle(234, 2, 22, 14), () -> this.verCapaTerreno,
				() -> this.verCapaTerreno = !this.verCapaTerreno));
		this.barraSuperior.add(new BotonTogglePixel("ENT", new Rectangle(258, 2, 22, 14), () -> this.verCapaEntidades,
				() -> this.verCapaEntidades = !this.verCapaEntidades));
		this.barraSuperior.add(new BotonTogglePixel("TRG", new Rectangle(282, 2, 22, 14), () -> this.verCapaTriggers,
				() -> this.verCapaTriggers = !this.verCapaTriggers));

		// 4. CLIMA Y HORA TEST
		this.barraSuperior.add(new BotonPixel("CLM", new Rectangle(308, 2, 22, 14), () -> {
			if (Globales.GESTOR_CLIMA != null) {
				final TipoClima[] climas = TipoClima.values();
				this.idxClimaTest = (this.idxClimaTest + 1) % climas.length;
				Globales.GESTOR_CLIMA.setClima(climas[this.idxClimaTest], 0.0);
			}
		}));
		this.barraSuperior.add(new BotonPixel("HOR", new Rectangle(332, 2, 22, 14), () -> {
			if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
				final FaseDia[] fases = FaseDia.values();
				this.idxHoraTest = (this.idxHoraTest + 1) % fases.length;
				Globales.GESTOR_LUZ.getCiclo().setHora(fases[this.idxHoraTest]);
			}
		}));

		// 5. PINCELES DIRECTOS
		this.barraSuperior.add(new BotonTogglePixel("1x", new Rectangle(358, 2, 18, 14), () -> this.tamanoPincel == 1,
				() -> this.tamanoPincel = 1));
		this.barraSuperior.add(new BotonTogglePixel("2x", new Rectangle(378, 2, 18, 14), () -> this.tamanoPincel == 2,
				() -> this.tamanoPincel = 2));
		this.barraSuperior.add(new BotonTogglePixel("3x", new Rectangle(398, 2, 18, 14), () -> this.tamanoPincel == 3,
				() -> this.tamanoPincel = 3));
		this.barraSuperior.add(new BotonTogglePixel("4x", new Rectangle(418, 2, 18, 14), () -> this.tamanoPincel == 4,
				() -> this.tamanoPincel = 4));
		this.barraSuperior.add(new BotonTogglePixel("O/[]", new Rectangle(438, 2, 22, 14), () -> this.pincelCircular,
				() -> this.pincelCircular = !this.pincelCircular));
	}

	private void inicializarCamara() {
		this.x = this.ANCHO / 2;
		this.y = this.ALTO / 2;
		this.asistenteCamara.setPosicion(this.x, this.y);
		Globales.CAMARA.setEntidadEnfocada(this.asistenteCamara);
		Globales.CAMARA.deshabilitarGestorLimite();
		Globales.CAMARA.reiniciarZoom();
	}

	private void salirAlMenu() {
		this.itemPuntero.limpiar();
		if (Globales.GESTOR_LUZ != null) {
			Globales.GESTOR_LUZ.restablecerModoExterior();
		}
		Globales.CAMARA.reiniciarZoom();
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		this.GE.disposeEditor();
	}

	@Override
	public void actualizar() {
		// 1. Modales (Bloqueo absoluto)
		if (this.modalConfirmarSalir.isAbierta()) {
			this.modalConfirmarSalir.actualizar(this.RATON);
			return;
		}
		if (this.modalMundo.isAbierta()) {
			this.modalMundo.actualizar(this.RATON);
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
				this.modalMundo.cerrar();
			}
			return;
		}
		if (this.modalTrigger.isAbierta()) {
			this.modalTrigger.actualizar(this.RATON);
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
				this.modalTrigger.cerrar();
			}
			return;
		}
		if (this.modalAmbiente.isAbierta()) {
			this.modalAmbiente.actualizar(this.RATON);
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
				this.modalAmbiente.cerrar();
			}
			return;
		}
		if (this.modalLuz.isAbierta()) {
			this.modalLuz.actualizar(this.RATON);
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
				this.modalLuz.cerrar();
			}
			return;
		}
		if (this.modalSpawn.isAbierta()) {
			this.modalSpawn.actualizar(this.RATON);
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
				this.modalSpawn.cerrar();
			}
			return;
		}

		// 2. Cofre Abierto
		if (this.cofreAbierto != null) {
			if (!this.PALETAS.isPaletaItemSelected()) {
				this.PALETAS.setPaletaItemSelected();
			}
			this.actualizarProyeccionRaton();
			this.PALETAS.actualizar(this.RATON);
			this.actualizarCofreAbierto();
			if (this.itemPuntero.contieneItem()) {
				if (this.RATON.presionadoClickDerUnicaAct()) {
					this.itemPuntero.limpiar();
					GestorSonido.reproducir(IDSonido.GOLPE_1);

					final Paleta p = this.PALETAS.getPaletaActual();
					if ((p != null) && p.haySeleccion()) {
						p.deseleccionar();
						GestorSonido.reproducir(IDSonido.GOLPE_1);
					}
				}
				return;
			}
			return;
		}

		// 3. Barra Superior Extensible
		for (int i = 0; i < this.barraSuperior.size(); i++) {
			this.barraSuperior.get(i).actualizar(this.RATON);
		}

		// 4. Clima y Luces
		if (Globales.GESTOR_CLIMA != null) {
			Globales.GESTOR_CLIMA.actualizar();
		}
		if (Globales.GESTOR_LUZ != null) {
			Globales.GESTOR_LUZ.actualizar();
		}

		// 5. Entorno, Navegación y Pintura
		this.actualizarZoom();
		this.mover();
		this.actualizarProyeccionRaton();
		this.actualizarAtajosTeclado();
		this.actualizarTileApuntado();
		this.PALETAS.actualizar(this.RATON);

		// 6. Traslado de Entidades (Shift + Drag) o Edición estándar
		if (this.actualizarTrasladoEntidades()) {
			this.MUNDO_EDITOR.actualizar();
			return;
		}

		this.actualizarInspeccionConTeclaE();
		this.alterarElementoSeleccionado();
		this.borrarElemento();

		this.MUNDO_EDITOR.actualizar();

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.modalConfirmarSalir.abrir();
		}
	}

	private void actualizarAtajosTeclado() {
		// Pinceles
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_1)) {
			this.tamanoPincel = 1;
		} else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_2)) {
			this.tamanoPincel = 2;
		} else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_3)) {
			this.tamanoPincel = 3;
		} else if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_4)) {
			this.tamanoPincel = 4;
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_C)) {
			this.pincelCircular = !this.pincelCircular;
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_G)) {
			this.mostrarGrid = !this.mostrarGrid;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_L)) {
			this.modoPreviewLuz = !this.modoPreviewLuz;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Conmutador Snap a Grilla (S)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
			this.modoSnapGrilla = !this.modoSnapGrilla;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Cuentagotas / Pipeta (Q)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_Q)) {
			this.ejecutarCuentagotas();
		}

		// Variación Manual de Tile (V)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_V) && this.tileApuntadoValido) {
			final Tile tile = this.TERRENO.getTileReferenciado(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y);
			if (tile != null) {
				final byte nuevaVar = (byte) ((tile.getVariacionPropia() + 1) % 4);
				tile.setVariacionPropia(nuevaVar);
				this.TERRENO.marcarChunkSucio(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y);
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		// Regla de Medición (R)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_R)) {
			this.modoRegla = !this.modoRegla;
			if (this.modoRegla && this.tileApuntadoValido) {
				this.startReglaX = this.AREA_MOUSE_APUNTADO.x;
				this.startReglaY = this.AREA_MOUSE_APUNTADO.y;
			}
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Heatmap de Navegabilidad IA (F7)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_F7)) {
			this.mostrarOverlayIA = !this.mostrarOverlayIA;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Conmutador de Capas (F8 Terreno, F9 Entidades, F10 Triggers/Luces)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_F8)) {
			this.verCapaTerreno = !this.verCapaTerreno;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_F9)) {
			this.verCapaEntidades = !this.verCapaEntidades;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_F10)) {
			this.verCapaTriggers = !this.verCapaTriggers;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}
	}

	private boolean isSnapEfectivo() {
		final boolean alt = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_ALT);
		return this.modoSnapGrilla != alt;
	}

	private Point calcularPosicionAnclada(final Object elemento, final int mouseTileX, final int mouseTileY) {
		int targetX = mouseTileX * this.LADO_TILE;
		int targetY = mouseTileY * this.LADO_TILE;

		if (elemento instanceof Ente) {
			final Ente e = (Ente) elemento;
			final int w = e.getAncho();
			final int h = e.getAlto();

			if (e instanceof Criatura) {
				targetX = (mouseTileX * this.LADO_TILE) + ((this.LADO_TILE - w) / 2);
				targetY = (mouseTileY * this.LADO_TILE) + ((this.LADO_TILE - h) / 2);
			} else if ((w > this.LADO_TILE) || (h > this.LADO_TILE)) {
				targetX = (mouseTileX * this.LADO_TILE) - ((w - this.LADO_TILE) / 2);
				targetY = (mouseTileY * this.LADO_TILE) - (h - this.LADO_TILE);
			}
		} else if (elemento instanceof Spawn) {
			targetX = mouseTileX * this.LADO_TILE;
			targetY = mouseTileY * this.LADO_TILE;
		} else if (elemento instanceof FuenteLuz) {
			targetX = (mouseTileX * this.LADO_TILE) + 8;
			targetY = (mouseTileY * this.LADO_TILE) + 8;
		}

		return new Point(targetX, targetY);
	}

	private boolean actualizarTrasladoEntidades() {
		final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);

		if (shift && this.tileApuntadoValido && this.RATON.presionadoClickIzqUnicaAct() && !this.arrastrandoElemento) {
			this.AREA_CURSOR_INSPECCION.setBounds(this.AREA_MOUSE_APUNTADO.x - 4, this.AREA_MOUSE_APUNTADO.y - 4, 8, 8);
			this.elementoArrastrado = null;

			// 1. Spawns
			for (final Spawn s : this.MUNDO_EDITOR.getPuntosSpawn()) {
				if (s.getArea().intersects(this.AREA_CURSOR_INSPECCION)) {
					this.elementoArrastrado = s;
					this.dragInicioX = s.getX();
					this.dragInicioY = s.getY();
					break;
				}
			}

			// 2. Triggers
			if (this.elementoArrastrado == null) {
				for (final ZonaTP tp : this.MUNDO_EDITOR.getTriggersEditor()) {
					if (tp.getArea().intersects(this.AREA_CURSOR_INSPECCION)) {
						this.elementoArrastrado = tp;
						this.dragInicioX = tp.getPosicionXInt();
						this.dragInicioY = tp.getPosicionYInt();
						break;
					}
				}
			}

			// 3. Luces
			if (this.elementoArrastrado == null) {
				final FuenteLuz luz = this.MUNDO_EDITOR.getLuzEn(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y,
						16);
				if (luz != null) {
					this.elementoArrastrado = luz;
					this.dragInicioX = (int) Math.round(luz.getPosX());
					this.dragInicioY = (int) Math.round(luz.getPosY());
				}
			}

			// 4. Entidades Físicas
			if (this.elementoArrastrado == null) {
				this.MUNDO_EDITOR.paraCadaEnteEn(this.AREA_CURSOR_INSPECCION, false, false, new AccionEntidad<Ente>() {
					@Override
					public void ejecutar(final Ente ente) {
						if ((EditorMapa.this.elementoArrastrado == null) && (ente != null) && !ente.estaEliminado()) {
							EditorMapa.this.elementoArrastrado = ente;
							EditorMapa.this.dragInicioX = ente.getPosicionXInt();
							EditorMapa.this.dragInicioY = ente.getPosicionYInt();
						}
					}
				});
			}

			if (this.elementoArrastrado != null) {
				this.arrastrandoElemento = true;
				this.dragOffsetX = this.AREA_MOUSE_APUNTADO.x - this.dragInicioX;
				this.dragOffsetY = this.AREA_MOUSE_APUNTADO.y - this.dragInicioY;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return true;
			}
		}

		if (this.arrastrandoElemento) {
			if (this.RATON.presionadoClickIzq()) {
				final boolean snap = this.isSnapEfectivo();
				int targetX, targetY;

				if (snap) {
					final int mouseTileX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
					final int mouseTileY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);
					final Point pAnclado = this.calcularPosicionAnclada(this.elementoArrastrado, mouseTileX,
							mouseTileY);
					targetX = pAnclado.x;
					targetY = pAnclado.y;
				} else {
					targetX = this.AREA_MOUSE_APUNTADO.x - this.dragOffsetX;
					targetY = this.AREA_MOUSE_APUNTADO.y - this.dragOffsetY;
				}

				if (this.elementoArrastrado instanceof Ente) {
					final Ente e = (Ente) this.elementoArrastrado;
					e.setPosicion(targetX, targetY);
				} else if (this.elementoArrastrado instanceof Spawn) {
					final Spawn s = (Spawn) this.elementoArrastrado;
					s.setPosicion(targetX, targetY);
				} else if (this.elementoArrastrado instanceof FuenteLuz) {
					final FuenteLuz l = (FuenteLuz) this.elementoArrastrado;
					l.setPosicion(targetX, targetY);
				}
				return true;

			}
			this.arrastrandoElemento = false;
			int finalX = this.dragInicioX;
			int finalY = this.dragInicioY;

			if (this.elementoArrastrado instanceof Ente) {
				final Ente e = (Ente) this.elementoArrastrado;
				finalX = e.getPosicionXInt();
				finalY = e.getPosicionYInt();
				e.verificarZoneBox();
			} else if (this.elementoArrastrado instanceof Spawn) {
				final Spawn s = (Spawn) this.elementoArrastrado;
				finalX = s.getX();
				finalY = s.getY();
			} else if (this.elementoArrastrado instanceof FuenteLuz) {
				final FuenteLuz l = (FuenteLuz) this.elementoArrastrado;
				finalX = (int) Math.round(l.getPosX());
				finalY = (int) Math.round(l.getPosY());
			}

			if ((finalX != this.dragInicioX) || (finalY != this.dragInicioY)) {
				this.HISTORIAL.registrarAccion(new AccionHistorialMover(this.elementoArrastrado, this.dragInicioX,
						this.dragInicioY, finalX, finalY));
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}

			this.elementoArrastrado = null;
			return true;
		}

		return false;
	}

	private void ejecutarCuentagotas() {
		if (!this.tileApuntadoValido) {
			return;
		}

		if (this.verCapaEntidades) {
			this.enteMuestreado = null;
			this.AREA_CUENTAGOTAS.setBounds(this.AREA_MOUSE_APUNTADO.x - 2, this.AREA_MOUSE_APUNTADO.y - 2, 4, 4);

			this.MUNDO_EDITOR.paraCadaEnteEn(this.AREA_CUENTAGOTAS, false, false, new AccionEntidad<Ente>() {
				@Override
				public void ejecutar(final Ente ente) {
					if ((EditorMapa.this.enteMuestreado == null) && (ente != null) && !ente.estaEliminado()) {
						EditorMapa.this.enteMuestreado = ente;
					}
				}
			});

			if (this.enteMuestreado != null) {
				boolean seleccionado = false;

				if (this.enteMuestreado instanceof Criatura) {
					this.PALETAS.seleccionarPestanaPorNombre("Criaturas");
					final PaletaCriaturas pc = (PaletaCriaturas) this.PALETAS.getPaletaActual();
					if (pc != null) {
						seleccionado = pc.seleccionarPorNombre(this.enteMuestreado.getClass().getSimpleName());
					}
				} else if (this.enteMuestreado instanceof Complemento) {
					final int cod = ((Complemento) this.enteMuestreado).getCodigoModelo();
					String nombreBusqueda = "Casa Grande";

					switch (cod) {
					case ListaModeloComplemento.COD_CASA_1:
						nombreBusqueda = "Casa Grande";
						break;
					case ListaModeloComplemento.COD_ARBOL_1:
						nombreBusqueda = "Árbol Decorativo 1";
						break;
					case ListaModeloComplemento.COD_ARBOL_2:
						nombreBusqueda = "Árbol Decorativo 2";
						break;
					case ListaModeloComplemento.COD_BARRERA_INVISIBLE:
						nombreBusqueda = "Barrera Invisible";
						break;
					default:
						nombreBusqueda = "Árbol Decorativo";
						break;
					}

					this.PALETAS.seleccionarPestanaPorNombre("Objetos");
					final PaletaComplento pObj = (PaletaComplento) this.PALETAS.getPaletaActual();
					if (pObj != null) {
						seleccionado = pObj.seleccionarPorNombre(nombreBusqueda);
					}
				} else if (this.enteMuestreado instanceof Objeto) {
					final String simpleName = this.enteMuestreado.getClass().getSimpleName();
					String pestana = "Objetos";
					String termino = simpleName;

					if (simpleName.contains("ArbolCosechable")) {
						pestana = "Recursos";
						termino = "Árbol Talable";
					} else if (simpleName.contains("RocaCosechable")) {
						pestana = "Recursos";
						termino = "Roca Minable";
					} else if (simpleName.contains("ArbolCofre")) {
						pestana = "Objetos";
						termino = "Árbol Cofre Secreto";
					} else if (simpleName.contains("CofreMediano")) {
						pestana = "Objetos";
						termino = "Cofre Mediano";
					} else if (simpleName.contains("CofrePequeño") || simpleName.contains("CofrePequeno")) {
						pestana = "Objetos";
						termino = "Cofre Pequeño";
					} else if (simpleName.contains("CuadradoInvisible")) {
						pestana = "Objetos";
						termino = "Cuadrado Invisible";
					}

					this.PALETAS.seleccionarPestanaPorNombre(pestana);
					final PaletaComplento pComp = (PaletaComplento) this.PALETAS.getPaletaActual();
					if (pComp != null) {
						seleccionado = pComp.seleccionarPorNombre(termino);
					}
				}

				if (seleccionado) {
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
				return;
			}
		}

		if (this.verCapaTerreno) {
			final Tile tile = this.TERRENO.getTileReferenciado(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y);
			if (tile != null) {
				this.PALETAS.seleccionarPestanaPorNombre("Suelos");
				final PaletaTile pt = (PaletaTile) this.PALETAS.getPaletaActual();
				if ((pt != null) && pt.seleccionarTipo(tile.getTipoTerreno())) {
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
		}
	}

	private void actualizarInspeccionConTeclaE() {
		final boolean teclaE = Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E);
		if (!teclaE || !this.tileApuntadoValido) {
			return;
		}

		this.AREA_CURSOR_INSPECCION.setBounds(this.AREA_MOUSE_APUNTADO.x - 8, this.AREA_MOUSE_APUNTADO.y - 8, 16, 16);

		if (this.verCapaTriggers) {
			for (final Spawn s : this.MUNDO_EDITOR.getPuntosSpawn()) {
				if (s.getArea().intersects(this.AREA_CURSOR_INSPECCION)) {
					this.modalSpawn.abrir(s);
					return;
				}
			}

			for (final ZonaTP tp : this.MUNDO_EDITOR.getTriggersEditor()) {
				if (tp.getArea().intersects(this.AREA_CURSOR_INSPECCION)) {
					this.modalTrigger.abrir(tp);
					return;
				}
			}

			for (final ZonaAmbiente z : this.MUNDO_EDITOR.getZonasAmbienteEditor()) {
				if (z.getLimites().intersects(this.AREA_CURSOR_INSPECCION)) {
					this.modalAmbiente.abrir(z);
					return;
				}
			}

			final FuenteLuz luz = this.MUNDO_EDITOR.getLuzEn(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y,
					16);
			if (luz != null) {
				this.modalLuz.abrir(luz);
				return;
			}
		}

		if (this.verCapaEntidades) {
			this.contenedorEncontrado = null;
			this.MUNDO_EDITOR.paraCadaObjetoEn(this.AREA_CURSOR_INSPECCION, new AccionEntidad<Objeto>() {
				@Override
				public void ejecutar(final Objeto obj) {
					if ((EditorMapa.this.contenedorEncontrado == null) && (obj instanceof Contenedor)) {
						EditorMapa.this.contenedorEncontrado = obj;
					}
				}
			});

			if (this.contenedorEncontrado != null) {
				this.cofreAbierto = ((Contenedor) this.contenedorEncontrado).getInventario();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}
	}

	private void actualizarCofreAbierto() {
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.cerrarCofre();
			GestorSonido.reproducir(IDSonido.GOLPE_1);
			return;
		}

		final Point pMouse = this.RATON.getPuntoPosicionEscalado();
		this.cofreAbierto.actualizar(this.RATON, this.itemPuntero, null);

		if (this.RATON.presionadoClickDerUnicaAct()) {
			final Slot slotApuntado = this.cofreAbierto.getSlot(pMouse);
			if ((slotApuntado != null) && slotApuntado.contieneItem()) {
				slotApuntado.eliminarObjeto();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		if (this.RATON.presionadoClickIzqUnicaAct()) {
			final boolean dentroCofre = this.cofreAbierto.getArea().contains(pMouse);
			final boolean dentroPaleta = this.PALETAS.AREA.contains(pMouse)
					|| this.PALETAS.AREA_CABECERA.contains(pMouse);

			if (!dentroCofre && !dentroPaleta && !this.itemPuntero.contieneItem()) {
				this.cerrarCofre();
			}
		}
	}

	public void cerrarCofre() {
		this.cofreAbierto = null;
		this.contenedorEncontrado = null;
	}

	private void actualizarZoom() {
		final int rueda = this.RATON.getRotacionRueda();
		if (rueda < 0) {
			Globales.CAMARA.aumentarZoom();
		} else if (rueda > 0) {
			Globales.CAMARA.reducirZoom();
		}

		if (Globales.TECLADO.TECLA_ZOOM_IN.presionadoUnicaActualizacion()) {
			Globales.CAMARA.aumentarZoom();
		} else if (Globales.TECLADO.TECLA_ZOOM_OUT.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reducirZoom();
		}
		if (Globales.TECLADO.TECLA_ZOOM_REINICIAR.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reiniciarZoom();
		}
	}

	private void actualizarProyeccionRaton() {
		final int viewW = this.PALETA_MAPA.width;
		final int viewH = this.PALETA_MAPA.height;
		final int viewX = this.PALETA_MAPA.x;
		final int viewY = this.PALETA_MAPA.y;

		final double centroVX = viewX + (viewW / 2.0);
		final double centroVY = viewY + (viewH / 2.0);

		final double z = Math.max(0.2, Globales.CAMARA.getZoom());
		final int mouseX = this.RATON.getPosicionXEscalada();
		final int mouseY = this.RATON.getPosicionYEscalada();

		if ((mouseX >= viewX) && (mouseX < (viewX + viewW)) && (mouseY >= viewY) && (mouseY < (viewY + viewH))) {
			final double dx = (mouseX - centroVX) / z;
			final double dy = (mouseY - centroVY) / z;

			this.AREA_MOUSE_APUNTADO.x = (int) Math.floor(this.x + dx);
			this.AREA_MOUSE_APUNTADO.y = (int) Math.floor(this.y + dy);
			this.tileApuntadoValido = true;
		} else {
			this.tileApuntadoValido = false;
		}
	}

	private void actualizarTileApuntado() {
		if (!this.tileApuntadoValido) {
			return;
		}

		final int baseTX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
		final int baseTY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);

		final int offset = (this.tamanoPincel - 1) / 2;
		final int startTX = baseTX - offset;
		final int startTY = baseTY - offset;
		final int anchoPx = this.tamanoPincel * this.LADO_TILE;
		final int altoPx = this.tamanoPincel * this.LADO_TILE;

		this.areaTileSelected.setBounds(startTX * this.LADO_TILE, startTY * this.LADO_TILE, anchoPx, altoPx);
	}

	private void alterarElementoSeleccionado() {
		if (this.itemPuntero.contieneItem() || this.arrastrandoElemento) {
			return;
		}

		final Paleta paleta = this.PALETAS.getPaletaActual();
		if ((paleta == null) || !this.tileApuntadoValido) {
			return;
		}

		final boolean snap = this.isSnapEfectivo();
		final int mouseTileX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
		final int mouseTileY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);
		final int cantTilesX = this.TERRENO.getAncho() / this.LADO_TILE;

		// 1. SUELOS
		if ((paleta instanceof PaletaTile) && this.verCapaTerreno) {
			final PaletaTile pTile = (PaletaTile) paleta;
			final Tile tilePaleta = pTile.getTileSeleccionado();
			if (tilePaleta == null) {
				return;
			}

			final TipoHerramientaDibujo tool = pTile.getHerramientaSeleccionada();

			// PINCEL
			if (tool == TipoHerramientaDibujo.PINCEL) {
				if (this.RATON.presionadoClickIzq()) {
					if (!this.grabandoTrazo) {
						this.grabandoTrazo = true;
						this.trazoTilesPrevios.clear();
					}

					final int startTX = this.areaTileSelected.x / this.LADO_TILE;
					final int startTY = this.areaTileSelected.y / this.LADO_TILE;

					for (int dy = 0; dy < this.tamanoPincel; dy++) {
						for (int dx = 0; dx < this.tamanoPincel; dx++) {
							final int curTX = startTX + dx;
							final int curTY = startTY + dy;
							final int idx = (curTY * cantTilesX) + curTX;

							final Tile tilePrevio = this.TERRENO.getTileGrid(curTX, curTY);
							if ((tilePrevio != null) && !this.trazoTilesPrevios.containsKey(idx)) {
								this.trazoTilesPrevios.put(idx, tilePrevio.getTipoTerreno());
							}

							this.TERRENO.establecerTileReferenciado(curTX * this.LADO_TILE, curTY * this.LADO_TILE,
									tilePaleta);
						}
					}
				} else if (this.grabandoTrazo && !this.RATON.presionadoClickIzq()) {
					this.grabandoTrazo = false;
					this.registrarAccionHistorialPincel(tilePaleta.getTipoTerreno());
				}
			}
			// FLOOD FILL
			else if (tool == TipoHerramientaDibujo.BOTE_RELLENO) {
				if (this.RATON.presionadoClickIzqUnicaAct()) {
					this.FLOOD_FILL.ejecutar(this.TERRENO, mouseTileX, mouseTileY, tilePaleta.getTipoTerreno());
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
			// RECTÁNGULOS
			else if ((tool == TipoHerramientaDibujo.RECTANGULO_HUECO)
					|| (tool == TipoHerramientaDibujo.RECTANGULO_RELLENO)) {
				if (this.RATON.presionadoClickIzqUnicaAct()) {
					this.arrastrandoRectangulo = true;
					this.startRectTileX = mouseTileX;
					this.startRectTileY = mouseTileY;
				} else if (this.arrastrandoRectangulo && !this.RATON.presionadoClickIzq()) {
					this.arrastrandoRectangulo = false;
					final boolean relleno = (tool == TipoHerramientaDibujo.RECTANGULO_RELLENO);
					this.TERRENO.pintarRectanguloTiles(this.startRectTileX, this.startRectTileY, mouseTileX, mouseTileY,
							tilePaleta.getTipoTerreno(), relleno);
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
			// REEMPLAZAR GLOBAL
			else if (tool == TipoHerramientaDibujo.REEMPLAZAR_GLOBAL) {
				if (this.RATON.presionadoClickIzqUnicaAct()) {
					final Tile tileBajoCursor = this.TERRENO.getTileGrid(mouseTileX, mouseTileY);
					if (tileBajoCursor != null) {
						this.TERRENO.reemplazarTipoTerreno(tileBajoCursor.getTipoTerreno(),
								tilePaleta.getTipoTerreno());
						GestorSonido.reproducir(IDSonido.GOLPE_1);
					}
				}
			}
		}
		// 2. RECURSOS Y COMPLEMENTOS
		else if ((paleta instanceof PaletaComplento) && this.verCapaEntidades) {
			final PaletaComplento paletaObj = (PaletaComplento) paleta;
			if (this.RATON.presionadoClickIzq()
					&& this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaComplento.EntradaPaleta entrada = paletaObj.getEntradaSeleccionada();
				if ((entrada != null) && (entrada.icono != null)) {
					final int imgW = entrada.icono.getWidth();
					final int imgH = entrada.icono.getHeight();
					int posX, posY;

					if (snap) {
						if ((imgW > this.LADO_TILE) || (imgH > this.LADO_TILE)) {
							posX = (mouseTileX * this.LADO_TILE) - ((imgW - this.LADO_TILE) / 2);
							posY = (mouseTileY * this.LADO_TILE) - (imgH - this.LADO_TILE);
						} else {
							posX = mouseTileX * this.LADO_TILE;
							posY = mouseTileY * this.LADO_TILE;
						}
					} else {
						posX = this.AREA_MOUSE_APUNTADO.x - (imgW / 2);
						posY = this.AREA_MOUSE_APUNTADO.y - (imgH / 2);
					}

					final Objeto nuevoObj = paletaObj.crearInstanciaSeleccionada(posX, posY);
					if (nuevoObj != null) {
						this.MUNDO_EDITOR.meterEntidad(nuevoObj);
						this.HISTORIAL.registrarAccion(new AccionHistorialEntidad(this.MUNDO_EDITOR, nuevoObj, true));
					}
				}
			}
		}
		// 3. CRIATURAS
		else if ((paleta instanceof PaletaCriaturas) && this.verCapaEntidades) {
			final PaletaCriaturas paletaCriat = (PaletaCriaturas) paleta;
			if (this.RATON.presionadoClickIzq()
					&& this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaCriaturas.EntradaCriatura entrada = paletaCriat.getEntradaSeleccionada();
				if ((entrada != null) && (entrada.icono != null)) {
					int hitboxX, hitboxY;

					if (snap) {
						hitboxX = (mouseTileX * this.LADO_TILE) + ((this.LADO_TILE - entrada.anchoHitbox) / 2);
						hitboxY = (mouseTileY * this.LADO_TILE) + ((this.LADO_TILE - entrada.altoHitbox) / 2);
					} else {
						final int spriteX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
						final int spriteY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);
						hitboxX = spriteX + entrada.margenX;
						hitboxY = spriteY + entrada.margenY;
					}

					final Criatura nuevaCriat = entrada.creador.crear(hitboxX, hitboxY);
					if (nuevaCriat != null) {
						this.MUNDO_EDITOR.meterEntidad(nuevaCriat);
						this.HISTORIAL.registrarAccion(new AccionHistorialEntidad(this.MUNDO_EDITOR, nuevaCriat, true));
					}
				}
			}
		}
		// 4. TRIGGERS / VOLÚMENES / LUCES / SPAWNS
		else if ((paleta instanceof PaletaTriggers) && this.verCapaTriggers) {
			final PaletaTriggers pTriggers = (PaletaTriggers) paleta;
			if (this.RATON.presionadoClickIzq()
					&& this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaTriggers.EntradaTrigger ent = pTriggers.getEntradaSeleccionada();
				if (ent != null) {
					final int snapTileX = mouseTileX * this.LADO_TILE;
					final int snapTileY = mouseTileY * this.LADO_TILE;

					switch (ent.categoria) {
					case PUNTO_SPAWN:
						final int spawnX = snap ? snapTileX : this.AREA_MOUSE_APUNTADO.x;
						final int spawnY = snap ? snapTileY : this.AREA_MOUSE_APUNTADO.y;

						String nombreSpawn = Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
						if (this.MUNDO_EDITOR.getSpawn(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO) != null) {
							nombreSpawn = "Spawn_" + (this.MUNDO_EDITOR.getPuntosSpawn().size() + 1);
						}
						final Spawn nuevoSpawn = new Spawn(spawnX, spawnY, nombreSpawn);
						this.MUNDO_EDITOR.agregarSpawn(nuevoSpawn);
						this.HISTORIAL.registrarAccion(new AccionHistorialSpawn(this.MUNDO_EDITOR, nuevoSpawn, true));
						break;
					case TELEPORT_PUERTA:
						final int tpX = snap ? snapTileX : (this.AREA_MOUSE_APUNTADO.x - 10);
						final int tpY = snap ? snapTileY : (this.AREA_MOUSE_APUNTADO.y - 10);
						final ZonaTP tp = new ZonaTP(new Rectangle(tpX, tpY, 20, 20),
								new PuertaMapa("Mapa1", "Exterior", "Comienzo", false, null));
						this.MUNDO_EDITOR.agregarTrigger(tp);
						this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, tp, true));
						break;
					case ZONA_AMBIENTE_BIOMA:
						final int zbX = snap ? snapTileX : (this.AREA_MOUSE_APUNTADO.x - 64);
						final int zbY = snap ? snapTileY : (this.AREA_MOUSE_APUNTADO.y - 64);
						final ZonaAmbiente zb = new ZonaAmbiente(zbX, zbY, 128, 128, new Color(60, 220, 120, 80),
								IntensidadNiebla.LEVE, "Bioma", false);
						this.MUNDO_EDITOR.agregarZonaAmbiente(zb);
						this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, zb, true));
						break;
					case ZONA_AMBIENTE_CUEVA:
						final int zcX = snap ? snapTileX : (this.AREA_MOUSE_APUNTADO.x - 64);
						final int zcY = snap ? snapTileY : (this.AREA_MOUSE_APUNTADO.y - 64);
						final ZonaAmbiente zc = new ZonaAmbiente(zcX, zcY, 128, 128, new Color(0, 0, 0, 255),
								IntensidadNiebla.DESACTIVADA, "Cueva", true);
						this.MUNDO_EDITOR.agregarZonaAmbiente(zc);
						this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, zc, true));
						break;
					case LUZ_ANTORCHA:
						if (Globales.GESTOR_LUZ != null) {
							final int luzX = snap ? (snapTileX + 8) : this.AREA_MOUSE_APUNTADO.x;
							final int luzY = snap ? (snapTileY + 8) : this.AREA_MOUSE_APUNTADO.y;
							final FuenteLuz luz = Globales.GESTOR_LUZ.agregarLuzEstatica(luzX, luzY, TipoLuz.ANTORCHA,
									80);
							this.MUNDO_EDITOR.agregarLuzEstatica(luz);
							this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, luz, true));
						}
						break;
					case LUZ_FOGATA:
						if (Globales.GESTOR_LUZ != null) {
							final int fogX = snap ? (snapTileX + 8) : this.AREA_MOUSE_APUNTADO.x;
							final int fogY = snap ? (snapTileY + 8) : this.AREA_MOUSE_APUNTADO.y;
							final FuenteLuz luz = Globales.GESTOR_LUZ.agregarLuzEstatica(fogX, fogY, TipoLuz.FOGATA,
									140);
							this.MUNDO_EDITOR.agregarLuzEstatica(luz);
							this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, luz, true));
						}
						break;
					default:
						break;
					}
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
		}
	}

	private void registrarAccionHistorialPincel(final TipoTerreno tipoNuevo) {
		final int total = this.trazoTilesPrevios.size();
		if (total == 0) {
			return;
		}

		final int[] indices = new int[total];
		final TipoTerreno[] previos = new TipoTerreno[total];
		final TipoTerreno[] nuevos = new TipoTerreno[total];

		int i = 0;
		for (final Map.Entry<Integer, TipoTerreno> entry : this.trazoTilesPrevios.entrySet()) {
			indices[i] = entry.getKey();
			previos[i] = entry.getValue();
			nuevos[i] = tipoNuevo;
			i++;
		}

		this.HISTORIAL.registrarAccion(new AccionHistorialTerreno(this.TERRENO, indices, previos, nuevos));
		this.trazoTilesPrevios.clear();
	}

	private void borrarElemento() {
		if (this.itemPuntero.contieneItem()) {
			if (this.RATON.presionadoClickDerUnicaAct()) {
				this.itemPuntero.limpiar();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
			return;
		}

		final boolean clickDer = this.RATON.presionadoClickDer() || this.RATON.presionadoClickDerUnicaAct();

		if (clickDer && this.tileApuntadoValido) {
			final int radioBorrado = Math.max(16, this.LADO_TILE);
			this.AREA_BORRADO_AUX.setBounds(this.AREA_MOUSE_APUNTADO.x - 1, this.AREA_MOUSE_APUNTADO.y - 1, 2, 2);

			boolean elementoBorrado = false;

			// A. Borrar Spawns
			if (this.verCapaTriggers) {
				final Spawn spawnBorrado = this.MUNDO_EDITOR.eliminarSpawnEn(this.AREA_MOUSE_APUNTADO.x,
						this.AREA_MOUSE_APUNTADO.y, radioBorrado);
				if (spawnBorrado != null) {
					this.HISTORIAL.registrarAccion(new AccionHistorialSpawn(this.MUNDO_EDITOR, spawnBorrado, false));
					GestorSonido.reproducir(IDSonido.GOLPE_1);
					return;
				}

				// B. Borrar Triggers / Zonas / Luces
				final Object triggerBorrado = this.MUNDO_EDITOR.eliminarTriggerOAmbienteEn(this.AREA_MOUSE_APUNTADO.x,
						this.AREA_MOUSE_APUNTADO.y, radioBorrado);
				if (triggerBorrado != null) {
					this.HISTORIAL
							.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, triggerBorrado, false));
					GestorSonido.reproducir(IDSonido.GOLPE_1);
					return;
				}
			}

			// C. Borrar Entidades y Objetos
			if (this.verCapaEntidades) {
				this.listaEntesABorrar.clear();
				this.MUNDO_EDITOR.paraCadaEnteEn(this.AREA_BORRADO_AUX, false, false, new AccionEntidad<Ente>() {
					@Override
					public void ejecutar(final Ente ente) {
						if ((ente != null) && !ente.estaEliminado()) {
							EditorMapa.this.listaEntesABorrar.add(ente);
						}
					}
				});

				for (int i = 0; i < this.listaEntesABorrar.size(); i++) {
					final Ente e = this.listaEntesABorrar.get(i);
					this.MUNDO_EDITOR.eliminarEntidad(e);
					this.HISTORIAL.registrarAccion(new AccionHistorialEntidad(this.MUNDO_EDITOR, e, false));
					elementoBorrado = true;
				}
				this.listaEntesABorrar.clear();
			}

			if (elementoBorrado) {
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.RATON.presionadoClickDerUnicaAct()) {
				final Paleta p = this.PALETAS.getPaletaActual();
				if ((p != null) && p.haySeleccion()) {
					p.deseleccionar();
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
		}
	}

	private void mover() {
		int velocidad = 4;
		if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
			velocidad = 14;
		}

		if (Globales.TECLADO.TECLA_ARRIBA.presionado()) {
			this.y -= velocidad;
		}
		if (Globales.TECLADO.TECLA_ABAJO.presionado()) {
			this.y += velocidad;
		}
		if (Globales.TECLADO.TECLA_IZQUIERDA.presionado()) {
			this.x -= velocidad;
		}
		if (Globales.TECLADO.TECLA_DERECHA.presionado()) {
			this.x += velocidad;
		}

		this.asistenteCamara.setPosicion(this.x, this.y);
	}

	private void verificarBuffer(final Graphics2D g) {
		final int w = Constantes.ANCHO_JUEGO;
		final int h = Constantes.ALTO_JUEGO;

		if ((this.bufferEditor == null) || (this.bufferEditor.getWidth() != w) || (this.bufferEditor.getHeight() != h)
				|| (this.bufferEditor.validate(g.getDeviceConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE)) {

			if (this.bufferEditor != null) {
				this.bufferEditor.flush();
			}
			this.bufferEditor = g.getDeviceConfiguration().createCompatibleVolatileImage(w, h, Transparency.OPAQUE);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.verificarBuffer(g);

		final int viewW = this.PALETA_MAPA.width;
		final int viewH = this.PALETA_MAPA.height;
		final int viewX = this.PALETA_MAPA.x;
		final int viewY = this.PALETA_MAPA.y;

		final double centroVX = viewX + (viewW / 2.0);
		final double centroVY = viewY + (viewH / 2.0);
		final double z = Math.max(0.2, Globales.CAMARA.getZoom());

		// 1. Renderizado a escala 1:1 en VolatileImage
		final Graphics2D gBuf = this.bufferEditor.createGraphics();
		try {
			gBuf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			gBuf.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			Render2D.dibujarRectanguloRelleno(gBuf, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.BLACK);

			if (this.verCapaTerreno) {
				this.TERRENO.pintar(gBuf);
			}
			if (this.verCapaEntidades) {
				this.MUNDO_EDITOR.pintar(gBuf);
			}
			if (this.verCapaTriggers) {
				this.MUNDO_EDITOR.pintarTriggersYAmbientes(gBuf);
				this.pintarSpawnsEnMundo(gBuf);
			}

			if (this.mostrarOverlayIA) {
				this.pintarHeatmapNavegacionIA(gBuf);
			}

			if (Globales.GESTOR_CLIMA != null) {
				Globales.GESTOR_CLIMA.pintar(gBuf);
			}

			if (this.mostrarGrid) {
				this.pintarGridOverlay(gBuf);
			}

			if (this.modoPreviewLuz && (Globales.GESTOR_LUZ != null)) {
				Globales.GESTOR_LUZ.pintar(gBuf);
			}

			this.pintarPreviewColocacion(gBuf);
			this.pintarReglaMedicion(gBuf);

		} finally {
			gBuf.dispose();
		}

		// 2. Proyección sobre el Viewport del editor (Recortado sin invadir barras)
		final Graphics2D gView = (Graphics2D) g.create();
		try {
			gView.setClip(this.PALETA_MAPA);
			gView.translate(centroVX, centroVY);
			gView.scale(z, z);
			gView.drawImage(this.bufferEditor, -Constantes.CENTROX, -Constantes.CENTROY, null);
		} finally {
			gView.dispose();
		}

		// 3. Cofre Abierto (HUD 1:1)
		if (this.cofreAbierto != null) {
			this.cofreAbierto.pintar(g);
			this.cofreAbierto.pintarTooltips(g);
		}

		// 4. Studio Layout: Barra Superior y Barra Inferior de Estado
		this.pintarBarraSuperior(g);
		this.pintarBarraInferiorEstado(g);

		// 5. Paleta lateral
		this.PALETAS.pintar(g);
		this.pintarTooltipPaleta(g);

		// 6. Modales (Capa superior absoluta)
		this.modalMundo.pintar(g);
		this.modalTrigger.pintar(g);
		this.modalAmbiente.pintar(g);
		this.modalLuz.pintar(g);
		this.modalSpawn.pintar(g);
		this.modalConfirmarSalir.pintar(g);

		// 7. Ítem flotante
		this.itemPuntero.pintar(g, this.RATON.getPuntoPosicionEscalado());
	}

	private void pintarBarraSuperior(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.AREA_BARRA_SUPERIOR, COLOR_BARRA_BG);
		Render2D.dibujarLinea(g, this.AREA_BARRA_SUPERIOR.x, this.AREA_BARRA_SUPERIOR.height - 1,
				this.AREA_BARRA_SUPERIOR.width, this.AREA_BARRA_SUPERIOR.height - 1, COLOR_BARRA_BORDE);

		for (int i = 0; i < this.barraSuperior.size(); i++) {
			this.barraSuperior.get(i).pintar(g);
		}
	}

	private void pintarBarraInferiorEstado(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.AREA_BARRA_INFERIOR, COLOR_BARRA_BG);
		Render2D.dibujarLinea(g, this.AREA_BARRA_INFERIOR.x, this.AREA_BARRA_INFERIOR.y, this.AREA_BARRA_INFERIOR.width,
				this.AREA_BARRA_INFERIOR.y, COLOR_BARRA_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_INFO);

		final String toolInfo = (this.PALETAS.getPaletaActual() instanceof PaletaTile)
				? ((PaletaTile) this.PALETAS.getPaletaActual()).getHerramientaSeleccionada().getNombreVisible()
				: "Colocación";

		final String telemetria = "Cursor: (" + this.AREA_MOUSE_APUNTADO.x + ", " + this.AREA_MOUSE_APUNTADO.y
				+ ") | Cam: (" + this.x + ", " + this.y + ") | Zoom: "
				+ String.format("%.2f", Globales.CAMARA.getZoom()) + "x | Tool: " + toolInfo + " (" + this.tamanoPincel
				+ "x" + this.tamanoPincel + (this.pincelCircular ? "O" : "[]") + ") | Undo/Redo: "
				+ this.HISTORIAL.getCantidadDeshacer() + "/" + this.HISTORIAL.getCantidadRehacer() + " | FPS: "
				+ Globales.fps;

		Render2D.dibujarStringConSombra(g, telemetria, 6, this.AREA_BARRA_INFERIOR.y + 10, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);
	}

	private void pintarHeatmapNavegacionIA(final Graphics2D g) {
		final int lado = this.LADO_TILE;
		final double zoomActivo = Math.max(0.2, Globales.CAMARA.getZoomFinal());
		final int camX = Globales.CAMARA.getPosicionXInt();
		final int camY = Globales.CAMARA.getPosicionYInt();

		final int radioX = (int) Math.ceil(Constantes.CENTROX / zoomActivo) + (lado * 2);
		final int radioY = (int) Math.ceil(Constantes.CENTROY / zoomActivo) + (lado * 2);

		final int minTX = Math.max(0, Math.floorDiv(camX - radioX, lado));
		final int maxTX = Math.min((this.ANCHO / lado) - 1, Math.floorDiv(camX + radioX, lado));
		final int minTY = Math.max(0, Math.floorDiv(camY - radioY, lado));
		final int maxTY = Math.min((this.ALTO / lado) - 1, Math.floorDiv(camY + radioY, lado));

		for (int ty = minTY; ty <= maxTY; ty++) {
			for (int tx = minTX; tx <= maxTX; tx++) {
				final int px = tx * lado;
				final int py = ty * lado;
				final Tile tile = this.TERRENO.getTileGrid(tx, ty);

				if (tile == null) {
					continue;
				}

				if (tile.esSolido() || this.MUNDO_EDITOR.colisionaConObjetoSolido(tile.getArea())) {
					Render2D.dibujarRectanguloRellenoRefCamara(g, px, py, lado, lado, COLOR_IA_SOLIDO);
				} else {
					int vecinosSolidos = 0;
					final Tile tN = this.TERRENO.getTileGrid(tx, ty - 1);
					final Tile tS = this.TERRENO.getTileGrid(tx, ty + 1);
					final Tile tE = this.TERRENO.getTileGrid(tx + 1, ty);
					final Tile tO = this.TERRENO.getTileGrid(tx - 1, ty);

					if ((tN != null) && tN.esSolido()) {
						vecinosSolidos++;
					}
					if ((tS != null) && tS.esSolido()) {
						vecinosSolidos++;
					}
					if ((tE != null) && tE.esSolido()) {
						vecinosSolidos++;
					}
					if ((tO != null) && tO.esSolido()) {
						vecinosSolidos++;
					}

					if (vecinosSolidos >= 2) {
						Render2D.dibujarRectanguloRellenoRefCamara(g, px, py, lado, lado, COLOR_IA_ESTRECHO);
					} else {
						Render2D.dibujarRectanguloRellenoRefCamara(g, px, py, lado, lado, COLOR_IA_LIBRE);
					}
				}
			}
		}
	}

	private void pintarReglaMedicion(final Graphics2D g) {
		if (!this.modoRegla || !this.tileApuntadoValido) {
			return;
		}

		final int curX = this.AREA_MOUSE_APUNTADO.x;
		final int curY = this.AREA_MOUSE_APUNTADO.y;

		Render2D.dibujarLineaRefCamara(g, this.startReglaX, this.startReglaY, curX, curY, COLOR_RULER_LINE);

		Render2D.dibujarFiguraEllipseRefCamara(g, this.startReglaX - 28, this.startReglaY - 28, 56, 56,
				new Color(255, 255, 0, 70));
		Render2D.dibujarFiguraEllipseRefCamara(g, this.startReglaX - 80, this.startReglaY - 80, 160, 160,
				new Color(255, 160, 40, 60));
		Render2D.dibujarFiguraEllipseRefCamara(g, this.startReglaX - 140, this.startReglaY - 140, 280, 280,
				new Color(255, 60, 60, 50));

		final double distPx = Math.hypot(curX - this.startReglaX, curY - this.startReglaY);
		final double distTiles = distPx / this.LADO_TILE;
		final String texto = String.format("%.1f px (%.1f tiles)", distPx, distTiles);

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_RULER);
		Render2D.dibujarStringConSombraRefCamara(g, texto, curX + 6, curY - 6, Color.CYAN, Color.BLACK);
		g.setFont(fontPrevia);
	}

	private void pintarGridOverlay(final Graphics2D g) {
		final int lado = this.LADO_TILE;
		final Color cGrid = new Color(255, 255, 255, 35);
		final Color cChunk = new Color(255, 215, 0, 60);

		for (int gx = 0; gx < this.ANCHO; gx += lado) {
			final boolean esChunk = ((gx % Terreno.LADO_CHUNK) == 0);
			Render2D.dibujarLineaRefCamara(g, gx, 0, gx, this.ALTO, esChunk ? cChunk : cGrid);
		}
		for (int gy = 0; gy < this.ALTO; gy += lado) {
			final boolean esChunk = ((gy % Terreno.LADO_CHUNK) == 0);
			Render2D.dibujarLineaRefCamara(g, 0, gy, this.ANCHO, gy, esChunk ? cChunk : cGrid);
		}
	}

	private void pintarSpawnsEnMundo(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_SPAWN);

		for (final Spawn s : this.MUNDO_EDITOR.getPuntosSpawn()) {
			final int sx = s.getX();
			final int sy = s.getY();
			final boolean esComienzo = s.getNombre().equalsIgnoreCase(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);

			final Color colorMarco = esComienzo ? new Color(255, 215, 0) : new Color(70, 180, 255);
			final Color colorFondo = esComienzo ? new Color(255, 215, 0, 90) : new Color(70, 180, 255, 75);

			Render2D.dibujarRectanguloRellenoRefCamara(g, sx, sy, 16, 16, colorFondo);
			Render2D.dibujarRectanguloContornoRefCamara(g, sx, sy, 16, 16, colorMarco);

			final String txt = s.getNombre();
			final int anchoTxt = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt);
			final int tx = (sx + 8) - (anchoTxt / 2);
			final int ty = sy - 3;

			Render2D.dibujarStringConSombraRefCamara(g, txt, tx, ty, colorMarco, Color.BLACK);
		}

		g.setFont(fontPrevia);
	}

	private void pintarPreviewColocacion(final Graphics2D g) {
		if (!this.tileApuntadoValido || this.itemPuntero.contieneItem()) {
			return;
		}

		final boolean snap = this.isSnapEfectivo();
		final int mouseTileX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
		final int mouseTileY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);

		if (this.arrastrandoElemento) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.areaTileSelected.x, this.areaTileSelected.y, 16, 16,
					Color.MAGENTA);
			return;
		}

		final Paleta paleta = this.PALETAS.getPaletaActual();

		if (paleta instanceof PaletaTile) {
			final PaletaTile pt = (PaletaTile) paleta;
			final TipoHerramientaDibujo tool = pt.getHerramientaSeleccionada();

			if (this.arrastrandoRectangulo) {
				final int curTX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
				final int curTY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);
				final int minX = Math.min(this.startRectTileX, curTX) * this.LADO_TILE;
				final int minY = Math.min(this.startRectTileY, curTY) * this.LADO_TILE;
				final int w = (Math.abs(curTX - this.startRectTileX) + 1) * this.LADO_TILE;
				final int h = (Math.abs(curTY - this.startRectTileY) + 1) * this.LADO_TILE;

				Render2D.dibujarRectanguloContornoRefCamara(g, minX, minY, w, h, Color.YELLOW);
			} else if (tool == TipoHerramientaDibujo.PINCEL) {
				if (this.pincelCircular && (this.tamanoPincel > 2)) {
					Render2D.dibujarFiguraEllipseRefCamara(g, this.areaTileSelected, Color.MAGENTA);
				} else {
					Render2D.dibujarRectanguloContornoRefCamara(g, this.areaTileSelected, Color.MAGENTA);
				}
			} else {
				Render2D.dibujarRectanguloContornoRefCamara(g, this.areaTileSelected, Color.CYAN);
			}

		} else if (paleta instanceof PaletaComplento) {
			final PaletaComplento p = (PaletaComplento) paleta;
			final PaletaComplento.EntradaPaleta entrada = p.getEntradaSeleccionada();

			if ((entrada != null) && (entrada.icono != null)) {
				final int imgW = entrada.icono.getWidth();
				final int imgH = entrada.icono.getHeight();
				int posX, posY;

				if (snap) {
					if ((imgW > this.LADO_TILE) || (imgH > this.LADO_TILE)) {
						posX = (mouseTileX * this.LADO_TILE) - ((imgW - this.LADO_TILE) / 2);
						posY = (mouseTileY * this.LADO_TILE) - (imgH - this.LADO_TILE);
					} else {
						posX = mouseTileX * this.LADO_TILE;
						posY = mouseTileY * this.LADO_TILE;
					}
				} else {
					posX = this.AREA_MOUSE_APUNTADO.x - (imgW / 2);
					posY = this.AREA_MOUSE_APUNTADO.y - (imgH / 2);
				}

				Render2D.dibujarImagenConTransparenciaRefCamara(g, entrada.icono, posX, posY, 0.65f);
				final Color color = entrada.esCosechable ? Color.GREEN : Color.CYAN;
				Render2D.dibujarRectanguloContornoRefCamara(g, posX, posY, imgW, imgH, color);
			}

		} else if (paleta instanceof PaletaCriaturas) {
			final PaletaCriaturas p = (PaletaCriaturas) paleta;
			final PaletaCriaturas.EntradaCriatura entrada = p.getEntradaSeleccionada();

			if ((entrada != null) && (entrada.icono != null)) {
				int spriteX, spriteY, hitboxX, hitboxY;

				if (snap) {
					hitboxX = (mouseTileX * this.LADO_TILE) + ((this.LADO_TILE - entrada.anchoHitbox) / 2);
					hitboxY = (mouseTileY * this.LADO_TILE) + ((this.LADO_TILE - entrada.altoHitbox) / 2);
					spriteX = hitboxX - entrada.margenX;
					spriteY = hitboxY - entrada.margenY;
				} else {
					spriteX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
					spriteY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);
					hitboxX = spriteX + entrada.margenX;
					hitboxY = spriteY + entrada.margenY;
				}

				Render2D.dibujarImagenConTransparenciaRefCamara(g, entrada.icono, spriteX, spriteY, 0.65f);
				Render2D.dibujarRectanguloContornoRefCamara(g, hitboxX, hitboxY, entrada.anchoHitbox,
						entrada.altoHitbox, Color.RED);
			}

		} else if (paleta instanceof PaletaTriggers) {
			final PaletaTriggers pTriggers = (PaletaTriggers) paleta;
			final PaletaTriggers.EntradaTrigger ent = pTriggers.getEntradaSeleccionada();

			if (ent != null) {
				final int snapX = mouseTileX * this.LADO_TILE;
				final int snapY = mouseTileY * this.LADO_TILE;

				if (ent.categoria == PaletaTriggers.CategoriaTrigger.PUNTO_SPAWN) {
					final int sx = snap ? snapX : this.AREA_MOUSE_APUNTADO.x;
					final int sy = snap ? snapY : this.AREA_MOUSE_APUNTADO.y;
					Render2D.dibujarRectanguloContornoRefCamara(g, sx, sy, 16, 16, Color.YELLOW);
					Render2D.dibujarRectanguloRellenoRefCamara(g, sx, sy, 16, 16, COLOR_PREVIEW_SPAWN);
					Render2D.dibujarStringConSombraRefCamara(g, "[SPAWN]", sx - 4, sy - 2, Color.YELLOW, Color.BLACK);
				} else if (ent.categoria == PaletaTriggers.CategoriaTrigger.TELEPORT_PUERTA) {
					final int px = snap ? snapX : (this.AREA_MOUSE_APUNTADO.x - 10);
					final int py = snap ? snapY : (this.AREA_MOUSE_APUNTADO.y - 10);
					Render2D.dibujarRectanguloContornoRefCamara(g, px, py, 20, 20, Color.RED);
					Render2D.dibujarRectanguloRellenoRefCamara(g, px, py, 20, 20, COLOR_PREVIEW_TP);
				} else if ((ent.categoria == PaletaTriggers.CategoriaTrigger.ZONA_AMBIENTE_BIOMA)
						|| (ent.categoria == PaletaTriggers.CategoriaTrigger.ZONA_AMBIENTE_CUEVA)) {
					Render2D.dibujarRectanguloContornoRefCamara(g, snapX, snapY, 128, 128, ent.colorDistintivo);
				} else if (ent.categoria == PaletaTriggers.CategoriaTrigger.LUZ_ANTORCHA) {
					final int lx = snap ? (snapX + 8) : this.AREA_MOUSE_APUNTADO.x;
					final int ly = snap ? (snapY + 8) : this.AREA_MOUSE_APUNTADO.y;
					Render2D.dibujarFiguraEllipseRefCamara(g, lx - 80, ly - 80, 160, 160, new Color(255, 160, 40, 60));
					Render2D.dibujarRectanguloRellenoRefCamara(g, lx - 2, ly - 2, 4, 4, Color.ORANGE);
				} else if (ent.categoria == PaletaTriggers.CategoriaTrigger.LUZ_FOGATA) {
					final int fx = snap ? (snapX + 8) : this.AREA_MOUSE_APUNTADO.x;
					final int fy = snap ? (snapY + 8) : this.AREA_MOUSE_APUNTADO.y;
					Render2D.dibujarFiguraEllipseRefCamara(g, fx - 140, fy - 140, 280, 280,
							new Color(255, 100, 20, 60));
					Render2D.dibujarRectanguloRellenoRefCamara(g, fx - 2, fy - 2, 4, 4, Color.RED);
				}
			}
		}
	}

	private void pintarTooltipPaleta(final Graphics2D g) {
		final Point pMouse = this.RATON.getPuntoPosicionEscalado();
		final Paleta paleta = this.PALETAS.getPaletaActual();

		if ((paleta != null) && paleta.AREA.contains(pMouse)) {
			final int relX = pMouse.x - (paleta.AREA.x + paleta.MARGEN);
			final int relY = pMouse.y - (paleta.AREA.y + paleta.MARGEN);
			final int paso = paleta.LADO_SLOT + paleta.MARGEN;
			final int col = relX / paso;
			final int fila = relY / paso;

			if ((col >= 0) && (col < paleta.COLUMNAS) && (fila >= 0) && (fila < paleta.FILAS)) {
				final int index = (paleta.paginaActual * paleta.ELEMENTOS_POR_PAGINA) + (fila * paleta.COLUMNAS) + col;
				if (index < paleta.getCantidadTotalElementos()) {
					final String nombre = paleta.getNombreElemento(index);
					Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltip(g, nombre, Color.WHITE,
							new Color(20, 20, 25, 230));
				}
			}
		}
	}

	private void validarIntegridadEscenario() {
		if (this.MUNDO_EDITOR.getSpawn(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO) == null) {
			final int centroX = (this.ANCHO / 2 / this.LADO_TILE) * this.LADO_TILE;
			final int centroY = (this.ALTO / 2 / this.LADO_TILE) * this.LADO_TILE;
			final Spawn spawnBase = new Spawn(centroX, centroY, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
			this.MUNDO_EDITOR.agregarSpawn(spawnBase);
			System.out.println("[SanityCheck] Auto-generado punto de spawn 'Comienzo' en centro de mapa.");
		}
	}

	public void guardarMapa(final String nombre) {
		this.validarIntegridadEscenario();

		final JSONObject jsonEntes = this.MUNDO_EDITOR.getEntesInJson();
		final String criaturas = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class);
		final String items = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class);
		final String complementos = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class);
		final String objetos = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class);
		final String spawns = Globales.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Spawn.class);

		final Escenario esc = new Escenario(this.TERRENO, jsonEntes.get(criaturas).toString(),
				jsonEntes.get(items).toString(), jsonEntes.get(complementos).toString(),
				jsonEntes.get(objetos).toString(), jsonEntes.get(spawns).toString(),
				this.MUNDO_EDITOR.getTriggersEnJson().toString(), this.MUNDO_EDITOR.getZonasAmbienteEnJson().toString(),
				this.MUNDO_EDITOR.getLucesEnJson().toString(), this.metadatos);

		final File carpetaDestino = new File("mundos" + File.separator + nombre);
		EscenarioLoader.exportarEscenario(esc, carpetaDestino);
		System.out.println("[EditorMapa] Mapa guardado exitosamente en: " + carpetaDestino.getAbsolutePath());
	}

	public ItemPuntero getItemPuntero() {
		return this.itemPuntero;
	}

	public MetadatosEscenario getMetadatos() {
		return this.metadatos;
	}
}