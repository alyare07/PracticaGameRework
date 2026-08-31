package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.objetos.especial.CuadradoInvisible;
import principal.entes.objetos.especial.ListaObjetosEspeciales;
import principal.utilidades.Constantes;
import principal.utilidades.Render2D;

public class GrupoPaleta {

	private final ArrayList<Paleta> LISTA = new ArrayList<Paleta>();
	private final ArrayList<Rectangle> AREAS_BOTONES_PALETA = new ArrayList<Rectangle>();
	private final ArrayList<Rectangle> AREAS_BOTONES_POSICION = new ArrayList<Rectangle>();
	protected final Rectangle AREA;
	protected final Rectangle AREA_CABECERA;
	private final ArrayList<String> TEXTO_POSICIONES = new ArrayList<String>();
	private int indice;

	public GrupoPaleta(final int x, final int y, final int ancho, final int alto) {
		this.AREA = new Rectangle(x, y, ancho, alto);
		this.AREA_CABECERA = new Rectangle(x, y, ancho, 40);
		this.iniciarPaletas();
		this.llenarCabeceraBotonesSeleccionPaleta();
		this.llenarCabeceraBotonesSeleccionPosicion();
		this.llenarTextoPosiciones();
	}

	private void iniciarPaletas() {
		{
			final PaletaTile terreno = new PaletaTile(this.AREA.x, this.AREA_CABECERA.y + this.AREA_CABECERA.height,
					this.AREA.width, this.AREA.height, Constantes.LADO_TILE);
			terreno.agregarTile(ListaModeloTile.COD_ASFALTO);
			terreno.agregarTile(ListaModeloTile.COD_ARENA);
			terreno.agregarTile(ListaModeloTile.COD_CESPED);
			terreno.agregarTile(ListaModeloTile.COD_PIEDRA);
			terreno.agregarTile(ListaModeloTile.COD_VACIO);
			terreno.agregarTile(ListaModeloTile.COD_AGUA);
			terreno.agregarTile(ListaModeloTile.COD_CESPED_2);
			terreno.agregarTile(ListaModeloTile.COD_TIERRA);
			terreno.agregarTile(ListaModeloTile.COD_TIERRA_2);
			terreno.agregarTile(ListaModeloTile.COD_CESPED_3);
			terreno.agregarTile(ListaModeloTile.COD_CESPED_3_NEVADO);
			this.LISTA.add(terreno);
		}

		{
			final PaletaComplento complementosx32 = new PaletaComplento(this.AREA.x,
					this.AREA_CABECERA.y + this.AREA_CABECERA.height, this.AREA.width, this.AREA.height, 32);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_1);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_2);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_3);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_4);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_1_NEVADO);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_2_NEVADO);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_3_NEVADO);
			complementosx32.agregarComplemento(ListaModeloComplemento.COD_ARBOL_4_NEVADO);
			complementosx32
					.agregarComplemento(new CuadradoInvisible(0, 0, ListaObjetosEspeciales.COD_CUADRADO_INVISIBLE_X32));
			this.LISTA.add(complementosx32);
		}

