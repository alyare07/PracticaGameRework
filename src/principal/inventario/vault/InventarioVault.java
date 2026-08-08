package principal.inventario.vault;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import principal.controles.Raton;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.inventario.Inventario;
import principal.inventario.equipamiento.SlotArma;
import principal.inventario.equipamiento.SlotManager;
import principal.inventario.slot.Slot;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;

public class InventarioVault {
	private final int LADO_SLOTS = SlotManager.getLadoSlots();
	private final ArrayList<Slot> SLOTS = new ArrayList<Slot>();
	private final Rectangle AREA = new Rectangle();
	private final static int MARGEN = 2;
	private final int MARGEN_PORTADA = 10;
	private final String NOMBRE;
	private Item itemSeleccionado;
	private Point posicionPuntero;
	private final GestorTiempo GT_RATON_PRESIONO;
	private Slot slotOrigenItemSeleccionado;
	private final Cofre COFRE_PROPIETARIO;
	private Slot slotInventarioTerceroSeleccionado;
	/*
	 * HACER QUE EN EL INVENTARIO SE VEA EL NOMBRE DEL COFRE EN EL TITULO. PUEDE SER TIPO TOOLTIP O DIRECTO DE DIBUJODEBUG????
	 */
	
	public InventarioVault(final Cofre propietario,final int cantSlots, final int cantMaxH,final String nombre) {
		this.COFRE_PROPIETARIO = propietario;
		this.crearSlots(cantSlots, cantMaxH);
		this.NOMBRE = nombre;
		this.GT_RATON_PRESIONO = new GestorTiempo();
	}
	
	public void pintar(final Graphics2D g) {
		DibujoDebug.dibujarRectanguloRelleno(g, AREA, Inventario.GRIS_TRANSPARENTE);
		DibujoDebug.dibujarRectanguloContorno(g, AREA, Color.LIGHT_GRAY);
		this.pintarPortada(g);
		this.pintarSlots(g);
		this.pintarItemSeleccionadoEnPuntero(g);
		this.pintarItemSeleccionadoEnPunteroInventarioTercero(g);
	}
	
	public void actualizar(final Raton raton, final Mundo mundo, final boolean visible) {
		this.posicionPuntero = raton.getPuntoPosicionEscalado();
		actualizarSlots(raton);
		actualizarObjetoSeleccionar(raton,this.GT_RATON_PRESIONO,Inventario.TIEMPO_ACTUALIZACION_RATON_PRESIONADO,posicionPuntero,mundo);
		this.intercambioInventarioExterno(raton);
	}
	
	public void cerrar() {
		this.COFRE_PROPIETARIO.cerrar();
	}
	
	public ArrayList<Item> getItems(){
		final ArrayList<Item> ITEMS = new ArrayList<Item>();
		for(Slot slot : this.SLOTS) {
			if(slot.contieneItem()) ITEMS.add(slot.getItem());
		}
		return ITEMS;
	}
	
	public void deseleccionarSlots() {
		if (this.itemSeleccionado != null) {
			this.itemSeleccionado = null;
		}
		if (this.slotOrigenItemSeleccionado != null) {
			this.slotOrigenItemSeleccionado = null;
		}
	}
	
	public boolean agregarItem(final Item item) {
		switch (item.getTipoItem()) {
		case Item.COD_ITEM_CONSUMIBLE:
			return this.agregarConsumible((Consumible) item);
		case Item.COD_ITEM_PORTABLE:
			return this.agregarPortable((Portable) item);
		default:
			return false;
		}

	}
	
	
	
	public void vaciar() {
		for (Slot slot : this.SLOTS) {
			slot.establecerObjeto(null);
		}
	}
	
	
	public void deseleccionarSlot() {
		this.slotOrigenItemSeleccionado = null;
		this.slotInventarioTerceroSeleccionado = null;
	}
	
	public Slot getSlotOrigenItemSeleccionado() {
		return this.slotOrigenItemSeleccionado;
	}
	
	
	public Slot getSlotItemInventarioTerceroSeleccionado() {
		return this.slotInventarioTerceroSeleccionado;
	}
	
	public void setSlotItemInventarioTerceroSeleccionado(final Slot slot) {
		 this.slotInventarioTerceroSeleccionado = slot;
	}
	
	public Rectangle getArea() {
		return this.AREA;
	}
	
	public GestorTiempo getGestorTiempo() {
		return this.GT_RATON_PRESIONO;
	}
	
	public void setSlotOrigenItemSeleccionado(final Slot slot) {
		this.slotOrigenItemSeleccionado = slot;
	}
	
	public boolean intersectaArea(final Rectangle r) {
		return r.intersects(this.AREA);
	}
	
