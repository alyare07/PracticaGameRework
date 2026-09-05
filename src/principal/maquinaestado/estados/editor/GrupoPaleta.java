package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.cofres.CofreMediano;
import principal.entes.objetos.cofres.CofrePequeño;
import principal.entes.objetos.especial.CuadradoInvisible;
import principal.entes.objetos.especial.ListaObjetosEspeciales;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.recursos.ClaveHoja;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

/**
 * Gestor maestro de pestañas del panel lateral del editor. Organiza las 6
 * categorías: Suelos, Recursos, Objetos, Criaturas, Ítems y Triggers/Eventos.
 * 
 * @version 2.2 (Vanilla Java 8)
 */
public class GrupoPaleta {

	private final ArrayList<Paleta> LISTA = new ArrayList<Paleta>();
	private final ArrayList<String> NOMBRES_PESTANAS = new ArrayList<String>();
	private final ArrayList<Rectangle> AREAS_PESTANAS = new ArrayList<Rectangle>();

	protected final Rectangle AREA;
	protected final Rectangle AREA_CABECERA;
	private int indiceActivo = 0;
	private final EditorMapa editor;
	private int indicePaletaItem;

	private static final Font FUENTE_PESTANAS = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public GrupoPaleta(final int x, final int y, final int ancho, final int alto, final EditorMapa editor) {
		this.AREA = new Rectangle(x, y, ancho, alto);
		this.AREA_CABECERA = new Rectangle(x, y, ancho, 20);
		this.editor = editor;

		this.iniciarPaletas();
		this.recalcularAreasPestanas();
	}

