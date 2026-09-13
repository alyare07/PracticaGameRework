package principal.comandos;

import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.CicloDiaNoche.FaseDia;
import principal.iluminacion.Estacion;
import principal.utilidades.Globales;

/**
 * Comando para el control del reloj solar de 24 horas, calendario canónico RPG
 * (112 días / 16 semanas), fotoperiodo dinámico y saltos estacionales.
 * <p>
 * Insensible a mayúsculas/minúsculas y compatible con terminales remotas
 * (Termux / Netcat).
 * </p>
 * 
 * @version 4.0 (Vanilla Java 8 - Canonical Calendar & Season Warp Support)
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
		if ((Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			this.enviarError(emisor, "El subsistema de iluminacion no esta inicializado.");
			return;
		}

		final CicloDiaNoche ciclo = Globales.GESTOR_LUZ.getCiclo();

		// 1. Consulta de Estado Completo (sin argumentos)
		if (args.length == 0) {
			final Estacion est = ciclo.getEstacionActual();
			final int diaEst = ciclo.getDiaDeLaEstacion();
			final int semEst = ciclo.getSemanaDeLaEstacion();
			final int diaAnio = ciclo.getDiaDelAnio();
			final int semAnio = ciclo.getSemanaAnio();
			final int anio = ciclo.getAnioActual();

			final int hAm = (int) ciclo.getHoraAmanecer();
			final int mAm = (int) Math.round((ciclo.getHoraAmanecer() - hAm) * 60.0);
			final int hAt = (int) ciclo.getHoraAtardecer();
			final int mAt = (int) Math.round((ciclo.getHoraAtardecer() - hAt) * 60.0);

			final String strAmanecer = String.format("%02d:%02d", hAm, mAm);
			final String strAtardecer = String.format("%02d:%02d", hAt, mAt);

			this.enviarInfo(emisor,
					"ESTADO DEL CALENDARIO Y RELOJ SOLAR:" + "\n -> HUD Linea 1   : " + ciclo.getTextoLinea1HUD()
							+ "\n -> HUD Linea 2   : " + ciclo.getTextoLinea2HUD() + "\n -> Estacion      : "
							+ est.getNombre() + " (Dia " + diaEst + "/28 | Sem " + semEst + "/4)"
							+ "\n -> Anual         : Anio " + anio + " | Dia " + diaAnio + "/112 | Sem " + semAnio
							+ "/16" + "\n -> Dia Semana    : " + ciclo.getNombreDiaSemanaLargo() + " (Indice "
							+ ciclo.getIndiceDiaSemana() + ")" + "\n -> Dia Absoluto  : " + ciclo.getDiaActual()
							+ " dias acumulados" + "\n -> Hora Solar    : " + ciclo.getHoraFormato24h() + " ("
							+ ciclo.getNombreMomentoDelDia() + ")" + "\n -> Fotoperiodo   : Alba " + strAmanecer
							+ " | Ocaso " + strAtardecer + "\n -> Velocidad     : " + ciclo.getMultiplicadorTiempo()
							+ "x | Pausado: " + (ciclo.isTiempoPausado() ? "SI" : "NO")
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
			ciclo.pausarTiempo();
			this.enviarInfo(emisor, "Reloj solar PAUSADO.");
			return;
		}

		if (sub.equals("reanudar") || sub.equals("play") || sub.equals("resume") || sub.equals("continuar")) {
			ciclo.reanudarTiempo();
			this.enviarInfo(emisor, "Reloj solar REANUDADO.");
			return;
		}

		// 4. Salto de Estaciones (hora estacion <nombre> [dia_1_a_28])
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
			final int anioActual = ciclo.getAnioActual();
			final int diaGlobalDestino = ((anioActual - 1) * CicloDiaNoche.DIAS_POR_ANIO) + offsetEstacion
					+ diaEnEstacion;

			ciclo.setDiaActual(diaGlobalDestino);
			this.enviarInfo(emisor, "Viaje estacional exitoso -> " + ciclo.getTextoLinea1HUD() + " ["
					+ ciclo.getEstacionActual().getNombre() + " Dia " + diaEnEstacion + "]");
			return;
		}

		// 5. Salto de Semanas (hora semana <1-16>)
		if (sub.equals("semana") || sub.equals("week")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Semana actual: " + ciclo.getSemanaAnio() + "/16 ("
						+ ciclo.getEstacionActual().getNombre() + ")");
				return;
			}
			final int semObjetivo = Math.max(1, Math.min(16, this.parsearEntero(args[1], 1)));
			final int anioActual = ciclo.getAnioActual();
			final int diaGlobalDestino = ((anioActual - 1) * CicloDiaNoche.DIAS_POR_ANIO)
					+ ((semObjetivo - 1) * CicloDiaNoche.DIAS_POR_SEMANA) + 1;

			ciclo.setDiaActual(diaGlobalDestino);
			this.enviarInfo(emisor, "Calendario situado en: " + ciclo.getTextoLinea1HUD());
			return;
		}

		// 6. Control de Días (hora dia <num> / hora dia +1)
		if (sub.equals("dia") || sub.equals("day")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Dia actual del calendario: " + ciclo.getTextoLinea1HUD());
				return;
			}

			final String argDia = args[1].trim();
			if (argDia.startsWith("+")) {
				final int incremento = this.parsearEntero(argDia.substring(1), 1);
				for (int i = 0; i < incremento; i++) {
					ciclo.avanzarDia();
				}
				this.enviarInfo(emisor, "Calendario avanzado +" + incremento + " dias -> " + ciclo.getTextoLinea1HUD());
			} else {
				final int nuevoDia = this.parsearEntero(argDia, -1);
				if (nuevoDia >= 1) {
					ciclo.setDiaActual(nuevoDia);
					this.enviarInfo(emisor, "Calendario establecido a: " + ciclo.getTextoLinea1HUD());
				} else {
					this.enviarError(emisor, "Numero de dia invalido. Uso: 'hora dia 5' o 'hora dia +1'");
				}
			}
			return;
		}

		// 7. Control de Velocidad (hora speed <mult> / hora speed normal)
		if (sub.equals("speed") || sub.equals("velocidad") || sub.equals("warp")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Velocidad actual: " + ciclo.getMultiplicadorTiempo() + "x");
				return;
			}

			final String argSpeed = args[1].toLowerCase().trim();
			if (argSpeed.equals("normal") || argSpeed.equals("reset") || argSpeed.equals("1")) {
				ciclo.restablecerVelocidadTiempo();
				this.enviarInfo(emisor, "Velocidad temporal restaurada a 1.0x (Normal).");
			} else {
				final double factor = this.parsearDouble(argSpeed, -1.0);
				if (factor >= 0.0) {
					ciclo.setMultiplicadorTiempo(factor);
					this.enviarInfo(emisor, "Velocidad temporal establecida a: " + factor + "x");
				} else {
					this.enviarError(emisor,
							"Factor de velocidad invalido. Ejemplo: 'hora speed 10' o 'hora speed normal'");
				}
			}
			return;
		}

		// 8. Fases Solares Predefinidas
		switch (sub) {
		case "medianoche":
		case "midnight":
			ciclo.irAMedianoche();
			this.enviarInfo(emisor, "Hora establecida a Medianoche (00:00).");
			return;
		case "madrugada":
			ciclo.setHora(FaseDia.MADRUGADA);
			this.enviarInfo(emisor, "Hora establecida a Madrugada (04:30).");
			return;
		case "amanecer":
		case "sunrise":
			ciclo.irAAmanecer();
			this.enviarInfo(emisor, "Hora establecida al Amanecer dinamico (" + ciclo.getHoraFormato24h() + ").");
			return;
		case "manana":
		case "mañana":
		case "morning":
			ciclo.setHora(FaseDia.MANANA);
			this.enviarInfo(emisor, "Hora establecida a Mañana (08:00).");
			return;
		case "mediodia":
		case "mediodía":
		case "noon":
		case "dia":
			ciclo.irAMediodia();
			this.enviarInfo(emisor, "Hora establecida a Mediodía (12:00).");
			return;
		case "tarde":
		case "afternoon":
			ciclo.setHora(FaseDia.TARDE);
			this.enviarInfo(emisor, "Hora establecida a Tarde (15:00).");
			return;
		case "atardecer":
		case "sunset":
			ciclo.irAAtardecer();
			this.enviarInfo(emisor, "Hora establecida al Atardecer dinamico (" + ciclo.getHoraFormato24h() + ").");
			return;
		case "crepusculo":
		case "crepúsculo":
		case "twilight":
			ciclo.setHora(FaseDia.CREPUSCULO);
			this.enviarInfo(emisor, "Hora establecida a Crepúsculo (19:00).");
			return;
		case "anochecer":
		case "dusk":
			ciclo.setHora(FaseDia.NOCHE);
			this.enviarInfo(emisor, "Hora establecida a Anochecer (20:30).");
			return;
		case "noche":
		case "night":
			ciclo.irANoche();
			this.enviarInfo(emisor, "Hora establecida a Noche cerrada.");
			return;
		default:
			break;
		}

		// 9. Hora Numérica Directa (ej: hora 14.5)
		final double horaNumerica = this.parsearDouble(args[0], -1.0);
		if ((horaNumerica >= 0.0) && (horaNumerica <= 24.0)) {
			ciclo.setHora(horaNumerica);
			this.enviarInfo(emisor,
					"Hora fijada en: " + ciclo.getHoraFormato24h() + " (" + ciclo.getNombreMomentoDelDia() + ")");
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
				+ "\n   - hora estacion invierno 14  -> Salta al solsticio de Invierno (Dia 14)"
				+ "\n   - hora semana 5              -> Salta a la Semana 5"
				+ "\n   - hora dia 85                -> Salta al Dia 85 global"
				+ "\n   - hora dia +1                -> Avanza un dia" + "\n4. Velocidad y Pausa:"
				+ "\n   - hora speed 10 | hora speed normal" + "\n   - hora pausar   | hora reanudar";
		this.enviarInfo(emisor, ayuda);
	}
}