package principal.mapa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.neutrales.CosaNeutral;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.particulas.Particula;
import principal.entes.proyectil.Proyectil;
import principal.entes.proyectil.ProyectilGeneral;
import principal.mapa.escenario.Escenario;
import principal.mapa.renderEntidades.MapRender;
import principal.mapa.renderEntidades.RenderEntidad;
import principal.mapa.renderEntidades.ZoneBox;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.dijkstra.Dijkstra;
import principal.utilidades.dijkstra.Nodo;

public class Mundo {
	protected final Escenario ESCENARIO;
	protected final Point PUNTO_COMIENZO;
	protected final int LADO_ZONEBOX = 64;
	protected final MapRender RENDERS;
	
	private boolean auxCrear;
	/*
	 * LAS ZONAS SE PODRIAN SEPARAR EN ZONAS DE ITEM, CRIATURAS, ETC. SEGUN SEA NECESARIO?
	 */
	protected final HashMap<Point, ZoneBox> ZONAS = new HashMap<Point, ZoneBox>();
	protected final ArrayList<Particula> PARTICULAS = new ArrayList<Particula>();
	protected final ArrayList<Proyectil> PROYECTILES = new ArrayList<Proyectil>();
	protected final Dijkstra dijkstra;
	protected int codAct;
	protected int codPintado;

	public Mundo(final Escenario esc, final Point comienzo) {
		this.ESCENARIO = esc;
		this.generarZonas();
		this.RENDERS = new MapRender(this);
		for(Item i : this.ESCENARIO.generarItemsEnMapa()) {
			this.meterEntidad(i);
		}
		esc.generarListaComplementos(this);
		int cantCriaturas = this.generarCriaturas(esc.generarListaCriaturas(this));
		int cantObjetos = this.ESCENARIO.generarObjetosEnMapa(this);
		
		this.PUNTO_COMIENZO = comienzo;
		this.dijkstra = new Dijkstra(this.ESCENARIO.getMapa().getTILES(), this.ESCENARIO.getMapa().ladoTile(), (this.ESCENARIO.getMapa().getAncho() - this.ESCENARIO.getMapa().ladoTile()), (this.ESCENARIO.getMapa().getAlto() - this.ESCENARIO.getMapa().ladoTile()));
		this.dijkstra.actualizar(Constantes.JUGADOR.getPosicionTile());
	}
	
	public Mundo(final Escenario esc, final Point comienzo, final GestorCarga gc, final int porcentajeCarga) {
		this.ESCENARIO = esc;
		//////////////////////
		int pesoCarga = 25;
		gc.setDetalleCarga("Generando render zonas");
		this.generarZonas();
		gc.setPorcentajeCarga(gc.getPorcentaje() + pesoCarga*porcentajeCarga/100);
		/////////////////////
		this.RENDERS = new MapRender(this);
		pesoCarga = 15;
		gc.setDetalleCarga("Generando items");
		for(Item i : this.ESCENARIO.generarItemsEnMapa()) {
			this.meterEntidad(i);
		}
		gc.setPorcentajeCarga(gc.getPorcentaje() + pesoCarga*porcentajeCarga/100);
		/////////////////////
		pesoCarga = 35;
		gc.setDetalleCarga("Generando complementos");
		esc.generarListaComplementos(this);
		gc.setPorcentajeCarga(gc.getPorcentaje() + pesoCarga*porcentajeCarga/100);
		/////////////
		pesoCarga = 15;
		gc.setDetalleCarga("Generando criaturas");
		int cantCriaturas = this.generarCriaturas(esc.generarListaCriaturas(this));
		gc.setPorcentajeCarga(gc.getPorcentaje() + pesoCarga*porcentajeCarga/100);
		////////////////////
		pesoCarga = 10;
		gc.setDetalleCarga("Generando objetos");
		int cantObjetos = this.ESCENARIO.generarObjetosEnMapa(this);
		gc.setPorcentajeCarga(gc.getPorcentaje() + pesoCarga*porcentajeCarga/100);
		//////////////////
		this.PUNTO_COMIENZO = comienzo;
		this.dijkstra = new Dijkstra(this.ESCENARIO.getMapa().getTILES(), this.ESCENARIO.getMapa().ladoTile(), (this.ESCENARIO.getMapa().getAncho() - this.ESCENARIO.getMapa().ladoTile()), (this.ESCENARIO.getMapa().getAlto() - this.ESCENARIO.getMapa().ladoTile()));
		this.dijkstra.actualizar(Constantes.JUGADOR.getPosicionTile());
	}
	
