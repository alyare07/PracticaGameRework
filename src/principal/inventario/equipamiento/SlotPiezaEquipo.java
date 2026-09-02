package principal.inventario.equipamiento;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.utilidades.Globales;

/**
 * Slot de inventario restringido por TipoEquipo (Casco, Torso, Botas, Anillo).
 * Notifica al Jugador para recalcular sus estadísticas derivadas en O(1).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class SlotPiezaEquipo extends SlotEquipamiento {

	private final TipoEquipo tipoRequerido;

	public SlotPiezaEquipo(final Rectangle area, final BufferedImage logo, final TipoEquipo tipoRequerido) {
		super(area, logo);
		this.tipoRequerido = (tipoRequerido != null) ? tipoRequerido : TipoEquipo.CASCO;
	}

	@Override
	public boolean validarAdmisionItem(final Item i) {
		if (i == null) {
			return true; // Permite desequipar
		}
		if (i instanceof PiezaEquipo) {
			return ((PiezaEquipo) i).getTipoEquipo() == this.tipoRequerido;
		}
		return false;
	}

	@Override
	public void establecerObjeto(final Item obj) {
		super.establecerObjeto(obj);
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.recalcularAtributos();
		}
	}

	@Override
	public void eliminarObjeto() {
		super.eliminarObjeto();
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.recalcularAtributos();
		}
	}

	public TipoEquipo getTipoRequerido() {
		return this.tipoRequerido;
	}
}