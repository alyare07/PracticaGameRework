package principal.entes.objetos.items.arrojadizos.granadas;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.Objeto;

public class GranadaT1 extends Granada {
	private static final long serialVersionUID = -6468671785650283188L;
	
	public GranadaT1(int cantidad) {
		super(0, 0, cantidad, 50, 20, ListaModelosItem.COD_CONSUMIBLE_GRANADAT1);
		this.rellenarInfo(LISTA_INFO);
	}
	
	public GranadaT1(int x, int y, int cantidad) {
		super(x, y, cantidad, 50, 20, ListaModelosItem.COD_CONSUMIBLE_GRANADAT1);
		this.rellenarInfo(LISTA_INFO);
	}
	
	@Override
	protected JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public Objeto copiar() {
		return new GranadaT1(x, y, this.getCantidad());
	}
	
	@Override
	protected void rellenarInfo(ArrayList<String> listaInfo) {
		listaInfo.add("Daño:  "+this.DAMAGE);
		listaInfo.add("Diametro del area:  "+this.DIAMENTRO_DEL_AREA+"mts");
		
	}


}
