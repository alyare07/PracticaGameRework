package principal.maquinaestado.estados;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.configuracion.Dificultad;
import principal.controles.Raton;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.proyectil.explosivo.BolaFuego;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.ZonaTP;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.maquinaestado.estados.pantallaCarga.cargaMapa;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.musica.GestorMusica;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public final class GestorJuego implements EstadoJuego, cargaMapa {

	private static final int MARGEN_BUFFER = 32;
	private VolatileImage bufferMundo;

	protected final GestorEstados GE;
	protected final GestorPartida GP;
	private final Raton RATON = Globales.RATON;
	protected Mapa mapa;

	protected final GestorTiempo GT_MOSTRAR_PANTALLA_MUERTE;
	protected final int TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE = 800;
	private boolean mostrarPantallaMuerte;
	private BotonPixel botonMuerte;

	private Tile tilePisado = null;

	private int lastSegundosJugados = -1;
	private String cachedTextoTiempoJugado = "0h 0m 0s";
	// --- Caché de Matriz de Pantalla Zero-GC (Anti-Jitter) ---
	private AffineTransform transformPantallaBase = null;
	private int lastDespX = Integer.MIN_VALUE;
	private int lastDespY = Integer.MIN_VALUE;
	private double lastFactorEscalaX = -1.0;
	private double lastFactorEscalaY = -1.0;

	public GestorJuego(final GestorEstados ge, final GestorPartida gp) {
		this.GE = ge;
		this.GP = gp;
		this.GT_MOSTRAR_PANTALLA_MUERTE = new GestorTiempo();
	}

	@Override
	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		// 0. Transición Cinemática de la Singularidad Óptica (Dormir)
		GestorTransicionSueno.getInstancia().actualizar(dt);
		if (GestorTransicionSueno.getInstancia().isActivo()) {
			Globales.CAMARA.actualizar();
			return; // Bloquea la locomoción, armas y combate mientras duerme
		}
		// 1. Control Modal del Taller de Crafteo y Cocina
		if (principal.crafteo.MenuCrafteo.getInstancia().isAbierto()) {
			// El mundo sigue vivo en tiempo real de fondo (simulación continua)
			Globales.GESTOR_ASTRONOMICO.actualizar(Globales.delta);
			Globales.GESTOR_CLIMA.actualizar();
			Globales.GESTOR_LUZ.actualizar();
			Globales.GESTOR_TERMICO_JUGADOR.actualizar(Globales.delta);
			Globales.GESTOR_METABOLISMO.actualizar(Globales.delta);
			this.mapa.actualizar();

			// Menú toma el control del ratón y teclado
			principal.crafteo.MenuCrafteo.getInstancia().actualizar(Globales.RATON);

			// Válvula de seguridad: Si el jugador recibe daño mientras forja, cierra el
			// menú
			if (Globales.JUGADOR.estaEnFlashDanio()) {
				principal.crafteo.MenuCrafteo.getInstancia().cerrar();
			}
			return; // Salta el movimiento y disparo del jugador mientras craftea
		}
		if (this.detectarCambioAMenu()) {
			return;
		}

		if (!Globales.partidaIniciada) {
			return;
		}

		// Si el jugador murió, se procesa la pantalla y el botón de muerte/reaparición
		if (Globales.JUGADOR.estaEliminado()) {
			this.verificarPantallaMuerte();
			if (this.mostrarPantallaMuerte && (this.botonMuerte != null)) {
				this.botonMuerte.actualizar(this.RATON);
				if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
						|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)
						|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)) {
					this.botonMuerte.accionar();
				}
			}
			return;
		}

		if (Globales.pausa) {
			GestorMusica.actualizarMusicaFondoPrincipal(false);
			GestorMusica.actualizarAmbienteClima(false);
			return;
		}

		this.actualizarControlesDebug();
		GestorMusica.actualizarMusicaFondoPrincipal(true);

		Globales.GESTOR_INVENTARIO.actualizar(this.RATON, this.mapa.getMundoActual());
		this.mapa.actualizar();

		Globales.JUGADOR.actualizar();

		if (Globales.TECLADO.TECLA_CONSTRUCCION.presionadoUnicaActualizacion()) {
			Globales.GESTOR_CONSTRUCCION.conmutarModoConstruccion();
		}

		if (Globales.GESTOR_CONSTRUCCION.isActivo() && (this.mapa != null) && (this.mapa.getMundoActual() != null)) {
			Globales.GESTOR_CONSTRUCCION.actualizar(this.RATON, this.mapa.getMundoActual());
		}

		Globales.MOTOR_IGU.actualizar();

		final Rectangle areaMovimiento = Globales.JUGADOR.getAreaInterseccionMovimiento();
		if ((areaMovimiento != null) && (this.mapa != null) && (this.mapa.getMundoActual() != null)) {
			final int pieX = areaMovimiento.x + (areaMovimiento.width / 2);
			final int pieY = areaMovimiento.y + areaMovimiento.height;

			this.tilePisado = this.mapa.getMundoActual().getTerreno().getTileReferenciado(pieX, pieY);
		}

		Globales.GESTOR_TEXTOS.actualizar();
		Globales.GESTOR_PARTICULAS.actualizar();
		Globales.GESTOR_ZONAS_AMBIENTE.actualizar(dt);
		if (Globales.GESTOR_ASTRONOMICO != null) {
			Globales.GESTOR_ASTRONOMICO.actualizar(dt);
		}
		Globales.GESTOR_CLIMA.actualizar();
		Globales.GESTOR_LUZ.actualizar();
		Globales.GESTOR_TERMICO_JUGADOR.actualizar(dt);
		Globales.GESTOR_METABOLISMO.actualizar(dt);
		Globales.GESTOR_CRAFTEO.actualizar(this.mapa.getMundoActual());
		this.actualizarEventos(dt);

	}

	private void actualizarControlesDebug() {
		this.actualizarCambioCamaraConEntesYZoom();
		if (Globales.RATON.presionadoClickIzqUnicaAct() && Globales.TECLADO.TECLA_DEBUG.presionado()) {
			final Rectangle puntoR = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			Globales.JUGADOR.setPosicion(puntoR.getX(), puntoR.getY());
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

	}

	private void actualizarEventos(final double dt) {
		Globales.GESTOR_INTERACCION.actualizar(this.mapa.getMundoActual());
		Globales.GESTOR_DIALOGOS.actualizar(dt);
		Globales.GESTOR_EVENTOS.actualizar(dt);
	}

	private boolean detectarCambioAMenu() {
		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			GestorMusica.actualizarMusicaFondoPrincipal(false);

			if (Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
				Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
				return false;
			}

			Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();
			this.GP.establecerEstadoActivoMenu();
			return true;
		}
		return false;
	}

	// =========================================================================
	// GESTIÓN DE PANTALLA Y BOTÓN DE MUERTE POR DIFICULTAD
	// =========================================================================

	protected void verificarPantallaMuerte() {
		if (Globales.JUGADOR.estaEliminado() && !this.mostrarPantallaMuerte) {
			this.mostrarPantallaMuerte = true;
			this.GT_MOSTRAR_PANTALLA_MUERTE.establecerReferenciaTiempoActual();
			Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();

			final int btnW = 140;
			final int btnH = 22;
			final int btnX = Constantes.CENTROX - (btnW / 2);
			final int btnY = Constantes.ALTO_JUEGO - 65;

			if (Globales.dificultad == Dificultad.HARDCORE_REAL) {
				this.botonMuerte = new BotonPixel("Fin del Juego", new Rectangle(btnX, btnY, btnW, btnH), () -> {
					this.ejecutarFinDelJuego();
				});
			} else if (Globales.dificultad == Dificultad.HARDCORE_RENACIMIENTO) {
				this.botonMuerte = new BotonPixel("Reencarnar", new Rectangle(btnX, btnY, btnW, btnH), () -> {

					this.ejecutarReaparicion();
				});
			} else {
				this.botonMuerte = new BotonPixel("Reaparecer", new Rectangle(btnX, btnY, btnW, btnH), () -> {
					this.ejecutarReaparicion();
				});
			}
			this.botonMuerte.setEnfocado(true);
		}
	}

	private void ejecutarReaparicion() {
		this.mostrarPantallaMuerte = false;
		this.botonMuerte = null;

		final Mundo mundoActual = (this.mapa != null) ? this.mapa.getMundoActual() : null;
		if (mundoActual != null) {
			Globales.JUGADOR.reaparecer(mundoActual);
		}

		Globales.CAMARA.setEntidadEnfocada(Globales.JUGADOR);
		Globales.CAMARA.habilitarGestorLimite();
		Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();
		GestorSonido.reproducir(IDSonido.SELECT);
	}

	private void ejecutarFinDelJuego() {
		this.mostrarPantallaMuerte = false;
		this.botonMuerte = null;

		this.GE.disposePartida();
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

	@Override
	public void pintar(final Graphics2D g) {
		// Si el taller está abierto, tapa la pantalla completa (ahorro masivo de GPU)
		if (principal.crafteo.MenuCrafteo.getInstancia().isAbierto()) {
			principal.crafteo.MenuCrafteo.getInstancia().pintar(g);
			return;
		}
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

		// =========================================================================
		// 1. RENDERIZADO DEL ESCENARIO Y ENTIDADES AL BUFFER DE MUNDO (VRAM)
		// =========================================================================
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

			Globales.GESTOR_INTERACCION.pintar(gMundo);

			if (Globales.GESTOR_CONSTRUCCION.isActivo()) {
				Globales.GESTOR_CONSTRUCCION.pintar(gMundo);
			}

			Globales.GESTOR_PARTICULAS.pintar(gMundo);
			Globales.GESTOR_TEXTOS.pintar(gMundo);

			gMundo.translate(-offsetMundoX, -offsetMundoY);

		} finally {
			gMundo.dispose();
		}

		// =========================================================================
		// 2. SINCRONIZACIÓN DE LA MATRIZ LIMPIA DE PANTALLA (ZERO-GC DIRTY FLAG)
		// =========================================================================
		if ((this.transformPantallaBase == null) || (this.lastDespX != Globales.DESPLAZAMIENTO_X)
				|| (this.lastDespY != Globales.DESPLAZAMIENTO_Y)
				|| (this.lastFactorEscalaX != Globales.FACTOR_ESCALADO_X)
				|| (this.lastFactorEscalaY != Globales.FACTOR_ESCALADO_Y)) {

			this.lastDespX = Globales.DESPLAZAMIENTO_X;
			this.lastDespY = Globales.DESPLAZAMIENTO_Y;
			this.lastFactorEscalaX = Globales.FACTOR_ESCALADO_X;
			this.lastFactorEscalaY = Globales.FACTOR_ESCALADO_Y;
			this.transformPantallaBase = g.getTransform(); // Se captura 1 sola vez en el inicio o al cambiar ventana
		}

		// =========================================================================
		// 3. PRESENTACIÓN DEL MUNDO CON TRANSFORMACIÓN CINEMÁTICA
		// =========================================================================
		final boolean hayTransformacionMundo = (zoomFinal != 1.0) || (shakeX != 0.0) || (shakeY != 0.0)
				|| (rotacion != 0.0);

		try {
			if (hayTransformacionMundo) {
				g.translate(Constantes.CENTROX + shakeX, Constantes.CENTROY + shakeY);
				g.scale(zoomFinal, zoomFinal);
				g.rotate(rotacion);
			} else {
				g.translate(Constantes.CENTROX, Constantes.CENTROY);
			}

			g.drawImage(this.bufferMundo, -centroBufX, -centroBufY, null);

		} finally {
			// RESTAURACIÓN EXACTA BIT-FOR-BIT: Cero decimales residuales, cero vibración
			g.setTransform(this.transformPantallaBase);
		}

		// =========================================================================
		// 4. CAPAS DE PANTALLA FIJAS (1:1 PIXEL-PERFECT EN COORDENADAS LIMPIAS)
		// =========================================================================
		// 4.1 La penumbra y las antorchas oscurecen el mundo físico
		Globales.GESTOR_CLIMA.pintar(g);
		Globales.GESTOR_LUZ.pintar(g);

		// 4.2 La Bóveda Celeste (Auroras boreales y estrellas) BRILLAN sobre la noche
		if (Globales.GESTOR_ASTRONOMICO != null) {
			Globales.GESTOR_ASTRONOMICO.pintar(g);
		}

		// 4.3 Clima terrestre (Nubes y lluvia)

		if (!Globales.JUGADOR.estaEliminado()) {
			this.pintarInventarios(g);
			Globales.MOTOR_IGU.pintar(g);
		}

		this.pintarPantallaDerrota(g);
		GestorTransicionSueno.getInstancia().pintar(g); // Capa Óptica de la Singularidad
		Globales.CAMARA.pintarLetterbox(g);
		this.pintarDebug(g);

		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			final ArrayList<Ente> entesIntersectadosRaton = this.getMundo().getEnteIntersectados(
					Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara(), true, true);
			for (final Ente e : entesIntersectadosRaton) {
				Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltip(g, e.getClass().getSimpleName(), Color.WHITE,
						Color.BLACK);
			}
		}

		Globales.GESTOR_DIALOGOS.pintar(g);
	}

	private void pintarInventarios(final Graphics2D g) {
		Globales.GESTOR_INVENTARIO.pintar(g);
		Globales.GESTOR_INVENTARIO.pintarTooltipsYPuntero(g, this.RATON.getPuntoPosicionEscalado());
	}

	private void pintarPantallaDerrota(final Graphics2D g) {
		if (this.mostrarPantallaMuerte && this.GT_MOSTRAR_PANTALLA_MUERTE
				.transcurrioMiliSegundos(this.TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE)) {

			// 1. Fondo sombreado oscuro
			Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO,
					new Color(10, 5, 8, 185));

			final Font fontPrevia = g.getFont();

			// 2. Título principal de muerte
			final String texto = (Globales.dificultad == Dificultad.DIFICIL) ? "FIN DEL JUEGO" : "HAS MUERTO";
			final Color color = (Globales.dificultad == Dificultad.DIFICIL) ? new Color(255, 30, 40)
					: new Color(235, 45, 45);

			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 36f));
			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int x = Constantes.CENTROX - (anchoTexto / 2);
			final int y = Constantes.CENTROY - 25;

			Render2D.dibujarStringConSombra(g, texto, x, y, color, Color.BLACK);

			// 3. Subtítulo explicativo según la dificultad
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
			final String subtitulo;
			if (Globales.dificultad == Dificultad.FACIL) {
				subtitulo = "Dificultad Fácil · Inventario y equipo conservados";
			} else if (Globales.dificultad == Dificultad.NORMAL) {
				subtitulo = "Dificultad Normal · 70% de tus ítems cayeron en el lugar de tu muerte";
			} else {
				subtitulo = "Dificultad Difícil · Muerte permanente";
			}

			final int anchoSub = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, subtitulo);
			final int xSub = Constantes.CENTROX - (anchoSub / 2);
			Render2D.dibujarStringConSombra(g, subtitulo, xSub, y + 18, new Color(200, 205, 220), Color.BLACK);

			g.setFont(fontPrevia);

			// 4. Botón centrado en la parte baja
			if (this.botonMuerte != null) {
				this.botonMuerte.pintar(g);
			}
		}
	}

	private void pintarDebug(final Graphics2D g) {
		if (Globales.pausa) {
			Render2D.dibujarString(g, "PAUSA", 10, 10, Color.RED);
		}

		this.pintarTiempoJugado(g);

		if (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()) {
			Render2D.dibujarString(g, (this.tilePisado != null) ? this.tilePisado.toString() : "PuntoTile: (none)", 120,
					20, Color.WHITE);
			if (this.tilePisado != null) {
				this.tilePisado.pintarContorno(g, Color.WHITE);
			}
		}

		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			g.setColor(Color.GREEN);
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, Constantes.TAMANO_FUENTE));
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
			Render2D.dibujarString(g, "Modo Construir(B): " + (Globales.GESTOR_CONSTRUCCION.isActivo() ? "ON" : "OFF"),
					20, 320);
		}
	}

	private void pintarTiempoJugado(final Graphics2D g) {
		if (Globales.segundosJugados != this.lastSegundosJugados) {
			this.lastSegundosJugados = Globales.segundosJugados;
			this.cachedTextoTiempoJugado = Globales.horasJugadas + "h " + Globales.minutosJugados + "m "
					+ Globales.segundosJugados + "s";
		}
		Render2D.dibujarStringConSombra(g, this.cachedTextoTiempoJugado, 15, 20, Color.CYAN, Color.BLACK, 10f);
	}

	@Override
	public void cargarMapa(final GestorCarga gc, final String nombreMapa, final String nombreMundo,
			final String nombreSpawn, final boolean reset) {
		if (gc != null) {
			gc.setPorcentajeCarga(10);
			gc.setDetalleCarga("Cargando mapa " + nombreMapa);
		}

		Globales.GESTOR_LUZ.apagarTodasLasLuces();
		Globales.GESTOR_PARTICULAS.limpiar();
		Globales.GESTOR_ZONAS_AMBIENTE.limpiarZonas();
		Globales.MOTOR_IGU.desvincularJefe();

		if (reset && (Globales.GESTOR_GRUPO != null)) {
			Globales.GESTOR_GRUPO.vaciar();
		}

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
		this.mapa.establecerMundoActual(nombreMundo);

		if (reset) {
			Globales.JUGADOR.restablecerYCambiarMundo(this.mapa.getMundoActual());
		} else {
			Globales.JUGADOR.setMundo(this.mapa.getMundoActual());
			this.mapa.getMundoActual().teletransportarJugadorASpawn(nombreSpawn);
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

	}

	public void cargarPartidaGuardada(final GestorCarga gc, final JSONObject saveJson) {
		if (saveJson == null) {
			if (gc != null) {
				gc.setCompleto(true);
			}
			return;
		}

		if (gc != null) {
			gc.setPorcentajeCarga(15);
			gc.setDetalleCarga("Leyendo metadatos de guardado");
		}

		final String nombreMapa = (saveJson.get("mapa") != null) ? saveJson.get("mapa").toString() : MapaManager.MAPA_1;
		final String nombreMundo = (saveJson.get("mundo") != null) ? saveJson.get("mundo").toString() : "exterior";

		Globales.GESTOR_LUZ.apagarTodasLasLuces();
		Globales.GESTOR_PARTICULAS.limpiar();
		Globales.GESTOR_ZONAS_AMBIENTE.limpiarZonas();
		Globales.MOTOR_IGU.desvincularJefe();

		if (Globales.GESTOR_GRUPO != null) {
			Globales.GESTOR_GRUPO.vaciar();
		}

		if (gc != null) {
			gc.setPorcentajeCarga(30);
			gc.setDetalleCarga("Cargando escenario: " + nombreMapa);
		}
		this.mapa = MapaManager.cargarMapa(nombreMapa, gc);

		if ((this.mapa == null) || (this.mapa.getMundo(nombreMundo) == null)) {
			System.err.println("[GestorJuego] Error: No se pudo cargar el mapa o mundo guardado.");
			if (gc != null) {
				gc.setCompleto(true);
			}
			return;
		}

		this.mapa.establecerMundoActual(nombreMundo);
		final Mundo mundoActivo = this.mapa.getMundoActual();

		if (gc != null) {
			gc.setPorcentajeCarga(60);
			gc.setDetalleCarga("Restaurando mundo, tiempo y deltas");
		}

		// Restaurar dificultad guardada
		if (saveJson.get("dificultad") != null) {
			try {
				Globales.dificultad = Dificultad.valueOf(saveJson.get("dificultad").toString());
			} catch (final Exception ignored) {
			}
		}

		// Restaurar Calendario y Astronomía
		if (Globales.GESTOR_ASTRONOMICO != null) {
			if (saveJson.get("astronomia") instanceof JSONObject) {
				Globales.GESTOR_ASTRONOMICO.importarJSON((JSONObject) saveJson.get("astronomia"));
			} else if (saveJson.get("calendario") instanceof JSONObject) {
				Globales.GESTOR_ASTRONOMICO.importarJSON((JSONObject) saveJson.get("calendario"));
			}
		}

		// Restaurar Progreso
		if ((saveJson.get("progreso") instanceof JSONArray) && (Globales.GESTOR_PROGRESO != null)) {
			Globales.GESTOR_PROGRESO.importarJSON((JSONArray) saveJson.get("progreso"));
		}

		// Restaurar Deltas
		if (saveJson.get("deltas") instanceof JSONObject) {
			Globales.GESTOR_DELTAS.importarJSON((JSONObject) saveJson.get("deltas"));
		}

		Globales.GESTOR_DELTAS.aplicarDelta(mundoActivo);
		mundoActivo.aplicarMetadatosAtmosfericos();

		if (gc != null) {
			gc.setPorcentajeCarga(80);
			gc.setDetalleCarga("Restaurando personaje e inventario");
		}

		// Restaurar Jugador
		Globales.JUGADOR.setMundo(mundoActivo);
		if (saveJson.get("jugador") instanceof JSONObject) {
			Globales.JUGADOR.importarDeJSON((JSONObject) saveJson.get("jugador"));
		}

		// Restaurar Séquito / Grupo
		if ((saveJson.get("grupo") instanceof JSONObject) && (Globales.GESTOR_GRUPO != null)) {
			Globales.GESTOR_GRUPO.importarJSON((JSONObject) saveJson.get("grupo"), mundoActivo);
		}

		Globales.CAMARA.setEntidadEnfocada(Globales.JUGADOR);
		Globales.CAMARA.habilitarGestorLimite();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().establecerMundo(mundoActivo);
		Globales.RATON.soltar();

		if (!Globales.TECLADO.TECLA_DIJKSTRA.presionado()) {
			Globales.TECLADO.TECLA_DIJKSTRA.presionar();
		}

		Globales.partidaIniciada = true;

		if (gc != null) {
			gc.setPorcentajeCarga(100);
			gc.setDetalleCarga("¡Partida cargada con éxito!");
			gc.setCompleto(true);
		}
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

		mundoActual.meterEntidad(zonaTP);
		mundoActual.meterEntidad(zonaTP2);
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
}