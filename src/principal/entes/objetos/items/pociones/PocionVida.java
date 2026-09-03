package principal.entes.objetos.items.pociones;

import principal.entes.criaturas.Criatura;
import principal.entes.objetos.items.Consumible;
import principal.recursos.TexturaItem;

public abstract class PocionVida extends Consumible {

	private static final long serialVersionUID = -836798363558613027L;
	protected final double PUNTOS_A_RESTAURAR;

	public PocionVida(final int x, final int y, final int cantidad, final String codModelo, final String nombre,
			final TexturaItem texInv, final TexturaItem texMapa, final int limite, final double puntosRestaurar) {
		super(x, y, cantidad, codModelo, nombre, texInv, texMapa, limite);
		this.PUNTOS_A_RESTAURAR = puntosRestaurar;
	}

	public PocionVida(final int cantidad, final String codModelo, final String nombre, final TexturaItem texInv,
			final TexturaItem texMapa, final int limite, final double puntosRestaurar) {
		super(cantidad, codModelo, nombre, texInv, texMapa, limite);
		this.PUNTOS_A_RESTAURAR = puntosRestaurar;
	}

	@Override
	public void consumir(final Criatura c) {
		if ((this.getCantidad() > 0) && (c != null) && !c.vidaCompleta()) {
			c.curar(this.PUNTOS_A_RESTAURAR);
			this.reducirCantidad(1);
		}
	}
}