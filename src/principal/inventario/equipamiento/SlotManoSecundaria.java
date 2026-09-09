package principal.inventario.equipamiento;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.utilidades.Globales;

/**
 * Ranura de equipamiento especializada para la Mano Secundaria / Offhand
 * (Antorchas, Linternas, Escudos y Talismanes). Sincroniza dinámicamente
 * fuentes de luz portátiles con el Jugador sin asignaciones en caliente
 * (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class SlotManoSecundaria extends SlotEquipamiento {

	private FuenteLuz luzPortatil = null;

	public SlotManoSecundaria(final Rectangle area, final BufferedImage logo) {
		super(area, logo);
	}

	@Override
	public boolean validarAdmisionItem(final Item i) {
		if (i == null) {
			return true;
		}
		// No permite armas primarias en mano secundaria
		return !(i instanceof Arma);
	}

	@Override
	public void establecerObjeto(final Item obj) {
		super.establecerObjeto(obj);
		this.actualizarEfectoPortatil();
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.recalcularAtributos();
		}
	}

	@Override
	public void eliminarObjeto() {
		super.eliminarObjeto();
		this.desactivarLuzPortatil();
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.recalcularAtributos();
		}
	}

	private void actualizarEfectoPortatil() {
		if ((this.item != null) && (Globales.JUGADOR != null) && (Globales.GESTOR_LUZ != null)) {
			final String nombreLower = this.item.getNombre().toLowerCase();

			if (nombreLower.contains("antorcha") || nombreLower.contains("fuego")) {
				if (this.luzPortatil == null) {
					this.luzPortatil = Globales.GESTOR_LUZ.agregarLuzAnclada(Globales.JUGADOR, TipoLuz.ANTORCHA, 85.0);
				} else {
					this.luzPortatil.setTipo(TipoLuz.ANTORCHA);
				}
				return;
			}
			if (nombreLower.contains("linterna") || nombreLower.contains("farol")) {
				if (this.luzPortatil == null) {
					this.luzPortatil = Globales.GESTOR_LUZ.agregarLuzAnclada(Globales.JUGADOR, TipoLuz.LINTERNA_CONICA,
							130.0);
				} else {
					this.luzPortatil.setTipo(TipoLuz.LINTERNA_CONICA);
				}
				return;
			}
		}
		this.desactivarLuzPortatil();
	}

	private void desactivarLuzPortatil() {
		if (this.luzPortatil != null) {
			this.luzPortatil.apagar();
			this.luzPortatil = null;
		}
	}
}