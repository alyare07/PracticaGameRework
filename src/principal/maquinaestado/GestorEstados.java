package principal.maquinaestado;

import java.awt.Graphics2D;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;
import principal.maquinaestado.estados.EstadoJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.editor.EditorMapa;
import principal.maquinaestado.estados.menu.MenuCargarPartida;
import principal.maquinaestado.estados.menu.MenuConfiguracion;
import principal.maquinaestado.estados.menu.MenuConfiguracionGeneral;
import principal.maquinaestado.estados.menu.MenuConfiguracionGrafica;
import principal.maquinaestado.estados.menu.MenuConfiguracionSeleccion;
import principal.maquinaestado.estados.menu.MenuDificultad;
import principal.maquinaestado.estados.menu.MenuEdirorSeleccion;
import principal.maquinaestado.estados.menu.MenuEditorNuevo;
import principal.maquinaestado.estados.menu.MenuGuardarPartida;
import principal.maquinaestado.estados.menu.MenuPrincipal;
import principal.persistencia.GestorGuardado;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

public class GestorEstados {

	public static final int NUMERO_ESTADO_PRUEBA = -1;
	public static final int NUMERO_ESTADO_PARTIDA = 0;
	public static final int NUMERO_ESTADO_MENU = 1;
	public static final int NUMERO_ESTADO_EDITOR_MAPA = 2;
	public static final int NUMERO_ESTADO_MENU_EDITOR_MAPA = 3;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES = 4;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA = 5;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_GRAFICAS = 6;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_CONTROLES = 7;
	public static final int NUMERO_ESTADO_MENU_CARGAR_PARTIDA = 8;
	public static final int NUMERO_ESTADO_MENU_GUARDAR_PARTIDA = 9;
	public static final int NUMERO_ESTADO_MENU_CONFIGURACIONES_GENERAL = 10;
	public static final int NUMERO_ESTADO_MENU_DIFICULTAD = 11;

	private static final EstadoJuego ESTADO_VACIO = new EstadoJuego() {
		@Override
		public void pintar(final Graphics2D g) {
		}

		@Override
		public void actualizar() {
		}
	};

	private final EstadoJuego[] estados;
	private EstadoJuego estadoActual;

	public GestorEstados() {
		this.estados = new EstadoJuego[3];
		this.estados[1] = new MenuPrincipal(this);
		this.establecerEstadoActual(NUMERO_ESTADO_MENU);
	}

	public void pintar(final Graphics2D g) {
		if (this.estadoActual != null) {
			g.setFont(Globales.GESTOR_FUENTES.getFuente(12f));
			this.estadoActual.pintar(g);
		}
	}

	public void actualizar() {
		if (this.estadoActual != null) {
			this.estadoActual.actualizar();
		}
	}

	public void establecerEstadoActual(final int numeroEstado) {
		Globales.estadoJuego = (numeroEstado == NUMERO_ESTADO_PARTIDA);
		Globales.RATON.soltar();

		switch (numeroEstado) {
		case NUMERO_ESTADO_PARTIDA:
			this.estadoActual = this.estados[0];
			break;
		case NUMERO_ESTADO_MENU:
			if (this.estados.length > 1) {
				this.estadoActual = this.estados[1];
			}
			break;
		case NUMERO_ESTADO_EDITOR_MAPA:
			if (this.estados.length > 1) {
				this.editorMapaSeleccion();
			}
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES:
			final MenuConfiguracionSeleccion mSel = new MenuConfiguracionSeleccion(this);
			mSel.setEsDesdePausa(false);
			this.estadoActual = mSel;
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_GENERAL:
			final MenuConfiguracionGeneral mGen = new MenuConfiguracionGeneral(this);
			mGen.setEsDesdePausa(false);
			this.estadoActual = mGen;
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_GRAFICAS:
			this.estadoActual = new MenuConfiguracionGrafica(this);
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_CONTROLES:
			this.estadoActual = new MenuConfiguracion(this);
			break;
		case NUMERO_ESTADO_MENU_CONFIGURACIONES_EN_PARTIDA:
			final MenuConfiguracionSeleccion mSelPausa = new MenuConfiguracionSeleccion(this);
			mSelPausa.setEsDesdePausa(true);
			this.estadoActual = mSelPausa;
			break;
		case NUMERO_ESTADO_MENU_CARGAR_PARTIDA:
			final MenuCargarPartida mCargar = new MenuCargarPartida(this);
			mCargar.setEsDesdePausa(false);
			this.estadoActual = mCargar;
			break;
		case NUMERO_ESTADO_MENU_DIFICULTAD:
			this.estadoActual = new MenuDificultad(this);
			break;
		default:
			break;
		}
	}

	public void abrirMenuConfiguracionGeneral(final boolean esDesdePausa) {
		Globales.RATON.soltar();
		final MenuConfiguracionGeneral m = new MenuConfiguracionGeneral(this);
		m.setEsDesdePausa(esDesdePausa);
		this.estadoActual = m;
	}

	public void abrirMenuConfiguracionGrafica(final boolean esDesdePausa) {
		Globales.RATON.soltar();
		final MenuConfiguracionGrafica m = new MenuConfiguracionGrafica(this);
		m.setEsDesdePausa(esDesdePausa); // <--- CONECTAR ESTA LÍNEA
		this.estadoActual = m;
	}

	public void abrirMenuConfiguracionControles(final boolean esDesdePausa) {
		Globales.RATON.soltar();
		final MenuConfiguracion m = new MenuConfiguracion(this);
		this.estadoActual = m;
	}

