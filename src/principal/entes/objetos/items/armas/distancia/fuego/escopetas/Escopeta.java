package principal.entes.objetos.items.armas.distancia.fuego.escopetas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public abstract class Escopeta extends Arma {

	private static final long serialVersionUID = 581920391203912L;

	public static final String COD_RECORTADA = "Escopeta Recortada";
	public static final String COD_TACTICA = "Escopeta Tactica";
	public static final String COD_AUTOMATICA = "Escopeta Automatica";

	protected final int cantidadPerdigones;
	protected final double anguloAperturaRad;
	protected final double velocidadPerdigon;

	public Escopeta(final String codModelo, final int damagePorPerdigon, final int alcance, final boolean penetrante,
			final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs, final int cantidadPerdigones,
			final double aperturaGrados, final double velocidadPerdigon) {
		super(codModelo, damagePorPerdigon, alcance, penetrante, capacidadCargador, tiempoRecargaMs, cadenciaMs,
				CajaMunicion.COD_12CAL);
		this.cantidadPerdigones = Math.max(2, cantidadPerdigones);
		this.anguloAperturaRad = Math.toRadians(aperturaGrados);
		this.velocidadPerdigon = velocidadPerdigon;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Escopeta(final int x, final int y, final String codModelo, final int damagePorPerdigon, final int alcance,
			final boolean penetrante, final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs,
			final int cantidadPerdigones, final double aperturaGrados, final double velocidadPerdigon) {
		super(x, y, codModelo, damagePorPerdigon, alcance, penetrante, capacidadCargador, tiempoRecargaMs, cadenciaMs,
				CajaMunicion.COD_12CAL);
		this.cantidadPerdigones = Math.max(2, cantidadPerdigones);
		this.anguloAperturaRad = Math.toRadians(aperturaGrados);
		this.velocidadPerdigon = velocidadPerdigon;
		this.rellenarInfo(this.LISTA_INFO);
	}

	@Override
	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante) {

		if (this.consumirDisparo(causante)) {
			final double dx = xDestino - xOrigen;
			final double dy = yDestino - yOrigen;
			final double dist = Math.hypot(dx, dy);

			if (escenario != null) {
				final double spawnX = (dist > 0.001) ? xOrigen + ((dx / dist) * 12.0) : xOrigen;
				final double spawnY = (dist > 0.001) ? yOrigen + ((dy / dist) * 12.0) : yOrigen;

				final double anguloCentral = Math.atan2(dy, dx);
				final double pasoAngular = this.anguloAperturaRad / (this.cantidadPerdigones - 1);
				final double anguloInicial = anguloCentral - (this.anguloAperturaRad / 2.0);

				for (int i = 0; i < this.cantidadPerdigones; i++) {
					final double anguloActual = anguloInicial + (i * pasoAngular);
					final double targetX = spawnX + (Math.cos(anguloActual) * 1000.0);
					final double targetY = spawnY + (Math.sin(anguloActual) * 1000.0);

					escenario.getGestorProyectiles().dispararPerdigon(this.damage, this.velocidadPerdigon,
							this.penetrante, this.alcance, escenario, spawnX, spawnY, targetX, targetY, causante);
				}
			}

			if ((causante instanceof Jugador) && (Globales.CAMARA != null) && (dist > 0.001)) {
				Globales.CAMARA.aplicarRetroceso(-dx, -dy, 8.5, 160.0);
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

	public int getCantidadPerdigones() {
		return this.cantidadPerdigones;
	}

	public double getAnguloAperturaGrados() {
		return Math.toDegrees(this.anguloAperturaRad);
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
		listaInfo.add("Daño: " + this.damage + " pts x " + this.cantidadPerdigones + " perdigones.");
		listaInfo.add("Cargador: " + this.balasCargador + "/" + this.capacidadCargador + " balas.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
		listaInfo.add("Tiempo de recarga: " + (this.tiempoRecargaMs / 1000.0) + " s.");
		listaInfo.add("Apertura: " + (int) Math.toDegrees(this.anguloAperturaRad) + "°.");
		listaInfo.add("Munición: Calibre 12.");
	}
}