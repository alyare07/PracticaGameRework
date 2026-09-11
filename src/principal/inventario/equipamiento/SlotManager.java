package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.inventario.Inventario;
import principal.inventario.slot.Slot;
import principal.inventario.slot.SlotIGU;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Gestor maestro de casillas de inventario, equipamiento rápido, Hotbar
 * numérica 1-0 y proyección HUD de [ARM] y [SEC] (Zero-GC / O(1)).
 * 
 * @version 2.7 (Vanilla Java 8 - Offhand HUD Projection & Fast Unequip)
 */
public class SlotManager {

	private static final int LADO_SLOTS = 18;
	private static final int CANTIDAD_SLOTS_FILA = 10;
	private static final int FILAS_ALMACEN = 3;

	private static final int[] TECLAS_HOTBAR = { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
			KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0 };

	private final Inventario INVENTARIO;
	private final ArrayList<Slot> LISTA_SLOTS = new ArrayList<Slot>(40);
	private final ArrayList<SlotIGU> LISTA_SLOTS_IGU = new ArrayList<SlotIGU>(12);
	private final ArrayList<Slot> LISTA_SLOTS_GENERAL = new ArrayList<Slot>(48);
	private final ArrayList<Slot> LISTA_SLOTS_ALMACEN = new ArrayList<Slot>(30);
	private final ArrayList<Slot> LISTA_SLOTS_PRINCIPALES = new ArrayList<Slot>(10);
	private final ArrayList<SlotEquipamiento> LISTA_SLOTS_EQUIPAMIENTO = new ArrayList<SlotEquipamiento>(8);

	private final Rectangle ZONA_SLOTS_ALMACEN;
	private final Rectangle ZONA_SLOTS_PRINCIPALES;
	private final Rectangle ZONA_SLOTS_EQUIPAMIENTOS;
	private final int MARGEN_GENERAL;

	private SlotArma slotArma;
	private SlotManoSecundaria slotManoSecundaria;
	private SlotPiezaEquipo slotCasco;
	private SlotPiezaEquipo slotTorso;
	private SlotPiezaEquipo slotBotas;
	private SlotPiezaEquipo slotAnillo1;
	private SlotPiezaEquipo slotAnillo2;
	private SlotPiezaEquipo slotAnillo3;

	private Slot slotApuntado;
	private SlotIGU slotIguApuntado;

	public SlotManager(final Inventario inventario, final int margenGeneral, final Rectangle zonaSlotAlmacen,
			final Rectangle zonaSlotPrincipales, final Rectangle zonaSlotEquipamiento) {
		this.INVENTARIO = inventario;
		this.ZONA_SLOTS_EQUIPAMIENTOS = zonaSlotEquipamiento;
		this.ZONA_SLOTS_ALMACEN = zonaSlotAlmacen;
		this.ZONA_SLOTS_PRINCIPALES = zonaSlotPrincipales;
		this.MARGEN_GENERAL = margenGeneral;

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
		this.actualizarHotbarTeclado();
	}

	public void actualizarIGU(final Raton raton) {
		this.actualizarSlotsIGU(raton);
		this.actualizarActivarItemIGU(raton);
		this.actualizarHotbarTeclado();
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
				slotDestino = this.slotAnillo1;
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

		for (int i = 0; i < this.LISTA_SLOTS_PRINCIPALES.size(); i++) {
			final Slot s = this.LISTA_SLOTS_PRINCIPALES.get(i);
			if (!s.contieneItem()) {
				s.establecerObjeto(slotEquipo.getItem());
				slotEquipo.eliminarObjeto();
				return;
			}
		}

		for (int i = 0; i < this.LISTA_SLOTS_ALMACEN.size(); i++) {
			final Slot s = this.LISTA_SLOTS_ALMACEN.get(i);
			if (!s.contieneItem()) {
				s.establecerObjeto(slotEquipo.getItem());
				slotEquipo.eliminarObjeto();
				return;
			}
		}
	}

