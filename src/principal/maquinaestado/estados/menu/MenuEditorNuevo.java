package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.entes.modelos.tile.ListaModeloTile;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class MenuEditorNuevo extends Menu {

	private static final int PANEL_ANCHO = 320;
	private static final int PANEL_ALTO = 160;

	private CajaTextoPixel ctAncho;
	private CajaTextoPixel ctAlto;
	private CajaTextoPixel ctIdTile;

	private BotonPixel botonCrear;
	private BotonPixel botonVolver;

	public MenuEditorNuevo(final GestorEstados ge) {
		super(ge, "CREAR NUEVO MAPA");
		this.subtituloMenu = "- ASISTENTE DE TERRENO -";
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		final int panelY = Constantes.CENTROY - (PANEL_ALTO / 2) - 10;

		// 1. Campos de entrada
		this.ctAncho = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 25, 55, 16), "50", 4,
				true);
		this.ctAlto = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 55, 55, 16), "50", 4,
				true);
		this.ctIdTile = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 85, 55, 16),
				String.valueOf(ListaModeloTile.COD_TIERRA), 2, true);

		this.componentes.add(this.ctAncho);
		this.componentes.add(this.ctAlto);
		this.componentes.add(this.ctIdTile);

		// 2. Botones de acción
		final int yBotones = panelY + PANEL_ALTO + 12;
		this.botonCrear = new BotonPixel("Crear", new Rectangle(Constantes.CENTROX - 105, yBotones, 100, 18), () -> {
			final int ancho = this.ctAncho.getNumeroEntero(50);
			final int alto = this.ctAlto.getNumeroEntero(50);
			final int idTile = this.ctIdTile.getNumeroEntero(ListaModeloTile.COD_TIERRA);

			if ((ancho > 0) && (alto > 0)) {
				this.GE.editorMapa(ancho, alto, idTile);
			}
		});

		this.botonVolver = new BotonPixel("Volver", new Rectangle(Constantes.CENTROX + 5, yBotones, 100, 18), () -> {
			this.alPresionarEscape();
		});

		this.componentes.add(this.botonCrear);
		this.componentes.add(this.botonVolver);
		this.botones.add(this.botonCrear);
		this.botones.add(this.botonVolver);
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.botonCrear.actualizar(Globales.RATON);
		this.botonVolver.actualizar(Globales.RATON);
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.editorMapaSeleccion();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarFondo(g);
		this.pintarCabecera(g);

		final int panelX = Constantes.CENTROX - (PANEL_ANCHO / 2);
		final int panelY = Constantes.CENTROY - (PANEL_ALTO / 2) - 10;

		// 1. Panel contenedor
		Render2D.dibujarRectanguloRelleno(g, panelX, panelY, PANEL_ANCHO, PANEL_ALTO, new Color(16, 20, 26, 235));
		Render2D.dibujarRectanguloContorno(g, panelX, panelY, PANEL_ANCHO, PANEL_ALTO, new Color(55, 60, 75));

		// 2. Etiquetas de texto en m5x7 (16f)
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		Render2D.dibujarStringConSombra(g, "Ancho del Terreno (Tiles):", panelX + 16, panelY + 38, Color.WHITE,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Alto del Terreno (Tiles):", panelX + 16, panelY + 68, Color.WHITE,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, "ID Modelo Tile Inicial:", panelX + 16, panelY + 98, Color.WHITE,
				Color.BLACK);

		g.setFont(fontPrevia);

		// 3. Previsualización del Tile en vivo
		final int idTile = this.ctIdTile.getNumeroEntero(1);
		if (ListaModeloTile.getModelo(idTile) != null) {
			final BufferedImage texturaTile = ListaModeloTile.getModelo(idTile).getTextura();
			if (texturaTile != null) {
				final int previewX = (panelX + PANEL_ANCHO) - 110;
				final int previewY = panelY + 85;
				Render2D.dibujarImagen(g, texturaTile, previewX, previewY);
				Render2D.dibujarRectanguloContorno(g, previewX, previewY, 16, 16, Color.YELLOW);
			}
		}

		// 4. Componentes y Botones
		this.ctAncho.pintar(g);
		this.ctAlto.pintar(g);
		this.ctIdTile.pintar(g);
		this.botonCrear.pintar(g);
		this.botonVolver.pintar(g);

		this.pintarGuiaControles(g);
	}
}