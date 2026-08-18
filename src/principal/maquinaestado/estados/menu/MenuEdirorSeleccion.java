package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.Boton;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Globales;

public class MenuEdirorSeleccion extends Menu {
	protected boolean cargandoMapa;
	protected int contadorPuntosCarga;
	protected int contador;
	protected String textoCarga = "Cargando mapa.";

	public MenuEdirorSeleccion(final GestorEstados ge) {
		super(ge);
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!this.cargandoMapa) {
			super.pintar(g);
		} else {
			this.pintarAvisoCarga(g);
		}
	}

	@Override
	public void actualizar() {
		if (!this.cargandoMapa) {
			super.actualizar();
		} else {
			if (this.textoCarga == null) {
				this.textoCarga = "Cargando mapa.";
			}
			if ((this.contador % 60) == 0) {
				this.textoCarga += ".";
				this.contadorPuntosCarga++;
				if (this.contadorPuntosCarga > 3) {
					this.contadorPuntosCarga = 0;
					this.textoCarga = "Cargando mapa.";
				}
			}
			this.contador++;
		}
	}

	@Override
	protected void inicializarBotones() {
		final int anchoBoton = 380;
		final int altoBoton = 45;
		final int xBoton = (this.DIMENSION.width / 2) - (anchoBoton / 2);

		final Boton nuevoMapa = new Boton("Nuevo Mapa", Color.gray, new Rectangle(xBoton, 100, anchoBoton, altoBoton));
		nuevoMapa.establecerAccion(() -> {
			this.GE.editorMapaNuevoMenu();
			this.accionPostClick();
		});
		final Boton mapaExistente = new Boton("Abrir Mapa", Color.gray,
				new Rectangle(xBoton, 160, anchoBoton, altoBoton));
		mapaExistente.establecerAccion(() -> {
			this.abrirMapa();
		});

		final Boton volver = new Boton("Volver", Color.gray, new Rectangle(xBoton, 220, anchoBoton, altoBoton));
		volver.establecerAccion(() -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
			this.GE.disposeEditor();
			this.accionPostClick();
		});

		this.COMPONENTES.add(nuevoMapa);
		this.COMPONENTES.add(mapaExistente);
		this.COMPONENTES.add(volver);
	}

	private void pintarAvisoCarga(final Graphics2D g) {
		g.setFont(g.getFont().deriveFont(18));

		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.textoCarga);
		final int alto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, this.textoCarga);
		final int puntoX = (this.DIMENSION.width / 2) - (ancho / 2);
		final int puntoY = (this.DIMENSION.height / 2) + (alto / 2);
		DibujoDebug.dibujarString(g, this.textoCarga, puntoX, puntoY, Color.YELLOW);
		g.setFont(g.getFont().deriveFont(Globales.CONSTANTES.TAMANO_FUENTE));

	}

	private void abrirMapa() {
		final JFileChooser selector = new JFileChooser(new File("./mapas/"));
		selector.setFileFilter(new FileNameExtensionFilter("Mapa", "mp"));
		selector.setApproveButtonText("Seleccionar");
		final int seleccion = selector.showOpenDialog(null);
		if (seleccion == JFileChooser.APPROVE_OPTION) {
			this.cargandoMapa = true;
			final Thread hilo = new Thread(() -> {
				this.GE.editorMapa(EscenarioLoader
						.importarEscenario(new File(selector.getSelectedFile().getAbsolutePath())).getTerreno());
			});
			hilo.start();

		}

	}

}
