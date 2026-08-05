package principal.entes.objetos.cofres;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.inventario.vault.InventarioVault;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;

public abstract class Cofre extends Objeto {
	public enum EstadoCofre {
		ABIERTO("Abierto"), CERRADO("Cerrado");

		private EstadoCofre(final String descripcion) {
			this.DESCRIPCION = descripcion;
		}

		private final String DESCRIPCION;

		@Override
		public String toString() {
			return this.DESCRIPCION;
		}
	}
	private static final int TIEMPO_MS_COFRE_INTERACCION_VISIBILIDAD = 500;
	private EstadoCofre estado =  EstadoCofre.CERRADO;
	private static final long serialVersionUID = 2158894619671109923L;
	private final InventarioVault INVENTARIO;
	private final int CANT_MAX_SLOT_H;
	private final int CANT_SLOT;
	private final String NOMBRE;
	private final GestorTiempo GT_COFRE_INTERACCION_VISIBILIDAD;
	
	public Cofre(int x, int y,final int cantSlot, final int cantMaxSlotH, final String nombre) {
		super(x, y);
		this.CANT_MAX_SLOT_H = cantMaxSlotH;
		this.CANT_SLOT = cantSlot;
		this.NOMBRE = nombre;
		this.INVENTARIO = new InventarioVault(this,CANT_SLOT, CANT_MAX_SLOT_H, NOMBRE);
		this.GT_COFRE_INTERACCION_VISIBILIDAD = new GestorTiempo();
		
	}
	
	
	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}
	
	
	
	@Override
	public void actualizar() {
		if(!Constantes.GLOBALES.viendoCofre &&this.estado == EstadoCofre.CERRADO && Constantes.JUGADOR.getAreaInteraccionCofre().intersects(this.getArea()) && Constantes.TECLADO.TECLA_RECOGIENDO.presionado() && this.GT_COFRE_INTERACCION_VISIBILIDAD.transcurrioMiliSegundos(TIEMPO_MS_COFRE_INTERACCION_VISIBILIDAD)) {
			this.estado = EstadoCofre.ABIERTO;
			Constantes.GLOBALES.viendoCofre = true;
			Constantes.GLOBALES.inventarioVault = INVENTARIO;
			Constantes.INVENTARIO.hacerVisible();
			this.GT_COFRE_INTERACCION_VISIBILIDAD.establecerReferenciaTiempoActual();
		}else if(this.estado == EstadoCofre.ABIERTO && !Constantes.INVENTARIO.esVisible()) {
			this.cerrar();
		}else if(this.estado == EstadoCofre.ABIERTO && Constantes.TECLADO.TECLA_RECOGIENDO.presionado() && this.GT_COFRE_INTERACCION_VISIBILIDAD.transcurrioMiliSegundos(TIEMPO_MS_COFRE_INTERACCION_VISIBILIDAD)) {
			this.cerrar();
			Constantes.INVENTARIO.ocultar();
		}else {
			 if(!Constantes.JUGADOR.getAreaInteraccionCofre().intersects(this.getArea()) && this.estado == EstadoCofre.ABIERTO) {
					this.cerrar();
					Constantes.INVENTARIO.ocultar();
					System.out.println("Cofre cerrado por alejamiento");
				}
		}
	}
	
	public void cerrar() {
		this.estado = EstadoCofre.CERRADO;
		Constantes.GLOBALES.viendoCofre = false;
		Constantes.GLOBALES.inventarioVault = null;
		this.INVENTARIO.deseleccionarSlots();
		this.GT_COFRE_INTERACCION_VISIBILIDAD.establecerReferenciaTiempoActual();
	}
	
	public boolean meterItem(final Item i) {
		return this.INVENTARIO.agregarItem(i);
	}
	
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}
	
	public EstadoCofre getEstado() {
		return this.estado;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJson() {
		JSONObject json = new JSONObject();
		json.put("tipo", this.getTipoCofre());
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), this.getListaJsonItems());
		final JSONObject jsonPrincipal = new JSONObject();
		jsonPrincipal.put("tipoObjeto", Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Cofre.class));
		jsonPrincipal.put("entiti", json);
		return jsonPrincipal;
	}
	
	@SuppressWarnings("unchecked")
	protected JSONArray getListaJsonItems() {
		JSONArray lista = new JSONArray();
		for(Item i : this.INVENTARIO.getItems()) {
			lista.add(i.getJsonItem());
		}
		return lista;
	}
	
	public static Cofre crearDesdeJSON(final JSONObject json) {
		Cofre c = null;
		if(json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofrePequeño.class))) {
			c = CofrePequeño.crearDesdeJson(json);
		}else if(json.get("tipo").toString().equals(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(CofreMediano.class))) {
			c = CofreMediano.crearDesdeJson(json);
		}
		return c;
	}
	
	protected abstract String getTipoCofre();
	
	
	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public double getPosicionX() {
		return this.x;
	}

	@Override
	public double getPosicionY() {
		return this.y;
	}

	@Override
	public void modificarPosicionX(double desplazamientoX) {
		this.x = (int) desplazamientoX;
	}

	@Override
	public void modificarPosicionY(double desplazamientoY) {
		this.y = (int) desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public String toString() {
		return "Cofre [X=" + x + ", Y=" + y + ", estado=" + estado + "]";
	}
	

	
	
	
	

}