	public Mundo(final Mapa mapaSoloParaEDITOR) {
			this.ESCENARIO = new Escenario(mapaSoloParaEDITOR, "[]", "[]", "[]", "[]");
			this.RENDERS = new MapRender(this);
			this.PUNTO_COMIENZO = new Point();
			this.dijkstra = new Dijkstra(this.ESCENARIO.getMapa().getTILES(), this.ESCENARIO.getMapa().ladoTile(), (this.ESCENARIO.getMapa().getAncho() - this.ESCENARIO.getMapa().ladoTile()), (this.ESCENARIO.getMapa().getAlto() - this.ESCENARIO.getMapa().ladoTile()));
			this.generarZonas();
	}

	public void actualizar() {
		
		if(Constantes.TECLADO.TECLA_PUNTO.presionado() && auxCrear) {
			double x = Constantes.JUGADOR.getPosicionX();
			double y = Constantes.JUGADOR.getPosicionY();
			/*
			 * REVISAR BUG CON COSA NEUTRAL
			 */
//			final Ente cn = new Enemigo(x, y, 16, 16, 100, 200, CargadorRecursos.cargarImagenCompatibleTranslucida("/imagenes/sprites/jugadores.png").getSubimage(48, 48, 48, 48));
			final Ente cn = new CosaNeutral(x, y, 6, 6, Color.black, this.getMapa(), 0.25);
			this.meterEntidad(cn);
			System.out.println("CosaNeutral creada en X:" +x+" y:"+y+" , "+cn);
			auxCrear = false;
		}else if(!Constantes.TECLADO.TECLA_PUNTO.presionado() && !auxCrear) {
			auxCrear = true;
		}
		this.actualizarDijkstra();
		this.getMapa().actualizarZonas(ZONAS, LADO_ZONEBOX);
		this.actualizarParticulas();
		this.actualizarProyectiles();
		this.updateNextCodAct();
	}

	public void pintar(final Graphics2D g) {
		this.ESCENARIO.getMapa().pintar(g);
		this.pintarParticulas(g);
		this.getMapa().pintarZonas(g, ZONAS, LADO_ZONEBOX);
		this.pintarProyectiles(g);
		if (Constantes.TECLADO.TECLA_DIJKSTRA_INFO.presionado()) {
			pintarNodosOptimizado(g);
		}
		
		this.updateNextCodPintado();
	}
	
	public boolean meterEntidad(final Ente e) {
		
		if (this.RENDERS.containsKey(e)) {
			return false;
		}
		if(!this.getMapa().AreaDentroDelMapa(e.getArea())) {
			return false;
		}
		boolean exito = false;

		RenderEntidad re = new RenderEntidad(e, this);
		for (ZoneBox zb : this.getZonasIntersectadas(e)) {
			if (re.contieneZona(zb)) {
				continue;
			}
			re.meterZoneBox(zb);
			zb.addEntidad(e);
			if (exito != true) {
				exito = true;
			}
		}

		if (exito) {
			e.setMundo(this);
			this.RENDERS.meterEntidad(re);
			if(e instanceof Objeto && ((Objeto)e).esSolido()) {
				this.objetoSolidoVerificarTile((Objeto)e);
			}
			if(Constantes.isEstadoEditor())
				System.out.println("entidad "+e+" agregada en el punto x: "+e.getPosicionXInt()+" , y: "+e.getPosicionYInt());
		}

		return exito;
	}
	
	public MapRender getRenders() {
		return this.RENDERS;
	}
	
	public HashMap<Point, ZoneBox> getZonas(){
		return this.ZONAS;
	}

