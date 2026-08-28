package principal.comandos;

import principal.iluminacion.CicloDiaNoche;
import principal.iluminacion.CicloDiaNoche.FaseDia;
import principal.utilidades.Globales;

public class ComandoHora extends Comando {

	public ComandoHora() {
		super("hora", "hora <0-24 | fase | pausar | reanudar>", 
				"Ajusta la hora del dia, salta a una fase solar o controla el paso del tiempo.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.GESTOR_LUZ == null || Globales.GESTOR_LUZ.getCiclo() == null) {
			System.err.println("[Consola] El subsistema de iluminacion no esta inicializado.");
			return;
		}

		final CicloDiaNoche ciclo = Globales.GESTOR_LUZ.getCiclo();

		if (args.length == 0) {
			System.out.println("[Consola] Hora actual: " + ciclo.getHoraFormato24h() 
					+ " (" + ciclo.getNombreMomentoDelDia() + ") | Pausado: " + ciclo.isTiempoPausado());
			return;
		}

		final String sub = args[0].toLowerCase();

		if (sub.equals("pausar") || sub.equals("pause")) {
			ciclo.pausarTiempo();
			System.out.println("[Consola] Reloj solar pausado.");
			return;
		}

		if (sub.equals("reanudar") || sub.equals("play")) {
			ciclo.reanudarTiempo();
			System.out.println("[Consola] Reloj solar reanudado.");
			return;
		}

		// Atajos directos por nombre de momento del día
		switch (sub) {
		case "medianoche":
			ciclo.irAMedianoche();
			System.out.println("[Consola] Hora establecida a Medianoche (00:00).");
			return;
		case "amanecer":
			ciclo.irAAmanecer();
			System.out.println("[Consola] Hora establecida a Amanecer (06:30).");
			return;
		case "dia":
		case "mediodia":
			ciclo.irAMediodia();
			System.out.println("[Consola] Hora establecida a Mediodía (12:00).");
			return;
		case "tarde":
		case "atardecer":
			ciclo.irAAtardecer();
			System.out.println("[Consola] Hora establecida a Atardecer (17:30).");
			return;
		case "noche":
			ciclo.irANoche();
			System.out.println("[Consola] Hora establecida a Noche (21:30).");
			return;
		default:
			break;
		}

		// Si no fue un atajo de texto, intentamos parsear número decimal (0.0 a 24.0)
		final double hora = this.parsearDouble(args[0], -1.0);
		if (hora >= 0.0 && hora <= 24.0) {
			ciclo.setHora(hora);
			System.out.println("[Consola] Hora ajustada a: " + ciclo.getHoraFormato24h() 
					+ " (" + ciclo.getNombreMomentoDelDia() + ")");
		} else {
			System.err.println("[Consola] Hora invalida. Ingresa un numero entre 0.0 y 24.0 o una fase:");
			for (final FaseDia f : FaseDia.values()) {
				System.err.print(f.name().toLowerCase() + " ");
			}
			System.err.println();
		}
	}
}