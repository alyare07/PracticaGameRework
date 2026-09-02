package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.maquinaestado.GestorEstados;

public class MenuConfiguracionEnPartida extends MenuConfiguracion {

	public MenuConfiguracionEnPartida(final GestorEstados ge) {
		super(ge);
		this.colorFondo = new Color(6, 8, 12, 210);
	}

	@Override
	protected void alPresionarEscape() {
		this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_PARTIDA);
		if (this.GE.getEstadoActual() instanceof principal.maquinaestado.estados.GestorPartida) {
			((principal.maquinaestado.estados.GestorPartida) this.GE.getEstadoActual()).establecerEstadoActivoMenu();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.GE.getEstadoActual() instanceof principal.maquinaestado.estados.GestorPartida) {
			final principal.maquinaestado.estados.GestorPartida gp = (principal.maquinaestado.estados.GestorPartida) this.GE
					.getEstadoActual();
			if (gp.getGestorJuego() != null) {
				gp.getGestorJuego().pintar(g);
			}
		}
		super.pintar(g);
	}
}