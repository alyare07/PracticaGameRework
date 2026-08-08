package principal.inventario.equipamiento;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.inventario.CajaInfo;
import principal.inventario.Info;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public class SlotArma extends SlotEquipamiento {
	protected HashMap<String, Info> lista;
	protected final CajaInfo cajaInfo;

	public SlotArma(Rectangle area, final CajaInfo cajaInfo) {
		super(area, Constantes.FUNCIONES.CARGADOR_RECURSOS.cargarImagenCompatibleTranslucida("/imagenes/objetos/gun16x12_transparente.png"));
		this.cajaInfo = cajaInfo;
	}
	
	public void actualizar(final Raton raton) {
		super.actualizar(raton);
		this.actualizarLista();
		
		//esto dara problemas para el igu por el pisado del metodo al heredar
	}
	
	protected void pintarObjeto(final Graphics2D g, final Rectangle area) {
		if (item != null) {
			
			this.item.pintarInventario(g, area.x + this.MARGEN_ESPACIADO, area.y + this.MARGEN_ESPACIADO);
			if(item instanceof Pistola) {
				float aux = g.getFont().getSize();
				g.setFont(g.getFont().deriveFont(4f));
				
				final String cantidadBalas = String.valueOf(((Pistola) this.item).getMunicion().getCantidad());
				final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, cantidadBalas);
				final int altoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, cantidadBalas);
				
				DibujoDebug.dibujarRectanguloRelleno(g, area.x, area.y +area.height-altoTexto-1, 11, 6, Color.LIGHT_GRAY);
				DibujoDebug.dibujarString(g, cantidadBalas, area.x, area.y +area.height - (altoTexto/2), Color.black);
				DibujoDebug.dibujarImagen(g, Textura.getTextura(Textura.TEXTURA_x4_BALA), area.x+anchoTexto, area.y+ area.height - altoTexto);
				g.setFont(g.getFont().deriveFont(aux));
				
			}
//			this.cajaInfo.pintar(g);
		}else {
			DibujoDebug.dibujarImagen(g, logo, area.x+1, area.y+5);
		}

	}
	
	
	
	private void actualizarLista() {
		if(this.item == null) {
			this.lista = new HashMap<String, Info> ();
			this.cajaInfo.actualizarLista(lista);
			return;
		}
		final Arma arma = (Arma) this.item;
		this.lista = new HashMap<String, Info> ();
		this.lista.put("Ataque", new Info("Ataque", String.valueOf(arma.getAtaque())));
		this.lista.put("Alcance", new Info("Alcance", String.valueOf(arma.getAlcance())));
		this.lista.put("Vel. Ataque", new Info("Penetrante", String.valueOf(arma.esPenetrante())));
		if(arma.getMunicion() != null) {
			this.lista.put("Municion", new Info("Municion", arma.getMunicion().toString()));
		}
		this.cajaInfo.actualizarLista(lista);
		
	}



	@Override
	public boolean validarAdmisionItem(Item i) {
		return i instanceof Arma || i == null;
	}
	
	public HashMap<String, Info> getLista(){
		return this.lista;
	}
	
	
	@Override
	public void establecerObjeto(final Item obj) {
		this.item = obj;
		this.actualizarLista();
	}
	
	

}
