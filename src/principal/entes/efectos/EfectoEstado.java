package principal.entes.efectos;

import principal.entes.criaturas.Criatura;
import principal.utilidades.Globales;

/**
 * Instancia activa de un efecto sobre una Criatura. Soporta duraciones
 * estándar, acumulación de cargas (stacks) y transición de efectos infinitos a
 * tiempo residual (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class EfectoEstado {

	private final TipoEfectoEstado tipo;

	private boolean activo = false;
	private boolean infinito = false;

	private double tiempoRestante = 0.0;
	private double duracionTotal = 1.0;
	private int stacks = 0;
	private double potencia = 1.0;

	private double tiempoAcumuladoTick = 0.0;

	public EfectoEstado(final TipoEfectoEstado tipo) {
		this.tipo = tipo;
	}

	// =========================================================================
	// === MÉTODOS DE APLICACIÓN Y CONTROL
	// =========================================================================

	public void aplicar(final double duracionSegundos, final double potencia, final int maxStacks) {
		this.activo = true;
		this.infinito = false;
		this.potencia = Math.max(0.1, potencia);
		this.duracionTotal = Math.max(0.1, duracionSegundos);
		this.tiempoRestante = this.duracionTotal;

		if (this.tipo.isAcumulable()) {
			this.stacks = Math.min(Math.max(1, maxStacks), this.stacks + 1);
		} else {
			this.stacks = 1;
		}
	}

	public void aplicarInfinito(final double potencia) {
		this.activo = true;
		this.infinito = true;
		this.potencia = Math.max(0.1, potencia);
		this.duracionTotal = 1.0;
		this.tiempoRestante = 1.0;
		this.stacks = 1;
	}

	public void desactivarInfinito(final double tiempoResidualSegundos) {
		if (!this.activo) {
			return;
		}

		if (this.infinito) {
			this.infinito = false;
			this.duracionTotal = Math.max(0.5, tiempoResidualSegundos);
			this.tiempoRestante = this.duracionTotal;
		}
	}

	public void apagar() {
		this.activo = false;
		this.infinito = false;
		this.tiempoRestante = 0.0;
		this.stacks = 0;
		this.tiempoAcumuladoTick = 0.0;
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA Y DESPACHO DE TICKS (60 APS)
	// =========================================================================

	public void actualizar(final Criatura portador, final double dt) {
		if (!this.activo || portador == null || portador.estaEliminado()) {
			return;
		}

		// 1. Descuento de tiempo si no es infinito
		if (!this.infinito) {
			this.tiempoRestante -= dt;
			if (this.tiempoRestante <= 0.0) {
				this.apagar();
				return;
			}
		}

		// 2. Despacho de daño o curación periódica por tick
		if (this.tipo.tieneTickPeriodico()) {
			this.tiempoAcumuladoTick += dt;
			if (this.tiempoAcumuladoTick >= this.tipo.getIntervaloTick()) {
				this.tiempoAcumuladoTick = 0.0;
				this.ejecutarTick(portador);
			}
		}
	}

	private void ejecutarTick(final Criatura portador) {
		final double valorEfectivo = this.potencia * Math.max(1, this.stacks);

		switch (this.tipo) {
		case REGENERACION:
			if (!portador.vidaCompleta()) {
				portador.curar(valorEfectivo);
			}
			break;

		case VENENO:
			portador.reducirVida(valorEfectivo);
			Globales.GESTOR_PARTICULAS.emitirMagia(portador.getCentroX(), portador.getCentroY(), 4);
			Globales.GESTOR_TEXTOS.agregarDanio((int) Math.ceil(valorEfectivo), portador.getPosicionX(),
					portador.getPosicionY(), false);
			break;

		case SANGRADO:
			portador.reducirVida(valorEfectivo);
			Globales.GESTOR_PARTICULAS.emitirSangre(portador.getCentroX(), portador.getCentroY(), 0.0, -1.0, 5);
			Globales.GESTOR_TEXTOS.agregarDanio((int) Math.ceil(valorEfectivo), portador.getPosicionX(),
					portador.getPosicionY(), false);
			break;

		case QUEMADURA:
			portador.reducirVida(valorEfectivo);
			Globales.GESTOR_PARTICULAS.emitirExplosion(portador.getCentroX(), portador.getCentroY(), 3);
			Globales.GESTOR_TEXTOS.agregarDanio((int) Math.ceil(valorEfectivo), portador.getPosicionX(),
					portador.getPosicionY(), false);
			break;

		default:
			break;
		}
	}

	// =========================================================================
	// === GETTERS
	// =========================================================================

	public TipoEfectoEstado getTipo() {
		return this.tipo;
	}

	public boolean isActivo() {
		return this.activo;
	}

	public boolean isInfinito() {
		return this.infinito;
	}

	public double getTiempoRestante() {
		return this.tiempoRestante;
	}

	public double getDuracionTotal() {
		return this.duracionTotal;
	}

	public int getStacks() {
		return this.stacks;
	}

	public double getPotencia() {
		return this.potencia;
	}

	public double getProgresoNormalizado() {
		if (this.infinito || this.duracionTotal <= 0.0) {
			return 1.0;
		}
		return Math.max(0.0, Math.min(1.0, this.tiempoRestante / this.duracionTotal));
	}
}