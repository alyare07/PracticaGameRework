package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Rectangle;

import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.menu.herramientas.Boton;
import principal.utilidades.Globales;

public class MenuPartida extends Menu {
	protected final GestorPartida GP;

	public MenuPartida(final GestorEstados ge, final GestorPartida gp) {
		super(ge);
		this.GP = gp;
	}

	@Override
	protected void inicializarBotones() {
		final int anchoBoton = 380;
		final int altoBoton = 45;
		final int xBoton = (this.DIMENSION.width / 2) - (anchoBoton / 2);
		final Boton b1 = new Boton("Continuar Partida", Color.gray, new Rectangle(xBoton, 100, anchoBoton, altoBoton));
		b1.establecerAccion(() -> {
			this.GP.establecerEstadoActivoJuego();
		});

		final Boton b2 = new Boton("Guardar Partida", Color.gray, new Rectangle(xBoton, 160, anchoBoton, altoBoton));

		final Boton b3 = new Boton("Configuracion", Color.gray, new Rectangle(xBoton, 220, anchoBoton, altoBoton));
		b3.establecerAccion(() -> {
			this.GE.establecerEstadoActual(this.GE.NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA);
		});

		final Boton b4 = new Boton("Salir de la Partida", Color.gray,
				new Rectangle(xBoton, 280, anchoBoton, altoBoton));
		b4.establecerAccion(() -> {
			MapaManager.vaciarTemp();
			Globales.FUNCIONES.TEMP_MANAGER.reiniciarTemp();
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
			this.GE.disposePartida();
			this.accionPostClick();
			// el click se mantiene cierto tiempo por lo que llega al menuprincipal y activa
			// la accion del boton salir de ese menu
		});
		this.COMPONENTES.add(b1);
		this.COMPONENTES.add(b2);
		this.COMPONENTES.add(b3);
		this.COMPONENTES.add(b4);
	}

}
