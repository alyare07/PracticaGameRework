package principal.maquinaestado.estados;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.entes.criaturas.neutrales.CosaNeutral;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.proyectil.ProyectilGranada;
import principal.entes.proyectil.explosivo.BolaFuego;
import principal.eventos.Evento;
import principal.eventos.EventoJugadorZonaTP;
import principal.igu.MotorIGU;
import principal.mapa.Mundo;
import principal.mapa.Mapa;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.mapa.escenario.tps.PuertaZona;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.pantallaCarga.CargaJuego;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.SonidoMP3;

public final class GestorJuego implements EstadoJuego, CargaJuego{
	protected final GestorEstados GE;
	protected final GestorPartida GP;
	protected final GestorTiempo GT_MOSTRAR_PANTALLA_MUERTE;
	protected final ArrayList<Evento> EVENTOS = new ArrayList<Evento>();
	protected Mundo mundo;
	protected final int TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE=1500;
	Tile tilePisado = null;
	
	private boolean mostrarPantallaMuerte;
	private final Raton RATON = Constantes.RATON;
	private SonidoMP3 sonidoFondo;
	
	private MotorIGU motoIGU;
	private Ellipse2D.Double areaAUX;
	
	public GestorJuego(final GestorEstados ge, final GestorPartida gp) {
		this.GE = ge;
		this.GP = gp;
		this.GT_MOSTRAR_PANTALLA_MUERTE = new GestorTiempo();
		this.motoIGU = new MotorIGU();
	}

	@Override
	public void cargarJuego(String rutaMundo, GestorCarga gc) {
		gc.setPorcentajeCarga(0);
		gc.setCompleto(false);
		Escenario esc = EscenarioLoader.importarEscenario(new File(rutaMundo), gc,75);
		boolean cargaExitosa = true;
		if (esc == null) {
			System.err.println("No se ha podido cargar el mapa especificado...");
			File f = new File("error escenario, no load.txt");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			gc.setDetalleCarga("Generando terreno");
			esc = new Escenario(new Mapa(1000, 1000, Constantes.LADO_TILE),"[]" , "[]","[]","[]");
			cargaExitosa = false;
			gc.setPorcentajeCarga(75);
		}
		Constantes.LADO_TILE = esc.getMapa().ladoTile();
		gc.setDetalleCarga("Generando mundo");
		this.mundo = new Mundo(esc, new Point(776, 300),gc,25);
		// SACAR EL ELIMINADO DEL JUGADOR Y DEMAS
		Constantes.JUGADOR.restablecer(this.mundo);
		Constantes.CAMARA.setEntidadEnfocada(Constantes.JUGADOR);
		Constantes.CAMARA.habilitarGestorLimite();
		this.mundo.moverJugadorPuntoComienzo();
		Constantes.INVENTARIO.establecerMundo(this.mundo);
		Constantes.JUGADOR.setMundo(mundo);
		Constantes.RATON.soltar();
		
		if (cargaExitosa) {
//			EscenarioLoader.exportarEscenarioPrueba(esc, new File("mundos\\"+"Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp"));
//			if((1+1) == 2) {
//				System.exit(0);
//			}
			
			this.areaAUX = new Ellipse2D.Double(575,320,64,64);
			
			agregarObjetosAlMapa();
			
			if(!Constantes.TECLADO.TECLA_DIJKSTRA.presionado()) {
				Constantes.TECLADO.TECLA_DIJKSTRA.presionar();
			}

		} else {
			Constantes.JUGADOR.establecerPosicion((esc.getMapa().getAncho() / 2) - (Constantes.JUGADOR.getRectangulo().width / 2),
					(esc.getMapa().getAlto() / 2) - (Constantes.JUGADOR.getRectangulo().height / 2));
		}

		sonidoFondo = new SonidoMP3("sonidos/forest.mp3");
		Constantes.GLOBALES.partidaIniciada = true;
		gc.setCompleto(true);
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
			sonidoFondo.pausar();
			return;
		}
		//COD PRUEBA
		this.actualizarCambioCamaraConEntes();
		//FIN COD PRUEBA
		Constantes.JUGADOR.actualizar();
		
		if(!Constantes.JUGADOR.estaEliminado()) {
			sonidoFondo.actualizar(!Constantes.GLOBALES.pausa);
			Constantes.INVENTARIO.actualizar(RATON);
			if(Constantes.GLOBALES.viendoCofre) {
				Constantes.GLOBALES.inventarioVault.actualizar(RATON, mundo, Constantes.GLOBALES.viendoCofre);
			}
			this.mundo.actualizar();
			this.actualizarEventos();
		}else {
			sonidoFondo.pausar();
		}
		
