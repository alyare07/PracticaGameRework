package principal.comandos;

import principal.clima.GestorClima;
import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.utilidades.Globales;

/**
 * Comando para la manipulación meteorológica, control de viento, perfiles de
 * bioma y simulaciones climáticas aceleradas.
 * <p>
 * Totalmente insensible a mayúsculas/minúsculas y compatible con terminales
 * remotas (Termux / Netcat).
 * </p>
 * 
 * @version 3.0
 */
public class ComandoClima extends Comando {

	public ComandoClima() {
		super("clima",
				"clima <nombre | viento <fuerza> [angulo] | bioma <tipo> | auto <on/off> | test <seg> | siguiente | ayuda>",
				"Cambia el clima, modula el viento, asigna biomas o activa simulaciones de prueba.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_CLIMA == null) {
			this.enviarError(emisor, "El subsistema de clima no esta inicializado.");
			return;
		}

		final GestorClima clima = Globales.GESTOR_CLIMA;

		// 1. Consulta de Estado (sin argumentos)
		if (args.length == 0) {
			this.enviarInfo(emisor, "ESTADO METEOROLOGICO:" + "\n -> Clima Activo   : " + clima.getNombreClimaActual()
					+ "\n -> Pronostico    : " + clima.getClimaPronosticado().getNombre() + " (en "
					+ (int) clima.getTiempoRestanteEstadoClima() + "s)" + "\n -> Bioma Base    : "
					+ clima.getPerfilBiomaActual().getNombreVisible() + "\n -> Temperatura   : "
					+ String.format("%.1f", clima.getTemperaturaCelsius()) + " °C" + "\n -> Humedad Rel.  : "
					+ (int) (clima.getHumedadRelativa() * 100) + " %" + "\n -> Presion Atm.  : "
					+ (int) clima.getPresionHPa() + " hPa" + "\n -> Viento        : Fuerza " + clima.getFuerzaViento()
					+ " | Ciclo Auto: " + (clima.isCicloAutomaticoHabilitado() ? "ON" : "OFF")
					+ "\n (Escribe 'clima ayuda' para ver todos los comandos)");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Forzar Siguiente Clima del Pronóstico
		if (sub.equals("siguiente") || sub.equals("next") || sub.equals("skip")) {
			clima.forzarSiguienteClima();
			this.enviarInfo(emisor, "Transicion forzada hacia el proximo clima del pronostico.");
			return;
		}

		// 4. Control de Viento (clima viento <fuerza> [angulo])
		if (sub.equals("viento") || sub.equals("wind")) {
			if (args.length < 2) {
				this.enviarError(emisor,
						"Uso: 'clima viento <fuerza>' o 'clima viento <fuerza> <grados_angulo>'\nEjemplo: 'clima viento 3.0 45'");
				return;
			}
			final double fuerza = this.parsearDouble(args[1], 1.0);
			if (args.length >= 3) {
				final double grados = this.parsearDouble(args[2], 45.0);
				clima.setViento(grados, fuerza);
				this.enviarInfo(emisor, "Viento ajustado -> Fuerza: " + fuerza + " | Direccion: " + grados + "°");
			} else {
				clima.setFuerzaViento(fuerza);
				this.enviarInfo(emisor, "Fuerza del viento ajustada a: " + fuerza);
			}
			return;
		}

		// 5. Asignación de Bioma (clima bioma <nombre>)
		if (sub.equals("bioma") || sub.equals("biome")) {
			if (args.length < 2) {
				this.enviarError(emisor,
						"Indica el bioma. Opciones: TEMPLADO, DESIERTO, MONTANA, PANTANO, VOLCANICO, MISTICO");
				return;
			}
			final PerfilClima biomaSeleccionado = this.parsearBioma(args[1]);
			if (biomaSeleccionado != null) {
				clima.setPerfilBioma(biomaSeleccionado);
				this.enviarInfo(emisor, "Perfil de bioma cambiado a: " + biomaSeleccionado.getNombreVisible()
						+ " (Temp base: " + biomaSeleccionado.getTemperaturaBase() + "°C)");
			} else {
				this.enviarError(emisor, "Bioma desconocido: '" + args[1]
						+ "'.\nOpciones: TEMPLADO, DESIERTO, MONTANA, PANTANO, VOLCANICO, MISTICO");
			}
			return;
		}

		// 6. Conmutar Ciclo Automático (clima auto on/off)
		if (sub.equals("auto") || sub.equals("ciclo")) {
			if (args.length < 2) {
				this.enviarInfo(emisor,
						"Ciclo meteorologico automatico: " + (clima.isCicloAutomaticoHabilitado() ? "ON" : "OFF"));
				return;
			}
			final String flag = args[1].toLowerCase();
			final boolean habilitar = flag.equals("on") || flag.equals("true") || flag.equals("1") || flag.equals("si");
			clima.setCicloAutomaticoHabilitado(habilitar);
			this.enviarInfo(emisor,
					"Ciclo climatico automatico establecido en: " + (habilitar ? "ACTIVADO" : "DESACTIVADO"));
			return;
		}

		// 7. Modo Prueba Acelerada (clima test <segundos>)
		if (sub.equals("test") || sub.equals("prueba") || sub.equals("fast")) {
			final double segsClima = (args.length >= 2) ? this.parsearDouble(args[1], 8.0) : 8.0;
			final double segsTrans = (args.length >= 3) ? this.parsearDouble(args[2], 2.0) : 2.0;
			clima.activarModoPruebaRapida(segsClima, segsTrans);
			this.enviarInfo(emisor, "Modo Prueba Rapida activado: cada clima durara " + segsClima + "s con " + segsTrans
					+ "s de transicion.");
			return;
		}

		// 8. Asignación Directa de TipoClima por Nombre o Alias
		final TipoClima tipoClima = this.parsearTipoClima(args[0]);
		if (tipoClima != null) {
			final double transicion = (args.length >= 2) ? this.parsearDouble(args[1], 2.0) : 2.0;
			clima.setCicloAutomaticoHabilitado(false);
			clima.setClima(tipoClima, transicion);
			this.enviarInfo(emisor,
					"Clima cambiado exitosamente a: " + tipoClima.getNombre() + " (Transicion: " + transicion + "s)");
		} else {
			this.enviarError(emisor, "Clima no reconocido: '" + args[0]
					+ "'.\nEscribe 'clima ayuda' para ver la lista completa de opciones.");
		}
	}

	private TipoClima parsearTipoClima(final String str) {
		final String clean = str.toUpperCase().trim().replace(" ", "_");

		// Intentar coincidencia exacta de Enum
		try {
			return TipoClima.valueOf(clean);
		} catch (final IllegalArgumentException ignored) {
		}

		// Alias tolerantes
		switch (clean) {
		case "DESPEJADO":
		case "SOL":
		case "CLEAR":
			return TipoClima.DESPEJADO;
		case "VIENTO":
		case "VENTOSO":
		case "WIND":
			return TipoClima.VENTOSO;
		case "LLUVIA":
		case "LLUVIA_LEVE":
		case "RAIN":
			return TipoClima.LLUVIA_LEVE;
		case "TORMENTA":
		case "LLUVIA_TORMENTA":
		case "RAYOS":
		case "THUNDER":
			return TipoClima.LLUVIA_TORMENTA;
		case "NIEVE":
		case "SNOW":
			return TipoClima.NIEVE;
		case "VENTISCA":
		case "BLIZZARD":
			return TipoClima.VENTISCA;
		case "ARENA":
		case "TORMENTA_ARENA":
		case "SAND":
			return TipoClima.TORMENTA_ARENA;
		case "CENIZA":
		case "CENIZA_VOLCANICA":
		case "ASH":
			return TipoClima.CENIZA_VOLCANICA;
		case "ESPORAS":
		case "ESPORAS_MAGICAS":
		case "SPORES":
			return TipoClima.ESPORAS_MAGICAS;
		case "NIEBLA":
		case "NIEBLA_CERRADA":
		case "FOG":
			return TipoClima.NIEBLA_CERRADA;
		case "PETALOS":
		case "PETALOS_CEREZO":
		case "SAKURA":
			return TipoClima.PETALOS_CEREZO;
		case "ACIDO":
		case "LLUVIA_ACIDA":
		case "ACID":
			return TipoClima.LLUVIA_ACIDA;
		case "AURORA":
		case "AURORA_BOREAL":
			return TipoClima.AURORA_BOREAL;
		case "ECLIPSE":
		case "ECLIPSE_SOLAR":
			return TipoClima.ECLIPSE_SOLAR;
		case "ESTRELLAS":
		case "LLUVIA_ESTRELLAS":
		case "METEOROS":
			return TipoClima.LLUVIA_ESTRELLAS;
		default:
			return null;
		}
	}

	private PerfilClima parsearBioma(final String str) {
		final String clean = str.toUpperCase().trim();
		if (clean.contains("BOSQUE") || clean.contains("TEMPLADO")) {
			return PerfilClima.TEMPLADO_BOSQUE;
		}
		if (clean.contains("DESIERTO") || clean.contains("CALIDO") || clean.contains("ARENA")) {
			return PerfilClima.DESIERTO_CALIDO;
		}
		if (clean.contains("MONTANA") || clean.contains("MONTAÑA") || clean.contains("NIEVE")
				|| clean.contains("HELADA")) {
			return PerfilClima.MONTANA_NEVADA;
		}
		if (clean.contains("PANTANO") || clean.contains("HUMEDO") || clean.contains("CIENAGA")) {
			return PerfilClima.PANTANO_HUMEDO;
		}
		if (clean.contains("VOLCAN") || clean.contains("VOLCANICO") || clean.contains("FUEGO")) {
			return PerfilClima.VOLCANICO;
		}
		if (clean.contains("MISTICO") || clean.contains("MÁGICO") || clean.contains("MAGICO")
				|| clean.contains("HADAS")) {
			return PerfilClima.BOSQUE_MISTICO;
		}
		return null;
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO CLIMA ===" + "\n1. Asignar Clima Directo:"
				+ "\n   - clima despejado | ventoso | lluvia | tormenta"
				+ "\n   - clima nieve | ventisca | arena | ceniza | esporas"
				+ "\n   - clima niebla | petalos | acido | aurora | eclipse | estrellas" + "\n2. Control de Viento:"
				+ "\n   - clima viento 3.0          -> Fija fuerza 3.0"
				+ "\n   - clima viento 2.5 90       -> Fuerza 2.5 y angulo 90° (Sur)"
				+ "\n3. Perfil de Bioma (Cadenas de Markov):"
				+ "\n   - clima bioma templado | desierto | montana | pantano | volcanico | mistico"
				+ "\n4. Simulacion y Pruebas:" + "\n   - clima auto on/off         -> Activa/detiene cambio automatico"
				+ "\n   - clima test 6              -> Cicla climas cada 6 segundos"
				+ "\n   - clima siguiente           -> Salta al proximo clima previsto";
		this.enviarInfo(emisor, ayuda);
	}
}