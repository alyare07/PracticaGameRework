package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.iluminacion.IntensidadNiebla;
import principal.iluminacion.ZonaAmbiente;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Inspector modal para configurar Zonas de Ambiente con coordenadas (X, Y),
 * dimensiones (Ancho x Alto), nivel de niebla y tipo de espacio.
 * 
 * @version 3.0 (Vanilla Java 8 - Full Position & Size)
 */
public class VentanaModalAmbiente extends ComponenteMenu {

	private static final int ANCHO_MODAL = 340;
	private static final int ALTO_MODAL = 230;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(60, 220, 120);

	private ZonaAmbiente zonaSeleccionada;
	private boolean abierta = false;

	private CajaTextoPixel ctNombreZona;
	private CajaTextoPixel ctX;
	private CajaTextoPixel ctY;
	private CajaTextoPixel ctAncho;
	private CajaTextoPixel ctAlto;

	private int idxNiebla = 0;
	private boolean esInterior = false;

	private final Rectangle areaBtnNiebla = new Rectangle();
	private final Rectangle areaBtnInterior = new Rectangle();

	private BotonPixel btnGuardar;
	private BotonPixel btnCerrar;

	public VentanaModalAmbiente() {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.ctNombreZona = new CajaTextoPixel(new Rectangle(x + 130, y + 36, 190, 16), "Zona Bosque", 18, false);
		this.areaBtnNiebla.setBounds(x + 130, y + 60, 190, 18);
		this.areaBtnInterior.setBounds(x + 130, y + 86, 190, 18);

		this.ctX = new CajaTextoPixel(new Rectangle(x + 130, y + 112, 85, 16), "0", 6, true);
		this.ctY = new CajaTextoPixel(new Rectangle(x + 235, y + 112, 85, 16), "0", 6, true);

		this.ctAncho = new CajaTextoPixel(new Rectangle(x + 130, y + 136, 85, 16), "128", 5, true);
		this.ctAlto = new CajaTextoPixel(new Rectangle(x + 235, y + 136, 85, 16), "128", 5, true);

		this.btnGuardar = new BotonPixel("Guardar", new Rectangle(x + 35, (y + ALTO_MODAL) - 30, 110, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cerrar", new Rectangle(x + 195, (y + ALTO_MODAL) - 30, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final ZonaAmbiente zona) {
		if (zona == null) {
			return;
		}
		this.zonaSeleccionada = zona;
		this.ctNombreZona.setTexto(zona.getNombre());
		this.idxNiebla = (zona.getNivelNiebla() != null) ? zona.getNivelNiebla().ordinal() : 0;
		this.esInterior = zona.isEsInterior();

		final Rectangle r = zona.getLimites();
		this.ctX.setTexto(String.valueOf(r.x));
		this.ctY.setTexto(String.valueOf(r.y));
		this.ctAncho.setTexto(String.valueOf(r.width));
		this.ctAlto.setTexto(String.valueOf(r.height));

		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
		this.zonaSeleccionada = null;
	}

	private void guardarCambios() {
		if (this.zonaSeleccionada != null) {
			final int nx = this.ctX.getNumeroEntero(this.zonaSeleccionada.getLimites().x);
			final int ny = this.ctY.getNumeroEntero(this.zonaSeleccionada.getLimites().y);
			final int nw = Math.max(16, this.ctAncho.getNumeroEntero(64));
			final int nh = Math.max(16, this.ctAlto.getNumeroEntero(64));
			this.zonaSeleccionada.getLimites().setBounds(nx, ny, nw, nh);
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();
			if (this.areaBtnNiebla.contains(p)) {
				this.idxNiebla = (this.idxNiebla + 1) % IntensidadNiebla.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnInterior.contains(p)) {
				this.esInterior = !this.esInterior;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		this.ctNombreZona.actualizar(raton);
		this.ctX.actualizar(raton);
		this.ctY.actualizar(raton);
		this.ctAncho.actualizar(raton);
		this.ctAlto.actualizar(raton);
		this.btnGuardar.actualizar(raton);
		this.btnCerrar.actualizar(raton);
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.abierta) {
			return;
		}

		final int x = this.area.x;
		final int y = this.area.y;
		final int w = this.area.width;
		final int h = this.area.height;

		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO,
				new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "CONFIGURACION DE ZONA AMBIENTE";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 20, new Color(120, 240, 160),
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
		Render2D.dibujarStringConSombra(g, "Nombre Zona:", x + 16, y + 48, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Nivel Niebla:", x + 16, y + 72, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Tipo Espacio:", x + 16, y + 98, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Posición (X / Y):", x + 16, y + 124, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Tamaño (W / H):", x + 16, y + 148, Color.WHITE, Color.BLACK);

		this.ctNombreZona.pintar(g);

		// Selector Niebla
		Render2D.dibujarRectanguloRelleno(g, this.areaBtnNiebla, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, this.areaBtnNiebla, new Color(75, 80, 95));
		final String txtNiebla = IntensidadNiebla.values()[this.idxNiebla].name();
		final int anchoN = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtNiebla);
		Render2D.dibujarStringConSombra(g, txtNiebla, this.areaBtnNiebla.x + ((this.areaBtnNiebla.width - anchoN) / 2),
				this.areaBtnNiebla.y + 13, Color.WHITE, Color.BLACK);

		// Selector Interior
		Render2D.dibujarRectanguloRelleno(g, this.areaBtnInterior, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, this.areaBtnInterior, new Color(75, 80, 95));
		final String txtInt = this.esInterior ? "[Cueva / Interior]" : "[Exterior con Sol]";
		final int anchoI = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtInt);
		Render2D.dibujarStringConSombra(g, txtInt, this.areaBtnInterior.x + ((this.areaBtnInterior.width - anchoI) / 2),
				this.areaBtnInterior.y + 13, new Color(255, 200, 60), Color.BLACK);

		// Campos X, Y, Ancho, Alto
		this.ctX.pintar(g);
		this.ctY.pintar(g);
		this.ctAncho.pintar(g);
		this.ctAlto.pintar(g);

		this.btnGuardar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}