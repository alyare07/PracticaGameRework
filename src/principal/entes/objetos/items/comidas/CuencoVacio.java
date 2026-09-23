package principal.entes.objetos.items.comidas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.clima.TipoClima;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.igu.textos.TipoTextoFlotante;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.recursos.TexturaItem;
import principal.recursos.TipoTerreno;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Recipiente de madera para recolección de líquidos en ríos o lluvia (Zero-GC /
 * O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class CuencoVacio extends Consumible {

	private static final long serialVersionUID = 1L;

	public static final String CODIGO = "Cuenco de Madera";
	public static final int LIMITE_PILA = 10;

	public CuencoVacio(final int x, final int y, final int cantidad) {
		super(x, y, cantidad, CODIGO, CODIGO, TexturaItem.CUENCO_VACIO_INV, TexturaItem.CUENCO_VACIO_MAPA, LIMITE_PILA);
		this.setPrecioBasePlata(1L);
	}

	public CuencoVacio(final int cantidad) {
		this(0, 0, cantidad);
	}

	@Override
	public void consumir(final Criatura c) {
		if (!(c instanceof Jugador)) {
			return;
		}

		final Jugador j = (Jugador) c;
		final Mundo mundo = j.getMundo();
		if (mundo == null) {
			return;
		}

		final boolean cercaAgua = this.hayAguaAdyacente(j, mundo);
		final boolean lloviendoAlAireLibre = this.estaBajoLluvia(j);

		if (cercaAgua || lloviendoAlAireLibre) {
			GestorSonido.reproducir(IDSonido.RECOGER);

			// Consume un cuenco vacío y entrega agua sucia
			this.reducirCantidad(1);

			final CuencoAguaSucia aguaSucia = new CuencoAguaSucia(1);
			final boolean guardado = Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(aguaSucia);

			if (!guardado) {
				mundo.meterEntidad(new CuencoAguaSucia(j.getCentroX(), j.getPosicionYInt(), 1));
			}

			final String origen = cercaAgua ? "+Agua de Río" : "+Agua de Lluvia";
			Globales.GESTOR_TEXTOS.agregarTexto(origen, j.getCentroX(), j.getPosicionYInt() - 8,
					TipoTextoFlotante.ESTADO);
		} else {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			Globales.GESTOR_TEXTOS.agregarTexto("¡Requiere agua o lluvia!", j.getCentroX(), j.getPosicionYInt() - 8,
					TipoTextoFlotante.BLOQUEO);
		}
	}

	private boolean hayAguaAdyacente(final Jugador j, final Mundo mundo) {
		final Terreno terreno = mundo.getTerreno();
		if (terreno == null) {
			return false;
		}

		final int cx = j.getCentroX();
		final int cy = j.getPosicionYBase();

		// Escaneo en cruz a 16 px del jugador (Zero-GC)
		final Tile tCentro = terreno.getTileReferenciado(cx, cy);
		final Tile tSur = terreno.getTileReferenciado(cx, cy + 16);
		final Tile tNorte = terreno.getTileReferenciado(cx, cy - 16);
		final Tile tEste = terreno.getTileReferenciado(cx + 16, cy);
		final Tile tOeste = terreno.getTileReferenciado(cx - 16, cy);

		return this.esAgua(tCentro) || this.esAgua(tSur) || this.esAgua(tNorte) || this.esAgua(tEste)
				|| this.esAgua(tOeste);
	}

	private boolean esAgua(final Tile t) {
		if (t == null) {
			return false;
		}
		return (t.getTipoTerreno() == TipoTerreno.AGUA) || (t.getTipoFondo() == TipoTerreno.AGUA);
	}

	private boolean estaBajoLluvia(final Jugador j) {
		if (Globales.GESTOR_CLIMA == null) {
			return false;
		}
		final TipoClima clima = Globales.GESTOR_CLIMA.getClimaActual();
		final boolean lloviendo = (clima == TipoClima.LLUVIA_LEVE) || (clima == TipoClima.LLUVIA_TORMENTA);
		final boolean bajoTecho = (Globales.GESTOR_TERMICO_JUGADOR != null)
				&& Globales.GESTOR_TERMICO_JUGADOR.isBajoTechoInterior();

		return lloviendo && !bajoTecho;
	}

	@Override
	protected void rellenarInfo(final ArrayList<String> listaInfo) {
		listaInfo.add("Cuenco de madera tallada.");
		listaInfo.add("Uso: Llenar en ríos o bajo la lluvia.");
	}

	@Override
	public Objeto copiar() {
		return new CuencoVacio(this.getPosicionXInt(), this.getPosicionYInt(), this.getCantidad());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("codModelo", this.codigoModelo);
		json.put("cantidad", Integer.valueOf(this.getCantidad()));
		return json;
	}

	public static CuencoVacio crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new CuencoVacio(1);
		}
		final int cant = (json.get("cantidad") != null) ? ((Number) json.get("cantidad")).intValue() : 1;
		return new CuencoVacio(0, 0, cant);
	}
}