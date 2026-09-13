package principal.maquinaestado.estados.menu;

import java.awt.Color;

import principal.maquinaestado.GestorEstados;

/**
 * Hub central de configuración para navegar entre Video/Rendimiento y Controles.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class MenuConfiguracionSeleccion extends Menu {

	public MenuConfiguracionSeleccion(final GestorEstados ge) {
		super(ge, "CONFIGURACION");
		this.subtituloMenu = "- AJUSTES DEL SISTEMA -";
		this.colorFondo = new Color(10, 12, 16, 255);
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		this.agregarBoton("Video y Rendimiento", () -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES_GRAFICAS);
		});

		this.agregarBoton("Controles y Teclas", () -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES_CONTROLES);
		});

		this.agregarBoton("Volver", () -> {
			this.alPresionarEscape();
		});

		this.establecerIndiceEnfocado(0);
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
	}
}