	public void intercambioInventarioExterno(final Raton raton) {
		Inventario invExt = Constantes.INVENTARIO;
		final Point posRaton = raton.getPuntoPosicionEscalado();
		if(raton.getRectanguloPosicionEscalado().intersects(Constantes.INVENTARIO.getArea())) {
			if(this.slotOrigenItemSeleccionado != null && this.slotOrigenItemSeleccionado.contieneItem()) {
				if(invExt.getSlotInventarioTerceroSeleccionado()!=this.slotOrigenItemSeleccionado ) {
					invExt.setSlotInventarioTerceroSeleccionado(slotOrigenItemSeleccionado);
				}
				if(raton.presionadoClickIzq() && this.GT_RATON_PRESIONO.transcurrioMiliSegundos(Inventario.TIEMPO_ACTUALIZACION_RATON_PRESIONADO)) {
//					System.out.println("click dentro del area invExt");
					this.GT_RATON_PRESIONO.establecerReferenciaTiempoActual();
					invExt.getGestorTiempoRaton().establecerReferenciaTiempoActual();
					if(invExt.getSlotOrigenItemSeleccionado()!=null) {
						if(invExt.getSlotOrigenItemSeleccionado() instanceof SlotArma) {
							if(!(this.slotOrigenItemSeleccionado.getItem() instanceof Arma)) {
								this.deseleccionarSlot();
								invExt.deseleccionar();
								return;
							}
							
						}
//						System.out.println("CASO A1");
						if(invExt.getSlotOrigenItemSeleccionado().contieneItem()) {
//							System.out.println("CASO A1.1");
							Item aux = invExt.getSlotOrigenItemSeleccionado().getItem();
							invExt.getSlotOrigenItemSeleccionado().establecerObjeto(this.slotOrigenItemSeleccionado.getItem());
							this.slotOrigenItemSeleccionado.establecerObjeto(aux);
							this.deseleccionarSlot();
							invExt.deseleccionar();
						}else {
//							System.out.println("CASO A1.2 : "+ this.getSlotOrigenItemSeleccionado().getItem().getNombre());
							invExt.getSlotOrigenItemSeleccionado().establecerObjeto(this.slotOrigenItemSeleccionado.getItem());
							this.slotOrigenItemSeleccionado.eliminarObjeto();
							this.deseleccionarSlot();
							invExt.deseleccionar();
						}
					}else {
//						System.out.println("CASO A2");
						this.deseleccionarSlot();
						invExt.deseleccionar();
					}
				}
			}
		}else {
			Slot apuntado = this.getSlot(posRaton);
			if(raton.getRectanguloPosicionEscalado().intersects(this.AREA) && apuntado!=null && apuntado.contieneItem() && apuntado.getItem() instanceof Arma && invExt.getArmaEquipada() instanceof Desarmado && raton.presionadoClickDer()) {
				invExt.equiparArma((Arma)apuntado.getItem());
				apuntado.eliminarObjeto();
				this.deseleccionarSlot();
				invExt.deseleccionar();
			}
		}
	}
	
	private void pintarSlots(final Graphics2D g) {
		final float tamaFuente = g.getFont().getSize();
		g.setFont(g.getFont().deriveFont(6f));
		Slot apuntado = null;
		
		for(Slot  slot : this.SLOTS) {
			slot.pintar(g);
			if (slot.estaApuntado() && apuntado == null) {
				apuntado = slot;
			}
		}
		
		g.setFont(g.getFont().deriveFont(tamaFuente));
		if (apuntado != null && apuntado.contieneItem()) {
			apuntado.pintarTooltip(g);
		}
	}
	
	private void pintarItemSeleccionadoEnPunteroInventarioTercero(final Graphics2D g) {
		if(this.slotInventarioTerceroSeleccionado!=null && this.slotInventarioTerceroSeleccionado.contieneItem()) {
			DibujoDebug.dibujarImagen(g, this.slotInventarioTerceroSeleccionado.getItem().getTextura(), posicionPuntero);
		}
	}
	
	
	private void pintarItemSeleccionadoEnPuntero(final Graphics2D g) {
		if (this.itemSeleccionado == null || this.slotOrigenItemSeleccionado == null || this.posicionPuntero == null) {
			return;
		}
		DibujoDebug.dibujarImagen(g, this.itemSeleccionado.getTextura(), posicionPuntero);
	}
	
	
	
