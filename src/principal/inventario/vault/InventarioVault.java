package principal.inventario.vault;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.Ente;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.inventario.Contenedor;
import principal.inventario.Inventario;
import principal.inventario.equipamiento.SlotManager;
import principal.inventario.slot.Slot;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;
import principal.utilidades.inventario.ItemPuntero;

public class InventarioVault {

	public enum EstadoInventario {
		ABIERTO("Abierto"), CERRADO("Cerrado");

		private final String DESCRIPCION;

		private EstadoInventario(final String descripcion) {
			this.DESCRIPCION = descripcion;
		}

		@Override
		public String toString() {
			return this.DESCRIPCION;
		}
	}

	private static final int MARGEN = 2;
	private static final int MARGEN_PORTADA = 10;
	private static final Font FUENTE_PORTADA = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
	private static final Font FUENTE_SLOTS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);
	private static final Color COLOR_BORDE = Color.LIGHT_GRAY;
	private static final Color COLOR_TEXTO_TITULO = Color.LIGHT_GRAY;

	private final int ladoSlots;
	private final ArrayList<Slot> slots;
	private final Rectangle area;
	private final Rectangle areaPortada;
	private final String nombre;
	private final GestorTiempo gtRatonPresiono;
	private final Contenedor contenedor;

	private EstadoInventario estadoInventario = EstadoInventario.CERRADO;
	private Slot slotApuntado;

	public InventarioVault(final Contenedor contenedor, final int cantSlots, final int cantMaxH, final String nombre) {
		this.contenedor = contenedor;
		this.ladoSlots = SlotManager.getLadoSlots();
		this.slots = new ArrayList<Slot>();
		this.area = new Rectangle();
		this.areaPortada = new Rectangle();
		this.nombre = (nombre != null) ? nombre : "";
		this.gtRatonPresiono = new GestorTiempo();

		this.crearSlots(cantSlots, cantMaxH);
	}

	public void actualizar(final Raton raton, final ItemPuntero itemPuntero, final Mundo mundo) {
		if (raton == null) {
			return;
		}

		this.actualizarSlots(raton);
		this.actualizarClickIzquierdo(raton, itemPuntero);
		this.actualizarTransferenciaRapida(raton);
	}

	private void actualizarClickIzquierdo(final Raton raton, final ItemPuntero itemPuntero) {
		if (raton.presionadoClickIzq()
				&& this.gtRatonPresiono.transcurrioMiliSegundos(Inventario.TIEMPO_ACTUALIZACION_RATON_PRESIONADO)) {

			final Slot slot = this.getSlot(raton.getPuntoPosicionEscalado());
			if (slot == null) {
				return;
			}

			this.gtRatonPresiono.establecerReferenciaTiempoActual();

			if (!itemPuntero.contieneItem()) {
				itemPuntero.agarrarItem(slot);
			} else {
				itemPuntero.interactuarConSlot(slot);
			}
		}
	}

	/**
	 * Transferencia Rápida con Clic Derecho (Quick-Withdraw): Mueve el ítem
	 * seleccionado del cofre directamente al inventario del jugador.
	 */
	private void actualizarTransferenciaRapida(final Raton raton) {
		if (raton.presionadoClickDerUnicaAct()) {
			final Slot apuntado = this.getSlot(raton.getPuntoPosicionEscalado());
			if ((apuntado != null) && apuntado.contieneItem()) {
				final Item item = apuntado.getItem();
				final Inventario invJugador = Globales.GESTOR_INVENTARIO.getInventarioJugador();

				if (invJugador.agregarObjeto(item)) {
					if (item instanceof Consumible) {
						if (((Consumible) item).getCantidad() <= 0) {
							apuntado.eliminarObjeto();
						}
					} else {
						apuntado.eliminarObjeto();
					}
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
		}
	}

	public void actualizarEstadoCofre() {
		final Ente propietario = this.getEntePropietario();
		if (propietario == null) {
			return;
		}

		final boolean jugadorEnRango = (Globales.JUGADOR != null)
				&& Globales.JUGADOR.getAreaInteraccionCofre().intersects(propietario.getArea());
		final boolean teclaPresionada = (Globales.TECLADO != null)
				&& Globales.TECLADO.TECLA_RECOGIENDO.presionadoUnicaActualizacion();

		if (this.estadoInventario == EstadoInventario.CERRADO) {
			if (!Globales.GESTOR_INVENTARIO.hayInventarioTerceroAbierto() && jugadorEnRango && teclaPresionada) {
				this.estadoInventario = EstadoInventario.ABIERTO;
				Globales.GESTOR_INVENTARIO.abrirInventarioTercero(this);
				Globales.GESTOR_INVENTARIO.getInventarioJugador().hacerVisible();
			}
		} else if (this.estadoInventario == EstadoInventario.ABIERTO) {
			if (!Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible() || teclaPresionada || !jugadorEnRango) {
				this.cerrar();
			}
		}
	}

	public void cerrar() {
		this.estadoInventario = EstadoInventario.CERRADO;
		Globales.GESTOR_INVENTARIO.eliminarInventarioTercero(this.getMundo());
		if (Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
			Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
		}
	}

	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.area, Inventario.GRIS_TRANSPARENTE);
		Render2D.dibujarRectanguloContorno(g, this.area, COLOR_BORDE);

		this.pintarPortada(g);
		this.pintarSlots(g);
	}

	private void pintarPortada(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		g.setFont(FUENTE_PORTADA);

		final int anchoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.nombre);
		final int altoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, this.nombre);
		final int xNombre = (this.areaPortada.x + (this.areaPortada.width / 2)) - (anchoNombre / 2);
		final int yNombre = this.areaPortada.y + (this.areaPortada.height / 2) + (altoNombre / 2);

		Render2D.dibujarString(g, this.nombre, xNombre, yNombre, COLOR_TEXTO_TITULO);
		g.setFont(fuenteOriginal);
	}

	private void pintarSlots(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		g.setFont(FUENTE_SLOTS);

		this.slotApuntado = null;
		for (final Slot slot : this.slots) {
			slot.pintar(g);
			if (slot.estaApuntado() && (this.slotApuntado == null)) {
				this.slotApuntado = slot;
			}
		}

		g.setFont(fuenteOriginal);
	}

	public void pintarTooltips(final Graphics2D g) {
		if ((this.slotApuntado != null) && this.slotApuntado.contieneItem()) {
			this.slotApuntado.pintarTooltip(g);
		}
	}

	private void crearSlots(final int cant, final int cantMaxH) {
		if ((cantMaxH <= 0) || (cant <= 0)) {
			return;
		}

		final int ancho = (cantMaxH * this.ladoSlots) + (cantMaxH * MARGEN) + MARGEN;
		final int cantFilas = ((cant + cantMaxH) - 1) / cantMaxH;

		final int alto = MARGEN + (cantFilas * this.ladoSlots) + (MARGEN * cantFilas);
		final int x = Constantes.CENTROX - (ancho / 2);
		final int y = Constantes.CENTROY - alto - (MARGEN * 3) - MARGEN_PORTADA;

		this.area.x = x;
		this.area.y = y;
		this.area.width = ancho;
		this.area.height = alto + MARGEN_PORTADA;

		this.areaPortada.x = x;
		this.areaPortada.y = y;
		this.areaPortada.width = ancho;
		this.areaPortada.height = MARGEN_PORTADA;

		int cantSlot = 0;
		for (int y2 = y + MARGEN + MARGEN_PORTADA; y2 < (y + alto); y2 += this.ladoSlots + MARGEN) {
			for (int x2 = x + MARGEN; x2 < (x + ancho); x2 += this.ladoSlots + MARGEN) {
				if (cantSlot >= cant) {
					break;
				}
				this.slots.add(new Slot(new Rectangle(x2, y2, this.ladoSlots, this.ladoSlots)));
				cantSlot++;
			}
		}
	}

	public Slot getSlot(final Point posicion) {
		if (posicion == null) {
			return null;
		}
		for (final Slot slot : this.slots) {
			if (slot.intersecta(posicion)) {
				return slot;
			}
		}
		return null;
	}

	public boolean contieneSlot(final Slot slot) {
		return this.slots.contains(slot);
	}

	public ArrayList<Item> getItems() {
		final ArrayList<Item> items = new ArrayList<Item>();
		for (final Slot slot : this.slots) {
			if (slot.contieneItem()) {
				items.add(slot.getItem());
			}
		}
		return items;
	}

	public boolean agregarItem(final Item item) {
		if (item == null) {
			return false;
		}
		switch (item.getTipoItem()) {
		case Item.COD_ITEM_CONSUMIBLE:
			return this.agregarConsumible((Consumible) item);
		case Item.COD_ITEM_PORTABLE:
			return this.agregarPortable((Portable) item);
		default:
			return false;
		}
	}

	private boolean agregarPortable(final Portable item) {
		for (final Slot slot : this.slots) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto((Portable) item.copiar());
				return true;
			}
		}
		return false;
	}

	private boolean agregarConsumible(final Consumible item) {
		Slot slotVacio = null;

		for (final Slot slot : this.slots) {
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
		return false;
	}

	public boolean ratonEnAreaInventario() {
		return Globales.RATON.getRectanguloPosicionEscalado().intersects(this.area);
	}

	public void vaciar() {
		for (final Slot slot : this.slots) {
			slot.establecerObjeto(null);
		}
	}

	private void actualizarSlots(final Raton raton) {
		for (final Slot slot : this.slots) {
			slot.actualizar(raton);
		}
	}

	public Mundo getMundo() {
		if ((this.contenedor != null) && (this.contenedor.getEntePropietario() != null)) {
			return this.contenedor.getEntePropietario().getMundo();
		}
		return null;
	}

	public Ente getEntePropietario() {
		return (this.contenedor != null) ? this.contenedor.getEntePropietario() : null;
	}

	public EstadoInventario getEstadoInventario() {
		return this.estadoInventario;
	}

	public Rectangle getArea() {
		return this.area;
	}

	public String getNombre() {
		return this.nombre;
	}

	public GestorTiempo getGestorTiempo() {
		return this.gtRatonPresiono;
	}

	public boolean intersectaArea(final Rectangle r) {
		if (r == null) {
			return false;
		}
		return r.intersects(this.area);
	}
}