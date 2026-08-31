package principal.entes.objetos.items.armas;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.mapa.Mundo;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Clase base abstracta para todo el armamento del juego (cuerpo a cuerpo y a
 * distancia).
 * <p>
 * <b>SISTEMA DE CARGADOR, CADENCIA Y RECARGA (Zero-GC / O(1)):</b> Administra
 * internamente el estado de la recámara (balas cargadas), el temporizador de
 * cadencia entre tiros y el proceso de recarga con delegación de reserva al
 * inventario.
 * </p>
 * 
 * @version 3.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public abstract class Arma extends Portable {

	private static final long serialVersionUID = -1515324317822932516L;

	protected final boolean penetrante;
	protected final int damage;
	protected final int alcance;

	// =========================================================================
	// === GESTIÓN DE CARGADOR, CADENCIA Y RECARGA
	// =========================================================================

	protected int cadenciaMs = 500;
	protected int capacidadCargador = 0;
	protected int balasCargador = 0;
	protected int tiempoRecargaMs = 1500;
	protected String tipoMunicionRequerida = null;
	protected boolean recargando = false;

	protected final GestorTiempo GT_CADENCIA = new GestorTiempo();
	protected final GestorTiempo GT_RECARGA = new GestorTiempo();

	// Contenedor DTO persistente para compatibilidad con interfaces IGU (Zero-GC)
	protected final Municion MUNICION_COMPATIBLE;

	// =========================================================================
	// === CONSTRUCTORES
	// =========================================================================

	/** Constructor para armas cuerpo a cuerpo (Melee / Desarmado). */
	public Arma(final String codModelo, final int damage, final int alcance, final boolean penetrante) {
		super(codModelo);
		this.penetrante = penetrante;
		this.alcance = alcance;
		this.damage = damage;
		this.MUNICION_COMPATIBLE = new Municion(0, 0);
	}

	public Arma(final int x, final int y, final String codModelo, final int damage, final int alcance,
			final boolean penetrante) {
		super(x, y, codModelo);
		this.penetrante = penetrante;
		this.alcance = alcance;
		this.damage = damage;
		this.MUNICION_COMPATIBLE = new Municion(0, 0);
	}

	/** Constructor principal para armas de fuego a distancia. */
	public Arma(final String codModelo, final int damage, final int alcance, final boolean penetrante,
			final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs,
			final String tipoMunicionRequerida) {
		super(codModelo);
		this.penetrante = penetrante;
		this.alcance = alcance;
		this.damage = damage;
		this.capacidadCargador = capacidadCargador;
		this.balasCargador = capacidadCargador;
		this.tiempoRecargaMs = tiempoRecargaMs;
		this.cadenciaMs = cadenciaMs;
		this.tipoMunicionRequerida = tipoMunicionRequerida;
		this.MUNICION_COMPATIBLE = new Municion(capacidadCargador, capacidadCargador);
	}

	public Arma(final int x, final int y, final String codModelo, final int damage, final int alcance,
			final boolean penetrante, final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs,
			final String tipoMunicionRequerida) {
		super(x, y, codModelo);
		this.penetrante = penetrante;
		this.alcance = alcance;
		this.damage = damage;
		this.capacidadCargador = capacidadCargador;
		this.balasCargador = capacidadCargador;
		this.tiempoRecargaMs = tiempoRecargaMs;
		this.cadenciaMs = cadenciaMs;
		this.tipoMunicionRequerida = tipoMunicionRequerida;
		this.MUNICION_COMPATIBLE = new Municion(capacidadCargador, capacidadCargador);
	}

	// =========================================================================
	// === CICLO DE RECARGA Y DISPARO
	// =========================================================================

	@Override
	public void actualizar() {
		super.actualizar();
		this.actualizarCicloRecarga(Globales.JUGADOR);
	}

	/**
	 * Actualiza el temporizador de recarga activa en cada tick del juego.
	 *
	 * @param portador Criatura que empuña el arma.
	 */
	public void actualizarCicloRecarga(final Criatura portador) {
		if (this.recargando && this.GT_RECARGA.transcurrioMiliSegundos(this.tiempoRecargaMs)) {
			this.finalizarRecarga(portador);
		}
	}

	/**
	 * Inicia la secuencia de recarga bloqueando el arma durante
	 * {@link #tiempoRecargaMs}.
	 *
	 * @param portador Criatura que solicita la recarga.
	 * @return {@code true} si la recarga comenzó exitosamente.
	 */
	public boolean iniciarRecarga(final Criatura portador) {
		if (!this.esArmaDistancia() || this.recargando || (this.balasCargador >= this.capacidadCargador)) {
			return false;
		}

		// Si es el jugador, verificar si posee munición en la mochila antes de iniciar
		if (portador instanceof Jugador) {
			final int reserva = Globales.GESTOR_INVENTARIO.getInventarioJugador()
					.contarMunicionTotal(this.tipoMunicionRequerida);
			if (reserva <= 0) {
				return false; // Sin munición de reserva en inventario
			}
		}

		this.recargando = true;
		this.GT_RECARGA.establecerReferenciaTiempoActual();
		return true;
	}

	/**
	 * Finaliza la recarga transfiriendo las balas desde el inventario o reponiendo
	 * el cargador de la IA.
	 */
	protected void finalizarRecarga(final Criatura portador) {
		this.recargando = false;
		final int faltantes = this.capacidadCargador - this.balasCargador;
		if (faltantes <= 0) {
			return;
		}

		if (portador instanceof Jugador) {
			final int extraidas = Globales.GESTOR_INVENTARIO.getInventarioJugador()
					.extraerMunicion(this.tipoMunicionRequerida, faltantes);
			this.balasCargador += extraidas;
		} else {
			// Los enemigos rellenan el cargador completo automáticamente
			this.balasCargador = this.capacidadCargador;
		}
	}

	/**
	 * Valida si el arma puede disparar y descuenta un cartucho del cargador activo.
	 *
	 * @param causante Criatura que dispara.
	 * @return {@code true} si el disparo es permitido y se consumió la bala.
	 */
	protected boolean consumirDisparo(final Criatura causante) {
		if (this.recargando) {
			return false;
		}

		if (this.balasCargador <= 0) {
			this.iniciarRecarga(causante);
			if (causante != null) {
				GestorSonido.reproducirEnPosicion(IDSonido.SIN_MUNICION, causante.getCentroX(), causante.getCentroY(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionY());
			}
			return false;
		}

		if (!this.GT_CADENCIA.transcurrioMiliSegundos(this.cadenciaMs)) {
			return false; // Bloqueado por cadencia de tiro
		}

		this.balasCargador--;
		this.GT_CADENCIA.establecerReferenciaTiempoActual();
		return true;
	}

	// =========================================================================
	// === MÉTODOS BALÍSTICOS POLIMÓRFICOS EN 360°
	// =========================================================================

	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante, final boolean soloContraJugador) {
		// Sobrescrito por armas de fuego
	}

	public void disparar(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo escenario,
			final Criatura causante, final boolean soloContraJugador) {
		// Sobrescrito por armas de fuego
	}

	// =========================================================================
	// === ACCESORES Y MUNICIÓN
	// =========================================================================

	public boolean esArmaDistancia() {
		return (this.capacidadCargador > 0) && (this.tipoMunicionRequerida != null);
	}

	public int getBalasCargador() {
		return this.balasCargador;
	}

	public void setBalasCargador(final int balas) {
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balas));
	}

	public int getCapacidadCargador() {
		return this.capacidadCargador;
	}

	public int getTiempoRecargaMs() {
		return this.tiempoRecargaMs;
	}

	public boolean isRecargando() {
		return this.recargando;
	}

	public String getTipoMunicionRequerida() {
		return this.tipoMunicionRequerida;
	}

	public int getAlcance() {
		return this.alcance;
	}

	public int getAtaque() {
		return this.damage;
	}

	public boolean esPenetrante() {
		return this.penetrante;
	}

	public int getCadenciaMs() {
		return this.cadenciaMs;
	}

	public void setCadenciaMs(final int cadenciaMs) {
		this.cadenciaMs = Math.max(50, cadenciaMs);
	}

	public Municion getMunicion() {
		return this.MUNICION_COMPATIBLE;
	}
}