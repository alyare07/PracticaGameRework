package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.inventario.slot.Slot;
import principal.utilidades.Render2D;

/**
 * Clase base abstracta para cualquier casilla del inventario destinada a
 * albergar equipamiento activo del personaje (armas, cascos, armaduras, botas,
 * accesorios, etc.).
 * 
 * <p>
 * <b>Características y Comportamiento Especializado:</b>
 * </p>
 * <ul>
 * <li><b>Identidad Visual Diferenciada:</b> Sobrescribe el renderizado de fondo
 * y borde ({@link #COLOR_FONDO} y {@link #COLOR_BORDE_APUNTADO}) para
 * distinguir visualmente las ranuras de equipamiento de las celdas de
 * almacenamiento común.</li>
 * <li><b>Silueta Guía (Placeholder Logo):</b> Proyecta una textura translúcida
 * representativa del tipo de ranura cuando la casilla está vacía, guiando
 * intuitivamente al jugador.</li>
 * <li><b>Contrato de Admisión Estricto:</b> Obliga a las subclases a
 * implementar {@link #validarAdmisionItem(Item)}, integrándose fluidamente con
 * {@link #puedeAceptar(Item)} para blindar el sistema contra equipamiento
 * indebido desde el puntero del ratón.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Slot
 * @see SlotArma
 */
public abstract class SlotEquipamiento extends Slot {

	/***/
	/* ========================================================================= */
	/* 1. PALETA DE COLORES DISTINTIVA PARA EQUIPAMIENTO */
	/* ========================================================================= */
	/***/
	protected static final Color COLOR_FONDO = Color.CYAN;
	protected static final Color COLOR_BORDE_APUNTADO = Color.BLUE;

	/** Silueta o icono translúcido dibujado cuando el slot está vacío */
	protected final BufferedImage logo;

	/**
	 * Construye una ranura de equipamiento con límites espaciales y un icono
	 * representativo.
	 * 
	 * @param area Rectángulo con la posición (X, Y) y dimensiones de la celda.
	 * @param logo Imagen/Textura translúcida representativa del tipo de equipo
	 *             (puede ser {@code null}).
	 */
	public SlotEquipamiento(final Rectangle area, final BufferedImage logo) {
		super(area);
		this.logo = logo;
	}

	/***/
	/* ========================================================================= */
	/* 2. PASADAS DE RENDERIZADO PERSONALIZADAS */
	/* ========================================================================= */
	/***/

	/**
	 * Dibuja el fondo coloreado y el borde de selección específico para
	 * equipamiento.
	 * 
	 * @param g    Contexto gráfico 2D activo.
	 * @param area Límites espaciales donde se pintará la base de la casilla.
	 */
	@Override
	protected void pintarArea(final Graphics2D g, final Rectangle area) {
		Render2D.dibujarRectanguloRelleno(g, area, COLOR_FONDO);
		if (this.apuntado) {
			Render2D.dibujarRectanguloContorno(g, area, COLOR_BORDE_APUNTADO);
		}
	}

	/**
	 * Dibuja el ítem equipado o la silueta translúcida si la casilla está vacía.
	 * 
	 * @param g    Contexto gráfico 2D activo.
	 * @param area Límites espaciales donde se acomodará el sprite o silueta.
	 */
	@Override
	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (this.item != null) {
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);
		} else if (this.logo != null) {
			Render2D.dibujarImagen(g, this.logo, area.x, area.y);
		}
	}

	/***/
	/* ========================================================================= */
	/* 3. INTEGRACIÓN DE REGLAS DE ADMISIÓN POLIMÓRFICA */
	/* ========================================================================= */
	/***/

	/**
	 * Conecta la regla polimórfica general del sistema de puntero con el contrato
	 * abstracto de equipamiento {@link #validarAdmisionItem(Item)}.
	 * 
	 * @param itemAColocar Ítem candidato a equipar.
	 * @return {@code true} si el ítem no es nulo y es admitido por la subclase de
	 *         equipo.
	 */
	@Override
	public boolean puedeAceptar(final Item itemAColocar) {
		return (itemAColocar != null) && this.validarAdmisionItem(itemAColocar);
	}

	/**
	 * Establece el objeto en la casilla únicamente si cumple con la validación de
	 * admisión o si se envía {@code null} para desequipar la pieza.
	 * 
	 * @param obj Ítem a colocar o {@code null} para vaciar la casilla.
	 */
	@Override
	public void establecerObjeto(final Item obj) {
		if ((obj == null) || this.validarAdmisionItem(obj)) {
			this.item = obj;
		}
	}

	/**
	 * Valida si un ítem específico cumple con los requisitos para ingresar en esta
	 * ranura de equipamiento (ej: verificar que sea un Casco, Armadura, Anillo o
	 * Arma).
	 *
	 * @param i Ítem a comprobar (o {@code null} para validar la acción de
	 *          desequipar).
	 * @return {@code true} si el ítem es apto para esta casilla; {@code false} en
	 *         caso contrario.
	 */
	public abstract boolean validarAdmisionItem(final Item i);

	/**
	 * Obtiene la textura del logo/silueta asignada a este tipo de ranura.
	 * 
	 * @return La imagen del placeholder, o {@code null} si no posee silueta.
	 */
	public BufferedImage getLogo() {
		return this.logo;
	}
}