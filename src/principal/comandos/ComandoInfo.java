package principal.comandos;

import principal.clima.GestorClima;
import principal.configuracion.ConfiguracionGrafica;
import principal.iluminacion.CalculadorSigilo;
import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.Estacion;
import principal.mapa.renderEntidades.camara.Camara;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Comando de telemetría integral, diagnóstico de memoria JVM, perfiles
 * gráficos, calendario canónico y monitoreo de subsistemas en tiempo real.
 * <p>
 * Diseñado especialmente para inspección remota desde terminales móviles
 * (Termux / Netcat) y consola local de Eclipse.
 * </p>
 * 
 * @version 2.0 (Vanilla Java 8 - Graphics Engine & Canonical Calendar
 *          Telemetry)
 */
public class ComandoInfo extends Comando {

	public ComandoInfo() {
		super("info", "info [ram | grafica | calendario | clima | jugador | camara | ayuda]",
				"Despliega el panel de telemetria en tiempo real (FPS, RAM, Graficos, Calendario, Clima, etc.).");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		// 1. Dashboard Completo (sin argumentos o con 'stats' / 'all')
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

		case "grafica":
		case "video":
		case "display":
		case "gpu":
			this.mostrarInfoGrafica(emisor);
			break;

		case "calendario":
		case "tiempo":
		case "estacion":
		case "reloj":
			this.mostrarInfoCalendario(emisor);
			break;

		case "clima":
		case "ambiente":
			this.mostrarInfoClima(emisor);
			break;

		case "jugador":
		case "player":
		case "pos":
			this.mostrarInfoJugador(emisor);
			break;

		case "camara":
		case "optica":
		case "zoom":
			this.mostrarInfoCamara(emisor);
			break;

		default:
			this.enviarError(emisor,
					"Modulo desconocido: '" + args[0] + "'. Escribe 'info ayuda' para ver las opciones.");
			break;
		}
	}

	// =========================================================================
	// === DASHBOARD GENERAL COMPLETO
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

		final StringBuilder sb = new StringBuilder(1024);
		sb.append("=================================================================\n");
		sb.append("                     TELEMETRIA DEL MOTOR 2D                    \n");
		sb.append("=================================================================\n");

		// --- 1. RENDIMIENTO & JVM ---
		sb.append("[1] RENDIMIENTO & JVM:\n");
		sb.append(String.format("    FPS: %-4d | APS: %-4d | OPF (DrawCalls): %-5d\n", Globales.fps, Globales.aps,
				Render2D.getContadorObjetos()));
		sb.append(String.format("    RAM Usada: %d MB / %d MB (Max: %d MB)\n", usedMem, totalMem, maxMem));
		sb.append(String.format("    Pool Particulas: %d / %d | Luces Activas: %d / 256\n",
				(Globales.GESTOR_PARTICULAS != null ? Globales.GESTOR_PARTICULAS.getCantidadActivas() : 0),
				ConfiguracionGrafica.LIMITE_PARTICULAS_MAX,
				(Globales.GESTOR_LUZ != null ? Globales.GESTOR_LUZ.getCantidadActivas() : 0)));

		// --- 2. CONFIGURACIÓN GRÁFICA & MONITOR ---
		sb.append("\n[2] CONFIGURACION GRAFICA & PANTALLA:\n");
		sb.append(String.format("    Perfil Motor   : %s | Limite: %s\n",
				ConfiguracionGrafica.getPerfil().getNombreLegible(),
				ConfiguracionGrafica.getLimiteFps().getNombreLegible()));
		sb.append(String.format("    Pantalla       : %s | Escalado: %s\n",
				ConfiguracionGrafica.getTipoPantalla().getNombreLegible(),
				ConfiguracionGrafica.getModoEscalado().getNombreLegible()));
		sb.append(String.format("    Escala Efectiva: %.2fx (X) / %.2fx (Y) | Offset: (%d, %d)\n",
				Globales.FACTOR_ESCALADO_X, Globales.FACTOR_ESCALADO_Y, Globales.DESPLAZAMIENTO_X,
				Globales.DESPLAZAMIENTO_Y));
		sb.append(String.format("    Optimizaciones : Lightmap SD: %s | Viento: %s | Sombras: %s\n",
				(ConfiguracionGrafica.OPT_LIGHTMAP_BAJA_RESOLUCION ? "ON" : "OFF"),
				(ConfiguracionGrafica.OPT_BALANCEO_EOLICO ? "ON" : "OFF"),
				(ConfiguracionGrafica.OPT_SOMBRAS_VOLUMETRICAS ? "ON" : "OFF")));

		// --- 3. CALENDARIO CANÓNICO & FOTOPERIODO ---
		sb.append("\n[3] CALENDARIO CANONICO & CICLO SOLAR:\n");
		if (ciclo != null) {
			final Estacion est = ciclo.getEstacionActual();
			final int hAm = (int) ciclo.getHoraAmanecer();
			final int mAm = (int) Math.round((ciclo.getHoraAmanecer() - hAm) * 60.0);
			final int hAt = (int) ciclo.getHoraAtardecer();
			final int mAt = (int) Math.round((ciclo.getHoraAtardecer() - hAt) * 60.0);

			sb.append(String.format("    Fecha HUD      : %s\n", ciclo.getTextoLinea1HUD()));
			sb.append(String.format("    Estacion       : %s (Dia %d/28 · Sem %d/4) [Delta: %+.1f °C]\n",
					est.getNombre(), ciclo.getDiaDeLaEstacion(), ciclo.getSemanaDeLaEstacion(),
					est.getDeltaTemperaturaCelsius()));
			sb.append(
					String.format("    Anual          : Anio %d · Dia %d/112 · Sem %d/16 (%s)\n", ciclo.getAnioActual(),
							ciclo.getDiaDelAnio(), ciclo.getSemanaAnio(), ciclo.getNombreDiaSemanaLargo()));
			sb.append(String.format("    Hora Solar     : %s (%s) | Warp: %.1fx %s\n", ciclo.getHoraFormato24h(),
					ciclo.getNombreMomentoDelDia(), ciclo.getMultiplicadorTiempo(),
					(ciclo.isTiempoPausado() ? "[PAUSADO]" : "")));
			sb.append(String.format("    Fotoperiodo    : Alba: %02d:%02d | Ocaso: %02d:%02d\n", hAm, mAm, hAt, mAt));
		} else {
			sb.append("    CicloDiaNoche no inicializado.\n");
		}

		// --- 4. ATMÓSFERA & CLIMA ---
		sb.append("\n[4] ATMOSFERA & METEOROLOGIA:\n");
		if (clima != null) {
			sb.append(String.format("    Clima Activo   : %s | Pronostico: %s (en %ds)\n", clima.getNombreClimaActual(),
					clima.getClimaPronosticado().getNombre(), (int) clima.getTiempoRestanteEstadoClima()));
			sb.append(String.format("    Bioma Base     : %s | Temperatura: %.1f °C\n",
					clima.getPerfilBiomaActual().getNombreVisible(), clima.getTemperaturaCelsius()));
			sb.append(String.format("    Viento         : Fuerza %.1f | Humedad: %d%% | Presion: %d hPa\n",
					clima.getFuerzaViento(), (int) (clima.getHumedadRelativa() * 100), (int) clima.getPresionHPa()));
		}

		// --- 5. JUGADOR & SIGILO ---
		sb.append("\n[5] JUGADOR & FISIOLOGIA:\n");
		if (Globales.JUGADOR != null) {
			final int jx = Globales.JUGADOR.getPosicionXInt();
			final int jy = Globales.JUGADOR.getPosicionYInt();
			final float visibilidad = CalculadorSigilo.calcularFactorVisibilidad(Globales.JUGADOR);
			final double tempCorp = (Globales.GESTOR_TERMICO_JUGADOR != null)
					? Globales.GESTOR_TERMICO_JUGADOR.getTemperaturaCorporal()
					: 37.0;

			sb.append(String.format("    Posicion Mundo : (%d, %d) | Direccion: %s\n", jx, jy,
					Globales.JUGADOR.getDireccion()));
			sb.append(String.format("    Fisiologia     : Temp Corporal: %.1f °C | Sigilo: %d%% visibilidad\n",
					tempCorp, (int) (visibilidad * 100)));
		}

		// --- 6. CÁMARA & ÓPTICA ---
		sb.append("\n[6] CAMARA & OPTICA:\n");
		if (cam != null) {
			sb.append(String.format("    Zoom Base: %.2fx | Zoom Final: %.2fx | Modo Cine: %s\n", cam.getZoom(),
					cam.getZoomFinal(), (cam.isModoCinematico() ? "ON" : "OFF")));
			sb.append(String.format("    Efectos FX Activos : %d\n", cam.getGestorEfectos().getCantidadActivos()));
		}

		sb.append("=================================================================");
		this.enviarInfo(emisor, sb.toString());
	}

	// =========================================================================
	// === PANELES ESPECÍFICOS POR MÓDULO
	// =========================================================================

	private void mostrarInfoGrafica(final EmisorRespuesta emisor) {
		this.enviarInfo(emisor,
				"CONFIGURACION GRAFICA Y PANTALLA:" + "\n -> Perfil Rendimiento: "
						+ ConfiguracionGrafica.getPerfil().getNombreLegible() + "\n -> Tipo de Pantalla  : "
						+ ConfiguracionGrafica.getTipoPantalla().getNombreLegible() + "\n -> Modo de Escalado  : "
						+ ConfiguracionGrafica.getModoEscalado().getNombreLegible() + "\n -> Limite de FPS     : "
						+ ConfiguracionGrafica.getLimiteFps().getNombreLegible() + "\n -> Factor Escala (X) : "
						+ String.format("%.2f", Globales.FACTOR_ESCALADO_X) + "x" + "\n -> Factor Escala (Y) : "
						+ String.format("%.2f", Globales.FACTOR_ESCALADO_Y) + "x" + "\n -> Desplazamiento    : ("
						+ Globales.DESPLAZAMIENTO_X + " px, " + Globales.DESPLAZAMIENTO_Y + " px)"
						+ "\n -> Lightmap Baja Res : "
						+ (ConfiguracionGrafica.OPT_LIGHTMAP_BAJA_RESOLUCION ? "ACTIVADO (1/2 Res)"
								: "DESACTIVADO (HD)")
						+ "\n -> Balanceo Eolico   : "
						+ (ConfiguracionGrafica.OPT_BALANCEO_EOLICO ? "ACTIVADO" : "DESACTIVADO (Bypass)")
						+ "\n -> Sombras Muros 2D  : "
						+ (ConfiguracionGrafica.OPT_SOMBRAS_VOLUMETRICAS ? "ACTIVADO" : "DESACTIVADO")
						+ "\n -> Particulas Max    : " + ConfiguracionGrafica.LIMITE_PARTICULAS_MAX);
	}

	private void mostrarInfoCalendario(final EmisorRespuesta emisor) {
		if ((Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			this.enviarError(emisor, "CicloDiaNoche no disponible.");
			return;
		}

		final CicloDiaNoche c = Globales.GESTOR_LUZ.getCiclo();
		final Estacion est = c.getEstacionActual();

		final int hAm = (int) c.getHoraAmanecer();
		final int mAm = (int) Math.round((c.getHoraAmanecer() - hAm) * 60.0);
		final int hAt = (int) c.getHoraAtardecer();
		final int mAt = (int) Math.round((c.getHoraAtardecer() - hAt) * 60.0);

		this.enviarInfo(emisor,
				"TELEMETRIA DE CALENDARIO Y TIEMPO:" + "\n -> HUD Linea 1       : " + c.getTextoLinea1HUD()
						+ "\n -> HUD Linea 2       : " + c.getTextoLinea2HUD() + "\n -> Estacion          : "
						+ est.getNombre() + " (Dia " + c.getDiaDeLaEstacion() + "/28 · Sem " + c.getSemanaDeLaEstacion()
						+ "/4)" + "\n -> Modulador Termico : "
						+ String.format("%+.1f", est.getDeltaTemperaturaCelsius()) + " °C base"
						+ "\n -> Progresion Anual  : Anio " + c.getAnioActual() + " · Dia " + c.getDiaDelAnio()
						+ "/112 · Sem " + c.getSemanaAnio() + "/16" + "\n -> Dia de la Semana  : "
						+ c.getNombreDiaSemanaLargo() + " (Indice " + c.getIndiceDiaSemana() + ")"
						+ "\n -> Dias Acumulados   : " + c.getDiaActual() + " dias globales monótonos"
						+ "\n -> Hora Solar        : " + c.getHoraFormato24h() + " (" + c.getNombreMomentoDelDia() + ")"
						+ "\n -> Fotoperiodo       : Alba a las " + String.format("%02d:%02d", hAm, mAm)
						+ " | Ocaso a las " + String.format("%02d:%02d", hAt, mAt) + "\n -> Velocidad Tiempo  : "
						+ c.getMultiplicadorTiempo() + "x " + (c.isTiempoPausado() ? "[PAUSADO]" : ""));
	}

	private void mostrarInfoMemoria(final EmisorRespuesta emisor) {
		final Runtime rt = Runtime.getRuntime();
		final long total = rt.totalMemory() / (1024 * 1024);
		final long libre = rt.freeMemory() / (1024 * 1024);
		final long usada = total - libre;
		final long max = rt.maxMemory() / (1024 * 1024);

		this.enviarInfo(emisor,
				"DIAGNOSTICO DE MEMORIA JVM:" + "\n -> Memoria Usada    : " + usada + " MB"
						+ "\n -> Memoria Libre    : " + libre + " MB" + "\n -> Memoria Asignada : " + total + " MB"
						+ "\n -> Memoria Maxima   : " + max + " MB" + "\n -> Estado           : "
						+ (usada > (max * 0.85) ? "ALERTA (Presion de memoria)" : "OPTIMO"));
	}

	private void mostrarInfoJugador(final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "Jugador no disponible.");
			return;
		}

		final float visibilidad = CalculadorSigilo.calcularFactorVisibilidad(Globales.JUGADOR);
		final boolean enInterior = (Globales.GESTOR_ZONAS_AMBIENTE != null)
				&& Globales.GESTOR_ZONAS_AMBIENTE.isEnZonaInterior();
		final double tempCorp = (Globales.GESTOR_TERMICO_JUGADOR != null)
				? Globales.GESTOR_TERMICO_JUGADOR.getTemperaturaCorporal()
				: 37.0;

		this.enviarInfo(emisor, "TELEMETRIA DEL JUGADOR:" + "\n -> Posicion Mundo    : ("
				+ Globales.JUGADOR.getPosicionXInt() + ", " + Globales.JUGADOR.getPosicionYInt() + ")"
				+ "\n -> Direccion         : " + Globales.JUGADOR.getDireccion() + "\n -> Velocidad Actual  : "
				+ Globales.JUGADOR.getVelocidad() + "\n -> Temperatura Corp. : " + String.format("%.1f", tempCorp)
				+ " °C" + "\n -> Visibilidad Sigilo: " + (int) (visibilidad * 100) + " %" + "\n -> Ubicacion         : "
				+ (enInterior ? "Interior / Cueva (Bloquea Sol)" : "Exterior (A la intemperie)"));
	}

	private void mostrarInfoClima(final EmisorRespuesta emisor) {
		if (Globales.GESTOR_CLIMA == null) {
			this.enviarError(emisor, "GestorClima no disponible.");
			return;
		}
		final GestorClima c = Globales.GESTOR_CLIMA;
		this.enviarInfo(emisor,
				"TELEMETRIA METEOROLOGICA:" + "\n -> Estado Clima  : " + c.getNombreClimaActual()
						+ "\n -> Pronostico    : " + c.getClimaPronosticado().getNombre() + " (en "
						+ (int) c.getTiempoRestanteEstadoClima() + "s)" + "\n -> Bioma Base    : "
						+ c.getPerfilBiomaActual().getNombreVisible() + "\n -> Temperatura   : "
						+ String.format("%.1f", c.getTemperaturaCelsius()) + " °C" + "\n -> Humedad Rel.  : "
						+ (int) (c.getHumedadRelativa() * 100) + " %" + "\n -> Presion Atm.  : "
						+ (int) c.getPresionHPa() + " hPa" + "\n -> Fuerza Viento : " + c.getFuerzaViento()
						+ "\n -> Reporte       : " + c.getReporteMeteorologico());
	}

	private void mostrarInfoCamara(final EmisorRespuesta emisor) {
		if (Globales.CAMARA == null) {
			this.enviarError(emisor, "Camara no disponible.");
			return;
		}
		final Camara cam = Globales.CAMARA;
		this.enviarInfo(emisor,
				"TELEMETRIA OPTICA DE CAMARA:" + "\n -> Posicion Foco     : (" + cam.getPosicionXInt() + ", "
						+ cam.getPosicionYInt() + ")" + "\n -> Zoom Base         : " + cam.getZoom() + "x"
						+ "\n -> Zoom Compensado   : " + String.format("%.2f", cam.getZoomFinal()) + "x"
						+ "\n -> Look-Ahead Cursor : " + (cam.isLookAheadHabilitado() ? "ACTIVADO" : "DESACTIVADO")
						+ "\n -> Speed-Zoom Correr : " + (cam.isSpeedZoomHabilitado() ? "ACTIVADO" : "DESACTIVADO")
						+ "\n -> Modo Letterbox    : " + (cam.isModoCinematico() ? "ACTIVADO" : "DESACTIVADO")
						+ "\n -> Efectos en Curso  : " + cam.getGestorEfectos().getCantidadActivos());
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO INFO / STATS ==="
				+ "\n1. info                 -> Despliega el Dashboard completo del motor"
				+ "\n2. info ram             -> Diagnostico de memoria Heap de la JVM"
				+ "\n3. info grafica         -> Perfil de rendimiento, resolucion, escalado y flags"
				+ "\n4. info calendario      -> Estado canónico, estacion activa y fotoperiodo solar"
				+ "\n5. info clima           -> Meteorologia, termodinamica y presion barometrica"
				+ "\n6. info jugador         -> Coordenadas, fisiologia corporal y sigilo"
				+ "\n7. info camara          -> Optica, zoom compensado y efectos cinemáticos";
		this.enviarInfo(emisor, ayuda);
	}
}