	/**
	 * Despachador universal de activación, consumo, equipamiento y venta rápida de
	 * ítems. Unifica el comportamiento de teclas 1-0, HUD y ventana de inventario
	 * (Zero-GC).
	 */
	public void activarItemDeSlot(final Slot slot) {
		if ((slot == null) || !slot.contieneItem()) {
			return;
		}

		final Item i = slot.getItem();

		// 1. Gancho de Tienda: Si hay tienda abierta, clic derecho vende el ítem
		if (Globales.GESTOR_INVENTARIO.hayInventarioTerceroAbierto() && (Globales.GESTOR_INVENTARIO
				.getInventarioTercero() instanceof principal.inventario.tienda.InventarioTienda)) {

			final principal.inventario.tienda.InventarioTienda tienda = (principal.inventario.tienda.InventarioTienda) Globales.GESTOR_INVENTARIO
					.getInventarioTercero();

			// Protección: No permite vender equipo que se encuentre puesto
			if (slot instanceof SlotEquipamiento) {
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				tienda.mostrarNotificacion("¡Desequipa el objeto para venderlo!", new Color(255, 100, 100));
				return;
			}

			final boolean venderTodo = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
			tienda.venderItemJugador(slot, venderTodo);
			return;
		}

		// 2. Arrojadizos (Granadas / Cuchillos)
		if (i instanceof Arrojadizo) {
			this.INVENTARIO.getSlotArrojadizo().establecerObjeto(i);
			if (this.INVENTARIO.esVisible()) {
				this.INVENTARIO.ocultar();
			}
			GestorSonido.reproducir(IDSonido.RECOGER);
			return;
		}

		// 3. Armas Principales (Equipar / Intercambiar con SlotArma)
		if (i instanceof Arma) {
			if (slot == this.slotArma) {
				this.desequiparArma();
			} else {
				final Item armaPrevia = this.slotArma.getItem();
				this.slotArma.establecerObjeto(i);
				slot.establecerObjeto(armaPrevia);
			}
			GestorSonido.reproducir(IDSonido.RECOGER);
			return;
		}

		// 4. Mano Secundaria (Desequipar)
		if (slot == this.slotManoSecundaria) {
			this.desequiparAAlmacen(this.slotManoSecundaria);
			GestorSonido.reproducir(IDSonido.RECOGER);
			return;
		}

		// 5. Piezas de Equipo (Cascos, Armaduras, Botas, Anillos)
		if (i instanceof PiezaEquipo) {
			this.equiparPiezaRapida(slot, (PiezaEquipo) i);
			GestorSonido.reproducir(IDSonido.RECOGER);
			return;
		}

		// 6. Consumibles y Desplegables (Pociones, Kit de Fogatas, etc.)
		if (i instanceof Consumible) {
			final Consumible c = (Consumible) i;
			c.consumir(Globales.JUGADOR);
			GestorSonido.reproducir(IDSonido.RECOGER);
			return;
		}
	}

	private void actualizarHotbarTeclado() {
		if (Globales.GESTOR_DIALOGOS.isActivo() || Globales.GESTOR_EVENTOS.haySecuenciaEnCurso()) {
			return;
		}

		for (int i = 0; i < TECLAS_HOTBAR.length; i++) {
			if (Globales.TECLADO.isTeclaPresionadaUnaVez(TECLAS_HOTBAR[i])) {
				if (i < this.LISTA_SLOTS_PRINCIPALES.size()) {
					final Slot slotHotbar = this.LISTA_SLOTS_PRINCIPALES.get(i);
					this.activarItemDeSlot(slotHotbar);
				}
				break;
			}
		}
	}

	private void actualizarActivarItem(final Raton raton) {
		if (raton.presionadoClickDerUnicaAct() && this.INVENTARIO.getActivarItemDisponible()) {
			final int totalSlots = this.LISTA_SLOTS_GENERAL.size();
			for (int s = 0; s < totalSlots; s++) {
				final Slot slot = this.LISTA_SLOTS_GENERAL.get(s);
				if (slot.ratonIntersecta(raton) && slot.contieneItem()) {
					this.activarItemDeSlot(slot);
					this.INVENTARIO.setActivarItemDisponible(false);
					break;
				}
			}
		} else if (!this.INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDer()) {
			this.INVENTARIO.setActivarItemDisponible(true);
		}
	}

	private void actualizarActivarItemIGU(final Raton raton) {
		if (raton.presionadoClickDerUnicaAct() && this.INVENTARIO.getActivarItemDisponible()) {
			for (int idx = 0; idx < this.LISTA_SLOTS_IGU.size(); idx++) {
				final SlotIGU slotIGU = this.LISTA_SLOTS_IGU.get(idx);
				if (slotIGU.apuntado() && slotIGU.contieneItem()) {
					this.activarItemDeSlot(slotIGU.getSlot());
					this.INVENTARIO.setActivarItemDisponible(false);
					break;
				}
			}
		} else if (!this.INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDerUnicaAct()) {
			this.INVENTARIO.setActivarItemDisponible(true);
		}
	}

	public int contarMunicionTotal(final String codModeloMunicion) {
		return this.contarItemGenericoTotal(codModeloMunicion);
	}

