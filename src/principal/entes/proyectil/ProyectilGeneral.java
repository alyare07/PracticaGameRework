package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Arrays;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;

/**
 * Proyectil estándar balístico con resolución de impactos Zero-GC. Reemplaza el
 * HashSet dinámico por un arreglo plano con conteo primitivo para eliminar por
 * completo la creación de nodos internos en el Heap.
 * 
 * @version 3.0 (Vanilla Java 8 - Zero-GC Array Backed)
 */
public class ProyectilGeneral extends Proyectil implements Serializable, AccionEntidad<Criatura> {

	private static final long serialVersionUID = -3596461015684122157L;

	/**
	 * Capacidad máxima de perforación simultánea por proyectil antes de saturar el
	 * buffer.
	 */
	private static final int MAX_PERFORADOS = 8;

	/**
	 * Buffer pre-asignado de entidades ya impactadas (Cero creación de nodos en
	 * runtime).
	 */
	protected final Criatura[] perforados = new Criatura[MAX_PERFORADOS];
	protected int cantidadPerforados = 0;

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

	/**
	 * Reinicia las propiedades del proyectil al ser reutilizado desde el Pool
	 * limpiando las referencias de memoria para evitar fugas (Memory Leaks).
	 */
	@Override
	public void reiniciar(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante) {
		super.reiniciar(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho,
				alto, causante);

		// Limpieza de referencias fuertes a criaturas para permitir que el GC limpie
		// las muertas
		Arrays.fill(this.perforados, 0, this.cantidadPerforados, null);
		this.cantidadPerforados = 0;
	}

	protected void verificarImpacto() {
		if (this.mundo == null) {
			return;
		}

		final Rectangle area = this.getArea();

		// 1. Evaluación directa pasando 'this' como visitor (0 asignaciones en Heap)
		this.mundo.paraCadaCriaturaEn(area, true, this);

		if (this.eliminado) {
			return;
		}

		// 2. Colisión contra paredes y objetos sólidos del mapa
		if (!this.PENETRANTE && this.mundo.colisionaConZonaUObjetoSolido(area)) {
			this.eliminar();
		}
	}

	@Override
	public void ejecutar(final Criatura victima) {
		if (this.eliminado || (victima == this.CAUSANTE) || this.yaPerforado(victima) || victima.estaEliminado()) {
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
		if ((c == null) || this.yaPerforado(c)) {
			return;
		}
		this.registrarPerforado(c);
		c.recibirAtaque(this.DAMAGE, this.CAUSANTE);
	}

	/**
	 * Búsqueda secuencial O(K) en arreglo primitivo (K <= 8). Al ser un arreglo
	 * pequeño contiguo en caché L1 de CPU, es más rápido que calcular hashes.
	 */
	protected boolean yaPerforado(final Criatura c) {
		for (int i = 0; i < this.cantidadPerforados; i++) {
			if (this.perforados[i] == c) {
				return true;
			}
		}
		return false;
	}

	protected void registrarPerforado(final Criatura c) {
		if (this.cantidadPerforados < MAX_PERFORADOS) {
			this.perforados[this.cantidadPerforados++] = c;
		}
	}

	@Override
	public void pintarAnimacionImpacto(final Graphics2D g) {
	}
}