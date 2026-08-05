package principal.inventario;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class CajaInfo {
	protected final Rectangle AREA;
	protected HashMap<String, Info> lista = new HashMap<String, Info> ();
	protected Color colorBordes;
	protected Color colorLabels;
	protected Color colorValores;
	protected final float tamanoFuente;
	protected final int margenLabelValor = 2;
	
	
	public CajaInfo(final Rectangle area) {
		this.AREA = area;
		this.colorBordes = Color.black;
		this.colorLabels = Color.white;
		this.colorValores = Color.blue;
		this.tamanoFuente = 4f;
	}
	
	public void pintar(final Graphics2D g) {
		if(lista.isEmpty()) {
			return;
		}
		int x = this.AREA.x;
		int y = this.AREA.y;
		int proxDesplazamientoX = 0;
		int auxAnchoValores=0;
		int auxAnchoLabel = 0;
		int desplazamientoX = 0;
		g.setFont(g.getFont().deriveFont(this.tamanoFuente));
		for(Info info : this.lista.values()) {
			y += calcularAltoPixeles(g, info,true);
			if(y > (this.AREA.y+this.AREA.height)) {
				y = this.AREA.y+calcularAltoPixeles(g, info,true);
				x= this.AREA.x + desplazamientoX + proxDesplazamientoX+this.margenLabelValor;
				desplazamientoX+=proxDesplazamientoX+this.margenLabelValor;
				proxDesplazamientoX = 0;
			}
			DibujoDebug.dibujarString(g, info.getTexto(), x, y, colorLabels);
			auxAnchoValores = calcularAnchoPixeles(g, info, false);
			auxAnchoLabel = calcularAnchoPixeles(g, info,true);
			if(proxDesplazamientoX < (auxAnchoLabel+ auxAnchoValores)) {
				proxDesplazamientoX = ( auxAnchoLabel+ auxAnchoValores);
			}
			DibujoDebug.dibujarString(g, String.valueOf(info.getValor()),x+auxAnchoLabel+this.margenLabelValor, y, colorValores);
			y+=1;
		}
		
		
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}
	
	public void actualizarLista(final HashMap<String, Info> lista) {
		this.lista = lista;
	}
	
	private int calcularAltoPixeles(final Graphics2D g ,final Info i,final boolean label) {
		return label? Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, i.getTexto()) : Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, String.valueOf(i.getValor()));
	}
	
	private int calcularAnchoPixeles(final Graphics2D g ,final Info i,final boolean label) {
		return label? Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, i.getTexto()) : Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, String.valueOf(i.getValor()));
	}
	
	public Info getInfo(final String clave) {
		return this.lista.get(clave);
	}
}
