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
import principal.inventario.equipamiento.SlotArma;
import principal.inventario.equipamiento.SlotManager;
import principal.inventario.slot.Slot;
import principal.inventario.slot.SlotArrojadizo;
import principal.inventario.vault.InventarioVault;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;

public class Inventario{
    private final int X;
    private final int Y;
    private final int ANCHO;
    private final int ALTO;
    private final Rectangle ZONA_INFO_JUGADOR;
    private final Rectangle ZONA_SLOTS_ALMACEN;
    private final Rectangle ZONA_SLOTS_PRINCIPALES;
    private final Rectangle ZONA_SLOTS_EQUIPAMIENTOS;
    private final Rectangle AREA_PERSONAJE;
    private final int MARGEN_GENERAL;
    private boolean visible;
    private Item itemSeleccionado;
    private Slot slotOrigenItemSeleccionado;
    private Point posicionPuntero;
    private Mundo mundo;
    private final GestorTiempo GE_RATON_PRESIONO;
    private boolean activarItemDisponible;
    private Slot slotItemInventarioTerceroSeleccionado;
    public static final int TIEMPO_ACTUALIZACION_RATON_PRESIONADO = 500;
    private final SlotManager SLOT_MANAGER;
    public static final Color GRIS_TRANSPARENTE = new Color(80, 53, 67, 150);
    public static final Color NEGRO_TRANSPARENTE = new Color(43, 24, 34, 80);
    public static final Color BLANCO_TRANSPARENTE = new Color(255, 255, 255, 100);
    private final SlotArrojadizo SLOT_ARROJADIZO = new SlotArrojadizo();

