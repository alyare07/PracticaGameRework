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
import principal.maquinaestado.estados.editor.metadatos.MetadatosEscenario.TipoAmbiente;
import principal.maquinaestado.estados.editor.metadatos.TipoIluminacionInterior;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.musica.IDMusica;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Inspector modal interactivo para configurar los metadatos del mundo en el
 * editor (Música, Bioma, Clima inicial, Tipo de Ambiente y Estilo de Luz para
 * Interiores).
 * 
 * @version 2.0 (Vanilla Java 8 - Dedicated Atmosphere & Interior Lighting
 *          Inspector)
 */
public class VentanaModalMundo extends ComponenteMenu {

	private static final int ANCHO_MODAL = 350;
	private static final int ALTO_MODAL = 235;

	private static final Color COLOR_FONDO = new Color(16, 20, 28, 245);
	private static final Color COLOR_BORDE = new Color(220, 180, 50); // Oro
	private static final Color COLOR_BORDE_SOMBRA = new Color(8, 10, 14);

	private MetadatosEscenario metadatos;
	private boolean abierta = false;

	// Índices seleccionados
	private int idxMusica = 0;
	private int idxBioma = 0;
	private int idxClima = 0;
	private int idxAmbiente = 0;
	private int idxIluminacion = 0;

	private final Rectangle areaBtnMusica = new Rectangle();
	private final Rectangle areaBtnBioma = new Rectangle();
	private final Rectangle areaBtnClima = new Rectangle();
	private final Rectangle areaBtnAmbiente = new Rectangle();
	private final Rectangle areaBtnIluminacion = new Rectangle();

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

		this.areaBtnMusica.setBounds(x + 135, y + 36, 195, 18);
		this.areaBtnBioma.setBounds(x + 135, y + 62, 195, 18);
		this.areaBtnClima.setBounds(x + 135, y + 88, 195, 18);
		this.areaBtnAmbiente.setBounds(x + 135, y + 114, 195, 18);
		this.areaBtnIluminacion.setBounds(x + 135, y + 140, 195, 18);

		this.btnAceptar = new BotonPixel("Aplicar", new Rectangle(x + 40, (y + ALTO_MODAL) - 30, 110, 18), () -> {
			this.guardarCambios();
			this.cerrar();
		});

