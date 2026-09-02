package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.HashSet;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;

/**
 * Proyectil estándar con resolución de impacto Zero-GC. Implementa
 * AccionEntidad directamente para evitar la creación de lambdas capturadoras en
 * el bucle caliente de colisiones.
 * 
 * @version 2.0 (Vanilla Java 8 - Zero-GC)
 */
public class ProyectilGeneral extends Proyectil implements Serializable, AccionEntidad<Criatura> {

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

		// 1. Evaluación directa pasando 'this' como visitor (CERO asignaciones en Heap)
		this.mundo.paraCadaCriaturaEn(area, true, this);

		if (this.eliminado) {
			return;
		}

		// 2. Colisión contra paredes y objetos sólidos del mapa
		if (!this.PENETRANTE && this.mundo.colisionaConZonaUObjetoSolido(area)) {
			this.eliminar();
		}
	}

	/**
	 * Callback ejecutado por ZoneBox / Mundo para cada criatura que intersecta el
	 * proyectil.
	 */
	@Override
	public void ejecutar(final Criatura victima) {
		if (this.eliminado || (victima == this.CAUSANTE) || this.perforados.contains(victima)
				|| victima.estaEliminado()) {
			return;
		}

		boolean esBlancoValido = true;
		if (this.CAUSANTE instanceof Criatura) {
			esBlancoValido = ((Criatura) this.CAUSANTE).esHostilHacia(victima);
		}

		if (esBlancoValido) {
			this.impactar(victima);
			if (!this.PENETRANTE) {
				this.eliminar();
			}
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