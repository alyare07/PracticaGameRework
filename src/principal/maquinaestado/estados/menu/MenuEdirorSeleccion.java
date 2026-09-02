package principal.maquinaestado.estados.menu;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.GestorEstados;

public class MenuEdirorSeleccion extends Menu {

	public MenuEdirorSeleccion(final GestorEstados ge) {
		super(ge, "EDITOR DE ESCENARIOS");
		this.subtituloMenu = "- GESTION DE MAPAS -";
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		this.agregarBoton("Nuevo Mapa", () -> {
			this.GE.editorMapaNuevoMenu();
		});

		this.agregarBoton("Abrir Mapa", () -> {
			this.abrirSelectorArchivo();
		});

		this.agregarBoton("Volver", () -> {
			this.alPresionarEscape();
		});

		this.establecerIndiceEnfocado(0);
	}

	private void abrirSelectorArchivo() {
		final File carpetaMundos = new File("mundos");
		if (!carpetaMundos.exists()) {
			carpetaMundos.mkdirs();
		}

		final JFileChooser selector = new JFileChooser(carpetaMundos);
		selector.setFileFilter(new FileNameExtensionFilter("Mapas (*.mp, *.json)", "mp", "json", "esc"));
		selector.setApproveButtonText("Abrir");

		final int resultado = selector.showOpenDialog(null);
		if ((resultado == JFileChooser.APPROVE_OPTION) && (selector.getSelectedFile() != null)) {
			final File archivo = selector.getSelectedFile();
			final Thread hilo = new Thread(() -> {
				this.GE.editorMapa(EscenarioLoader.importarEscenario(archivo).getTerreno());
			});
			hilo.start();
		}
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		this.GE.disposeEditor();
	}
}