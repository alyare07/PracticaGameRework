package principal.inventario;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

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
import principal.recursos.TexturaItem;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.inventario.ItemPuntero;

/**
 * Ventana central del inventario del jugador (240 px) con cabecera de 8 slots
 * de equipamiento, Atributos RPG, Resistencia Térmica y Billetera (Zero-GC /
 * O(1)).
 * 
 * @version 4.3 (Vanilla Java 8 - Thermal Resistance HUD Integration)
 */
public class Inventario {

	public static final int TIEMPO_ACTUALIZACION_RATON_PRESIONADO = 500;

	public static final Color FONDO_PANEL_OSCURO = new Color(16, 18, 24, 235);
	public static final Color FONDO_SECCION_HEROE = new Color(24, 28, 38, 240);
	public static final Color FONDO_SLOTS_ALMACEN = new Color(20, 23, 30, 220);
	public static final Color FONDO_SLOTS_HOTBAR = new Color(12, 14, 18, 245);
	public static final Color BORDE_EXTERIOR_PANEL = new Color(75, 80, 95, 255);
	public static final Color BORDE_INTERIOR_BISEL = new Color(35, 40, 50, 200);

	public static final Color GRIS_TRANSPARENTE = FONDO_PANEL_OSCURO;
	public static final Color NEGRO_TRANSPARENTE = FONDO_SLOTS_HOTBAR;
	public static final Color BLANCO_TRANSPARENTE = new Color(30, 35, 45, 180);

	private static final Color COLOR_TEXTO_FUE = new Color(255, 180, 70);
	private static final Color COLOR_TEXTO_AGI = new Color(140, 240, 100);
	private static final Color COLOR_TEXTO_INT = new Color(100, 215, 255);
	private static final Color COLOR_TEXTO_DEF = new Color(220, 225, 240);
	private static final Color COLOR_TEXTO_ORO = new Color(255, 215, 50);
	private static final Color COLOR_TEXTO_PLATA = new Color(210, 225, 240);
	private static final Color COLOR_DESC_TOOLTIP = new Color(230, 235, 245);

	private static final String TITULO_FUE = "Fuerza: ";
	private static final String DESC_FUE = "Aumenta el daño melee (+0.5/pt) y la vida máxima (+2 HP/pt).";

	private static final String TITULO_AGI = "Agilidad: ";
	private static final String DESC_AGI = "Aumenta la velocidad (+0.01/pt) y la estamina (+0.5/pt).";

	private static final String TITULO_INT = "Inteligencia: ";
	private static final String DESC_INT = "Aumenta la estamina (+0.5/pt) y el poder de habilidades mágicas.";

	private static final String TITULO_DEF = "Defensa: ";
	private static final String DESC_DEF = "Reduce el daño recibido de todos los ataques.";

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

	private final Rectangle areaStatsFUE;
	private final Rectangle areaStatsAGI;
	private final Rectangle areaStatsINT;
	private final Rectangle areaStatsDEF;
	private final Rectangle areaBilletera;

	private long lastDineroPlata = -1;
	private String cachedOro = "0";
	private String cachedPlata = "0";

	private boolean visible;
	private Mundo mundo;
	private final GestorTiempo GE_RATON_PRESIONO;
	private boolean activarItemDisponible;
	private final SlotManager SLOT_MANAGER;
	private final SlotArrojadizo SLOT_ARROJADIZO;

	public Inventario() {
		this.ANCHO = 240;
		this.ALTO = 110;
		this.X = Constantes.CENTROX - (this.ANCHO / 2);
		this.Y = Constantes.CENTROY;
		this.MARGEN_GENERAL = 2;

		this.AREA_TOTAL = new Rectangle(this.X, this.Y, this.ANCHO, this.ALTO);
		this.ZONA_INFO_JUGADOR = new Rectangle(this.X, this.Y, this.ANCHO, 25);
		this.ZONA_SLOTS_EQUIPAMIENTOS = new Rectangle(this.X + 26, this.Y + 3, 158, 18);
		this.ZONA_SLOTS_ALMACEN = new Rectangle(this.X, this.ZONA_INFO_JUGADOR.y + this.ZONA_INFO_JUGADOR.height,
				this.ANCHO, 62);
		this.ZONA_SLOTS_PRINCIPALES = new Rectangle(this.X, this.ZONA_SLOTS_ALMACEN.y + this.ZONA_SLOTS_ALMACEN.height,
				this.ANCHO, 22);
		this.AREA_PERSONAJE = new Rectangle(this.X + 3, this.Y + 3, 20, 20);

		final int xStats = this.X + 186;
		final int yBase = this.Y + 5;
		this.areaStatsFUE = new Rectangle(xStats, yBase - 4, 25, 5);
		this.areaStatsAGI = new Rectangle(xStats, yBase + 1, 25, 5);
		this.areaStatsINT = new Rectangle(xStats, yBase + 6, 25, 5);
		this.areaStatsDEF = new Rectangle(xStats, yBase + 11, 25, 5);

		final int xDinero = this.X + 213;
		this.areaBilletera = new Rectangle(xDinero, yBase - 4, 25, 22);

		this.SLOT_MANAGER = new SlotManager(this, this.MARGEN_GENERAL, this.ZONA_SLOTS_ALMACEN,
				this.ZONA_SLOTS_PRINCIPALES, this.ZONA_SLOTS_EQUIPAMIENTOS);

		this.GE_RATON_PRESIONO = new GestorTiempo();
		this.activarItemDisponible = true;
		this.SLOT_ARROJADIZO = new SlotArrojadizo();
	}

