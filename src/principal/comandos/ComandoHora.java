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
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			this.enviarError(emisor, "El subsistema de iluminacion no esta inicializado.");
			return;
		}

		final CicloDiaNoche ciclo = Globales.GESTOR_LUZ.getCiclo();

		if (args.length == 0) {
			this.enviarInfo(emisor, "Hora actual: " + ciclo.getHoraFormato24h() + " (" + ciclo.getNombreMomentoDelDia()
					+ ") | Pausado: " + ciclo.isTiempoPausado());
			return;
		}

		final String sub = args[0].toLowerCase();

		if (sub.equals("pausar") || sub.equals("pause")) {
			ciclo.pausarTiempo();
			this.enviarInfo(emisor, "Reloj solar pausado.");
			return;
		}

		if (sub.equals("reanudar") || sub.equals("play")) {
			ciclo.reanudarTiempo();
			this.enviarInfo(emisor, "Reloj solar reanudado.");
			return;
		}

		switch (sub) {
		case "medianoche":
			ciclo.irAMedianoche();
			this.enviarInfo(emisor, "Hora establecida a Medianoche (00:00).");
			return;
		case "amanecer":
			ciclo.irAAmanecer();
			this.enviarInfo(emisor, "Hora establecida a Amanecer (06:30).");
			return;
		case "dia":
		case "mediodia":
			ciclo.irAMediodia();
			this.enviarInfo(emisor, "Hora establecida a Mediodía (12:00).");
			return;
		case "tarde":
		case "atardecer":
			ciclo.irAAtardecer();
			this.enviarInfo(emisor, "Hora establecida a Atardecer (17:30).");
			return;
		case "noche":
			ciclo.irANoche();
			this.enviarInfo(emisor, "Hora establecida a Noche (21:30).");
			return;
		default:
			break;
		}

		final double hora = this.parsearDouble(args[0], -1.0);
		if ((hora >= 0.0) && (hora <= 24.0)) {
			ciclo.setHora(hora);
			this.enviarInfo(emisor,
					"Hora ajustada a: " + ciclo.getHoraFormato24h() + " (" + ciclo.getNombreMomentoDelDia() + ")");
		} else {
			final StringBuilder sb = new StringBuilder("Hora invalida. Opciones: ");
			for (final FaseDia f : FaseDia.values()) {
				sb.append(f.name().toLowerCase()).append(" ");
			}
			this.enviarError(emisor, sb.toString());
		}
	}
}