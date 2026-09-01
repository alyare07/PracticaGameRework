package principal.entes.objetos.items.arrojadizos;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.objetos.items.Consumible;
import principal.mapa.Mundo;

public abstract class Arrojadizo extends Consumible {

	private static final long serialVersionUID = -1126293944952431996L;

	protected final int DIAMENTRO_DEL_AREA;

	public Arrojadizo(final int x, final int y, final int cantidad, final int diametroArea, final String codModelo) {
		super(x, y, cantidad, codModelo);
		this.DIAMENTRO_DEL_AREA = diametroArea;
	}

	public Arrojadizo(final int cantidad, final int diametroArea, final String codModelo) {
		super(cantidad, codModelo);
		this.DIAMENTRO_DEL_AREA = diametroArea;
	}

	@Override
	public void consumir(final Criatura c) {
		if ((this.getCantidad() > 0) && !c.vidaCompleta()) {
			this.reducirCantidad(1);
		}
	}

	public int getDiamentroAreaCaida() {
		return this.DIAMENTRO_DEL_AREA;
	}

	public abstract double getTiempoMsCaidaEnAnchoPantalla();

	public abstract void arrojar(final int xDestino, final int yDestino, final Direccion direccion,
			final Mundo escenario, final Criatura causante);
}