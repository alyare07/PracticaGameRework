package principal.inventario.slot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.utilidades.DibujoDebug;

public class SlotIGU {
	private final Slot SLOT;
	private final Rectangle AREA_IGU;
	
	public SlotIGU(final Slot slot, final int xIGU, final int yIGU) {
		this.SLOT = slot;
		this.AREA_IGU = new Rectangle(xIGU,yIGU,slot.AREA.width,slot.AREA.height);
	}
	
	
	public void pintar(final Graphics2D g) {
		this.SLOT.pintar(g, AREA_IGU);
		DibujoDebug.dibujarRectanguloContorno(g, AREA_IGU.x-1, AREA_IGU.y-1,AREA_IGU.width+2,AREA_IGU.height+2, Color.black);
	}
	
	public void actualizar(final Raton raton) {
		this.SLOT.actualizarIGU(raton, AREA_IGU);
		
	}
	
	public boolean apuntado() {
		return this.SLOT.estaApuntado();
	}
	
	public boolean contieneItem() {
		return this.SLOT.contieneItem();
	}
	
	public void pintarTooltip(final Graphics2D g) {
		this.SLOT.pintarTooltip(g);
	}
	
	public Item getItem() {
		return this.SLOT.getItem();
	}
	
	public void establecerObjeto(final Item i) {
		this.SLOT.establecerObjeto(i);
	}
	
	
	
	
	
}
