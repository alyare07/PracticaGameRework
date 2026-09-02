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
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.inventario.CajaInfo;
import principal.inventario.Inventario;
import principal.inventario.slot.Slot;
import principal.inventario.slot.SlotIGU;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.inventario.ItemPuntero;

public class SlotManager {

	private static final int LADO_SLOTS = 18;
	private static final int CANTIDAD_SLOTS_FILA = 10;
	private static final int FILAS_ALMACEN = 3;

	private final Inventario INVENTARIO;
	private final ArrayList<Slot> LISTA_SLOTS = new ArrayList<Slot>();
	private final ArrayList<SlotIGU> LISTA_SLOTS_IGU = new ArrayList<SlotIGU>();
	private final ArrayList<Slot> LISTA_SLOTS_GENERAL = new ArrayList<Slot>();
	private final ArrayList<Slot> LISTA_SLOTS_ALMACEN = new ArrayList<Slot>();
	private final ArrayList<Slot> LISTA_SLOTS_PRINCIPALES = new ArrayList<Slot>();
	private final ArrayList<SlotEquipamiento> LISTA_SLOTS_EQUIPAMIENTO = new ArrayList<SlotEquipamiento>();

	private final Rectangle ZONA_SLOTS_ALMACEN;
	private final Rectangle ZONA_SLOTS_PRINCIPALES;
	private final Rectangle ZONA_SLOTS_EQUIPAMIENTOS;
	private final int MARGEN_GENERAL;

	private SlotArma slotArma;
	private SlotPiezaEquipo slotCasco;
	private SlotPiezaEquipo slotTorso;
	private SlotPiezaEquipo slotBotas;
	private SlotPiezaEquipo slotAnillo1;
	private SlotPiezaEquipo slotAnillo2;
	private SlotPiezaEquipo slotAnillo3;

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

		this.infoArma = new CajaInfo(new Rectangle(this.ZONA_SLOTS_EQUIPAMIENTOS.x + (LADO_SLOTS * 7) + 14,
				this.ZONA_SLOTS_EQUIPAMIENTOS.y, 40, LADO_SLOTS));

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

						if (i instanceof PiezaEquipo) {
							this.equiparPiezaRapida(slot, (PiezaEquipo) i);
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

	private void equiparPiezaRapida(final Slot slotOrigen, final PiezaEquipo pieza) {
		SlotPiezaEquipo slotDestino = null;

		switch (pieza.getTipoEquipo()) {
		case CASCO:
			slotDestino = this.slotCasco;
			break;
		case TORSO:
			slotDestino = this.slotTorso;
			break;
		case BOTAS:
			slotDestino = this.slotBotas;
			break;
		case ANILLO:
			if (!this.slotAnillo1.contieneItem()) {
				slotDestino = this.slotAnillo1;
			} else if (!this.slotAnillo2.contieneItem()) {
				slotDestino = this.slotAnillo2;
			} else if (!this.slotAnillo3.contieneItem()) {
				slotDestino = this.slotAnillo3;
			} else {
				slotDestino = this.slotAnillo1; // Swap con el primero si están llenos
			}
			break;
		default:
			break;
		}

		if (slotDestino != null) {
			if (slotOrigen == slotDestino) {
				this.desequiparAAlmacen(slotDestino);
			} else {
				final Item aux = slotDestino.getItem();
				slotDestino.establecerObjeto(pieza);
				slotOrigen.establecerObjeto(aux);
			}
		}
	}

	private void desequiparAAlmacen(final Slot slotEquipo) {
		if ((slotEquipo == null) || !slotEquipo.contieneItem()) {
			return;
		}

		for (final Slot s : this.LISTA_SLOTS_PRINCIPALES) {
			if (!s.contieneItem()) {
				s.establecerObjeto(slotEquipo.getItem());
				slotEquipo.eliminarObjeto();
				return;
			}
		}

		for (final Slot s : this.LISTA_SLOTS_ALMACEN) {
			if (!s.contieneItem()) {
				s.establecerObjeto(slotEquipo.getItem());
				slotEquipo.eliminarObjeto();
				return;
			}
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

	private void desequiparArma() {
		this.desequiparAAlmacen(this.slotArma);
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
		if (item == null) {
			return false;
		}

		final int cantidadInicial = item.getCantidad();
		Slot slotVacio = null;

		for (final Slot slot : this.LISTA_SLOTS) {
			if (slot.contieneItem()) {
				if (slot.getItem().getTipoItem() == Item.COD_ITEM_CONSUMIBLE) {
					final Consumible cons = (Consumible) slot.getItem();
					if (cons.getCodigoModelo().equals(item.getCodigoModelo())) {
						final int sobrante = cons.agregarCantidad(item.getCantidad());
						item.establecerCantidad(sobrante);

						if (item.getCantidad() <= 0) {
							return true;
						}
					}
				}
			} else if (slotVacio == null) {
				slotVacio = slot;
			}
		}

		if ((item.getCantidad() > 0) && (slotVacio != null)) {
			slotVacio.establecerObjeto((Consumible) item.copiar());
			item.establecerCantidad(0);
			return true;
		}

		return item.getCantidad() < cantidadInicial;
	}

	public void vaciar() {
		for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
			slot.establecerObjeto(null);
		}
	}

	public void pintar(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(6f));

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
		int x = this.ZONA_SLOTS_EQUIPAMIENTOS.x;
		final int y = this.ZONA_SLOTS_EQUIPAMIENTOS.y;

		// 1. Slot Arma
		this.slotArma = new SlotArma(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), this.infoArma);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotArma);
		this.LISTA_SLOTS_GENERAL.add(this.slotArma);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 2. Slot Casco
		this.slotCasco = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null, TipoEquipo.CASCO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotCasco);
		this.LISTA_SLOTS_GENERAL.add(this.slotCasco);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 3. Slot Torso / Indumentaria
		this.slotTorso = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null, TipoEquipo.TORSO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotTorso);
		this.LISTA_SLOTS_GENERAL.add(this.slotTorso);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 4. Slot Botas
		this.slotBotas = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null, TipoEquipo.BOTAS);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotBotas);
		this.LISTA_SLOTS_GENERAL.add(this.slotBotas);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 5. Slot Anillo 1
		this.slotAnillo1 = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null, TipoEquipo.ANILLO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotAnillo1);
		this.LISTA_SLOTS_GENERAL.add(this.slotAnillo1);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 6. Slot Anillo 2
		this.slotAnillo2 = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null, TipoEquipo.ANILLO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotAnillo2);
		this.LISTA_SLOTS_GENERAL.add(this.slotAnillo2);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 7. Slot Anillo 3
		this.slotAnillo3 = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null, TipoEquipo.ANILLO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotAnillo3);
		this.LISTA_SLOTS_GENERAL.add(this.slotAnillo3);
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

	public ArrayList<SlotEquipamiento> getSlotsEquipamiento() {
		return this.LISTA_SLOTS_EQUIPAMIENTO;
	}

	public static int getLadoSlots() {
		return LADO_SLOTS;
	}
}