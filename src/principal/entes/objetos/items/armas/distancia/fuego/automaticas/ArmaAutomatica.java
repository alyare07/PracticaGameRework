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

	@Override
	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante) {

		if (this.consumirDisparo(causante)) {
			if (escenario != null) {
				final double dx = xDestino - xOrigen;
				final double dy = yDestino - yOrigen;
				final double anguloCentral = Math.atan2(dy, dx);

				final double desviacion = (Math.random() - 0.5) * 2.0 * this.dispersionRad;
				final double anguloFinal = anguloCentral + desviacion;

				final double targetX = xOrigen + (Math.cos(anguloFinal) * 1000.0);
				final double targetY = yOrigen + (Math.sin(anguloFinal) * 1000.0);

				escenario.crearProyectil(
						new ProyectilBala(this.damage, this.velocidadBala, this.penetrante, this.alcance, escenario,
								xOrigen, yOrigen, targetX, targetY, this.tamanoBala, this.tamanoBala, causante));
			}

			if ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null)) {
				GestorSonido.reproducirEnPosicion(IDSonido.DISPARO_PISTOLA, xOrigen, yOrigen,
						Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionY());
			}
		}
	}

	@Override
	public void disparar(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo escenario,
			final Criatura causante) {

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

		this.disparar(xOrigen, yOrigen, xDest, yDest, escenario, causante);
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