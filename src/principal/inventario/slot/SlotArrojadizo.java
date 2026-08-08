package principal.inventario.slot;

import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.utilidades.Constantes;

public class SlotArrojadizo extends Slot {

	public SlotArrojadizo() {
		super(0, 0);
	}
	
	
	public void actualizar(final Raton raton) {
		this.verificarEliminacion();
		if(Constantes.INVENTARIO.esVisible()) {
			this.eliminarObjeto();
		}
	}
	
	public void establecerObjeto(final Item obj) {
		if(obj !=null && obj instanceof Arrojadizo)
			this.item = obj;
	}

}
