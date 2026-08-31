package principal.comandos;

import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.CicloDiaNoche.FaseDia;
import principal.utilidades.Globales;

/**
 * Comando para el control del reloj solar de 24 horas, calendario de días y
 * velocidad temporal (Time Warp).
 * <p>
 * Insensible a mayúsculas/minúsculas y compatible con terminales remotas
 * (Termux / Netcat).
 * </p>
 * 
 * @version 3.0
 */
public class ComandoHora extends Comando {

	public ComandoHora() {
		super("hora", "hora <0-24 | fase | dia <num|+1> | speed <mult|normal> | pausar | reanudar | ayuda>",
				"Ajusta la hora, salta a fases solares, modifica el dia o acelera el tiempo.");
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

		// 1. Consulta de Estado (sin argumentos)
		if (args.length == 0) {
			this.enviarInfo(emisor,
					"ESTADO TEMPORAL:" + "\n -> Calendario : " + ciclo.getTextoDia() + "\n -> Hora Solar : "
							+ ciclo.getHoraFormato24h() + " (" + ciclo.getNombreMomentoDelDia() + ")"
							+ "\n -> Velocidad  : " + ciclo.getMultiplicadorTiempo() + "x" + "\n -> Pausado    : "
							+ (ciclo.isTiempoPausado() ? "SI" : "NO")
							+ "\n (Escribe 'hora ayuda' para ver todos los comandos)");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda Detallada
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

		// 4. Control de Días (hora dia <num> / hora dia +1)
		if (sub.equals("dia") || sub.equals("day")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Dia actual del calendario: " + ciclo.getTextoDia());
				return;
			}

			final String argDia = args[1].trim();
			if (argDia.startsWith("+")) {
				final int incremento = this.parsearEntero(argDia.substring(1), 1);
				for (int i = 0; i < incremento; i++) {
					ciclo.avanzarDia();
				}
				this.enviarInfo(emisor, "Calendario avanzado en +" + incremento + " dias -> " + ciclo.getTextoDia());
			} else {
				final int nuevoDia = this.parsearEntero(argDia, -1);
				if (nuevoDia >= 1) {
					ciclo.setDiaActual(nuevoDia);
					this.enviarInfo(emisor, "Calendario establecido a: " + ciclo.getTextoDia());
				} else {
					this.enviarError(emisor, "Numero de dia invalido. Uso: 'hora dia 5' o 'hora dia +1'");
				}
			}
			return;
		}

		// 5. Control de Velocidad (hora speed <mult> / hora speed normal)
		if (sub.equals("speed") || sub.equals("velocidad") || sub.equals("warp")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Velocidad actual: " + ciclo.getMultiplicadorTiempo() + "x");
				return;
			}

			final String argSpeed上下 = args[1].toLowerCase().trim();
			if (argSpeed上下.equals("normal") || argSpeed上下.equals("reset") || argSpeed上下.equals("1")) {
				ciclo.restablecerVelocidadTiempo();
				this.enviarInfo(emisor, "Velocidad temporal restaurada a 1.0x (Normal).");
			} else {
				final double factor = this.parsearDouble(argSpeed上下, -1.0);
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

		// 6. Fases Solares Predefinidas
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
			this.enviarInfo(emisor, "Hora establecida a Amanecer (06:30).");
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
			this.enviarInfo(emisor, "Hora establecida a Atardecer (17:00).");
			return;
		case "crepusculo":
		case "crepúsculo":
		case "twilight":
			ciclo.setHora(FaseDia.CREPUSCULO);
			this.enviarInfo(emisor, "Hora establecida a Crepúsculo (19:00).");
			return;
		case "anochecer":
		case "dusk":
			ciclo.setHora(FaseDia.ANOCHECER);
			this.enviarInfo(emisor, "Hora establecida a Anochecer (20:30).");
			return;
		case "noche":
		case "night":
			ciclo.irANoche();
			this.enviarInfo(emisor, "Hora establecida a Noche (21:30).");
			return;
		default:
			break;
		}

		// 7. Hora Numérica Directa (ej: hora 14.5)
		final double horaNumerica = this.parsearDouble(args[0], -1.0);
		if ((horaNumerica >= 0.0) && (horaNumerica <= 24.0)) {
			ciclo.setHora(horaNumerica);
			this.enviarInfo(emisor,
					"Hora fijada en: " + ciclo.getHoraFormato24h() + " (" + ciclo.getNombreMomentoDelDia() + ")");
		} else {
			this.enviarError(emisor, "Parametro no reconocido: '" + args[0]
					+ "'\nEscribe 'hora ayuda' para ver las opciones disponibles.");
		}
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO HORA ===" + "\n1. Ajuste por hora decimal (0.0 a 24.0):"
				+ "\n   - hora 7.5         -> Fija las 07:30 AM" + "\n   - hora 18          -> Fija las 18:00 PM"
				+ "\n2. Salto a Fases Solares:" + "\n   - hora amanecer | manana | mediodia | tarde"
				+ "\n   - hora atardecer | crepusculo | anochecer | noche | medianoche" + "\n3. Control de Calendario:"
				+ "\n   - hora dia 14      -> Salta al Día 14" + "\n   - hora dia +1      -> Avanza al siguiente día"
				+ "\n4. Velocidad Temporal (Time Warp):" + "\n   - hora speed 20    -> Corre a 20x de velocidad"
				+ "\n   - hora speed normal-> Restaura a 1.0x" + "\n5. Pausa:" + "\n   - hora pausar | hora reanudar";
		this.enviarInfo(emisor, ayuda);
	}
}