	public int extraerMunicion(final String codModeloMunicion, final int cantidadRequerida) {
		if ((codModeloMunicion == null) || (cantidadRequerida <= 0)) {
			return 0;
		}

		int faltan = cantidadRequerida;
		final int cantSlots = this.LISTA_SLOTS.size();

		for (int i = 0; i < cantSlots; i++) {
			final Slot slot = this.LISTA_SLOTS.get(i);
			if (slot.contieneItem()) {
				final Item item = slot.getItem();

				if (item instanceof Consumible) {
					final Consumible cons = (Consumible) item;
					if (codModeloMunicion.equalsIgnoreCase(cons.getCodigoModelo())
							|| codModeloMunicion.equalsIgnoreCase(cons.getNombre())) {
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
				} else if (codModeloMunicion.equalsIgnoreCase(item.getNombre())) {
					slot.eliminarObjeto();
					faltan--;
					if (faltan == 0) {
						break;
					}
				}
			}
		}

		return cantidadRequerida - faltan;
	}

	public int contarItemGenericoTotal(final String codigoONombre) {
		if (codigoONombre == null) {
			return 0;
		}

		int total = 0;
		final int cantSlots = this.LISTA_SLOTS.size();

		for (int i = 0; i < cantSlots; i++) {
			final Slot slot = this.LISTA_SLOTS.get(i);
			if (slot.contieneItem()) {
				final Item item = slot.getItem();
				if (item instanceof Consumible) {
					final Consumible cons = (Consumible) item;
					if (codigoONombre.equalsIgnoreCase(cons.getCodigoModelo())
							|| codigoONombre.equalsIgnoreCase(cons.getNombre())) {
						total += cons.getCantidad();
					}
				} else if (codigoONombre.equalsIgnoreCase(item.getNombre())) {
					total++;
				}
			}
		}

		return total;
	}

	public boolean extraerItemGenerico(final String codigoONombre, final int cantidadRequerida) {
		if ((codigoONombre == null) || (cantidadRequerida <= 0)) {
			return false;
		}
		if (this.contarItemGenericoTotal(codigoONombre) < cantidadRequerida) {
			return false;
		}
		return this.extraerMunicion(codigoONombre, cantidadRequerida) == cantidadRequerida;
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
		for (int i = 0; i < this.LISTA_SLOTS.size(); i++) {
			final Slot slot = this.LISTA_SLOTS.get(i);
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

		for (int i = 0; i < this.LISTA_SLOTS.size(); i++) {
			final Slot slot = this.LISTA_SLOTS.get(i);
			if (slot.contieneItem()) {
				if (slot.getItem().getTipoItem() == Item.COD_ITEM_CONSUMIBLE) {
					final Consumible cons = (Consumible) slot.getItem();
					if (cons.getCodigoModelo().equalsIgnoreCase(item.getCodigoModelo())) {
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
		for (int i = 0; i < this.LISTA_SLOTS_GENERAL.size(); i++) {
			this.LISTA_SLOTS_GENERAL.get(i).establecerObjeto(null);
		}
	}

	public void pintar(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(6f));

		this.slotApuntado = null;
		for (int i = 0; i < this.LISTA_SLOTS_GENERAL.size(); i++) {
			final Slot slot = this.LISTA_SLOTS_GENERAL.get(i);
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
		for (int i = 0; i < this.LISTA_SLOTS_IGU.size(); i++) {
			final SlotIGU slotIGU = this.LISTA_SLOTS_IGU.get(i);
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
		for (int i = 0; i < this.LISTA_SLOTS_GENERAL.size(); i++) {
			this.LISTA_SLOTS_GENERAL.get(i).actualizar(raton);
		}
	}

	private void actualizarSlotsIGU(final Raton raton) {
		for (int i = 0; i < this.LISTA_SLOTS_IGU.size(); i++) {
			this.LISTA_SLOTS_IGU.get(i).actualizar(raton);
		}
	}

	public Slot getSlot(final Point posicion) {
		if (posicion == null) {
			return null;
		}
		for (int i = 0; i < this.LISTA_SLOTS_GENERAL.size(); i++) {
			final Slot slot = this.LISTA_SLOTS_GENERAL.get(i);
			if (slot.intersecta(posicion)) {
				return slot;
			}
		}
		return null;
	}

	private void llenarSlotsAlmacenamiento() {
		final int anchoSlotsTotal = (CANTIDAD_SLOTS_FILA * LADO_SLOTS)
				+ ((CANTIDAD_SLOTS_FILA + 1) * this.MARGEN_GENERAL);
		final int xOffsetCentrado = (this.INVENTARIO.getAncho() - anchoSlotsTotal) / 2;
		int y = this.ZONA_SLOTS_ALMACEN.y + this.MARGEN_GENERAL;

		for (int f = 0; f < FILAS_ALMACEN; f++) {
			int x = this.INVENTARIO.getX() + xOffsetCentrado;
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
		final int anchoSlotsTotal = (CANTIDAD_SLOTS_FILA * LADO_SLOTS)
				+ ((CANTIDAD_SLOTS_FILA + 1) * this.MARGEN_GENERAL);
		final int xOffsetCentrado = (this.INVENTARIO.getAncho() - anchoSlotsTotal) / 2;
		int x = this.INVENTARIO.getX() + xOffsetCentrado;
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

		// 1. Arma Principal
		this.slotArma = new SlotArma(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS));
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotArma);
		this.LISTA_SLOTS_GENERAL.add(this.slotArma);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 2. Mano Secundaria (Offhand / Antorcha / Escudo)
		this.slotManoSecundaria = new SlotManoSecundaria(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotManoSecundaria);
		this.LISTA_SLOTS_GENERAL.add(this.slotManoSecundaria);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 3. Casco
		this.slotCasco = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null,
				principal.entes.objetos.items.equipamiento.TipoEquipo.CASCO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotCasco);
		this.LISTA_SLOTS_GENERAL.add(this.slotCasco);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 4. Torso
		this.slotTorso = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null,
				principal.entes.objetos.items.equipamiento.TipoEquipo.TORSO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotTorso);
		this.LISTA_SLOTS_GENERAL.add(this.slotTorso);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 5. Botas
		this.slotBotas = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null,
				principal.entes.objetos.items.equipamiento.TipoEquipo.BOTAS);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotBotas);
		this.LISTA_SLOTS_GENERAL.add(this.slotBotas);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 6. Anillo 1
		this.slotAnillo1 = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null,
				principal.entes.objetos.items.equipamiento.TipoEquipo.ANILLO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotAnillo1);
		this.LISTA_SLOTS_GENERAL.add(this.slotAnillo1);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 7. Anillo 2
		this.slotAnillo2 = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null,
				principal.entes.objetos.items.equipamiento.TipoEquipo.ANILLO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotAnillo2);
		this.LISTA_SLOTS_GENERAL.add(this.slotAnillo2);
		x += LADO_SLOTS + this.MARGEN_GENERAL;

		// 8. Anillo 3
		this.slotAnillo3 = new SlotPiezaEquipo(new Rectangle(x, y, LADO_SLOTS, LADO_SLOTS), null,
				principal.entes.objetos.items.equipamiento.TipoEquipo.ANILLO);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(this.slotAnillo3);
		this.LISTA_SLOTS_GENERAL.add(this.slotAnillo3);
	}

