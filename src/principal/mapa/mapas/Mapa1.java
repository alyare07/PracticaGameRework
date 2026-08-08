package principal.mapa.mapas;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;

import org.json.simple.JSONObject;

import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.eventos.EventoJugadorZonaTP;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;

public class Mapa1 extends Mapa{
    public static final String NOMBRE_MAPA = "Mapa1";
    public static final String EXTERIOR = "Exterior";

    public Mapa1(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp) {
	super(gc, porcentajeCarga, gp);
    }

    public Mapa1(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp, final JSONObject jsonMapa) {
	super(gc, porcentajeCarga, gp, jsonMapa);
    }

    @Override
    protected void establecerMundos(final GestorCarga gc, final int porcentajeCarga) {
	final int cantMundos = 1;
	final int porcentajeCargaParcial = porcentajeCarga / cantMundos;
	final int porcentajeCargaEscenario = 75 * porcentajeCargaParcial / 100;
	final int porcentajeCargaMundo = 25 * porcentajeCargaParcial / 100;

	gc.setDetalleCarga("Generando mundo " + EXTERIOR);
//	this.mundo = new Mundo(esc, new Point(776, 300), gc, 25);
	this.MUNDOS.put(EXTERIOR, new Mundo(this.cargarEscenario(gc, porcentajeCargaEscenario, new File("escenario1.json")), new Point(776, 280), gc, porcentajeCargaMundo));

    }

    @Override
    protected void establecerMundoActual() {
	this.mundoActual = this.MUNDOS.get(EXTERIOR);
    }

    @Override
    protected void cargarFuncionalidadesPropias() {
	final GestorJuego jg = this.GP.getGestorJuego();
	final ZonaTP zonaTP2 = new ZonaTP(new Rectangle(684, 215, 20, 20), null);
//	this.mundoActual.eliminarCriaturas();
	final ZonaTP zonaTP = new ZonaTP(new Rectangle(878, 173, 20, 20), new PuertaArea(new Rectangle(832, 333, 16, 16)));
	zonaTP2.setPuertaTP(new PuertaMapa(MapaManager.MAPA_0, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, false, this.GP));
	this.mundoActual.meterEntidad(zonaTP);
	this.mundoActual.meterEntidad(zonaTP2);
	jg.meterEvento(new EventoJugadorZonaTP(zonaTP, jg, true));
	jg.meterEvento(new EventoJugadorZonaTP(zonaTP2, jg, true));
	this.mundoActual.meterEntidad(new BandidoGarrote(890, 220, 50, 50, this.mundoActual));
	this.mundoActual.meterEntidad(new BandidoGarrote(897, 220, 50, 50, this.mundoActual));
	this.mundoActual.meterEntidad(new BandidoGarrote(876, 220, 50, 50, this.mundoActual));
	this.mundoActual.meterEntidad(new BandidoGranadero(927, 64, 50, 50, this.mundoActual));
	this.mundoActual.meterEntidad(new BandidoPistolero(670, 121, 50, 50, this.mundoActual));
//	this.mundoActual.agregarCriatura(new Enemigo(976, 90, 16, 16, 50, 50,
//		Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/sprites/jugadores.png").getSubimage(48, 48, 48, 48), this.mundoActual));

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
