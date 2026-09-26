package principal.maquinaestado.estados.menu;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import principal.mapa.mapas.ManifiestoMapa;
import principal.maquinaestado.GestorEstados;

public class MenuEdirorSeleccion extends Menu {

	public MenuEdirorSeleccion(final GestorEstados ge) {
		super(ge, "EDITOR DE ESCENARIOS");
		this.subtituloMenu = "- PROYECTOS DE MAPA -";
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		this.agregarBoton("Nuevo Proyecto", () -> {
			this.GE.editorMapaNuevoMenu();
		});

		this.agregarBoton("Abrir Proyecto", () -> {
			this.abrirSelectorProyecto();
		});

		this.agregarBoton("Volver", () -> {
			this.alPresionarEscape();
		});

		this.establecerIndiceEnfocado(0);
	}

	private void abrirSelectorProyecto() {
		File carpetaMapas = new File("mapas");
		if (!carpetaMapas.exists()) {
			carpetaMapas = new File("mundos");
			if (!carpetaMapas.exists()) {
				carpetaMapas.mkdirs();
			}
		}

		final JFileChooser selector = new JFileChooser(carpetaMapas);
		selector.setDialogTitle("Seleccionar Proyecto de Mapa (mapa.mp)");
		selector.setFileFilter(new FileNameExtensionFilter("Manifiesto de Mapa (*.mp, *.json)", "mp", "json"));
		selector.setApproveButtonText("Cargar Proyecto");

		final int resultado = selector.showOpenDialog(null);
		if ((resultado == JFileChooser.APPROVE_OPTION) && (selector.getSelectedFile() != null)) {
			final File archivoSeleccionado = selector.getSelectedFile();
			final File directorioProyecto = archivoSeleccionado.isDirectory() ? archivoSeleccionado
					: archivoSeleccionado.getParentFile();

			final ManifiestoMapa manifiesto = ManifiestoMapa.cargarDesdeDirectorio(directorioProyecto);
			if (manifiesto != null) {
				final String submundoInicial = manifiesto.getMundoComienzo();
				this.GE.editorMapa(directorioProyecto, submundoInicial);
			} else {
				JOptionPane.showMessageDialog(null,
						"No se encontró un manifiesto válido (mapa.mp) en:\n" + directorioProyecto.getAbsolutePath(),
						"Error al abrir proyecto", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		this.GE.disposeEditor();
	}
}