	public ArrayList<ZoneBox> getZonasIntersectadas(final Ente e) {
		final ArrayList<ZoneBox> zonas = new ArrayList<ZoneBox>();

		final int x = (int) e.getArea().x;
		final int y = (int) e.getArea().y;
		final int ancho = (int) e.getArea().width;
		final int alto = (int) e.getArea().height;

		ZoneBox zb = null;
		// zona 1
		zb = this.getZonaPuntoReferido(x, y);
		if (zb != null) {
			if (zb.getArea().intersects(e.getArea())) {
				zonas.add(zb);
			}
		}

		// zona 2
		zb = this.getZonaPuntoReferido(x + ancho, y);
		if (zb != null) {
			if (zb.getArea().intersects(e.getArea())) {
				zonas.add(zb);
			}
		}

		// zona 3
		zb = this.getZonaPuntoReferido(x, y + alto);
		if (zb != null) {
			if (zb.getArea().intersects(e.getArea())) {
				zonas.add(zb);
			}
		}

		// zona 4
		zb = this.getZonaPuntoReferido(x + ancho, y + alto);
		if (zb != null) {
			if (zb.getArea().intersects(e.getArea())) {
				zonas.add(zb);
			}
		}
		return zonas;
	}
	
	
	public int getCodAct() {
		return this.codAct;
	}
	
	public int getCodPintado() {
		return this.codPintado;
	}
	
