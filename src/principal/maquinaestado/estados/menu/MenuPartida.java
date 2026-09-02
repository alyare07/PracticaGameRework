package principal.maquinaestado.estados.menu;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.GestorPartida;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

public class MenuPartida extends Menu {

	protected final GestorPartida GP;

	public MenuPartida(final GestorEstados ge, final GestorPartida gp) {
		super(ge, "PAUSA");
		this.GP = gp;
		this.subtituloMenu = "- PARTIDA EN CURSO -";
		this.colorFondo = new Color(6, 8, 12, 200);
		this.inicializarMenu();
	}

	@Override
	protected void inicializarMenu() {
		this.componentes.clear();
		this.botones.clear();

		this.agregarBoton("Continuar", () -> {
			if (this.GP != null) {
				this.GP.establecerEstadoActivoJuego();
			}
		});

		this.agregarBoton("Guardar Partida", () -> {
			if ((this.GP != null) && (this.GP.getGestorJuego().getMapa() != null)) {
				MapaManager.guardarMapaEnTemp(this.GP.getGestorJuego().getMapa());
				Globales.GESTOR_TEXTOS.agregarTexto("Partida Guardada", Constantes.CENTROX, Constantes.CENTROY - 40,
						principal.igu.textos.TipoTextoFlotante.ORO_EXP);
			}
		});

		this.agregarBoton("Configuracion", () -> {
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA);
		});

		this.agregarBoton("Salir al Menu", () -> {
			MapaManager.vaciarTemp();
			Globales.CAMARA.reiniciarZoom();
			this.GE.establecerEstadoActual(GestorEstados.NUMERO_ESTADO_MENU);
			this.GE.disposePartida();
		});

		this.establecerIndiceEnfocado(0);
	}

	@Override
	protected void alPresionarEscape() {
		if (this.GP != null) {
			this.GP.establecerEstadoActivoJuego();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if ((this.GP != null) && (this.GP.getGestorJuego() != null)) {
			this.GP.getGestorJuego().pintar(g);
		}
		super.pintar(g);
	}
}