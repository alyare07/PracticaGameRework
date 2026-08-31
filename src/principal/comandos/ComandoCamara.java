package principal.comandos;

import principal.mapa.renderEntidades.camara.Camara;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.Globales;

/**
 * Comando para el control óptico de la cámara, zoom escalonado, disparo de
 * efectos de sacudida, franjas cinemáticas (Letterbox) y seguimiento
 * predictivo.
 * <p>
 * Totalmente insensible a mayúsculas/minúsculas y compatible con terminales
 * remotas (Termux / Netcat).
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class ComandoCamara extends Comando {

	public ComandoCamara() {
		super("camara", "camara <zoom <val|in|out|reset> | efecto <tipo> [ms] [fuerza] | letterbox <on/off> | lookahead <on/off> | stop | ayuda>",
				"Modifica el zoom, dispara efectos de camara, activa cinematecas o resetea la lente.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.CAMARA == null) {
			this.enviarError(emisor, "La camara principal no esta inicializada.");
			return;
		}

		final Camara cam = Globales.CAMARA;

		// 1. Consulta de Estado (sin argumentos)
		if (args.length == 0) {
			this.enviarInfo(emisor, "ESTADO OPTICO DE CAMARA:"
					+ "\n -> Zoom Base       : " + cam.getZoom() + "x"
					+ "\n -> Zoom Final (Crop): " + String.format("%.2f", cam.getZoomFinal()) + "x"
					+ "\n -> Efectos Activos : " + cam.getGestorEfectos().getCantidadActivos()
					+ "\n -> Letterbox (Cine): " + (cam.isModoCinematico() ? "ON" : "OFF")
					+ "\n -> Look-Ahead      : " + (cam.isLookAheadHabilitado() ? "ON" : "OFF")
					+ "\n (Escribe 'camara ayuda' para ver todos los comandos)");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Detener todos los efectos (camara stop / camara reset)
		if (sub.equals("stop") || sub.equals("detener") || sub.equals("limpiar")) {
			cam.getGestorEfectos().detenerTodosLosEfectos();
			this.enviarInfo(emisor, "Todos los efectos de camara activos han sido detenidos.");
			return;
		}

		// 4. Control de Zoom (camara zoom <valor | in | out | reset>)
		if (sub.equals("zoom")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Zoom base actual: " + cam.getZoom() + "x (Final con efectos: "
						+ String.format("%.2f", cam.getZoomFinal()) + "x)");
				return;
			}

			final String accionZoom = args[1].toLowerCase().trim();
			switch (accionZoom) {
			case "in":
			case "+":
				cam.aumentarZoom();
				this.enviarInfo(emisor, "Zoom incrementado a: " + cam.getZoom() + "x");
				break;
			case "out":
			case "-":
				cam.reducirZoom();
				this.enviarInfo(emisor, "Zoom reducido a: " + cam.getZoom() + "x");
				break;
			case "reset":
			case "normal":
			case "1":
				cam.reiniciarZoom();
				this.enviarInfo(emisor, "Zoom restablecido a 1.0x (Escala 1:1)");
				break;
			default:
				final double nuevoZoom = this.parsearDouble(accionZoom, -1.0);
				if (nuevoZoom >= 0.2) {
					cam.setZoom(nuevoZoom);
					this.enviarInfo(emisor, "Zoom fijado en: " + cam.getZoom() + "x");
				} else {
					this.enviarError(emisor, "Valor de zoom invalido. Usa un numero (ej: 1.25, 0.75) o 'in', 'out', 'reset'.");
				}
				break;
			}
			return;
		}

		// 5. Franjas Negras Cinemáticas (camara letterbox on/off)
		if (sub.equals("letterbox") || sub.equals("cine") || sub.equals("barras")) {
			if (args.length < 2) {
				final boolean nuevoEstado = !cam.isModoCinematico();
				cam.activarModoCinematico(nuevoEstado);
				this.enviarInfo(emisor, "Modo Cinematico (Letterbox): " + (nuevoEstado ? "ACTIVADO" : "DESACTIVADO"));
				return;
			}
			final boolean activar = this.parsearBooleano(args[1]);
			cam.activarModoCinematico(activar);
			this.enviarInfo(emisor, "Modo Cinematico (Letterbox): " + (activar ? "ACTIVADO" : "DESACTIVADO"));
			return;
		}

		// 6. Enfoque Predictivo (camara lookahead on/off)
		if (sub.equals("lookahead") || sub.equals("cursor") || sub.equals("apuntado")) {
			if (args.length < 2) {
				final boolean nuevoEstado = !cam.isLookAheadHabilitado();
				cam.setLookAheadHabilitado(nuevoEstado);
				this.enviarInfo(emisor, "Seguimiento Look-Ahead: " + (nuevoEstado ? "ACTIVADO" : "DESACTIVADO"));
				return;
			}
			final boolean activar = this.parsearBooleano(args[1]);
			cam.setLookAheadHabilitado(activar);
			this.enviarInfo(emisor, "Seguimiento Look-Ahead: " + (activar ? "ACTIVADO" : "DESACTIVADO"));
			return;
		}

		// 7. Zoom Reactivo a Velocidad (camara speedzoom on/off)
		if (sub.equals("speedzoom") || sub.equals("speedfov")) {
			if (args.length < 2) {
				return;
			}
			final boolean activar = this.parsearBooleano(args[1]);
			cam.setSpeedZoomHabilitado(activar);
			this.enviarInfo(emisor, "Speed Zoom por velocidad: " + (activar ? "ACTIVADO" : "DESACTIVADO"));
			return;
		}

		// 8. Disparo de Efectos (camara efecto <nombre> [duracionMs] [intensidad])
		if (sub.equals("efecto") || sub.equals("fx") || sub.equals("shake")) {
			if (args.length < 2) {
				this.mostrarListaEfectos(emisor);
				return;
			}

			final String nombreFx = args[1].toUpperCase().trim().replace(" ", "_");
			TipoEfectoCamara tipo;
			try {
				tipo = TipoEfectoCamara.valueOf(nombreFx);
			} catch (final IllegalArgumentException e) {
				this.enviarError(emisor, "Efecto desconocido: '" + args[1] + "'.");
				this.mostrarListaEfectos(emisor);
				return;
			}

			// Manejo de efectos continuos/infinitos con toggle (on/off)
			if ((args.length >= 3) && (args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("off"))) {
				final boolean activar = args[2].equalsIgnoreCase("on");
				final double intensidad = (args.length >= 4) ? this.parsearDouble(args[3], 1.0) : 1.0;
				cam.getGestorEfectos().conmutarEfectoInfinito(tipo, activar, intensidad);
				this.enviarInfo(emisor, "Efecto infinito '" + tipo.name() + "' -> " + (activar ? "ACTIVADO" : "DESACTIVADO"));
				return;
			}

			// Efecto temporal con duración
			final double duracionMs = (args.length >= 3) ? this.parsearDouble(args[2], 1200.0) : 1200.0;
			final double intensidad = (args.length >= 4) ? this.parsearDouble(args[3], 1.0) : 1.0;

			cam.getGestorEfectos().reproducirEfectoTemporal(tipo, duracionMs, intensidad);
			this.enviarInfo(emisor, "Reproduciendo efecto '" + tipo.name() + "' (" + (int) duracionMs + "ms | Fuerza: " + intensidad + ")");
			return;
		}

		this.enviarError(emisor, "Subcomando desconocido: '" + args[0] + "'. Escribe 'camara ayuda' para ver las opciones.");
	}

	private boolean parsearBooleano(final String str) {
		final String clean = str.toLowerCase().trim();
		return clean.equals("on") || clean.equals("true") || clean.equals("1") || clean.equals("si");
	}

	private void mostrarListaEfectos(final EmisorRespuesta emisor) {
		final StringBuilder sb = new StringBuilder("Efectos de camara disponibles:\n -> ");
		for (final TipoEfectoCamara t : TipoEfectoCamara.values()) {
			sb.append(t.name()).append(" | ");
		}
		this.enviarInfo(emisor, sb.toString());
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO CAMARA ==="
				+ "\n1. Control de Zoom:"
				+ "\n   - camara zoom 1.25          -> Fija zoom a 1.25x"
				+ "\n   - camara zoom in | out      -> Aumenta/reduce un paso"
				+ "\n   - camara zoom reset         -> Vuelve a escala 1:1"
				+ "\n2. Efectos de Sacudida y Cinemática:"
				+ "\n   - camara efecto TERREMOTO 1500 5.0  -> Temblor de 1.5s"
				+ "\n   - camara efecto PISOTON 400 1.0     -> Golpe elástico de zoom"
				+ "\n   - camara efecto BORRACHO on/off     -> Balanceo continuo"
				+ "\n   - camara efecto BARCO_NAVEGACION on -> Oleaje marítimo"
				+ "\n3. Modos y Lentes Especiales:"
				+ "\n   - camara letterbox on/off   -> Franjas negras de cine"
				+ "\n   - camara lookahead on/off   -> Seguimiento predictivo de cursor"
				+ "\n   - camara stop               -> Detiene todos los efectos activos";
		this.enviarInfo(emisor, ayuda);
	}
}