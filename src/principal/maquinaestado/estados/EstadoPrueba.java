package principal.maquinaestado.estados;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import principal.controles.Raton;
import principal.controles.Teclado;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.objetos.Complemento;
import principal.igu.MotorIGU;
import principal.inventario.Inventario;
import principal.mapa.Mapa;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public class EstadoPrueba implements EstadoJuego {
	private final Point PUNTO_COMIENZO = new Point(0, 0);
	private final Mundo MUNDO;
	private final Raton RATON = Constantes.RATON;
	private final Teclado TECLADO = Constantes.TECLADO;
	private final Jugador JUGADOR = Constantes.JUGADOR;
	private final Inventario INVENTARIO = Constantes.INVENTARIO;
	private final MotorIGU MOTOR_IGU = new MotorIGU();;
	private final String TITULO = "ESTADO PRUEBA";
	private final Rectangle AREA_TITULO = new Rectangle();
	private Tile tilePisado = null;
	public EstadoPrueba() {
		Constantes.CAMARA.setEntidadEnfocada(JUGADOR);
		MUNDO = new Mundo(new Escenario(new Mapa(100, 100, 16, ListaModeloTile.COD_CESPED_3	), "[]", "[]", "[]", "[]"), PUNTO_COMIENZO );
		JUGADOR.restablecer(MUNDO);
		INVENTARIO.establecerMundo(MUNDO);
		this.agregarEntidades();
		Constantes.GLOBALES.estadoJuego = true; // DESACTIVAR PARA NO VER LAS COLISIONES NI DE MAS INFOS
	}

	@Override
	public void actualizar() {
		if(TECLADO.TECLA_ESCAPE.presionado()) {
			System.exit(0);
		}
		if (Constantes.GLOBALES.pausa) {
			return;
		}
		Constantes.INVENTARIO.actualizar(this.RATON);
		if(Constantes.GLOBALES.viendoCofre) {
			Constantes.GLOBALES.inventarioVault.actualizar(RATON, MUNDO, Constantes.GLOBALES.viendoCofre);
		}
		{
			final Rectangle pieJugador = Constantes.JUGADOR.getRectanguloInterseccionAbajo(0);
			tilePisado = this.MUNDO.getMapa().getTileReferenciado(pieJugador.x, pieJugador.y + pieJugador.height);
			if (tilePisado == null) {

				return;
			}
		}
		
		this.JUGADOR.actualizar();
		this.MUNDO.actualizar();
		this.MOTOR_IGU.actualizar();
		
		
	}

	@Override
	public void pintar(Graphics2D g) {
		
		this.MUNDO.pintar(g);
		
		this.JUGADOR.pintar(g);
		if(Constantes.GLOBALES.viendoCofre) {
			Constantes.GLOBALES.inventarioVault.pintar(g);
		}
		INVENTARIO.pintar(g);
		
		this.MOTOR_IGU.pintar(g);
		this.pintarTitulo(g);
		this.pintarDebug(g);
	}
	
	
	
	private void pintarTitulo(final Graphics2D g) {
		if(AREA_TITULO.width == 0) {
			this.AREA_TITULO.height = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, TITULO);
			this.AREA_TITULO.width = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, TITULO);
			this.AREA_TITULO.x =Constantes.ANCHO_JUEGO/2 - this.AREA_TITULO.width/2;
			this.AREA_TITULO.y = this.AREA_TITULO.height/2 + 10;
		}
		DibujoDebug.dibujarRectanguloRelleno(g, AREA_TITULO.x,AREA_TITULO.y,AREA_TITULO.width,AREA_TITULO.height+2, Color.black);
		DibujoDebug.dibujarString(g, TITULO, AREA_TITULO.x, AREA_TITULO.y+AREA_TITULO.height, Color.white);
	}
	
	private void pintarDebug(final Graphics2D g) {
		if (Constantes.GLOBALES.pausa) {
			DibujoDebug.dibujarString(g, "PAUSA", 10, 10, Color.RED);
		}
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
			DibujoDebug.dibujarString(g, "Estados: "+JUGADOR.getStringEstados(), 20, 305);
		}
	}
	
	private void agregarEntidades() {
		this.MUNDO.meterEntidad(new Complemento(300, 250, ListaModeloComplemento.COD_CASA_1));
		this.MUNDO.meterEntidad(new Complemento(370, 300, ListaModeloComplemento.COD_ARBOL_2));
	}

}
