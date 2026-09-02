package principal.maquinaestado.estados.menu.herramientas;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.controles.Raton;

/**
 * Clase base para todos los widgets interactivos del menú (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public abstract class ComponenteMenu {

	protected final Rectangle area;
	protected boolean visible = true;
	protected boolean enfocado = false;

	public ComponenteMenu(final Rectangle area) {
		this.area = (area != null) ? area : new Rectangle();
	}

	public abstract void actualizar(final Raton raton);

	public abstract void pintar(final Graphics2D g);

	public Rectangle getArea() {
		return this.area;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public boolean isEnfocado() {
		return this.enfocado;
	}

	public void setEnfocado(final boolean enfocado) {
		this.enfocado = enfocado;
	}
}