	private void iniciarPaletas() {
		final int yPaleta = this.AREA_CABECERA.y + this.AREA_CABECERA.height;
		final int altoPaleta = this.AREA.height - this.AREA_CABECERA.height;

		final HojaSprite arboles = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_32);
		final HojaSprite arbolesNevados = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_NEVADOS_32);
		final HojaSprite dungeon = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.DUNGEON_16);
		final HojaSprite casa = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.CASA_1);
		final HojaSprite cofres = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.COFRES_16);
		final BufferedImage transparente = Globales.GESTOR_TEXTURAS.getTexturaTransparente();

		// 1. PESTAÑA: SUELOS & HERRAMIENTAS
		final PaletaTile paletaSuelos = new PaletaTile(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				Constantes.LADO_TILE);
		this.registrarPaleta("Suelos", paletaSuelos);

		// 2. PESTAÑA: RECURSOS COSECHABLES
		final PaletaComplento paletaRecursos = new PaletaComplento(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				32);
		paletaRecursos.agregarEntrada("Árbol Talable 1", arboles.getSprite(0), true,
				(x, y) -> new ArbolCosechable(x, y, ClaveHoja.ARBOLES_32, 0));
		paletaRecursos.agregarEntrada("Árbol Talable 2", arboles.getSprite(1), true,
				(x, y) -> new ArbolCosechable(x, y, ClaveHoja.ARBOLES_32, 1));
		paletaRecursos.agregarEntrada("Árbol Nevado Talable", arbolesNevados.getSprite(0), true,
				(x, y) -> new ArbolCosechable(x, y, ClaveHoja.ARBOLES_NEVADOS_32, 0));
		paletaRecursos.agregarEntrada("Roca Minable", dungeon.getSprite(813), true,
				(x, y) -> new RocaCosechable(x, y, ClaveHoja.DUNGEON_16, 813));
		this.registrarPaleta("Recursos", paletaRecursos);

		// 3. PESTAÑA: OBJETOS Y COMPLEMENTOS
		final PaletaComplento paletaObjetos = new PaletaComplento(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				32);
		paletaObjetos.agregarEntrada("Casa Grande", casa.getSprite(0), false,
				(x, y) -> new Complemento(x, y, ListaModeloComplemento.COD_CASA_1));
		paletaObjetos.agregarEntrada("Árbol Decorativo 1", arboles.getSprite(0), false,
				(x, y) -> new Complemento(x, y, ListaModeloComplemento.COD_ARBOL_1));
		paletaObjetos.agregarEntrada("Árbol Decorativo 2", arboles.getSprite(1), false,
				(x, y) -> new Complemento(x, y, ListaModeloComplemento.COD_ARBOL_2));
		paletaObjetos.agregarEntrada("Cofre Mediano", (cofres != null ? cofres.getSprite(1) : null), false,
				(x, y) -> new CofreMediano(x, y));
		paletaObjetos.agregarEntrada("Cofre Pequeño", (cofres != null ? cofres.getSprite(1) : null), false,
				(x, y) -> new CofrePequeño(x, y));
		paletaObjetos.agregarEntrada("Árbol Cofre Secreto", arboles.getSprite(1), false,
				(x, y) -> new ArbolCofre(x, y));
		paletaObjetos.agregarEntrada("Barrera Invisible", transparente, false,
				(x, y) -> new Complemento(x, y, ListaModeloComplemento.COD_BARRERA_INVISIBLE));
		paletaObjetos.agregarEntrada("Cuadrado Invisible", transparente, false,
				(x, y) -> new CuadradoInvisible(x, y, ListaObjetosEspeciales.COD_CUADRADO_INVISIBLE_X32));
		this.registrarPaleta("Objetos", paletaObjetos);

		// 4. PESTAÑA: CRIATURAS Y ENEMIGOS
		final PaletaCriaturas paletaCriaturas = new PaletaCriaturas(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				32);
		this.registrarPaleta("Criaturas", paletaCriaturas);

		// 5. PESTAÑA: ÍTEMS Y EQUIPAMIENTO
		final PaletaItems paletaItems = new PaletaItems(this.AREA.x, yPaleta, this.AREA.width, altoPaleta, 32,
				this.editor);
		this.indicePaletaItem = this.registrarPaleta("Items", paletaItems);

		// 6. PESTAÑA: TRIGGERS, VOLÚMENES Y LUCES
		final PaletaTriggers paletaTriggers = new PaletaTriggers(this.AREA.x, yPaleta, this.AREA.width, altoPaleta, 32);
		this.registrarPaleta("Triggers", paletaTriggers);
	}

	public int registrarPaleta(final String nombrePestana, final Paleta paleta) {
		if ((nombrePestana != null) && (paleta != null)) {
			this.NOMBRES_PESTANAS.add(nombrePestana);
			this.LISTA.add(paleta);
			this.recalcularAreasPestanas();
			return this.LISTA.size() - 1;
		}
		return 0;
	}

	private void recalcularAreasPestanas() {
		this.AREAS_PESTANAS.clear();
		final int total = this.LISTA.size();
		if (total <= 0) {
			return;
		}

		final int anchoBoton = (this.AREA_CABECERA.width - 4) / total;
		int xBoton = this.AREA_CABECERA.x + 2;

		for (int i = 0; i < total; i++) {
			final Rectangle r = new Rectangle(xBoton, this.AREA_CABECERA.y + 2, anchoBoton,
					this.AREA_CABECERA.height - 4);
			this.AREAS_PESTANAS.add(r);
			xBoton += anchoBoton;
		}
	}

	public void actualizar(final Raton raton) {
		if (this.LISTA.isEmpty() || (raton == null)) {
			return;
		}

		if (raton.presionadoClickIzq()) {
			final Rectangle pClick = raton.getPuntoPresionado();
			if (pClick.intersects(this.AREA_CABECERA)) {
				for (int i = 0; i < this.AREAS_PESTANAS.size(); i++) {
					if (this.AREAS_PESTANAS.get(i).intersects(pClick)) {
						this.indiceActivo = i;
						return;
					}
				}
			}
		}

		this.LISTA.get(this.indiceActivo).actualizar(raton);
	}

	public void pintar(final Graphics2D g) {
		Render2D.dibujarRectanguloRelleno(g, this.AREA_CABECERA, new Color(30, 30, 35));
		Render2D.dibujarRectanguloContorno(g, this.AREA_CABECERA, Color.BLACK);

		this.pintarPestanas(g);

		if (!this.LISTA.isEmpty()) {
			this.LISTA.get(this.indiceActivo).pintar(g);
		}
	}

	private void pintarPestanas(final Graphics2D g) {
		final Font fuentePrevia = g.getFont();
		g.setFont(FUENTE_PESTANAS);

		for (int i = 0; i < this.AREAS_PESTANAS.size(); i++) {
			final Rectangle r = this.AREAS_PESTANAS.get(i);
			final boolean activa = (i == this.indiceActivo);

			Render2D.dibujarRectanguloRelleno(g, r, activa ? new Color(70, 70, 80) : new Color(45, 45, 50));
			Render2D.dibujarRectanguloContorno(g, r, activa ? Color.YELLOW : Color.DARK_GRAY);

			final String texto = this.NOMBRES_PESTANAS.get(i);
			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);

			final int xTexto = (r.x + (r.width / 2)) - (anchoTexto / 2);
			final int yTexto = ((r.y + (r.height / 2)) + (altoTexto / 2)) - 1;

			Render2D.dibujarString(g, texto, xTexto, yTexto, activa ? Color.WHITE : Color.LIGHT_GRAY);
		}

		g.setFont(fuentePrevia);
	}

	public Paleta getPaletaActual() {
		return !this.LISTA.isEmpty() ? this.LISTA.get(this.indiceActivo) : null;
	}

	public int getIndiceActivo() {
		return this.indiceActivo;
	}

	public void setPaletaItemSelected() {
		this.indiceActivo = this.indicePaletaItem;
	}

	public boolean isPaletaItemSelected() {
		return this.indiceActivo == this.indicePaletaItem;
	}
}