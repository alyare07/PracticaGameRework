package principal.entes.proyectil.filtro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.proyectil.Proyectil;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

public class ProyectilContraJugador extends Proyectil implements Serializable {

	public ProyectilContraJugador(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo escenario, final double x, final double y, final int ancho,
			final int alto, final Direccion direccion, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, escenario, x, y, ancho, alto, direccion, causante);
	}

	private static final long serialVersionUID = 8936062246678950377L;

	@Override
	public void actualizar() {
		if (!this.eliminado) {
			if (this.distanciaRecorrida >= this.ALCANCE) {
				this.eliminar();
				return;
			}
			this.mover();
			this.verificarImpacto();

		}

	}

	@Override
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
				this.alto, Color.red);
	}

	@Override
	public Rectangle getArea() {
		return new Rectangle((int) this.x, (int) this.y, this.ancho, this.alto);
	}

	protected void verificarImpacto() {
		final Rectangle area = this.getArea();

		if (area.intersects(Globales.JUGADOR.getRectangulo())) {
			this.impactar(Globales.JUGADOR);
		}

		final Tile tilePosicionado = this.mundo.getTerreno().getTileReferenciado(area.x, area.y);
		if (tilePosicionado == null) { // se encuentra fuera del terreno
			this.eliminar();
			return;
		}
		if (!this.PENETRANTE) {
			if (this.mundo.getTerreno().intersecta(area)) {
				this.eliminar();
			}
		}
	}

	@Override
	protected void impactar(final Criatura c) {
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