	public void actualizar(final Raton raton, final ItemPuntero itemPuntero, final Mundo mundo) {
		if (raton == null) {
			return;
		}

		if (!this.visible) {
			this.SLOT_MANAGER.actualizarIGU(raton);
			this.SLOT_ARROJADIZO.actualizar(raton);
			return;
		}

		this.SLOT_ARROJADIZO.actualizar(raton);
		this.SLOT_MANAGER.actualizar(raton, this.GE_RATON_PRESIONO, TIEMPO_ACTUALIZACION_RATON_PRESIONADO, itemPuntero,
				mundo);
	}

	public void pintar(final Graphics2D g) {
		if (!this.visible) {
			this.SLOT_MANAGER.pintarSlotsIGU(g);
			return;
		}
		this.pintarInventario(g);
	}

	public void pintarTooltips(final Graphics2D g) {
		if (!this.visible) {
			this.SLOT_MANAGER.pintarTooltipIGU(g);
			return;
		}

		this.SLOT_MANAGER.pintarTooltip(g);

		final Point pMouse = Globales.RATON.getPuntoPosicionEscalado();

		if (this.areaStatsFUE.contains(pMouse)) {
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, TITULO_FUE, DESC_FUE, COLOR_TEXTO_FUE,
					COLOR_DESC_TOOLTIP, FONDO_PANEL_OSCURO);
		} else if (this.areaStatsAGI.contains(pMouse)) {
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, TITULO_AGI, DESC_AGI, COLOR_TEXTO_AGI,
					COLOR_DESC_TOOLTIP, FONDO_PANEL_OSCURO);
		} else if (this.areaStatsINT.contains(pMouse)) {
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, TITULO_INT, DESC_INT, COLOR_TEXTO_INT,
					COLOR_DESC_TOOLTIP, FONDO_PANEL_OSCURO);
		} else if (this.areaStatsDEF.contains(pMouse)) {
			final int frio = (Globales.JUGADOR != null) ? Globales.JUGADOR.getAislamientoFrioTotal() : 0;
			final int calor = (Globales.JUGADOR != null) ? Globales.JUGADOR.getAislamientoCalorTotal() : 0;
			final String descDefConAisla = DESC_DEF + " [Resist. Frío: +" + frio + "°C | Calor: +" + calor + "°C]";
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, TITULO_DEF, descDefConAisla,
					COLOR_TEXTO_DEF, COLOR_DESC_TOOLTIP, FONDO_PANEL_OSCURO);
		} else if (this.areaBilletera.contains(pMouse)) {
			final String totalDesc = "Fondos: " + this.cachedOro + " Oro, " + this.cachedPlata + " Plata.";
			Globales.FUNCIONES.GENERADOR_TOOLTIP.dibujarTooltipConCabecera(g, "Billetera: ", totalDesc, COLOR_TEXTO_ORO,
					COLOR_DESC_TOOLTIP, FONDO_PANEL_OSCURO);
		}
	}

	private void pintarInventario(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.AREA_TOTAL, FONDO_PANEL_OSCURO);
		Render2D.dibujarRectanguloRelleno(g, this.ZONA_INFO_JUGADOR, FONDO_SECCION_HEROE);
		Render2D.dibujarRectanguloRelleno(g, this.ZONA_SLOTS_ALMACEN, FONDO_SLOTS_ALMACEN);
		Render2D.dibujarRectanguloRelleno(g, this.ZONA_SLOTS_PRINCIPALES, FONDO_SLOTS_HOTBAR);

		Render2D.dibujarRectanguloRelleno(g, this.AREA_PERSONAJE, new Color(12, 14, 18));
		Render2D.dibujarRectanguloContorno(g, this.AREA_PERSONAJE, BORDE_INTERIOR_BISEL);

		Render2D.dibujarRectanguloContorno(g, this.X, this.Y, this.ANCHO, this.ALTO, BORDE_EXTERIOR_PANEL);
		Render2D.dibujarRectanguloContorno(g, this.X + 1, this.Y + 1, this.ANCHO - 2, this.ALTO - 2,
				BORDE_INTERIOR_BISEL);

		this.SLOT_MANAGER.pintar(g);

		if ((Globales.JUGADOR != null) && (Animaciones.JUGADOR != null)) {
			final int xAnim = ((this.AREA_PERSONAJE.x + ((this.AREA_PERSONAJE.width - Globales.JUGADOR.getAncho()) / 2))
					- Globales.JUGADOR.getMargenXSprite()) + 1;
			final int yAnim = (this.AREA_PERSONAJE.y - Globales.JUGADOR.getMargenYSprite())
					+ ((this.AREA_PERSONAJE.height - Globales.JUGADOR.getAlto()) / 2) + 1;

			Animaciones.JUGADOR.pintar(g, xAnim, yAnim);
		}

		this.pintarFichaAtributosYBilletera(g);
	}

	private void pintarFichaAtributosYBilletera(final Graphics2D g) {
		if (Globales.JUGADOR == null) {
			return;
		}

		final Font fuentePrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 7f));

		final int str = Globales.JUGADOR.getFuerzaTotal();
		final int agi = Globales.JUGADOR.getAgilidadTotal();
		final int intel = Globales.JUGADOR.getInteligenciaTotal();
		final int def = Globales.JUGADOR.getDefensaTotal();

		final int xStats = this.X + 186;
		final int yBase = this.Y + 7;

		Render2D.dibujarStringConSombra(g, "F: " + str, xStats, yBase, COLOR_TEXTO_FUE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "A: " + agi, xStats, yBase + 5, COLOR_TEXTO_AGI, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "I: " + intel, xStats, yBase + 10, COLOR_TEXTO_INT, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "D: " + def, xStats, yBase + 15, COLOR_TEXTO_DEF, Color.BLACK);

		final long dineroTotal = Globales.JUGADOR.getDineroPlata();
		if (dineroTotal != this.lastDineroPlata) {
			this.lastDineroPlata = dineroTotal;
			this.cachedOro = String.valueOf(dineroTotal / 100L);
			this.cachedPlata = String.valueOf(dineroTotal % 100L);
		}

		final int xDinero = this.X + 213;

		final BufferedImage iconOro = Globales.GESTOR_TEXTURAS.get(TexturaItem.ANILLO_ORO_MAPA);
		if (iconOro != null) {
			Render2D.dibujarImagen(g, iconOro, xDinero, this.Y + 2);
		}
		Render2D.dibujarStringConSombra(g, this.cachedOro, xDinero + 10, this.Y + 9, COLOR_TEXTO_ORO, Color.BLACK);

		final BufferedImage iconPlata = Globales.GESTOR_TEXTURAS.get(TexturaItem.ANILLO_PLATA_MAPA);
		if (iconPlata != null) {
			Render2D.dibujarImagen(g, iconPlata, xDinero, this.Y + 11);
		}
		Render2D.dibujarStringConSombra(g, this.cachedPlata, xDinero + 10, this.Y + 18, COLOR_TEXTO_PLATA, Color.BLACK);

		g.setFont(fuentePrevia);
	}

	public boolean ratonEnAreaInventario() {
		return Globales.RATON.getRectanguloPosicionEscalado().intersects(this.AREA_TOTAL);
	}

	public int contarMunicionTotal(final String codModeloMunicion) {
		return this.SLOT_MANAGER.contarItemGenericoTotal(codModeloMunicion);
	}

	public int extraerMunicion(final String codModeloMunicion, final int cantidadRequerida) {
		return this.SLOT_MANAGER.extraerMunicion(codModeloMunicion, cantidadRequerida);
	}

	public int contarItemGenericoTotal(final String codigoONombre) {
		return this.SLOT_MANAGER.contarItemGenericoTotal(codigoONombre);
	}

	public boolean extraerItemGenerico(final String codigoONombre, final int cantidadRequerida) {
		return this.SLOT_MANAGER.extraerItemGenerico(codigoONombre, cantidadRequerida);
	}

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

	public Slot getSlot(final Point posicion) {
		return this.SLOT_MANAGER.getSlot(posicion);
	}

	public SlotManager getSlotManager() {
		return this.SLOT_MANAGER;
	}

	public Arma equiparArma(final Arma arma) {
		return this.SLOT_MANAGER.equiparArma(arma);
	}

	public Item getArmaEquipada() {
		return this.SLOT_MANAGER.getArmaEquipada();
	}

	public void vaciar() {
		this.SLOT_MANAGER.vaciar();
	}

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