package principal.utilidades.inventario;

import java.awt.Graphics2D;
import java.awt.Point;

import principal.entes.objetos.items.Item;
import principal.inventario.slot.Slot;
import principal.mapa.Mundo;
import principal.utilidades.Render2D;

public final class ItemPuntero {

	private Item item;
	private Slot slotOrigen;

	public ItemPuntero() {
		this.item = null;
		this.slotOrigen = null;
	}

	/**
	 * Extrae el ítem de un slot y lo pone en el cursor.
	 */
	public boolean agarrarItem(final Slot slot) {
		if ((slot == null) || !slot.contieneItem() || !slot.puedeExtraer()) {
			return false;
		}

		this.item = slot.getItem();
		this.slotOrigen = slot;
		slot.eliminarObjeto();
		return true;
	}

	/**
	 * Coloca o intercambia el ítem sostenido con el slot destino con validación
	 * cruzada.
	 */
	public boolean interactuarConSlot(final Slot slotDestino) {
		if ((slotDestino == null) || (this.item == null)) {
			return false;
		}

		// CASO 1: Slot destino vacío
		if (!slotDestino.contieneItem()) {
			if (slotDestino.puedeAceptar(this.item)) {
				slotDestino.establecerObjeto(this.item);
				this.limpiar();
				return true;
			}
			return false;
		}

		// CASO 2: Mismo slot de origen -> Devolver sin costo
		if (slotDestino == this.slotOrigen) {
			slotDestino.establecerObjeto(this.item);
			this.limpiar();
			return true;
		}

		// CASO 3: Intercambio (Swap) con validación bidireccional
		final Item itemDestino = slotDestino.getItem();
		final boolean destinoAcepta = slotDestino.puedeAceptar(this.item);
		final boolean origenAcepta = (this.slotOrigen == null) || this.slotOrigen.puedeAceptar(itemDestino);

		if (destinoAcepta && origenAcepta) {
			slotDestino.establecerObjeto(this.item);
			this.item = itemDestino;
			this.slotOrigen = slotDestino;
			return true;
		}

		return false;
	}

	/**
	 * Cancela el arrastre devolviendo el ítem a su origen o soltándolo al suelo de
	 * forma segura.
	 */
	public void cancelarItemAgarrado(final Mundo mundo) {
		if (this.item == null) {
			return;
		}

		if ((this.slotOrigen != null) && !this.slotOrigen.contieneItem() && this.slotOrigen.puedeAceptar(this.item)) {
			this.slotOrigen.establecerObjeto(this.item);
			this.limpiar();
		} else if (mundo != null) {
			this.soltarItemEnMundo(mundo);
		}
	}

	/**
	 * Suelta el ítem sostenido en la posición del jugador.
	 */
	public void soltarItemEnMundo(final Mundo mundo) {
		if ((this.item == null) || (mundo == null)) {
			return;
		}
		mundo.agregarItemEnPosicionJugador(this.item, false);
		this.limpiar();
	}

	/**
	 * Dibuja el ítem centrado en el puntero del ratón (Capa 3 de render).
	 */
	public void pintar(final Graphics2D g, final Point posicionPuntero) {
		if ((this.item == null) || (posicionPuntero == null)) {
			return;
		}
		Render2D.dibujarImagen(g, this.item.getTextura(), posicionPuntero);
	}

	public boolean contieneItem() {
		return this.item != null;
	}

	public Item getItem() {
		return this.item;
	}

	public Slot getSlotOrigen() {
		return this.slotOrigen;
	}

	public void limpiar() {
		this.item = null;
		this.slotOrigen = null;
	}
}