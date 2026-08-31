package principal.entes.objetos.items.armas.distancia.fuego;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.proyectil.ProyectilBala;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Representa un arma de fuego de mano (Pistola estándar de 9mm). Integra
 * cargador de 12 proyectiles, cadencia semi-automática, balística en 360 grados
 * y deserialización JSON segura contra nulos.
 * 
 * @version 3.1 (Java 8 Compatible - Zero-GC Architecture)
 */
public class Pistola extends Arma {

	private static final long serialVersionUID = -9196405156055570071L;

	protected final double velocidadDisparo = 3.5;
	protected final int tamanoBala360 = 3;

	public Pistola(final String codModelo) {
		super(codModelo, 10, 250, false, 12, 1200, 400, ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Pistola(final int x, final int y, final String codModelo) {
		super(x, y, codModelo, 10, 250, false, 12, 1200, 400, ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA);
		this.rellenarInfo(this.LISTA_INFO);
	}

	public Pistola(final int x, final int y, final String codModelo, final int balasCargador) {
		super(x, y, codModelo, 10, 250, false, 12, 1200, 400, ListaModelosItem.COD_CONSUMIBLE_MUNICION_PISTOLA);
		this.balasCargador = Math.max(0, Math.min(this.capacidadCargador, balasCargador));
		this.rellenarInfo(this.LISTA_INFO);
	}

	/**
	 * Dispara una bala vectorial en 360 grados apuntando a una coordenada exacta
	 * del mundo.
	 */
	@Override
	public void disparar(final int xOrigen, final int yOrigen, final int xDestino, final int yDestino,
			final Mundo escenario, final Criatura causante, final boolean soloContraJugador) {

		if (this.consumirDisparo(causante)) {
			if (escenario != null) {
				escenario.crearProyectil(new ProyectilBala(this.damage, this.velocidadDisparo, this.penetrante,
						this.alcance, escenario, xOrigen, yOrigen, xDestino, yDestino, this.tamanoBala360,
						this.tamanoBala360, causante, soloContraJugador));
			}

			GestorSonido.reproducirEnPosicion(IDSonido.DISPARO_PISTOLA, xOrigen, yOrigen,
					Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
					Globales.CAMARA.getEntidadEnfocada().getPosicionY());
		}
	}

	/**
	 * Disparo cardinal adaptado.
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

	@Override
	public Objeto copiar() {
		return new Pistola(this.getPosicionXInt(), this.getPosicionYInt(), this.CODIGO_MODELO, this.balasCargador);
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

	/**
	 * Reconstruye la pistola desde JSON con soporte de retrocompatibilidad y
	 * protección contra nulos.
	 */
	public static Pistola crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Pistola(ListaModelosItem.COD_EQUIPABLE_ARMA);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String codModelo = (json.get("codModelo") != null) ? json.get("codModelo").toString()
				: ListaModelosItem.COD_EQUIPABLE_ARMA;

		// Retrocompatibilidad: Si no existe "balasCargador", lee "municion" antigua o
		// usa 12 por defecto
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
		listaInfo.add("Daño: " + this.damage + " pts.");
		listaInfo.add(
				"Cargador: " + this.getMunicion().getCantidad() + "/" + this.getMunicion().getLimite() + " balas.");
		listaInfo.add("Cadencia: " + this.cadenciaMs + " ms.");
		listaInfo.add("Tiempo de recarga: " + (this.tiempoRecargaMs / 1000.0) + " s.");
		listaInfo.add("Munición requerida: 9mm.");
	}
}