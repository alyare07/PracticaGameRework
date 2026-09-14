package principal.ia.arbol;

import principal.entes.Ente;

/**
 * Memoria local preasignada para decisiones de Criaturas con soporte de agenda
 * de tiempo de juego, dirección de flanqueo táctico y válvula anti-spam de
 * pathfinding (Zero-GC / O(1)).
 * 
 * @version 1.5 (Vanilla Java 8 - Flanking State Encapsulation)
 */
public class BlackboardIA {

	private static final int MAX_COOLDOWNS = 16;
	private final double[] timersCooldown = new double[MAX_COOLDOWNS];

	public static final int CD_ATAQUE = 0;
	public static final int CD_DISPARO = 1;
	public static final int CD_RECARGA = 2;
	public static final int CD_HABILIDAD_ESPECIAL = 3;
	public static final int CD_PAUSA_PATRULLA = 4;
	public static final int CD_DESVIO_TANGENCIAL = 5;

	private Ente objetivoActual;
	private double ultimoTargetX;
	private double ultimoTargetY;
	private boolean tienePosicionObjetivoRecordada;

	private double tiempoSinVerObjetivo;
	private double tiempoEnAlerta;
	private double tiempoInvestigando;

	private double xInvestigacion;
	private double yInvestigacion;
	private boolean sospechaPendiente;

	private boolean enPanico;
	private boolean siguiendoLider = true;
	private int waypointPatrullaActual;

	private double destinoVagarX;
	private double destinoVagarY;
	private boolean tieneDestinoVagarActivo;

	private double timestampRetornoJuegoHoras = 0.0;
	private double cooldownRecalculoRuta = 0.0;

	// Dirección de flanqueo lateral táctico (1 = Derecha, -1 = Izquierda)
	private int direccionFlanqueo = 1;

	public BlackboardIA() {
		this.reiniciar();
	}

	public void actualizar(final double dt) {
		for (int i = 0; i < MAX_COOLDOWNS; i++) {
			if (this.timersCooldown[i] > 0.0) {
				this.timersCooldown[i] = Math.max(0.0, this.timersCooldown[i] - dt);
			}
		}

		if (this.cooldownRecalculoRuta > 0.0) {
			this.cooldownRecalculoRuta = Math.max(0.0, this.cooldownRecalculoRuta - dt);
		}

		if (this.objetivoActual != null) {
			this.tiempoEnAlerta += dt;
		} else {
			this.tiempoEnAlerta = 0.0;
		}

		if (this.tienePosicionObjetivoRecordada) {
			this.tiempoSinVerObjetivo += dt;
		}
	}

	public boolean estaEnCooldown(final int idCooldown) {
		if ((idCooldown < 0) || (idCooldown >= MAX_COOLDOWNS)) {
			return false;
		}
		return this.timersCooldown[idCooldown] > 0.0;
	}

	public void iniciarCooldown(final int idCooldown, final double duracionSegundos) {
		if ((idCooldown >= 0) && (idCooldown < MAX_COOLDOWNS)) {
			this.timersCooldown[idCooldown] = Math.max(0.0, duracionSegundos);
		}
	}

	public void resetearCooldown(final int idCooldown) {
		if ((idCooldown >= 0) && (idCooldown < MAX_COOLDOWNS)) {
			this.timersCooldown[idCooldown] = 0.0;
		}
	}

	public boolean puedeRecalcularRuta() {
		return this.cooldownRecalculoRuta <= 0.0;
	}

	public void setCooldownRecalculoRuta(final double segundos) {
		this.cooldownRecalculoRuta = Math.max(0.0, segundos);
	}

	public void memorizarPosicionObjetivo(final double x, final double y) {
		this.ultimoTargetX = x;
		this.ultimoTargetY = y;
		this.tienePosicionObjetivoRecordada = true;
		this.tiempoSinVerObjetivo = 0.0;
	}

	public void olvidarPosicionObjetivo() {
		this.tienePosicionObjetivoRecordada = false;
		this.tiempoSinVerObjetivo = 0.0;
	}

