package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Inspector modal interactivo para configurar Fuentes de Luz estáticas en el mapa (Radio y Tipo).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class VentanaModalLuz extends ComponenteMenu {

	private static final int ANCHO_MODAL = 300;
	private static final int ALTO_MODAL = 160;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(255, 180, 50); // Oro/Luz

	private FuenteLuz luzSeleccionada;
	private boolean abierta = false;

	private int idxTipoLuz = 0;
	private final Rectangle areaBtnTipo = new Rectangle();
	private CajaTextoPixel ctRadio;

	private BotonPixel btnGuardar;
	private BotonPixel btnCerrar;

	public VentanaModalLuz() {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.areaBtnTipo.setBounds(x + 110, y + 42, 170, 18);
		this.ctRadio = new CajaTextoPixel(new Rectangle(x + 110, y + 70, 80, 16), "80", 4, true);

		this.btnGuardar = new BotonPixel("Guardar", new Rectangle(x + 25, y + ALTO_MODAL - 30, 110, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cerrar", new Rectangle(x + 165, y + ALTO_MODAL - 30, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final FuenteLuz luz) {
		if (luz == null) {
			return;
		}
		this.luzSeleccionada = luz;
		this.idxTipoLuz = (luz.getTipo() != null) ? luz.getTipo().ordinal() : 0;
		this.ctRadio.setTexto(String.valueOf((int) luz.getRadioActual()));

		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
		this.luzSeleccionada = null;
	}

	private void guardarCambios() {
		if (this.luzSeleccionada == null) {
			return;
		}
		final double radio = this.ctRadio.getNumeroEntero(80);
		final TipoLuz tipo = TipoLuz.values()[this.idxTipoLuz];
		this.luzSeleccionada.spawnFija(this.luzSeleccionada.getPosX(), this.luzSeleccionada.getPosY(), tipo, radio);
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || raton == null) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();
			if (this.areaBtnTipo.contains(p)) {
				this.idxTipoLuz = (this.idxTipoLuz + 1) % TipoLuz.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		this.ctRadio.actualizar(raton);
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

		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "CONFIGURACION DE LUZ";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 22, new Color(255, 215, 80), Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));
		Render2D.dibujarStringConSombra(g, "Tipo de Luz:", x + 16, y + 54, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Radio (px):", x + 16, y + 82, Color.WHITE, Color.BLACK);

		// Selector Tipo Luz
		Render2D.dibujarRectanguloRelleno(g, this.areaBtnTipo, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, this.areaBtnTipo, new Color(75, 80, 95));
		final String txtTipo = TipoLuz.values()[this.idxTipoLuz].name();
		final int anchoT = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txtTipo);
		Render2D.dibujarStringConSombra(g, txtTipo, this.areaBtnTipo.x + ((this.areaBtnTipo.width - anchoT) / 2),
				this.areaBtnTipo.y + 13, new Color(255, 180, 50), Color.BLACK);

		this.ctRadio.pintar(g);
		this.btnGuardar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}