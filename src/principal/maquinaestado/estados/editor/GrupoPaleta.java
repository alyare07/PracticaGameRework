package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.modelos.complemento.TipoModeloComplemento;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.EntradaCueva;
import principal.entes.objetos.cofres.CofreMediano;
import principal.entes.objetos.cofres.CofrePequeño;
import principal.entes.objetos.especial.CuadradoInvisible;
import principal.entes.objetos.especial.ListaObjetosEspeciales;
import principal.entes.objetos.fabricables.Cama;
import principal.entes.objetos.fabricables.Carpa;
import principal.entes.objetos.fabricables.Fogata;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.ArbustoCosechable;
import principal.entes.objetos.recursos.arboles.TipoArbol;
import principal.entes.objetos.recursos.minerales.MineralCarbon;
import principal.entes.objetos.recursos.minerales.MineralCobre;
import principal.entes.objetos.recursos.minerales.MineralCristal;
import principal.entes.objetos.recursos.minerales.MineralHierro;
import principal.entes.objetos.recursos.minerales.MineralOro;
import principal.entes.objetos.recursos.minerales.MineralRoca;
import principal.recursos.ClaveHoja;
import principal.recursos.TexturaObjetos;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

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

		final HojaSprite arboles = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_32x48);
		final HojaSprite arbolesNevados = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_NEVADOS_32);
		final HojaSprite minerales = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.MINERALES_COSECHABLES_16);
		final HojaSprite arbustos = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBUSTO_COSECHABLES);
		final HojaSprite cofres = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.COFRES_16);
		final HojaSprite hojaFogata = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.FOGATA);
		final HojaSprite dungeon = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.DUNGEON_16);
		final BufferedImage transparente = Globales.GESTOR_TEXTURAS.getTexturaTransparente();

		// =====================================================================
		// 1. PESTAÑA: SUELOS & HERRAMIENTAS
		// =====================================================================
		final PaletaTile paletaSuelos = new PaletaTile(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				Constantes.LADO_TILE);
		this.registrarPaleta("Suelos", paletaSuelos);

		// 2. PESTAÑA: RECURSOS COSECHABLES (ÁRBOLES, ARBUSTOS Y MINERALES)
		final PaletaComplento paletaRecursos = new PaletaComplento(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				32);

		// Todos los árboles talables desde el Enum TipoArbol con su sprite exacto
		if (arboles != null) {
			for (final TipoArbol t : TipoArbol.values()) {
				paletaRecursos.agregarEntrada(t.getNombre(), arboles.getSprite(t.getSpriteIndex()), true,
						(x, y) -> new ArbolCosechable(x, y, t));
			}
		}

		// Arbusto de bayas
		if (arbustos != null) {
			paletaRecursos.agregarEntrada("Arbusto Silvestre", arbustos.getSprite(1), true,
					(x, y) -> new ArbustoCosechable(x, y));
		}

		// Los 6 Minerales
		if (minerales != null) {
			paletaRecursos.agregarEntrada("Roca de Piedra", minerales.getSprite(MineralRoca.SPRITE_INDEX), true,
					(x, y) -> new MineralRoca(x, y));
			paletaRecursos.agregarEntrada("Veta de Cobre", minerales.getSprite(MineralCobre.SPRITE_INDEX), true,
					(x, y) -> new MineralCobre(x, y));
			paletaRecursos.agregarEntrada("Veta de Hierro", minerales.getSprite(MineralHierro.SPRITE_INDEX), true,
					(x, y) -> new MineralHierro(x, y));
			paletaRecursos.agregarEntrada("Veta de Oro", minerales.getSprite(MineralOro.SPRITE_INDEX), true,
					(x, y) -> new MineralOro(x, y));
			paletaRecursos.agregarEntrada("Veta de Carbón", minerales.getSprite(MineralCarbon.SPRITE_INDEX), true,
					(x, y) -> new MineralCarbon(x, y));
			paletaRecursos.agregarEntrada("Cristal Arcano", minerales.getSprite(MineralCristal.SPRITE_INDEX), true,
					(x, y) -> new MineralCristal(x, y));
		}
		this.registrarPaleta("Recursos", paletaRecursos);

		// =====================================================================
		// 3. PESTAÑA: OBJETOS, ESTRUCTURAS Y COMPLEMENTOS
		// =====================================================================
		final PaletaComplento paletaObjetos = new PaletaComplento(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				32);

		// Contenedores
		paletaObjetos.agregarEntrada("Cofre Pequeño", (cofres != null ? cofres.getSprite(1) : null), false,
				(x, y) -> new CofrePequeño(x, y));
		paletaObjetos.agregarEntrada("Cofre Mediano", (cofres != null ? cofres.getSprite(1) : null), false,
				(x, y) -> new CofreMediano(x, y));
		if (arboles != null) {
			paletaObjetos.agregarEntrada("Árbol Cofre Secreto", arboles.getSprite(1), false,
					(x, y) -> new ArbolCofre(x, y));
		}

		// Estructuras y Refugios
		paletaObjetos.agregarEntrada("Fogata", (hojaFogata != null ? hojaFogata.getSprite(0) : null), false,
				(x, y) -> new Fogata(x, y));
		paletaObjetos.agregarEntrada("Carpa de Pionero", Globales.GESTOR_TEXTURAS.get(TexturaObjetos.CARPA_X32), false,
				(x, y) -> new Carpa(x, y));
		paletaObjetos.agregarEntrada("Cama de Interior", Globales.GESTOR_TEXTURAS.get(TexturaObjetos.CAMA_X32), false,
				(x, y) -> new Cama(x, y));
		paletaObjetos.agregarEntrada("Entrada a Cueva", (dungeon != null ? dungeon.getSprite(50) : null), false,
				(x, y) -> new EntradaCueva(x, y, "cueva_1", "Comienzo"));

		// Edificios funcionales completos desde TipoEdificio
		for (final principal.entes.estructuras.TipoEdificio e : principal.entes.estructuras.TipoEdificio.values()) {
			paletaObjetos.agregarEntrada(e.getNombre(), e.getTextura(), false,
					(x, y) -> new principal.entes.estructuras.Edificio(x, y, e));
		}

		// Complementos del escenario desde el Enum TipoModeloComplemento
		for (final TipoModeloComplemento m : TipoModeloComplemento.values()) {
			if (m != TipoModeloComplemento.BARRERA_INVISIBLE) {
				paletaObjetos.agregarEntrada(m.getNombre(), m.getTextura(), false, (x, y) -> new Complemento(x, y, m));
			}
		}

		// Barreras invisibles
		paletaObjetos.agregarEntrada("Barrera Invisible", transparente, false,
				(x, y) -> new Complemento(x, y, TipoModeloComplemento.BARRERA_INVISIBLE));
		paletaObjetos.agregarEntrada("Cuadrado Invisible", transparente, false,
				(x, y) -> new CuadradoInvisible(x, y, ListaObjetosEspeciales.COD_CUADRADO_INVISIBLE_X32));

		this.registrarPaleta("Objetos", paletaObjetos);

		// =====================================================================
		// 4. PESTAÑA: CRIATURAS Y FAUNA
		// =====================================================================
		final PaletaCriaturas paletaCriaturas = new PaletaCriaturas(this.AREA.x, yPaleta, this.AREA.width, altoPaleta,
				32);
		this.registrarPaleta("Criaturas", paletaCriaturas);

		// =====================================================================
		// 5. PESTAÑA: ÍTEMS Y EQUIPAMIENTO
		// =====================================================================
		final PaletaItems paletaItems = new PaletaItems(this.AREA.x, yPaleta, this.AREA.width, altoPaleta, 32,
				this.editor);
		this.indicePaletaItem = this.registrarPaleta("Items", paletaItems);

		// =====================================================================
		// 6. PESTAÑA: TRIGGERS, VOLÚMENES Y LUCES
		// =====================================================================
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

	public boolean seleccionarPestanaPorNombre(final String nombrePestana) {
		if (nombrePestana == null) {
			return false;
		}
		for (int i = 0; i < this.NOMBRES_PESTANAS.size(); i++) {
			if (this.NOMBRES_PESTANAS.get(i).equalsIgnoreCase(nombrePestana)) {
				this.indiceActivo = i;
				return true;
			}
		}
		return false;
	}

	public Paleta getPaleta(final int indice) {
		return ((indice >= 0) && (indice < this.LISTA.size())) ? this.LISTA.get(indice) : null;
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