	public HashSet<Item> getItemsIntersectados(final Shape area){
		final Rectangle rArea = area.getBounds();
		HashSet<Item> lista = new HashSet<Item>();
		if(!this.getMapa().AreaDentroDelMapa(rArea)) {
			System.out.println("area Afuera del mapa");
			return lista;
			
		}
		
		ArrayList<ZoneBox> listaZonas = new ArrayList<ZoneBox>();
		ZoneBox zona = this.getZonaPuntoReferido(rArea.x, rArea.y);
		if(zona != null) {
			listaZonas.add(zona);
			lista.addAll(zona.getItemsIntersectados(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getItemsIntersectados(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x, rArea.y+ rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getItemsIntersectados(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getItemsIntersectados(area));
			zona = null;
		}
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getItemsIntersectados(area));
			zona = null;
		}
		
		
		return lista;
	}
	
	public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area, final boolean tenerEnCuentaJugador){
		final Rectangle rArea = area.getBounds();
		ArrayList<Criatura> lista = new ArrayList<Criatura>();
		if(!this.getMapa().AreaDentroDelMapa(rArea)) {
			return lista;
		}
		if(tenerEnCuentaJugador && area.intersects(Constantes.JUGADOR.getArea())) {
			lista.add(Constantes.JUGADOR);
		}
		
		ArrayList<ZoneBox> listaZonas = new ArrayList<ZoneBox>();
		ZoneBox zona = this.getZonaPuntoReferido(rArea.x, rArea.y);
		if(zona != null) {
			listaZonas.add(zona);
			lista.addAll(zona.getCriaturasIntersectadas(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getCriaturasIntersectadas(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x, rArea.y+ rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getCriaturasIntersectadas(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getCriaturasIntersectadas(area));
			zona = null;
		}
		
		
		return lista;
	}
	
	public ArrayList<Ente> getEnteIntersectados(final Shape area, final boolean tenerEnCuentaJugador){
		final Rectangle rArea = area.getBounds();
		ArrayList<Ente> lista = new ArrayList<Ente>();
		if(!this.getMapa().AreaDentroDelMapa(rArea)) {
			return lista;
		}
		if(tenerEnCuentaJugador && area.intersects(Constantes.JUGADOR.getArea())) {
			lista.add(Constantes.JUGADOR);
		}
		
		ArrayList<ZoneBox> listaZonas = new ArrayList<ZoneBox>();
		ZoneBox zona = this.getZonaPuntoReferido(rArea.x, rArea.y);
		if(zona != null) {
			listaZonas.add(zona);
			lista.addAll(zona.getEntesIntersectados(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getEntesIntersectados(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x, rArea.y+ rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getEntesIntersectados(area));
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			lista.addAll(zona.getEntesIntersectados(area));
			zona = null;
		}
		
		for(Proyectil p : this.PROYECTILES) {
			if(area.intersects(p.getArea())) {
				lista.add(p);
			}
		}
		for(Proyectil p : this.PROYECTILES) {
			if(area.intersects(p.getArea())) {
				lista.add(p);
			}
		}
		
		
		return lista;
	}
	
	public boolean intersectaAlgunaCriatura(final Shape area, final boolean tenerEnCuentaJugador){
		final Rectangle rArea = area.getBounds();
		if(!this.getMapa().AreaDentroDelMapa(rArea)) {
			return false;
		}
		if(tenerEnCuentaJugador && area.intersects(Constantes.JUGADOR.getArea())) {
			return true;
		}
		
		ArrayList<ZoneBox> listaZonas = new ArrayList<ZoneBox>();
		ZoneBox zona = this.getZonaPuntoReferido(rArea.x, rArea.y);
		if(zona != null) {
			listaZonas.add(zona);
			if(zona.intersectaAlgunaCriatura(area)) return true;
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			if(zona.intersectaAlgunaCriatura(area)) return true;
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x, rArea.y+ rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			if(zona.intersectaAlgunaCriatura(area)) return true;
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona != null && !listaZonas.contains(zona)) {
			listaZonas.add(zona);
			if(zona.intersectaAlgunaCriatura(area)) return true;
			zona = null;
		}
		
		return false;
	}
	
	
	public boolean colisionaConObjetoSolido(final Shape area){
		final Rectangle rArea = area.getBounds();
		if(!this.getMapa().AreaDentroDelMapa(rArea)) {
			return false;
		}
		
		ZoneBox zona = this.getZonaPuntoReferido(rArea.x, rArea.y);
		if(zona != null) {
			if(zona.intersectaObjetoSolido(area)) {
				return true;
			}
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y);
		if(zona.intersectaObjetoSolido(area)) {
			return true;
		}
		zona = null;
		
		zona = this.getZonaPuntoReferido(rArea.x, rArea.y+ rArea.height);
		if(zona.intersectaObjetoSolido(area)) {
			return true;
		}
		zona = null;
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona.intersectaObjetoSolido(area)) {
			return true;
		}
		zona = null;
		
		
		return false;
	}
	
	public boolean colisionaConObjetoSolidoPeroEnZonaNoSolida(final Shape area){
		final Rectangle rArea = area.getBounds();
		if(!this.getMapa().AreaDentroDelMapa(rArea)) {
			return false;
		}
		
		ZoneBox zona = this.getZonaPuntoReferido(rArea.x, rArea.y);
		if(zona != null) {
			if(zona.intersectaAreaNoSolidaDeAlgunComplemento(area)) {
				return true;
			}
			zona = null;
		}
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y);
		if(zona.intersectaAreaNoSolidaDeAlgunComplemento(area)) {
			return true;
		}
		zona = null;
		
		zona = this.getZonaPuntoReferido(rArea.x, rArea.y+ rArea.height);
		if(zona.intersectaAreaNoSolidaDeAlgunComplemento(area)) {
			return true;
		}
		zona = null;
		
		zona = this.getZonaPuntoReferido(rArea.x+rArea.width, rArea.y+rArea.height);
		if(zona.intersectaAreaNoSolidaDeAlgunComplemento(area)) {
			return true;
		}
		zona = null;
		return false;
	}
	
	public boolean agregarItemEnPosicionJugador(final Item item, final boolean copiar) {
		final int x = Constantes.JUGADOR.getPosicionXParado();
		final int y = Constantes.JUGADOR.getPosicionYParado();
		item.setPosicion(x, y);
		
		if(copiar) {
			return this.meterEntidad(item.copiar());
		}else {
			return this.meterEntidad(item);
		}
	}
	
	
	//VER ACA
	private void objetoSolidoVerificarTile(final Objeto obj) {
		if(obj instanceof Complemento) {
			final Complemento c = (Complemento)obj;
			if(ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo()) instanceof ModeloComplementoT1) {
				this.objetoSolidoVerificarTileByArea(c, c.getAreaInterseccionEnBaseMargen(((ModeloComplementoT1)ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo())).getMargenesInterseccion()));
			}else if(ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo()) instanceof ModeloComplementoT2) {
				for(Rectangle margen : ((ModeloComplementoT2)ListaModeloComplemento.getModeloComplemento(c.getCodigoModelo())).getMargenesInterseccion()) {
					this.objetoSolidoVerificarTileByArea(c, c.getAreaInterseccionEnBaseMargen(margen));
				}
			}
		}else {
			this.objetoSolidoVerificarTileByArea(obj, obj.getArea());
		}
		
		
	}
	
