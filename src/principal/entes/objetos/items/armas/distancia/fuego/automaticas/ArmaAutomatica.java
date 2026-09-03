package principal.entes.objetos.items.armas.distancia.fuego.automaticas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.items.armas.Arma;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Base abstracta para armas de disparo automático y ráfaga continua con
 * dispersión angular procedural y retroceso (Zero-GC / Fast Math).
 * 
 * @version 2.0 (Vanilla Java 8)
 */
public abstract class ArmaAutomatica extends Arma {

	private static final long serialVersionUID = 781920391209381L;

	public static final String COD_SUBFUSIL = "Subfusil Ligero";
	public static final String COD_RIFLE = "Rifle de Asalto";
	public static final String COD_AMETRALLADORA = "Ametralladora Pesada";

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
			final double dx = xDestino - xOrigen;
			final double dy = yDestino - yOrigen;
			final double dist = Math.sqrt((dx * dx) + (dy * dy));

			if (escenario != null) {
				final double spawnX = (dist > 0.001) ? xOrigen + ((dx / dist) * 12.0) : xOrigen;
				final double spawnY = (dist > 0.001) ? yOrigen + ((dy / dist) * 12.0) : yOrigen;

				final double anguloCentral = Math.atan2(dy, dx);
				final double desviacion = (Math.random() - 0.5) * 2.0 * this.dispersionRad;
				final double anguloFinal = anguloCentral + desviacion;

				final double targetX = spawnX + (Math.cos(anguloFinal) * 1000.0);
				final double targetY = spawnY + (Math.sin(anguloFinal) * 1000.0);

				escenario.getGestorProyectiles().dispararBala(this.damage, this.velocidadBala, this.penetrante,
						this.alcance, escenario, spawnX, spawnY, targetX, targetY, this.tamanoBala, this.tamanoBala,
						causante);
			}

			if ((causante instanceof Jugador) && (Globales.CAMARA != null) && (dist > 0.001)) {
				Globales.CAMARA.aplicarRetroceso(-dx, -dy, 1.8, 75.0);
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
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", this.codigoModelo);
		json.put("balasCargador", Integer.valueOf(this.balasCargador));
		json.put("capacidadCargador", Integer.valueOf(this.capacidadCargador));
		return json;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Daño por bala: " + this.damage + " pts.");
		listaInfo.add("Cargador: " + this.balasCargador + "/" + this.capacidadCargador + " balas.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
		listaInfo.add("Tiempo de recarga: " + (this.tiempoRecargaMs / 1000.0) + " s.");
		listaInfo.add("Dispersión: ±" + String.format("%.1f", Math.toDegrees(this.dispersionRad)) + "°.");
	}
}