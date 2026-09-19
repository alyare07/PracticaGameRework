package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import principal.configuracion.Dificultad;
import principal.controles.Raton;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Menú de selección de dificultad previo a iniciar una nueva partida.
 * Asigna la variable en {@link Globales#dificultad} para que se guarde con la partida.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class MenuDificultad extends Menu {

	private static final int ANCHO_BOTON_DIF = 220;
	private static final int ALTO_BOTON_DIF = 24;
	private static final int ESPACIADO_DIF = 10;
	private static final int Y_INICIO_DIF = 110;

	private final Dificultad[] dificultades = Dificultad.values();
	private final ArrayList<BotonPixel> botonesDificultad = new ArrayList<BotonPixel>();
	private BotonPixel botonVolver;

	private int dificultadEnfocada = 1; // Por defecto: Normal
	private int ultimoMouseY = -999;

	public MenuDificultad(final GestorEstados ge) {
		super(ge, "SELECCION DE DIFICULTAD");
		this.subtituloMenu = "- ELIGE EL NIVEL DEL DESAFIO -";
		this.colorFondo = new Color(10, 12, 16, 255);
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();
		this.botonesDificultad.clear();

		final int xBoton = Constantes.CENTROX - (ANCHO_BOTON_DIF / 2);

		for (int i = 0; i < this.dificultades.length; i++) {
			final Dificultad dif = this.dificultades[i];
			final int yBoton = Y_INICIO_DIF + (i * (ALTO_BOTON_DIF + ESPACIADO_DIF));

			final BotonPixel btn = new BotonPixel(dif.getNombre(),
					new Rectangle(xBoton, yBoton, ANCHO_BOTON_DIF, ALTO_BOTON_DIF), () -> {
						Globales.dificultad = dif;
						GestorSonido.reproducir(IDSonido.SELECT);
						MenuDificultad.this.GE.iniciarPartidaNueva();
					});

			this.botonesDificultad.add(btn);
			this.botones.add(btn);
			this.componentes.add(btn);
		}

		final int yVolver = Y_INICIO_DIF + (this.dificultades.length * (ALTO_BOTON_DIF + ESPACIADO_DIF)) + 14;
		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX - 60, yVolver, 120, 18), () -> {
			this.alPresionarEscape();
		});

		this.botones.add(this.botonVolver);
		this.componentes.add(this.botonVolver);

		this.establecerIndiceEnfocado(this.dificultadEnfocada);
	}

	@Override
	public void actualizar() {
		final Raton raton = Globales.RATON;

		final int my = raton.getPosicionYEscalada();
		if (my != this.ultimoMouseY) {
			this.ultimoMouseY = my;
			final Point pMouse = raton.getPuntoPosicionEscalado();

			for (int i = 0; i < this.botonesDificultad.size(); i++) {
				if (this.botonesDificultad.get(i).getArea().contains(pMouse)) {
					this.dificultadEnfocada = i;
					this.establecerIndiceEnfocado(i);
					break;
				}
			}

			if (this.botonVolver.getArea().contains(pMouse)) {
				this.establecerIndiceEnfocado(this.dificultades.length);
			}
		}

		final int total = this.botones.size();

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_UP)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_W)) {
			final int nuevoIdx = (this.indiceBotonEnfocado <= 0) ? total - 1 : this.indiceBotonEnfocado - 1;
			this.establecerIndiceEnfocado(nuevoIdx);
			if (nuevoIdx < this.dificultades.length) {
				this.dificultadEnfocada = nuevoIdx;
			}
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_DOWN)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_S)) {
			final int nuevoIdx = (this.indiceBotonEnfocado >= total - 1) ? 0 : this.indiceBotonEnfocado + 1;
			this.establecerIndiceEnfocado(nuevoIdx);
			if (nuevoIdx < this.dificultades.length) {
				this.dificultadEnfocada = nuevoIdx;
			}
			GestorSonido.reproducir(IDSonido.SELECT_MENU);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ENTER)
				|| Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_SPACE)) {
			if (this.indiceBotonEnfocado >= 0 && this.indiceBotonEnfocado < this.botones.size()) {
				this.botones.get(this.indiceBotonEnfocado).accionar();
			}
		}

		for (int i = 0; i < this.componentes.size(); i++) {
			this.componentes.get(i).actualizar(raton);
		}

		if (Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)) {
			this.alPresionarEscape();
		}
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		for (int i = 0; i < this.componentes.size(); i++) {
			this.componentes.get(i).pintar(g);
		}

		// Cuadro inferior con la descripción de la dificultad enfocada
		if (this.dificultadEnfocada >= 0 && this.dificultadEnfocada < this.dificultades.length) {
			final Dificultad dif = this.dificultades[this.dificultadEnfocada];
			final String desc = dif.getDescripcion();

			final Font fontPrevia = g.getFont();
			g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

			final int anchoDesc = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, desc);
			final int xDesc = Constantes.CENTROX - (anchoDesc / 2);
			final int yDesc = Constantes.ALTO_JUEGO - 50;

			Render2D.dibujarStringConSombra(g, desc, xDesc, yDesc, new Color(200, 215, 235), Color.BLACK);
			g.setFont(fontPrevia);
		}

		this.pintarGuiaControles(g);
	}
}