package principal.entes.proyectil.explosivo;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.proyectil.ProyectilGeneral;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

public abstract class ProyectilExplosivo extends ProyectilGeneral {

	private static final long serialVersionUID = -6101794986241939867L;
	protected final double DIAMETRO_EXPLOSION;
	protected final int TIEMPO_MS_ANIMACION_IMPACTO = 400;
	protected final GestorTiempo GT_ANIMACION_IMPACTO = new GestorTiempo();
	protected boolean impactoRealizado;

	public ProyectilExplosivo(final double damage, final double velocidad, final double alcance,
			final boolean penetraObstaculos, final Mundo escenario, final double x, final double y, final int ancho,
			final int alto, final double radioExplosion, final Direccion direccion, final Ente causante) {
		super(damage, velocidad, penetraObstaculos, alcance, escenario, x, y, ancho, alto, direccion, causante);
		this.DIAMETRO_EXPLOSION = radioExplosion;
	}

	@Override
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		for (final Criatura c : this.mundo.getCriaturasIntersectadasConEnte(this)) {
			if (area.intersects(c.getRectangulo())) {
				if (c == this.CAUSANTE) {
					continue;
				}
				this.impactar(c);
				this.realizarImpacto();
				break;
			}
		}

		if (this.impactoRealizado) {
			return;
		}
		if (Globales.JUGADOR != this.CAUSANTE) {
			if (area.intersects(Globales.JUGADOR.getRectangulo())) {

				this.impactar(Globales.JUGADOR);
				this.realizarImpacto();
				return;
			}
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

	protected void realizarImpacto() {
		this.impactoRealizado = true;
		this.GT_ANIMACION_IMPACTO.establecerReferenciaTiempoActual();
	}

	public boolean impactoRealizado() {
		return this.impactoRealizado;
	}

	protected Ellipse2D getAreaExplosion() {
		return new Ellipse2D.Double(this.getPosicionXInt() - (this.DIAMETRO_EXPLOSION / 2),
				(this.getPosicionYInt() + (this.alto / 2)) - (this.DIAMETRO_EXPLOSION / 2), this.DIAMETRO_EXPLOSION,
				this.DIAMETRO_EXPLOSION);
	}

}
