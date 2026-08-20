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
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.inventario.Contenedor;
import principal.inventario.Inventario;
import principal.inventario.equipamiento.SlotManager;
import principal.inventario.slot.Slot;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Gestor de interfaz y almacenamiento para contenedores externos (cofres,
 * baúles, cadáveres saqueables o entidades con inventario propio).
 * 
 * <p>
 * <b>Características Arquitectónicas:</b>
 * </p>
 * <ul>
 * <li><b>Generación Dinámica de Cuadrícula:</b> Calcula el tamaño de la ventana
 * y distribuye las casillas en base al número total de slots y el límite
 * horizontal máximo (columnas).</li>
 * <li><b>Máquina de Estados de Interacción (FSM):</b> Gestiona la apertura y
 * cierre mediante {@link EstadoInventario}, evaluando la distancia del jugador
 * y teclas de interacción.</li>
 * <li><b>Resolución Espacial Desacoplada:</b> Obtiene el {@link Mundo} y el
 * {@link Ente} propietario a través de la interfaz {@link Contenedor},
 * asegurando que el cierre o destrucción del cofre arroje los ítems retenidos
 * en su posición espacial exacta.</li>
 * <li><b>Integración Zero-GC:</b> Reutiliza estructuras geométricas fijas
 * ({@link #area}, {@link #areaPortada}) y fuentes estáticas para no presionar
 * al Garbage Collector durante el renderizado.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Contenedor
 * @see Slot
 * @see ItemPuntero
 */
public class InventarioVault {

	/***/
	/* ========================================================================= */
	/* 1. MÁQUINA DE ESTADOS DEL CONTENEDOR (FSM) */
	/* ========================================================================= */
	/***/

	/**
	 * Representa el estado actual de interacción de la ventana del cofre.
	 */
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

	/***/
	/* ========================================================================= */
	/* 2. CONSTANTES DE DISEÑO Y RECURSOS GRÁFICOS (GC FRIENDLY) */
	/* ========================================================================= */
	/***/
	private static final int MARGEN = 2;
	private static final int MARGEN_PORTADA = 10;
	private static final Font FUENTE_PORTADA = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
	private static final Font FUENTE_SLOTS = new Font(Font.SANS_SERIF, Font.PLAIN, 6);
	private static final Color COLOR_BORDE = Color.LIGHT_GRAY;
	private static final Color COLOR_TEXTO_TITULO = Color.BLACK;

	/***/
	/* ========================================================================= */
	/* 3. ATRIBUTOS Y LÍMITES GEOMÉTRICOS */
	/* ========================================================================= */
	/***/
	private final int ladoSlots;
	private final ArrayList<Slot> slots;
	private final Rectangle area;
	private final Rectangle areaPortada;
	private final String nombre;
	private final GestorTiempo gtRatonPresiono;
	private final Contenedor contenedor;

	private EstadoInventario estadoInventario = EstadoInventario.CERRADO;
	private Slot slotApuntado;

	/**
	 * Construye el inventario externo, dimensiona su ventana y genera sus casillas.
	 * 
	 * @param contenedor Objeto o Ente propietario que implementa
	 *                   {@link Contenedor}.
	 * @param cantSlots  Cantidad total de casillas que contendrá el cofre.
	 * @param cantMaxH   Cantidad máxima de casillas horizontales por fila
	 *                   (columnas).
	 * @param nombre     Título que se mostrará en la portada superior de la
	 *                   ventana.
	 */
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

	/***/
	/* ========================================================================= */
	/* 4. ACTUALIZACIÓN LÓGICA Y ENTRADA DE USUARIO (60 APS) */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado de las casillas y procesa las interacciones de ratón
	 * mientras la ventana del contenedor está abierta.
	 * 
	 * @param raton       Instancia del controlador del ratón.
	 * @param itemPuntero Controlador del ítem sostenido en el cursor.
	 * @param mundo       Referencia al mundo activo.
	 */
	public void actualizar(final Raton raton, final ItemPuntero itemPuntero, final Mundo mundo) {
		if (raton == null) {
			return;
		}

		this.actualizarSlots(raton);
		this.actualizarClickIzquierdo(raton, itemPuntero);
		this.actualizarClickDerechoEquipamientoRapido(raton);
	}

	/**
	 * Procesa la transferencia o intercambio de ítems con el {@link ItemPuntero}
	 * mediante clic izquierdo.
	 */
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
	 * Acción rápida de equipamiento con clic derecho: Si el jugador hace clic
	 * derecho sobre un arma dentro del cofre y actualmente está desarmado, se
	 * equipa automáticamente.
	 */
	private void actualizarClickDerechoEquipamientoRapido(final Raton raton) {
		if (raton.presionadoClickDer()) {
			final Slot apuntado = this.getSlot(raton.getPuntoPosicionEscalado());
			if ((apuntado != null) && apuntado.contieneItem() && (apuntado.getItem() instanceof Arma)) {
				final Inventario invJugador = Globales.GESTOR_INVENTARIO.getInventarioJugador();
				if (invJugador.getArmaEquipada() instanceof Desarmado) {
					invJugador.equiparArma((Arma) apuntado.getItem());
					apuntado.eliminarObjeto();
				}
			}
		}
	}

	/***/
	/* ========================================================================= */
	/* 5. GESTIÓN DEL CICLO DE VIDA Y PROXIMIDAD DEL CONTENEDOR */
	/* ========================================================================= */
	/***/

	/**
	 * Evalúa las condiciones para abrir o cerrar la ventana del contenedor según la
	 * proximidad del jugador y la pulsación de la tecla de interacción.
	 */
	public void actualizarEstadoCofre() {
		final Ente propietario = this.getEntePropietario();
		if (propietario == null) {
			return;
		}

		// Comprobar si el jugador está dentro del rango de interacción del cofre
		final boolean jugadorEnRango = (Globales.JUGADOR != null)
				&& Globales.JUGADOR.getAreaInteraccionCofre().intersects(propietario.getArea());
		final boolean teclaPresionada = (Globales.TECLADO != null)
				&& Globales.TECLADO.TECLA_RECOGIENDO.presionadoUnicaActualizacion();

		if (this.estadoInventario == EstadoInventario.CERRADO) {
			// Apertura: Requiere estar en rango, pulsar tecla y que no haya otro menú
			// abierto
			if (!Globales.GESTOR_INVENTARIO.hayInventarioTerceroAbierto() && jugadorEnRango && teclaPresionada) {
				this.estadoInventario = EstadoInventario.ABIERTO;
				Globales.GESTOR_INVENTARIO.abrirInventarioTercero(this);
				Globales.GESTOR_INVENTARIO.getInventarioJugador().hacerVisible();
			}
		} else if (this.estadoInventario == EstadoInventario.ABIERTO) {
			// Cierre reactivo: Si el jugador se aleja, cierra su inventario o presiona la
			// tecla de nuevo
			if (!Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible() || teclaPresionada || !jugadorEnRango) {
				this.cerrar();
			}
		}
	}

	/**
	 * Cierra el cofre de forma segura, notificando al gestor de inventario para
	 * devolver o soltar al suelo cualquier ítem retenido en el puntero.
	 */
	public void cerrar() {
		this.estadoInventario = EstadoInventario.CERRADO;
		Globales.GESTOR_INVENTARIO.eliminarInventarioTercero(this.getMundo());
		if (Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
			Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
		}
	}

	/***/
	/* ========================================================================= */
	/* 6. PASADAS DE RENDERIZADO (GRAPHICS2D) */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja el fondo de la ventana, el título y sus casillas (Capa 1).
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarRectanguloRelleno(g, this.area, Inventario.GRIS_TRANSPARENTE);
		DibujoDebug.dibujarRectanguloContorno(g, this.area, COLOR_BORDE);

		this.pintarPortada(g);
		this.pintarSlots(g);
	}

	/**
	 * Dibuja el título del cofre centrado en la barra superior.
	 */
	private void pintarPortada(final Graphics2D g) {
		final Font fuenteOriginal = g.getFont();
		g.setFont(FUENTE_PORTADA);

		final int anchoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.nombre);
		final int altoNombre = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, this.nombre);
		final int xNombre = (this.areaPortada.x + (this.areaPortada.width / 2)) - (anchoNombre / 2);
		final int yNombre = this.areaPortada.y + (this.areaPortada.height / 2) + (altoNombre / 2);

		DibujoDebug.dibujarString(g, this.nombre, xNombre, yNombre, COLOR_TEXTO_TITULO);

		g.setFont(fuenteOriginal);
	}

	/**
	 * Dibuja las casillas del cofre y registra cuál está apuntada por el ratón.
	 */
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

	/**
	 * Dibuja el tooltip informativo del slot apuntado dentro del cofre (Capa 2).
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintarTooltips(final Graphics2D g) {
		if ((this.slotApuntado != null) && this.slotApuntado.contieneItem()) {
			this.slotApuntado.pintarTooltip(g);
		}
	}

	/***/
	/* ========================================================================= */
	/* 7. GENERACIÓN DINÁMICA DE CUADRÍCULA (LAYOUT ENGINE) */
	/* ========================================================================= */
	/* Calcula la geometría de la ventana y acomoda los slots en pantalla */
	/* centrados sobre el inventario principal del jugador. */
	/***/
	private void crearSlots(final int cant, final int cantMaxH) {
		if ((cantMaxH <= 0) || (cant <= 0)) {
			return;
		}

		// 1. Dimensionamiento del ancho en base al límite horizontal de columnas
		final int ancho = (cantMaxH * this.ladoSlots) + (cantMaxH * MARGEN) + MARGEN;
		final int cantFilas = ((cant + cantMaxH) - 1) / cantMaxH;

		// 2. Dimensionamiento del alto total (grilla + barra de título)
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

		// 3. Generación y ubicación de cada celda Slot
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

	/***/
	/* ========================================================================= */
	/* 8. MÉTODOS DE CONSULTA, ALMACENAMIENTO Y ACCESO */
	/* ========================================================================= */
	/***/

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
		Consumible cons = null;

		for (final Slot slot : this.slots) {
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