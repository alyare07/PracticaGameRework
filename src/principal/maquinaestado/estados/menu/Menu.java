package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.menu.herramientas.Componente;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public abstract class Menu implements EstadoJuego {
	protected final GestorEstados GE;
	protected final Dimension DIMENSION = new Dimension(Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
	protected final BufferedImage FONDO = Constantes.FUNCIONES.TEXTURAS_TOOLS.crearTextura(new Color(20, 20, 19), DIMENSION.width, DIMENSION.height);
	protected final ArrayList<Componente> COMPONENTES = new ArrayList<Componente>();

	public Menu(final GestorEstados ge) {
		this.GE = ge;
		this.inicializarBotones();
	}

	@Override
	public void actualizar() {
		for (Componente c : this.COMPONENTES) {
			if (c.visible()) {
				c.actualizar();
			}
		}

	}

	@Override
	public void pintar(Graphics2D g) {
		DibujoDebug.dibujarImagen(g, FONDO, 0, 0);
		for (Componente c : this.COMPONENTES) {
			if (c.visible()) {
				c.pintar(g);
			}
		}
	}

	protected abstract void inicializarBotones();

	protected void accionPostClick() {
		Constantes.RATON.dormirMS(500);
		Constantes.RATON.soltar();
	}

}
