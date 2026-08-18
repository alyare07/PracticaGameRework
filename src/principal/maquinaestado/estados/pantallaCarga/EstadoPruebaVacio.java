package principal.maquinaestado.estados.pantallaCarga;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.maquinaestado.estados.EstadoJuego;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

public class EstadoPruebaVacio implements EstadoJuego {
	final ArrayList<Rectangle> lista = new ArrayList<Rectangle>();

	final Rectangle jugador = new Rectangle(50, 50, 8, 8);

	public EstadoPruebaVacio() {
		this.lista.add(new Rectangle(0, 0, 32, 32));
		this.lista.add(new Rectangle(50, 50, 32, 32));
		this.lista.add(new Rectangle(130, 25, 32, 32));
		this.lista.add(new Rectangle(25, 130, 32, 32));
		this.lista.add(new Rectangle(200, 100, 32, 32));
		this.lista.add(new Rectangle(300, 200, 32, 32));
		this.lista.add(new Rectangle(250, 10, 32, 32));
		this.lista.add(new Rectangle(330, 250, 32, 32));
	}

	@Override
	public void actualizar() {
		if (Globales.TECLADO.TECLA_DERECHA.presionado()) {
			this.jugador.x += 1;
		}
		if (Globales.TECLADO.TECLA_IZQUIERDA.presionado()) {
			this.jugador.x -= 1;
		}
		if (Globales.TECLADO.TECLA_ARRIBA.presionado()) {
			this.jugador.y -= 1;
		}
		if (Globales.TECLADO.TECLA_ABAJO.presionado()) {
			this.jugador.y += 1;
		}
		if (Globales.TECLADO.TECLA_ESCAPE.presionado()) {
			System.exit(0);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		for (int x = 50; x <= 150; x += 16) {
			for (int y = 50; y <= 150; y += 16) {
				g.drawImage(Textura.getTextura(Textura.TEXTURA_x32_PIEDRA), x, y, null);
			}
		}
		final Composite com = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.drawImage(Textura.getTextura(Textura.TEXTURA_x32_ARBOL_1), 100, 100, null);
		g.setComposite(com);
		g.drawImage(Textura.getTextura(Textura.TEXTURA_x32_ARBOL_1), 80, 120, null);
	}

}
