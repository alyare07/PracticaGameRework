package principal.inventario.slot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.inventario.Inventario;
import principal.inventario.equipamiento.SlotManager;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;

/**
 * Representa una celda o casilla interactiva dentro de cualquier interfaz de
 * usuario (IGU). Capaz de contener, validar y renderizar ítems con badges de
 * cantidad y estado de recarga.
 * 
 * @version 2.0 (Java 8 Compatible - Zero-GC Architecture)
 */
public class Slot {

	private static final Font FUENTE_CANTIDAD = new Font(Font.SANS_SERIF, Font.PLAIN, 5);
	private static final Font FUENTE_MUNICION = new Font(Font.SANS_SERIF, Font.PLAIN, 4);
	private static final Font FUENTE_TOOLTIP = new Font(Font.SANS_SERIF, Font.PLAIN, 5);

	private static final Color COLOR_FONDO_RECARGA = new Color(30, 30, 30, 220);
	private static final Color COLOR_TEXTO_RECARGA = new Color(255, 185, 40);

	protected static final int MARGEN_ESPACIADO_DEFECTO = 2;

	protected final Rectangle AREA;
	protected final int MARGEN_ESPACIADO;
	protected Item item;
	protected boolean apuntado;
	protected int valorPrioridad;

	public Slot(final Rectangle area, final Item item) {
		this.AREA = area;
		this.item = item;
		this.MARGEN_ESPACIADO = MARGEN_ESPACIADO_DEFECTO;
	}

	public Slot(final Rectangle area) {
		this(area, null);
	}

	public Slot(final int x, final int y) {
		this(new Rectangle(x, y, SlotManager.getLadoSlots(), SlotManager.getLadoSlots()), null);
	}

	public boolean puedeAceptar(final Item itemAColocar) {
		return itemAColocar != null;
	}

	public boolean puedeExtraer() {
		return true;
	}

	public void actualizar(final Raton raton) {
		if ((raton != null) && raton.getRectanguloPosicionEscalado().intersects(this.AREA)) {
			this.apuntado = true;
		} else {
			this.apuntado = false;
		}
		this.verificarEliminacion();
	}

	public void actualizarIGU(final Raton raton, final Rectangle areaHUD) {
		if ((raton != null) && raton.getRectanguloPosicionEscalado().intersects(areaHUD)) {
			this.apuntado = true;
		} else {
			this.apuntado = false;
		}
		this.verificarEliminacion();
	}

	protected void verificarEliminacion() {
		if (this.contieneItem() && this.item.estaEliminado()) {
			this.eliminarObjeto();
		}
	}

	public void pintar(final Graphics2D g) {
		this.pintarArea(g, this.AREA);
		this.pintarObjeto(g, this.AREA);
	}

	public void pintar(final Graphics2D g, final Rectangle area) {
		this.pintarArea(g, area);
		this.pintarObjeto(g, area);
	}

	public void pintarSoloSlot(final Graphics2D g) {
		this.pintarArea(g, this.AREA);
	}

	public void pintarTooltip(final Graphics2D g) {
		if (this.apuntado && this.contieneItem()) {
			final Font fuenteOriginal = g.getFont();
			g.setFont(FUENTE_TOOLTIP);
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipItem(g, this.item);
			g.setFont(fuenteOriginal);
		}
	}

	protected void pintarArea(final Graphics2D g, final Rectangle area) {
		Render2D.dibujarRectanguloRelleno(g, area, Inventario.BLANCO_TRANSPARENTE);
		if (this.apuntado) {
			Render2D.dibujarRectanguloContorno(g, area, Color.YELLOW);
		}
	}

	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (this.item == null) {
			return;
		}

		// 1. Dibujar textura del ítem
		this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);

		// 2. Metadatos de Consumible (Cantidad de stack en esquina superior)
		if (this.item instanceof Consumible) {
			final Font fuenteOriginal = g.getFont();
			g.setFont(FUENTE_CANTIDAD);

			Render2D.dibujarRectanguloRelleno(g, area.x, area.y, 6, 6, Color.LIGHT_GRAY);
			Render2D.dibujarString(g, String.valueOf(((Consumible) this.item).getCantidad()), area.x, area.y + 6,
					Color.BLACK);

			g.setFont(fuenteOriginal);
		}
		// 3. Metadatos de Arma de Fuego (Balas en cargador o estado de recarga)
		else if (this.item instanceof Arma) {
			final Arma arma = (Arma) this.item;
			if (arma.esArmaDistancia()) {
				final Font fuenteOriginal = g.getFont();
				g.setFont(FUENTE_MUNICION);

				// Visualización de estado RECARGANDO
				if (arma.isRecargando()) {
					Render2D.dibujarRectanguloRelleno(g, area.x, (area.y + area.height) - 6, 14, 5,
							COLOR_FONDO_RECARGA);
					Render2D.dibujarString(g, "REC...", area.x + 1, (area.y + area.height) - 2, COLOR_TEXTO_RECARGA);
				} else {
					final String cantidadBalas = String.valueOf(arma.getBalasCargador());
					final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, cantidadBalas);
					final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, cantidadBalas);

					Render2D.dibujarRectanguloRelleno(g, area.x, (area.y + area.height) - altoTexto - 1, 11, 6,
							Color.LIGHT_GRAY);
					Render2D.dibujarString(g, cantidadBalas, area.x, (area.y + area.height) - (altoTexto / 2),
							Color.BLACK);
					Render2D.dibujarImagen(g, Textura.getTextura(Textura.TEXTURA_x4_BALA), area.x + anchoTexto,
							(area.y + area.height) - altoTexto);
				}

				g.setFont(fuenteOriginal);
			}
		}
	}

	public boolean ratonIntersecta(final Raton raton) {
		if (raton == null) {
			return false;
		}
		return raton.getRectanguloPosicionEscalado().intersects(this.AREA);
	}

	public boolean intersecta(final Point punto) {
		if (punto == null) {
			return false;
		}
		return this.AREA.contains(punto.x, punto.y);
	}

	public void establecerObjeto(final Item obj) {
		this.item = obj;
	}

	public void eliminarObjeto() {
		this.item = null;
	}

	public boolean contieneItem() {
		return this.item != null;
	}

	public Item getItem() {
		return this.item;
	}

	public Arrojadizo getItemArrojadizo() {
		if (this.item instanceof Arrojadizo) {
			return (Arrojadizo) this.item;
		}
		return null;
	}

	public boolean estaApuntado() {
		return this.apuntado;
	}

	public void setX(final int x) {
		this.AREA.x = x;
	}

	public void setY(final int y) {
		this.AREA.y = y;
	}

	public void setAncho(final int ancho) {
		this.AREA.width = ancho;
	}

	public void setAlto(final int alto) {
		this.AREA.height = alto;
	}

	public int getX() {
		return this.AREA.x;
	}

	public int getY() {
		return this.AREA.y;
	}

	public int getAncho() {
		return this.AREA.width;
	}

	public int getAlto() {
		return this.AREA.height;
	}

	public Rectangle getArea() {
		return this.AREA;
	}

	public void setValorPrioridad(final int valor) {
		this.valorPrioridad = valor;
	}

	public int getValorPrioridad() {
		return this.valorPrioridad;
	}
}