	private void objetoSolidoVerificarTileByArea(final Objeto obj, final Rectangle area) {
		GroupTile gt = this.getMapa().getGrupoTileReferenciado(area.x, area.y);
		if(gt != null) {
			for(Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}
		
		gt = this.getMapa().getGrupoTileReferenciado(area.x+area.width, area.y);
		if(gt != null) {
			for(Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}
		
		gt = this.getMapa().getGrupoTileReferenciado(area.x, area.y+ area.height);
		if(gt != null) {
			for(Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}
		
		gt = this.getMapa().getGrupoTileReferenciado(area.x+area.width, area.y+area.height);
		if(gt != null) {
			for(Tile t : gt.getTiles()) {
				t.meterObjetoSolido(obj);
			}
			gt = null;
		}
	}
	
	private int generarCriaturas(final ArrayList<Criatura> criaturas) {
		int cant = 0;
		for(Criatura c : criaturas) {
			this.meterEntidad(c);
			++cant;
		}
		return cant;
	}
	
	private void generarZonas() {
		this.ZONAS.clear();
		int x;
		int y;
		final int limiteY = this.ESCENARIO.getMapa().CANTIDAD_ALTO_GROUPTILE * (this.ESCENARIO.getMapa().LADO_GRUPO_TILE);
		final int limiteX = this.ESCENARIO.getMapa().CANTIDAD_ANCHO_GROUPTILE * (this.ESCENARIO.getMapa().LADO_GRUPO_TILE);
		for (y = 0; y < limiteY; y += this.LADO_ZONEBOX) {
			for (x = 0; x < limiteX; x += this.LADO_ZONEBOX) {
				this.ZONAS.put(new Point(x / LADO_ZONEBOX, y / LADO_ZONEBOX),
						new ZoneBox(x, y, this.LADO_ZONEBOX, this.LADO_ZONEBOX, this));
			}
		}
	}
	
	private ZoneBox getZonaPuntoReferido(final float x, final float y) {
		return this.ZONAS.get(new Point((int) x / LADO_ZONEBOX, (int) y / LADO_ZONEBOX));
	}
	
	
	public void updateNextCodAct() {
		if (this.codAct < Integer.MAX_VALUE) {
			this.codAct++;
		} else {
			this.codAct = Integer.MIN_VALUE;
		}
	}
	
	public void updateNextCodPintado() {
		if (this.codPintado < Integer.MAX_VALUE) {
			this.codPintado++;
		} else {
			this.codPintado = Integer.MIN_VALUE;
		}
	}
	
	private void pintarParticulas(final Graphics2D g) {
		if (this.PARTICULAS.size() == 0) {
			return;
		}
		for (Particula p : this.PARTICULAS) {
			p.pintar(g);
		}
	}
	
	private void pintarProyectiles(final Graphics2D g) {
		if(this.PROYECTILES.isEmpty()) {
			return;
		}
		
		Proyectil p = null;
		for (int i = 0; i < this.PROYECTILES.size(); i++) {
			p = this.PROYECTILES.get(i);
			p.pintar(g);
		}
	}


	private void pintarNodosOptimizado(final Graphics2D g) {
		float tama = g.getFont().getSize2D();
		g.setFont(g.getFont().deriveFont(6f));
		final Color color = Constantes.TECLADO.TECLA_OCULTAR_TERRENO.presionado() ? Color.WHITE : Color.BLACK;

		final int lado = Constantes.LADO_TILE;
		final int puntoX = Constantes.CAMARA.getPosicionXInt() - Constantes.CENTROX - (3 * Constantes.LADO_TILE);
		final int limiteX = Constantes.CAMARA.getPosicionXInt() + Constantes.CENTROX + (3 * Constantes.LADO_TILE);

		final int puntoY = Constantes.CAMARA.getPosicionYInt() - Constantes.CENTROY - (3 * Constantes.LADO_TILE);
		final int limiteY = Constantes.CAMARA.getPosicionYInt() + Constantes.CENTROY + (3 * Constantes.LADO_TILE);
		boolean contieneEnY = false;

		Nodo nodo = null;
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
				if ((nodo = this.dijkstra.getNodoReferenciado(x, y)) != null) {

					final int puntoXNodo = (nodo.POSICION_TILE.x * lado) - Constantes.CAMARA.getPosicionXInt() + (Constantes.CAMARA.getMargenX());
					final int puntoYNodo = (nodo.POSICION_TILE.y * lado) - Constantes.CAMARA.getPosicionYInt() + (Constantes.CAMARA.getMargenY());
					DibujoDebug.dibujarString(g, (nodo.distancia == Double.MAX_VALUE ? "MAX" : String.format("%.2f", nodo.distancia)), puntoXNodo, puntoYNodo + 10, color);
					x += Constantes.LADO_TILE;
					if (!contieneEnY) {
						contieneEnY = true;
					}
				} else {
					x++;
				}
			}
			if (contieneEnY) {
				y += Constantes.LADO_TILE;
			} else {
				y++;
			}
		}
		
		g.setFont(g.getFont().deriveFont(tama));
	}

	private void actualizarDijkstra() {
		if (Constantes.TECLADO.TECLA_DIJKSTRA.presionado() && this.dijkstra.actualizarDijkstra()) {
			this.dijkstra.actualizar(Constantes.JUGADOR.getPosicionTileParado());
		}

	}

	private void actualizarParticulas() {
		if (this.PARTICULAS.size() == 0) {
			return;
		}
		Particula p = null;
		for (int i = 0; i < this.PARTICULAS.size(); i++) {
			p = this.PARTICULAS.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PARTICULAS.remove(i);
			}
		}

	}
	
