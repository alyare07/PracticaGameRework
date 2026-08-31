package principal.entes.objetos.items.armas.distancia.fuego.escopetas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.proyectil.ProyectilPerdigon;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Clase base para escopetas de posta y combate.
 * <p>
 * <b>SISTEMA DE CARGADOR Y CONO BALÍSTICO (Zero-GC):</b> Controla la capacidad
 * de cartuchos en recámara, cadencia entre disparos, tiempo de recarga y la
 * proyección cónica de perdigones en 360 grados.
 * </p>
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public abstract class Escopeta extends Arma {

	private static final long serialVersionUID = 581920391203912L;

	protected final int cantidadPerdigones;
	protected final double anguloAperturaRad;
	protected final double velocidadPerdigon;

	public Escopeta(final String codModelo, final int damagePorPerdigon, final int alcance, final boolean penetrante,
			final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs, final int cantidadPerdigones,
			final double aperturaGrados, final double velocidadPerdigon) {
		super(codModelo, damagePorPerdigon, alcance, penetrante, capacidadCargador, tiempoRecargaMs, cadenciaMs,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_ESCOPETA);
		this.cantidadPerdigones = Math.max(2, cantidadPerdigones);
		this.anguloAperturaRad = Math.toRadians(aperturaGrados);
		this.velocidadPerdigon = velocidadPerdigon;
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Escopeta(final int x, final int y, final String codModelo, final int damagePorPerdigon, final int alcance,
			final boolean penetrante, final int capacidadCargador, final int tiempoRecargaMs, final int cadenciaMs,
			final int cantidadPerdigones, final double aperturaGrados, final double velocidadPerdigon) {
		super(x, y, codModelo, damagePorPerdigon, alcance, penetrante, capacidadCargador, tiempoRecargaMs, cadenciaMs,
				ListaModelosItem.COD_CONSUMIBLE_MUNICION_ESCOPETA);
		this.cantidadPerdigones = Math.max(2, cantidadPerdigones);
		this.anguloAperturaRad = Math.toRadians(aperturaGrados);
		this.velocidadPerdigon = velocidadPerdigon;
		this.rellenarInfo(this.LISTA_INFO);
	}

	/**
	 * Dispara una ráfaga cónica de perdigones en 360 grados consumiendo un cartucho
	 * del cargador.
	 */
	@Override
	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante, final boolean soloContraJugador) {

		if (this.consumirDisparo(causante)) {
			if (escenario != null) {
				final double dx = xDestino - xOrigen;
				final double dy = yDestino - yOrigen;
				final double anguloCentral = Math.atan2(dy, dx);
				final double pasoAngular = this.anguloAperturaRad / (this.cantidadPerdigones - 1);
				final double anguloInicial = anguloCentral - (this.anguloAperturaRad / 2.0);

				for (int i = 0; i < this.cantidadPerdigones; i++) {
					final double anguloActual = anguloInicial + (i * pasoAngular);
					final double targetX = xOrigen + (Math.cos(anguloActual) * 1000.0);
					final double targetY = yOrigen + (Math.sin(anguloActual) * 1000.0);

					escenario.crearProyectil(new ProyectilPerdigon(this.damage, this.velocidadPerdigon, this.penetrante,
							this.alcance, escenario, xOrigen, yOrigen, targetX, targetY, causante, soloContraJugador));
				}
			}

			GestorSonido.reproducirEnPosicion(IDSonido.DISPARO_PISTOLA, xOrigen, yOrigen,
					Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
					Globales.CAMARA.getEntidadEnfocada().getPosicionY());
		}
	}

	/**
	 * Disparo cardinal con apertura de cono adaptada a la dirección.
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
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.CODIGO_MODELO);
		json.put("balasCargador", this.balasCargador);
		json.put("capacidadCargador", this.capacidadCargador);
		return json;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.add("Daño: " + this.damage + " pts x " + this.cantidadPerdigones + " perdigones.");
		listaInfo.add(
				"Cargador: " + this.getMunicion().getCantidad() + "/" + this.getMunicion().getLimite() + " balas.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
		listaInfo.add("Tiempo de recarga: " + (this.tiempoRecargaMs / 1000.0) + " s.");
		listaInfo.add("Apertura: " + (int) Math.toDegrees(this.anguloAperturaRad) + "°.");
		listaInfo.add("Munición: Calibre 12.");
	}
}