package principal.entes.objetos.items.armas.distancia.fuego;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.entes.proyectil.ProyectilBala;
import principal.mapa.Mundo;
import principal.utilidades.Sonidos;

public class Pistola extends Arma{
    private static final long serialVersionUID = -9196405156055570071L;
    protected final Municion municion;
    protected final double velocidadDisparo = 3;
    protected final int anchoBala = 4;
    protected final int altoBala = 2;

    public Pistola(final String codModelo) {
	super(codModelo, 10, 250, false);
	this.municion = new Municion(20);
	this.rellenarInfo(this.LISTA_INFO);
    }

    public Pistola(final int x, final int y, final String codModelo) {
	super(x, y, codModelo, 10, 250, false);
	this.municion = new Municion(20);
	this.rellenarInfo(this.LISTA_INFO);
    }

    public Pistola(final String codModelo, final Municion municion) {
	super(codModelo, 10, 250, false);
	this.municion = municion;
	this.rellenarInfo(this.LISTA_INFO);
    }

    public Pistola(final int x, final int y, final String codModelo, final Municion municion) {
	super(x, y, codModelo, 10, 250, false);
	this.municion = municion;
	this.rellenarInfo(this.LISTA_INFO);
    }

    public void disparar(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo escenario, final Criatura causante, final boolean soloContraJugador) {

	if (this.municion.utilizarMunicion()) {
	    if (direccion == Direccion.OESTE || direccion == Direccion.ESTE) {
		escenario.crearProyectil(new ProyectilBala(this.damage, this.velocidadDisparo, this.penetrante, this.alcance, escenario, xOrigen, yOrigen, this.anchoBala, this.altoBala, direccion,
			causante, soloContraJugador));
	    } else {
		escenario.crearProyectil(new ProyectilBala(this.damage, this.velocidadDisparo, this.penetrante, this.alcance, escenario, xOrigen, yOrigen, this.altoBala, this.anchoBala, direccion,
			causante, soloContraJugador));
	    }
//			System.out.println(causante+" a disparado, municion ["+this.municion.getCantidad()+"/"+this.municion.getLimite()+"]");
	    Sonidos.SONIDO_DISPARO_PISTOLA.reproducir();
	} else {
	    Sonidos.SONIDO_SIN_MUNICION.reproducir();
	}

    }

    @Override
    public Objeto copiar() {
	return new Pistola(this.x, this.y, this.CODIGO_MODELO, this.municion);
    }

    @Override
    public Municion getMunicion() {
	return this.municion;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected JSONObject exportarParaJSON() {
	final JSONObject json = new JSONObject();
	json.put("x", this.x);
	json.put("y", this.y);
	json.put("codModelo", this.CODIGO_MODELO);
	json.put("municion", this.municion.getCantidad());
	json.put("municionLimite", this.municion.getLimite());
	return json;
    }

    public static Pistola crearDesdeJson(final JSONObject json) {
	final int x = Integer.parseInt(json.get("x").toString());
	final int y = Integer.parseInt(json.get("y").toString());
	final String codModelo = json.get("codModelo").toString();
	final int municion = Integer.parseInt(json.get("municion").toString());
	final int municionLimite = Integer.parseInt(json.get("municionLimite").toString());

	return new Pistola(x, y, codModelo, new Municion(municionLimite, municion));
    }

    @Override
    public String exportarTipoItem() {
	return "Pistola";
    }

    @Override
    protected void rellenarInfo(final ArrayList<String> listaInfo) {
	listaInfo.add("Daño de impacto  " + this.damage + "pts de vida.");
	listaInfo.add("Alcance del disparo  " + this.alcance + "mts.");
	listaInfo.add("Arma del tipo penetrante  " + this.esPenetrante());

    }

}
