package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.HashSet;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;

public class ProyectilGeneral extends Proyectil implements Serializable {

	private static final long serialVersionUID = -3596461015684122157L;

	protected final HashSet<Criatura> perforados = new HashSet<Criatura>(4);

	public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
	}

	public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante);
	}

	@Override
	public void actualizar() {
		if (!this.eliminado) {
			if ((this.ALCANCE > 0) && (this.distanciaRecorrida >= this.ALCANCE)) {
				this.eliminar();
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
		if (this.mundo == null) {
			return;
		}

		final Rectangle area = this.getArea();

		// 1. Detección de impacto sobre cualquier criatura mediante Visitor Zero-GC
		this.mundo.paraCadaCriaturaEn(area, true, victima -> {
			if (this.eliminado || (victima == this.CAUSANTE) || this.perforados.contains(victima)
					|| victima.estaEliminado()) {
				return;
			}

			boolean esBlancoValido = true;
			if (this.CAUSANTE instanceof Criatura) {
				// Evalúa en O(1) si el emisor considera hostil a la víctima según su máscara de
				// facciones
				esBlancoValido = ((Criatura) this.CAUSANTE).esHostilHacia(victima);
			}

			if (esBlancoValido) {
				this.impactar(victima);
				if (!this.PENETRANTE) {
					this.eliminar();
				}
			}
		});

		if (this.eliminado) {
			return;
		}

		// 2. Colisión contra paredes y objetos sólidos del mapa
		if (!this.PENETRANTE && this.mundo.colisionaConZonaUObjetoSolido(area)) {
			this.eliminar();
		}
	}

	@Override
	protected void impactar(final Criatura c) {
		if ((c == null) || this.perforados.contains(c)) {
			return;
		}
		this.perforados.add(c);
		c.recibirAtaque(this.DAMAGE, this.CAUSANTE);
	}

	@Override
	public void pintarAnimacionImpacto(final Graphics2D g) {
	}
}