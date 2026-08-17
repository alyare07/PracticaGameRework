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
import principal.inventario.slot.SlotArrojadizo;
import principal.inventario.slot.SlotIGU;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Administrador y orquestador central de la cuadrícula de casillas (slots) del
 * inventario del jugador y de su proyección en el HUD inferior (IGU).
 * 
 * <p>
 * <b>Topología de Listas en Memoria (Zero-Duplication):</b>
 * </p>
 * <p>
 * Maneja múltiples listas especializadas para categorizar las casillas según su
 * zona visual y funcional, pero <b>todas comparten las mismas referencias de
 * objetos {@link Slot}</b> creadas una única vez en el constructor:
 * </p>
 * <ul>
 * <li><b>{@link #LISTA_SLOTS_ALMACEN}:</b> 30 casillas de almacenamiento
 * general (3 filas x 10 columnas).</li>
 * <li><b>{@link #LISTA_SLOTS_PRINCIPALES}:</b> 10 casillas de la barra de
 * acceso rápido (Hotbar).</li>
 * <li><b>{@link #LISTA_SLOTS_EQUIPAMIENTO}:</b> Casillas dedicadas a equipo
 * activo (ej: {@link SlotArma}).</li>
 * <li><b>{@link #LISTA_SLOTS_GENERAL}:</b> Colección agregada para iteraciones
 * masivas de actualización y render.</li>
 * <li><b>{@link #LISTA_SLOTS_IGU}:</b> Vistas proyectadas (Proxies) para el HUD
 * inferior en pantalla.</li>
 * </ul>
 * 
 * <p>
 * <b>Flujo de Entrada y Acciones:</b>
 * </p>
 * <ul>
 * <li><b>Clic Izquierdo:</b> Delega atómicamente el agarre, depósito e
 * intercambio de ítems a {@link ItemPuntero}.</li>
 * <li><b>Clic Derecho (Acción Rápida):</b> Consume consumibles, ceba
 * arrojadizos en {@link principal.inventario.slot.SlotArrojadizo} o
 * equipa/desequipa armas directamente con un solo clic.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Slot
 * @see SlotIGU
 * @see SlotArma
 * @see ItemPuntero
 */
public class SlotManager {

	/***/
	/* ========================================================================= */
	/* 1. CONSTANTES DE DIMENSIÓN Y RECURSOS GRÁFICOS (GC FRIENDLY) */
	/* ========================================================================= */
	/***/
	private static final int LADO_SLOTS = 18;
	private static final Font FUENTE_SLOTS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);

	private static final int CANTIDAD_SLOTS_FILA = 10;
	private static final int FILAS_ALMACEN = 3;

	/***/
	/* ========================================================================= */
	/* 2. REFERENCIAS ESTRUCTURALES Y COLECCIONES DE SLOTS */
	/* ========================================================================= */
	/***/
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

	/**
	 * Construye el administrador de casillas, inicializa los límites espaciales y
	 * genera la cuadrícula completa del inventario y del HUD.
	 * 
	 * @param inventario           Instancia del inventario del jugador propietario.
	 * @param margenGeneral        Espaciado en píxeles entre bordes y casillas.
	 * @param zonaSlotAlmacen      Área destinada a los slots de almacenamiento.
	 * @param zonaSlotPrincipales  Área destinada a la barra de acceso rápido.
	 * @param zonaSlotEquipamiento Área destinada a las ranuras de equipo.
	 */
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

		// Inicialización de la caja de información del arma equipada
		this.infoArma = new CajaInfo(new Rectangle(this.ZONA_SLOTS_EQUIPAMIENTOS.x + LADO_SLOTS + this.MARGEN_GENERAL,
				this.ZONA_SLOTS_EQUIPAMIENTOS.y, LADO_SLOTS, LADO_SLOTS));

		// Generación de cuadrículas en memoria
		this.llenarSlotsPrincipales();
		this.llenarSlotsEquipamientos();
		this.llenarSlotsAlmacenamiento();
		this.llenarSlotsIGU();
	}

	/***/
	/* ========================================================================= */
	/* 3. ACTUALIZACIÓN LÓGICA (60 APS) */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado lógico de todos los slots cuando la ventana del
	 * inventario está abierta.
	 * 
	 * @param raton                 Instancia del controlador del ratón.
	 * @param gtRatonPresiono       Gestor de tiempo para control de debounce de
	 *                              clics.
	 * @param tiempoMsRatonPresiono Intervalo mínimo de milisegundos entre
	 *                              pulsaciones.
	 * @param itemPuntero           Controlador del ítem sostenido en el cursor.
	 * @param mundo                 Referencia al mundo activo.
	 */
	public void actualizar(final Raton raton, final GestorTiempo gtRatonPresiono, final int tiempoMsRatonPresiono,
			final ItemPuntero itemPuntero, final Mundo mundo) {
		this.actualizarSlots(raton);
		this.actualizarClickIzquierdo(raton, gtRatonPresiono, tiempoMsRatonPresiono, itemPuntero);
		this.actualizarActivarItem(raton);
	}

	/**
	 * Actualiza el estado lógico de las casillas del HUD inferior cuando el
	 * inventario está cerrado.
	 * 
	 * @param raton Instancia del controlador del ratón.
	 */
	public void actualizarIGU(final Raton raton) {
		this.actualizarSlotsIGU(raton);
		this.actualizarActivarItemIGU(raton);
	}

	/***/
	/* ========================================================================= */
	/* 4. GESTIÓN DE ACCIONES DE RATÓN (CLIC IZQUIERDO Y DERECHO) */
	/* ========================================================================= */
	/***/

	/**
	 * Procesa la transferencia de ítems mediante clic izquierdo delegando
	 * directamente en {@link ItemPuntero}.
	 */
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

	/**
	 * Procesa acciones rápidas con clic derecho dentro de la ventana del
	 * inventario:
	 * <ul>
	 * <li><b>Arrojadizos:</b> Pasan al {@link SlotArrojadizo} y cierran la ventana
	 * para apuntar.</li>
	 * <li><b>Armas:</b> Se equipan en {@link #slotArma} o se desequipan al
	 * inventario si ya estaban equipadas.</li>
	 * <li><b>Consumibles:</b> Se aplican inmediatamente sobre el jugador.</li>
	 * </ul>
	 */
	private void actualizarActivarItem(final Raton raton) {
		if (raton.presionadoClickDerUnicaAct() && this.INVENTARIO.getActivarItemDisponible()) {
			for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
				if (slot.ratonIntersecta(raton)) {
					if (slot.contieneItem()) {
						final Item i = slot.getItem();

						// 1. Caso Arrojadizo
						if (i instanceof Arrojadizo) {
							this.INVENTARIO.getSlotArrojadizo().establecerObjeto(i);
							Constantes.GESTOR_INVENTARIO.getInventarioJugador().invertirVisibilidad();
							return;
						}

						// 2. Caso Arma
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

						// 3. Caso Consumible
						if (i instanceof Consumible) {
							final Consumible c = (Consumible) i;
							c.consumir(Constantes.JUGADOR);
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

	/**
	 * Procesa acciones rápidas con clic derecho desde el HUD inferior (Hotbar).
	 */
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
						c.consumir(Constantes.JUGADOR);
						break;
					}
				}
			}
		} else if (!this.INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDerUnicaAct()) {
			this.INVENTARIO.setActivarItemDisponible(true);
		}
	}

	/***/
	/* ========================================================================= */
	/* 5. GESTIÓN DE EQUIPAMIENTO DE ARMAS */
	/* ========================================================================= */
	/***/

	/**
	 * Desequipa el arma activa y busca la primera casilla vacía disponible
	 * (priorizando la barra principal y luego el almacén).
	 */
	private void desequiparArma() {
		if ((this.slotArma == null) || !this.slotArma.contieneItem()) {
			return;
		}

		// 1. Intentar depositar en barra principal
		for (final Slot slot : this.LISTA_SLOTS_PRINCIPALES) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto(this.slotArma.getItem());
				this.slotArma.eliminarObjeto();
				return;
			}
		}

		// 2. Intentar depositar en almacén general
		for (final Slot slot : this.LISTA_SLOTS_ALMACEN) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto(this.slotArma.getItem());
				this.slotArma.eliminarObjeto();
				return;
			}
		}
	}

	/**
	 * Obtiene el arma equipada actualmente o una instancia de {@link Desarmado}.
	 * 
	 * @return El ítem arma activo o {@link Desarmado}.
	 */
	public Item getArmaEquipada() {
		if ((this.slotArma != null) && (this.slotArma.getItem() != null)) {
			return this.slotArma.getItem();
		}
		return new Desarmado();
	}

	/**
	 * Equipa un arma directamente en la ranura de arma y devuelve la que estaba
	 * equipada.
	 * 
	 * @param arma Nueva arma a equipar.
	 * @return El arma previamente equipada, o {@link Desarmado} si la casilla
	 *         estaba vacía.
	 */
	public Arma equiparArma(final Arma arma) {
		final Arma aux = ((this.slotArma != null) && (this.slotArma.getItem() != null)) ? (Arma) this.slotArma.getItem()
				: new Desarmado();

		if (this.slotArma != null) {
			this.slotArma.establecerObjeto(arma);
		}
		return aux;
	}

	/***/
	/* ========================================================================= */
	/* 6. INSERCIÓN INTELIGENTE Y APILADO DE ÍTEMS */
	/* ========================================================================= */
	/***/

	/**
	 * Inserta un ítem portable en la primera casilla vacía disponible.
	 * 
	 * @param item Ítem portable a guardar.
	 * @return {@code true} si se encontró espacio; {@code false} si el inventario
	 *         está lleno.
	 */
	public boolean agregarPortable(final Portable item) {
		for (final Slot slot : this.LISTA_SLOTS) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto((Portable) item.copiar());
				return true;
			}
		}
		return false;
	}

	/**
	 * Inserta un ítem consumible buscando pilas existentes del mismo tipo para
	 * acumularlas, o lo deposita en la primera casilla vacía disponible si sobra
	 * cantidad.
	 * 
	 * @param item Consumible a ingresar.
	 * @return {@code true} si se logró guardar total o parcialmente; {@code false}
	 *         si no hubo espacio.
	 */
	public boolean agregarConsumible(final Consumible item) {
		Slot slotVacio = null;
		Consumible cons = null;

		// 1. Intentar apilar en casillas con el mismo consumible
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

		// 2. Depositar el excedente en la primera casilla vacía
		if (slotVacio != null) {
			slotVacio.establecerObjeto((Consumible) item.copiar());
			item.establecerCantidad(0);
			return true;
		}

		return false;
	}

	/**
	 * Vacía por completo todas las casillas del inventario.
	 */
	public void vaciar() {
		for (final Slot slot : this.LISTA_SLOTS_GENERAL) {
			slot.establecerObjeto(null);
		}
	}

	/***/
	/* ========================================================================= */
	/* 7. PASADAS DE RENDERIZADO (GRAPHICS2D) */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja la cuadrícula de casillas del inventario (Capa 1).
	 */
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

	/**
	 * Dibuja el tooltip informativo del slot apuntado en el inventario (Capa 2).
	 */
	public void pintarTooltip(final Graphics2D g) {
		if ((this.slotApuntado != null) && this.slotApuntado.contieneItem()) {
			this.slotApuntado.pintarTooltip(g);
		}
	}

	/**
	 * Dibuja las casillas del HUD inferior cuando el inventario está cerrado (Capa
	 * 1).
	 */
	public void pintarSlotsIGU(final Graphics2D g) {
		this.slotIguApuntado = null;
		for (final SlotIGU slotIGU : this.LISTA_SLOTS_IGU) {
			slotIGU.pintar(g);
			if (slotIGU.apuntado() && slotIGU.contieneItem()) {
				this.slotIguApuntado = slotIGU;
			}
		}
	}

	/**
	 * Dibuja el tooltip informativo del HUD inferior (Capa 2).
	 */
	public void pintarTooltipIGU(final Graphics2D g) {
		if ((this.slotIguApuntado != null) && this.slotIguApuntado.contieneItem()) {
			this.slotIguApuntado.pintarTooltip(g);
		}
	}

	/***/
	/* ========================================================================= */
	/* 8. GENERACIÓN DE CUADRÍCULAS Y SLOTS EN MEMORIA */
	/* ========================================================================= */
	/***/

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

	/**
	 * Busca y obtiene el slot que intersecta con una coordenada de pantalla.
	 * 
	 * @param posicion Punto (X, Y) a evaluar.
	 * @return El {@link Slot} correspondiente, o {@code null} si ninguno coincide.
	 */
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