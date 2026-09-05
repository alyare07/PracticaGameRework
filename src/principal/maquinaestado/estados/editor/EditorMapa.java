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
import principal.maquinaestado.estados.editor.historial.AccionHistorialTerreno;
import principal.maquinaestado.estados.editor.historial.AccionHistorialTrigger;
import principal.maquinaestado.estados.editor.historial.HistorialEditor;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.editor.modal.VentanaModalAmbiente;
import principal.maquinaestado.estados.editor.modal.VentanaModalConfirmarSalir;
import principal.maquinaestado.estados.editor.modal.VentanaModalLuz;
import principal.maquinaestado.estados.editor.modal.VentanaModalMundo;
import principal.maquinaestado.estados.editor.modal.VentanaModalTrigger;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
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
 * Editor maestro de mapas con inspectores dedicados con 'E' (Trigger, Ambiente,
 * Luz, Cofre), limpieza de puntero con Clic Derecho, previsualización en vivo
 * de clima y botones superiores.
 * 
 * @version 7.0 (Vanilla Java 8)
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
	private final Rectangle ultimaAreaTileAlterado = new Rectangle();

	private final Rectangle PALETA_MAPA;
	private final GrupoPaleta PALETAS;
	private final HistorialEditor HISTORIAL = new HistorialEditor();
	private final FloodFillTerreno FLOOD_FILL = new FloodFillTerreno();

	// Modales interactivos
	private final VentanaModalMundo modalMundo = new VentanaModalMundo();
	private final VentanaModalTrigger modalTrigger = new VentanaModalTrigger();
	private final VentanaModalAmbiente modalAmbiente = new VentanaModalAmbiente();
	private final VentanaModalLuz modalLuz = new VentanaModalLuz();
	private final VentanaModalConfirmarSalir modalConfirmarSalir;

	// Botones superiores
	private final BotonPixel btnGuardar;
	private final BotonPixel btnConfigMundo;
	private final BotonPixel btnSalir;

	// Captura de trazo para Undo/Redo
	private final Map<Integer, TipoTerreno> trazoTilesPrevios = new HashMap<Integer, TipoTerreno>();
	private boolean grabandoTrazo = false;

	private int tamanoPincel = 1;
	private boolean pincelCircular = false;
	private boolean mostrarGrid = false;
	private boolean modoPreviewLuz = false;

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

	private final Rectangle AREA_MOUSE_APUNTADO = new Rectangle(-1, -1, 1, 1);
	private final Rectangle AREA_BORRADO_AUX = new Rectangle();
	private final ArrayList<Ente> listaEntesABorrar = new ArrayList<Ente>(8);

	// Gestión de Contenedores
	private InventarioVault cofreAbierto = null;
	private final ItemPuntero itemPuntero = new ItemPuntero();
	private Ente contenedorEncontrado = null;

	private static final Font FUENTE_INFO = new Font(Font.SANS_SERIF, Font.PLAIN, 6);
	private static final Font FUENTE_SPAWN = new Font(Font.SANS_SERIF, Font.BOLD, 7);
	private VolatileImage bufferEditor;

	public EditorMapa(final int ladoTile, final int anchoTiles, final int altoTiles, final TipoTerreno tipoInicial,
			final GestorEstados ge) {
		this.GE = ge;
		this.LADO_TILE = ladoTile;
		this.ANCHO = anchoTiles * ladoTile;
		this.ALTO = altoTiles * ladoTile;
		this.TERRENO = new Terreno(anchoTiles, altoTiles, this.LADO_TILE, tipoInicial);

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height, this);
		this.MUNDO_EDITOR = new MundoEditor(this.TERRENO);
		this.asistenteCamara = new AsistenteCamara(0, 0, 0, 0);

		this.modalConfirmarSalir = new VentanaModalConfirmarSalir(() -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			this.salirAlMenu();
		}, () -> this.salirAlMenu());

		this.btnGuardar = new BotonPixel("Guardar", new Rectangle(this.PALETA_MAPA.width - 165, 2, 50, 14), () -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		});

		this.btnConfigMundo = new BotonPixel("Mundo", new Rectangle(this.PALETA_MAPA.width - 110, 2, 50, 14), () -> {
			this.modalMundo.abrir(this.metadatos);
		});

		this.btnSalir = new BotonPixel("Salir", new Rectangle(this.PALETA_MAPA.width - 55, 2, 50, 14), () -> {
			this.modalConfirmarSalir.abrir();
		});

		this.inicializarCamara();
	}

	public EditorMapa(final Escenario esc, final GestorEstados ge) {
		this.GE = ge;
		this.TERRENO = (esc != null) ? esc.getTerreno() : new Terreno(50, 50, Constantes.LADO_TILE, TipoTerreno.TIERRA);
		this.ANCHO = this.TERRENO.getAncho();
		this.ALTO = this.TERRENO.getAlto();
		this.LADO_TILE = this.TERRENO.ladoTile();
		this.metadatos = (esc != null) ? esc.getMetadatos() : new MetadatosEscenario();

		this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4),
				Constantes.ALTO_JUEGO);
		this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width,
				this.PALETA_MAPA.height, this);
		this.MUNDO_EDITOR = (esc != null) ? new MundoEditor(esc) : new MundoEditor(this.TERRENO);
		this.asistenteCamara = new AsistenteCamara(0, 0, 0, 0);

		this.modalConfirmarSalir = new VentanaModalConfirmarSalir(() -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			this.salirAlMenu();
		}, () -> this.salirAlMenu());

		this.btnGuardar = new BotonPixel("Guardar", new Rectangle(this.PALETA_MAPA.width - 165, 2, 50, 14), () -> {
			this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		});

		this.btnConfigMundo = new BotonPixel("Mundo", new Rectangle(this.PALETA_MAPA.width - 110, 2, 50, 14), () -> {
			this.modalMundo.abrir(this.metadatos);
		});

		this.btnSalir = new BotonPixel("Salir", new Rectangle(this.PALETA_MAPA.width - 55, 2, 50, 14), () -> {
			this.modalConfirmarSalir.abrir();
		});

		this.inicializarCamara();
	}

	public EditorMapa(final Terreno terreno, final GestorEstados ge) {
		this(new Escenario(terreno, "[]", "[]", "[]", "[]", "[]", "[]", "[]", "[]", new MetadatosEscenario()), ge);
	}

	public EditorMapa(final String rutaMapa, final GestorEstados ge) {
		this(EscenarioLoader.importarEscenario(new File(rutaMapa)), ge);
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
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		this.GE.disposeEditor();
	}

	@Override
	public void actualizar() {
		// 1. Modales (Prioridad absoluta y bloqueo de foco)
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

		// 2. Cofre Abierto
		if (this.cofreAbierto != null) {
			if (!this.PALETAS.isPaletaItemSelected()) {
				this.PALETAS.setPaletaItemSelected();
			}
			this.actualizarProyeccionRaton();
			this.PALETAS.actualizar(this.RATON);
			this.actualizarCofreAbierto();
			// 1. Si el puntero tiene un ítem sostenido de inventario, clic derecho lo
			// libera
			if (this.itemPuntero.contieneItem()) {
				if (this.RATON.presionadoClickDerUnicaAct()) {
					this.itemPuntero.limpiar();
					GestorSonido.reproducir(IDSonido.GOLPE_1);

					// D. Si no había nada que borrar, DESELECCIONA el sello/herramienta activa en
					// la paleta
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

		// 3. Botones Superiores
		this.btnGuardar.actualizar(this.RATON);
		this.btnConfigMundo.actualizar(this.RATON);
		this.btnSalir.actualizar(this.RATON);

		// 4. Clima y Luces en el editor
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

		this.actualizarInspeccionConTeclaE();
		this.alterarElementoSeleccionado();
		this.borrarElemento();

		this.MUNDO_EDITOR.actualizar();

		// Salida segura con ESC
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

		// Ciclar Clima (K)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_K) && (Globales.GESTOR_CLIMA != null)) {
			final TipoClima[] climas = TipoClima.values();
			this.idxClimaTest = (this.idxClimaTest + 1) % climas.length;
			Globales.GESTOR_CLIMA.setClima(climas[this.idxClimaTest], 0.0);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Ciclar Hora (H)
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_H) && (Globales.GESTOR_LUZ != null)
				&& (Globales.GESTOR_LUZ.getCiclo() != null)) {
			final FaseDia[] fases = FaseDia.values();
			this.idxHoraTest = (this.idxHoraTest + 1) % fases.length;
			Globales.GESTOR_LUZ.getCiclo().setHora(fases[this.idxHoraTest]);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}

		// Undo (Ctrl+Z) y Redo (Ctrl+Y)
		final boolean ctrl = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_CONTROL);
		if (ctrl && Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_Z)) {
			if (this.HISTORIAL.puedeDeshacer()) {
				this.HISTORIAL.deshacer();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		} else if (ctrl && Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_Y)) {
			if (this.HISTORIAL.puedeRehacer()) {
				this.HISTORIAL.rehacer();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}
	}

	private void actualizarInspeccionConTeclaE() {
		final boolean teclaE = Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E);
		if (!teclaE || !this.tileApuntadoValido) {
			return;
		}

		final Rectangle areaCursor = new Rectangle(this.AREA_MOUSE_APUNTADO.x - 8, this.AREA_MOUSE_APUNTADO.y - 8, 16,
				16);

		// 1. Triggers (ZonaTP)
		for (final ZonaTP tp : this.MUNDO_EDITOR.getTriggersEditor()) {
			if (tp.getArea().intersects(areaCursor)) {
				this.modalTrigger.abrir(tp);
				return;
			}
		}

		// 2. Zonas de Ambiente (Bioma/Cueva)
		for (final ZonaAmbiente z : this.MUNDO_EDITOR.getZonasAmbienteEditor()) {
			if (z.getLimites().intersects(areaCursor)) {
				this.modalAmbiente.abrir(z);
				return;
			}
		}

		// 3. Luces Estáticas (Antorcha / Fogata)
		final FuenteLuz luz = this.MUNDO_EDITOR.getLuzEn(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y, 16);
		if (luz != null) {
			this.modalLuz.abrir(luz);
			return;
		}

		// 4. Contenedores / Cofres
		this.contenedorEncontrado = null;
		this.MUNDO_EDITOR.paraCadaObjetoEn(areaCursor, new AccionEntidad<Objeto>() {
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
		final double centroVX = viewW / 2.0;
		final double centroVY = Constantes.ALTO_JUEGO / 2.0;

		final double z = Math.max(0.2, Globales.CAMARA.getZoom());
		final int mouseX = this.RATON.getPosicionXEscalada();
		final int mouseY = this.RATON.getPosicionYEscalada();

		if (mouseX < viewW) {
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
		if (this.itemPuntero.contieneItem()) {
			return;
		}

		final Paleta paleta = this.PALETAS.getPaletaActual();
		if ((paleta == null) || !this.tileApuntadoValido) {
			return;
		}

		final int mouseTileX = Math.floorDiv(this.AREA_MOUSE_APUNTADO.x, this.LADO_TILE);
		final int mouseTileY = Math.floorDiv(this.AREA_MOUSE_APUNTADO.y, this.LADO_TILE);
		final int cantTilesX = this.TERRENO.getAncho() / this.LADO_TILE;

		// 1. SUELOS
		if (paleta instanceof PaletaTile) {
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
		// 2. RECURSOS
		else if (paleta instanceof PaletaComplento) {
			final PaletaComplento paletaObj = (PaletaComplento) paleta;
			if (this.RATON.presionadoClickIzq()
					&& this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaComplento.EntradaPaleta entrada = paletaObj.getEntradaSeleccionada();
				if ((entrada != null) && (entrada.icono != null)) {
					final int posX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
					final int posY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

					final Objeto nuevoObj = paletaObj.crearInstanciaSeleccionada(posX, posY);
					if (nuevoObj != null) {
						this.MUNDO_EDITOR.meterEntidad(nuevoObj);
						this.HISTORIAL.registrarAccion(new AccionHistorialEntidad(this.MUNDO_EDITOR, nuevoObj, true));
					}
				}
			}
		}
		// 3. CRIATURAS
		else if (paleta instanceof PaletaCriaturas) {
			final PaletaCriaturas paletaCriat = (PaletaCriaturas) paleta;
			if (this.RATON.presionadoClickIzq()
					&& this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaCriaturas.EntradaCriatura entrada = paletaCriat.getEntradaSeleccionada();
				if ((entrada != null) && (entrada.icono != null)) {
					final int spriteX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
					final int spriteY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

					final int hitboxX = spriteX + entrada.margenX;
					final int hitboxY = spriteY + entrada.margenY;

					final Criatura nuevaCriat = entrada.creador.crear(hitboxX, hitboxY);
					if (nuevaCriat != null) {
						this.MUNDO_EDITOR.meterEntidad(nuevaCriat);
						this.HISTORIAL.registrarAccion(new AccionHistorialEntidad(this.MUNDO_EDITOR, nuevaCriat, true));
					}
				}
			}
		}
		// 4. TRIGGERS / VOLÚMENES / LUCES
		else if (paleta instanceof PaletaTriggers) {
			final PaletaTriggers pTriggers = (PaletaTriggers) paleta;
			if (this.RATON.presionadoClickIzq()
					&& this.GT_COLOCACION.transcurrioMiliSegundos(TIEMPO_ESPERA_MS_COLOCACION)) {
				this.GT_COLOCACION.establecerReferenciaTiempoActual();

				final PaletaTriggers.EntradaTrigger ent = pTriggers.getEntradaSeleccionada();
				if (ent != null) {
					final int snapX = mouseTileX * this.LADO_TILE;
					final int snapY = mouseTileY * this.LADO_TILE;

					switch (ent.categoria) {
					case TELEPORT_PUERTA:
						final ZonaTP tp = new ZonaTP(new Rectangle(snapX, snapY, 20, 20),
								new PuertaMapa("Mapa1", "Exterior", "Comienzo", false, null));
						this.MUNDO_EDITOR.agregarTrigger(tp);
						this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, tp, true));
						break;
					case ZONA_AMBIENTE_BIOMA:
						final ZonaAmbiente zb = new ZonaAmbiente(snapX, snapY, 128, 128, new Color(60, 220, 120, 80),
								IntensidadNiebla.LEVE, "Bioma", false);
						this.MUNDO_EDITOR.agregarZonaAmbiente(zb);
						this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, zb, true));
						break;
					case ZONA_AMBIENTE_CUEVA:
						final ZonaAmbiente zc = new ZonaAmbiente(snapX, snapY, 128, 128, new Color(0, 0, 0, 255),
								IntensidadNiebla.DESACTIVADA, "Cueva", true);
						this.MUNDO_EDITOR.agregarZonaAmbiente(zc);
						this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, zc, true));
						break;
					case LUZ_ANTORCHA:
						if (Globales.GESTOR_LUZ != null) {
							final FuenteLuz luz = Globales.GESTOR_LUZ.agregarLuzEstatica(snapX + 8, snapY + 8,
									TipoLuz.ANTORCHA, 80);
							this.MUNDO_EDITOR.agregarLuzEstatica(luz);
							this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, luz, true));
						}
						break;
					case LUZ_FOGATA:
						if (Globales.GESTOR_LUZ != null) {
							final FuenteLuz luz = Globales.GESTOR_LUZ.agregarLuzEstatica(snapX + 8, snapY + 8,
									TipoLuz.FOGATA, 140);
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
		// 1. Si el puntero tiene un ítem sostenido de inventario, clic derecho lo
		// libera
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
//			this.AREA_BORRADO_AUX.setBounds(this.AREA_MOUSE_APUNTADO.x - radioBorrado,
//					this.AREA_MOUSE_APUNTADO.y - radioBorrado, radioBorrado * 2, radioBorrado * 2);
			this.AREA_BORRADO_AUX.setBounds(this.AREA_MOUSE_APUNTADO.x - 1, this.AREA_MOUSE_APUNTADO.y - 1, 2, 2);

			boolean elementoBorrado = false;

			// A. Borrar Spawns
			final Spawn spawnBorrado = this.MUNDO_EDITOR.eliminarSpawnEn(this.AREA_MOUSE_APUNTADO.x,
					this.AREA_MOUSE_APUNTADO.y, radioBorrado);
			if (spawnBorrado != null) {
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return;
			}

			// B. Borrar Triggers / Zonas / Luces (Antorchas y Fogatas con tolerancia 16px)
			final Object triggerBorrado = this.MUNDO_EDITOR.eliminarTriggerOAmbienteEn(this.AREA_MOUSE_APUNTADO.x,
					this.AREA_MOUSE_APUNTADO.y, radioBorrado);
			if (triggerBorrado != null) {
				this.HISTORIAL.registrarAccion(new AccionHistorialTrigger(this.MUNDO_EDITOR, triggerBorrado, false));
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return;
			}

			// C. Borrar Entidades y Objetos
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

			if (elementoBorrado) {
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.RATON.presionadoClickDerUnicaAct()) {
				// D. Si no había nada que borrar, DESELECCIONA el sello/herramienta activa en
				// la paleta
				final Paleta p = this.PALETAS.getPaletaActual();
				if ((p != null) && p.haySeleccion()) {
					p.deseleccionar();
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}

			this.listaEntesABorrar.clear();
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
		final int centroVX = viewW / 2;
		final int centroVY = Constantes.ALTO_JUEGO / 2;
		final double z = Math.max(0.2, Globales.CAMARA.getZoom());

		// 1. Renderizado a escala 1:1 en VolatileImage
		final Graphics2D gBuf = this.bufferEditor.createGraphics();
		try {
			gBuf.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			gBuf.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			Render2D.dibujarRectanguloRelleno(gBuf, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.BLACK);

			this.TERRENO.pintar(gBuf);
			this.MUNDO_EDITOR.pintar(gBuf);
			this.MUNDO_EDITOR.pintarTriggersYAmbientes(gBuf);
			this.pintarSpawnsEnMundo(gBuf);

			// Clima en tiempo real proyectado sobre el mundo
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

		} finally {
			gBuf.dispose();
		}

		// 2. Proyección sobre el Viewport del editor
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

		// 4. Barra Superior, Botones y Paleta lateral
		this.pintarBarraEstadoSuperior(g);
		this.btnGuardar.pintar(g);
		this.btnConfigMundo.pintar(g);
		this.btnSalir.pintar(g);

		this.PALETAS.pintar(g);
		this.pintarTooltipPaleta(g);

		// 5. Modales (Capa superior absoluta)
		this.modalMundo.pintar(g);
		this.modalTrigger.pintar(g);
		this.modalAmbiente.pintar(g);
		this.modalLuz.pintar(g);
		this.modalConfirmarSalir.pintar(g);

		// 6. Ítem flotante
		this.itemPuntero.pintar(g, this.RATON.getPuntoPosicionEscalado());
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
				final int posX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
				final int posY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

				Render2D.dibujarImagenConTransparenciaRefCamara(g, entrada.icono, posX, posY, 0.65f);
				final Color color = entrada.esCosechable ? Color.GREEN : Color.CYAN;
				Render2D.dibujarRectanguloContornoRefCamara(g, posX, posY, entrada.icono.getWidth(),
						entrada.icono.getHeight(), color);
			}

		} else if (paleta instanceof PaletaCriaturas) {
			final PaletaCriaturas p = (PaletaCriaturas) paleta;
			final PaletaCriaturas.EntradaCriatura entrada = p.getEntradaSeleccionada();

			if ((entrada != null) && (entrada.icono != null)) {
				final int spriteX = this.AREA_MOUSE_APUNTADO.x - (entrada.icono.getWidth() / 2);
				final int spriteY = this.AREA_MOUSE_APUNTADO.y - (entrada.icono.getHeight() / 2);

				Render2D.dibujarImagenConTransparenciaRefCamara(g, entrada.icono, spriteX, spriteY, 0.65f);
				Render2D.dibujarRectanguloContornoRefCamara(g, spriteX + entrada.margenX, spriteY + entrada.margenY,
						entrada.anchoHitbox, entrada.altoHitbox, Color.RED);
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

	private void pintarBarraEstadoSuperior(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_INFO);

		final String toolInfo = (this.PALETAS.getPaletaActual() instanceof PaletaTile)
				? ((PaletaTile) this.PALETAS.getPaletaActual()).getHerramientaSeleccionada().getNombreVisible()
				: "Colocación";

		Render2D.dibujarString(g,
				"Herramienta: " + toolInfo + " | Pincel: " + this.tamanoPincel + "x" + this.tamanoPincel + " ("
						+ (this.pincelCircular ? "O" : "[]") + ") | Zoom: "
						+ String.format("%.2f", Globales.CAMARA.getZoom()) + "x | Grilla[G]: "
						+ (this.mostrarGrid ? "ON" : "OFF") + " | Luz[L]: " + (this.modoPreviewLuz ? "ON" : "OFF")
						+ " | Clima[K] | Hora[H]",
				15, 12, Color.CYAN);

		Render2D.dibujarString(g,
				"Cursor: (" + this.AREA_MOUSE_APUNTADO.x + ", " + this.AREA_MOUSE_APUNTADO.y + ") | Cámara: (" + this.x
						+ ", " + this.y + ") | Undo/Redo: " + this.HISTORIAL.getCantidadDeshacer() + "/"
						+ this.HISTORIAL.getCantidadRehacer(),
				15, 23, Color.WHITE);

		Render2D.dibujarString(g, "[Ctrl+Z] Undo | [Ctrl+Y] Redo | [E] Configurar Elemento", 15, 34, Color.YELLOW);

		g.setFont(fontPrevia);
	}

	public void guardarMapa(final String nombre) {
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