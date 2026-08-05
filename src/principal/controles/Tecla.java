package principal.controles;

import org.json.simple.JSONObject;

import principal.utilidades.Constantes;

public class Tecla {
	protected int codigoTecla;
	protected boolean presionada;
	protected boolean accionarInvertible;
	protected final String nombre;
	protected boolean presionadaUnaSolaVez;
	private int codActPresionado;
	
	public Tecla(final int codigo,final String nombre) {
		this.codigoTecla = codigo;
		this.nombre = nombre;
	}
	
	public Tecla(final int codigo, final boolean accionarInvertible, final String nombre) {
		this.codigoTecla = codigo;
		this.accionarInvertible = accionarInvertible;
		this.nombre = nombre;
	}
	
	public int getCodigoTecla() {
		return this.codigoTecla;
	}
	
	public boolean presionado() {
		return  this.presionada;
	}
	
	public void establecerCodigoTecla(final int codigoTecla) {
		this.codigoTecla = codigoTecla;
	}
	
	public void presionar() {
		if(this.accionarInvertible) {
			this.presionada = !this.presionada;
		}else if(!presionada) {
			this.codActPresionado = Constantes.getCodActualizacion();
			this.presionada = true;
		}else if(Constantes.getCodActualizacion() == (this.codActPresionado+1)) { //Esto es para que se haga verdadero solo en la siguiente actualizacion
			this.presionadaUnaSolaVez = true;
		}else if(this.presionadaUnaSolaVez) {
			this.presionadaUnaSolaVez = false;
		}
		
	}
	
	public boolean presionadoUnicaActualizacion() {
		return this.presionadaUnaSolaVez;
	}
	
	public void establecerAccionarInvertible(final boolean b) {
		this.accionarInvertible = b;
	}
	
	public boolean accionarInvertible() {
		return this.accionarInvertible;
	}
	
	public void soltar() {
		if(presionada) {
			this.presionada = false;
		}
	}
	
	
	public String getNombre() {
		return nombre;
	}
	
	@SuppressWarnings("unchecked")
	public void agregarEnJSON(final JSONObject jo) {
		jo.put(this.nombre, this.codigoTecla);
	}
	
	

}
