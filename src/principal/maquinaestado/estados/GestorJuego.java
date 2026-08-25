package principal.maquinaestado.estados;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.proyectil.explosivo.BolaFuego;
import principal.eventos.Evento;
import principal.eventos.EventoJugadorZonaTP;
import principal.igu.MotorIGU;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.maquinaestado.estados.pantallaCarga.cargaMapa;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.musica.IDMusica;

public final class GestorJuego implements EstadoJuego, cargaMapa {
	protected final GestorEstados GE;
	protected final GestorPartida GP;
	protected final GestorTiempo GT_MOSTRAR_PANTALLA_MUERTE;
	protected final ArrayList<Evento> EVENTOS = new ArrayList<Evento>();
	protected Mapa mapa;
	protected final int TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE = 1500;
	Tile tilePisado = null;

	private boolean mostrarPantallaMuerte;
	private final Raton RATON = Globales.RATON;

	private final MotorIGU motoIGU;

	public GestorJuego(final GestorEstados ge, final GestorPartida gp) {
		this.GE = ge;
		this.GP = gp;
		this.GT_MOSTRAR_PANTALLA_MUERTE = new GestorTiempo();
		this.motoIGU = new MotorIGU();
		GestorMusica.reproducirMusicaFondoPrincipal(IDMusica.FONDO_FOREST);
	}

	@Override
	public void actualizar() {

		if (this.detectarCambioAMenu()) {
			return;
		}
		if (!Globales.partidaIniciada) {
			return;
		}

		if (Globales.pausa) {
			GestorMusica.actualizarMusicaFondoPrincipal(false);
			return;
		}
		GestorMusica.actualizarMusicaFondoPrincipal(true);
		// COD PRUEBA
		this.actualizarCambioCamaraConEntesYZoom();
		// FIN COD PRUEBA
		Globales.JUGADOR.actualizar();

		if (!Globales.JUGADOR.estaEliminado()) {
			// 1. Actualización centralizada de inventarios (Jugador, Tercero/Vault y
			// Puntero)
			Globales.GESTOR_INVENTARIO.actualizar(this.RATON, this.mapa.getMundoActual());

			// 2. Actualización de mapa y eventos
			this.mapa.actualizar();
			this.actualizarEventos();
		} else {
			// Lógica en caso de jugador eliminado (ej: game over o respawn)
		}

		this.verificarPantallaMuerte();

		this.motoIGU.actualizar();

		{
			final Shape s = Globales.JUGADOR.getAreaInterseccionMovimiento();
			this.tilePisado = this.mapa.getMundoActual().getTerreno().getTileReferenciado(
					s.getBounds().x + (s.getBounds().width / 2), s.getBounds().y + s.getBounds().height);
			if (this.tilePisado == null) {

				return;
			}
		}

	}

	private void actualizarCambioCamaraConEntesYZoom() {
		// Control de Zoom por Teclado (+, - y 0)
		if (Globales.TECLADO.TECLA_ZOOM_IN.presionadoUnicaActualizacion()) {
			Globales.CAMARA.aumentarZoom();
			; // Zoom In suave
		} else if (Globales.TECLADO.TECLA_ZOOM_OUT.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reducirZoom();
			; // Zoom Out suave
		}

		if (Globales.TECLADO.TECLA_ZOOM_REINICIAR.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reiniciarZoom(); // Reset a 100% normal
		}

		if (Globales.RATON.presionadoClickIzqUnicaAct()) {
			for (final Ente e : this.mapa.getMundoActual().getEnteIntersectados(
					Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara(), true)) {
				Globales.CAMARA.setEntidadEnfocada(e);
				Globales.CAMARA.habilitarGestorLimite();
				System.out.println("ENFOQUE CAMARA CAMBIADO: " + e.getClass().getName());
			}
		}

	}

	private void actualizarEventos() {
		for (int i = 0; i < this.EVENTOS.size(); i++) {
			this.EVENTOS.get(i).actualizar();
			if (this.EVENTOS.get(i).estaEliminado()) {
				this.EVENTOS.remove(this.EVENTOS.get(i));
			}
		}
	}