//		{
//			final PaletaComplento complementosx16 = new PaletaComplento(this.AREA.x, this.AREA_CABECERA.y + this.AREA_CABECERA.height, this.AREA.width, this.AREA.height, 16);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_HORIZONTAL_X16);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_HORIZONTAL_X16_2);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_HORIZONTAL_X16_3);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_HORIZONTAL_X16_4);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_HORIZONTAL_X16_5);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_HORIZONTAL_X16_6);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_VERTICAL_X16);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_VERTICAL_X16_2);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_VERTICAL_X16_3);
//			complementosx16.agregarComplemento(ListaComplemento.COD_AGUA_VERTICAL_X16_4);
//			this.LISTA.add(complementosx16);
//		}

	}

	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.AREA, Color.gray);
		if (this.LISTA.isEmpty()) {
			return;
		}
		if (this.LISTA.get(this.indice) instanceof PaletaComplento) {
			this.pintarBotonesPosicion(g);
		}
		this.pintarBotonesPaleta(g);
		this.LISTA.get(this.indice).pintar(g);
	}

	public void actualizar(final Raton raton) {
		if (this.LISTA.isEmpty()) {
			return;
		}

		this.LISTA.get(this.indice).actualizar(raton);
		this.actualizarCabecera(raton);
		this.actualizarSeleccionPosicion(raton);
		if (this.LISTA.get(this.indice) instanceof PaletaComplento) {
			this.actualizarSeleccionPosicion(raton);
		}

	}

	private void pintarBotonesPaleta(final Graphics2D g) {
		if (this.AREAS_BOTONES_PALETA.isEmpty()) {
			return;
		}
		int numeroPaleta = 1;
		for (int i = 0; i < this.AREAS_BOTONES_PALETA.size(); i++) {
			final Rectangle r = this.AREAS_BOTONES_PALETA.get(i);
			if (i == this.indice) {
				Render2D.dibujarRectanguloRelleno(g, r, Color.CYAN);
			} else {
				Render2D.dibujarRectanguloRelleno(g, r, Color.white);
			}

			Render2D.dibujarString(g, String.valueOf(numeroPaleta), r.x + 2, (r.y + r.height) - 1, Color.black);
			numeroPaleta++;
		}
	}

	private void pintarBotonesPosicion(final Graphics2D g) {
		if (this.AREAS_BOTONES_POSICION.isEmpty()) {
			return;
		}
		Rectangle r = null;
		for (int i = 0; i < this.AREAS_BOTONES_POSICION.size(); i++) {
			r = this.AREAS_BOTONES_POSICION.get(i);
			if (((PaletaComplento) this.LISTA.get(this.indice)).getPosicionamientoActual() == i) {
				Render2D.dibujarRectanguloRelleno(g, r, Color.CYAN);
				Render2D.dibujarString(g, this.TEXTO_POSICIONES.get(i), r.x + 1, (r.y + r.height) - 1, Color.BLACK);
				continue;

			}
			Render2D.dibujarRectanguloRelleno(g, r, Color.white);
			Render2D.dibujarString(g, this.TEXTO_POSICIONES.get(i), r.x + 1, (r.y + r.height) - 1, Color.BLACK);
		}
	}

	private void llenarCabeceraBotonesSeleccionPaleta() {
		Rectangle rectanguloAnterior = new Rectangle(this.AREA.x, this.AREA.y + 5, 3, 10);

		for (int i = 0; i < this.LISTA.size(); i++) {
			final Rectangle r = new Rectangle(rectanguloAnterior.x + rectanguloAnterior.width + 2, rectanguloAnterior.y,
					15, 10);
			this.AREAS_BOTONES_PALETA.add(r);
			rectanguloAnterior = r;

		}
	}

	private void llenarCabeceraBotonesSeleccionPosicion() {

		Rectangle rectanguloAnterior = new Rectangle(this.AREA.x, this.AREA.y + 25, 3, 10);

		for (int i = 1; i < 10; i++) {
			final Rectangle r = new Rectangle(rectanguloAnterior.x + rectanguloAnterior.width + 2, rectanguloAnterior.y,
					15, 10);
			this.AREAS_BOTONES_POSICION.add(r);
			rectanguloAnterior = r;
		}

	}

	private void llenarTextoPosiciones() {
//		public static final int POSICIONAMIENTO_CENTRO = 1;
//		public static final int POSICIONAMIENTO_SUPERIOR_IZQUIERDA = 2;
//		public static final int POSICIONAMIENTO_SUPERIOR_DERECHA = 3;
//		public static final int POSICIONAMIENTO_INFERIOR_IZQUIERDA = 4;
//		public static final int POSICIONAMIENTO_INFERIOR_DERECHA = 5;
//		public static final int POSICIONAMIENTO_NORTE = 6;
//		public static final int POSICIONAMIENTO_SUR = 7;
//		public static final int POSICIONAMIENTO_ESTE = 8;
//		public static final int POSICIONAMIENTO_OESTE = 9;
//		public static final int POSICIONAMIENTO_LIBRE = 10;
		this.TEXTO_POSICIONES.add("C");
		this.TEXTO_POSICIONES.add("SI");
		this.TEXTO_POSICIONES.add("SD");
		this.TEXTO_POSICIONES.add("II");
		this.TEXTO_POSICIONES.add("ID");
		this.TEXTO_POSICIONES.add("N");
		this.TEXTO_POSICIONES.add("S");
		this.TEXTO_POSICIONES.add("E");
		this.TEXTO_POSICIONES.add("W");
	}

	public void actualizarCabecera(final Raton raton) {
		if (!raton.presionadoClickIzq()) {
			return;
		}
		final Rectangle posicionClicked = raton.getPuntoPresionado();
		if (posicionClicked.intersects(this.AREA_CABECERA)) {
			for (int i = 0; i < this.AREAS_BOTONES_PALETA.size(); i++) {
				final Rectangle r = this.AREAS_BOTONES_PALETA.get(i);
				if (r.intersects(posicionClicked)) {
					this.indice = i;
					return;
				}
			}
		}
	}

	public void actualizarSeleccionPosicion(final Raton raton) {
		if (!raton.presionadoClickIzq()) {
			return;
		}
		final Rectangle r = raton.getPuntoPresionado();
		for (int i = 0; i < this.AREAS_BOTONES_POSICION.size(); i++) {
			if (this.AREAS_BOTONES_POSICION.get(i).intersects(r)) {
				((PaletaComplento) this.LISTA.get(this.indice)).establecerPosicion(i);
			}
		}
	}

	public Paleta getPaletaActual() {
		return this.LISTA.get(this.indice);
	}

}
