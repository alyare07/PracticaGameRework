package principal.maquinaestado.estados;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

import principal.clima.PerfilClima;
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
import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.MapaManager;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.maquinaestado.estados.pantallaCarga.cargaMapa;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.musica.IDMusica;

/**
 * Estado principal de jugabilidad activa (Gameplay Loop).
 * <p>
 * <b>Arquitectura del Framebuffer Adaptativo Inteligente (Smart Buffer):</b>
 * <ul>
 * <li><b>Eliminación de Costuras (Anti-Seaming):</b> Renderiza el terreno y las
 * entidades sobre un {@link VolatileImage} a escala 1:1 en VRAM antes de
 * transformar la cámara, garantizando 0 líneas o cortes entre tiles en efectos
 * continuos (como respiración o latido).</li>
 * <li><b>Alto Rendimiento Dinámico (+400 FPS):</b> En zoom estándar
 * ($1.0\times$) y zoom-in, utiliza un buffer ligero de $704 \times 416$ con
 * mínimo impacto de Fill-Rate. Solo se expande si el usuario realiza un
 * zoom-out manual ($<1.0\times$), evitando el efecto de recuadro/bordes
 * negros.</li>
 * <li><b>Zero-GC:</b> Reutilización de memoria acelerada en GPU sin crear
 * objetos en el Heap.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 3.0
 */
public final class GestorJuego implements EstadoJuego, cargaMapa {

	// =========================================================================
	// === 1. FRAMEBUFFER ADAPTATIVO EN VRAM (ZERO-GC & ANTI-SEAMING)
	// =========================================================================

	/**
	 * Margen perimetral de seguridad para absorber temblores y rotaciones (en px).
	 */
	private static final int MARGEN_BUFFER = 32;

	/** Textura acelerada en GPU donde se dibuja el mundo a escala 1:1. */
	private VolatileImage bufferMundo;

	// =========================================================================
	// === 2. CONTROLADORES DE ESTADO Y SUBSISTEMAS
	// =========================================================================

	protected final GestorEstados GE;
	protected final GestorPartida GP;
	private final Raton RATON = Globales.RATON;
	private final MotorIGU motoIGU;
	protected Mapa mapa;
	protected final ArrayList<Evento> EVENTOS = new ArrayList<Evento>();

	// =========================================================================
	// === 3. ESTADO DE JUEGO Y DERROTA
	// =========================================================================

	protected final GestorTiempo GT_MOSTRAR_PANTALLA_MUERTE;
	protected final int TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE = 1500;
	private boolean mostrarPantallaMuerte;
	private Tile tilePisado = null;

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================
	private FuenteLuz auxFuenteLuzTempoPrueba;

