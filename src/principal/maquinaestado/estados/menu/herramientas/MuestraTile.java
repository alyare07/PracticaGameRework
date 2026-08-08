package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Graphics2D;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.utilidades.DibujoDebug;

public class MuestraTile extends Componente {
	protected int idTile;
	protected int x;
	protected int y;
	
	public MuestraTile(final int idTile, final int x, final int y) {
		this.idTile = idTile;
		this.x = x;
		this.y = y;
	}
	@Override
	public void pintar(Graphics2D g) {
		
		DibujoDebug.dibujarImagen(g, ListaModeloTile.getModelo(idTile).getTextura(), x , y);
	}

	@Override
	public void pintar(Graphics2D g, int desplazamientoY) {

	}

	@Override
	public void actualizar() {
		
	}
	
	public void cambiarIdTile(final int idTile) {
		if(idTile != this.idTile) {
			if(ListaModeloTile.getModelo(idTile)==null) {
				if(this.visible) {
					this.visible = false; 
				}
			}else {
				if(!this.visible) {
					this.visible = true; 
				}
			}
			this.idTile = idTile;
		}
	}

}
