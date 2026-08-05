package principal.entes.objetos.items.armas;

import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;

public abstract class Arma extends Portable {
	protected final boolean penetrante;
	protected final int damage;
	protected final int alcance;

	private static final long serialVersionUID = -1515324317822932516L;

	public Arma(String codModelo, final int damage, final int alcance, final boolean penetrante) {
		super(codModelo);
		this.penetrante = penetrante;
		this.alcance = alcance;
		this.damage = damage;
	}
	
	public Arma(int x, int y, String codModelo, final int damage, final int alcance, final boolean penetrante) {
		super(x, y, codModelo);
		this.penetrante = penetrante;
		this.alcance = alcance;
		this.damage = damage;
	}
	
	
	public int getAlcance() {
		return this.alcance;
	}
	public int getAtaque() {
		return this.damage;
	}
	public boolean esPenetrante() {
		return this.penetrante;
	}
	public abstract Municion getMunicion();
	
	

	

}
