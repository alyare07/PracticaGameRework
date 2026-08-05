package principal.entes.objetos.items.pociones;


import principal.entes.criaturas.Criatura;
import principal.entes.objetos.items.Consumible;

public abstract class PocionVida extends Consumible {

	private static final long serialVersionUID = -836798363558613027L;
	protected final double PUNTOS_A_RESTAURAR;
	
	public PocionVida(int cantidad, String codModelo, final double puntosRestaurar) {
		super(cantidad, codModelo);
		this.PUNTOS_A_RESTAURAR = puntosRestaurar;
		
	}
	
	public PocionVida(int x, int y, int cantidad, String codModelo, final double puntosRestaurar) {
		super(x, y, cantidad, codModelo);
		this.PUNTOS_A_RESTAURAR = puntosRestaurar;
	
	}

	@Override
	public void consumir(final Criatura c) {
		if(this.getCantidad()>0 && !c.vidaCompleta()) {
			c.curar(this.PUNTOS_A_RESTAURAR);
			System.out.println("Se ha curado a la Criatura: "+ c.getClass().getName());
			this.reducirCantidad(1);
		}
	}
	
	
	
	
	
	


}