	private void actualizarObjetoSeleccionar(final Raton raton, final GestorTiempo gtRatonPresiono, final int tiempoMsRatonPresiono, final Point posicionPuntero, final Mundo mundo) {

		if ((this.getItemSeleccionado() == null || this.getSlotOrigenItemSeleccionado() == null) && raton.presionadoClickIzq() // verificamos si se selecciona un item
				&& gtRatonPresiono.transcurrioMiliSegundos(tiempoMsRatonPresiono)) {
			for (Slot slot : this.SLOTS) {
				if (slot.ratonIntersecta(raton)) {
					
					this.setItemSeleccionado(slot.getItem());
					this.setSlotOrigenItemSeleccionado(slot);
					gtRatonPresiono.establecerReferenciaTiempoActual();
					break;
				}
			}
			return;
		}
		
		 // si ya previamente existia un item seleccionado entonces verificamos donde se vuelve a hacer click para poder cambiar de lugar el item  o soltarlo en el mapa
		
		if(raton.getRectanguloPosicionEscalado().intersects(Constantes.INVENTARIO.getArea())) {
			return;
		}
		
		if (itemSeleccionado!=null && raton.presionadoClickIzq() && gtRatonPresiono.transcurrioMiliSegundos(tiempoMsRatonPresiono)) {
			gtRatonPresiono.establecerReferenciaTiempoActual();
			final Slot slot = getSlot(posicionPuntero);
			if (slot == null || slot == this.getSlotOrigenItemSeleccionado()) {
				// si se selecciona fuera del inventario se suelta el item en el mapa
//				if (slot == null && (!this.AREA.intersects(new Rectangle(posicionPuntero.x, posicionPuntero.y, 1, 1)))) { 
//					if (mundo == null) {
//						System.out.println("mapa nulo para inventario...");
//						return;
//					}
//					mundo.agregarItemEnPosicionJugador(this.getItemSeleccionado(), false);
//					this.getSlotOrigenItemSeleccionado().establecerObjeto(null);
//
//				}
//				this.setItemSeleccionado(null);
//				this.setSlotOrigenItemSeleccionado(null);
				return;
			} else {
				
				if (slot.contieneItem()) {
					
					Item aux = slot.getItem();
					slot.establecerObjeto(this.getItemSeleccionado());
					this.getSlotOrigenItemSeleccionado() .establecerObjeto(aux);
					this.setItemSeleccionado(null);
					this.setSlotOrigenItemSeleccionado(null);
				} else {
					slot.establecerObjeto(this.getItemSeleccionado());
					this.getSlotOrigenItemSeleccionado() .establecerObjeto(null);
					this.setItemSeleccionado(null);
					this.setSlotOrigenItemSeleccionado(null);
				}

				return;
			}
		}
	}
	
	private Item getItemSeleccionado() {
		return this.itemSeleccionado;
	}
	
	private void setItemSeleccionado(final Item item) {
		this.itemSeleccionado = item;
	}
	
	
	
	public Slot getSlot(final Point posicion) {

		for (Slot slot : this.SLOTS) {
			if (slot.intersecta(posicion)) {
				return slot;
			}
		}
		return null;
	}
	
	private void actualizarSlots(final Raton raton) {
		for (Slot slot : this.SLOTS) {
			slot.actualizar(raton);
		}
		
	}
	
	private boolean agregarPortable(final Portable item) {
		for (Slot slot : this.SLOTS) {
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
		for (Slot slot : this.SLOTS) {
			if (slot.contieneItem()) {
				if (slot.getItem().getTipoItem() == Item.COD_ITEM_CONSUMIBLE) {
					cons = (Consumible) slot.getItem();
					if (cons.getCodigoModelo() == item.getCodigoModelo()) {
						item.establecerCantidad(cons.agregarCantidad(item.getCantidad()));
						if (item.getCantidad() > 0) {
							continue;
						} else {
							return true;
						}
					}
				}

			} else {
				if (slotVacio == null) {
					slotVacio = slot;
				}
			}
		}
		if (slotVacio != null) {
			slotVacio.establecerObjeto((Consumible) item.copiar());
			item.establecerCantidad(0);
			return true;
		}
		return false;
	}
	private void pintarPortada(final Graphics2D g) {
		Rectangle areaPortada = new Rectangle(this.AREA.x, this.AREA.y, this.AREA.width, this.MARGEN_PORTADA);
		g.setFont(g.getFont().deriveFont(8f));
		int anchoNombre, altoNombre;
		anchoNombre = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, NOMBRE);
		altoNombre = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, NOMBRE);
		int xNombre = areaPortada.x + (areaPortada.width)/2 - (anchoNombre)/2;
		int yNombre = areaPortada.y + (areaPortada.height)/2 + (altoNombre)/2;
		
		DibujoDebug.dibujarString(g, NOMBRE, xNombre, yNombre, Color.black);
		
	}
	
	private void crearSlots(final int cant, final int cantMaxH) {
		int ancho = (cantMaxH*LADO_SLOTS) + (cantMaxH*MARGEN) +MARGEN;
		float cantFilasFloat= (float)cant/(float)cantMaxH;
		int cantFilas = cantFilasFloat > (int)cantFilasFloat? (int)cantFilasFloat +1 : (int)cantFilasFloat;
		
		int alto = MARGEN + (cantFilas * LADO_SLOTS) + (MARGEN*cantFilas);
		int x = Constantes.CENTROX - ancho / 2;
		int y = Constantes.CENTROY - alto - (MARGEN*3) - MARGEN_PORTADA;
		this.AREA.x = x;
		this.AREA.y = y;
		this.AREA.width = ancho;
		this.AREA.height = alto + MARGEN_PORTADA;
		
		int cantSlot = 0;
		
		for(int y2 = y + MARGEN + MARGEN_PORTADA; y2 < (y+alto) ; y2+=LADO_SLOTS+MARGEN) {
			for(int x2 = x + MARGEN; x2 < (x+ancho); x2+=LADO_SLOTS+MARGEN) {
				if(!(cantSlot < cant)) {
					break;
				}
				this.SLOTS.add(new Slot(new Rectangle(x2, y2, LADO_SLOTS, LADO_SLOTS)));
				cantSlot++;
			}
		}
	}
			
}