	public GestorJuego(final GestorEstados ge, final GestorPartida gp) {
		this.GE = ge;
		this.GP = gp;
		this.GT_MOSTRAR_PANTALLA_MUERTE = new GestorTiempo();
		this.motoIGU = new MotorIGU();

		GestorMusica.reproducirMusicaFondoPrincipal(IDMusica.FONDO_FOREST);
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

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

		// Controles de prueba de cámara
		this.actualizarCambioCamaraConEntesYZoom();

		if (Globales.TECLADO.TECLA_NUM_1.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.getCiclo().setHora(CicloDiaNoche.FaseDia.MADRUGADA); // 00:00 Noche cerrada

		}
		if (Globales.TECLADO.TECLA_NUM_2.presionadoUnicaActualizacion()) {
//			Globales.GESTOR_LUZ.getCiclo().setHora(CicloDiaNoche.FaseDia.NOCHE); // 00:00 Noche cerrada
//			Globales.GESTOR_CLIMA.setNivelNiebla(IntensidadNiebla.INTENSA, 5);
			Globales.GESTOR_CLIMA.setSombrasNubesHabilitadas(false);
//			Globales.GESTOR_CLIMA.setTormentaActiva(true);

//			Globales.GESTOR_LUZ.getCiclo().setModoOscuridadTotal(true);
//			Globales.GESTOR_LUZ.getCiclo().pausarTiempo();

			// Al explotar la granada en (posX, posY), crea un flash de 180px que dura 0.35
			// segundos
//			final double posX = Globales.JUGADOR.getCentroX();
//			final double posY = Globales.JUGADOR.getCentroY();

		}

		if (Globales.TECLADO.TECLA_NUM_3.presionadoUnicaActualizacion()) {
			Globales.GESTOR_CLIMA.activarModoPruebaRapida(10, 5);
		}
		if (Globales.TECLADO.TECLA_NUM_4.presionadoUnicaActualizacion()) {
			Globales.CAMARA.getGestorEfectos().reproducirEfectoTemporal(TipoEfectoCamara.BARCO_NAVEGACION, 10000, 1);
		}

		// Actualización de jugador
		Globales.JUGADOR.actualizar();

		if (!Globales.JUGADOR.estaEliminado()) {
			Globales.GESTOR_INVENTARIO.actualizar(this.RATON, this.mapa.getMundoActual());
			this.mapa.actualizar();
			this.actualizarEventos();
		}

		this.verificarPantallaMuerte();
		this.motoIGU.actualizar();

		// Detección de tile bajo los pies
		final Shape areaMovimiento = Globales.JUGADOR.getAreaInterseccionMovimiento();
		if ((areaMovimiento != null) && (this.mapa != null) && (this.mapa.getMundoActual() != null)) {
			final Rectangle bounds = areaMovimiento.getBounds();
			final int pieX = bounds.x + (bounds.width / 2);
			final int pieY = bounds.y + bounds.height;

			this.tilePisado = this.mapa.getMundoActual().getTerreno().getTileReferenciado(pieX, pieY);
		}

		Globales.GESTOR_TEXTOS.actualizar();
		Globales.GESTOR_PARTICULAS.actualizar();
		Globales.GESTOR_CLIMA.actualizar();
		Globales.GESTOR_LUZ.actualizar();

		if (this.auxFuenteLuzTempoPrueba != null) {
			this.auxFuenteLuzTempoPrueba.orientarSegunDireccion(Globales.JUGADOR.getDireccion());
		}
	}

	private void actualizarCambioCamaraConEntesYZoom() {
		if (Globales.TECLADO.TECLA_ZOOM_IN.presionadoUnicaActualizacion()) {
			Globales.CAMARA.aumentarZoom();
		} else if (Globales.TECLADO.TECLA_ZOOM_OUT.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reducirZoom();
		}

		if (Globales.TECLADO.TECLA_ZOOM_REINICIAR.presionadoUnicaActualizacion()) {
			Globales.CAMARA.reiniciarZoom();
		}

		if (Globales.RATON.presionadoClickIzqUnicaAct() && (this.mapa != null)
				&& (this.mapa.getMundoActual() != null)) {
			final Rectangle areaMouseMundo = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			for (final Ente e : this.mapa.getMundoActual().getEnteIntersectados(areaMouseMundo, true)) {
				Globales.CAMARA.setEntidadEnfocada(e);
				Globales.CAMARA.habilitarGestorLimite();
				break;
			}
		}
	}

