package principal.comandos;

import principal.clima.GestorClima;
import principal.iluminacion.CalculadorSigilo;
import principal.iluminacion.CicloDiaNoche;
import principal.mapa.renderEntidades.camara.Camara;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Comando de telemetría integral, diagnóstico de memoria JVM y monitoreo de
 * subsistemas del motor en tiempo real.
 * <p>
 * Diseñado especialmente para inspección remota desde terminales móviles
 * (Termux / Netcat) y consola local de Eclipse.
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class ComandoInfo extends Comando {

	public ComandoInfo() {
		super("info", "info [ram | jugador | clima | camara | ayuda]",
				"Despliega el panel de telemetria en tiempo real (FPS, RAM, OPF, Sigilo, Clima, etc.).");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		// 1. Panel General Completo (sin argumentos o con 'stats')
		if ((args.length == 0) || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("all")) {
			this.mostrarDashboardCompleto(emisor);
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		switch (sub) {
		case "ayuda":
		case "help":
		case "?":
			this.mostrarMenuAyuda(emisor);
			break;

		case "ram":
		case "mem":
		case "memoria":
			this.mostrarInfoMemoria(emisor);
			break;

		case "jugador":
		case "player":
		case "pos":
			this.mostrarInfoJugador(emisor);
			break;

		case "clima":
		case "tiempo":
		case "ambiente":
			this.mostrarInfoClima(emisor);
			break;

		case "camara":
		case "optica":
		case "zoom":
			this.mostrarInfoCamara(emisor);
			break;

		default:
			this.enviarError(emisor, "Modulo desconocido: '" + args[0] + "'. Escribe 'info ayuda' para ver las opciones.");
			break;
		}
	}

	// =========================================================================
	// === PANELES DE TELEMETRÍA DETALLADOS
	// =========================================================================

	private void mostrarDashboardCompleto(final EmisorRespuesta emisor) {
		final Runtime rt = Runtime.getRuntime();
		final long totalMem = rt.totalMemory() / (1024 * 1024);
		final long freeMem = rt.freeMemory() / (1024 * 1024);
		final long usedMem = totalMem - freeMem;
		final long maxMem = rt.maxMemory() / (1024 * 1024);

		final Camara cam = Globales.CAMARA;
		final CicloDiaNoche ciclo = (Globales.GESTOR_LUZ != null) ? Globales.GESTOR_LUZ.getCiclo() : null;
		final GestorClima clima = Globales.GESTOR_CLIMA;

		final StringBuilder sb = new StringBuilder();
		sb.append("=================================================================\n");
		sb.append("                     TELEMETRIA DEL MOTOR 2D                    \n");
		sb.append("=================================================================\n");

		// --- 1. RENDIMIENTO & MEMORIA ---
		sb.append("[1] RENDIMIENTO & JVM:\n");
		sb.append(String.format("    FPS: %-4d | APS: %-4d | OPF (DrawCalls): %-5d\n", Globales.fps, Globales.aps, Render2D.getContadorObjetos()));
		sb.append(String.format("    RAM Usada: %d MB / %d MB (Max: %d MB)\n", usedMem, totalMem, maxMem));
		sb.append(String.format("    Pool Particulas: %d / 2048 | Luces Activas: %d / 256\n",
				(Globales.GESTOR_PARTICULAS != null ? Globales.GESTOR_PARTICULAS.getCantidadActivas() : 0),
				(Globales.GESTOR_LUZ != null ? Globales.GESTOR_LUZ.getCantidadActivas() : 0)));

		// --- 2. ESPACIO & JUGADOR ---
		sb.append("\n[2] JUGADOR & SIGILO:\n");
		if (Globales.JUGADOR != null) {
			final int jx = Globales.JUGADOR.getPosicionXInt();
			final int jy = Globales.JUGADOR.getPosicionYInt();
			final float visibilidad = CalculadorSigilo.calcularFactorVisibilidad(Globales.JUGADOR);

			sb.append(String.format("    Posicion Mundo : (%d, %d)\n", jx, jy));
			sb.append(String.format("    Direccion      : %s | Velocidad: %.2f\n", Globales.JUGADOR.getDireccion(), Globales.JUGADOR.getVelocidad()));
			sb.append(String.format("    Indice Sigilo  : %d%% de visibilidad (Alerta IA)\n", (int) (visibilidad * 100)));
		} else {
			sb.append("    Jugador no inicializado en memoria.\n");
		}

		// --- 3. CÁMARA & CINEMÁTICA ---
		sb.append("\n[3] CAMARA & OPTICA:\n");
		if (cam != null) {
			sb.append(String.format("    Zoom Base: %.2fx | Zoom Final: %.2fx (Auto-Crop)\n", cam.getZoom(), cam.getZoomFinal()));
			sb.append(String.format("    Look-Ahead : %-3s | Speed-Zoom: %-3s | Letterbox: %s\n",
					(cam.isLookAheadHabilitado() ? "ON" : "OFF"),
					(cam.isSpeedZoomHabilitado() ? "ON" : "OFF"),
					(cam.isModoCinematico() ? "ON" : "OFF")));
			sb.append(String.format("    Efectos FX Activos : %d\n", cam.getGestorEfectos().getCantidadActivos()));
		}

		// --- 4. ATMÓSFERA & CALENDARIO ---
		sb.append("\n[4] ATMOSFERA & TIEMPO:\n");
		if (ciclo != null) {
			sb.append(String.format("    Calendario : %s | %s (%s)\n", ciclo.getTextoDia(), ciclo.getHoraFormato24h(), ciclo.getNombreMomentoDelDia()));
			sb.append(String.format("    Velocidad  : %.1fx %s\n", ciclo.getMultiplicadorTiempo(), (ciclo.isTiempoPausado() ? "[PAUSADO]" : "")));
		}
		if (clima != null) {
			sb.append(String.format("    Clima      : %s (Bioma: %s)\n", clima.getNombreClimaActual(), clima.getPerfilBiomaActual().getNombreVisible()));
			sb.append(String.format("    Temperatura: %.1f °C | Viento: %.1f Fv | Presion: %d hPa\n",
					clima.getTemperaturaCelsius(), clima.getFuerzaViento(), (int) clima.getPresionHPa()));
		}
		sb.append("=================================================================");

		this.enviarInfo(emisor, sb.toString());
	}

	private void mostrarInfoMemoria(final EmisorRespuesta emisor) {
		final Runtime rt = Runtime.getRuntime();
		final long total = rt.totalMemory() / (1024 * 1024);
		final long libre = rt.freeMemory() / (1024 * 1024);
		final long usada = total - libre;
		final long max = rt.maxMemory() / (1024 * 1024);

		this.enviarInfo(emisor, "DIAGNOSTICO DE MEMORIA JVM:"
				+ "\n -> Memoria Usada    : " + usada + " MB"
				+ "\n -> Memoria Libre    : " + libre + " MB"
				+ "\n -> Memoria Asignada : " + total + " MB"
				+ "\n -> Memoria Maxima   : " + max + " MB"
				+ "\n -> Estado           : " + (usada > (max * 0.85) ? "ALERTA (Presion de memoria)" : "OPTIMO"));
	}

	private void mostrarInfoJugador(final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "Jugador no disponible.");
			return;
		}

		final float visibilidad = CalculadorSigilo.calcularFactorVisibilidad(Globales.JUGADOR);
		final boolean enInterior = (Globales.GESTOR_ZONAS_AMBIENTE != null) && Globales.GESTOR_ZONAS_AMBIENTE.isEnZonaInterior();

		this.enviarInfo(emisor, "TELEMETRIA DEL JUGADOR:"
				+ "\n -> Posicion Mundo    : (" + Globales.JUGADOR.getPosicionXInt() + ", " + Globales.JUGADOR.getPosicionYInt() + ")"
				+ "\n -> Direccion         : " + Globales.JUGADOR.getDireccion()
				+ "\n -> Velocidad Actual  : " + Globales.JUGADOR.getVelocidad()
				+ "\n -> Visibilidad Sigilo: " + (int) (visibilidad * 100) + " %"
				+ "\n -> Ubicacion         : " + (enInterior ? "Interior / Cueva (Bloquea Sol)" : "Exterior"));
	}

	private void mostrarInfoClima(final GestorClima clima) {
		// Auxiliar
	}

	private void mostrarInfoClima(final EmisorRespuesta emisor) {
		if (Globales.GESTOR_CLIMA == null) {
			this.enviarError(emisor, "GestorClima no disponible.");
			return;
		}
		final GestorClima c = Globales.GESTOR_CLIMA;
		this.enviarInfo(emisor, "TELEMETRIA CLIMATICA:"
				+ "\n -> Estado Clima  : " + c.getNombreClimaActual()
				+ "\n -> Pronostico    : " + c.getClimaPronosticado().getNombre() + " (en " + (int) c.getTiempoRestanteEstadoClima() + "s)"
				+ "\n -> Temperatura   : " + String.format("%.1f", c.getTemperaturaCelsius()) + " °C"
				+ "\n -> Humedad Rel.  : " + (int) (c.getHumedadRelativa() * 100) + " %"
				+ "\n -> Presion Atm.  : " + (int) c.getPresionHPa() + " hPa"
				+ "\n -> Fuerza Viento : " + c.getFuerzaViento());
	}

	private void mostrarInfoCamara(final EmisorRespuesta emisor) {
		if (Globales.CAMARA == null) {
			this.enviarError(emisor, "Camara no disponible.");
			return;
		}
		final Camara cam = Globales.CAMARA;
		this.enviarInfo(emisor, "TELEMETRIA OPTICA DE CAMARA:"
				+ "\n -> Posicion Foco     : (" + cam.getPosicionXInt() + ", " + cam.getPosicionYInt() + ")"
				+ "\n -> Zoom Base         : " + cam.getZoom() + "x"
				+ "\n -> Zoom Compensado   : " + String.format("%.2f", cam.getZoomFinal()) + "x"
				+ "\n -> Look-Ahead Cursor : " + (cam.isLookAheadHabilitado() ? "ACTIVADO" : "DESACTIVADO")
				+ "\n -> Speed-Zoom Correr : " + (cam.isSpeedZoomHabilitado() ? "ACTIVADO" : "DESACTIVADO")
				+ "\n -> Modo Letterbox    : " + (cam.isModoCinematico() ? "ACTIVADO" : "DESACTIVADO")
				+ "\n -> Efectos en Curso  : " + cam.getGestorEfectos().getCantidadActivos());
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO INFO / STATS ==="
				+ "\n1. info                 -> Despliega el Dashboard completo"
				+ "\n2. info ram             -> Diagnostico de memoria Heap de la JVM"
				+ "\n3. info jugador         -> Coordenadas, velocidad y sigilo del personaje"
				+ "\n4. info clima           -> Meteorologia, termodinamica y barometria"
				+ "\n5. info camara          -> Estado de zoom, efectos y seguimiento";
		this.enviarInfo(emisor, ayuda);
	}
}