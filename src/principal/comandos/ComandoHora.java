package principal.comandos;

import principal.astronomia.Estacion;
import principal.astronomia.GestorAstronomico;
import principal.astronomia.GestorAstronomico.FaseDia;
import principal.utilidades.Globales;

/**
 * Comando para el control del reloj solar de 24 horas, calendario canónico RPG
 * (112 días / 16 semanas), fotoperiodo dinámico y saltos estacionales.
 * 
 * @version 5.0 (Vanilla Java 8 - Sovereign Astronomical Command)
 */
public class ComandoHora extends Comando {

	public ComandoHora() {
		super("hora",
				"hora <0-24 | fase | dia <num|+1> | semana <1-16> | estacion <nombre> [dia] | speed <mult|normal> | pausar | reanudar | ayuda>",
				"Ajusta la hora, salta a fases solares, modifica el calendario o viaja entre estaciones.");
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

		// 1. Consulta de Estado Completo (sin argumentos)
		if (args.length == 0) {
			final Estacion est = astro.getEstacionActual();
			final int diaEst = astro.getDiaDeLaEstacion();
			final int semEst = astro.getSemanaDeLaEstacion();
			final int diaAnio = astro.getDiaDelAnio();
			final int semAnio = astro.getSemanaAnio();
			final int anio = astro.getAnioActual();

			final int hAm = (int) astro.getHoraAmanecer();
			final int mAm = (int) Math.round((astro.getHoraAmanecer() - hAm) * 60.0);
			final int hAt = (int) astro.getHoraAtardecer();
			final int mAt = (int) Math.round((astro.getHoraAtardecer() - hAt) * 60.0);

			final String strAmanecer = String.format("%02d:%02d", hAm, mAm);
			final String strAtardecer = String.format("%02d:%02d", hAt, mAt);

			this.enviarInfo(emisor,
					"ESTADO DEL CALENDARIO Y RELOJ SOLAR:" + "\n -> HUD Linea 1   : " + astro.getTextoLinea1HUD()
							+ "\n -> HUD Linea 2   : " + astro.getTextoLinea2HUD() + "\n -> Estacion      : "
							+ est.getNombre() + " (Dia " + diaEst + "/28 | Sem " + semEst + "/4)"
							+ "\n -> Fase Lunar    : " + astro.getFaseLunarActual().getNombreVisible()
							+ "\n -> Anual         : Anio " + anio + " | Dia " + diaAnio + "/112 | Sem " + semAnio
							+ "/16" + "\n -> Dia Semana    : " + astro.getNombreDiaSemanaLargo() + " (Indice "
							+ astro.getIndiceDiaSemana() + ")" + "\n -> Dia Absoluto  : " + astro.getDiaActual()
							+ " dias acumulados" + "\n -> Hora Solar    : " + astro.getHoraFormato24h()
							+ "\n -> Fotoperiodo   : Alba " + strAmanecer + " | Ocaso " + strAtardecer
							+ "\n -> Pausado       : " + (astro.isTiempoPausado() ? "SI" : "NO")
							+ "\n (Escribe 'hora ayuda' para ver todos los comandos)");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Pausa y Reanudación
		if (sub.equals("pausar") || sub.equals("pause") || sub.equals("stop")) {
			astro.pausarTiempo();
			this.enviarInfo(emisor, "Reloj solar PAUSADO.");
			return;
		}

		if (sub.equals("reanudar") || sub.equals("play") || sub.equals("resume") || sub.equals("continuar")) {
			astro.reanudarTiempo();
			this.enviarInfo(emisor, "Reloj solar REANUDADO.");
			return;
		}

		// 4. Salto de Estaciones
		if (sub.equals("estacion") || sub.equals("season")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Indica la estacion: PRIMAVERA, VERANO, OTONO o INVIERNO.");
				return;
			}

			final int offsetEstacion = this.parsearOffsetEstacion(args[1]);
			if (offsetEstacion < 0) {
				this.enviarError(emisor,
						"Estacion desconocida: '" + args[1] + "'. Opciones: PRIMAVERA, VERANO, OTONO, INVIERNO.");
				return;
			}

			final int diaEnEstacion = (args.length >= 3) ? Math.max(1, Math.min(28, this.parsearEntero(args[2], 1)))
					: 1;
			final int anioActual = astro.getAnioActual();
			final int diaGlobalDestino = ((anioActual - 1) * GestorAstronomico.DIAS_POR_ANIO) + offsetEstacion
					+ diaEnEstacion;

			astro.setDiaActual(diaGlobalDestino);
			this.enviarInfo(emisor, "Viaje estacional exitoso -> " + astro.getTextoLinea1HUD() + " ["
					+ astro.getEstacionActual().getNombre() + " Dia " + diaEnEstacion + "]");
			return;
		}

		// 5. Salto de Semanas
		if (sub.equals("semana") || sub.equals("week")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Semana actual: " + astro.getSemanaAnio() + "/16 ("
						+ astro.getEstacionActual().getNombre() + ")");
				return;
			}
			final int semObjetivo = Math.max(1, Math.min(16, this.parsearEntero(args[1], 1)));
			final int anioActual = astro.getAnioActual();
			final int diaGlobalDestino = ((anioActual - 1) * GestorAstronomico.DIAS_POR_ANIO)
					+ ((semObjetivo - 1) * GestorAstronomico.DIAS_POR_SEMANA) + 1;

			astro.setDiaActual(diaGlobalDestino);
			this.enviarInfo(emisor, "Calendario situado en: " + astro.getTextoLinea1HUD());
			return;
		}

