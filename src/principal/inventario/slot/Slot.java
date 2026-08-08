package principal.inventario.slot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.inventario.Inventario;
import principal.inventario.equipamiento.SlotManager;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public class Slot {
	protected final Rectangle AREA;
	protected Item item;
	protected final int MARGEN_ESPACIADO;
	protected boolean apuntado;
	protected int valorPrioridad; //mientras mas chico, mayor prioridad

	public Slot(final Rectangle area, final Item item) {
		this.AREA = area;
		this.item = item;
		this.MARGEN_ESPACIADO = 2;
	}

	public Slot(final Rectangle area) {
		this.AREA = area;
		this.MARGEN_ESPACIADO = 2;
	}

	public Slot(final int x, final int y) {
		this.AREA = new Rectangle(x, y, SlotManager.getLadoSlots(), SlotManager.getLadoSlots());
		this.MARGEN_ESPACIADO = 2;
	}

	public void actualizar(final Raton raton) {
		if (raton.getRectanguloPosicionEscalado().intersects(this.AREA)) {
			if (!this.apuntado) {
				this.apuntado = true;
			}
		} else {
			if (apuntado) {
				this.apuntado = false;
			}
		}
		this.verificarEliminacion();
	}
	
	public void actualizarIGU(final Raton raton, final Rectangle area) {
		if (raton.getRectanguloPosicionEscalado().intersects(area)) {
			if (!this.apuntado) {
				this.apuntado = true;
			}
		} else {
			if (apuntado) {
				this.apuntado = false;
			}
		}
		this.verificarEliminacion();
		
	}
	
	protected void verificarEliminacion() {
		if(this.contieneItem()&&this.item.estaEliminado()) {
			System.out.println("se ha eliminado el objeto: "+this.item);
			this.eliminarObjeto();
		}
	}

	public void pintar(final Graphics2D g) {
		pintarArea(g,this.AREA);
		pintarObjeto(g,this.AREA);

	}
	
	public void pintar(final Graphics2D g, final Rectangle area) {
		pintarArea(g, area);
		pintarObjeto(g, area);

	}
	
	public void pintarSoloSlot(final Graphics2D g) {
		pintarArea(g,this.AREA);
	}

	public boolean ratonIntersecta(final Raton raton) {
		return raton.getRectanguloPosicionEscalado().intersects(this.AREA);
	}

	public boolean intersecta(final Point punto) {
		return this.AREA.intersects(new Rectangle(punto.x, punto.y, 1, 1));
	}

	public void pintarTooltip(final Graphics2D g) {
		
		if (apuntado && contieneItem()) {
			g.setFont(g.getFont().deriveFont(5f));
			Constantes.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipItem(g, item);
			g.setFont(g.getFont().deriveFont(9f));
		}

	}

	protected void pintarArea(final Graphics2D g, final Rectangle area) {
		DibujoDebug.dibujarRectanguloRelleno(g, area, Inventario.BLANCO_TRANSPARENTE);
		if (this.apuntado) {
			DibujoDebug.dibujarRectanguloContorno(g, area, Color.YELLOW);
		}
		

	}

	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		float aux = g.getFont().getSize();
		if (item != null) {
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);
			if (this.item instanceof Consumible) {
				g.setFont(g.getFont().deriveFont(5f));
				DibujoDebug.dibujarRectanguloRelleno(g, area.x, area.y, 6, 6, Color.LIGHT_GRAY);
				DibujoDebug.dibujarString(g, String.valueOf(((Consumible) this.item).getCantidad()), area.x,
						area.y + 6, Color.black);
			}else if(item instanceof Pistola) {
				
				g.setFont(g.getFont().deriveFont(4f));
				
				final String cantidadBalas = String.valueOf(((Pistola) this.item).getMunicion().getCantidad());
				final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, cantidadBalas);
				final int altoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, cantidadBalas);
				
				DibujoDebug.dibujarRectanguloRelleno(g, area.x, area.y +area.height-altoTexto-1, 11, 6, Color.LIGHT_GRAY);
				DibujoDebug.dibujarString(g, cantidadBalas, area.x, area.y +area.height - (altoTexto/2), Color.black);
				DibujoDebug.dibujarImagen(g, Textura.getTextura(Textura.TEXTURA_x4_BALA), area.x+anchoTexto, area.y+ area.height - altoTexto);
				
			}

		}
		g.setFont(g.getFont().deriveFont(aux));
	}

	public void establecerObjeto(final Item obj) {
		this.item = obj;
	}
	
	public void eliminarObjeto() {
		this.item = null;
	}

	public boolean contieneItem() {
		return this.item != null;
	}

	public Item getItem() {
		return this.item;
	}
	
	public Arrojadizo getItemArrojadizo() {
		return (Arrojadizo)this.item;
	}

	public boolean estaApuntado() {
		return this.apuntado;
	}
	
	public void setX(final int x) {
		this.AREA.x = x;
	}
	
	public void setY(final int y) {
		this.AREA.y = y;
	}
	
	public void setValorPrioridad(final int valor) {
		this.valorPrioridad = valor;
	}
	
	public void setAncho(final int ancho) {
		this.AREA.width = ancho;
	}
	
	public void setAlto(final int alto) {
		this.AREA.height = alto;
	}
	
	public int getX() {
		return this.AREA.x;
	}
	
	public int getY() {
		return this.AREA.y;
	}
	
	public int getValorPrioridad() {
		return this.valorPrioridad;
	}
	
	public int getAncho() {
		return this.AREA.width;
	}

}
