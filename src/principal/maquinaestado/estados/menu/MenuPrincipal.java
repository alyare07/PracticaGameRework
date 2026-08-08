package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.herramientas.Boton;
import principal.utilidades.Constantes;

public class MenuPrincipal extends Menu{

    public MenuPrincipal(final GestorEstados ge) {
	super(ge);
    }

    @Override
    protected void inicializarBotones() {
	final int anchoBoton = 380;
	final int altoBoton = 45;
	final int xBoton = (this.DIMENSION.width / 2) - (anchoBoton / 2);
	final Boton b1 = new Boton("Iniciar Partida", Color.gray, new Rectangle(xBoton, 100, anchoBoton, altoBoton));
	b1.establecerAccion(() -> {
	    this.GE.iniciarPartidaNueva();
//			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_PARTIDA);
	});

	final Boton b2 = new Boton("Cargar Partida", Color.gray, new Rectangle(xBoton, 160, anchoBoton, altoBoton));
	b2.establecerAccion(() -> {
	    this.GE.seleccionarMundo();
	    this.accionPostClick();
	});

	final Boton b3 = new Boton("Configuracion", Color.gray, new Rectangle(xBoton, 220, anchoBoton, altoBoton));

	b3.establecerAccion(() -> {
	    this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES);
	});

	final Boton b4 = new Boton("Editor", Color.gray, new Rectangle((xBoton / 2) - ((anchoBoton / 8)), 220, anchoBoton / 4, altoBoton));
	b4.establecerAccion(() -> {
	    this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_EDITOR_MAPA);
	});

	final Boton b5 = new Boton("Salir", Color.gray, new Rectangle(xBoton, 280, anchoBoton, altoBoton));
	b5.establecerAccion(() -> {
	    Constantes.FUNCIONES.TEMP_MANAGER.eliminarTemp();
	    System.exit(0);
	});
	this.COMPONENTES.add(b1);
	this.COMPONENTES.add(b2);
	this.COMPONENTES.add(b3);
	this.COMPONENTES.add(b4);
	this.COMPONENTES.add(b5);
    }

}