	public void registrarSospechaRuido(final double x, final double y) {
		this.xInvestigacion = x;
		this.yInvestigacion = y;
		this.sospechaPendiente = true;
		this.tiempoInvestigando = 0.0;
	}

	public void limpiarSospechaRuido() {
		this.sospechaPendiente = false;
		this.tiempoInvestigando = 0.0;
	}

	public void fijarDestinoVagar(final double x, final double y) {
		this.destinoVagarX = x;
		this.destinoVagarY = y;
		this.tieneDestinoVagarActivo = true;
	}

	public void limpiarDestinoVagar() {
		this.tieneDestinoVagarActivo = false;
	}

	public double getTimestampRetornoJuegoHoras() {
		return this.timestampRetornoJuegoHoras;
	}

	public void setTimestampRetornoJuegoHoras(final double horas) {
		this.timestampRetornoJuegoHoras = Math.max(0.0, horas);
	}

	public int getDireccionFlanqueo() {
		return this.direccionFlanqueo;
	}

	public void setDireccionFlanqueo(final int dir) {
		this.direccionFlanqueo = (dir >= 0) ? 1 : -1;
	}

	public void invertirDireccionFlanqueo() {
		this.direccionFlanqueo = -this.direccionFlanqueo;
	}

	public void reiniciar() {
		for (int i = 0; i < MAX_COOLDOWNS; i++) {
			this.timersCooldown[i] = 0.0;
		}
		this.objetivoActual = null;
		this.ultimoTargetX = 0.0;
		this.ultimoTargetY = 0.0;
		this.tienePosicionObjetivoRecordada = false;
		this.tiempoSinVerObjetivo = 0.0;
		this.tiempoEnAlerta = 0.0;
		this.tiempoInvestigando = 0.0;
		this.xInvestigacion = 0.0;
		this.yInvestigacion = 0.0;
		this.sospechaPendiente = false;
		this.enPanico = false;
		this.siguiendoLider = true;
		this.waypointPatrullaActual = 0;
		this.tieneDestinoVagarActivo = false;
		this.timestampRetornoJuegoHoras = 0.0;
		this.cooldownRecalculoRuta = 0.0;
		this.direccionFlanqueo = 1;
	}

	public boolean isSiguiendoLider() {
		return this.siguiendoLider;
	}

	public void setSiguiendoLider(final boolean siguiendo) {
		this.siguiendoLider = siguiendo;
	}

	public Ente getObjetivoActual() {
		return this.objetivoActual;
	}

	public void setObjetivoActual(final Ente objetivo) {
		this.objetivoActual = objetivo;
		if (objetivo != null) {
			this.memorizarPosicionObjetivo(objetivo.getCentroX(), objetivo.getCentroY());
		}
	}

	public double getUltimoTargetX() {
		return this.ultimoTargetX;
	}

	public double getUltimoTargetY() {
		return this.ultimoTargetY;
	}

	public boolean tienePosicionObjetivoRecordada() {
		return this.tienePosicionObjetivoRecordada;
	}

	public double getTiempoSinVerObjetivo() {
		return this.tiempoSinVerObjetivo;
	}

	public double getTiempoEnAlerta() {
		return this.tiempoEnAlerta;
	}

	public double getTiempoInvestigando() {
		return this.tiempoInvestigando;
	}

	public void aumentarTiempoInvestigando(final double dt) {
		this.tiempoInvestigando += dt;
	}

	public double getXInvestigacion() {
		return this.xInvestigacion;
	}

	public double getYInvestigacion() {
		return this.yInvestigacion;
	}

	public boolean tieneSospechaPendiente() {
		return this.sospechaPendiente;
	}

	public boolean isEnPanico() {
		return this.enPanico;
	}

	public void setEnPanico(final boolean enPanico) {
		this.enPanico = enPanico;
	}

	public double getDestinoVagarX() {
		return this.destinoVagarX;
	}

	public double getDestinoVagarY() {
		return this.destinoVagarY;
	}

	public boolean tieneDestinoVagarActivo() {
		return this.tieneDestinoVagarActivo;
	}
}