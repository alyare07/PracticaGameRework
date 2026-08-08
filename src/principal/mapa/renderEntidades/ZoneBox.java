package principal.mapa.renderEntidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Item;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.ZonaTP;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class ZoneBox extends Ente{
    protected final Rectangle AREA;
    protected final ArrayList<Criatura> CRIATURAS = new ArrayList<Criatura>();
    protected final ArrayList<Item> ITEMS = new ArrayList<Item>();
    protected final ArrayList<Cofre> COFRES = new ArrayList<Cofre>();
    protected final ArrayList<Complemento> COMPLEMENTOS = new ArrayList<Complemento>();
    protected final ArrayList<ZonaTP> ZONAS_TP = new ArrayList<ZonaTP>();
    protected final Mundo mundo;

    public ZoneBox(final int x, final int y, final int ancho, final int alto, final Mundo mundo) {
	this.AREA = new Rectangle(x, y, ancho, alto);
	this.mundo = mundo;
    }

    @Override
    public void actualizar() {
	if (Constantes.isEstadoEditor()) {
	    return;
	}
	RenderEntidad re = null;

	for (int pos = 0; pos < this.COFRES.size(); pos++) {
	    re = this.mundo.getRenders().getRender(this.COFRES.get(pos));
	    if (!re.estaRenderizado()) {
		this.COFRES.get(pos).actualizar();
		re.update();
		re.renderizado();
	    }
	}

	for (int pos = 0; pos < this.ITEMS.size(); pos++) {
	    re = this.mundo.getRenders().getRender(this.ITEMS.get(pos));
	    if (!re.estaRenderizado()) {
		this.ITEMS.get(pos).actualizar();
		re.update();
		re.renderizado();
	    }
	}

	for (int pos = 0; pos < this.CRIATURAS.size(); pos++) {
	    re = this.mundo.getRenders().getRender(this.CRIATURAS.get(pos));
	    if (!re.estaRenderizado()) {
		this.CRIATURAS.get(pos).actualizar();
		re.update();
		re.renderizado();
	    }
	}

	for (int pos = 0; pos < this.ZONAS_TP.size(); pos++) {
	    re = this.mundo.getRenders().getRender(this.ZONAS_TP.get(pos));
	    if (!re.estaRenderizado()) {
		this.ZONAS_TP.get(pos).actualizar();
		re.update();
		re.renderizado();
	    }
	}

	/*
	 * AGREGAR UN ACTUALIZAR PARA COMPLEMENTOS???
	 */

    }

    @Override
    public void pintar(final Graphics2D g) {
	RenderEntidad re = null;
	for (final Complemento c : this.COMPLEMENTOS) {
	    re = this.mundo.getRenders().getRender(c);
	    if (!re.estaPintado()) {
		c.pintar(g);
		re.pintado();
	    }
	}

	for (final Cofre c : this.COFRES) {
	    re = this.mundo.getRenders().getRender(c);
	    if (!re.estaPintado()) {
		c.pintar(g);
		re.pintado();
	    }
	}
	for (final Item i : this.ITEMS) {
	    re = this.mundo.getRenders().getRender(i);
	    if (!re.estaPintado()) {
		i.pintar(g);
		re.pintado();
	    }
	}
	for (final Criatura c : this.CRIATURAS) {
	    re = this.mundo.getRenders().getRender(c);
	    if (!re.estaPintado()) {
		c.pintar(g);
		re.pintado();
	    }
	}

	for (final ZonaTP zonaTP : this.ZONAS_TP) {
	    re = this.mundo.getRenders().getRender(zonaTP);
	    if (!re.estaPintado()) {
		zonaTP.pintar(g);
		re.pintado();
	    }
	}

    }

    public void addEntidad(final Ente e) {
	if (e instanceof Criatura) {
	    this.CRIATURAS.add((Criatura) e);
	} else if (e instanceof Item) {
	    this.ITEMS.add((Item) e);
	} else if (e instanceof Cofre) {
	    this.COFRES.add((Cofre) e);
	}
	if (e instanceof Complemento) {
	    this.COMPLEMENTOS.add((Complemento) e);
	} else if (e instanceof ZonaTP) {
	    this.ZONAS_TP.add((ZonaTP) e);
	}
    }

    public ArrayList<Criatura> getCriaturas() {
	return this.CRIATURAS;
    }

    public ArrayList<Item> getItems() {
	return this.ITEMS;
    }
    
    public ArrayList<Cofre> getCofres(){
		return this.COFRES;
	}

    public void eliminarEntidad(final Ente e) {
	if (e instanceof Criatura) {
	    this.CRIATURAS.remove(e);
	} else if (e instanceof Item) {
	    this.ITEMS.remove(e);
	}

    }

    @Override
    public void eliminar() {

    }

    @Override
    public int getPosicionXInt() {
	return this.AREA.x;
    }

    @Override
    public int getPosicionYInt() {
	return this.AREA.y;
    }

    @Override
    public double getPosicionX() {
	return this.AREA.x;
    }

    @Override
    public double getPosicionY() {
	return this.AREA.y;
    }

    @Override
    public void modificarPosicionX(final double desplazamientoX) {

    }

    @Override
    public void modificarPosicionY(final double desplazamientoY) {

    }

    @Override
    public boolean estaEliminado() {
	return false;
    }

    @Override
    public Rectangle getArea() {
	return this.AREA;
    }

    public ArrayList<Ente> getEntesIntersectados(final Shape area) {
	final ArrayList<Ente> lista = new ArrayList<Ente>();
	if (!this.intersectaZona(area)) {
	    return lista;
	}

	for (final Criatura c : this.CRIATURAS) {
	    if (area.intersects(c.getArea())) {
		lista.add(c);
		break;
	    }
	}

	for (final Item i : this.ITEMS) {
	    if (area.intersects(i.getArea())) {
		lista.add(i);
	    }
	}

	for (final Cofre c : this.COFRES) {
	    if (area.intersects(c.getArea())) {
		lista.add(c);
	    }
	}

	return lista;
    }

    public ArrayList<Item> getItemsIntersectados(final Shape area) {
	final ArrayList<Item> lista = new ArrayList<Item>();
	if (!this.intersectaZona(area)) {
	    return lista;
	}

	for (final Item i : this.ITEMS) {
	    if (area.intersects(i.getArea())) {
		lista.add(i);
	    }
	}

	return lista;
    }

    public ArrayList<Criatura> getCriaturasIntersectadas(final Shape area) {
	final ArrayList<Criatura> lista = new ArrayList<Criatura>();
	if (!this.intersectaZona(area)) {
	    return lista;
	}

	for (final Criatura c : this.CRIATURAS) {
	    if (area.intersects(c.getArea())) {
		lista.add(c);
	    }
	}

	return lista;
    }

    public ArrayList<Complemento> getComplementosIntersectados(final Shape area) {
	final ArrayList<Complemento> lista = new ArrayList<Complemento>();
	if (!this.intersectaZona(area)) {
	    return lista;
	}

	for (final Complemento c : this.COMPLEMENTOS) {
	    if (area.intersects(c.getArea())) {
		lista.add(c);
	    }
	}

	return lista;
    }

    public boolean intersectaAlgunComplemento(final Shape area) {
	if (!this.intersectaZona(area)) {
	    return false;
	}
	for (final Complemento c : this.COMPLEMENTOS) {
	    if (area.intersects(c.getArea())) {
		return true;
	    }
	}
	return false;
    }

    public boolean intersectaAlgunaCriatura(final Shape area) {
	if (!this.intersectaZona(area)) {
	    return false;
	}
	for (final Criatura c : this.CRIATURAS) {
	    if (area.intersects(c.getArea())) {
		return true;
	    }
	}

	return false;
    }

    public boolean intersectaAlgunItem(final Shape area) {
	if (!this.intersectaZona(area)) {
	    return false;
	}
	for (final Item i : this.ITEMS) {
	    if (area.intersects(i.getArea())) {
		return true;
	    }
	}
	return false;
    }

    public boolean intersectaObjetoSolido(final Shape area) {
	if (!this.intersectaZona(area)) {
	    return false;
	}
	for (final Complemento c : this.COMPLEMENTOS) {
	    if (c.intersecta(area) && c.esSolido()) {
		return true;
	    }
	}
	for (final Cofre c : this.COFRES) {
	    if (area.intersects(c.getArea()) && c.esSolido()) {
		return true;
	    }
	}
	return false;
    }

    public boolean intersectaObjetoSolidoPermanente(final Shape area) {
	if (!this.intersectaZona(area)) {
	    return false;
	}
	for (final Complemento c : this.COMPLEMENTOS) {
	    if (c.intersecta(area) && c.esSolido()) {
		return true;
	    }
	}
	for (final Cofre c : this.COFRES) {
	    if (area.intersects(c.getArea()) && c.esSolido()) {
		return true;
	    }
	}
	return false;
    }

    public boolean intersectaAreaNoSolidaDeAlgunComplemento(final Shape area) {
	if (!this.intersectaZona(area)) {
	    return false;
	}
	for (final Complemento c : this.COMPLEMENTOS) {
	    if (c.esSolido() && c.intersectaAreaNoSolida(area)) {
		return true;
	    }
	}
	return false;
    }

    public boolean intersectaZona(final Shape area) {
	return area.intersects(this.AREA);
    }

}
