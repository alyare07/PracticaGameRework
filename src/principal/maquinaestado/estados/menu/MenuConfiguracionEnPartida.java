package principal.maquinaestado.estados.menu;

import principal.maquinaestado.GestorEstados;

public class MenuConfiguracionEnPartida extends MenuConfiguracion {

	public MenuConfiguracionEnPartida(GestorEstados ge) {
		super(ge);
		this.btnVolver.establecerAccion(() ->{
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_PARTIDA);
		});
	}

}