		this.verificarPantallaMuerte();
		
		this.motoIGU.actualizar();
		
		{
			final Rectangle pieJugador = Constantes.JUGADOR.getRectanguloInterseccionAbajo(0);
			tilePisado = this.mundo.getMapa().getTileReferenciado(pieJugador.x, pieJugador.y + pieJugador.height);
			if (tilePisado == null) {

				return;
			}
		}

	}
	
	private void actualizarCambioCamaraConEntes() {
		if(Constantes.RATON.presionadoClickIzqUnicaAct()) {
			for(Ente e: this.mundo.getEnteIntersectados(Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara(), true)) {
				Constantes.CAMARA.setEntidadEnfocada(e);
				Constantes.CAMARA.habilitarGestorLimite();
				System.out.println("ENFOQUE CAMARA CAMBIADO: "+e.getClass().getName());
			}
		}
		
	}
	
	private void actualizarEventos() {
		for(int i = 0 ; i < this.EVENTOS.size(); i++) {
			this.EVENTOS.get(i).actualizar();
			if(this.EVENTOS.get(i).estaEliminado()) this.EVENTOS.remove(this.EVENTOS.get(i));
		}
	}

	private boolean detectarCambioAMenu() {
		if (Constantes.TECLADO.TECLA_ESCAPE.presionado()) {
			if(Constantes.INVENTARIO.esVisible()) {
				Constantes.INVENTARIO.ocultar();
				Constantes.TECLADO.TECLA_ESCAPE.soltar();
				return false;
			}
			sonidoFondo.actualizar(false);
			this.GP.establecerEstadoActivoMenu();
			return true;
		}
		return false;
	}

	@Override
	public void pintar(Graphics2D g) {
		if (!Constantes.GLOBALES.partidaIniciada) {
			return;
		}
		this.mundo.pintar(g);
		pintarDebug(g);
		Constantes.JUGADOR.pintar(g);
		if(!Constantes.JUGADOR.estaEliminado()) {
			if(Constantes.GLOBALES.viendoCofre) {
				Constantes.GLOBALES.inventarioVault.pintar(g);
			}
			Constantes.INVENTARIO.pintar(g);
			
		}
		
		this.motoIGU.pintar(g);
		
		
		if(mostrarPantallaMuerte && this.GT_MOSTRAR_PANTALLA_MUERTE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_MOSTRAR_PANTALLA_MUERTE)) {
			final String texto = "DERROTA";
			final float tamanoLetra = 48f;
			final Color color= Color.red;
			g.setFont(g.getFont().deriveFont(tamanoLetra));
			final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int altoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
			final int x = Constantes.CENTROX -(anchoTexto/2);
			final int y = Constantes.CENTROY - (altoTexto/2);
			DibujoDebug.dibujarString(g, texto, x, y, color);
			g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
		}

	}

	private void pintarDebug(final Graphics2D g) {
		if (Constantes.GLOBALES.pausa) {
			DibujoDebug.dibujarString(g, "PAUSA", 10, 10, Color.RED);
		}
		pintarTiempoJugado(g);
		if (Constantes.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()) {
			DibujoDebug.dibujarString(g, tilePisado != null ? tilePisado.toString() : "PuntoTile: (none)", 120, 20, Color.white);
			if (tilePisado != null) {
				this.tilePisado.pintarContorno(g, Color.WHITE);
			}
		}

		if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
			g.setColor(Color.green);
			DibujoDebug.dibujarString(g, "X: " + String.valueOf(Constantes.JUGADOR.getPosicionXInt()), 20, 80);
			DibujoDebug.dibujarString(g, "Y: " + String.valueOf(Constantes.JUGADOR.getPosicionYInt()), 20, 95);
			DibujoDebug.dibujarString(g, "X_PARADO: " + String.valueOf(Constantes.JUGADOR.getPosicionXParado()), 20, 110);
			DibujoDebug.dibujarString(g, "Y_PARADO: " + String.valueOf(Constantes.JUGADOR.getPosicionYParado()), 20, 125);
			DibujoDebug.dibujarString(g, "Velocidad: " + String.valueOf(Constantes.JUGADOR.getVelocidad()), 20, 140);
			DibujoDebug.dibujarString(g, "Dijkstra(F2): " + (Constantes.TECLADO.TECLA_DIJKSTRA.presionado() ? "Activo" : "Inactivo"), 20, 155);
			DibujoDebug.dibujarString(g, "DijkstraInfo(F6): " + (Constantes.TECLADO.TECLA_DIJKSTRA_INFO.presionado() ? "Activo" : "Inactivo"), 20, 170);
			DibujoDebug.dibujarString(g, "DebugGroupTile(F4): " + (Constantes.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado() ? "Activo" : "Inactivo"), 20, 185);
			DibujoDebug.dibujarString(g, "DebugTile(F3): " + (Constantes.TECLADO.TECLA_DEBUG_TILE.presionado() ? "Activo" : "Inactivo"), 20, 200);
			DibujoDebug.dibujarString(g, "DebugTileInfo(F5): " + (Constantes.TECLADO.TECLA_DEBUG_TILE_INFO.presionado() ? "Activo" : "Inactivo"), 20, 215);
			DibujoDebug.dibujarString(g, "VerColisiones(F7): " + (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() ? "Activo" : "Inactivo"), 20, 230);
			DibujoDebug.dibujarString(g, "OcultarTerreno(F8): " + (Constantes.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? "Activo" : "Inactivo"), 20, 245);
			DibujoDebug.dibujarString(g, "OcultarComplementos(F9): " + (Constantes.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado() ? "Activo" : "Inactivo"), 20, 260);
			DibujoDebug.dibujarString(g, "VerAlcanceAtaque(F10): " + (Constantes.TECLADO.TECLA_VER_ALCANCE_ATAQUE.presionado() ? "Activo" : "Inactivo"), 20, 275);
			DibujoDebug.dibujarString(g, "Direccion: " + Constantes.JUGADOR.getDireccion().toString(), 20, 290);
			DibujoDebug.dibujarString(g, "Estados: "+Constantes.JUGADOR.getStringEstados(), 20, 305);
		}
	}

	public void agregarObjetosAlMapa() {
		
		//CODIGO PRUEBA
		this.mundo.crearProyectil(new BolaFuego(25, 0.25, 100000000,false, mundo, 1005, 392,40, Direccion.OESTE, null));
		this.mundo.crearProyectil(new ProyectilGranada(40, areaAUX, this.mundo, 954, 79,ListaModelosItem.COD_CONSUMIBLE_GRANADAT1));
		this.mundo.meterEntidad(new GranadaT1(800, 80, 50));
		ZonaTP zonaTP2 = new ZonaTP(new Rectangle(684,215,20,20), null);;
		ZonaTP zonaTP = new ZonaTP(new Rectangle(878,173,20,20), new PuertaZona(zonaTP2));
		zonaTP2.setPuertaTP(new PuertaZona(zonaTP));
		
		this.mundo.meterEntidad(zonaTP);
		this.mundo.meterEntidad(zonaTP2);
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

		this.mundo.meterEntidad(new Complemento(773, 177, ListaModeloComplemento.COD_CASA_1));
	}

	public void establecerCriaturas() {
		this.mundo.agregarCriatura(
				new Enemigo(330, 552, 16, 16, 50, 50, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/jugadores.png").getSubimage(48, 48, 48, 48),this.mundo));
		this.mundo.agregarCriatura(
				new Enemigo(976, 90, 16, 16, 50, 50, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/jugadores.png").getSubimage(48, 48, 48, 48),this.mundo));
		this.mundo.agregarCriatura(new CosaNeutral(184, 166, 8, 8, Color.ORANGE, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(468, 645, 8, 8, Color.CYAN, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(1096, 156, 8, 8, Color.magenta, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(1931, 491, 8, 8, Color.YELLOW, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(156, 166, 8, 8, Color.BLUE, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(734, 52, 8, 8, Color.PINK, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(787, 57, 8, 8, Color.magenta, this.mundo.getMapa(), 0.2));
		this.mundo.agregarCriatura(new CosaNeutral(578, 868, 8, 8, Color.GREEN, this.mundo.getMapa(), 0.2));
		
		
		
	}

	private void pintarTiempoJugado(final Graphics2D g) {
		final String texto = String.valueOf(Constantes.GLOBALES.horasJugadas) + "h " + String.valueOf(Constantes.GLOBALES.minutosJugados) + "m "
				+ String.valueOf(Constantes.GLOBALES.segundosJugados) + "s";
		DibujoDebug.dibujarString(g, texto, 20, 20, Color.CYAN);
	}

	public Mapa getMapa() {
		return this.mundo.getMapa();
	}
	
	public Mundo getMundo() {
		return this.mundo;
	}
	
	protected void verificarPantallaMuerte() {
		if(Constantes.JUGADOR.estaEliminado() && !this.mostrarPantallaMuerte) {
			this.mostrarPantallaMuerte = true;
			this.GT_MOSTRAR_PANTALLA_MUERTE.establecerReferenciaTiempoActual();
			
		}
	}

}
