package principal.inventario.equipamiento;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.inventario.CajaInfo;
import principal.inventario.Inventario;
import principal.inventario.slot.Slot;
import principal.inventario.slot.SlotIGU;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;

public class SlotManager {
	private final Inventario INVENTARIO;
	
	private final static int LADO_SLOTS = 18;
	private final ArrayList<Slot> LISTA_SLOTS = new ArrayList<Slot>();
	private final ArrayList<SlotIGU> LISTA_SLOTS_IGU = new ArrayList<SlotIGU>();
	private final ArrayList<Slot> LISTA_SLOTS_GENERAL = new ArrayList<Slot>();
	private final ArrayList<Slot> LISTA_SLOTS_ALMACEN = new ArrayList<Slot>();
	private final ArrayList<Slot> LISTA_SLOTS_PRINCIPALES = new ArrayList<Slot>();
	private final ArrayList<Slot> LISTA_SLOTS_EQUIPAMIENTO = new ArrayList<Slot>();
	private final Rectangle ZONA_SLOTS_ALMACEN;
	private final Rectangle ZONA_SLOTS_PRINCIPALES;
	private final Rectangle ZONA_SLOTS_EQUIPAMIENTOS;
	private final int MARGEN_GENERAL;
	private SlotArma slotArma;
	private  CajaInfo infoArma;
	
	public SlotManager(final Inventario inventario, final int margenGeneral,Rectangle zonaSlotAlmacen, final Rectangle zonaSlotPrincipales, final Rectangle zonaSlotEquipamiento) {
		this.INVENTARIO = inventario;
		this.ZONA_SLOTS_EQUIPAMIENTOS = zonaSlotEquipamiento;
		this.ZONA_SLOTS_ALMACEN = zonaSlotAlmacen;
		this.ZONA_SLOTS_PRINCIPALES = zonaSlotPrincipales;
		this.MARGEN_GENERAL = margenGeneral;
		this.infoArma = new CajaInfo(new Rectangle(this.ZONA_SLOTS_EQUIPAMIENTOS.x+18+this.MARGEN_GENERAL,ZONA_SLOTS_EQUIPAMIENTOS.y,18,18));
		
		
		this.llenarSlotsPrincipales();
		this.llenarSlotsEquipamientos();
		this.llenarSlotsAlmacenamiento();
		this.llenarSlotsIGU();
	}
	
	
	public void actualizar(final Raton raton, final GestorTiempo gtRatonPresiono, final int tiempoMsRatonPresiono, final Point posicionPuntero, final Mundo mundo) {
		actualizarSlots(raton);
		actualizarObjetoSeleccionar(raton,gtRatonPresiono,tiempoMsRatonPresiono,posicionPuntero,mundo);
		actualizarActivarItem(raton);
	}
	
	public void actualizarIGU(final Raton raton) {
		this.actualizarSlotsIGU(raton);
		this.actualizarActivarItemIGU(raton);
	}
	
	public void pintar(final Graphics2D g) {
		this.pintarSlots(g);
	}
	
	
	public boolean agregarPortable(final Portable item) {
		for (Slot slot : LISTA_SLOTS) {
			if (!slot.contieneItem()) {
				slot.establecerObjeto((Portable) item.copiar());
				return true;
			}
		}
		return false;
	}

