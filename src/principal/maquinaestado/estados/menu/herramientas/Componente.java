package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Graphics2D;

public abstract class Componente {
	protected boolean visible = true;

	public abstract void pintar(final Graphics2D g);
	
	public abstract void pintar(final Graphics2D g, final int desplazamientoY);

	public abstract void actualizar();

	public boolean visible() {
		return this.visible;
	}

	public void visible(final boolean visible) {
		this.visible = visible;
	}
}
