package principal.configuracion;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.json.simple.JSONObject;

import principal.Main;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Gestor del subsistema gráfico y escalado de pantalla. Delega su persistencia
 * al {@link GestorConfiguracion} unificado y sincroniza perfiles en caliente
 * (Zero-GC).
 * 
 * @version 4.0 (Vanilla Java 8 - Reactive Profile Synchronization)
 */
public final class ConfiguracionGrafica {

	// --- Opciones de Usuario ---
	private static TipoPantalla tipoPantalla = TipoPantalla.SIN_BORDES;
	private static ModoEscalado modoEscalado = ModoEscalado.AJUSTE_PROPORCIONAL_16_9;
	private static PerfilRendimiento perfil = PerfilRendimiento.ALTO;
	private static LimiteFPS limiteFps = LimiteFPS.FPS_60;
	private static int escalaVentana = 1;

	// --- Banderas O(1) de Consulta Rápida en Hot Path ---
	public static boolean OPT_LIGHTMAP_BAJA_RESOLUCION = false;
	public static boolean OPT_BALANCEO_EOLICO = true;
	public static boolean OPT_SOMBRAS_VOLUMETRICAS = true;
	public static int LIMITE_PARTICULAS_MAX = 2048;

	private ConfiguracionGrafica() {
	}

	public static void inicializar() {
		GestorConfiguracion.inicializar();
	}

	public static void detectarConfiguracionOptima() {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final DisplayMode dm = gd.getDisplayMode();
		final int anchoMonitor = dm.getWidth();
		final int altoMonitor = dm.getHeight();

		final int nucleos = Runtime.getRuntime().availableProcessors();
		final long memoriaMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);

		// Detección escalonada real de los 4 perfiles sin saltar BASICO
		if ((nucleos <= 2) && (memoriaMB <= 256)) {
			perfil = PerfilRendimiento.POTATO;
		} else if (nucleos <= 2) {
			perfil = PerfilRendimiento.BASICO;
		} else if (nucleos <= 4) {
			perfil = PerfilRendimiento.MEDIO;
		} else {
			perfil = PerfilRendimiento.ALTO;
		}

		final boolean esMultiploExacto16_9 = ((anchoMonitor % Constantes.ANCHO_JUEGO) == 0)
				&& ((altoMonitor % Constantes.ALTO_JUEGO) == 0)
				&& ((anchoMonitor / Constantes.ANCHO_JUEGO) == (altoMonitor / Constantes.ALTO_JUEGO));

		if (esMultiploExacto16_9) {
			modoEscalado = ModoEscalado.PIXEL_PERFECT_ENTERO;
		} else {
			modoEscalado = ModoEscalado.AJUSTE_PROPORCIONAL_16_9;
		}