		// 6. Control de Días
		if (sub.equals("dia") || sub.equals("day")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Dia actual del calendario: " + astro.getTextoLinea1HUD());
				return;
			}

			final String argDia = args[1].trim();
			if (argDia.startsWith("+")) {
				final int incremento = this.parsearEntero(argDia.substring(1), 1);
				astro.setDiaActual(astro.getDiaActual() + incremento);
				this.enviarInfo(emisor, "Calendario avanzado +" + incremento + " dias -> " + astro.getTextoLinea1HUD());
			} else {
				final int nuevoDia = this.parsearEntero(argDia, -1);
				if (nuevoDia >= 1) {
					astro.setDiaActual(nuevoDia);
					this.enviarInfo(emisor, "Calendario establecido a: " + astro.getTextoLinea1HUD());
				} else {
					this.enviarError(emisor, "Numero de dia invalido. Uso: 'hora dia 5' o 'hora dia +1'");
				}
			}
			return;
		}

		// 7. Fases Solares Predefinidas
		switch (sub) {
		case "medianoche":
		case "midnight":
			astro.setHora(FaseDia.MEDIANOCHE);
			this.enviarInfo(emisor, "Hora establecida a Medianoche (00:00).");
			return;
		case "madrugada":
			astro.setHora(FaseDia.MADRUGADA);
			this.enviarInfo(emisor, "Hora establecida a Madrugada (04:30).");
			return;
		case "amanecer":
		case "sunrise":
			astro.setHora(astro.getHoraAmanecer());
			this.enviarInfo(emisor, "Hora establecida al Amanecer dinamico (" + astro.getHoraFormato24h() + ").");
			return;
		case "manana":
		case "mañana":
		case "morning":
			astro.setHora(FaseDia.MANANA);
			this.enviarInfo(emisor, "Hora establecida a Mañana (08:00).");
			return;
		case "mediodia":
		case "mediodía":
		case "noon":
		case "dia":
			astro.setHora(FaseDia.MEDIODIA);
			this.enviarInfo(emisor, "Hora establecida a Mediodía (12:00).");
			return;
		case "tarde":
		case "afternoon":
			astro.setHora(FaseDia.TARDE);
			this.enviarInfo(emisor, "Hora establecida a Tarde (15:00).");
			return;
		case "atardecer":
		case "sunset":
			astro.setHora(astro.getHoraAtardecer());
			this.enviarInfo(emisor, "Hora establecida al Atardecer dinamico (" + astro.getHoraFormato24h() + ").");
			return;
		case "crepusculo":
		case "crepúsculo":
		case "twilight":
			astro.setHora(FaseDia.CREPUSCULO);
			this.enviarInfo(emisor, "Hora establecida a Crepúsculo (19:00).");
			return;
		case "anochecer":
		case "dusk":
			astro.setHora(FaseDia.NOCHE);
			this.enviarInfo(emisor, "Hora establecida a Anochecer (20:30).");
			return;
		case "noche":
		case "night":
			astro.setHora(astro.getHoraAtardecer() + 2.5);
			this.enviarInfo(emisor, "Hora establecida a Noche cerrada.");
			return;
		default:
			break;
		}

		// 8. Hora Numérica Directa
		final double horaNumerica = this.parsearDouble(args[0], -1.0);
		if ((horaNumerica >= 0.0) && (horaNumerica <= 24.0)) {
			astro.setHora(horaNumerica);
			this.enviarInfo(emisor, "Hora fijada en: " + astro.getHoraFormato24h());
		} else {
			this.enviarError(emisor,
					"Parametro no reconocido: '" + args[0] + "'\nEscribe 'hora ayuda' para ver las opciones.");
		}
	}

	private int parsearOffsetEstacion(final String str) {
		final String s = str.toUpperCase().trim();
		if (s.contains("PRIMAVERA") || s.equals("SPRING")) {
			return 0;
		}
		if (s.contains("VERANO") || s.equals("SUMMER")) {
			return 28;
		}
		if (s.contains("OTONO") || s.contains("OTOÑO") || s.equals("AUTUMN") || s.equals("FALL")) {
			return 56;
		}
		if (s.contains("INVIERNO") || s.equals("WINTER")) {
			return 84;
		}
		return -1;
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO HORA ===" + "\n1. Ajuste Horario Decimal:"
				+ "\n   - hora 7.5          -> Fija las 07:30 AM" + "\n   - hora 18           -> Fija las 18:00 PM"
				+ "\n2. Fases Solares Dinamicas:" + "\n   - hora amanecer | mediodia | atardecer | noche"
				+ "\n3. Control de Estaciones y Calendario:"
				+ "\n   - hora estacion verano       -> Salta al Dia 1 de Verano"
				+ "\n   - hora estacion invierno 14  -> Salta al Dia 14 de Invierno"
				+ "\n   - hora semana 5              -> Salta a la Semana 5"
				+ "\n   - hora dia 85                -> Salta al Dia 85 global"
				+ "\n   - hora dia +1                -> Avanza un dia" + "\n4. Pausa:"
				+ "\n   - hora pausar   | hora reanudar";
		this.enviarInfo(emisor, ayuda);
	}
}