	public void abrirMenuConfiguracionesSeleccionEnPausa() {
		Globales.RATON.soltar();
		final MenuConfiguracionSeleccion m = new MenuConfiguracionSeleccion(this);
		m.setEsDesdePausa(true);
		this.estadoActual = m;
	}

	public void abrirMenuGuardarPartida(final GestorPartida gp) {
		Globales.RATON.soltar();
		this.estadoActual = new MenuGuardarPartida(this, gp);
	}

	public void abrirMenuCargarPartidaEnPausa() {
		Globales.RATON.soltar();
		final MenuCargarPartida mCargar = new MenuCargarPartida(this);
		mCargar.setEsDesdePausa(true);
		this.estadoActual = mCargar;
	}

	public void editorMapaSeleccion() {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new MenuEdirorSeleccion(this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final int cantAncho, final int cantAlto, final principal.recursos.TipoTerreno tipoInicial) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(Constantes.LADO_TILE, cantAncho, cantAlto, tipoInicial, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final java.io.File directorioProyecto, final String idSubmundo, final String spawnEnfocar) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(directorioProyecto, idSubmundo, spawnEnfocar, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final java.io.File directorioProyecto, final String idSubmundo) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(directorioProyecto, idSubmundo, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final Escenario esc) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(esc, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapa(final Terreno mapa) {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new EditorMapa(mapa, this);
		this.estadoActual = this.estados[2];
	}

	public void editorMapaNuevoMenu() {
		Globales.estadoJuego = false;
		Globales.RATON.soltar();
		this.estados[2] = new MenuEditorNuevo(this);
		this.estadoActual = this.estados[2];
	}

	public void disposeEditor() {
		this.estados[2] = ESTADO_VACIO;
		if (this.estadoActual == this.estados[2]) {
			this.establecerEstadoActual(NUMERO_ESTADO_MENU);
		}
	}

	public void iniciarPartidaNueva() {
		Globales.estadoJuego = true;
		Globales.RATON.soltar();
		this.estados[0] = new GestorPartida(this);
		this.estadoActual = this.estados[0];
		Globales.GESTOR_INVENTARIO.getInventarioJugador().vaciar();
		Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
	}

	public void iniciarPartidaGuardada(final JSONObject saveJson) {
		if (saveJson == null) {
			return;
		}
		Globales.estadoJuego = true;
		Globales.RATON.soltar();
		final GestorPartida gp = new GestorPartida(this, saveJson);
		this.estados[0] = gp;
		this.estadoActual = gp;
	}

	public void cargarPartidaSlot(final int slot) {
		final JSONObject saveJson = GestorGuardado.leerJsonGuardado(slot);
		if (saveJson != null) {
			this.iniciarPartidaGuardada(saveJson);
		} else {
			JOptionPane.showMessageDialog(null, "No existe archivo de guardado en el Slot " + slot,
					"Partida no encontrada", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Destructor maestro de sesión: purga audio de ambiente, música, clima, luz,
	 * termorregulación, efectos del jugador y estructuras volátiles antes de
	 * regresar al Menú Principal (Zero-GC / O(1)).
	 */
	public void disposePartida() {
		// 1. Audio: Detener sonido ambiental de clima y música de fondo de partida
		principal.utilidades.audio.musica.GestorMusica.detenerAmbienteClima();
		principal.utilidades.audio.musica.GestorMusica.detenerMusicaFondoPrincipal();

		// 2. Clima y Atmósfera: Regresar a cielo despejado y apagar penumbra
		if (Globales.GESTOR_CLIMA != null) {
			Globales.GESTOR_CLIMA.setClima(principal.clima.TipoClima.DESPEJADO, 0.0);
		}
		if (Globales.GESTOR_LUZ != null) {
			Globales.GESTOR_LUZ.apagarTodasLasLuces();
			Globales.GESTOR_LUZ.restablecerModoExterior();
		}

		// 3. Fisiología y Jugador
		if (Globales.GESTOR_TERMICO_JUGADOR != null) {
			Globales.GESTOR_TERMICO_JUGADOR.reiniciar();
		}
		if (Globales.JUGADOR != null) {
			Globales.JUGADOR.reiniciarEstadoCompleto();
		}

		// 4. Memoria Espacial, Grupos y Telemetría
		if (Globales.GESTOR_ZONAS_AMBIENTE != null) {
			Globales.GESTOR_ZONAS_AMBIENTE.limpiarZonas();
		}
		if (Globales.GESTOR_GRUPO != null) {
			Globales.GESTOR_GRUPO.vaciar();
		}
		if (Globales.GESTOR_PARTICULAS != null) {
			Globales.GESTOR_PARTICULAS.limpiar();
		}
		if (Globales.GESTOR_TEXTOS != null) {
			Globales.GESTOR_TEXTOS.limpiar();
		}
		if (Globales.MOTOR_IGU != null) {
			Globales.MOTOR_IGU.desvincularJefe();
		}
		if (Globales.CAMARA != null) {
			Globales.CAMARA.reiniciarZoom();
			Globales.CAMARA.getGestorEfectos().detenerTodosLosEfectos();
		}

		// 5. Limpieza de mapas temporales y flags globales
		principal.mapa.mapas.MapaManager.vaciarTemp();
		Globales.partidaIniciada = false;
		Globales.pausa = false;
		Globales.RATON.soltar();

		// 6. Anular estado de partida y conmutar formalmente al Menú Principal
		this.estados[0] = ESTADO_VACIO;
		this.establecerEstadoActual(NUMERO_ESTADO_MENU);
	}

	public EstadoJuego getEstadoActual() {
		return this.estadoActual;
	}
}