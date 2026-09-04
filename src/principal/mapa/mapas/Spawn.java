package principal.mapa.mapas;

import java.awt.Point;
import java.awt.Rectangle;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Jugador;
import principal.utilidades.Globales;

public class Spawn {

	private final Point PUNTO;
	private final String NOMBRE;
	private final Rectangle AREA_COLISION;

	public Spawn(final int x, final int y, final String nombre) {
		this.PUNTO = new Point(x, y);
		this.NOMBRE = (nombre != null) ? nombre : "Spawn";
		this.AREA_COLISION = new Rectangle(x, y, 16, 16);
	}

	public Spawn(final Point p, final String nombre) {
		this((p != null) ? p.x : 0, (p != null) ? p.y : 0, nombre);
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

	public Rectangle getArea() {
		this.AREA_COLISION.setBounds(this.PUNTO.x, this.PUNTO.y, 16, 16);
		return this.AREA_COLISION;
	}

	public void moverJugadorCentrado() {
		final Jugador jugador = Globales.JUGADOR;
		if (jugador != null) {
			jugador.setPosicion(this.getX() - (jugador.getAncho() / 2), this.getY() - (jugador.getAlto() / 2));
		}
	}

	public void moverJugador() {
		final Jugador jugador = Globales.JUGADOR;
		if (jugador != null) {
			jugador.setPosicion(this.getX(), this.getY());
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.PUNTO.x));
		json.put("y", Integer.valueOf(this.PUNTO.y));
		json.put("nombre", this.NOMBRE);
		return json;
	}

	public static Spawn crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Spawn(0, 0, "Spawn");
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String nombre = (json.get("nombre") != null) ? json.get("nombre").toString() : "Spawn";
		return new Spawn(x, y, nombre);
	}

	@Override
	public String toString() {
		return "Spawn [nombre=" + this.NOMBRE + ", x=" + this.PUNTO.x + ", y=" + this.PUNTO.y + "]";
	}
}