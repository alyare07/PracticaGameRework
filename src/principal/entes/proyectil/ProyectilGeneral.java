package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.HashMap;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.Constantes;

public class ProyectilGeneral extends Proyectil implements Serializable{
    protected final boolean SOLO_CONTRA_JUGADOR;

    public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance, final Mundo mundo, final double x, final double y, final int ancho,
	    final int alto, final Direccion direccion, final Ente causante) {
	super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
	this.SOLO_CONTRA_JUGADOR = false;
    }

    public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance, final Mundo mundo, final double x, final double y, final int ancho,
	    final int alto, final Direccion direccion, final Ente causante, final boolean soloContraJugador) {
	super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
	this.SOLO_CONTRA_JUGADOR = soloContraJugador;
    }

    private static final long serialVersionUID = -3596461015684122157L;
    protected final HashMap<Criatura, Criatura> perforados = new HashMap<Criatura, Criatura>();

    @Override
    public void actualizar() {
//		System.out.println("Proyectil : "+ this.area);
	if (!this.eliminado) {
	    if (this.distanciaRecorrida >= this.ALCANCE) {
		this.eliminar();
		System.out.println("eliminado por distancia");
		return;
	    }
	    this.mover();
	    this.verificarImpacto();

	}

    }

    @Override
    public void pintar(final Graphics2D g) {
	super.pintar(g);
    }

    protected void verificarImpacto() {
	final Rectangle area = this.getArea();
	if (Constantes.JUGADOR != this.CAUSANTE) {
	    if (area.intersects(Constantes.JUGADOR.getRectangulo())) {

		this.impactar(Constantes.JUGADOR);
		if (!this.PENETRANTE) {
		    this.eliminar();
		    return;
		}
	    }
	}

	if (!this.SOLO_CONTRA_JUGADOR) {

	    for (final Criatura c : this.mundo.getCriaturasIntersectadasConEnte(this)) {
		if (area.intersects(c.getRectangulo())) {
		    if (c == this.CAUSANTE) {
			continue;
		    }
		    this.impactar(c);
		    if (!this.PENETRANTE) {
			this.eliminar();
			System.out.println("eliminado por impacto!");
			break;
		    }
		}
	    }
	}
	if (this.eliminado) {
	    return;
	}

	final Tile tilePosicionado = this.mundo.getTerreno().getTileReferenciado(area.x, area.y);
	if (tilePosicionado == null) { // se encuentra fuera del terreno
	    System.out.println("eliminado por tile null!: ");
	    this.eliminar();
	    return;
	}
	if (!this.PENETRANTE) {
	    if (this.mundo.getTerreno().intersectaSolidoDijkstra(area)) {
		System.out.println("eliminado por impacto con tile solido! " + tilePosicionado.getArea());
		this.eliminar();
	    }
	}
    }

    @Override
    protected void impactar(final Criatura c) {
	if (this.perforados.containsKey(c)) {
	    return;
	}
	this.perforados.put(c, c);
	c.recibirAtaque(this.DAMAGE, this.CAUSANTE);
    }

    @Override
    public void eliminar() {
	this.eliminado = true;
    }

    @Override
    public int getPosicionXInt() {
	return (int) this.x;
    }

    @Override
    public int getPosicionYInt() {

	return (int) this.y;
    }

    @Override
    public double getPosicionX() {
	return this.x;
    }

    @Override
    public double getPosicionY() {
	return this.y;
    }

    @Override
    public void modificarPosicionX(final double desplazamientoX) {

    }

    @Override
    public void modificarPosicionY(final double desplazamientoY) {

    }

    @Override
    public boolean estaEliminado() {
	return this.eliminado;
    }

    @Override
    public void pintarAnimacionImpacto(final Graphics2D g) {
	// TODO Auto-generated method stub

    }

}
