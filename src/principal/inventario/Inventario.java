package principal.inventario;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.animaciones.Animaciones;
import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.Arma;
import principal.inventario.equipamiento.SlotManager;
import principal.inventario.slot.Slot;
import principal.inventario.slot.SlotArrojadizo;
import principal.mapa.Mundo;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Ventana central y gestor del inventario principal del jugador.
 * 
 * <p>
 * <b>Arquitectura y Ciclo de Vida Dual (Modo Ventana vs. Modo HUD):</b>
 * </p>
 * <ul>
 * <li><b>Modo Ventana Abierta ({@code visible == true}):</b> Renderiza el panel
 * completo con todas las secciones (ranuras de equipamiento, almacén, barra de
 * acceso rápido, cuadro de estadísticas y previsualización animada del
 * personaje).</li>
 * <li><b>Modo HUD / Hotbar ({@code visible == false}):</b> Desactiva la ventana
 * principal y delega la interacción exclusivamente al HUD inferior en pantalla
 * a través de {@link SlotManager#actualizarIGU(Raton)} y
 * {@link SlotManager#pintarSlotsIGU(Graphics2D)}.</li>
 * <li><b>Partición Geométrica Inmutable (Zero-GC):</b> Las regiones de la
 * interfaz (almacén, equipo, barra rápida) se calculan una sola vez en el
 * constructor y se reutilizan durante toda la partida.</li>
 * <li><b>Centrado y Compensación de Sprite del Personaje:</b> Alinea la
 * animación activa del jugador dentro de {@link #AREA_PERSONAJE} compensando
 * los márgenes internos de renderizado del sprite.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see SlotManager
 * @see SlotArrojadizo
 * @see ItemPuntero
 */
public class Inventario {

	/***/
	/* ========================================================================= */
	/* 1. CONSTANTES GRÁFICAS Y TEMPORIZADORES (GC FRIENDLY) */
	/* ========================================================================= */
	/***/
	public static final int TIEMPO_ACTUALIZACION_RATON_PRESIONADO = 500;
	public static final Color GRIS_TRANSPARENTE = new Color(80, 53, 67, 150);
	public static final Color NEGRO_TRANSPARENTE = new Color(43, 24, 34, 80);
	public static final Color BLANCO_TRANSPARENTE = new Color(255, 255, 255, 100);
	private static final Color COLOR_BORDE = Color.LIGHT_GRAY;

	/***/
	/* ========================================================================= */
	/* 2. LÍMITES GEOMÉTRICOS Y PARTICIONADO DE ZONAS */
	/* ========================================================================= */
	/***/
	private final int X;
	private final int Y;
	private final int ANCHO;
	private final int ALTO;
	private final int MARGEN_GENERAL;

	private final Rectangle AREA_TOTAL;
	private final Rectangle ZONA_INFO_JUGADOR;
	private final Rectangle ZONA_SLOTS_ALMACEN;
	private final Rectangle ZONA_SLOTS_PRINCIPALES;
	private final Rectangle ZONA_SLOTS_EQUIPAMIENTOS;
	private final Rectangle AREA_PERSONAJE;

	/***/
	/* ========================================================================= */
	/* 3. ESTADO Y GESTORES AUXILIARES */
	/* ========================================================================= */
	/***/
	private boolean visible;
	private Mundo mundo;
	private final GestorTiempo GE_RATON_PRESIONO;
	private boolean activarItemDisponible;
	private final SlotManager SLOT_MANAGER;
	private final SlotArrojadizo SLOT_ARROJADIZO;

	/**
	 * Construye la ventana del inventario, centra las dimensiones en pantalla y
	 * calcula las regiones rectangulares de cada subpanel.
	 */
	public Inventario() {
		this.ANCHO = 202;
		this.ALTO = 110;
		this.X = Globales.CONSTANTES.CENTROX - (this.ANCHO / 2);
		this.Y = Globales.CONSTANTES.CENTROY;
		this.MARGEN_GENERAL = 2;

		// 1. Límites globales y subsecciones
		this.AREA_TOTAL = new Rectangle(this.X, this.Y, this.ANCHO, this.ALTO);
		this.ZONA_INFO_JUGADOR = new Rectangle(this.X, this.Y, this.ANCHO, 25);
		this.ZONA_SLOTS_EQUIPAMIENTOS = new Rectangle(this.ZONA_INFO_JUGADOR.x + 25, this.Y + 1, this.ANCHO - 24, 18);
		this.ZONA_SLOTS_ALMACEN = new Rectangle(this.X, this.ZONA_INFO_JUGADOR.y + this.ZONA_INFO_JUGADOR.height,
				this.ANCHO, 62);
		this.ZONA_SLOTS_PRINCIPALES = new Rectangle(this.X, this.ZONA_SLOTS_ALMACEN.y + this.ZONA_SLOTS_ALMACEN.height,
				this.ANCHO, 22);
		this.AREA_PERSONAJE = new Rectangle(this.X + 2, this.Y + 2, 22, 22);

		// 2. Administrador central de casillas
		this.SLOT_MANAGER = new SlotManager(this, this.MARGEN_GENERAL, this.ZONA_SLOTS_ALMACEN,
				this.ZONA_SLOTS_PRINCIPALES, this.ZONA_SLOTS_EQUIPAMIENTOS);

		this.GE_RATON_PRESIONO = new GestorTiempo();
		this.activarItemDisponible = true;
		this.SLOT_ARROJADIZO = new SlotArrojadizo();
	}

	/***/
	/* ========================================================================= */
	/* 4. ACTUALIZACIÓN LÓGICA (60 APS) */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado lógico del inventario o del HUD según la visibilidad
	 * activa.
	 * 
	 * @param raton       Instancia del controlador del ratón.
	 * @param itemPuntero Controlador del ítem sostenido por el cursor.
	 * @param mundo       Referencia al mundo activo.
	 */
	public void actualizar(final Raton raton, final ItemPuntero itemPuntero, final Mundo mundo) {
		if (raton == null) {
			return;
		}

		// Si el inventario está cerrado, actualizar únicamente la barra de acceso
		// rápido del HUD
		if (!this.visible) {
			this.SLOT_MANAGER.actualizarIGU(raton);
			this.SLOT_ARROJADIZO.actualizar(raton);
			return;
		}

		// Si el inventario está abierto, actualizar ventana completa y ranura de
		// arrojadizos
		this.SLOT_ARROJADIZO.actualizar(raton);
		this.SLOT_MANAGER.actualizar(raton, this.GE_RATON_PRESIONO, TIEMPO_ACTUALIZACION_RATON_PRESIONADO, itemPuntero,
				mundo);
	}

	/***/
	/* ========================================================================= */
	/* 5. PASADAS DE RENDERIZADO (GRAPHICS2D) */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja la base visual del inventario (Capa 1). Si la ventana está cerrada,
	 * dibuja el HUD inferior.
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.visible) {
			this.SLOT_MANAGER.pintarSlotsIGU(g);
			return;
		}
		this.pintarInventario(g);
	}

	/**
	 * Dibuja los tooltips informativos por encima de las ventanas (Capa 2).
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintarTooltips(final Graphics2D g) {
		if (!this.visible) {
			this.SLOT_MANAGER.pintarTooltipIGU(g);
			return;
		}
		this.SLOT_MANAGER.pintarTooltip(g);
	}

	/**
	 * Renderiza los paneles de fondo, las casillas y el sprite animado del jugador.
	 */
	private void pintarInventario(final Graphics2D g) {
		// 1. Fondos de secciones
		DibujoDebug.dibujarRectanguloRelleno(g, this.ZONA_INFO_JUGADOR, GRIS_TRANSPARENTE);
		DibujoDebug.dibujarRectanguloRelleno(g, this.ZONA_SLOTS_ALMACEN, GRIS_TRANSPARENTE);
		DibujoDebug.dibujarRectanguloRelleno(g, this.ZONA_SLOTS_PRINCIPALES, NEGRO_TRANSPARENTE);
		DibujoDebug.dibujarRectanguloRelleno(g, this.AREA_PERSONAJE, BLANCO_TRANSPARENTE);
		DibujoDebug.dibujarRectanguloContorno(g, this.X, this.Y, this.ANCHO, this.ALTO, COLOR_BORDE);

		// 2. Grilla de slots
		this.SLOT_MANAGER.pintar(g);

		// 3. Previsualización del jugador centrada con compensación de offset
		if ((Globales.JUGADOR != null) && (Animaciones.JUGADOR != null)) {
			final int xAnim = (this.AREA_PERSONAJE.x
					+ ((this.AREA_PERSONAJE.width - Globales.JUGADOR.getAncho()) / 2))
					- Globales.JUGADOR.getMargenXSprite();
			final int yAnim = (this.AREA_PERSONAJE.y - Globales.JUGADOR.getMargenYSprite())
					+ ((this.AREA_PERSONAJE.height - Globales.JUGADOR.getAlto()) / 2);

			Animaciones.JUGADOR.pintar(g, xAnim, yAnim);
		}
	}

	/***/
	/* ========================================================================= */
	/* 6. GESTIÓN DE ÍTEMS Y ACCIONES DEL JUGADOR */
	/* ========================================================================= */
	/***/

	/**
	 * Agrega un nuevo ítem al inventario derivándolo según su tipo (Consumible o
	 * Portable).
	 * 
	 * @param item Ítem a guardar.
	 * @return {@code true} si fue agregado con éxito; {@code false} si no hubo
	 *         espacio.
	 */
	public boolean agregarObjeto(final Item item) {
		if (item == null) {
			return false;
		}
		switch (item.getTipoItem()) {
		case Item.COD_ITEM_CONSUMIBLE:
			return this.SLOT_MANAGER.agregarConsumible((Consumible) item);
		case Item.COD_ITEM_PORTABLE:
			return this.SLOT_MANAGER.agregarPortable((Portable) item);
		default:
			return false;
		}
	}

	/**
	 * Obtiene el slot bajo una coordenada específica de pantalla.
	 */
	public Slot getSlot(final Point posicion) {
		return this.SLOT_MANAGER.getSlot(posicion);
	}

	/**
	 * Equipa un arma directamente en la ranura de equipamiento.
	 */
	public Arma equiparArma(final Arma arma) {
		return this.SLOT_MANAGER.equiparArma(arma);
	}

	/**
	 * Obtiene el arma equipada actualmente.
	 */
	public Item getArmaEquipada() {
		return this.SLOT_MANAGER.getArmaEquipada();
	}

	/**
	 * Vacía por completo el inventario.
	 */
	public void vaciar() {
		this.SLOT_MANAGER.vaciar();
	}

	/***/
	/* ========================================================================= */
	/* 7. VISIBILIDAD, LÍMITES Y ACCESORES */
	/* ========================================================================= */
	/***/

	public SlotArrojadizo getSlotArrojadizo() {
		return this.SLOT_ARROJADIZO;
	}

	public Rectangle getArea() {
		return this.AREA_TOTAL;
	}

	public void hacerVisible() {
		this.visible = true;
	}

	public void ocultar() {
		this.visible = false;
	}

	public boolean esVisible() {
		return this.visible;
	}

	public void invertirVisibilidad() {
		this.visible = !this.visible;
	}

	public GestorTiempo getGestorTiempoRaton() {
		return this.GE_RATON_PRESIONO;
	}

	public void setActivarItemDisponible(final boolean activarItemDisponible) {
		this.activarItemDisponible = activarItemDisponible;
	}

	public boolean getActivarItemDisponible() {
		return this.activarItemDisponible;
	}

	public int getX() {
		return this.X;
	}

	public int getY() {
		return this.Y;
	}

	public int getAncho() {
		return this.ANCHO;
	}

	public int getAlto() {
		return this.ALTO;
	}

	public void establecerMundo(final Mundo mundo) {
		this.mundo = mundo;
	}
}