	private void actualizarProyectiles() {
		if(this.PROYECTILES.size()==0) {
			return;
		}
		
		Proyectil p = null;
		for (int i = 0; i < this.PROYECTILES.size(); i++) {
			p = this.PROYECTILES.get(i);
			p.actualizar();
			if (p.estaEliminado()) {
				this.PROYECTILES.remove(i);
				System.out.println("remove proyectil: "+p.getClass().getName());
			}
		}
	}
	
	public long getCantEntidadesEnMapa() {
		return this.RENDERS.getCantEntidades();
	}

	public long getCantEntidadesTotal() {
		return this.RENDERS.getCantEntidades() + this.ESCENARIO.getMapa().getCantidadTiles();
	}

	public void moverJugadorPuntoComienzo() {
		Constantes.JUGADOR.establecerPosicion(this.PUNTO_COMIENZO.x, this.PUNTO_COMIENZO.y);
	}

	public void agregarParticula(final Particula p) {
		this.PARTICULAS.add(p);
	}

	public void agregarCriatura(final Criatura c) {
		this.meterEntidad(c);
	}

	public Mapa getMapa() {
		return this.ESCENARIO.getMapa();
	}
	
	public int getLadoZoneBox() {
		return this.LADO_ZONEBOX;
	}

	public ArrayList<Criatura> getCriaturasIntersectadasConEnte(final Ente e) {
		ArrayList<Criatura> criaturas = new ArrayList<Criatura>();
		for(ZoneBox zb : getZonasIntersectadas(e)) {
			for(Criatura c : zb.getCriaturas()) {
				if(!criaturas.contains(c)) {
					criaturas.add(c);
				}
			}
		}
		return criaturas;
	}
	
	public HashSet<Ente> getEntes(){
		return this.RENDERS.getEntes();
	}

	public Dijkstra getDijkstra() {
		return this.dijkstra;
	}
	
	public void crearProyectil(final int damage, final double velocidad, final boolean penetrante, final int alcance, final double x, final double y , final int ancho , final int alto, final Direccion direccion, final Criatura causante) {
		this.PROYECTILES.add(new ProyectilGeneral(damage,velocidad,penetrante,alcance,this,x,y,ancho,alto,direccion,causante));
	}
	
	public void crearProyectil(final Proyectil proyectil) {
		this.PROYECTILES.add(proyectil);
	}
	

}
