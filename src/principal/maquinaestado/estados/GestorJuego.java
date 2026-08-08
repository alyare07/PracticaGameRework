package principal.maquinaestado.estados;

import java.awt.Color;
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
import principal.utilidades.SonidoMP3;

public final class GestorJuego implements EstadoJuego, cargaMapa {
	protected final GestorEstados GE;
	protected final GestorPartida GP;
	protected final GestorTiempo GT_MOSTRAR_PANTALLA_MUERTE;
	protected final ArrayList<Evento> EVENTOS = new ArrayList<Evento>();
	protected Mapa mapa;
	protected final int TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE = 1500;
	Tile tilePisado = null;

	private boolean mostrarPantallaMuerte;
	private final Raton RATON = Constantes.RATON;
	private SonidoMP3 sonidoFondo;

	private final MotorIGU motoIGU;

	public GestorJuego(final GestorEstados ge, final GestorPartida gp) {
		this.GE = ge;
		this.GP = gp;
		this.GT_MOSTRAR_PANTALLA_MUERTE = new GestorTiempo();
		this.motoIGU = new MotorIGU();
	}

	@Override
	public void actualizar() {

		if (this.detectarCambioAMenu()) {
			return;
		}
		if (!Constantes.GLOBALES.partidaIniciada) {
			return;
		}

		if (Constantes.GLOBALES.pausa) {
//	    this.sonidoFondo.pausar();
			return;
		}
		// COD PRUEBA
		this.actualizarCambioCamaraConEntes();
		// FIN COD PRUEBA
		Constantes.JUGADOR.actualizar();

		if (!Constantes.JUGADOR.estaEliminado()) {
//	    this.sonidoFondo.actualizar(!Constantes.GLOBALES.pausa);
			Constantes.INVENTARIO.actualizar(this.RATON);
			if (Constantes.GLOBALES.viendoCofre) {
				Constantes.GLOBALES.inventarioVault.actualizar(this.RATON, this.mapa.getMundoActual(),
						Constantes.GLOBALES.viendoCofre);
			}
			this.mapa.actualizar();
			this.actualizarEventos();
		} else {
//	    this.sonidoFondo.pausar();
		}

		this.verificarPantallaMuerte();

		this.motoIGU.actualizar();

		{
			final Shape s = Constantes.JUGADOR.getAreaInterseccionMovimiento();
			this.tilePisado = this.mapa.getMundoActual().getTerreno().getTileReferenciado(
					s.getBounds().x + (s.getBounds().width / 2), s.getBounds().y + s.getBounds().height);
			if (this.tilePisado == null) {

				return;
			}
		}

	}

