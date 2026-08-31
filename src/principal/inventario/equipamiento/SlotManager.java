package principal.inventario.equipamiento;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.inventario.CajaInfo;
import principal.inventario.Inventario;
import principal.inventario.slot.Slot;
import principal.inventario.slot.SlotIGU;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Administrador central de casillas del inventario y del HUD inferior. Gestiona
 * el equipamiento de armas, apilado de consumibles y extracción de municiones.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class SlotManager {

	private static final int LADO_SLOTS = 18;
	private static final Font FUENTE_SLOTS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);

	private static final int CANTIDAD_SLOTS_FILA = 10;
	private static final int FILAS_ALMACEN = 3;

	private final Inventario INVENTARIO;
	private final ArrayList<Slot> LISTA_SLOTS;
	private final ArrayList<SlotIGU> LISTA_SLOTS_IGU;
	private final ArrayList<Slot> LISTA_SLOTS_GENERAL;
	private final ArrayList<Slot> LISTA_SLOTS_ALMACEN;
	private final ArrayList<Slot> LISTA_SLOTS_PRINCIPALES;
	private final ArrayList<Slot> LISTA_SLOTS_EQUIPAMIENTO;

	private final Rectangle ZONA_SLOTS_ALMACEN;
	private final Rectangle ZONA_SLOTS_PRINCIPALES;
	private final Rectangle ZONA_SLOTS_EQUIPAMIENTOS;
	private final int MARGEN_GENERAL;

	private SlotArma slotArma;
	private final CajaInfo infoArma;
	private Slot slotApuntado;
	private SlotIGU slotIguApuntado;

	public SlotManager(final Inventario inventario, final int margenGeneral, final Rectangle zonaSlotAlmacen,
			final Rectangle zonaSlotPrincipales, final Rectangle zonaSlotEquipamiento) {
		this.INVENTARIO = inventario;
		this.ZONA_SLOTS_EQUIPAMIENTOS = zonaSlotEquipamiento;
		this.ZONA_SLOTS_ALMACEN = zonaSlotAlmacen;
		this.ZONA_SLOTS_PRINCIPALES = zonaSlotPrincipales;
		this.MARGEN_GENERAL = margenGeneral;

		this.LISTA_SLOTS = new ArrayList<Slot>();
		this.LISTA_SLOTS_IGU = new ArrayList<SlotIGU>();
		this.LISTA_SLOTS_GENERAL = new ArrayList<Slot>();
		this.LISTA_SLOTS_ALMACEN = new ArrayList<Slot>();
		this.LISTA_SLOTS_PRINCIPALES = new ArrayList<Slot>();
		this.LISTA_SLOTS_EQUIPAMIENTO = new ArrayList<Slot>();

		this.infoArma = new CajaInfo(new Rectangle(this.ZONA_SLOTS_EQUIPAMIENTOS.x + LADO_SLOTS + this.MARGEN_GENERAL,
				this.ZONA_SLOTS_EQUIPAMIENTOS.y, LADO_SLOTS, LADO_SLOTS));

		this.llenarSlotsPrincipales();
		this.llenarSlotsEquipamientos();
		this.llenarSlotsAlmacenamiento();
		this.llenarSlotsIGU();
	}

	public void actualizar(final Raton raton, final GestorTiempo gtRatonPresiono, final int tiempoMsRatonPresiono,
			final ItemPuntero itemPuntero, final Mundo mundo) {
		this.actualizarSlots(raton);
		this.actualizarClickIzquierdo(raton, gtRatonPresiono, tiempoMsRatonPresiono, itemPuntero);
		this.actualizarActivarItem(raton);
	}

	public void actualizarIGU(final Raton raton) {
		this.actualizarSlotsIGU(raton);
		this.actualizarActivarItemIGU(raton);
	}

	private void actualizarClickIzquierdo(final Raton raton, final GestorTiempo gtRaton, final int tiempoMs,
			final ItemPuntero itemPuntero) {
		if (raton.presionadoClickIzq() && gtRaton.transcurrioMiliSegundos(tiempoMs)) {
			final Slot slot = this.getSlot(raton.getPuntoPosicionEscalado());
			if (slot == null) {
				return;
			}

			gtRaton.establecerReferenciaTiempoActual();

			if (!itemPuntero.contieneItem()) {
				itemPuntero.agarrarItem(slot);
			} else {
				itemPuntero.interactuarConSlot(slot);
			}
		}
	}

	private void actualizarActivarItem(final Raton raton) {
		if (raton.presionadoClickDerUnicaAct() && this.INVENTARIO.getActivarItemDisponible()) {
			for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
				if (slot.ratonIntersecta(raton)) {
					if (slot.contieneItem()) {
						final Item i = slot.getItem();

						if (i instanceof Arrojadizo) {
							this.INVENTARIO.getSlotArrojadizo().establecerObjeto(i);
							Globales.GESTOR_INVENTARIO.getInventarioJugador().invertirVisibilidad();
							return;
						}

						if (i instanceof Arma) {
							if (slot == this.slotArma) {
								this.desequiparArma();
							} else {
								final Item itemAux = this.slotArma.getItem();
								this.slotArma.establecerObjeto(i);
								slot.establecerObjeto(itemAux);
							}
							break;
						}

						if (i instanceof Consumible) {
							final Consumible c = (Consumible) i;
							c.consumir(Globales.JUGADOR);
							this.INVENTARIO.setActivarItemDisponible(false);
							break;
						}
					}
					break;
				}
			}
		} else if (!this.INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDer()) {
			this.INVENTARIO.setActivarItemDisponible(true);
		}
	}

	private void actualizarActivarItemIGU(final Raton raton) {
		if (raton.presionadoClickDerUnicaAct() && this.INVENTARIO.getActivarItemDisponible()) {
			for (final SlotIGU slotIGU : this.LISTA_SLOTS_IGU) {
				if (slotIGU.apuntado() && slotIGU.contieneItem()) {
					final Item i = slotIGU.getItem();
					this.INVENTARIO.setActivarItemDisponible(false);

					if (i instanceof Arrojadizo) {
						this.INVENTARIO.getSlotArrojadizo().establecerObjeto(i);
						return;
					}
					if (i instanceof Arma) {
						if (slotIGU.getSlot() == this.slotArma) {
							this.desequiparArma();
						} else {
							final Item itemAux = this.slotArma.getItem();
							this.slotArma.establecerObjeto(i);
							slotIGU.establecerObjeto(itemAux);
						}
						break;
					}
					if (i instanceof Consumible) {
						final Consumible c = (Consumible) i;
						c.consumir(Globales.JUGADOR);
						break;
					}
				}
			}
		} else if (!this.INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDerUnicaAct()) {
			this.INVENTARIO.setActivarItemDisponible(true);
		}
	}

	// =========================================================================
	// === MÉTODOS DE EXTRACCIÓN Y CONTEO DE MUNICIÓN (ZERO-GC)
	// =========================================================================

	/**
	 * Cuenta la cantidad total de balas de reserva de un tipo específico en todo el
	 * inventario.
	 *
	 * @param codModeloMunicion Código del modelo de la caja de munición.
	 * @return Cantidad acumulada de proyectiles disponibles.
	 */
	public int contarMunicionTotal(final String codModeloMunicion) {
		if (codModeloMunicion == null) {
			return 0;
		}

		int total = 0;
		final int cantSlots = this.LISTA_SLOTS.size();

		for (int i = 0; i < cantSlots; i++) {
			final Slot slot = this.LISTA_SLOTS.get(i);
			if (slot.contieneItem() && (slot.getItem().getTipoItem() == Item.COD_ITEM_CONSUMIBLE)) {
				final Consumible cons = (Consumible) slot.getItem();
				if (codModeloMunicion.equals(cons.getCodigoModelo())) {
					total += cons.getCantidad();
				}
			}
		}

		return total;
	}

	/**
	 * Extrae y descuenta del inventario la cantidad de munición solicitada para
	 * recargar el arma. Si un stack se agota a 0, la casilla se vacía de forma
	 * segura.
	 *
	 * @param codModeloMunicion Código de la caja de munición requerida.
	 * @param cantidadRequerida Balas que faltan para llenar el cargador.
	 * @return Cantidad real de balas extraídas.
	 */
	public int extraerMunicion(final String codModeloMunicion, final int cantidadRequerida) {
		if ((codModeloMunicion == null) || (cantidadRequerida <= 0)) {
			return 0;
		}

		int faltan = cantidadRequerida;
		final int cantSlots = this.LISTA_SLOTS.size();

		for (int i = 0; i < cantSlots; i++) {
			final Slot slot = this.LISTA_SLOTS.get(i);
			if (slot.contieneItem() && (slot.getItem().getTipoItem() == Item.COD_ITEM_CONSUMIBLE)) {
				final Consumible cons = (Consumible) slot.getItem();
				if (codModeloMunicion.equals(cons.getCodigoModelo())) {
					final int disponible = cons.getCantidad();

					if (disponible > faltan) {
						cons.establecerCantidad(disponible - faltan);
						faltan = 0;
						break;
					}
					faltan -= disponible;
					cons.establecerCantidad(0);
					slot.eliminarObjeto();
				}
			}
		}

		return cantidadRequerida - faltan;
	}

	// =========================================================================
	// === GESTIÓN DE EQUIPAMIENTO
	// =========================================================================

	private void desequiparArma() {
		if ((this.slotArma == null) || !this.slotArma.contieneItem()) {
			return;
		}

		for (final Slot slot : this.LISTA_SLOTS_PRINCIPALES) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto(this.slotArma.getItem());
				this.slotArma.eliminarObjeto();
				return;
			}
		}

		for (final Slot slot : this.LISTA_SLOTS_ALMACEN) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto(this.slotArma.getItem());
				this.slotArma.eliminarObjeto();
				return;
			}
		}
	}

	public Item getArmaEquipada() {
		if ((this.slotArma != null) && (this.slotArma.getItem() != null)) {
			return this.slotArma.getItem();
		}
		return new Desarmado();
	}

	public Arma equiparArma(final Arma arma) {
		final Arma aux = ((this.slotArma != null) && (this.slotArma.getItem() != null)) ? (Arma) this.slotArma.getItem()
				: new Desarmado();

		if (this.slotArma != null) {
			this.slotArma.establecerObjeto(arma);
		}
		return aux;
	}

	public boolean agregarPortable(final Portable item) {
		for (final Slot slot : this.LISTA_SLOTS) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto((Portable) item.copiar());
				return true;
			}
		}
		return false;
	}

	public boolean agregarConsumible(final Consumible item) {
		Slot slotVacio = null;
		Consumible cons = null;

		for (final Slot slot : this.LISTA_SLOTS) {
			if (slot.contieneItem()) {
				if (slot.getItem().getTipoItem() == Item.COD_ITEM_CONSUMIBLE) {
					cons = (Consumible) slot.getItem();
					if (cons.getCodigoModelo() == item.getCodigoModelo()) {
						item.establecerCantidad(cons.agregarCantidad(item.getCantidad()));
						if (item.getCantidad() <= 0) {
							return true;
						}
					}
				}
			} else if (slotVacio == null) {
				slotVacio = slot;
			}
		}

		if (slotVacio != null) {
			slotVacio.establecerObjeto((Consumible) item.copiar());
			item.establecerCantidad(0);
			return true;
		}

		return false;
	}

	public void vaciar() {
		for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
			slot.establecerObjeto(null);
		}
	}

	public void pintar(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		g.setFont(FUENTE_SLOTS);

		this.slotApuntado = null;
		for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
			slot.pintar(g);
			if (slot.estaApuntado() && (this.slotApuntado == null)) {
				this.slotApuntado = slot;
			}
		}

		g.setFont(fuenteOriginal);
	}

	public void pintarTooltip(final Graphics2D g) {
		if ((this.slotApuntado != null) && this.slotApuntado.contieneItem()) {
			this.slotApuntado.pintarTooltip(g);
		}
	}

	public void pintarSlotsIGU(final Graphics2D g) {
		this.slotIguApuntado = null;
		for (final SlotIGU slotIGU : this.LISTA_SLOTS_IGU) {
			slotIGU.pintar(g);
			if (slotIGU.apuntado() && slotIGU.contieneItem()) {
				this.slotIguApuntado = slotIGU;
			}
		}
	}

	public void pintarTooltipIGU(final Graphics2D g) {
		if ((this.slotIguApuntado != null) && this.slotIguApuntado.contieneItem()) {
			this.slotIguApuntado.pintarTooltip(g);
		}
	}

	private void actualizarSlots(final Raton raton) {
		for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
			slot.actualizar(raton);
		}
	}

	private void actualizarSlotsIGU(final Raton raton) {
		for (final SlotIGU slotIGU : this.LISTA_SLOTS_IGU) {
			slotIGU.actualizar(raton);
		}
	}

	public Slot getSlot(final Point posicion) {
		if (posicion == null) {
			return null;
		}
		for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
			if (slot.intersecta(posicion)) {
				return slot;
			}
		}
		return null;
	}

	private void llenarSlotsAlmacenamiento() {
		int y = this.ZONA_SLOTS_ALMACEN.y + this.MARGEN_GENERAL;

		for (int f = 0; f < FILAS_ALMACEN; f++) {
			int x = this.INVENTARIO.getX();
			for (int i = 0; i < CANTIDAD_SLOTS_FILA; i++) {
				x += this.MARGEN_GENERAL;
				final Slot slot = new Slot(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS));
				this.LISTA_SLOTS.add(slot);
				this.LISTA_SLOTS_ALMACEN.add(slot);
				this.LISTA_SLOTS_GENERAL.add(slot);
				x += LADO_SLOTS;
			}
			y += LADO_SLOTS + this.MARGEN_GENERAL;
		}
	}

	private void llenarSlotsPrincipales() {
		int x = this.INVENTARIO.getX();
		final int y = this.ZONA_SLOTS_PRINCIPALES.y + this.MARGEN_GENERAL;

		for (int i = 0; i < CANTIDAD_SLOTS_FILA; i++) {
			x += this.MARGEN_GENERAL;
			final Slot slot = new Slot(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS));
			this.LISTA_SLOTS.add(slot);
			this.LISTA_SLOTS_PRINCIPALES.add(slot);
			this.LISTA_SLOTS_GENERAL.add(slot);
			x += LADO_SLOTS;
		}
	}

	private void llenarSlotsEquipamientos() {
		final int x = this.ZONA_SLOTS_EQUIPAMIENTOS.x;
		final int y = this.ZONA_SLOTS_EQUIPAMIENTOS.y;

		final Rectangle rectArma = new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS);
		this.slotArma = new SlotArma(rectArma, this.infoArma);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotArma);
		this.LISTA_SLOTS_GENERAL.add(this.slotArma);
	}

	private void llenarSlotsIGU() {
		final int posIguY = Constantes.ALTO_JUEGO - LADO_SLOTS - this.MARGEN_GENERAL;

		for (final Slot slot : this.LISTA_SLOTS_PRINCIPALES) {
			this.LISTA_SLOTS_IGU.add(new SlotIGU(slot, slot.getX(), posIguY));
		}

		if (this.slotArma != null) {
			this.LISTA_SLOTS_IGU.add(new SlotIGU(this.slotArma,
					this.ZONA_SLOTS_PRINCIPALES.x - this.slotArma.getAncho() - (2 * this.MARGEN_GENERAL),
					posIguY - this.MARGEN_GENERAL));
		}
	}

	public static int getLadoSlots() {
		return LADO_SLOTS;
	}
}