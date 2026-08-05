package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import principal.entes.objetos.items.Item;
import principal.inventario.slot.Slot;
import principal.utilidades.DibujoDebug;

public abstract class SlotEquipamiento extends Slot {
	protected final BufferedImage logo;
	
	public SlotEquipamiento(Rectangle area , final BufferedImage logo) {
		super(area);
		this.logo = logo;
	}
	
	
	protected void pintarArea(final Graphics2D g, final Rectangle area) {
		DibujoDebug.dibujarRectanguloRelleno(g, area, Color.cyan);
		if (this.apuntado) {
			DibujoDebug.dibujarRectanguloContorno(g, area, Color.blue);
		}

	}

	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (item != null) {
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO,area.y + this.MARGEN_ESPACIADO);
		}else {
			DibujoDebug.dibujarImagen(g, logo, area.x, area.y);
		}

	}
	
	public void establecerObjeto(final Item obj) {
		if(this.validarAdmisionItem(obj)) this.item = obj;
	}
	
	public abstract boolean validarAdmisionItem(final Item i);
	
	
	
	
	
	



	

	

}
