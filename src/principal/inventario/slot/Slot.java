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

/**
 * Casilla interactiva de interfaz con renderizado Pixel-Art táctico (Zero-GC).
 * 
 * @version 3.1 (Vanilla Java 8 - Crisp Pixel Art)
 */
public class Slot {

	private static final Color COLOR_TEXTO_LLENO = new Color(255, 255, 255);
	private static final Color COLOR_TEXTO_MEDIO = new Color(255, 205, 50);
	private static final Color COLOR_TEXTO_VACIO = new Color(255, 65, 65);

	private static final Color COLOR_BARRA_FONDO = new Color(15, 15, 20, 220);
	private static final Color COLOR_BARRA_LLENA = new Color(40, 220, 240);
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
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipItem(g, this.item);
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

		// 2. Cantidad de Consumibles (Esquina superior derecha con sombra)
		if (this.item instanceof Consumible) {
			final Font fuentePrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 5.5f));

			final String cantidad = String.valueOf(((Consumible) this.item).getCantidad());
			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, cantidad);

			final int xCant = (area.x + area.width) - anchoTexto - 1;
			final int yCant = area.y + 6;

			Render2D.dibujarStringConSombra(g, cantidad, xCant, yCant, Color.WHITE, Color.BLACK);

			g.setFont(fuentePrevia);
		}
		// 3. Munición de Armas de Fuego (Esquina inferior derecha + micro-barra)
		else if (this.item instanceof Arma) {
			final Arma arma = (Arma) this.item;
			if (arma.esArmaDistancia()) {
				final Font fuentePrevia = g.getFont();
				g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 5.5f));

				if (arma.isRecargando()) {
					final String txtRec = "REC";
					final int anchoRec = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtRec);
					final int xRec = area.x + ((area.width - anchoRec) / 2);
					final int yRec = (area.y + area.height) - 3;

					Render2D.dibujarStringConSombra(g, txtRec, xRec, yRec, COLOR_TEXTO_RECARGA, Color.BLACK);
				} else {
					final int balas = arma.getBalasCargador();
					final int capacidad = Math.max(1, arma.getCapacidadCargador());
					final double ratio = (double) balas / capacidad;

					// Micro-barra inferior
					final int barraAnchoMax = area.width - 2;
					final int barraProgreso = (int) Math.round(ratio * barraAnchoMax);
					final int barraX = area.x + 1;
					final int barraY = (area.y + area.height) - 2;

					Render2D.dibujarRectanguloRelleno(g, barraX, barraY, barraAnchoMax, 1, COLOR_BARRA_FONDO);
					if (barraProgreso > 0) {
						final Color colorBarra = (ratio > 0.35) ? COLOR_BARRA_LLENA
								: ((ratio > 0.15) ? COLOR_TEXTO_MEDIO : COLOR_TEXTO_VACIO);
						Render2D.dibujarRectanguloRelleno(g, barraX, barraY, barraProgreso, 1, colorBarra);
					}

					// Número con sombra de alto contraste
					final String txtBalas = String.valueOf(balas);
					final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtBalas);

					final int xNum = (area.x + area.width) - anchoTexto - 1;
					final int yNum = (area.y + area.height) - 4;

					final Color colorNumero = (balas == 0) ? COLOR_TEXTO_VACIO
							: ((ratio <= 0.30) ? COLOR_TEXTO_MEDIO : COLOR_TEXTO_LLENO);

					Render2D.dibujarStringConSombra(g, txtBalas, xNum, yNum, colorNumero, Color.BLACK);
				}

				g.setFont(fuentePrevia);
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