	private boolean detectarCambioAMenu() {
		if (Globales.TECLADO.TECLA_ESCAPE.presionado()) {
			GestorMusica.actualizarMusicaFondoPrincipal(false);
			if (Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
				Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
				Globales.TECLADO.TECLA_ESCAPE.soltar();
				return false;
			}
//	    this.sonidoFondo.actualizar(false);
			this.GP.establecerEstadoActivoMenu();
			return true;
		}
		return false;
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!Globales.partidaIniciada) {
			return;
		}

		final double zoom = Globales.CAMARA.getZoom(); // ej: 1.0 (normal), 1.5 (zoom-in), 0.75 (zoom-out)
		final boolean conZoom = (zoom != 1.0);

		// =========================================================================
		// === CAPAS DE MUNDO: TERRENO, ENTIDADES Y DEBUG (CON ZOOM)
		// =========================================================================
		if (conZoom) {
			g.translate(Constantes.CENTROX, Constantes.CENTROY);
			g.scale(zoom, zoom);
			g.translate(-Constantes.CENTROX, -Constantes.CENTROY);
		}

		// 1. Capa de Mundo
		this.mapa.pintar(g);

		// 2. Capa de Entidades
		Globales.JUGADOR.pintar(g);

		if (conZoom) {
			// Restauramos la matriz al 100% para que el HUD no sufra ningún cambio
			g.translate(Constantes.CENTROX, Constantes.CENTROY);
			g.scale(1.0 / zoom, 1.0 / zoom);
			g.translate(-Constantes.CENTROX, -Constantes.CENTROY);
		}

		// =========================================================================
		// === CAPAS DE INTERFAZ / HUD (ESCALA FIJA 1:1)
		// =========================================================================

		// 1. Capa de Debug
		this.pintarDebug(g);

		// 3. Capa de Inventarios, Contenedores y Tooltips
		if (!Globales.JUGADOR.estaEliminado()) {
			this.pintarInventarios(g);
		}

		// 4. Capa de HUD / IGU permanente
		this.motoIGU.pintar(g);

