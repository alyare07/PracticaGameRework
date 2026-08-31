package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.utilidades.Globales;

/**
 * Implementación base para proyectiles físicos con detección de impacto,
 * perforación y colisión contra la arquitectura del terreno.
 * 
 * @version 2.5 (Java 8 Compatible - Zero-GC Architecture)
 */
public class ProyectilGeneral extends Proyectil implements Serializable {

	private static final long serialVersionUID = -3596461015684122157L;

	protected final boolean SOLO_CONTRA_JUGADOR;

	/**
	 * Registro de criaturas ya impactadas para evitar daño múltiple por proyectiles
	 * penetrantes.
	 */
	protected final HashSet<Criatura> perforados = new HashSet<Criatura>(4);

	// =========================================================================
	// === CONSTRUCTORES CARDINALES (COMPATIBILIDAD)
	// =========================================================================

	public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
		this.SOLO_CONTRA_JUGADOR = false;
	}

	public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double x, final double y, final int ancho, final int alto,
			final Direccion direccion, final Ente causante, final boolean soloContraJugador) {
		super(damage, velocidad, penetrante, alcance, mundo, x, y, ancho, alto, direccion, causante);
		this.SOLO_CONTRA_JUGADOR = soloContraJugador;
	}

	// =========================================================================
	// === CONSTRUCTORES VECTORIALES EN 360°
	// =========================================================================

	public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante);
		this.SOLO_CONTRA_JUGADOR = false;
	}

	public ProyectilGeneral(final double damage, final double velocidad, final boolean penetrante, final double alcance,
			final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino, final double yDestino,
			final int ancho, final int alto, final Ente causante, final boolean soloContraJugador) {
		super(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante);
		this.SOLO_CONTRA_JUGADOR = soloContraJugador;
	}

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
		super.pintar(g);
	}

	/**
	 * Evalúa el impacto contra entidades vivas y obstáculos físicos del mapa.
	 */
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();

		// 1. Verificación de impacto contra el Jugador
		if (Globales.JUGADOR != this.CAUSANTE) {
			if (area.intersects(Globales.JUGADOR.getArea())) {
				this.impactar(Globales.JUGADOR);
				if (!this.PENETRANTE) {
					this.eliminar();
					return;
				}
			}
		}

		// 2. Verificación de impacto contra otras criaturas en el mapa
		if (!this.SOLO_CONTRA_JUGADOR && (this.mundo != null)) {
			final ArrayList<Criatura> criaturasCercanas = this.mundo.getCriaturasIntersectadasConEnte(this);
			final int total = criaturasCercanas.size();

			for (int i = 0; i < total; i++) {
				final Criatura c = criaturasCercanas.get(i);
				if (c == this.CAUSANTE) {
					continue;
				}

				if (area.intersects(c.getArea())) {
					this.impactar(c);
					if (!this.PENETRANTE) {
						this.eliminar();
						return;
					}
				}
			}
		}

		if (this.eliminado) {
			return;
		}

		// 3. Verificación de colisión contra el terreno y objetos sólidos
		if (this.mundo != null) {
			final Tile tilePosicionado = this.mundo.getTerreno().getTileReferenciado(area.x, area.y);
			if (tilePosicionado == null) {
				this.eliminar();
				return;
			}

			if (!this.PENETRANTE && this.mundo.getTerreno().intersectaAlgoSolido(area)) {
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
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public void pintarAnimacionImpacto(final Graphics2D g) {
	}
}