package principal.graficos;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

import principal.utilidades.Globales;

public class Ventana extends JFrame {
	private static final long serialVersionUID = 5979421777239930009L;
	private final String titulo;
//	private final ImageIcon icono;

	public Ventana(final String titulo, final SuperficieDibujo sd) {
		this.titulo = titulo;
//		BufferedImage imagen = CargadorRecursos.cargarImagenCompatibleTranslucida(Constantes.RUTA_ICONO_VENTANA);
//		this.icono = new ImageIcon(imagen);
		this.configurarVentana(sd);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent evt) {
				Globales.ANCHO_PANTALLA_COMPLETA = evt.getComponent().getWidth();
				Globales.ALTO_PANTALLA_COMPLETA = evt.getComponent().getHeight();
				Globales.actualizarFactorEscalado();
			}
		});
	}

	private void configurarVentana(final SuperficieDibujo sd) {
		this.setTitle(this.titulo);
//		setIconImage(icono.getImage());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setResizable(false);
		this.setLayout(new BorderLayout());
		this.add(sd, BorderLayout.CENTER);
		this.setUndecorated(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
