package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.controles.Raton;
import principal.mapa.Tile;
import principal.maquinaestado.estados.editor.herramientas.TipoHerramientaDibujo;
import principal.recursos.SetTerreno;
import principal.recursos.TipoTerreno;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Paleta de selección de terrenos y herramientas de pintura geométrica (Lápiz,
 * Flood Fill / Bote, Rectángulos y Reemplazador global).
 * 
 * @version 2.0 (Vanilla Java 8 - Tool Integration)
 */
public class PaletaTile extends Paleta {

	private final TipoTerreno[] TIPOS_TERRENO = TipoTerreno.values();
	private TipoHerramientaDibujo herramientaSeleccionada = TipoHerramientaDibujo.PINCEL;

	private final Rectangle[] botonesHerramientas = new Rectangle[TipoHerramientaDibujo.values().length];
	private static final Font FUENTE_TOOL = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public PaletaTile(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y + 20, ancho, alto - 20, ladoSlot);
		this.inicializarBotonesHerramientas(x, y, ancho);
	}

	private void inicializarBotonesHerramientas(final int x, final int y, final int ancho) {
		final int total = TipoHerramientaDibujo.values().length;
		final int anchoBoton = (ancho - 4) / total;
		int xBoton = x + 2;

		for (int i = 0; i < total; i++) {
			this.botonesHerramientas[i] = new Rectangle(xBoton, y + 2, anchoBoton, 16);
			xBoton += anchoBoton;
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (raton == null) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point pClick = raton.getPuntoPosicionEscalado();

			// 1. Selección de herramienta de trazado
			for (int i = 0; i < this.botonesHerramientas.length; i++) {
				if (this.botonesHerramientas[i].contains(pClick)) {
					this.herramientaSeleccionada = TipoHerramientaDibujo.values()[i];
					GestorSonido.reproducir(IDSonido.GOLPE_1);
					return;
				}
			}
		}

		super.actualizar(raton);
	}

	@Override
	public void pintar(final Graphics2D g) {
		// 1. Barra de botones de herramientas de dibujo
		final int cabY = this.AREA.y - 20;
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, cabY, this.AREA.width, 20, new Color(30, 32, 40));
		Render2D.dibujarRectanguloContorno(g, this.AREA.x, cabY, this.AREA.width, 20, Color.BLACK);

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_TOOL);

		final TipoHerramientaDibujo[] herramientas = TipoHerramientaDibujo.values();
		final String[] siglas = { "PEN", "FILL", "REC", "BOX", "REP" };

		for (int i = 0; i < this.botonesHerramientas.length; i++) {
			final Rectangle r = this.botonesHerramientas[i];
			final boolean activa = (herramientas[i] == this.herramientaSeleccionada);

			Render2D.dibujarRectanguloRelleno(g, r, activa ? new Color(70, 75, 95) : new Color(40, 42, 50));
			Render2D.dibujarRectanguloContorno(g, r, activa ? Color.YELLOW : Color.DARK_GRAY);

			final String sigla = siglas[i];
			final int anchoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, sigla);
			final int altoTexto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, sigla);
			final int tx = (r.x + (r.width / 2)) - (anchoTexto / 2);
			final int ty = ((r.y + (r.height / 2)) + (altoTexto / 2)) - 1;

			Render2D.dibujarString(g, sigla, tx, ty, activa ? Color.WHITE : Color.LIGHT_GRAY);
		}

		g.setFont(fontPrevia);

		// 2. Grilla de selección de tiles
		super.pintar(g);
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.TIPOS_TERRENO.length;
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final TipoTerreno tipo = this.TIPOS_TERRENO[index];
		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(tipo);
		if (set != null) {
			final BufferedImage img = set.getSpriteBase();
			if (img != null) {
				Render2D.dibujarImagen(g, img, slotX, slotY);
			}
		}
	}

	public Tile getTileSeleccionado() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.TIPOS_TERRENO.length)) {
			final TipoTerreno tipo = this.TIPOS_TERRENO[this.indiceSeleccionado];
			return new Tile(0, 0, Constantes.LADO_TILE, tipo);
		}
		return null;
	}

	public TipoHerramientaDibujo getHerramientaSeleccionada() {
		return this.herramientaSeleccionada;
	}

	public void setHerramientaSeleccionada(final TipoHerramientaDibujo herramienta) {
		if (herramienta != null) {
			this.herramientaSeleccionada = herramienta;
		}
	}

	@Override
	public String getNombreElemento(final int index) {
		return ((index >= 0) && (index < this.TIPOS_TERRENO.length)) ? this.TIPOS_TERRENO[index].getNombre() : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		final Tile seleccionado = this.getTileSeleccionado();
		return (seleccionado != null) && (tileEvaluar != null)
				&& (seleccionado.getTipoTerreno() == tileEvaluar.getTipoTerreno());
	}
}