	private void llenarSlotsIGU() {
		final int posIguY = Constantes.ALTO_JUEGO - LADO_SLOTS - this.MARGEN_GENERAL;

		// 1. Hotbar principal (10 casillas de acceso rápido)
		for (int i = 0; i < this.LISTA_SLOTS_PRINCIPALES.size(); i++) {
			final Slot slot = this.LISTA_SLOTS_PRINCIPALES.get(i);
			this.LISTA_SLOTS_IGU.add(new SlotIGU(slot, slot.getX(), posIguY));
		}

		// 2. Ranuras activas de equipo en el HUD (Mano Secundaria y Arma Principal)
		if (!this.LISTA_SLOTS_PRINCIPALES.isEmpty()) {
			final Slot primerSlotHotbar = this.LISTA_SLOTS_PRINCIPALES.get(0);
			final int xBase = primerSlotHotbar.getX();

			// Mano Secundaria [SEC] inmediatamente a la izquierda de la Hotbar
			if (this.slotManoSecundaria != null) {
				final int xSlotSecIGU = xBase - this.slotManoSecundaria.getAncho() - (2 * this.MARGEN_GENERAL);
				this.LISTA_SLOTS_IGU
						.add(new SlotIGU(this.slotManoSecundaria, xSlotSecIGU, posIguY - this.MARGEN_GENERAL));
			}

			// Arma Principal [ARM] a la izquierda de la Mano Secundaria
			if (this.slotArma != null) {
				final int xSlotArmaIGU = xBase - (2 * this.slotArma.getAncho()) - (3 * this.MARGEN_GENERAL);
				this.LISTA_SLOTS_IGU.add(new SlotIGU(this.slotArma, xSlotArmaIGU, posIguY - this.MARGEN_GENERAL));
			}
		}
	}

	public ArrayList<SlotEquipamiento> getSlotsEquipamiento() {
		return this.LISTA_SLOTS_EQUIPAMIENTO;
	}

	public SlotManoSecundaria getSlotManoSecundaria() {
		return this.slotManoSecundaria;
	}

	public static int getLadoSlots() {
		return LADO_SLOTS;
	}

	public SlotPiezaEquipo getSlotCasco() {
		return this.slotCasco;
	}

	public SlotPiezaEquipo getSlotTorso() {
		return this.slotTorso;
	}
}