		this.btnCerrar = new BotonPixel("Cancelar", new Rectangle(x + 200, (y + ALTO_MODAL) - 30, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir(final MetadatosEscenario metadatosActuales) {
		this.metadatos = (metadatosActuales != null) ? metadatosActuales : new MetadatosEscenario();
		this.idxMusica = (this.metadatos.getMusicaFondo() != null) ? this.metadatos.getMusicaFondo().ordinal() : 0;
		this.idxBioma = (this.metadatos.getPerfilBioma() != null) ? this.metadatos.getPerfilBioma().ordinal() : 0;
		this.idxClima = (this.metadatos.getClimaInicial() != null) ? this.metadatos.getClimaInicial().ordinal() : 0;
		this.idxAmbiente = (this.metadatos.getTipoAmbiente() != null) ? this.metadatos.getTipoAmbiente().ordinal() : 0;
		this.idxIluminacion = (this.metadatos.getIluminacionInterior() != null)
				? this.metadatos.getIluminacionInterior().ordinal()
				: 0;

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
			this.metadatos.setTipoAmbiente(TipoAmbiente.values()[this.idxAmbiente]);
			this.metadatos.setIluminacionInterior(TipoIluminacionInterior.values()[this.idxIluminacion]);

			// Notifica en caliente a los subsistemas del motor
			if (Globales.GESTOR_CLIMA != null) {
				Globales.GESTOR_CLIMA.setPerfilBioma(this.metadatos.getPerfilBioma());
				Globales.GESTOR_CLIMA.setClima(this.metadatos.getClimaInicial(), 0.0);
			}

			if (Globales.GESTOR_LUZ != null) {
				if (this.metadatos.esCueva()) {
					Globales.GESTOR_LUZ.establecerModoCueva(true);
				} else if (this.metadatos.esInterior()) {
					Globales.GESTOR_LUZ.establecerAmbienteTransicion(this.metadatos.resolverColorLuzEfectivo(), 0.0);
				} else {
					Globales.GESTOR_LUZ.restablecerModoExterior();
				}
			}
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || (raton == null)) {
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
			} else if (this.areaBtnAmbiente.contains(p)) {
				this.idxAmbiente = (this.idxAmbiente + 1) % TipoAmbiente.values().length;
				GestorSonido.reproducir(IDSonido.GOLPE_1);
			} else if (this.areaBtnIluminacion.contains(p)
					&& (TipoAmbiente.values()[this.idxAmbiente] == TipoAmbiente.INTERIOR)) {
				this.idxIluminacion = (this.idxIluminacion + 1) % TipoIluminacionInterior.values().length;
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
		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO,
				new Color(0, 0, 0, 180));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x - 1, y - 1, w + 2, h + 2, COLOR_BORDE_SOMBRA);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "METADATOS Y ATMOSFERA DEL MUNDO";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 20, new Color(255, 235, 180),
				Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 14f));

		// 2. Etiquetas
		Render2D.dibujarStringConSombra(g, "Musica de Fondo:", x + 16, y + 49, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Bioma / Clima Base:", x + 16, y + 75, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Clima Inicial:", x + 16, y + 101, Color.WHITE, Color.BLACK);
		Render2D.dibujarStringConSombra(g, "Tipo de Ambiente:", x + 16, y + 127, Color.WHITE, Color.BLACK);

		final boolean esInteriorActual = (TipoAmbiente.values()[this.idxAmbiente] == TipoAmbiente.INTERIOR);
		final Color colorLabelIlum = esInteriorActual ? Color.WHITE : new Color(120, 125, 135);
		Render2D.dibujarStringConSombra(g, "Luz de Interior:", x + 16, y + 153, colorLabelIlum, Color.BLACK);

		// 3. Selectores interactivos
		this.pintarBotonSelector(g, this.areaBtnMusica, IDMusica.values()[this.idxMusica].name(),
				new Color(220, 180, 50));
		this.pintarBotonSelector(g, this.areaBtnBioma, PerfilClima.values()[this.idxBioma].getNombreVisible(),
				new Color(220, 180, 50));
		this.pintarBotonSelector(g, this.areaBtnClima, TipoClima.values()[this.idxClima].getNombre(),
				new Color(220, 180, 50));

		final TipoAmbiente amb = TipoAmbiente.values()[this.idxAmbiente];
		final Color cAmb = (amb == TipoAmbiente.EXTERIOR) ? new Color(100, 240, 120)
				: ((amb == TipoAmbiente.INTERIOR) ? new Color(255, 200, 60) : new Color(255, 80, 80));
		this.pintarBotonSelector(g, this.areaBtnAmbiente, amb.name(), cAmb);

		if (esInteriorActual) {
			final String txtIlum = TipoIluminacionInterior.values()[this.idxIluminacion].getNombreVisible();
			this.pintarBotonSelector(g, this.areaBtnIluminacion, txtIlum, new Color(255, 215, 140));
		} else {
			this.pintarBotonSelector(g, this.areaBtnIluminacion, "[N/A - Solo Interiores]", new Color(90, 95, 105));
		}

		// 4. Botones
		this.btnAceptar.pintar(g);
		this.btnCerrar.pintar(g);

		g.setFont(fontPrevia);
	}

	private void pintarBotonSelector(final Graphics2D g, final Rectangle r, final String valor,
			final Color colorTexto) {
		Render2D.dibujarRectanguloRelleno(g, r, new Color(28, 35, 48));
		Render2D.dibujarRectanguloContorno(g, r, new Color(75, 80, 95));

		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, valor);
		final int tx = r.x + ((r.width - ancho) / 2);
		final int ty = r.y + 13;

		Render2D.dibujarStringConSombra(g, valor, tx, ty, colorTexto, Color.BLACK);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}