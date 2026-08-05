package principal.entes.objetos.items.arrojadizos.granadas;



import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.entes.proyectil.ProyectilGranada;
import principal.mapa.Mundo;

public abstract class Granada extends Arrojadizo {
	private static final long serialVersionUID = -5344660789105549240L;
	protected final double DAMAGE;
	protected final double TIEMPO_MS_CAIDA_ANCHO_PANTALLA = 2000;

	public Granada(int cantidad, int diametroArea, final double damage, String codModelo) {
		super(cantidad, diametroArea, codModelo);
		this.DAMAGE = damage;
	}
	
	public Granada(int x, int y, int cantidad, int diametroArea, final double damage, String codModelo) {
		super(x, y, cantidad, diametroArea, codModelo);
		this.DAMAGE = damage;
	}
	

	@Override
	public void arrojar(int xDestino, int yDestino, Direccion direccion, Mundo escenario, Criatura causante) {
		escenario.crearProyectil(new ProyectilGranada(xDestino, yDestino, escenario, causante,this));
		this.reducirCantidad(1);
	}
	
	@Override
	public double getTiempoMsCaidaEnAnchoPantalla() {
		return this.TIEMPO_MS_CAIDA_ANCHO_PANTALLA;
	}
	
	public double getDamage() {
		return this.DAMAGE;
	}

}
