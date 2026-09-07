package principal.inventario.tienda;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.monedas.ItemMoneda;
import principal.inventario.Contenedor;
import principal.inventario.slot.Slot;
import principal.inventario.vault.InventarioVault;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Inventario comercial para NPCs y tiendas (Zero-GC / O(1)). Soporta
 * compra/venta unitaria o por lotes con Shift, venta desde cursor y
 * notificaciones visuales en pantalla fija.
 * 
 * @version 3.0 (Vanilla Java 8 - Bulk Trade & Drag Sell Support)
 */
public class InventarioTienda extends InventarioVault {

	private static final Color COLOR_BORDE_TIENDA = new Color(220, 180, 50); // Oro
	private static final Color COLOR_TEXTO_TITULO = new Color(255, 235, 180);
	private static final Color COLOR_PRECIO_COMPRA = new Color(255, 215, 50);

	private boolean stockInfinito = false;

	// Sistema de avisos visuales en espacio de pantalla (HUD)
	private String mensajeNotificacion = null;
	private Color colorNotificacion = Color.WHITE;
	private double tiempoNotificacionRestante = 0.0;

	public InventarioTienda(final Contenedor contenedor, final int cantSlots, final int cantMaxH, final String nombre) {
		super(contenedor, cantSlots, cantMaxH, nombre);
	}

	@Override
	public void actualizar(final Raton raton, final ItemPuntero itemPuntero, final Mundo mundo) {
		if (raton == null) {
			return;
		}

		// 1. Monitoreo de estado y cierre seguro
		this.actualizarEstadoCofre();

		// 2. Temporizador de la notificación en pantalla fija
		if (this.tiempoNotificacionRestante > 0.0) {
			final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);
			this.tiempoNotificacionRestante -= dt;
			if (this.tiempoNotificacionRestante <= 0.0) {
				this.mensajeNotificacion = null;
			}
		}

		// 3. Venta directa si el jugador hace clic sobre la tienda con un ítem en el
		// cursor
		if ((itemPuntero != null) && itemPuntero.contieneItem()) {
			if (raton.presionadoClickIzqUnicaAct() && this.getArea().contains(raton.getPuntoPosicionEscalado())) {
				this.venderItemPuntero(itemPuntero);
				return;
			}
		}

		// 4. Hover sobre las casillas
		final ArrayList<Slot> listaSlots = this.slots;
		for (int i = 0; i < listaSlots.size(); i++) {
			listaSlots.get(i).actualizar(raton);
		}

