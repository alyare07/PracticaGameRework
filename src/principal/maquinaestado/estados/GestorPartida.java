package principal.maquinaestado.estados;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.mapa.Mundo;
import principal.mapa.mapas.MapaManager;
import principal.maquinaestado.GestorEstados;
import principal.maquinaestado.estados.menu.MenuPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCargaMapa;
import principal.maquinaestado.estados.pantallaCarga.PantallaCarga;
import principal.utilidades.Globales;

public class GestorPartida implements EstadoJuego {
	@SuppressWarnings("unused")
	private final GestorEstados GE;
	private final GestorJuego GJ;
	private final MenuPartida MP;
	private EstadoJuego estadoActivo;
	private final GestorCargaMapa GCJ = new GestorCargaMapa();
	private final BufferedImage FONDO_CARGA = Globales.FUNCIONES.CARGADOR_RECURSOS
			.cargarImagenCompatibleOpaca("/imagenes/FondoCarga.png");

	public GestorPartida(final GestorEstados ge) {
		this.GE = ge;
		this.GJ = new GestorJuego(ge, this);
		MapaManager.setGestorPartida(this);
		this.GCJ.cargar(this.GJ, this.GCJ, MapaManager.MAPA_1, true, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
//		this.GJ.partidaNueva("escenario1.json");
		this.MP = new MenuPartida(ge, this);
		this.estadoActivo = new PantallaCarga(this.GCJ, this.FONDO_CARGA);
	}

	public GestorPartida(final GestorEstados ge, final String mapa, final boolean reset) {
		this.GE = ge;
		this.GJ = new GestorJuego(ge, this);
		this.GCJ.cargar(this.GJ, this.GCJ, mapa, reset, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
		this.MP = new MenuPartida(ge, this);
		this.estadoActivo = this.GJ;
	}

	@Override
	public void actualizar() {
		if (this.estadoActivo instanceof PantallaCarga) {
			if (this.GCJ.isCompleto()) {
				final PantallaCarga pc = (PantallaCarga) this.estadoActivo;
				pc.disposeMundo();
				this.estadoActivo = this.GJ;
			}
		}
		this.estadoActivo.actualizar();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.estadoActivo.pintar(g);
	}

	public void establecerEstadoActivoJuego() {
		this.estadoActivo = this.GJ;
	}

	public void establecerEstadoActivoMenu() {
		this.estadoActivo = this.MP;
	}

	public EstadoJuego getEstadoActivo() {
		return this.estadoActivo;
	}

	public GestorJuego getGestorJuego() {
		return this.GJ;
	}

	public void cambiarMundo(final String nombreMapa, final String nombreSpawn) {
		this.GCJ.cargar(this.GJ, this.GCJ, nombreMapa, false, nombreSpawn);
		this.estadoActivo = new PantallaCarga(this.GCJ, this.FONDO_CARGA);
	}

	public void reiniciar() {
		this.GCJ.cargar(this.GJ, this.GCJ, MapaManager.MAPA_1, true, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
		this.estadoActivo = new PantallaCarga(this.GCJ, this.FONDO_CARGA);
	}

}
