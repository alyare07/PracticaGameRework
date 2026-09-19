package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.GestorPartida;

/**
 * Hub central de configuración con jerarquía ordenada: General (Audio), Video y
 * Controles.
 * 
 * @version 2.0 (Vanilla Java 8 - General Configuration Hub)
 */
public class MenuConfiguracionSeleccion extends Menu {

	private boolean esDesdePausa = false;

	public MenuConfiguracionSeleccion(final GestorEstados ge) {
		super(ge, "CONFIGURACION");
		this.subtituloMenu = "- AJUSTES DEL SISTEMA -";
		this.colorFondo = new Color(10, 12, 16, 255);
		this.inicializarMenu();
	}

	public void setEsDesdePausa(final boolean desdePausa) {
		this.esDesdePausa = desdePausa;
		this.colorFondo = desdePausa ? new Color(6, 8, 12, 210) : new Color(10, 12, 16, 255);
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		// 1. Configuración General (Audio / Sonido)
		this.agregarBoton("Configuracion General", () -> {
			this.GE.abrirMenuConfiguracionGeneral(this.esDesdePausa);
		});

		// 2. Video y Rendimiento
		this.agregarBoton("Video y Rendimiento", () -> {
			this.GE.abrirMenuConfiguracionGrafica(this.esDesdePausa);
		});

		// 3. Controles y Teclas
		this.agregarBoton("Controles y Teclas", () -> {
			this.GE.abrirMenuConfiguracionControles(this.esDesdePausa);
		});

		// 4. Volver
		this.agregarBoton("Volver", () -> {
			this.alPresionarEscape();
		});

		this.establecerIndiceEnfocado(0);
	}

	@Override
	protected void alPresionarEscape() {
		if (this.esDesdePausa) {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_PARTIDA);
			if (this.GE.getEstadoActual() instanceof GestorPartida) {
				((GestorPartida) this.GE.getEstadoActual()).establecerEstadoActivoMenu();
			}
		} else {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.esDesdePausa && (this.GE.getEstadoActual() instanceof GestorPartida)) {
			final GestorPartida gp = (GestorPartida) this.GE.getEstadoActual();
			if (gp.getGestorJuego() != null) {
				gp.getGestorJuego().pintar(g);
			}
		}
		super.pintar(g);
	}
}