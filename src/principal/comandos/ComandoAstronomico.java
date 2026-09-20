package principal.comandos;

import principal.astronomia.FaseLunar;
import principal.astronomia.FenomenoAstronomico;
import principal.astronomia.GestorAstronomico;
import principal.utilidades.Globales;

/**
 * Comando para el diagnóstico, forzado y manipulación de eventos cósmicos,
 * fases lunares, meteoros, auroras boreales y mecánicas celestes.
 * 
 * @version 1.0 (Vanilla Java 8 - Sovereign Celestial Command)
 */
public class ComandoAstronomico extends Comando {

	public ComandoAstronomico() {
		super("astro",
				"astro <evento <tipo> [seg] | luna <fase> | forzar <lunaroja|eclipse> | off | status | ayuda>",
				"Controla eventos cósmicos (Luna Roja, Eclipse, Auroras, Meteoros) y fases lunares.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_ASTRONOMICO == null) {
			this.enviarError(emisor, "El subsistema astronomico no esta inicializado.");
			return;
		}

		final GestorAstronomico astro = Globales.GESTOR_ASTRONOMICO;

		// 1. Telemetría y Estado Cósmico (sin argumentos o con 'status')
		if ((args.length == 0) || args[0].equalsIgnoreCase("status")) {
			this.mostrarEstadoAstronomico(astro, emisor);
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Apagar Eventos Celestes (astro off / astro normal)
		if (sub.equals("off") || sub.equals("normal") || sub.equals("clear") || sub.equals("reset")) {
			astro.desactivarFenomeno();
			this.enviarInfo(emisor, "Firmamento restablecido a condiciones normales (Cielo Despejado).");
			return;
		}

		// 4. Forzar Eventos Canónicos (astro forzar lunaroja / astro forzar eclipse)
		if (sub.equals("forzar") || sub.equals("force")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Uso: 'astro forzar lunaroja' o 'astro forzar eclipse'");
				return;
			}
			final String target = args[1].toLowerCase().trim();
			if (target.contains("luna") || target.contains("roja") || target.contains("blood")) {
				astro.forzarLunaRoja();
				this.enviarInfo(emisor, "¡Luna Roja forzada! Reloj saltó a las 21:30 en Luna Llena.");
			} else if (target.contains("eclipse") || target.contains("solar")) {
				astro.forzarEclipse();
				this.enviarInfo(emisor, "¡Eclipse Solar forzado! Reloj saltó a las 12:00 en Luna Nueva.");
			} else {
				this.enviarError(emisor, "Evento no reconocido para forzar: '" + args[1] + "'. Opciones: lunaroja, eclipse");
			}
			return;
		}

		// 5. Asignar Fenómeno Directo (astro evento <tipo> [duracion_seg])
		if (sub.equals("evento") || sub.equals("event")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Indica el evento: LUNA_ROJA, ECLIPSE_SOLAR, AURORA_BOREAL, LLUVIA_ESTRELLAS, CONJUNCION_ASTRAL o NORMAL");
				return;
			}

			final FenomenoAstronomico fen = this.parsearFenomeno(args[1]);
			if (fen != null) {
				final double duracion = (args.length >= 3) ? this.parsearDouble(args[2], 300.0) : 300.0;
				if (fen == FenomenoAstronomico.NORMAL) {
					astro.desactivarFenomeno();
					this.enviarInfo(emisor, "Evento cósmico desactivado.");
				} else {
					astro.activarFenomeno(fen, duracion);
					this.enviarInfo(emisor, "Fenómeno cósmico activado: " + fen.getNombreVisible() + " por " + (int) duracion + " segundos.");
				}
			} else {
				this.enviarError(emisor, "Fenómeno desconocido: '" + args[1] + "'. Escribe 'astro ayuda' para ver las opciones.");
			}
			return;
		}

