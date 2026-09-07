package principal.inventario.tienda;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
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
 * Inventario comercial para NPCs y tiendas (Zero-GC / O(1)). Transacciones
 * atómicas de compra y venta con avisos visuales en pantalla fija y tooltips
 * enriquecidos con precio en renglón aparte.
 * 
 * @version 2.2 (Vanilla Java 8 - Dedicated Price Line Tooltip)
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

		// 3. Hover sobre las casillas
		final ArrayList<Slot> listaSlots = this.slots;
		for (int i = 0; i < listaSlots.size(); i++) {
			listaSlots.get(i).actualizar(raton);
		}

		// 4. Clic de compra sobre casilla de tienda
		if (raton.presionadoClickIzqUnicaAct() || raton.presionadoClickDerUnicaAct()) {
			final Slot slot = this.getSlot(raton.getPuntoPosicionEscalado());
			if ((slot != null) && slot.contieneItem()) {
				this.intentarCompra(slot);
			}
		}
	}

	public void mostrarNotificacion(final String mensaje, final Color color) {
		this.mensajeNotificacion = mensaje;
		this.colorNotificacion = (color != null) ? color : Color.WHITE;
		this.tiempoNotificacionRestante = 2.0;
	}

	public boolean intentarCompra(final Slot slotTienda) {
		if ((slotTienda == null) || !slotTienda.contieneItem() || (Globales.JUGADOR == null)) {
			return false;
		}

		final Item itemTienda = slotTienda.getItem();
		final long precio = itemTienda.getPrecioBasePlata();

		if (!Globales.JUGADOR.tieneDineroSuficiente(precio)) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			this.mostrarNotificacion("¡Fondos insuficientes!", new Color(255, 65, 65));
			return false;
		}

		final Item copiaComprada = (Item) itemTienda.copiar();
		if (copiaComprada instanceof Consumible) {
			((Consumible) copiaComprada).establecerCantidad(1);
		}

		final boolean agregado = Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(copiaComprada);
		if (!agregado) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			this.mostrarNotificacion("¡Inventario lleno!", new Color(255, 205, 50));
			return false;
		}

		Globales.JUGADOR.restarDinero(precio);
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		this.mostrarNotificacion("Comprado: -" + Item.formatearMoneda(precio), new Color(100, 240, 120));

		if (!this.stockInfinito) {
			if (itemTienda instanceof Consumible) {
				final Consumible c = (Consumible) itemTienda;
				c.reducirCantidad(1);
				if (c.getCantidad() <= 0) {
					slotTienda.eliminarObjeto();
				}
			} else {
				slotTienda.eliminarObjeto();
			}
		}

		return true;
	}

	public boolean venderItemJugador(final Slot slotJugador) {
		if ((slotJugador == null) || !slotJugador.contieneItem() || (Globales.JUGADOR == null)) {
			return false;
		}

		final Item itemVenta = slotJugador.getItem();

		if (itemVenta instanceof ItemMoneda) {
			return false;
		}

		final long ganancia = itemVenta.getPrecioVentaPlata();

		if (itemVenta instanceof Consumible) {
			final Consumible c = (Consumible) itemVenta;
			c.reducirCantidad(1);
			if (c.getCantidad() <= 0) {
				slotJugador.eliminarObjeto();
			}
		} else {
			slotJugador.eliminarObjeto();
		}

		Globales.JUGADOR.sumarDinero(ganancia);
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		this.mostrarNotificacion("Vendido: +" + Item.formatearMoneda(ganancia), new Color(255, 215, 80));

		final Item copiaParaTienda = (Item) itemVenta.copiar();
		if (copiaParaTienda instanceof Consumible) {
			((Consumible) copiaParaTienda).establecerCantidad(1);
		}
		this.agregarItem(copiaParaTienda);

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
			final String lineaCompra = "Compra: " + Item.formatearMoneda(precio);

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