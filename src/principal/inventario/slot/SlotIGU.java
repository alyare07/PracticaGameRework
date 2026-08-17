package principal.inventario.slot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.utilidades.DibujoDebug;

/**
 * Representación visual y punto de acceso rápido (HUD / Hotbar) en la parte
 * inferior de la pantalla para un {@link Slot} lógico existente del inventario
 * del jugador.
 * 
 * <p>
 * <b>Patrón de Diseño (Proxy / View-Projection):</b>
 * </p>
 * <p>
 * Esta clase no almacena datos de ítems por su cuenta. Actúa como un
 * intermediario o vista proyectada hacia el {@link Slot} real de la barra
 * principal de acceso rápido. Cualquier cambio realizado en el HUD (usar
 * consumible, cambiar arma, arrastrar con ratón) impacta directamente en el
 * inventario base, garantizando una <b>Única Fuente de la Verdad (*Single
 * Source of Truth*)</b>.
 * </p>
 * 
 * <p>
 * <b>Rendimiento y Zero-GC:</b>
 * </p>
 * <ul>
 * <li>El rectángulo {@link #areaIGU} se crea una sola vez en el constructor
 * para las coordenadas del HUD.</li>
 * <li>Las pasadas de renderizado y detección de ratón reutilizan los métodos
 * parametrizados de {@link Slot} sin instanciar objetos temporales en
 * memoria.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Slot
 * @see principal.inventario.equipamiento.SlotManager
 */
public class SlotIGU {

	/***/
	/* ========================================================================= */
	/* 1. CONSTANTES GRÁFICAS Y ESTADO DEL HUD */
	/* ========================================================================= */
	/***/
	private static final Color COLOR_BORDE = Color.BLACK;
	private static final int MARGEN_BORDE_HUD = 1;
	private static final int EXPANSION_BORDE_HUD = 2;

	/** Referencia al slot lógico real contenido dentro del inventario */
	private final Slot slot;

	/** Coordenadas y dimensiones fijas en la pantalla para el HUD inferior */
	private final Rectangle areaIGU;

	/**
	 * Construye una proyección HUD vinculada a un slot lógico del inventario.
	 * 
	 * @param slot Instancia del slot lógico original que contiene los datos del
	 *             ítem.
	 * @param xIGU Coordenada X absoluta en la pantalla donde se dibujará la casilla
	 *             del HUD.
	 * @param yIGU Coordenada Y absoluta en la pantalla donde se dibujará la casilla
	 *             del HUD.
	 */
	public SlotIGU(final Slot slot, final int xIGU, final int yIGU) {
		this.slot = slot;
		this.areaIGU = new Rectangle(xIGU, yIGU, (slot != null) ? slot.getAncho() : 0,
				(slot != null) ? slot.getAlto() : 0);
	}

	/***/
	/* ========================================================================= */
	/* 2. ACTUALIZACIÓN LÓGICA (60 APS) */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado de interacción del ratón evaluando la colisión sobre el
	 * área fija del HUD inferior, delegando la verificación de hover al slot
	 * subyacente.
	 * 
	 * @param raton Instancia del controlador de entrada del ratón.
	 */
	public void actualizar(final Raton raton) {
		if (this.slot != null) {
			this.slot.actualizarIGU(raton, this.areaIGU);
		}
	}

	/***/
	/* ========================================================================= */
	/* 3. PASADAS DE RENDERIZADO (GRAPHICS2D) */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja la casilla del HUD en la pantalla (Capa 1).
	 * <p>
	 * Proyecta el contenido del slot original sobre las coordenadas de
	 * {@link #areaIGU} y añade un marco exterior distintivo para la interfaz de
	 * acceso rápido.
	 * </p>
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintar(final Graphics2D g) {
		if (this.slot == null) {
			return;
		}

		// 1. Dibujar el slot y su contenido sobre la posición del HUD
		this.slot.pintar(g, this.areaIGU);

		// 2. Dibujar el marco exterior para resaltar el HUD inferior
		DibujoDebug.dibujarRectanguloContorno(g, this.areaIGU.x - MARGEN_BORDE_HUD, this.areaIGU.y - MARGEN_BORDE_HUD,
				this.areaIGU.width + EXPANSION_BORDE_HUD, this.areaIGU.height + EXPANSION_BORDE_HUD, COLOR_BORDE);
	}

	/**
	 * Dibuja el tooltip informativo en la Capa 2 si el cursor del ratón está encima
	 * del HUD.
	 * 
	 * @param g Contexto gráfico 2D activo.
	 */
	public void pintarTooltip(final Graphics2D g) {
		if (this.slot != null) {
			this.slot.pintarTooltip(g);
		}
	}

	/***/
	/* ========================================================================= */
	/* 4. DELEGACIÓN DE ESTADO Y ACCESO (PROXY METHODS) */
	/* ========================================================================= */
	/* Métodos que redirigen las consultas directamente al Slot base. */
	/***/

	/**
	 * Comprueba si el cursor del ratón está actualmente sobre esta casilla del HUD.
	 * 
	 * @return {@code true} si el slot está apuntado; {@code false} en caso
	 *         contrario.
	 */
	public boolean apuntado() {
		return (this.slot != null) && this.slot.estaApuntado();
	}

	/**
	 * Comprueba si el slot subyacente posee un ítem no nulo.
	 * 
	 * @return {@code true} si hay un ítem alojado; {@code false} si está vacío.
	 */
	public boolean contieneItem() {
		return (this.slot != null) && this.slot.contieneItem();
	}

	/**
	 * Obtiene el ítem contenido en el slot lógico original.
	 * 
	 * @return Instancia del {@link Item} o {@code null} si la casilla está vacía.
	 */
	public Item getItem() {
		return (this.slot != null) ? this.slot.getItem() : null;
	}

	/**
	 * Establece un nuevo ítem en el slot lógico original.
	 * 
	 * @param item Objeto a depositar en la casilla.
	 */
	public void establecerObjeto(final Item item) {
		if (this.slot != null) {
			this.slot.establecerObjeto(item);
		}
	}

	/**
	 * Obtiene la referencia directa al {@link Slot} lógico real del inventario.
	 * 
	 * @return El objeto {@link Slot} original.
	 */
	public Slot getSlot() {
		return this.slot;
	}

	/**
	 * Obtiene los límites espaciales inmutables de la casilla en el HUD.
	 * 
	 * @return Instancia de {@link Rectangle} con la posición en pantalla del HUD.
	 */
	public Rectangle getAreaIGU() {
		return this.areaIGU;
	}
}