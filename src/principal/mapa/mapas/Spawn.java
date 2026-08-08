package principal.mapa.mapas;

import java.awt.Point;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Constantes;

public class Spawn{
    private final Point PUNTO;
    private final String NOMBRE;

    public Spawn(final int x, final int y, final String nombre) {
	this.PUNTO = new Point(x, y);
	this.NOMBRE = nombre;
    }

    public Spawn(final Point p, final String nombre) {
	this.PUNTO = p;
	this.NOMBRE = nombre;
    }

    public String getNombre() {
	return this.NOMBRE;
    }

    public Point getPoint() {
	return this.PUNTO;
    }

    public int getX() {
	return this.PUNTO.x;
    }

    public int getY() {
	return this.PUNTO.y;
    }

    public void moverJugadorCentrado() {
	final Jugador jugador = Constantes.JUGADOR;
	jugador.establecerPosicion(this.getX() - jugador.getAncho() / 2, this.getY() - jugador.getAlto() / 2);
    }

    public void moverJugador() {
	final Jugador jugador = Constantes.JUGADOR;
	jugador.establecerPosicion(this.getX(), this.getY());
    }
}