	public boolean agregarConsumible(final Consumible item) {
		Slot slotVacio = null;
		Consumible cons = null;
		for (Slot slot : LISTA_SLOTS) {
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
	
	public void vaciar() {
		for (Slot slot : LISTA_SLOTS_GENERAL) {
			slot.establecerObjeto(null);
		}
	}
	
	
	public Item getArmaEquipada() {
		if(this.slotArma.getItem()!=null) {
			return this.slotArma.getItem();
		}else {
			return new Desarmado();
		}
	}
	
	public Arma equiparArma(final Arma arma) {
		final Arma aux =  this.slotArma.getItem() != null? (Arma)this.slotArma.getItem(): new Desarmado();
		this.slotArma.establecerObjeto(arma);
		return aux;
	}
	
	private void actualizarSlots(final Raton raton) {
		for (Slot slot : this.LISTA_SLOTS_GENERAL) {
			slot.actualizar(raton);
		}
		
	}
	
	private void actualizarSlotsIGU(final Raton raton) {
		for (SlotIGU slotIGU : this.LISTA_SLOTS_IGU) {
			slotIGU.actualizar(raton);
		}
	}
	
	private void actualizarActivarItem(final Raton raton) {
		if(raton.presionadoClickDerUnicaAct() && this.INVENTARIO.getActivarItemDisponible()) {
			for (Slot slot : this.LISTA_SLOTS_GENERAL) {
				if(slot.ratonIntersecta(raton)) {
					if(slot.contieneItem()) {
						Item i = slot.getItem();
						if(i instanceof Arrojadizo) {
							this.INVENTARIO.getSlotArrojadizo().establecerObjeto(i);
							System.out.println("arrojar activado");
							Constantes.INVENTARIO.invertirVisibilidad();
							return;
						}else if(i instanceof Arma) {
							final Item itemAux = slotArma.getItem();
							slotArma.establecerObjeto(i);
							slot.establecerObjeto(itemAux);
							
							/*
							 * HACER QUE EL ARMA SI YA ESTABA EQUIPADA SE DESEQUIPE Y PRIORICE PRIMERO IR A UN SLOT DE EQUIPAMIENTO PRINCIPAL LIBRE SINO A UNO DE ALMACENAMIENTO. SI EL INVENTARIO
							 * ESTA LLENO QUE NO REALICE ALGUNA ACCION.
							 * (UNA BUENA IDEA SERIA QUE EN AJUSTES EL USUARIO PUEDA HABILITAR EL SOLTAR ARMA AL HACER CLICK DERECHO EN UN ARMA EQUIPADA CON EL INVENTARIO LLENO)
							 */
							break;
						}else if(i instanceof Consumible) {
							Consumible c = (Consumible) i;
							c.consumir(Constantes.JUGADOR);
							this.INVENTARIO.setActivarItemDisponible(false);
							break;
						}
					}
					break;
				}
			}
			

		}else if(!this.INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDer()) {
			this.INVENTARIO.setActivarItemDisponible(true);
		}
		
	}
	
	private void actualizarActivarItemIGU(final Raton raton) {
		if(raton.presionadoClickDerUnicaAct() && INVENTARIO.getActivarItemDisponible()) {
			for (SlotIGU slot : this.LISTA_SLOTS_IGU) {
				if(slot.apuntado()) {
					if(slot.contieneItem()) {
						Item i = slot.getItem();
						INVENTARIO.setActivarItemDisponible(false);
						if(i instanceof Arrojadizo) {
							this.INVENTARIO.getSlotArrojadizo().establecerObjeto(i);
							return;
						}else if(i instanceof Arma) {
							final Item itemAux = slotArma.getItem();
							slotArma.establecerObjeto(i);
							slot.establecerObjeto(itemAux);
							break;
						}else if(i instanceof Consumible) {
							Consumible c = (Consumible) i;
							c.consumir(Constantes.JUGADOR);
							break;
						}
					}
					break;
				}
			}
		}else if(!INVENTARIO.getActivarItemDisponible() && !raton.presionadoClickDerUnicaAct()) {
			INVENTARIO.setActivarItemDisponible(true);
		}
		
	}
	
	
	private void actualizarObjetoSeleccionar(final Raton raton, final GestorTiempo gtRatonPresiono, final int tiempoMsRatonPresiono, final Point posicionPuntero, final Mundo mundo) {

		if ((INVENTARIO.getItemSeleccionado() == null || this.INVENTARIO.getSlotOrigenItemSeleccionado() == null) && raton.presionadoClickIzq() // verificamos si se selecciona un item
				&& gtRatonPresiono.transcurrioMiliSegundos(tiempoMsRatonPresiono)) {
			for (Slot slot : this.LISTA_SLOTS_GENERAL) {
				if (slot.ratonIntersecta(raton)) {
					
					this.INVENTARIO.setItemSeleccionado(slot.getItem());
					this.INVENTARIO.setSlotOrigenItemSeleccionado(slot);
					gtRatonPresiono.establecerReferenciaTiempoActual();
					break;
				}
			}
			return;
		}
		
		if(Constantes.GLOBALES.viendoCofre && Constantes.GLOBALES.inventarioVault.intersectaArea(raton.getRectanguloPosicionEscalado())) {
			return;
		}
		
		 // si ya previamente existia un item seleccionado entonces verificamos donde se vuelve a hacer click para poder cambiar de lugar el item  o soltarlo en el mapa

		if (INVENTARIO.getItemSeleccionado()!=null && raton.presionadoClickIzq() && gtRatonPresiono.transcurrioMiliSegundos(tiempoMsRatonPresiono)) {
			gtRatonPresiono.establecerReferenciaTiempoActual();
			final Slot slot = getSlot(posicionPuntero);
			if (slot == null || slot == this.INVENTARIO.getSlotOrigenItemSeleccionado()) {
				// si se selecciona fuera del inventario se suelta el item en el mapa
				if (slot == null && (!this.INVENTARIO.getArea().intersects(new Rectangle(posicionPuntero.x, posicionPuntero.y, 1, 1)))) { 
					if (mundo == null) {
						System.out.println("mapa nulo para inventario...");
						return;
					}
					mundo.agregarItemEnPosicionJugador(INVENTARIO.getItemSeleccionado(), false);
					this.INVENTARIO.getSlotOrigenItemSeleccionado().eliminarObjeto();
				}
				this.INVENTARIO.setItemSeleccionado(null);
				this.INVENTARIO.setSlotOrigenItemSeleccionado(null);
				return;
			} else {
				
				//primero verifico si el slot al que se quiere colocar el item es de tipo equipamiento o no
				if(slot instanceof SlotEquipamiento) {
					final SlotEquipamiento se = (SlotEquipamiento) slot;
					if(!se.validarAdmisionItem(INVENTARIO.getItemSeleccionado())) {
						System.out.println("no valido: "+ INVENTARIO.getItemSeleccionado().getClass());
						return; // el slot de equipamiento no admite el tipo de item que se desea colocar!
					}

				}
				
				if (slot.contieneItem()) {
					
					Item aux = slot.getItem();
					slot.establecerObjeto(INVENTARIO.getItemSeleccionado());
					INVENTARIO.getSlotOrigenItemSeleccionado() .establecerObjeto(aux);
					INVENTARIO.setItemSeleccionado(null);
					INVENTARIO.setSlotOrigenItemSeleccionado(null);
				} else {
					slot.establecerObjeto(INVENTARIO.getItemSeleccionado());
					INVENTARIO.getSlotOrigenItemSeleccionado() .establecerObjeto(null);
					INVENTARIO.setItemSeleccionado(null);
					INVENTARIO.setSlotOrigenItemSeleccionado(null);
				}

				return;
			}
		}
		
		
		
	}
	
	
	private Slot getSlot(final Point posicion) {

		for (Slot slot : this.LISTA_SLOTS_GENERAL) {
			if (slot.intersecta(posicion)) {
				return slot;
			}
		}
		return null;
	}
	
	
	/*
	 * SACAR LSO TRES BUCLES SEPARADOS Y CREAR UN BUCLE DENTRO DE OTRO QUE RECORRA TANTO Y COMO X (BUCLE DOBLE)
	 */
	private void llenarSlotsAlmacenamiento() {
		int x = INVENTARIO.getX();
		final int lado = LADO_SLOTS;
		final int cantidadSlotFila = 10;
		Slot slotAux = null;
		Rectangle area = null;
			//25 ; y < 55 ; y+= 10
		int y = this.ZONA_SLOTS_ALMACEN.y + MARGEN_GENERAL;
		int limiteY = (y + (3*(lado + MARGEN_GENERAL) ) );
		for( ; y < limiteY ; y+= lado + MARGEN_GENERAL){
			for (int i = 0; i < cantidadSlotFila; i++) {
				
				x += MARGEN_GENERAL;
				area = new Rectangle(x, y, lado, lado);
				slotAux = new Slot(area);
				this.LISTA_SLOTS.add(slotAux);
				this.LISTA_SLOTS_ALMACEN.add(slotAux);
				this.LISTA_SLOTS_GENERAL.add(slotAux);
				x += lado;
			}
			x = INVENTARIO.getX();
		}
		
//		y += lado + MARGEN_GENERAL;
//		for (int i = 0; i < cantidadSlotFila; i++) {
//			x += MARGEN_GENERAL;
//			area = new Rectangle(x, y, lado, lado);
//			slotAux = new Slot(area);
//			this.LISTA_SLOTS.add(slotAux);
//			this.LISTA_SLOTS_ALMACEN.add(slotAux);
//			this.LISTA_SLOTS_GENERAL.add(slotAux);
//			x += lado;
//		}
//
//		x = INVENTARIO.getX();
//		y += lado + MARGEN_GENERAL;
//		for (int i = 0; i < cantidadSlotFila; i++) {
//			x += MARGEN_GENERAL;
//			area = new Rectangle(x, y, lado, lado);
//			slotAux = new Slot(area);
//			this.LISTA_SLOTS.add(slotAux);
//			this.LISTA_SLOTS_ALMACEN.add(slotAux);
//			this.LISTA_SLOTS_GENERAL.add(slotAux);
//			x += lado;
//		}

	}
	
	private void llenarSlotsPrincipales() {
		int x = INVENTARIO.getX();
		int y = this.ZONA_SLOTS_PRINCIPALES.y + MARGEN_GENERAL;
		final int lado = LADO_SLOTS;
		final int cantidadSlotFila = 10;
		Slot slot = null;
		 Rectangle area = null;
		for (int i = 0; i < cantidadSlotFila; i++) {
			x += MARGEN_GENERAL;
			area = new Rectangle(x, y, lado, lado);
			slot = new Slot(area);
			this.LISTA_SLOTS.add(slot);
			this.LISTA_SLOTS_PRINCIPALES.add(slot);
			this.LISTA_SLOTS_GENERAL.add(slot);
			x += lado;
			//SLOTS IGU DE PRINCIPALES
			this.LISTA_SLOTS_IGU.add(new SlotIGU(slot, slot.getX(), slot.getY() +Constantes.ALTO_JUEGO - LADO_SLOTS - MARGEN_GENERAL));
			//Constantes.ALTO_JUEGO - LADO_SLOTS - MARGEN_GENERAL
		}
		
	}
	
	private void llenarSlotsEquipamientos() {///PRUEBA
		int x = this.ZONA_SLOTS_EQUIPAMIENTOS.x;
		int y = this.ZONA_SLOTS_EQUIPAMIENTOS.y;
		final int lado = LADO_SLOTS;
		
		final Rectangle RECT_ARMA = new Rectangle(x, y, lado, lado);
		slotArma = new SlotArma(RECT_ARMA, this.infoArma);
		this.LISTA_SLOTS_EQUIPAMIENTO.add(slotArma);
		this.LISTA_SLOTS_GENERAL.add(slotArma);
	}
	
	private void llenarSlotsIGU() {
		final int posIguY = Constantes.ALTO_JUEGO - LADO_SLOTS - MARGEN_GENERAL;
		for(Slot slot : this.LISTA_SLOTS_PRINCIPALES ) {
			this.LISTA_SLOTS_IGU.add(new SlotIGU(slot, slot.getX(), posIguY));
		}
		this.LISTA_SLOTS_IGU.add(new SlotIGU(slotArma,ZONA_SLOTS_PRINCIPALES.x-slotArma.getAncho()- (2*MARGEN_GENERAL), posIguY - MARGEN_GENERAL));
	}
	
	public void pintarSlotsIGU(final Graphics2D g) {
		SlotIGU slotToolTip = null;
		for(SlotIGU slotIGU : this.LISTA_SLOTS_IGU) {
			slotIGU.pintar(g);
			if(slotIGU.apuntado()&&slotIGU.contieneItem()) {
				slotToolTip = slotIGU;
			}
		}
		if(slotToolTip != null) {
			slotToolTip.pintarTooltip(g);
		}
	}
	
	private void pintarSlots(final Graphics2D g) {
		final float tamaFuente = g.getFont().getSize();
		g.setFont(g.getFont().deriveFont(6f));
		Slot apuntado = null;
		
		for(Slot  slot : this.LISTA_SLOTS_GENERAL) {
			if(this.INVENTARIO.getSlotOrigenItemSeleccionado() == slot && slot.contieneItem()) {
				slot.pintarSoloSlot(g);
				continue;
			}
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
	
	public static int getLadoSlots() {
		return LADO_SLOTS;
	}
	
}
