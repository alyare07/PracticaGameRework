package principal.mapa.mapas;

import java.awt.Point;
import java.awt.Rectangle;

import org.json.simple.JSONObject;

import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.cofres.CofreMediano;
import principal.eventos.EventoJugadorZonaTP;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.recursos.TipoTerreno;

public class MapaPlano extends Mapa {
	public static final String NOMBRE_MAPA = "Mapa Plano";
	public static final String EXTERIOR = "Exterior";

	public MapaPlano(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp) {
		super(gc, porcentajeCarga, gp);
	}

	public MapaPlano(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp,
			final JSONObject jsonMapa) {
		super(gc, porcentajeCarga, gp, jsonMapa);
	}

	@Override
	protected void establecerMundos(final GestorCarga gc, final int porcentajeCarga) {
		gc.setDetalleCarga("Generando terreno");
		final Terreno t = new Terreno(1500, 1500, 16, TipoTerreno.CESPED_2);
		this.MUNDOS.put(EXTERIOR, new Mundo(new Escenario(t, "[]", "[]", "[]", "[]"), new Point(326, 268)));
	}

	@Override
	protected void establecerMundoActual() {
		this.mundoActual = this.MUNDOS.get(EXTERIOR);
	}

	@Override
	protected void cargarFuncionalidadesPropias() {
		final GestorJuego jg = this.GP.getGestorJuego();
		this.mundoActual.meterEntidad(new CofreMediano(30, 100));
		this.mundoActual.meterEntidad(new Complemento(300, 200, ListaModeloComplemento.COD_CASA_1));

		final ZonaTP zonaTP2 = new ZonaTP(new Rectangle(184, 215, 20, 20), null);
		zonaTP2.setPuertaTP(new PuertaMapa(MapaManager.MAPA_1, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, true, this.GP));
		this.mundoActual.meterEntidad(zonaTP2);
		jg.meterEvento(new EventoJugadorZonaTP(zonaTP2, jg, true));
	}

	@Override
	public String[] getNombreMundos() {
		final String[] lista = { EXTERIOR };
		return lista;
	}

	@Override
	public String getNombre() {
		return NOMBRE_MAPA;
	}
}