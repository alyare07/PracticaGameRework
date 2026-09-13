package principal.comandos;

import principal.configuracion.ConfiguracionGrafica;
import principal.configuracion.LimiteFPS;
import principal.utilidades.Globales;

/**
 * Comando para consultar y conmutar en caliente la tasa máxima de cuadros por
 * segundo (FPS) del motor (30 FPS, 60 FPS o Ilimitado para pruebas de estrés).
 * <p>
 * Los cambios se aplican en el siguiente frame y se persisten en
 * 'ConfigGrafica.json'.
 * </p>
 * 
 * @version 1.0 (Vanilla Java 8 - Hot-Swap Frame Limiter)
 */
public class ComandoFPS extends Comando {

	public ComandoFPS() {
		super("fps", "fps [30 | 60 | 0 / max / ilimitado | toggle | ayuda]",
				"Consulta o ajusta el limite de cuadros por segundo del motor en tiempo real.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		// 1. Consulta de Estado (sin argumentos)
		if (args.length == 0) {
			final LimiteFPS limActual = ConfiguracionGrafica.getLimiteFps();
			final String msPorCuadro = (limActual != LimiteFPS.ILIMITADO)
					? String.format("%.1f ms", limActual.getNsPorFrame() / 1_000_000.0)
					: "Sin limite";

			this.enviarInfo(emisor, "ESTADO DEL LIMITADOR DE RENDIMIENTO:" + "\n -> FPS Actuales : " + Globales.fps
					+ " FPS (APS Logicos: " + Globales.aps + ")" + "\n -> Limite Activo: "
					+ limActual.getNombreLegible() + " (" + msPorCuadro + " por frame)" + "\n -> Pacing Motor : "
					+ (limActual == LimiteFPS.ILIMITADO ? "Desbloqueado (Thread.yield)" : "LockSupport / Sleep Activo")
					+ "\n (Comandos: 'fps 30', 'fps 60', 'fps max', 'fps toggle')");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Alternar al siguiente modo cíclico (toggle)
		if (sub.equals("toggle") || sub.equals("siguiente") || sub.equals("next") || sub.equals("cambiar")) {
			final LimiteFPS nuevoLimite = ConfiguracionGrafica.getLimiteFps().siguiente();
			this.aplicarYPersistir(nuevoLimite, emisor);
			return;
		}

		// 4. Modo 30 FPS (Ahorro de batería / Netbook / Hardware modesto)
		if (sub.equals("30") || sub.equals("30fps")) {
			this.aplicarYPersistir(LimiteFPS.FPS_30, emisor);
			return;
		}

		// 5. Modo 60 FPS (Sincronización 1:1 estándar determinista)
		if (sub.equals("60") || sub.equals("60fps")) {
			this.aplicarYPersistir(LimiteFPS.FPS_60, emisor);
			return;
		}

		// 6. Modo Ilimitado / Benchmark (Pruebas de estrés de CPU/GPU)
		if (sub.equals("0") || sub.equals("max") || sub.equals("ilimitado") || sub.equals("unlimited")
				|| sub.equals("desbloqueado")) {
			this.aplicarYPersistir(LimiteFPS.ILIMITADO, emisor);
			return;
		}

		this.enviarError(emisor,
				"Opcion no reconocida: '" + args[0] + "'. Escribe 'fps ayuda' para ver las opciones disponibles.");
	}

	private void aplicarYPersistir(final LimiteFPS nuevoLimite, final EmisorRespuesta emisor) {
		ConfiguracionGrafica.setLimiteFps(nuevoLimite);
		ConfiguracionGrafica.guardarConfig();

		this.enviarInfo(emisor, "Limite de cuadros establecido en: " + nuevoLimite.getNombreLegible()
				+ (nuevoLimite == LimiteFPS.ILIMITADO ? " [BENCHMARK DESBLOQUEADO]" : " [Sincronizacion Activa]")
				+ " (Guardado en ConfigGrafica.json)");
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO FPS ==="
				+ "\n1. fps                 -> Muestra los FPS reales y el limite activo"
				+ "\n2. fps 30              -> Fija el limite a 30 FPS (Modo ahorro / bateria)"
				+ "\n3. fps 60              -> Fija el limite a 60 FPS (Sincronizacion 1:1 nativa)"
				+ "\n4. fps max | fps 0     -> Desbloquea la tasa de cuadros para stress tests"
				+ "\n5. fps toggle          -> Cicla entre 30, 60 e Ilimitado";
		this.enviarInfo(emisor, ayuda);
	}
}