		// 5. Clic de compra sobre casilla de tienda (Shift = comprar lote máximo)
		if (raton.presionadoClickIzqUnicaAct() || raton.presionadoClickDerUnicaAct()) {
			final Slot slot = this.getSlot(raton.getPuntoPosicionEscalado());
			if ((slot != null) && slot.contieneItem()) {
				final boolean comprarLote = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
				this.intentarCompra(slot, comprarLote);
			}
		}
	}

	public void mostrarNotificacion(final String mensaje, final Color color) {
		this.mensajeNotificacion = mensaje;
		this.colorNotificacion = (color != null) ? color : Color.WHITE;
		this.tiempoNotificacionRestante = 2.0;
	}

	/**
	 * Transacción de compra (Unitaria o por lote con Shift).
	 */
	public boolean intentarCompra(final Slot slotTienda, final boolean comprarLote) {
		if ((slotTienda == null) || !slotTienda.contieneItem() || (Globales.JUGADOR == null)) {
			return false;
		}

		final Item itemTienda = slotTienda.getItem();
		final long precioUnitario = itemTienda.getPrecioBasePlata();

		// Determinar cantidad a comprar
		int cantidadDeseada = 1;
		if (comprarLote && (itemTienda instanceof Consumible)) {
			final Consumible c = (Consumible) itemTienda;
			final long dineroDisponible = Globales.JUGADOR.getDineroPlata();
			final int maxPorDinero = (int) Math.max(1, dineroDisponible / precioUnitario);
			cantidadDeseada = Math.min(c.getCantidad(), maxPorDinero);
		}

		final long costoTotal = precioUnitario * cantidadDeseada;

		// 1. Validar fondos
		if (!Globales.JUGADOR.tieneDineroSuficiente(costoTotal)) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			this.mostrarNotificacion("¡Fondos insuficientes!", new Color(255, 65, 65));
			return false;
		}

		// 2. Validar espacio en inventario intentando depositar una copia
		final Item copiaComprada = (Item) itemTienda.copiar();
		if (copiaComprada instanceof Consumible) {
			((Consumible) copiaComprada).establecerCantidad(cantidadDeseada);
		}

		final boolean agregado = Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(copiaComprada);
		if (!agregado) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			this.mostrarNotificacion("¡Inventario lleno!", new Color(255, 205, 50));
			return false;
		}

		// 3. Ejecutar cobro
		Globales.JUGADOR.restarDinero(costoTotal);
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		final String textoGasto = "-" + Item.formatearMoneda(costoTotal)
				+ (cantidadDeseada > 1 ? " (x" + cantidadDeseada + ")" : "");
		this.mostrarNotificacion("Comprado: " + textoGasto, new Color(100, 240, 120));

		// 4. Reducir stock si no es infinito
		if (!this.stockInfinito) {
			if (itemTienda instanceof Consumible) {
				final Consumible c = (Consumible) itemTienda;
				c.reducirCantidad(cantidadDeseada);
				if (c.getCantidad() <= 0) {
					slotTienda.eliminarObjeto();
				}
			} else {
				slotTienda.eliminarObjeto();
			}
		}

		return true;
	}

	/**
	 * Transacción de venta desde ranura de inventario (Unitaria o pila completa con
	 * Shift).
	 */
	public boolean venderItemJugador(final Slot slotJugador, final boolean venderTodo) {
		if ((slotJugador == null) || !slotJugador.contieneItem() || (Globales.JUGADOR == null)) {
			return false;
		}

		final Item itemVenta = slotJugador.getItem();

		if (itemVenta instanceof ItemMoneda) {
			return false;
		}

		final long precioUnitarioVenta = itemVenta.getPrecioVentaPlata();
		int cantidadAVender = 1;

		if (itemVenta instanceof Consumible) {
			final Consumible c = (Consumible) itemVenta;
			cantidadAVender = venderTodo ? c.getCantidad() : 1;
			c.reducirCantidad(cantidadAVender);
			if (c.getCantidad() <= 0) {
				slotJugador.eliminarObjeto();
			}
		} else {
			slotJugador.eliminarObjeto();
		}

		final long gananciaTotal = precioUnitarioVenta * cantidadAVender;

		// Sumar dinero
		Globales.JUGADOR.sumarDinero(gananciaTotal);
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		final String textoGanancia = "+" + Item.formatearMoneda(gananciaTotal)
				+ (cantidadAVender > 1 ? " (x" + cantidadAVender + ")" : "");
		this.mostrarNotificacion("Vendido: " + textoGanancia, new Color(255, 215, 80));

		// Recompra: coloca en la tienda si hay espacio
		final Item copiaParaTienda = (Item) itemVenta.copiar();
		if (copiaParaTienda instanceof Consumible) {
			((Consumible) copiaParaTienda).establecerCantidad(cantidadAVender);
		}
		this.agregarItem(copiaParaTienda);

		return true;
	}

	/**
	 * Venta directa de un ítem sostenido en el cursor.
	 */
	public boolean venderItemPuntero(final ItemPuntero itemPuntero) {
		if ((itemPuntero == null) || !itemPuntero.contieneItem() || (Globales.JUGADOR == null)) {
			return false;
		}

		final Item itemVenta = itemPuntero.getItem();
		if (itemVenta instanceof ItemMoneda) {
			return false;
		}

		int cantidad = 1;
		if (itemVenta instanceof Consumible) {
			cantidad = ((Consumible) itemVenta).getCantidad();
		}

		final long gananciaTotal = itemVenta.getPrecioVentaPlata() * cantidad;
		Globales.JUGADOR.sumarDinero(gananciaTotal);
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		final String texto = "+" + Item.formatearMoneda(gananciaTotal) + (cantidad > 1 ? " (x" + cantidad + ")" : "");
		this.mostrarNotificacion("Vendido: " + texto, new Color(255, 215, 80));

		final Item copia = (Item) itemVenta.copiar();
		this.agregarItem(copia);
		itemPuntero.limpiar();

		return true;
	}

	@Override
	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.getArea(), new Color(16, 20, 28, 240));
		Render2D.dibujarRectanguloContorno(g, this.getArea(), COLOR_BORDE_TIENDA);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 14f));

		final int anchoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.getNombre());
		final int xNombre = (this.getArea().x + (this.getArea().width / 2)) - (anchoNombre / 2);
		Render2D.dibujarStringConSombra(g, this.getNombre(), xNombre, this.getArea().y + 11, COLOR_TEXTO_TITULO,
				Color.BLACK);

		for (int i = 0; i < this.slots.size(); i++) {
			this.slots.get(i).pintar(g);
		}

		// Notificación en pantalla fija (HUD)
		if ((this.mensajeNotificacion != null) && (this.tiempoNotificacionRestante > 0.0)) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));

			final int anchoMsg = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.mensajeNotificacion);
			final int xMsg = this.getArea().x + ((this.getArea().width - anchoMsg) / 2);
			final int yMsg = this.getArea().y - 5;

			Render2D.dibujarRectanguloRelleno(g, xMsg - 5, yMsg - 11, anchoMsg + 10, 13, new Color(15, 18, 26, 240));
			Render2D.dibujarRectanguloContorno(g, xMsg - 5, yMsg - 11, anchoMsg + 10, 13, this.colorNotificacion);
			Render2D.dibujarStringConSombra(g, this.mensajeNotificacion, xMsg, yMsg, this.colorNotificacion,
					Color.BLACK);
		}

		g.setFont(fontPrevia);
	}

	@Override
	public void pintarTooltips(final Graphics2D g) {
		final Point pMouse = Globales.RATON.getPuntoPosicionEscalado();
		final Slot slot = this.getSlot(pMouse);

		if ((slot != null) && slot.contieneItem()) {
			final Item item = slot.getItem();
			final long precio = item.getPrecioBasePlata();
			final String lineaCompra = "Compra: " + Item.formatearMoneda(precio) + " [Shift: Lote]";

			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipItemConPrecio(g, item, lineaCompra, COLOR_PRECIO_COMPRA);
		}
	}

	public boolean isStockInfinito() {
		return this.stockInfinito;
	}

	public void setStockInfinito(final boolean stockInfinito) {
		this.stockInfinito = stockInfinito;
	}
}