		// 5. Capa de Overlays y Fin de Partida
		this.pintarPantallaDerrota(g);
	}

	/**
	 * Gestiona el orden estricto de capas (Z-Order) de los inventarios: Pasada 1:
	 * Fondos y Slots de todas las ventanas abiertas. Pasada 2: Tooltips y Punteros
	 * flotantes por encima de cualquier ventana.
	 */
	private void pintarInventarios(final Graphics2D g) {
		// Pasada 1: Ventanas y Grillas (Fondo y slots de Jugador y Tercero si está
		// abierto)
		Globales.GESTOR_INVENTARIO.pintar(g);

		// Pasadas 2 y 3: Tooltips e Ítem sostenido en el Puntero (Capa superior
		// absoluta)
		Globales.GESTOR_INVENTARIO.pintarTooltipsYPuntero(g, this.RATON.getPuntoPosicionEscalado());
	}

	/**
	 * Dibuja la pantalla de derrota si el jugador ha muerto.
	 */
	private void pintarPantallaDerrota(final Graphics2D g) {
		if (this.mostrarPantallaMuerte && this.GT_MOSTRAR_PANTALLA_MUERTE
				.transcurrioMiliSegundos(this.TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE)) {
			final String texto = "DERROTA";
			final float tamanoLetra = 48f;
			final Color color = Color.RED;

			final Font fuenteOriginal = g.getFont();
			g.setFont(fuenteOriginal.deriveFont(tamanoLetra));

			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
			final int x = Constantes.CENTROX - (anchoTexto / 2);
			final int y = Constantes.CENTROY - (altoTexto / 2);

			DibujoDebug.dibujarString(g, texto, x, y, color);
			g.setFont(fuenteOriginal);
		}
	}

	private void pintarDebug(final Graphics2D g) {
		if (Globales.pausa) {
			DibujoDebug.dibujarString(g, "PAUSA", 10, 10, Color.RED);
		}
		this.pintarTiempoJugado(g);
		if (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()) {
			DibujoDebug.dibujarString(g, this.tilePisado != null ? this.tilePisado.toString() : "PuntoTile: (none)",
					120, 20, Color.white);
			if (this.tilePisado != null) {
				this.tilePisado.pintarContorno(g, Color.WHITE);
			}
		}

		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			g.setColor(Color.green);
			DibujoDebug.dibujarString(g, "X: " + String.valueOf(Globales.JUGADOR.getPosicionXInt()), 20, 80);
			DibujoDebug.dibujarString(g, "Y: " + String.valueOf(Globales.JUGADOR.getPosicionYInt()), 20, 95);
			DibujoDebug.dibujarString(g, "X_PARADO: " + String.valueOf(Globales.JUGADOR.getPosicionXParado()), 20, 110);
			DibujoDebug.dibujarString(g, "Y_PARADO: " + String.valueOf(Globales.JUGADOR.getPosicionYParado()), 20, 125);
			DibujoDebug.dibujarString(g, "Velocidad: " + String.valueOf(Globales.JUGADOR.getVelocidad()), 20, 140);
			DibujoDebug.dibujarString(g,
					"Dijkstra(F2): " + (Globales.TECLADO.TECLA_DIJKSTRA.presionado() ? "Activo" : "Inactivo"), 20, 155);
			DibujoDebug.dibujarString(g,
					"DijkstraInfo(F6): " + (Globales.TECLADO.TECLA_DIJKSTRA_INFO.presionado() ? "Activo" : "Inactivo"),
					20, 170);
			DibujoDebug.dibujarString(g, "DebugGroupTile(F4): "
					+ (Globales.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado() ? "Activo" : "Inactivo"), 20, 185);
			DibujoDebug.dibujarString(g,
					"DebugTile(F3): " + (Globales.TECLADO.TECLA_DEBUG_TILE.presionado() ? "Activo" : "Inactivo"), 20,
					200);
			DibujoDebug.dibujarString(g, "DebugTileInfo(F5): "
					+ (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado() ? "Activo" : "Inactivo"), 20, 215);
			DibujoDebug.dibujarString(g, "VerColisiones(F7): "
					+ (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() ? "Activo" : "Inactivo"), 20, 230);
			DibujoDebug.dibujarString(g, "OcultarTerreno(F8): "
					+ (Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? "Activo" : "Inactivo"), 20, 245);
			DibujoDebug.dibujarString(g,
					"OcultarComplementos(F9): "
							+ (Globales.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado() ? "Activo" : "Inactivo"),
					20, 260);
			DibujoDebug.dibujarString(g,
					"VerAlcanceAtaque(F10): "
							+ (Globales.TECLADO.TECLA_VER_ALCANCE_ATAQUE.presionado() ? "Activo" : "Inactivo"),
					20, 275);
			DibujoDebug.dibujarString(g, "Direccion: " + Globales.JUGADOR.getDireccion().toString(), 20, 290);
			DibujoDebug.dibujarString(g, "Estados: " + Globales.JUGADOR.getStringEstados(), 20, 305);
			DibujoDebug.dibujarString(g,
					"FPS Limitado(F11): " + (Globales.TECLADO.TECLA_FPS_LIMITE.presionado() ? "Activo" : "Inactivo"),
					20, 320);
		}
	}

	public void agregarObjetosAlMundo() {

		// CODIGO PRUEBA
		this.mapa.getMundoActual().crearProyectil(new BolaFuego(25, 0.25, 100000000, false, this.mapa.getMundoActual(),
				1005, 392, 40, Direccion.OESTE, null));
//	this.mundo.getMundoActual().crearProyectil(new ProyectilGranada(40, this.areaAUX, this.mundo.getMundoActual(), 954, 79, ListaModelosItem.COD_CONSUMIBLE_GRANADAT1));
		this.mapa.getMundoActual().meterEntidad(new GranadaT1(800, 80, 50));
		final ZonaTP zonaTP2 = new ZonaTP(new Rectangle(684, 215, 20, 20), null);
		;
		final ZonaTP zonaTP = new ZonaTP(new Rectangle(878, 173, 20, 20),
				new PuertaArea(new Rectangle(832, 333, 16, 16)));
		zonaTP2.setPuertaTP(new PuertaMapa("escenario2.json", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, false, this.GP));

		this.mapa.getMundoActual().meterEntidad(zonaTP);
		this.mapa.getMundoActual().meterEntidad(zonaTP2);
		this.EVENTOS.add(new EventoJugadorZonaTP(zonaTP, this, true));
		this.EVENTOS.add(new EventoJugadorZonaTP(zonaTP2, this, true));

		this.mapa.getMundoActual().meterEntidad(new Complemento(773, 177, ListaModeloComplemento.COD_CASA_1));
	}

	public void establecerCriaturas() {

	}

	@Override
	public void cargarMapa(final GestorCarga gc, final String nombreMapa, final boolean reset,
			final String nombreSpawn) {
		if (gc != null) {
			gc.setPorcentajeCarga(10);
			gc.setDetalleCarga("Cargando mapa " + nombreMapa);
		}

		// 1. Carga del mapa y escenario
		this.mapa = MapaManager.cargarMapa(nombreMapa, gc);

		if ((this.mapa == null) || (this.mapa.getMundoActual() == null)) {
			System.err.println("Error crítico: El mapa cargado es nulo.");
			if (gc != null) {
				gc.setCompleto(true);
			}
			return;
		}

		if (gc != null) {
			gc.setPorcentajeCarga(80);
			gc.setDetalleCarga("Configurando jugador y mundo");
		}

		// 2. Establecer posición del jugador y mundo
		if (reset) {
			Globales.JUGADOR.restablecerYCambiarMundo(this.mapa.getMundoActual());
		} else {
			Globales.JUGADOR.setMundo(this.mapa.getMundoActual());
		}

		// Mover jugador al punto de Spawn
		if (this.mapa.getMundoActual().getSpawn(nombreSpawn) != null) {
			this.mapa.getMundoActual().getSpawn(nombreSpawn).moverJugadorCentrado();
		}

		// Configuración de Cámara e Inventario
		Globales.CAMARA.setEntidadEnfocada(Globales.JUGADOR);
		Globales.CAMARA.habilitarGestorLimite();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().establecerMundo(this.mapa.getMundoActual());
		Globales.RATON.soltar();

		// Activar Pathfinding de IA
		if (!Globales.TECLADO.TECLA_DIJKSTRA.presionado()) {
			Globales.TECLADO.TECLA_DIJKSTRA.presionar();
		}

		Globales.partidaIniciada = true;

		// 3. Finalizar carga
		if (gc != null) {
			gc.setDetalleCarga("¡Carga completa!");
			gc.setCompleto(true);
		}
	}

	private void pintarTiempoJugado(final Graphics2D g) {
		final String texto = String.valueOf(Globales.horasJugadas) + "h " + String.valueOf(Globales.minutosJugados)
				+ "m " + String.valueOf(Globales.segundosJugados) + "s";
		DibujoDebug.dibujarString(g, texto, 20, 20, Color.CYAN);
	}

	public Terreno getTerreno() {
		return this.mapa.getMundoActual().getTerreno();
	}

	public Mundo getMundo() {
		return this.mapa.getMundoActual();
	}

	public Mapa getMapa() {
		return this.mapa;
	}

	public void meterEvento(final Evento e) {
		this.EVENTOS.add(e);
	}

	protected void verificarPantallaMuerte() {
		if (Globales.JUGADOR.estaEliminado() && !this.mostrarPantallaMuerte) {
			this.mostrarPantallaMuerte = true;
			this.GT_MOSTRAR_PANTALLA_MUERTE.establecerReferenciaTiempoActual();

		}
	}

}
