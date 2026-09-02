package principal.maquinaestado.estados.menu;

import java.awt.Color;

import principal.maquinaestado.GestorEstados;

public class MenuPrincipal extends Menu {

	public MenuPrincipal(final GestorEstados ge) {
		super(ge, "PROYECTO RPG");
		this.subtituloMenu = "- MOTOR 2D -";
		this.colorFondo = new Color(10, 12, 16, 255);
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		this.agregarBoton("Iniciar Partida", () -> {
			this.GE.iniciarPartidaNueva();
		});

		this.agregarBoton("Cargar Partida", () -> {
			this.GE.seleccionarMundo();
		});

		this.agregarBoton("Configuracion", () -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES);
		});

		this.agregarBoton("Editor de Mapas", () -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_EDITOR_MAPA);
		});

		this.agregarBoton("Salir", () -> {
			System.exit(0);
		});

		this.establecerIndiceEnfocado(0);
	}
}