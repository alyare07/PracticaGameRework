package principal.inventario.equipamiento;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.utilidades.Globales;

/**
 * Ranura de equipamiento especializada para la Mano Secundaria / Offhand
 * (Antorchas, Linternas, Escudos). Notifica reactivamente al jugador para
 * sincronizar su iluminación sin fugas de luz.
 * 
 * @version 2.0 (Vanilla Java 8 - Unified Light Source Pipeline)
 */
public class SlotManoSecundaria extends SlotEquipamiento {

	public SlotManoSecundaria(final Rectangle area, final BufferedImage logo) {
		super(area, logo);
	}

	@Override
	public boolean validarAdmisionItem(final Item i) {
		if (i == null) {
			return true;
		}
		// No permite armas de fuego principales en mano secundaria
		return !(i instanceof Arma);
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
}