package principal.maquinaestado.estados.editor.modal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.maquinaestado.estados.menu.herramientas.BotonPixel;
import principal.maquinaestado.estados.menu.herramientas.ComponenteMenu;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Modal de seguridad que previene la pérdida de cambios al salir del editor.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class VentanaModalConfirmarSalir extends ComponenteMenu {

	private static final int ANCHO_MODAL = 300;
	private static final int ALTO_MODAL = 140;

	private static final Color COLOR_FONDO = new Color(18, 22, 30, 250);
	private static final Color COLOR_BORDE = new Color(255, 180, 50); // Alerta

	private boolean abierta = false;
	private final Runnable accionGuardarYSalir;
	private final Runnable accionSalirSinGuardar;

	private BotonPixel btnGuardarSalir;
	private BotonPixel btnSalirSinGuardar;
	private BotonPixel btnCancelar;

	public VentanaModalConfirmarSalir(final Runnable accionGuardarYSalir, final Runnable accionSalirSinGuardar) {
		super(new Rectangle(Constantes.CENTROX - (ANCHO_MODAL / 2), Constantes.CENTROY - (ALTO_MODAL / 2), ANCHO_MODAL,
				ALTO_MODAL));
		this.accionGuardarYSalir = accionGuardarYSalir;
		this.accionSalirSinGuardar = accionSalirSinGuardar;
		this.inicializarComponentes();
	}

	private void inicializarComponentes() {
		final int x = this.area.x;
		final int y = this.area.y;

		this.btnGuardarSalir = new BotonPixel("Guardar y Salir", new Rectangle(x + 15, y + 60, 125, 18), () -> {
			this.cerrar();
			if (this.accionGuardarYSalir != null) {
				this.accionGuardarYSalir.run();
			}
		});

		this.btnSalirSinGuardar = new BotonPixel("Salir sin Guardar", new Rectangle(x + 155, y + 60, 130, 18), () -> {
			this.cerrar();
			if (this.accionSalirSinGuardar != null) {
				this.accionSalirSinGuardar.run();
			}
		});

		this.btnCancelar = new BotonPixel("Cancelar", new Rectangle(x + 95, y + 95, 110, 18), () -> {
			this.cerrar();
		});
	}

	public void abrir() {
		this.abierta = true;
		this.visible = true;
		GestorSonido.reproducir(IDSonido.GOLPE_1);
	}

	public void cerrar() {
		this.abierta = false;
		this.visible = false;
	}

	@Override
	public void actualizar(final Raton raton) {
		if (!this.abierta || raton == null) {
			return;
		}
		this.btnGuardarSalir.actualizar(raton);
		this.btnSalirSinGuardar.actualizar(raton);
		this.btnCancelar.actualizar(raton);
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

		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, new Color(0, 0, 0, 190));
		Render2D.dibujarRectanguloRelleno(g, x, y, w, h, COLOR_FONDO);
		Render2D.dibujarRectanguloContorno(g, x, y, w, h, COLOR_BORDE);

		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 16f));

		final String titulo = "SALIR DEL EDITOR";
		final int anchoTit = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, titulo);
		Render2D.dibujarStringConSombra(g, titulo, x + ((w - anchoTit) / 2), y + 22, new Color(255, 200, 60), Color.BLACK);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.PLAIN, 12f));
		final String sub = "¿Deseas guardar los cambios antes de salir?";
		final int anchoSub = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, sub);
		Render2D.dibujarStringConSombra(g, sub, x + ((w - anchoSub) / 2), y + 42, Color.LIGHT_GRAY, Color.BLACK);

		this.btnGuardarSalir.pintar(g);
		this.btnSalirSinGuardar.pintar(g);
		this.btnCancelar.pintar(g);

		g.setFont(fontPrevia);
	}

	public boolean isAbierta() {
		return this.abierta;
	}
}