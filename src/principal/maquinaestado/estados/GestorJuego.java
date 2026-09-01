package principal.maquinaestado.estados;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.controles.Raton;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.facciones.GestorFacciones;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.proyectil.explosivo.BolaFuego;
import principal.eventos.Evento;
import principal.eventos.EventoJugadorZonaTP;
import principal.igu.MotorIGU;
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
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.musica.IDMusica;

/**
 * Estado principal de jugabilidad activa (Gameplay Loop).
 * 
 * @author Copiloto Técnico
 * @version 7.0
 */
public final class GestorJuego implements EstadoJuego, cargaMapa {

	private static final int MARGEN_BUFFER = 32;
	private VolatileImage bufferMundo;

	protected final GestorEstados GE;
	protected final GestorPartida GP;
	private final Raton RATON = Globales.RATON;
	private final MotorIGU motoIGU;
	protected Mapa mapa;
	protected final ArrayList<Evento> EVENTOS = new ArrayList<Evento>();

	protected final GestorTiempo GT_MOSTRAR_PANTALLA_MUERTE;
	protected final int TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE = 1500;
	private boolean mostrarPantallaMuerte;
	private Tile tilePisado = null;
	private FuenteLuz auxFuenteLuzTempoPrueba;

	// =========================================================================
	// === CACHÉ DE TEXTOS HUD (ZERO-GC: SOLO SE RECONSTRUYEN AL CAMBIAR VALOR)
	// =========================================================================
	private int lastSegundosJugados = -1;
	private String cachedTextoTiempoJugado = "0h 0m 0s";

	private int lastMinutoHUD = -1;
	private int lastDiaHUD = -1;
	private String cachedLineaReloj = "[Día 1 - 12:00]";

	private String lastMomentoHUD = "";
	private String cachedLineaMomento = "/ Mediodía \\";

	private TipoClima lastClimaHUD = null;
	private String cachedLineaClima = "< Despejado >";

	private int lastTempIntHUD = -999;
	private double lastFuerzaVientoHUD = -1.0;
	private String cachedLineaTermica = "(20.0°C | 1.0 Fv)";

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

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		this.actualizarControlesDebug();

		Globales.JUGADOR.actualizar();

		if (!Globales.JUGADOR.estaEliminado()) {
			Globales.GESTOR_INVENTARIO.actualizar(this.RATON, this.mapa.getMundoActual());
			this.mapa.actualizar();
			this.actualizarEventos();
		}

		if (Globales.TECLADO.TECLA_DIJKSTRA.presionadoUnicaActualizacion()) {
			if (Globales.JUGADOR.getFaccionBit() == GestorFacciones.FACCION_JUGADOR) {
				Globales.JUGADOR.setFaccion(GestorFacciones.FACCION_BANDIDOS);
			} else {
				Globales.JUGADOR.setFaccion(GestorFacciones.FACCION_JUGADOR);
			}
		}

		this.verificarPantallaMuerte();
		this.motoIGU.actualizar();

		final Shape areaMovimiento = Globales.JUGADOR.getAreaInterseccionMovimiento();
		if ((areaMovimiento != null) && (this.mapa != null) && (this.mapa.getMundoActual() != null)) {
			final Rectangle bounds = areaMovimiento.getBounds();
			final int pieX = bounds.x + (bounds.width / 2);
			final int pieY = bounds.y + bounds.height;

			this.tilePisado = this.mapa.getMundoActual().getTerreno().getTileReferenciado(pieX, pieY);
		}

		Globales.GESTOR_TEXTOS.actualizar();
		Globales.GESTOR_PARTICULAS.actualizar();
		Globales.GESTOR_ZONAS_AMBIENTE.actualizar(dt);
		Globales.GESTOR_CLIMA.actualizar();
		Globales.GESTOR_LUZ.actualizar();

