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
import principal.utilidades.Constantes;
import principal.utilidades.Sonidos;

public class Pistola extends Arma {
	private static final long serialVersionUID = -9196405156055570071L;
	protected final Municion municion;
	protected final double velocidadDisparo = 3;
	protected final int anchoBala = 4;
	protected final int altoBala = 2;
	
	public Pistola(String codModelo) {
		super(codModelo,10,250,false);
		this.municion = new Municion(20);
		this.rellenarInfo(LISTA_INFO);
	}
	
	public Pistola(int x, int y, String codModelo) {
		super(x, y, codModelo,10,250,false);
		this.municion = new Municion(20);
		this.rellenarInfo(LISTA_INFO);
	}
	
	public Pistola(String codModelo, final Municion municion) {
		super(codModelo,10,250,false);
		this.municion = municion;
		this.rellenarInfo(LISTA_INFO);
	}
	
	public Pistola(int x, int y, String codModelo, final Municion municion) {
		super(x, y, codModelo,10,250,false);
		this.municion = municion;
		this.rellenarInfo(LISTA_INFO);
	}
	
	
	public void disparar(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo escenario, final Criatura causante) {
		
		if(this.municion.utilizarMunicion()) {
			if(direccion == Direccion.OESTE || direccion == Direccion.ESTE) {
				escenario.crearProyectil(new ProyectilBala( this.damage,this.velocidadDisparo,this.penetrante, this.alcance,escenario,xOrigen, yOrigen,anchoBala,altoBala,direccion,causante));
			}else {
				escenario.crearProyectil(new ProyectilBala( this.damage,this.velocidadDisparo,this.penetrante, this.alcance,escenario,xOrigen, yOrigen,altoBala,anchoBala,direccion,causante));
			}
//			System.out.println(causante+" a disparado, municion ["+this.municion.getCantidad()+"/"+this.municion.getLimite()+"]");
			Sonidos.SONIDO_DISPARO_PISTOLA.reproducir();
		}else {
			Sonidos.SONIDO_SIN_MUNICION.reproducir();
		}
		
	}
	
	@Override
	public Objeto copiar() {
		return new Pistola(x, y, CODIGO_MODELO, municion);
	}


	@Override
	public Municion getMunicion() {
		return this.municion;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		JSONObject json = new JSONObject();
		json.put("x", x);
		json.put("y", y);
		json.put("codModelo", CODIGO_MODELO);
		json.put("municion", this.municion.getCantidad());
		json.put("municionLimite", this.municion.getLimite());
		return json;
	}
	
	public static Pistola crearDesdeJson(final JSONObject json) {
		int x = Integer.parseInt(json.get("x").toString());
		int y = Integer.parseInt(json.get("y").toString());
		String codModelo = json.get("codModelo").toString();
		int municion = Integer.parseInt(json.get("municion").toString());
		int municionLimite = Integer.parseInt(json.get("municionLimite").toString());
		
		return new Pistola(x, y, codModelo, new Municion(municionLimite, municion));
	}

	@Override
	public String exportarTipoItem() {
		return "Pistola";
	}

	@Override
	protected void rellenarInfo(ArrayList<String> listaInfo) {
		listaInfo.add("Daño de impacto  "+this.damage+"pts de vida.");
		listaInfo.add("Alcance del disparo  "+this.alcance+"mts.");
		listaInfo.add("Arma del tipo penetrante  "+this.esPenetrante());
		
	}

}
