package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.recursos.SetTerreno;
import principal.recursos.TipoTerreno;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class MenuEditorNuevo extends Menu {

	private static final int PANEL_ANCHO = 320;
	private static final int PANEL_ALTO = 160;

	private CajaTextoPixel ctAncho;
	private CajaTextoPixel ctAlto;

	private int indiceTerreno = 0;
	private final Rectangle areaSelectorTerreno = new Rectangle();

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

		// 1. Campos numéricos de dimensiones
		this.ctAncho = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 25, 55, 16), "50", 4,
				true);
		this.ctAlto = new CajaTextoPixel(new Rectangle((panelX + PANEL_ANCHO) - 75, panelY + 55, 55, 16), "50", 4,
				true);

		this.areaSelectorTerreno.setBounds((panelX + PANEL_ANCHO) - 130, panelY + 85, 110, 18);

		this.componentes.add(this.ctAncho);
		this.componentes.add(this.ctAlto);

		// 2. Botones de acción
		final int yBotones = panelY + PANEL_ALTO + 12;
		this.botonCrear = new BotonPixel("Crear", new Rectangle(Constantes.CENTROX - 105, yBotones, 100, 18), () -> {
			final int ancho = this.ctAncho.getNumeroEntero(50);
			final int alto = this.ctAlto.getNumeroEntero(50);
			final TipoTerreno tipoInicial = TipoTerreno.values()[this.indiceTerreno];

			if ((ancho > 0) && (alto > 0)) {
				this.GE.editorMapa(ancho, alto, tipoInicial);
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

		// Clic en el selector de terreno para ciclar al siguiente bioma
		if (Globales.RATON.presionadoClickIzqUnicaAct()) {
			final Point p = Globales.RATON.getPuntoPosicionEscalado();
			if (this.areaSelectorTerreno.contains(p)) {
				this.indiceTerreno = (this.indiceTerreno + 1) % TipoTerreno.values().length;
			}
		}
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

		// 2. Etiquetas
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		Render2D.dibujarStringConSombra(g, "Ancho del Terreno (Tiles):", panelX + 16, panelY + 38, Color.WHITE,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Alto del Terreno (Tiles):", panelX + 16, panelY + 68, Color.WHITE,
				Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Tipo de Terreno Inicial:", panelX + 16, panelY + 98, Color.WHITE,
				Color.BLACK);

		g.setFont(fontPrevia);

		// 3. Selector interactivo de Terreno con preview en vivo
		final TipoTerreno tipoActual = TipoTerreno.values()[this.indiceTerreno];
		final SetTerreno set = Globales.GESTOR_TEXTURAS.getSetTerreno(tipoActual);

		Render2D.dibujarRectanguloRelleno(g, this.areaSelectorTerreno, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, this.areaSelectorTerreno, new Color(220, 180, 50));

		if (set != null) {
			final BufferedImage preview = set.getSpriteBase();
			if (preview != null) {
				Render2D.dibujarImagen(g, preview, this.areaSelectorTerreno.x + 2, this.areaSelectorTerreno.y + 1);
			}
		}

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
		Render2D.dibujarStringConSombra(g, tipoActual.getNombre(), this.areaSelectorTerreno.x + 22,
				this.areaSelectorTerreno.y + 13, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);

		// 4. Componentes y Botones
		this.ctAncho.pintar(g);
		this.ctAlto.pintar(g);
		this.botonCrear.pintar(g);
		this.botonVolver.pintar(g);

		this.pintarGuiaControles(g);
	}
}