		if (this.auxFuenteLuzTempoPrueba != null) {
			this.auxFuenteLuzTempoPrueba.orientarSegunDireccion(Globales.JUGADOR.getDireccion());
		}
	}

	private void actualizarControlesDebug() {
		this.actualizarCambioCamaraConEntesYZoom();

		if (Globales.TECLADO.TECLA_NUM_1.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.getCiclo().setHora(7.5);
		}
		if (Globales.TECLADO.TECLA_NUM_2.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.getCiclo().setHora(17.5);
		}
		if (Globales.TECLADO.TECLA_NUM_3.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.agregarLuzEstatica(Globales.JUGADOR.getPosicionX(), Globales.JUGADOR.getPosicionY(),
					TipoLuz.FOGATA, 140);
		}
		if (Globales.TECLADO.TECLA_NUM_4.presionadoUnicaActualizacion()) {
			Globales.CAMARA.getGestorEfectos().reproducirEfectoTemporal(TipoEfectoCamara.BARCO_NAVEGACION, 10000, 1);
		}
		if (Globales.TECLADO.TECLA_NUM_5.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.getCiclo().irANoche();
			Globales.GESTOR_CLIMA.setClima(TipoClima.AURORA_BOREAL);
		}
		if (Globales.TECLADO.TECLA_NUM_6.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.getCiclo().irAMediodia();
			Globales.GESTOR_CLIMA.setClima(TipoClima.ECLIPSE_SOLAR);
		}
		if (Globales.TECLADO.TECLA_NUM_7.presionadoUnicaActualizacion()) {
			Globales.GESTOR_LUZ.getCiclo().irANoche();
			Globales.GESTOR_CLIMA.setClima(TipoClima.LLUVIA_ESTRELLAS);
		}
		if (Globales.TECLADO.TECLA_NUM_8.presionadoUnicaActualizacion()) {
			Globales.GESTOR_CLIMA.setClima(TipoClima.LLUVIA_TORMENTA);
		}
		if (Globales.TECLADO.TECLA_NUM_9.presionadoUnicaActualizacion()) {
			Globales.CAMARA.activarModoCinematico(!Globales.CAMARA.isModoCinematico());
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

	private void verificarBufferMundo(final Graphics2D g, final double zoomFinal) {
		final double factorZoomOut = Math.min(1.0, Math.max(0.5, zoomFinal));

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

		this.verificarBufferMundo(g, zoomFinal);

		final int anchoBuf = this.bufferMundo.getWidth();
		final int altoBuf = this.bufferMundo.getHeight();
		final int centroBufX = anchoBuf / 2;
		final int centroBufY = altoBuf / 2;

		final int offsetMundoX = centroBufX - Constantes.CENTROX;
		final int offsetMundoY = centroBufY - Constantes.CENTROY;

		// =====================================================================
		// FASE 1: RENDERIZADO DEL MUNDO A ESCALA 1:1
		// =====================================================================
		final Graphics2D gMundo = this.bufferMundo.createGraphics();
		try {
			gMundo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			gMundo.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			gMundo.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

			Render2D.dibujarRectanguloRelleno(gMundo, 0, 0, anchoBuf, altoBuf, Color.BLACK);

			gMundo.translate(offsetMundoX, offsetMundoY);

			if (this.mapa != null) {
				this.mapa.pintar(gMundo);
			}

			Globales.GESTOR_PARTICULAS.pintar(gMundo);
			Globales.GESTOR_TEXTOS.pintar(gMundo);

			gMundo.translate(-offsetMundoX, -offsetMundoY);

		} finally {
			gMundo.dispose();
		}

		// =====================================================================
		// FASE 2: PROYECCIÓN DEL BUFFER A PANTALLA CON CÁMARA
		// =====================================================================
		final AffineTransform transformOriginal = g.getTransform();
		try {
			final boolean hayTransformacionMundo = (zoomFinal != 1.0) || (shakeX != 0.0) || (shakeY != 0.0)
					|| (rotacion != 0.0);

			if (hayTransformacionMundo) {
				g.translate(Constantes.CENTROX + shakeX, Constantes.CENTROY + shakeY);
				g.scale(zoomFinal, zoomFinal);
				g.rotate(rotacion);
			} else {
				g.translate(Constantes.CENTROX, Constantes.CENTROY);
			}

			g.drawImage(this.bufferMundo, -centroBufX, -centroBufY, null);

		} finally {
			g.setTransform(transformOriginal);
		}

		// 2. Capa Atmosférica (Nubes, Niebla, Aurora, Lluvia en el aire)
		Globales.GESTOR_CLIMA.pintar(g);

		// 3. Capa de Iluminación y Penumbra (Lightmap + God Rays)
		Globales.GESTOR_LUZ.pintar(g);

		// =====================================================================
		// FASE 3: CAPAS DE INTERFAZ / HUD / LETTERBOX (1:1)
		// =====================================================================

		if (!Globales.JUGADOR.estaEliminado()) {
			this.pintarInventarios(g);
		}

		this.motoIGU.pintar(g);
		this.pintarPantallaDerrota(g);

		Globales.CAMARA.pintarLetterbox(g);
		this.pintarDebug(g);
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

			Render2D.dibujarString(g, texto, x, y, color);
			g.setFont(fuenteOriginal);
		}
	}

	private void pintarDebug(final Graphics2D g) {
		if (Globales.pausa) {
			Render2D.dibujarString(g, "PAUSA", 10, 10, Color.RED);
		}

		this.pintarTiempoJugado(g);
		this.pintarHoraJuego(g);

		if (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()) {
			Render2D.dibujarString(g, (this.tilePisado != null) ? this.tilePisado.toString() : "PuntoTile: (none)", 120,
					20, Color.WHITE);
			if (this.tilePisado != null) {
				this.tilePisado.pintarContorno(g, Color.WHITE);
			}
		}

		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			g.setColor(Color.GREEN);
			Render2D.dibujarString(g, "X: " + Globales.JUGADOR.getPosicionXInt(), 20, 80);
			Render2D.dibujarString(g, "Y: " + Globales.JUGADOR.getPosicionYInt(), 20, 95);
			Render2D.dibujarString(g, "X_PARADO: " + Globales.JUGADOR.getPosicionXParado(), 20, 110);
			Render2D.dibujarString(g, "Y_PARADO: " + Globales.JUGADOR.getPosicionYParado(), 20, 125);
			Render2D.dibujarString(g, "Velocidad: " + Globales.JUGADOR.getVelocidad(), 20, 140);
			Render2D.dibujarString(g,
					"Dijkstra(F2): " + (Globales.TECLADO.TECLA_DIJKSTRA.presionado() ? "Activo" : "Inactivo"), 20, 155);
			Render2D.dibujarString(g,
					"DijkstraInfo(F6): " + (Globales.TECLADO.TECLA_DIJKSTRA_INFO.presionado() ? "Activo" : "Inactivo"),
					20, 170);
			Render2D.dibujarString(g, "DebugGroupTile(F4): "
					+ (Globales.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado() ? "Activo" : "Inactivo"), 20, 185);
			Render2D.dibujarString(g,
					"DebugTile(F3): " + (Globales.TECLADO.TECLA_DEBUG_TILE.presionado() ? "Activo" : "Inactivo"), 20,
					200);
			Render2D.dibujarString(g, "DebugTileInfo(F5): "
					+ (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado() ? "Activo" : "Inactivo"), 20, 215);
			Render2D.dibujarString(g, "VerColisiones(F7): "
					+ (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() ? "Activo" : "Inactivo"), 20, 230);
			Render2D.dibujarString(g, "OcultarTerreno(F8): "
					+ (Globales.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? "Activo" : "Inactivo"), 20, 245);
			Render2D.dibujarString(g,
					"OcultarComplementos(F9): "
							+ (Globales.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado() ? "Activo" : "Inactivo"),
					20, 260);
			Render2D.dibujarString(g,
					"VerAlcanceAtaque(F10): "
							+ (Globales.TECLADO.TECLA_VER_ALCANCE_ATAQUE.presionado() ? "Activo" : "Inactivo"),
					20, 275);
			Render2D.dibujarString(g, "Direccion: " + Globales.JUGADOR.getDireccion().toString(), 20, 290);
			Render2D.dibujarString(g, "Estados: " + Globales.JUGADOR.getStringEstados(), 20, 305);
			Render2D.dibujarString(g,
					"FPS Limitado(F11): " + (Globales.TECLADO.TECLA_FPS_LIMITE.presionado() ? "Activo" : "Inactivo"),
					20, 320);
		}
	}

	// =========================================================================
	// === HUD: CRONÓMETRO DE SESIÓN (TOP-LEFT)
	// =========================================================================

	private void pintarTiempoJugado(final Graphics2D g) {
		if (Globales.segundosJugados != this.lastSegundosJugados) {
			this.lastSegundosJugados = Globales.segundosJugados;
			this.cachedTextoTiempoJugado = Globales.horasJugadas + "h " + Globales.minutosJugados + "m "
					+ Globales.segundosJugados + "s";
		}
		Render2D.dibujarStringConSombra(g, this.cachedTextoTiempoJugado, 15, 20, Color.CYAN, Color.BLACK, 10f);
	}

	// =========================================================================
	// === HUD: WIDGET ATMOSFÉRICO Y CRONOLÓGICO (TOP-RIGHT)
	// =========================================================================

	private void pintarHoraJuego(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		final int margenDerecho = 12;

		// --- 1. LÍNEA 1: [DÍA X - HH:MM] (Fuente 11f / Amarillo Dorado) ---
		final int minutoActual = (int) Math.round(Globales.GESTOR_LUZ.getCiclo().getHoraActual() * 60.0);
		final int diaActual = Globales.GESTOR_LUZ.getCiclo().getDiaActual();

		if ((minutoActual != this.lastMinutoHUD) || (diaActual != this.lastDiaHUD)) {
			this.lastMinutoHUD = minutoActual;
			this.lastDiaHUD = diaActual;
			this.cachedLineaReloj = "[" + Globales.GESTOR_LUZ.getCiclo().getTextoDia() + " - "
					+ Globales.GESTOR_LUZ.getCiclo().getHoraFormato24h() + "]";
		}

		g.setFont(fuenteOriginal.deriveFont(11f));
		final int anchoL1 = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedLineaReloj);
		final int xL1 = Constantes.ANCHO_JUEGO - anchoL1 - margenDerecho;
		Render2D.dibujarStringConSombra(g, this.cachedLineaReloj, xL1, 16, Color.YELLOW, Color.ORANGE, 11f);

		// --- 2. LÍNEA 2: / MOMENTO DEL DÍA \ (Fuente 9f / Dorado Suave) ---
		final String momentoActual = Globales.GESTOR_LUZ.getCiclo().getNombreMomentoDelDia();
		if (!momentoActual.equals(this.lastMomentoHUD)) {
			this.lastMomentoHUD = momentoActual;
			this.cachedLineaMomento = "/ " + momentoActual + " \\";
		}

		g.setFont(fuenteOriginal.deriveFont(9f));
		final int anchoL2 = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedLineaMomento);
		final int xL2 = Constantes.ANCHO_JUEGO - anchoL2 - margenDerecho;
		Render2D.dibujarStringConSombra(g, this.cachedLineaMomento, xL2, 30, new Color(255, 215, 120), Color.DARK_GRAY,
				9f);

		// --- 3. LÍNEA 3: < CLIMA ACTIVO > (Fuente 9f / Color Reactivo) ---
		final TipoClima climaActual = Globales.GESTOR_CLIMA.getClimaActual();
		if (climaActual != this.lastClimaHUD) {
			this.lastClimaHUD = climaActual;
			this.cachedLineaClima = "< " + Globales.GESTOR_CLIMA.getNombreClimaActual() + " >";
		}

		final Color colorClimaTexto = this.obtenerColorTextoClima(climaActual);
		final int anchoL3 = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedLineaClima);
		final int xL3 = Constantes.ANCHO_JUEGO - anchoL3 - margenDerecho;
		Render2D.dibujarStringConSombra(g, this.cachedLineaClima, xL3, 44, colorClimaTexto, Color.BLACK, 9f);

		// --- 4. LÍNEA 4: (TEMP °C | VIENTO Fv) (Fuente 9f / Gris Plateado) ---
		final double temp = Globales.GESTOR_CLIMA.getTemperaturaCelsius();
		final double viento = Globales.GESTOR_CLIMA.getFuerzaViento();
		final int tempInt = (int) Math.round(temp * 10.0);

		if ((tempInt != this.lastTempIntHUD) || (viento != this.lastFuerzaVientoHUD)) {
			this.lastTempIntHUD = tempInt;
			this.lastFuerzaVientoHUD = viento;
			final double tempDecimal = tempInt / 10.0;
			this.cachedLineaTermica = "(" + tempDecimal + "°C | Viento: " + viento + "Fv)";
		}

		final int anchoL4 = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedLineaTermica);
		final int xL4 = Constantes.ANCHO_JUEGO - anchoL4 - margenDerecho;
		Render2D.dibujarStringConSombra(g, this.cachedLineaTermica, xL4, 58, Color.LIGHT_GRAY, Color.BLACK, 9f);

		g.setFont(fuenteOriginal);
	}

	/**
	 * Retorna una paleta cromática reactiva para el nombre del clima.
	 */
	private Color obtenerColorTextoClima(final TipoClima clima) {
		if (clima == null) {
			return Color.WHITE;
		}
		switch (clima) {
		case LLUVIA_TORMENTA:
		case LLUVIA_LEVE:
			return new Color(130, 200, 255); // Azul agua
		case LLUVIA_ACIDA:
			return new Color(150, 255, 110); // Verde tóxico
		case AURORA_BOREAL:
			return new Color(80, 255, 210); // Verde esmeralda místico
		case ECLIPSE_SOLAR:
			return new Color(255, 75, 75); // Rojo carmesí
		case LLUVIA_ESTRELLAS:
			return new Color(255, 235, 130); // Oro estelar
		case NIEVE:
		case VENTISCA:
			return new Color(220, 240, 255); // Blanco hielo
		case TORMENTA_ARENA:
			return new Color(245, 185, 100); // Ámbar desértico
		case CENIZA_VOLCANICA:
			return new Color(255, 130, 60); // Naranja brasa
		case PETALOS_CEREZO:
			return new Color(255, 185, 215); // Rosa flor
		default:
			return Color.WHITE;
		}
	}

	@Override
	public void cargarMapa(final GestorCarga gc, final String nombreMapa, final boolean reset,
			final String nombreSpawn) {
		if (gc != null) {
			gc.setPorcentajeCarga(10);
			gc.setDetalleCarga("Cargando mapa " + nombreMapa);
		}

		Globales.GESTOR_LUZ.apagarTodasLasLuces();
		Globales.GESTOR_PARTICULAS.limpiar();
		Globales.GESTOR_ZONAS_AMBIENTE.limpiarZonas();

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

		this.auxFuenteLuzTempoPrueba = Globales.GESTOR_LUZ.agregarLuzAnclada(Globales.JUGADOR, TipoLuz.AURA_JUGADOR,
				75);
		this.auxFuenteLuzTempoPrueba.setOffset(4, 3);

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