    public Inventario() {

	this.ANCHO = 202;
	this.ALTO = 110;
	this.X = Constantes.CENTROX - this.ANCHO / 2;
//		this.Y = Constantes.CENTROY - ALTO / 2;
	this.Y = Constantes.CENTROY;

	this.MARGEN_GENERAL = 2;
	this.ZONA_INFO_JUGADOR = new Rectangle(this.X, this.Y, this.ANCHO, 25);

	this.ZONA_SLOTS_EQUIPAMIENTOS = new Rectangle(this.ZONA_INFO_JUGADOR.x + 25, this.Y + 1, this.ANCHO - 24, 18);

	this.ZONA_SLOTS_ALMACEN = new Rectangle(this.X, this.ZONA_INFO_JUGADOR.y + this.ZONA_INFO_JUGADOR.height, this.ANCHO, 62); // this.Y + 20;
	this.ZONA_SLOTS_PRINCIPALES = new Rectangle(this.X, this.ZONA_SLOTS_ALMACEN.y + this.ZONA_SLOTS_ALMACEN.height, this.ANCHO, 22);// this.Y + 82;
	this.AREA_PERSONAJE = new Rectangle(this.X + 2, this.Y + 2, 22, 22);

	this.SLOT_MANAGER = new SlotManager(this, this.MARGEN_GENERAL, this.ZONA_SLOTS_ALMACEN, this.ZONA_SLOTS_PRINCIPALES, this.ZONA_SLOTS_EQUIPAMIENTOS);

//		this.GRIS_TRANSPARENTE = new Color(80, 53, 67, 200);
//		this.NEGRO_TRANSPARENTE = new Color(43, 24, 34, 200);
	this.GE_RATON_PRESIONO = new GestorTiempo();
	this.activarItemDisponible = true;
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

    public void pintar(final Graphics2D g) {
	if (!this.visible) {
	    this.SLOT_MANAGER.pintarSlotsIGU(g);
	    return;
	}
	this.pintarInventario(g);
	this.pintarItemSeleccionadoEnPuntero(g);
	this.pintarItemSeleccionadoEnPunteroInventarioTercero(g);
    }

    private void pintarInventario(final Graphics2D g) {
	DibujoDebug.dibujarRectanguloRelleno(g, this.ZONA_INFO_JUGADOR, GRIS_TRANSPARENTE);
	DibujoDebug.dibujarRectanguloRelleno(g, this.ZONA_SLOTS_ALMACEN, GRIS_TRANSPARENTE);
	DibujoDebug.dibujarRectanguloRelleno(g, this.ZONA_SLOTS_PRINCIPALES, NEGRO_TRANSPARENTE);
	DibujoDebug.dibujarRectanguloRelleno(g, this.AREA_PERSONAJE, BLANCO_TRANSPARENTE);
	DibujoDebug.dibujarRectanguloContorno(g, this.X, this.Y, this.ANCHO, this.ALTO, Color.lightGray);
	this.SLOT_MANAGER.pintar(g);
	Animaciones.JUGADOR.pintar(g, (this.AREA_PERSONAJE.x + (this.AREA_PERSONAJE.width - Constantes.JUGADOR.getAncho()) / 2 - Constantes.JUGADOR.getMargenXSprite()),
		this.AREA_PERSONAJE.y - Constantes.JUGADOR.getMargenYSprite() + (this.AREA_PERSONAJE.height - Constantes.JUGADOR.getAlto()) / 2);
    }

    private void pintarItemSeleccionadoEnPunteroInventarioTercero(final Graphics2D g) {
	if (this.slotItemInventarioTerceroSeleccionado != null && this.slotItemInventarioTerceroSeleccionado.contieneItem()) {
	    DibujoDebug.dibujarImagen(g, this.slotItemInventarioTerceroSeleccionado.getItem().getTextura(), this.posicionPuntero);
	}
    }

    private void pintarItemSeleccionadoEnPuntero(final Graphics2D g) {
	if (this.itemSeleccionado == null || this.slotOrigenItemSeleccionado == null || this.posicionPuntero == null) {
	    return;
	}
	DibujoDebug.dibujarImagen(g, this.itemSeleccionado.getTextura(), this.posicionPuntero);
    }

    public void actualizar(final Raton raton) {
	if (!this.visible) {
	    if (this.itemSeleccionado != null) {
		this.itemSeleccionado = null;
	    }
	    if (this.slotOrigenItemSeleccionado != null) {
		this.slotOrigenItemSeleccionado = null;
	    }
	    this.SLOT_MANAGER.actualizarIGU(raton);
	    this.SLOT_ARROJADIZO.actualizar(raton);
	    return;
	}
	this.SLOT_ARROJADIZO.actualizar(raton);
	this.posicionPuntero = raton.getPuntoPosicionEscalado();
	this.SLOT_MANAGER.actualizar(raton, this.GE_RATON_PRESIONO, TIEMPO_ACTUALIZACION_RATON_PRESIONADO, this.posicionPuntero, this.mundo);
	this.intercambioInventarioExterno(raton);
    }

    public boolean agregarObjeto(final Item item) {
	switch (item.getTipoItem()) {
	case Item.COD_ITEM_CONSUMIBLE:
	    return this.SLOT_MANAGER.agregarConsumible((Consumible) item);
	case Item.COD_ITEM_PORTABLE:
	    return this.SLOT_MANAGER.agregarPortable((Portable) item);
	default:
	    return false;
	}

    }

    private void intercambioInventarioExterno(final Raton raton) {
	if (Constantes.GLOBALES.viendoCofre && raton.getRectanguloPosicionEscalado().intersects(Constantes.GLOBALES.inventarioVault.getArea())) {
	    final InventarioVault invExt = Constantes.GLOBALES.inventarioVault;
	    if (this.slotOrigenItemSeleccionado != null && this.slotOrigenItemSeleccionado.contieneItem()) {
		if (invExt.getSlotItemInventarioTerceroSeleccionado() != this.slotOrigenItemSeleccionado) {
		    invExt.setSlotItemInventarioTerceroSeleccionado(this.slotOrigenItemSeleccionado);
		}
		if (raton.presionadoClickIzq() && this.GE_RATON_PRESIONO.transcurrioMiliSegundos(TIEMPO_ACTUALIZACION_RATON_PRESIONADO)) {
//					System.out.println("click dentro del area invExt2");
		    this.GE_RATON_PRESIONO.establecerReferenciaTiempoActual();
		    invExt.getGestorTiempo().establecerReferenciaTiempoActual();

		    final Slot slotApuntadoInvExt = invExt.getSlot(raton.getPuntoPosicionEscalado());

		    if (this.slotOrigenItemSeleccionado instanceof SlotArma) {
			if (!((slotApuntadoInvExt != null && slotApuntadoInvExt.contieneItem() && slotApuntadoInvExt.getItem() instanceof Arma)
				|| (slotApuntadoInvExt != null && !slotApuntadoInvExt.contieneItem()))) {
			    this.deseleccionar();
			    invExt.deseleccionarSlot();
			    return;
			}
		    }

		    if (slotApuntadoInvExt != null) {
//						System.out.println("CASO B1");
			if (slotApuntadoInvExt.contieneItem()) {
//							System.out.println("CASO B1.1");
			    final Item aux = slotApuntadoInvExt.getItem();
			    slotApuntadoInvExt.establecerObjeto(this.slotOrigenItemSeleccionado.getItem());
			    this.slotOrigenItemSeleccionado.establecerObjeto(aux);
			    invExt.setSlotItemInventarioTerceroSeleccionado(null);
			    this.deseleccionar();
			    invExt.deseleccionarSlots();
			} else {
//							System.out.println("CASO B1.2 : "+ this.getSlotOrigenItemSeleccionado().getItem().getNombre());
			    slotApuntadoInvExt.establecerObjeto(this.slotOrigenItemSeleccionado.getItem());
			    this.slotOrigenItemSeleccionado.eliminarObjeto();
			    this.deseleccionar();
			    invExt.deseleccionarSlots();
			}
		    } else {
			this.deseleccionar();
			invExt.deseleccionarSlot();
		    }

		}
	    }
	}

	if (Constantes.GLOBALES.viendoCofre) {
	    if (Constantes.GLOBALES.inventarioVault.getSlotOrigenItemSeleccionado() == null && this.slotItemInventarioTerceroSeleccionado != null) {
		this.slotItemInventarioTerceroSeleccionado = null;
	    }
	    if (this.slotOrigenItemSeleccionado == null && Constantes.GLOBALES.inventarioVault.getSlotItemInventarioTerceroSeleccionado() != null) {
		Constantes.GLOBALES.inventarioVault.deseleccionarSlot();
	    }
	}
    }

    public SlotArrojadizo getSlotArrojadizo() {
	return this.SLOT_ARROJADIZO;
    }

    public Rectangle getArea() {
	return new Rectangle(this.X, this.Y, this.ANCHO, this.ALTO);
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

    public void deseleccionar() {
	this.slotOrigenItemSeleccionado = null;
	this.itemSeleccionado = null;
	this.slotItemInventarioTerceroSeleccionado = null;
    }

    public Slot getSlotOrigenItemSeleccionado() {
	return this.slotOrigenItemSeleccionado;
    }

    public void setSlotOrigenItemSeleccionado(final Slot slot) {
	this.slotOrigenItemSeleccionado = slot;
    }

    public Item getItemSeleccionado() {
	return this.itemSeleccionado;
    }

    public void setItemSeleccionado(final Item item) {
	this.itemSeleccionado = item;
    }

    public Slot getSlotInventarioTerceroSeleccionado() {
	return this.slotItemInventarioTerceroSeleccionado;
    }

    public GestorTiempo getGestorTiempoRaton() {
	return this.GE_RATON_PRESIONO;
    }

    public void setSlotInventarioTerceroSeleccionado(final Slot s) {
	this.slotItemInventarioTerceroSeleccionado = s;
    }

    public void vaciar() {
	this.SLOT_MANAGER.vaciar();
    }

    public Item getArmaEquipada() {

	return this.SLOT_MANAGER.getArmaEquipada();

    }

    public Arma equiparArma(final Arma arma) {
	return this.SLOT_MANAGER.equiparArma(arma);
    }

    public void setActivarItemDisponible(final boolean activarItemDisponible) {
	this.activarItemDisponible = activarItemDisponible;
    }

    public boolean getActivarItemDisponible() {
	return this.activarItemDisponible;
    }

}
