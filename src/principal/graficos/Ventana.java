package principal.graficos;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

import principal.utilidades.Constantes;

public class Ventana extends JFrame {
	private static final long serialVersionUID = 5979421777239930009L;
	private String titulo;
//	private final ImageIcon icono;

	public Ventana(final String titulo, final SuperficieDibujo sd) {
		this.titulo = titulo;
//		BufferedImage imagen = CargadorRecursos.cargarImagenCompatibleTranslucida(Constantes.RUTA_ICONO_VENTANA);
//		this.icono = new ImageIcon(imagen);
		configurarVentana(sd);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				Constantes.ANCHO_PANTALLA_COMPLETA = evt.getComponent().getWidth();
				Constantes.ALTO_PANTALLA_COMPLETA = evt.getComponent().getHeight();
				Constantes.actualizarFactorEscalado();
			}
		});
	}

	private void configurarVentana(SuperficieDibujo sd) {
//		getContentPane().addComponentListener(new ComponentListener() {
//			@Override
//			public void componentResized(ComponentEvent e) {
//				Component c = (Component) e.getSource();
//				Constantes.ANCHO_PANTALLA_COMPLETA = c.getWidth();
//				Constantes.ALTO_PANTALLA_COMPLETA = c.getWidth();
//				Constantes.actualizarFactorEscalado();
//			}
//
//			@Override
//			public void componentMoved(ComponentEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void componentShown(ComponentEvent e) {
//				Component c = (Component) e.getSource();
//				Constantes.ANCHO_PANTALLA_COMPLETA = c.getWidth();
//				Constantes.ALTO_PANTALLA_COMPLETA = c.getWidth();
//				Constantes.actualizarFactorEscalado();
//
//			}
//
//			@Override
//			public void componentHidden(ComponentEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});
		setTitle(titulo);
//		setIconImage(icono.getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());
		add(sd, BorderLayout.CENTER);
		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
