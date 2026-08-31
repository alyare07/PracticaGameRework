package principal.entes.objetos.items.armas.distancia.fuego.automaticas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.proyectil.ProyectilBala;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Clase base para armas de fuego automáticas y de repetición rápida (SMG,
 * Rifles, LMG).
 * <p>
 * <b>SISTEMA DE CARGADOR Y RETROCESO EN 360° (Zero-GC):</b> Controla la
 * capacidad de munición en cargador, cadencia automática de ráfagas, tiempo de
 * recarga y dispersión angular balística.
 * </p>
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public abstract class ArmaAutomatica extends Arma {

	private static final long serialVersionUID = 781920391209381L;

	protected final double dispersionRad;
	protected final double velocidadBala;
	protected final int tamanoBala = 3;

	public ArmaAutomatica(final String codModelo, final int damage, final int alcance, final boolean penetrante,
			final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs,
			final String tipoMunicionRequerida, final double dispersionGrados, final double velocidadBala) {
		super(codModelo, damage, alcance, penetrante, capacidadCargador, tiempoRecargaMs, cadenciaMs,
				tipoMunicionRequerida);
		this.dispersionRad = Math.toRadians(dispersionGrados);
		this.velocidadBala = velocidadBala;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public ArmaAutomatica(final int x, final int y, final String codModelo, final int damage, final int alcance,
			final boolean penetrante, final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs,
			final String tipoMunicionRequerida, final double dispersionGrados, final double velocidadBala) {
		super(x, y, codModelo, damage, alcance, penetrante, capacidadCargador, tiempoRecargaMs, cadenciaMs,
				tipoMunicionRequerida);
		this.dispersionRad = Math.toRadians(dispersionGrados);
		this.velocidadBala = velocidadBala;
		this.rellenarInfo(this.LISTA_INFO);
	}

	/**
	 * Dispara una bala individual aplicando dispersión balística en 360 grados
	 * consumiendo munición del cargador.
	 */
	@Override
	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante, final boolean soloContraJugador) {

		if (this.consumirDisparo(causante)) {
			if (escenario != null) {
				final double dx = xDestino - xOrigen;
				final double dy = yDestino - yOrigen;
				final double anguloCentral = Math.atan2(dy, dx);

				// Cálculo de desviación aleatoria dentro del cono de dispersión
				final double desviacion = (Math.random() - 0.5) * 2.0 * this.dispersionRad;
				final double anguloFinal = anguloCentral + desviacion;

				final double targetX = xOrigen + (Math.cos(anguloFinal) * 1000.0);
				final double targetY = yOrigen + (Math.sin(anguloFinal) * 1000.0);

				escenario.crearProyectil(new ProyectilBala(this.damage, this.velocidadBala, this.penetrante,
						this.alcance, escenario, xOrigen, yOrigen, targetX, targetY, this.tamanoBala, this.tamanoBala,
						causante, soloContraJugador));
			}

			GestorSonido.reproducirEnPosicion(IDSonido.DISPARO_PISTOLA, xOrigen, yOrigen,
					Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
					Globales.CAMARA.getEntidadEnfocada().getPosicionY());
		}
	}

	/**
	 * Disparo cardinal con dispersión adaptada a la dirección.
	 */
	@Override
	public void disparar(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo escenario,
			final Criatura causante, final boolean soloContraJugador) {

		double anguloBase = 0.0;
		if (direccion == Direccion.ESTE) {
			anguloBase = 0.0;
		} else if (direccion == Direccion.SUR) {
			anguloBase = Math.PI / 2.0;
		} else if (direccion == Direccion.OESTE) {
			anguloBase = Math.PI;
		} else if (direccion == Direccion.NORTE) {
			anguloBase = -Math.PI / 2.0;
		}

		final int xDest = xOrigen + (int) Math.round(Math.cos(anguloBase) * 500.0);
		final int yDest = yOrigen + (int) Math.round(Math.sin(anguloBase) * 500.0);

		this.disparar(xOrigen, yOrigen, xDest, yDest, escenario, causante, soloContraJugador);
	}

	public double getDispersionGrados() {
		return Math.toDegrees(this.dispersionRad);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.CODIGO_MODELO);
		json.put("balasCargador", this.balasCargador);
		json.put("capacidadCargador", this.capacidadCargador);
		return json;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.add("Daño por bala: " + this.damage + " pts.");
		listaInfo.add(
				"Cargador: " + this.getMunicion().getCantidad() + "/" + this.getMunicion().getLimite() + " balas.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
		listaInfo.add("Tiempo de recarga: " + (this.tiempoRecargaMs / 1000.0) + " s.");
		listaInfo.add("Dispersión: ±" + String.format("%.1f", Math.toDegrees(this.dispersionRad)) + "°.");
	}
}