package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.time.LocalDateTime;

import org.json.simple.JSONObject;

import principal.controles.Raton;
import principal.entes.criaturas.Criatura;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.graficos.SuperficieDibujo;
import principal.mapa.GroupTile;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.EstadoJuego;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;

public class EditorMapa implements EstadoJuego{
    private final GestorEstados GE;
    private final int LADO_TILE;
    private final int LADO_GRUPO_TILE;
    private final int ANCHO;
    private final int ALTO;
    private final Terreno MAPA;
    private int x;
    private int y;
    private final Raton RATON = SuperficieDibujo.obetenerSuperficieDibujo().RATON;
    private Rectangle areaTileSelected;
    private Rectangle ultimaAreaTileAlterado = new Rectangle();
    private final Rectangle PALETA_MAPA;
    private final GrupoPaleta PALETAS;
    private final GestorTiempo GT = new GestorTiempo();
    private final GestorTiempo GT_COLOCACION = new GestorTiempo();
    private final int TIEMPO_ESPERA_MS_COLOCACION = 300;
    private final MundoEditor MUNDO_EDITOR;
    private final Rectangle AREA_MOUSE_APUNTADO = new Rectangle(-1, -1, 1, 1);

    public EditorMapa(final int ladoTile, final int anchoTiles, final int altoTiles, final int idModeloTile, final GestorEstados ge) {
	this.GE = ge;
	this.LADO_TILE = ladoTile;
	this.LADO_GRUPO_TILE = ladoTile * 2;
	this.ANCHO = anchoTiles * ladoTile;
	this.ALTO = altoTiles * ladoTile;
	this.MAPA = new Terreno(anchoTiles, altoTiles, this.LADO_TILE, idModeloTile);
	Constantes.CAMARA.setEntidadEnfocada(Constantes.JUGADOR);
	Constantes.JUGADOR.establecerPosicion(Constantes.JUGADOR.getMargenX(), Constantes.JUGADOR.getMargenY());
	this.x = Constantes.JUGADOR.getMargenX();
	this.y = Constantes.JUGADOR.getMargenY();
	this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4), Constantes.ALTO_JUEGO);
	this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width, this.PALETA_MAPA.height);
	this.MUNDO_EDITOR = new MundoEditor(this.MAPA);
	Constantes.JUGADOR.establecerPosicion(this.x, this.y);

	// COMIENZO CODIGO EJECUCION PRUEBA UNA SOLA VEZ (BORRAR)

	this.MUNDO_EDITOR.meterEntidad(new PocionVidaMenor(38, 17, 8));
	this.MUNDO_EDITOR.meterEntidad(new PocionVidaMenor(126, 175, 3));
	this.MUNDO_EDITOR.meterEntidad(new PocionVidaMenor(190, 240, 10));
	this.MUNDO_EDITOR.meterEntidad(new Pistola(147, 148, ListaModelosItem.COD_EQUIPABLE_ARMA, new Municion(8, 3)));
	this.MUNDO_EDITOR.meterEntidad(new Pistola(8, 6, ListaModelosItem.COD_EQUIPABLE_ARMA, new Municion(15, 10)));
	this.MUNDO_EDITOR.meterEntidad(new Pistola(257, 148, ListaModelosItem.COD_EQUIPABLE_ARMA, new Municion(20, 20)));
	this.MUNDO_EDITOR.meterEntidad(new PocionVidaMenor(190, 240, 10));
	// FIN CODIGO EJECUCION PRUEBA UNA SOLA VEZ (BORRAR)
    }

    public EditorMapa(final Terreno terreno, final GestorEstados ge) {
	this.GE = ge;
	this.MAPA = terreno;
	this.ANCHO = this.MAPA.getAncho();
	this.ALTO = this.MAPA.getAlto();
	this.LADO_TILE = this.MAPA.ladoTile();
	this.LADO_GRUPO_TILE = this.LADO_TILE * 2;
	Constantes.CAMARA.setEntidadEnfocada(Constantes.JUGADOR);
	Constantes.JUGADOR.establecerPosicion(Constantes.JUGADOR.getMargenX(), Constantes.JUGADOR.getMargenY());
	this.x = Constantes.JUGADOR.getMargenX();
	this.y = Constantes.JUGADOR.getMargenY();
	this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4), Constantes.ALTO_JUEGO);
	this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width, this.PALETA_MAPA.height);
	this.MUNDO_EDITOR = new MundoEditor(this.MAPA);
	Constantes.JUGADOR.establecerPosicion(this.x, this.y);

    }

    public EditorMapa(final String rutaMapa, final GestorEstados ge) {
	this.GE = ge;
	this.MAPA = EscenarioLoader.importarEscenario(new File(rutaMapa)).getTerreno();
	this.ANCHO = this.MAPA.getAncho();
	this.ALTO = this.MAPA.getAlto();
	this.LADO_TILE = this.MAPA.ladoTile();
	this.LADO_GRUPO_TILE = this.LADO_TILE * 2;
	Constantes.CAMARA.setEntidadEnfocada(Constantes.JUGADOR);
	Constantes.JUGADOR.establecerPosicion(Constantes.JUGADOR.getMargenX(), Constantes.JUGADOR.getMargenY());
	this.x = Constantes.JUGADOR.getMargenX();
	this.y = Constantes.JUGADOR.getMargenY();
	this.PALETA_MAPA = new Rectangle(0, 0, Constantes.ANCHO_JUEGO - (Constantes.ANCHO_JUEGO / 4), Constantes.ALTO_JUEGO);
	this.PALETAS = new GrupoPaleta(this.PALETA_MAPA.width, 0, Constantes.ANCHO_JUEGO - this.PALETA_MAPA.width, this.PALETA_MAPA.height);
	this.MUNDO_EDITOR = new MundoEditor(this.MAPA);
	Constantes.JUGADOR.establecerPosicion(this.x, this.y);

    }

    @Override
    public void actualizar() {
	this.RATON.actualizar(SuperficieDibujo.obetenerSuperficieDibujo());
	final Rectangle rr = this.RATON.getRectanguloPosicionEscalado();// rectangulo raton
	this.AREA_MOUSE_APUNTADO.x = rr.x + Constantes.CAMARA.getPosicionXInt() - Constantes.CAMARA.getMargenX();
	this.AREA_MOUSE_APUNTADO.y = rr.y + Constantes.CAMARA.getPosicionYInt() - Constantes.CAMARA.getMargenY();
	this.mover();
	this.actualizarTileApuntado();
	this.PALETAS.actualizar(this.RATON);
	this.alterarTileSeleccionado();
	this.MUNDO_EDITOR.actualizar();
	if (Constantes.TECLADO.TECLA_GUARDAR_MAPA.presionado()) {
	    if (this.GT.transcurrioSegundos(1)) {
		this.GT.establecerReferenciaTiempoActual();
		this.guardarMapa("Mapa_" + LocalDateTime.now().toString().replace(":", "-") + ".mp");
	    }

	} else if (Constantes.TECLADO.TECLA_ESCAPE.presionado()) {
	    this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
	    this.GE.disposeEditor();
	}

	if (Constantes.TECLADO.TECLA_DEBUG_GROUP_TILE.presionado()) {
	    if (!Constantes.GLOBALES.editorSelectGroupTile) {
		Constantes.GLOBALES.editorSelectGroupTile = true;

	    }
	} else {
	    if (Constantes.GLOBALES.editorSelectGroupTile) {
		Constantes.GLOBALES.editorSelectGroupTile = false;
	    }
	}
    }

    private void alterarTileSeleccionado() {
	if (this.RATON.presionadoClickIzq() && this.PALETA_MAPA.intersects(this.RATON.getPuntoPresionado())) {
	    final Paleta paleta = this.PALETAS.getPaletaActual();
	    if (this.areaTileSelected == null) {
		return;
	    }
	    final Tile tileTerrenoSeleccionado = Constantes.GLOBALES.editorSelectGroupTile ? this.MAPA.getGrupoTileReferenciado(this.areaTileSelected.x, this.areaTileSelected.y)
		    : this.MAPA.getTileReferenciado(this.areaTileSelected.x, this.areaTileSelected.y);
	    if (paleta instanceof PaletaTile) {
		final PaletaTile paletaTile = ((PaletaTile) paleta);
		final Tile tilePaletaSeleccionado = paletaTile.getTileSeleccionado();
		if (tilePaletaSeleccionado == null) {
		    return;
		}

		if (paletaTile.valoresYaEstalecidosPreviamente(tileTerrenoSeleccionado)) {
		    return;
		}
		if (this.areaTileSelected.equals(this.ultimaAreaTileAlterado)) {
		    return;
		}
		this.ultimaAreaTileAlterado = this.areaTileSelected;
		if (tileTerrenoSeleccionado instanceof GroupTile) {
		    final GroupTile gt = (GroupTile) tileTerrenoSeleccionado;
		    System.out.println("GrupoTile establecido en :" + this.areaTileSelected);
		    this.MAPA.establecerTileReferenciado(gt.getTile1().getPosicion(), tilePaletaSeleccionado);
		    this.MAPA.establecerTileReferenciado(gt.getTile2().getPosicion(), tilePaletaSeleccionado);
		    this.MAPA.establecerTileReferenciado(gt.getTile3().getPosicion(), tilePaletaSeleccionado);
		    this.MAPA.establecerTileReferenciado(gt.getTile4().getPosicion(), tilePaletaSeleccionado);
		} else {
		    this.MAPA.establecerTileReferenciado(new Point(this.areaTileSelected.x, this.areaTileSelected.y), tilePaletaSeleccionado);
		    System.out.println(this.MAPA.getTileReferenciado(new Point(this.areaTileSelected.x, this.areaTileSelected.y)));
		}

		// Paleta complemento
	    }

	    else if (paleta instanceof PaletaComplento) {
		final PaletaComplento paletaComplemento = ((PaletaComplento) paleta);
		if (this.areaTileSelected != null && paletaComplemento.getComplementoSeleccionado() != null) {
		    if (paletaComplemento.valoresYaEstalecidosPreviamente(tileTerrenoSeleccionado)) {
			return;
		    }
		    if (!this.GT_COLOCACION.transcurrioMiliSegundos(this.TIEMPO_ESPERA_MS_COLOCACION)) {
			return;
		    }
		    this.GT_COLOCACION.establecerReferenciaTiempoActual();
		    final int pos = paletaComplemento.getPosicionamientoActual();
		    final Complemento c = paletaComplemento.getComplementoSeleccionado();
		    final Rectangle rr = this.RATON.getRectanguloPosicionEscalado();// rectangulo raton
		    final Rectangle areaRaton = new Rectangle(rr.x + Constantes.CAMARA.getPosicionXInt() - Constantes.CAMARA.getMargenX() - (c.getTextura().getWidth() / 2),
			    rr.y + Constantes.CAMARA.getPosicionYInt() - Constantes.CAMARA.getMargenY() - (c.getTextura().getHeight() / 2), rr.width, rr.height);// rectangulo dinamico en base a los

		    /*
		     * VER DE HACER QUE EL COMPLEMENTO SE PUEDA CENTRAR EN LOS TILES PARA NO HACER
		     * SOLIDOS VARIOS. USANDO TAMBIEN LA OPCION DE LOS BOTONES N S E O
		     * 
		     */
		    switch (pos) {
		    case PaletaComplento.POSICIONAMIENTO_CENTRO:
			this.MUNDO_EDITOR.meterEntidad(new Complemento(areaRaton.x, areaRaton.y, c.getCodigoModelo()));
			break;
//					case PaletaComplento.POSICIONAMIENTO_SUPERIOR_IZQUIERDA:
//						tileMapaSeleccionado.agregarObjetoZonaSuperiorIzquierda(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_SUPERIOR_DERECHA:
//						tileMapaSeleccionado.agregarObjetoZonaSuperiorDerecha(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_INFERIOR_IZQUIERDA:
//						tileMapaSeleccionado.agregarObjetoZonaInferiorIzquierda(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_INFERIOR_DERECHA:
//						tileMapaSeleccionado.agregarObjetoZonaInferiorDerecha(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_NORTE:
//						tileMapaSeleccionado.agregarObjetoZonaNorte(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_SUR:
//						tileMapaSeleccionado.agregarObjetoZonaSur(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_ESTE:
//						tileMapaSeleccionado.agregarObjetoZonaEste(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
//					case PaletaComplento.POSICIONAMIENTO_OESTE:
//						tileMapaSeleccionado.agregarObjetoZonaOeste(paletaComplemento.getComplementoSeleccionado(), true);
//						break;
		    default:

		    }

		}
	    } //

	}

    }

    private void actualizarTileApuntado() {

	if (this.AREA_MOUSE_APUNTADO.x > (this.x + this.PALETA_MAPA.width)) { // verifico q no intersecte un rectangulo
									      // fuera del ancho de
	    this.areaTileSelected = null;
	    return;
	}
	final Tile t = Constantes.GLOBALES.editorSelectGroupTile ? this.MAPA.getGrupoTileReferenciado(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y)
		: this.MAPA.getTileReferenciado(this.AREA_MOUSE_APUNTADO.x, this.AREA_MOUSE_APUNTADO.y);
	if (t == null) {
	    this.areaTileSelected = null;
	    return;
	}
	this.areaTileSelected = new Rectangle(t.getPosicionX(), t.getPosicionY(), t.getLado(), t.getLado());

    }

    @Override
    public void pintar(final Graphics2D g) {
	this.pintarTerreno(g);
	this.MUNDO_EDITOR.pintar(g);
	this.pintarCoordenadas(g);
	this.pintarTileSelectedTerreno(g);
	this.PALETAS.pintar(g);
    }

    private void pintarCoordenadas(final Graphics2D g) {
	if (this.PALETAS.getPaletaActual() instanceof PaletaTile) {
	    DibujoDebug.dibujarString(g,
		    "Area Apuntada: " + (this.areaTileSelected != null
			    ? ("(X: " + this.areaTileSelected.x + " , Y: " + this.areaTileSelected.y + " , W: " + this.areaTileSelected.width + " , h: " + this.areaTileSelected.height)
			    : "none"),
		    20, 170, Color.white);

	} else {
	    DibujoDebug.dibujarString(g,
		    "Area Mouse Apuntado: "
			    + ("(X: " + this.AREA_MOUSE_APUNTADO.x + " , Y: " + this.AREA_MOUSE_APUNTADO.y + " , W: " + this.AREA_MOUSE_APUNTADO.width + " , h: " + this.AREA_MOUSE_APUNTADO.height),
		    20, 170, Color.white);
	    DibujoDebug.dibujarRectanguloContorno(g, this.RATON.getRectanguloPosicionEscalado(), Color.blue);
	}
	DibujoDebug.dibujarString(g, "X Centro: " + this.x, 20, 180, Color.green);
	DibujoDebug.dibujarString(g, "Y Centro: " + this.y, 20, 190, Color.green);

    }

    private void pintarTileSelectedTerreno(final Graphics2D g) {
	if (this.areaTileSelected != null && this.PALETAS.getPaletaActual() instanceof PaletaTile) {
	    DibujoDebug.dibujarRectanguloContorno(g, this.areaTileSelected.x - Constantes.CAMARA.getPosicionXInt() + Constantes.CAMARA.getMargenX(),
		    this.areaTileSelected.y - Constantes.CAMARA.getPosicionYInt() + Constantes.CAMARA.getMargenY(), this.areaTileSelected.width, this.areaTileSelected.height, Color.magenta);
	} else if (this.areaTileSelected != null && this.PALETAS.getPaletaActual() instanceof PaletaComplento) {
	    final PaletaComplento p = (PaletaComplento) this.PALETAS.getPaletaActual();
	    if (p.getComplementoSeleccionado() != null) {
		final Rectangle rr = this.RATON.getRectanguloPosicionEscalado();// rectangulo raton

		DibujoDebug.dibujarImagen(g, p.getComplementoSeleccionado().getTextura(), rr.x - p.getComplementoSeleccionado().getTextura().getWidth() / 2,
			rr.y - p.getComplementoSeleccionado().getTextura().getHeight() / 2);
		if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado()) {
		    if (ListaModeloComplemento.getModeloComplemento(p.getComplementoSeleccionado().getCodigoModelo()) instanceof ModeloComplementoT1) {
			final ModeloComplementoT1 modelo = (ModeloComplementoT1) ListaModeloComplemento.getModeloComplemento(p.getComplementoSeleccionado().getCodigoModelo());
			final Rectangle colision = modelo.getMargenesInterseccionEnBasePosicion(rr.x - p.getComplementoSeleccionado().getTextura().getWidth() / 2,
				rr.y - p.getComplementoSeleccionado().getTextura().getHeight() / 2);
			DibujoDebug.dibujarRectanguloContorno(g, colision, Color.yellow);
		    } else if (ListaModeloComplemento.getModeloComplemento(p.getComplementoSeleccionado().getCodigoModelo()) instanceof ModeloComplementoT2) {
			final ModeloComplementoT2 modelo = (ModeloComplementoT2) ListaModeloComplemento.getModeloComplemento(p.getComplementoSeleccionado().getCodigoModelo());
			for (final Rectangle colision : modelo.getMargenesInterseccionEnBasePosicion(rr.x - p.getComplementoSeleccionado().getTextura().getWidth() / 2,
				rr.y - p.getComplementoSeleccionado().getTextura().getHeight() / 2)) {
			    DibujoDebug.dibujarRectanguloContorno(g, colision, Color.yellow);
			}
		    }
		}
	    }

	}
    }

    private void pintarTerreno(final Graphics2D g) {
	final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - this.LADO_GRUPO_TILE;
	final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + this.LADO_GRUPO_TILE - this.PALETAS.AREA.width;

	final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - this.LADO_GRUPO_TILE;
	final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + this.LADO_GRUPO_TILE;
	boolean contieneEnY = false;
	GroupTile gt = null;
	int px = puntoX;
	int py = puntoY;
	if (puntoX < 0 && limiteX > 0) {
	    px = 0;
	}
	if (puntoY < 0 && limiteY > 0) {
	    py = 0;
	}

	for (int y = py; y < limiteY;) {

	    for (int x = px; x < limiteX;) {

		if ((gt = this.MAPA.getGrupoTileReferenciado(x, y)) != null) {
		    gt.pintarEditor(g);
		    gt = null;
		    x += this.LADO_GRUPO_TILE;
		    if (!contieneEnY) {
			contieneEnY = true;
		    }

		} else {
		    x++;
		}

	    }
	    if (contieneEnY) {
		y += this.LADO_GRUPO_TILE;
	    } else {
		y++;
	    }
	}
    }

    private void mover() {
	int velocidad = 1;
	if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
	    velocidad = 8;
	}
	if (Constantes.TECLADO.TECLA_ARRIBA.presionado()) {
	    if (this.y - velocidad >= 0) {
		this.y -= velocidad;
		Constantes.JUGADOR.establecerPosicion(this.x, this.y);
	    }

	}

	if (Constantes.TECLADO.TECLA_ABAJO.presionado()) {
	    if (this.y + velocidad <= this.ALTO - Constantes.ALTO_JUEGO / 2) {
		this.y += velocidad;
		Constantes.JUGADOR.establecerPosicion(this.x, this.y);
	    }

	}

	if (Constantes.TECLADO.TECLA_IZQUIERDA.presionado()) {
	    if (this.x - velocidad >= 0) {
		this.x -= velocidad;
		Constantes.JUGADOR.establecerPosicion(this.x, this.y);
	    }

	}

	if (Constantes.TECLADO.TECLA_DERECHA.presionado()) {
	    if (this.x + velocidad <= this.ANCHO - this.PALETAS.AREA.width) {
		this.x += velocidad;
		Constantes.JUGADOR.establecerPosicion(this.x, this.y);
	    }
	}
    }

    public void guardarMapa(final String nombre) {
	final JSONObject jsonEntes = this.MUNDO_EDITOR.getEntesInJson();
	final String criaturas = Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class);
	final String items = Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class);
	final String complementos = Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class);
	final String objetos = Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class);

	final Escenario esc = new Escenario(this.MAPA, jsonEntes.get(criaturas).toString(), jsonEntes.get(items).toString(), jsonEntes.get(complementos).toString(), jsonEntes.get(objetos).toString());
	EscenarioLoader.exportarEscenario(esc, new File("mundos\\" + nombre));
    }

}
