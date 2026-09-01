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

public abstract class Arma extends Portable {

	private static final long serialVersionUID = -1515324317822932516L;

	protected final boolean penetrante;
	protected final int damage;
	protected final int alcance;

	protected int cadenciaMs = 500;
	protected int capacidadCargador = 0;
	protected int balasCargador = 0;
	protected int tiempoRecargaMs = 1500;
	protected String tipoMunicionRequerida = null;
	protected boolean recargando = false;

	protected final GestorTiempo GT_CADENCIA = new GestorTiempo();
	protected final GestorTiempo GT_RECARGA = new GestorTiempo();

	protected final Municion MUNICION_COMPATIBLE;

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

	@Override
	public void actualizar() {
		super.actualizar();
		this.actualizarCicloRecarga(Globales.JUGADOR);
	}

	public void actualizarCicloRecarga(final Criatura portador) {
		if (this.recargando && this.GT_RECARGA.transcurrioMiliSegundos(this.tiempoRecargaMs)) {
			this.finalizarRecarga(portador);
		}
	}

	public boolean iniciarRecarga(final Criatura portador) {
		if (!this.esArmaDistancia() || this.recargando || (this.balasCargador >= this.capacidadCargador)) {
			return false;
		}

		if (portador instanceof Jugador) {
			final int reserva = Globales.GESTOR_INVENTARIO.getInventarioJugador()
					.contarMunicionTotal(this.tipoMunicionRequerida);
			if (reserva <= 0) {
				return false;
			}
		}

		this.recargando = true;
		this.GT_RECARGA.establecerReferenciaTiempoActual();
		return true;
	}

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
			this.balasCargador = this.capacidadCargador;
		}
	}

	protected boolean consumirDisparo(final Criatura causante) {
		if (this.recargando) {
			return false;
		}

		if (this.balasCargador <= 0) {
			this.iniciarRecarga(causante);
			if ((causante != null) && (Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null)) {
				GestorSonido.reproducirEnPosicion(IDSonido.SIN_MUNICION, causante.getCentroX(), causante.getCentroY(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionY());
			}
			return false;
		}

		if (!this.GT_CADENCIA.transcurrioMiliSegundos(this.cadenciaMs)) {
			return false;
		}

		this.balasCargador--;
		this.GT_CADENCIA.establecerReferenciaTiempoActual();
		return true;
	}

	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante) {
	}

	public void disparar(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo escenario,
			final Criatura causante) {
	}

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