		tipoPantalla = TipoPantalla.SIN_BORDES;
		limiteFps = LimiteFPS.FPS_60;
		escalaVentana = Math.max(1,
				Math.min(anchoMonitor / Constantes.ANCHO_JUEGO, altoMonitor / Constantes.ALTO_JUEGO));
	}

	public static void aplicar() {
		switch (perfil) {
		case POTATO:
			OPT_LIGHTMAP_BAJA_RESOLUCION = true;
			OPT_BALANCEO_EOLICO = false;
			OPT_SOMBRAS_VOLUMETRICAS = false;
			LIMITE_PARTICULAS_MAX = 512;
			break;
		case BASICO:
			OPT_LIGHTMAP_BAJA_RESOLUCION = true;
			OPT_BALANCEO_EOLICO = true;
			OPT_SOMBRAS_VOLUMETRICAS = true;
			LIMITE_PARTICULAS_MAX = 1024;
			break;
		case MEDIO:
			OPT_LIGHTMAP_BAJA_RESOLUCION = false;
			OPT_BALANCEO_EOLICO = true;
			OPT_SOMBRAS_VOLUMETRICAS = true;
			LIMITE_PARTICULAS_MAX = 1536;
			break;
		case ALTO:
		default:
			OPT_LIGHTMAP_BAJA_RESOLUCION = false;
			OPT_BALANCEO_EOLICO = true;
			OPT_SOMBRAS_VOLUMETRICAS = true;
			LIMITE_PARTICULAS_MAX = 2048;
			break;
		}

		recalcularEscaladoYOffsets();

		if ((Main.gp != null) && (Main.gp.getVentana() != null)) {
			Main.gp.getVentana().aplicarModoVisualizacion(tipoPantalla, escalaVentana);
		}

		// Sincronización reactiva instantánea: el clima reajusta sus partículas en
		// caliente
		if (Globales.GESTOR_CLIMA != null) {
			Globales.GESTOR_CLIMA.sincronizarConPerfilRendimiento();
		}
	}

	public static void recalcularEscaladoYOffsets() {
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final DisplayMode dm = gd.getDisplayMode();
		final int anchoFisico = dm.getWidth();
		final int altoFisico = dm.getHeight();

		if (tipoPantalla != TipoPantalla.VENTANA) {
			Globales.ANCHO_PANTALLA_COMPLETA = anchoFisico;
			Globales.ALTO_PANTALLA_COMPLETA = altoFisico;

			switch (modoEscalado) {
			case PIXEL_PERFECT_ENTERO: {
				final int escalaX = anchoFisico / Constantes.ANCHO_JUEGO;
				final int escalaY = altoFisico / Constantes.ALTO_JUEGO;
				final int escalaFinal = Math.max(1, Math.min(escalaX, escalaY));

				Globales.FACTOR_ESCALADO_X = escalaFinal;
				Globales.FACTOR_ESCALADO_Y = escalaFinal;

				final int anchoRender = Constantes.ANCHO_JUEGO * escalaFinal;
				final int altoRender = Constantes.ALTO_JUEGO * escalaFinal;

				Globales.DESPLAZAMIENTO_X = (anchoFisico - anchoRender) / 2;
				Globales.DESPLAZAMIENTO_Y = (altoFisico - altoRender) / 2;
				break;
			}
			case AJUSTE_PROPORCIONAL_16_9: {
				final double escalaX = (double) anchoFisico / Constantes.ANCHO_JUEGO;
				final double escalaY = (double) altoFisico / Constantes.ALTO_JUEGO;
				final double escalaFinal = Math.min(escalaX, escalaY);

				Globales.FACTOR_ESCALADO_X = escalaFinal;
				Globales.FACTOR_ESCALADO_Y = escalaFinal;

				final int anchoRender = (int) Math.round(Constantes.ANCHO_JUEGO * escalaFinal);
				final int altoRender = (int) Math.round(Constantes.ALTO_JUEGO * escalaFinal);

				Globales.DESPLAZAMIENTO_X = (anchoFisico - anchoRender) / 2;
				Globales.DESPLAZAMIENTO_Y = (altoFisico - altoRender) / 2;
				break;
			}
			case ESTIRAR_PANTALLA_COMPLETA:
			default: {
				Globales.FACTOR_ESCALADO_X = (double) anchoFisico / Constantes.ANCHO_JUEGO;
				Globales.FACTOR_ESCALADO_Y = (double) altoFisico / Constantes.ALTO_JUEGO;
				Globales.DESPLAZAMIENTO_X = 0;
				Globales.DESPLAZAMIENTO_Y = 0;
				break;
			}
			}
		} else {
			final int anchoVentana = Constantes.ANCHO_JUEGO * escalaVentana;
			final int altoVentana = Constantes.ALTO_JUEGO * escalaVentana;

			Globales.ANCHO_PANTALLA_COMPLETA = anchoVentana;
			Globales.ALTO_PANTALLA_COMPLETA = altoVentana;
			Globales.FACTOR_ESCALADO_X = escalaVentana;
			Globales.FACTOR_ESCALADO_Y = escalaVentana;
			Globales.DESPLAZAMIENTO_X = 0;
			Globales.DESPLAZAMIENTO_Y = 0;
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONObject exportarJSON() {
		final JSONObject jo = new JSONObject();
		jo.put("tipoPantalla", tipoPantalla.name());
		jo.put("modoEscalado", modoEscalado.name());
		jo.put("perfil", perfil.name());
		jo.put("limiteFps", limiteFps.name());
		jo.put("escalaVentana", Integer.valueOf(escalaVentana));
		return jo;
	}

	public static void importarJSON(final JSONObject jo) {
		if (jo == null) {
			return;
		}
		if (jo.containsKey("tipoPantalla")) {
			tipoPantalla = TipoPantalla.valueOf((String) jo.get("tipoPantalla"));
		}
		if (jo.containsKey("modoEscalado")) {
			modoEscalado = ModoEscalado.valueOf((String) jo.get("modoEscalado"));
		}
		if (jo.containsKey("perfil")) {
			perfil = PerfilRendimiento.valueOf((String) jo.get("perfil"));
		}
		if (jo.containsKey("limiteFps")) {
			limiteFps = LimiteFPS.valueOf((String) jo.get("limiteFps"));
		}
		if (jo.containsKey("escalaVentana")) {
			escalaVentana = ((Number) jo.get("escalaVentana")).intValue();
		}
	}

	public static void guardarConfig() {
		GestorConfiguracion.guardarConfiguracion();
	}

	public static boolean cargarConfig() {
		return GestorConfiguracion.cargarConfiguracion();
	}

	public static TipoPantalla getTipoPantalla() {
		return tipoPantalla;
	}

	public static void setTipoPantalla(final TipoPantalla t) {
		tipoPantalla = (t != null) ? t : TipoPantalla.SIN_BORDES;
	}

	public static ModoEscalado getModoEscalado() {
		return modoEscalado;
	}

	public static void setModoEscalado(final ModoEscalado m) {
		modoEscalado = (m != null) ? m : ModoEscalado.AJUSTE_PROPORCIONAL_16_9;
	}

	public static PerfilRendimiento getPerfil() {
		return perfil;
	}

	public static void setPerfil(final PerfilRendimiento p) {
		perfil = (p != null) ? p : PerfilRendimiento.ALTO;
	}

	public static LimiteFPS getLimiteFps() {
		return limiteFps;
	}

	public static void setLimiteFps(final LimiteFPS l) {
		limiteFps = (l != null) ? l : LimiteFPS.FPS_60;
	}

	public static boolean isPantallaCompleta() {
		return tipoPantalla != TipoPantalla.VENTANA;
	}

	public static int getEscalaVentana() {
		return escalaVentana;
	}

	public static void setEscalaVentana(final int e) {
		escalaVentana = Math.max(1, e);
	}
}