package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.objetos.items.Item;
import principal.inventario.slot.Slot;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Clase base para cascos, armaduras, armas, botas y anillos. Renderiza un fondo
 * de slot táctico en relieve oscuro con siluetas guía de alta legibilidad.
 * 
 * @version 2.0 (Vanilla Java 8 - Dark Tactical UI)
 */
public abstract class SlotEquipamiento extends Slot {

	// =========================================================================
	// === PALETA CROMÁTICA EN RELIEVE (ESTÉTICA GRAFITO / ACERO)
	// =========================================================================
	protected static final Color COLOR_FONDO_EQUIPO = new Color(20, 24, 32, 240);
	protected static final Color COLOR_BORDE_REPOSO = new Color(55, 60, 75, 200);
	protected static final Color COLOR_BORDE_APUNTADO = new Color(70, 210, 255);
	protected static final Color COLOR_GUIA_VACIO = new Color(80, 90, 110, 160);

	/** Silueta o icono translúcido dibujado cuando el slot está vacío */
	protected final BufferedImage logo;

	public SlotEquipamiento(final Rectangle area, final BufferedImage logo) {
		super(area);
		this.logo = logo;
	}

	@Override
	protected void pintarArea(final Graphics2D g, final Rectangle area) {
		// 1. Fondo de ranura hundida oscura
		Render2D.dibujarRectanguloRelleno(g, area, COLOR_FONDO_EQUIPO);

		// 2. Borde de ranura individual
		if (this.apuntado) {
			Render2D.dibujarRectanguloContorno(g, area, COLOR_BORDE_APUNTADO);
		} else {
			Render2D.dibujarRectanguloContorno(g, area, COLOR_BORDE_REPOSO);
		}
	}

	@Override
	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (this.item != null) {
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);
		} else if (this.logo != null) {
			Render2D.dibujarImagen(g, this.logo, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);
		} else {
			// Si no hay archivo PNG de silueta, dibujamos un indicador guía limpio
			this.pintarGuiaRespaldo(g, area);
		}
	}

	/**
	 * Dibuja un placeholder tipográfico sutil si el PNG de la silueta aún no
	 * existe.
	 */
	protected void pintarGuiaRespaldo(final Graphics2D g, final Rectangle area) {
		final Font fuentePrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 5f));

		String etiqueta = "";
		if (this instanceof SlotArma) {
			etiqueta = "ARM";
		} else if (this instanceof SlotPiezaEquipo) {
			switch (((SlotPiezaEquipo) this).getTipoRequerido()) {
			case CASCO:
				etiqueta = "CAS";
				break;
			case TORSO:
				etiqueta = "TOR";
				break;
			case BOTAS:
				etiqueta = "BOT";
				break;
			case ANILLO:
				etiqueta = "ANI";
				break;
			default:
				etiqueta = "EQ";
				break;
			}
		}

		if (!etiqueta.isEmpty()) {
			final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, etiqueta);
			final int alto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, etiqueta);

			final int x = area.x + ((area.width - ancho) / 2);
			final int y = ((area.y + (area.height / 2)) + (alto / 2)) - 1;

			Render2D.dibujarString(g, etiqueta, x, y, COLOR_GUIA_VACIO);
		}

		g.setFont(fuentePrevia);
	}

	@Override
	public boolean puedeAceptar(final Item itemAColocar) {
		return (itemAColocar != null) && this.validarAdmisionItem(itemAColocar);
	}

	@Override
	public void establecerObjeto(final Item obj) {
		if ((obj == null) || this.validarAdmisionItem(obj)) {
			this.item = obj;
		}
	}

	public abstract boolean validarAdmisionItem(final Item i);

	public BufferedImage getLogo() {
		return this.logo;
	}
}