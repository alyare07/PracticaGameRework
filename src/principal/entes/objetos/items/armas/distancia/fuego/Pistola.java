package principal.entes.objetos.items.armas.distancia.fuego;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class Pistola extends Arma {

	private static final long serialVersionUID = -9196405156055570071L;
	public static final String COD_PISTOLA = "Pistola";

	protected final double velocidadDisparo = 3.5;
	protected final int tamanoBala360 = 3;

	public Pistola(final String codModelo) {
		super(codModelo, 10, 250, false, 12, 1200, 400, CajaMunicion.COD_9MM);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Pistola(final int x, final int y, final String codModelo) {
		super(x, y, codModelo, 10, 250, false, 12, 1200, 400, CajaMunicion.COD_9MM);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Pistola(final int x, final int y, final String codModelo, final int balasCargador) {
		super(x, y, codModelo, 10, 250, false, 12, 1200, 400, CajaMunicion.COD_9MM);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
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

				escenario.getGestorProyectiles().dispararBala(this.damage, this.velocidadDisparo, this.penetrante,
						this.alcance, escenario, spawnX, spawnY, xDestino, yDestino, this.tamanoBala360,
						this.tamanoBala360, causante);
			}

			if ((causante instanceof Jugador) && (Globales.CAMARA != null) && (dist > 0.001)) {
				Globales.CAMARA.aplicarRetroceso(-dx, -dy, 3.0, 90.0);
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

	@Override
	public Objeto copiar() {
		return new Pistola(this.getPosicionXInt(), this.getPosicionYInt(), this.codigoModelo, this.balasCargador);
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

	public static Pistola crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Pistola(COD_PISTOLA);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString() : COD_PISTOLA;

		int balas = 12;
		if (json.get("balasCargador") != null) {
			balas = ((Number) json.get("balasCargador")).intValue();
		} else if (json.get("municion") != null) {
			balas = ((Number) json.get("municion")).intValue();
		}

		return new Pistola(x, y, codModelo, balas);
	}

	@Override
	public String exportarTipoItem() {
		return "Pistola";
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.clear();
		listaInfo.add("Daño: " + this.damage + " pts.");
		listaInfo.add("Cargador: " + this.balasCargador + "/" + this.capacidadCargador + " balas.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
		listaInfo.add("Tiempo de recarga: " + (this.tiempoRecargaMs / 1000.0) + " s.");
		listaInfo.add("Munición requerida: 9mm.");
	}
}