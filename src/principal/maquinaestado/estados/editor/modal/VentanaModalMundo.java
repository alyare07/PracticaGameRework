package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.controles.Raton;
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.musica.IDMusica;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Ventana modal centrada para configurar los metadatos del mundo (Música, Bioma,
 * Clima inicial e Iluminación de Cueva/Interior). Bloquea la entrada al mapa mientras está abierta.
 * 
 * @version 1.0 (Vanilla Java 8 - Dedicated World Inspector)
 */
public class VentanaModalMundo extends ComponenteMenu {

	private static final int ANCHO_MODAL = 340;
	private static final int ALTO_MODAL = 210;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(220, 180, 50); // Oro
	private static final Color COLOR_BORDE_SOMBRA = new Color(8, 10, 14);

	private MetadatosEscenario metadatos;
	private boolean abierta = false;

	// Índices seleccionados
	private int idxMusica = 0;
	private int idxBioma = 0;
	private int idxClima = 0;
	private boolean esInterior = false;

	private final Rectangle areaBtnMusica = new Rectangle();
	private final Rectangle areaBtnBioma = new Rectangle();
	private final Rectangle areaBtnClima = new Rectangle();
	private final Rectangle areaBtnInterior = new Rectangle();

	private BotonPixel btnAceptar;
	private BotonPixel btnCerrar;

	public VentanaModalMundo() {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.areaBtnMusica.setBounds(x + 130, y + 40, 190, 18);
		this.areaBtnBioma.setBounds(x + 130, y + 68, 190, 18);
		this.areaBtnClima.setBounds(x + 130, y + 96, 190, 18);
		this.areaBtnInterior.setBounds(x + 130, y + 124, 190, 18);

		this.btnAceptar = new BotonPixel("Aplicar", new Rectangle(x + 40, y + ALTO_MODAL - 32, 110, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cancelar", new Rectangle(x + 190, y + ALTO_MODAL - 32, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final MetadatosEscenario metadatosActuales) {
		this.metadatos = (metadatosActuales != null) ? metadatosActuales : new MetadatosEscenario();
		this.idxMusica = (this.metadatos.getMusicaFondo() != null) ? this.metadatos.getMusicaFondo().ordinal() : 0;
		this.idxBioma = (this.metadatos.getPerfilBioma() != null) ? this.metadatos.getPerfilBioma().ordinal() : 0;
		this.idxClima = (this.metadatos.getClimaInicial() != null) ? this.metadatos.getClimaInicial().ordinal() : 0;
		this.esInterior = this.metadatos.isEsInteriorCueva();

		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
	}

	private void guardarCambios() {
		if (this.metadatos != null) {
			this.metadatos.setMusicaFondo(IDMusica.values()[this.idxMusica]);
			this.metadatos.setPerfilBioma(PerfilClima.values()[this.idxBioma]);
			this.metadatos.setClimaInicial(TipoClima.values()[this.idxClima]);
			this.metadatos.setEsInteriorCueva(this.esInterior);

			// Notifica en caliente a los subsistemas si están activos
			if (Globales.GESTOR_CLIMA != null) {
				Globales.GESTOR_CLIMA.setPerfilBioma(this.metadatos.getPerfilBioma());
				Globales.GESTOR_CLIMA.setClima(this.metadatos.getClimaInicial(), 0.0);
			}
			if (Globales.GESTOR_LUZ != null) {
				if (this.esInterior) {
					Globales.GESTOR_LUZ.establecerAmbienteTransicion(new Color(0, 0, 0, 255), 0.0);
				} else {
					Globales.GESTOR_LUZ.restablecerModoExterior();
				}
			}
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || raton == null) {
			return;
		}

		if (raton.presionadoClickIzqUnicaAct()) {
			final Point p = raton.getPuntoPosicionEscalado();

			if (this.areaBtnMusica.contains(p)) {
				this.idxMusica = (this.idxMusica + 1) % IDMusica.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnBioma.contains(p)) {
				this.idxBioma = (this.idxBioma + 1) % PerfilClima.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnClima.contains(p)) {
				this.idxClima = (this.idxClima + 1) % TipoClima.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnInterior.contains(p)) {
				this.esInterior = !this.esInterior;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			}
		}

		this.btnAceptar.actualizar(raton);
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

		// 1. Fondo sombreado y marco ornamental
		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_BORDE_SOMBRA);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		// 2. Título de cabecera en m5x7
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "CONFIGURACION DEL MUNDO";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 22, new Color(255, 235, 180), Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		// 3. Etiquetas de las filas
		Render2D.dibujarStringConSombra(g, "Musica de Fondo:", x + 16, y + 54, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Bioma / Clima Base:", x + 16, y + 82, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Clima Inicial:", x + 16, y + 110, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Tipo de Espacio:", x + 16, y + 138, Color.WHITE, Color.BLACK);

		// 4. Cajas de selección interactiva
		this.pintarBotonSelector(g, this.areaBtnMusica, IDMusica.values()[this.idxMusica].name());
		this.pintarBotonSelector(g, this.areaBtnBioma, PerfilClima.values()[this.idxBioma].getNombreVisible());
		this.pintarBotonSelector(g, this.areaBtnClima, TipoClima.values()[this.idxClima].getNombre());
		this.pintarBotonSelector(g, this.areaBtnInterior, this.esInterior ? "[Cueva / Interior]" : "[Exterior con Sol]");

		// 5. Botones de acción
		this.btnAceptar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	private void pintarBotonSelector(final Graphics2D g, final Rectangle r, final String valor) {
		Render2D.dibujarRectanguloRelleno(g, r, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, r, new Color(75, 80, 95));

		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, valor);
		final int tx = r.x + ((r.width - ancho) / 2);
		final int ty = r.y + 13;

		Render2D.dibujarStringConSombra(g, valor, tx, ty, new Color(220, 180, 50), Color.BLACK);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}