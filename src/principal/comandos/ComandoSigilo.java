package principal.comandos;

import principal.utilidades.Globales;

/**
 * Comando para auditar el estado de iluminación, penumbra y visibilidad del
 * jugador frente a la IA enemiga.
 */
public class ComandoSigilo extends Comando {

	public ComandoSigilo() {
		super("sigilo", "sigilo", "Muestra el nivel de claridad y estado de visibilidad del jugador para la IA.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.JUGADOR == null) || (Globales.GESTOR_LUZ == null)) {
			this.enviarError(emisor, "Jugador o GestorLuz no listos.");
			return;
		}

		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();

		final boolean iluminado = Globales.GESTOR_LUZ.isPosicionIluminada(jx, jy);
		final float nivelLuz = Globales.GESTOR_LUZ.getNivelLuzEn(jx, jy);
		final int porcentajeLuz = (int) Math.round(nivelLuz * 100.0);

		// Construimos el bloque completo para enviarlo en un único frame/paquete TCP a
		// Termux
		final StringBuilder sb = new StringBuilder();
		sb.append("\n=================== ESTADO DE SIGILO / IA ===================\n");
		sb.append("Posicion Jugador     : (").append((int) jx).append(", ").append((int) jy).append(")\n");
		sb.append("¿Esta Iluminado?     : ").append(iluminado ? "SI (Visible a distancia)" : "NO (Oculto en Penumbra)")
				.append("\n");
		sb.append("Nivel de Claridad    : ").append(porcentajeLuz).append("%\n");
		sb.append("Oscuridad Ambiental  : ").append(Globales.GESTOR_LUZ.getAlphaOscuridadActual()).append(" / 255\n");

		if (Globales.GESTOR_LUZ.getCiclo() != null) {
			sb.append("Fase del Dia / Hora  : ").append(Globales.GESTOR_LUZ.getCiclo().getHoraFormato24h()).append(" (")
					.append(Globales.GESTOR_LUZ.getCiclo().getNombreMomentoDelDia()).append(")\n");
		}

		sb.append("Modo Interior/Cueva  : ").append(Globales.GESTOR_LUZ.isModoAmbienteFijo() ? "SI" : "NO (Exterior)")
				.append("\n");
		sb.append("=============================================================");

		if (emisor != null) {
			emisor.enviarMensaje(sb.toString());
		} else {
			System.out.println(sb.toString());
		}
	}
}