		// 6. Cambiar Fase Lunar (astro luna <fase>)
		if (sub.equals("luna") || sub.equals("moon")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Fase Lunar actual: " + astro.getFaseLunarActual().getNombreVisible());
				return;
			}
			final int offsetDiaFase = this.parsearOffsetFaseLunar(args[1]);
			if (offsetDiaFase >= 0) {
				// Saltamos al día correspondiente dentro del mes/estación actual (28 días)
				final int diaEstacionActual = astro.getDiaDeLaEstacion();
				final int baseDiaGlobal = astro.getDiaActual() - diaEstacionActual;
				final int nuevoDiaGlobal = baseDiaGlobal + offsetDiaFase;

				astro.setDiaActual(nuevoDiaGlobal);
				this.enviarInfo(emisor, "Fase lunar cambiada a: " + astro.getFaseLunarActual().getNombreVisible()
						+ " (Día " + astro.getDiaDeLaEstacion() + " de " + astro.getEstacionActual().getNombre() + ")");
			} else {
				this.enviarError(emisor, "Fase desconocida. Opciones: NUEVA, CRECIENTE, CUARTO_CRECIENTE, GIBOSA_CRECIENTE, LLENA, GIBOSA_MENGUANTE, CUARTO_MENGUANTE, MENGUANTE");
			}
			return;
		}

		// 7. Si se pasa el nombre del evento directo (ej: 'astro lunaroja')
		final FenomenoAstronomico directo = this.parsearFenomeno(args[0]);
		if (directo != null) {
			final double duracion = (args.length >= 2) ? this.parsearDouble(args[1], 300.0) : 300.0;
			if (directo == FenomenoAstronomico.NORMAL) {
				astro.desactivarFenomeno();
				this.enviarInfo(emisor, "Firmamento restablecido a condiciones normales.");
			} else {
				astro.activarFenomeno(directo, duracion);
				this.enviarInfo(emisor, "Activado: " + directo.getNombreVisible() + " (" + (int) duracion + "s)");
			}
			return;
		}

		this.enviarError(emisor, "Subcomando no reconocido: '" + args[0] + "'. Escribe 'astro ayuda' para ver la sintaxis.");
	}

	private void mostrarEstadoAstronomico(final GestorAstronomico astro, final EmisorRespuesta emisor) {
		final FenomenoAstronomico fen = astro.getFenomenoActivo();
		final FaseLunar fase = astro.getFaseLunarActual();

		this.enviarInfo(emisor, "ESTADO DEL FIRMAMENTO Y BÓVEDA CELESTE:"
				+ "\n -> Fenómeno Activo : " + fen.getNombreVisible()
				+ "\n -> Hostilidad IA    : " + String.format("%.1fx", fen.getMultiplicadorHostilidadIA())
				+ "\n -> Fase Lunar      : " + fase.getNombreVisible() + " (Alpha Nocturno: " + fase.getAlphaPenumbraNocturna() + "/255)"
				+ "\n -> Hora / Momento  : " + astro.getHoraFormato24h() + " (" + astro.getNombreDiaSemanaLargo() + ")"
				+ "\n -> Estación / Día  : " + astro.getEstacionActual().getNombre() + " · Día " + astro.getDiaDeLaEstacion() + "/28 (Día Global " + astro.getDiaActual() + ")"
				+ "\n -> Fechas Clave    : Solsticio Verano (Día 42) | Noche de Brujas (Día 84)"
				+ "\n (Escribe 'astro ayuda' para ver los comandos de control)");
	}

	private FenomenoAstronomico parsearFenomeno(final String str) {
		final String clean = str.toUpperCase().trim().replace(" ", "_");
		try {
			return FenomenoAstronomico.valueOf(clean);
		} catch (final IllegalArgumentException ignored) {
		}

		if (clean.contains("ROJA") || clean.contains("BLOOD") || clean.contains("SANGRE")) {
			return FenomenoAstronomico.LUNA_ROJA;
		}
		if (clean.contains("ECLIPSE") || clean.contains("SOLAR")) {
			return FenomenoAstronomico.ECLIPSE_SOLAR;
		}
		if (clean.contains("AURORA") || clean.contains("BOREAL")) {
			return FenomenoAstronomico.AURORA_BOREAL;
		}
		if (clean.contains("ESTRELLA") || clean.contains("METEORO") || clean.contains("STAR")) {
			return FenomenoAstronomico.LLUVIA_ESTRELLAS;
		}
		if (clean.contains("CONJUNCION") || clean.contains("ASTRAL")) {
			return FenomenoAstronomico.CONJUNCION_ASTRAL;
		}
		if (clean.equals("NORMAL") || clean.equals("CLEAR") || clean.equals("OFF")) {
			return FenomenoAstronomico.NORMAL;
		}
		return null;
	}

	private int parsearOffsetFaseLunar(final String str) {
		final String s = str.toUpperCase().trim().replace(" ", "_");
		if (s.contains("NUEVA") || s.equals("NEW")) {
			return 1; // Días 1 a 3
		}
		if (s.contains("CRECIENTE_CONCAVA") || s.equals("CRECIENTE")) {
			return 5;
		}
		if (s.contains("CUARTO_CRECIENTE")) {
			return 8;
		}
		if (s.contains("GIBOSA_CRECIENTE")) {
			return 12;
		}
		if (s.contains("LLENA") || s.equals("FULL")) {
			return 15; // Días 14 a 17
		}
		if (s.contains("GIBOSA_MENGUANTE")) {
			return 19;
		}
		if (s.contains("CUARTO_MENGUANTE")) {
			return 22;
		}
		if (s.contains("MENGUANTE")) {
			return 26;
		}
		return -1;
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO ASTRO (BÓVEDA CELESTE) ==="
				+ "\n1. Consultas:"
				+ "\n   - astro                 -> Muestra estado celeste, fase lunar y eventos activos"
				+ "\n2. Forzar Escenarios Canónicos:"
				+ "\n   - astro forzar lunaroja -> Ajusta hora y luna para iniciar Noche de Sangre"
				+ "\n   - astro forzar eclipse  -> Ajusta hora y luna para iniciar Eclipse Solar"
				+ "\n3. Activar Eventos Específicos:"
				+ "\n   - astro evento aurora [seg]     -> Despliega auroras boreales en el cielo"
				+ "\n   - astro evento estrellas [seg]  -> Lluvia cósmica de estrellas fugaces"
				+ "\n   - astro evento conjuncion [seg] -> Conjunción astral mística"
				+ "\n   - astro off                     -> Desactiva cualquier evento y restaura cielo"
				+ "\n4. Control de Fases Lunares (Mes de 28 días):"
				+ "\n   - astro luna nueva | creciente | llena | menguante";
		this.enviarInfo(emisor, ayuda);
	}
}