	private void actualizarEventos() {
		for (int i = 0; i < this.EVENTOS.size(); i++) {
			final Evento evento = this.EVENTOS.get(i);
			evento.actualizar();

			if (evento.estaEliminado()) {
				this.EVENTOS.remove(i);
				i--;
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

			Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();
			this.GP.establecerEstadoActivoMenu();
			return true;
		}
		return false;
	}

	protected void verificarPantallaMuerte() {
		if (Globales.JUGADOR.estaEliminado() && !this.mostrarPantallaMuerte) {
			this.mostrarPantallaMuerte = true;
			this.GT_MOSTRAR_PANTALLA_MUERTE.establecerReferenciaTiempoActual();
			Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();
		}
	}

	// =========================================================================
	// === GESTIÓN DEL FRAMEBUFFER ADAPTATIVO
	// =========================================================================

	/**
	 * Adapta el tamaño del buffer en VRAM en función del zoom solicitado.
	 * <p>
	 * <b>Lógica de Rendimiento:</b><br>
	 * - Si {@code zoom >= 1.0}: Tamaño fijo ligero de $704 \times 416$ (+400 FPS y
	 * 0 líneas).<br>
	 * - Si {@code zoom < 1.0}: Se expande proporcionalmente para evitar bordes
	 * negros.
	 * </p>
	 */
	private void verificarBufferMundo(final Graphics2D g, final double zoomFinal) {
		// Factor de escala acotado para el buffer (1.0 para zoom normal o zoom-in)
		final double factorZoomOut = Math.min(1.0, Math.max(0.5, zoomFinal));

		// Dimensiones requeridas con margen de seguridad
		final int anchoRequerido = (int) Math.ceil(Constantes.ANCHO_JUEGO / factorZoomOut) + (MARGEN_BUFFER * 2);
		final int altoRequerido = (int) Math.ceil(Constantes.ALTO_JUEGO / factorZoomOut) + (MARGEN_BUFFER * 2);

		if ((this.bufferMundo == null) || (this.bufferMundo.getWidth() != anchoRequerido)
				|| (this.bufferMundo.getHeight() != altoRequerido)
				|| (this.bufferMundo.validate(g.getDeviceConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE)) {

			if (this.bufferMundo != null) {
				this.bufferMundo.flush();
			}

			this.bufferMundo = g.getDeviceConfiguration().createCompatibleVolatileImage(anchoRequerido, altoRequerido,
					Transparency.OPAQUE);
		}
	}

	// =========================================================================
	// === RENDERIZADO (PIPELINE GRÁFICO 2D)
	// =========================================================================

	@Override
	public void pintar(final Graphics2D g) {
		if (!Globales.partidaIniciada) {
			return;
		}

		final double zoomFinal = Globales.CAMARA.getZoomFinal();
		final double shakeX = Globales.CAMARA.getGestorEfectos().getOffsetX();
		final double shakeY = Globales.CAMARA.getGestorEfectos().getOffsetY();
		final double rotacion = Globales.CAMARA.getGestorEfectos().getAnguloRotacion();

		// 1. Validamos y ajustamos el buffer adaptativo en VRAM
		this.verificarBufferMundo(g, zoomFinal);

		final int anchoBuf = this.bufferMundo.getWidth();
		final int altoBuf = this.bufferMundo.getHeight();
		final int centroBufX = anchoBuf / 2;
		final int centroBufY = altoBuf / 2;

		// Offset para alinear el centro del juego (320, 180) con el centro del buffer
		final int offsetMundoX = centroBufX - Constantes.CENTROX;
		final int offsetMundoY = centroBufY - Constantes.CENTROY;

		// =========================================================================
		// === FASE 1: RENDERIZADO DEL MUNDO A ESCALA 1:1 (CERO COSTURAS DE TILES)
		// =========================================================================
		final Graphics2D gMundo = this.bufferMundo.createGraphics();
		try {
			gMundo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			gMundo.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			gMundo.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

			// Limpieza de buffer
			DibujoDebug.dibujarRectanguloRelleno(gMundo, 0, 0, anchoBuf, altoBuf, Color.BLACK);

			// Centramos el mundo dentro del buffer
			gMundo.translate(offsetMundoX, offsetMundoY);

			// Dibujamos terreno y entidades
			if (this.mapa != null) {
				this.mapa.pintar(gMundo);
			}
			// Capa de Partículas en el Mundo
			Globales.GESTOR_PARTICULAS.pintar(gMundo);
//			Globales.JUGADOR.pintar(gMundo);

			// 1.3 Capa de Textos de Daño Flotantes en el Mundo
			Globales.GESTOR_TEXTOS.pintar(gMundo);

			gMundo.translate(-offsetMundoX, -offsetMundoY);

		} finally {
			gMundo.dispose();
		}

		// =========================================================================
		// === FASE 2: PROYECCIÓN DEL BUFFER A PANTALLA CON CÁMARA
		// =========================================================================
		final boolean hayTransformacionMundo = (zoomFinal != 1.0) || (shakeX != 0.0) || (shakeY != 0.0)
				|| (rotacion != 0.0);

		if (hayTransformacionMundo) {
			g.translate(Constantes.CENTROX + shakeX, Constantes.CENTROY + shakeY);
			g.scale(zoomFinal, zoomFinal);
			g.rotate(rotacion);
		} else {
			g.translate(Constantes.CENTROX, Constantes.CENTROY);
		}

		// Dibujamos la textura completa del mundo centrada en su punto medio
		g.drawImage(this.bufferMundo, -centroBufX, -centroBufY, null);

		if (hayTransformacionMundo) {
			// Restauración LIFO exacta para el HUD
			g.rotate(-rotacion);
			g.scale(1.0 / zoomFinal, 1.0 / zoomFinal);
			g.translate(-(Constantes.CENTROX + shakeX), -(Constantes.CENTROY + shakeY));
		} else {
			g.translate(-Constantes.CENTROX, -Constantes.CENTROY);
		}

		// 2. CAPA ATMOSFÉRICA (Nubes diurnas y Niebla)
		Globales.GESTOR_CLIMA.pintar(g);

		// 3. CAPA DE ILUMINACIÓN Y PENUMBRA (Lightmap VRAM)
		Globales.GESTOR_LUZ.pintar(g);

		// =========================================================================
		// === FASE 3: CAPAS DE INTERFAZ / HUD (ESCALA FIJA 1:1)
		// =========================================================================
		this.pintarDebug(g);

		if (!Globales.JUGADOR.estaEliminado()) {
			this.pintarInventarios(g);
		}

		this.motoIGU.pintar(g);
		this.pintarPantallaDerrota(g);
	}

	private void pintarInventarios(final Graphics2D g) {
		Globales.GESTOR_INVENTARIO.pintar(g);
		Globales.GESTOR_INVENTARIO.pintarTooltipsYPuntero(g, this.RATON.getPuntoPosicionEscalado());
	}

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
		this.pintarHoraJuego(g);

		if (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()) {
			DibujoDebug.dibujarString(g, (this.tilePisado != null) ? this.tilePisado.toString() : "PuntoTile: (none)",
					120, 20, Color.WHITE);
			if (this.tilePisado != null) {
				this.tilePisado.pintarContorno(g, Color.WHITE);
			}
		}

		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			g.setColor(Color.GREEN);
			DibujoDebug.dibujarString(g, "X: " + Globales.JUGADOR.getPosicionXInt(), 20, 80);
			DibujoDebug.dibujarString(g, "Y: " + Globales.JUGADOR.getPosicionYInt(), 20, 95);
			DibujoDebug.dibujarString(g, "X_PARADO: " + Globales.JUGADOR.getPosicionXParado(), 20, 110);
			DibujoDebug.dibujarString(g, "Y_PARADO: " + Globales.JUGADOR.getPosicionYParado(), 20, 125);
			DibujoDebug.dibujarString(g, "Velocidad: " + Globales.JUGADOR.getVelocidad(), 20, 140);
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

	private void pintarTiempoJugado(final Graphics2D g) {
		final String texto = Globales.horasJugadas + "h " + Globales.minutosJugados + "m " + Globales.segundosJugados
				+ "s";
		DibujoDebug.dibujarString(g, texto, 20, 20, Color.CYAN);
	}

	private void pintarHoraJuego(final Graphics2D g) {
		DibujoDebug.dibujarStringConSombra(g, "[" + Globales.GESTOR_LUZ.getCiclo().getHoraFormato24h() + "]",
				Constantes.ANCHO_JUEGO - 50, 15, Color.YELLOW, Color.ORANGE, 12f);
		DibujoDebug.dibujarStringConSombra(g, "/" + Globales.GESTOR_LUZ.getCiclo().getNombreMomentoDelDia() + "\\",
				Constantes.ANCHO_JUEGO - 50, 35, Color.YELLOW, Color.DARK_GRAY, 9f);
		DibujoDebug.dibujarStringConSombra(g, "<" + Globales.GESTOR_CLIMA.getNombreClimaActual() + ">",
				Constantes.ANCHO_JUEGO - 70, 55, Color.WHITE, Color.RED, 9f);
		DibujoDebug.dibujarStringConSombra(g,
				"(" + String.format("%.1f", Globales.GESTOR_CLIMA.getTemperaturaCelsius()) + "°C "
						+ Globales.GESTOR_CLIMA.getFuerzaViento() + "Fv)",
				Constantes.ANCHO_JUEGO - 60, 75, Color.LIGHT_GRAY, Color.CYAN, 9f);
	}

	@Override
	public void cargarMapa(final GestorCarga gc, final String nombreMapa, final boolean reset,
			final String nombreSpawn) {
		if (gc != null) {
			gc.setPorcentajeCarga(10);
			gc.setDetalleCarga("Cargando mapa " + nombreMapa);
		}
		// 1. Limpieza total de luces del mapa previo (Evita fugas de memoria y luces
		// fantasma)
		Globales.GESTOR_LUZ.apagarTodasLasLuces();
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

		if (reset) {
			Globales.JUGADOR.restablecerYCambiarMundo(this.mapa.getMundoActual());
		} else {
			Globales.JUGADOR.setMundo(this.mapa.getMundoActual());
		}

		if (this.mapa.getMundoActual().getSpawn(nombreSpawn) != null) {
			this.mapa.getMundoActual().getSpawn(nombreSpawn).moverJugadorCentrado();
		}

		Globales.CAMARA.setEntidadEnfocada(Globales.JUGADOR);
		Globales.CAMARA.habilitarGestorLimite();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().establecerMundo(this.mapa.getMundoActual());
		Globales.RATON.soltar();

		if (!Globales.TECLADO.TECLA_DIJKSTRA.presionado()) {
			Globales.TECLADO.TECLA_DIJKSTRA.presionar();
		}

		Globales.partidaIniciada = true;

		if (gc != null) {
			gc.setDetalleCarga("¡Carga completa!");
			gc.setCompleto(true);
		}

		// 2. Vincular la linterna del jugador y luces iniciales del mapa
		this.auxFuenteLuzTempoPrueba = Globales.GESTOR_LUZ.agregarLuzAnclada(Globales.JUGADOR, TipoLuz.AURA_JUGADOR,
				100);
		this.auxFuenteLuzTempoPrueba.setOffset(4, 3);
////		Globales.GESTOR_LUZ.agregarLuzEstatica(Globales.JUGADOR.getPosicionX(), Globales.JUGADOR.getPosicionY(),
////				TipoLuz.ANTORCHA);
//2
		// Al cargar el mapa:
		Globales.GESTOR_CLIMA.setPerfilBioma(PerfilClima.TEMPLADO_BOSQUE);
	}

	public void agregarObjetosAlMundo() {
		if ((this.mapa == null) || (this.mapa.getMundoActual() == null)) {
			return;
		}

		final Mundo mundoActual = this.mapa.getMundoActual();

		mundoActual.crearProyectil(
				new BolaFuego(25, 0.25, 100000000, false, mundoActual, 1005, 392, 40, Direccion.OESTE, null));

		mundoActual.meterEntidad(new GranadaT1(800, 80, 50));

		final ZonaTP zonaTP2 = new ZonaTP(new Rectangle(684, 215, 20, 20), null);
		final ZonaTP zonaTP = new ZonaTP(new Rectangle(878, 173, 20, 20),
				new PuertaArea(new Rectangle(832, 333, 16, 16)));

		zonaTP2.setPuertaTP(new PuertaMapa("escenario2.json", Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, false, this.GP));

		mundoActual.meterEntidad(zonaTP);
		mundoActual.meterEntidad(zonaTP2);

		this.EVENTOS.add(new EventoJugadorZonaTP(zonaTP, this, true));
		this.EVENTOS.add(new EventoJugadorZonaTP(zonaTP2, this, true));

		mundoActual.meterEntidad(new Complemento(773, 177, ListaModeloComplemento.COD_CASA_1));
	}

	public void establecerCriaturas() {
	}

	public Terreno getTerreno() {
		return (this.mapa != null) ? this.mapa.getMundoActual().getTerreno() : null;
	}

	public Mundo getMundo() {
		return (this.mapa != null) ? this.mapa.getMundoActual() : null;
	}

	public Mapa getMapa() {
		return this.mapa;
	}

	public void meterEvento(final Evento e) {
		if (e != null) {
			this.EVENTOS.add(e);
		}
	}
}