	private void actualizarCambioCamaraConEntes() {
		if (Constantes.RATON.presionadoClickIzqUnicaAct()) {
			for (final Ente e : this.mapa.getMundoActual().getEnteIntersectados(
					Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara(), true)) {
				Constantes.CAMARA.setEntidadEnfocada(e);
				Constantes.CAMARA.habilitarGestorLimite();
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
		if (Constantes.TECLADO.TECLA_ESCAPE.presionado()) {
			if (Constantes.INVENTARIO.esVisible()) {
				Constantes.INVENTARIO.ocultar();
				Constantes.TECLADO.TECLA_ESCAPE.soltar();
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
		if (!Constantes.GLOBALES.partidaIniciada) {
			return;
		}
		this.mapa.pintar(g);
		this.pintarDebug(g);
		Constantes.JUGADOR.pintar(g);
		if (!Constantes.JUGADOR.estaEliminado()) {
			if (Constantes.GLOBALES.viendoCofre) {
				Constantes.GLOBALES.inventarioVault.pintar(g);
			}
			Constantes.INVENTARIO.pintar(g);

		}

		this.motoIGU.pintar(g);

		if (this.mostrarPantallaMuerte && this.GT_MOSTRAR_PANTALLA_MUERTE
				.transcurrioMiliSegundos(this.TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE)) {
			final String texto = "DERROTA";
			final float tamanoLetra = 48f;
			final Color color = Color.red;
			g.setFont(g.getFont().deriveFont(tamanoLetra));
			final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int altoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
			final int x = Constantes.CENTROX - (anchoTexto / 2);
			final int y = Constantes.CENTROY - (altoTexto / 2);
			DibujoDebug.dibujarString(g, texto, x, y, color);
			g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
		}

	}

	private void pintarDebug(final Graphics2D g) {
		if (Constantes.GLOBALES.pausa) {
			DibujoDebug.dibujarString(g, "PAUSA", 10, 10, Color.RED);
		}
		this.pintarTiempoJugado(g);
		if (Constantes.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()) {
			DibujoDebug.dibujarString(g, this.tilePisado != null ? this.tilePisado.toString() : "PuntoTile: (none)",
					120, 20, Color.white);
			if (this.tilePisado != null) {
				this.tilePisado.pintarContorno(g, Color.WHITE);
			}
		}

		if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
			g.setColor(Color.green);
			DibujoDebug.dibujarString(g, "X: " + String.valueOf(Constantes.JUGADOR.getPosicionXInt()), 20, 80);
			DibujoDebug.dibujarString(g, "Y: " + String.valueOf(Constantes.JUGADOR.getPosicionYInt()), 20, 95);
			DibujoDebug.dibujarString(g, "X_PARADO: " + String.valueOf(Constantes.JUGADOR.getPosicionXParado()), 20,
					110);
			DibujoDebug.dibujarString(g, "Y_PARADO: " + String.valueOf(Constantes.JUGADOR.getPosicionYParado()), 20,
					125);
			DibujoDebug.dibujarString(g, "Velocidad: " + String.valueOf(Constantes.JUGADOR.getVelocidad()), 20, 140);
			DibujoDebug.dibujarString(g,
					"Dijkstra(F2): " + (Constantes.TECLADO.TECLA_DIJKSTRA.presionado() ? "Activo" : "Inactivo"), 20,
					155);
			DibujoDebug.dibujarString(g, "DijkstraInfo(F6): "
					+ (Constantes.TECLADO.TECLA_DIJKSTRA_INFO.presionado() ? "Activo" : "Inactivo"), 20, 170);
			DibujoDebug.dibujarString(g,
					"DebugGroupTile(F4): "
							+ (Constantes.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado() ? "Activo" : "Inactivo"),
					20, 185);
			DibujoDebug.dibujarString(g,
					"DebugTile(F3): " + (Constantes.TECLADO.TECLA_DEBUG_TILE.presionado() ? "Activo" : "Inactivo"), 20,
					200);
			DibujoDebug.dibujarString(g, "DebugTileInfo(F5): "
					+ (Constantes.TECLADO.TECLA_DEBUG_TILE_INFO.presionado() ? "Activo" : "Inactivo"), 20, 215);
			DibujoDebug.dibujarString(g, "VerColisiones(F7): "
					+ (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() ? "Activo" : "Inactivo"), 20, 230);
			DibujoDebug.dibujarString(g, "OcultarTerreno(F8): "
					+ (Constantes.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? "Activo" : "Inactivo"), 20, 245);
			DibujoDebug.dibujarString(g,
					"OcultarComplementos(F9): "
							+ (Constantes.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado() ? "Activo" : "Inactivo"),
					20, 260);
			DibujoDebug.dibujarString(g,
					"VerAlcanceAtaque(F10): "
							+ (Constantes.TECLADO.TECLA_VER_ALCANCE_ATAQUE.presionado() ? "Activo" : "Inactivo"),
					20, 275);
			DibujoDebug.dibujarString(g, "Direccion: " + Constantes.JUGADOR.getDireccion().toString(), 20, 290);
			DibujoDebug.dibujarString(g, "Estados: " + Constantes.JUGADOR.getStringEstados(), 20, 305);
			DibujoDebug.dibujarString(g,
					"FPS Limitado(F11): " + (Constantes.TECLADO.TECLA_FPS_LIMITE.presionado() ? "Activo" : "Inactivo"),
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
//		
//		Cofre cofre = new CofreMediano(Constantes.JUGADOR.getPosicionXInt()+70, Constantes.JUGADOR.getPosicionYInt());
//		cofre.meterItem(new PocionVidaMenor(4));
//		cofre.meterItem(new PocionVidaMenor(1));
//		cofre.meterItem(new PocionVidaMenor(7));
//		cofre.meterItem(new PocionVidaMenor(6));
//		cofre.meterItem(new Pistola(ListaModelosItem.COD_EQUIPABLE_ARMA, new Municion(8, 3)));
//		this.mundo.meterEntidad(cofre);
//		{
//			JSONArray lista = new JSONArray();
//			lista.add(cofre.exportarParaJson());
//			
//			String jsonExpEncriptado = Constantes.FUNCIONES.ENCRIPTADOR_STRING.encriptar(lista.toJSONString());
//			File ruta = new File("jsonprueba.json");
//			PrintWriter pw = null;
//			try {
//				pw = new PrintWriter(ruta);
//				pw.print(jsonExpEncriptado);
//				pw.flush();
//				System.out.println("Json exportado en:  "+ ruta);
//			} catch (IOException e) {
//				System.out.println("Error al exportar escenario: "+ e.getMessage());
//			}finally {
//				pw.close();
//			}
//		}
//		System.exit(0);

		this.mapa.getMundoActual().meterEntidad(new Complemento(773, 177, ListaModeloComplemento.COD_CASA_1));
	}

	public void establecerCriaturas() {

	}

	@Override
	public void cargarMapa(final GestorCarga gc, final String nombreMapa, final boolean reset,
			final String nombreSpawn) {
		this.mapa = MapaManager.cargarMapa(nombreMapa, gc);
		// SACAR EL ELIMINADO DEL JUGADOR Y DEMAS

		if (reset) {
			Constantes.JUGADOR.restablecerYCambiarMundo(this.mapa.getMundoActual());
		} else {
			Constantes.JUGADOR.setMundo(this.mapa.getMundoActual());
		}
		this.mapa.getMundoActual().getSpawn(nombreSpawn).moverJugadorCentrado();
		Constantes.CAMARA.setEntidadEnfocada(Constantes.JUGADOR);
		Constantes.CAMARA.habilitarGestorLimite();
//////////////VER ACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
		Constantes.INVENTARIO.establecerMundo(this.mapa.getMundoActual());
		Constantes.RATON.soltar();
//	this.agregarObjetosAlMundo();
		if (!Constantes.TECLADO.TECLA_DIJKSTRA.presionado()) {
			Constantes.TECLADO.TECLA_DIJKSTRA.presionar();
		}
//	this.sonidoFondo = new SonidoMP3("sonidos/forest.mp3");
		Constantes.GLOBALES.partidaIniciada = true;
		gc.setCompleto(true);
	}

	private void pintarTiempoJugado(final Graphics2D g) {
		final String texto = String.valueOf(Constantes.GLOBALES.horasJugadas) + "h "
				+ String.valueOf(Constantes.GLOBALES.minutosJugados) + "m "
				+ String.valueOf(Constantes.GLOBALES.segundosJugados) + "s";
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
		if (Constantes.JUGADOR.estaEliminado() && !this.mostrarPantallaMuerte) {
			this.mostrarPantallaMuerte = true;
			this.GT_MOSTRAR_PANTALLA_MUERTE.establecerReferenciaTiempoActual();

		}
	}

}
