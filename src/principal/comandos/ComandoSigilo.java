package principal.comandos;

import principal.utilidades.Globales;

public class ComandoSigilo extends Comando {

	public ComandoSigilo() {
		super("sigilo", "sigilo", "Muestra el nivel de claridad y estado de visibilidad del jugador para la IA.");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (Globales.JUGADOR == null || Globales.GESTOR_LUZ == null) {
			System.err.println("[Consola] Jugador o GestorLuz no listos.");
			return;
		}

		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();

		final boolean iluminado = Globales.GESTOR_LUZ.isPosicionIluminada(jx, jy);
		final float nivelLuz = Globales.GESTOR_LUZ.getNivelLuzEn(jx, jy);
		final int porcentajeLuz = (int) Math.round(nivelLuz * 100.0);

		System.out.println("=================== ESTADO DE SIGILO / IA ===================");
		System.out.println("Posicion Jugador     : (" + (int) jx + ", " + (int) jy + ")");
		System.out.println("¿Esta Iluminado?     : " + (iluminado ? "SI (Visible a distancia)" : "NO (Oculto en Penumbra)"));
		System.out.println("Nivel de Claridad    : " + porcentajeLuz + "%");
		System.out.println("Oscuridad Ambiental  : " + Globales.GESTOR_LUZ.getAlphaOscuridadActual() + " / 255");
		System.out.println("Fase del Dia / Hora  : " + Globales.GESTOR_LUZ.getCiclo().getHoraFormato24h() 
				+ " (" + Globales.GESTOR_LUZ.getCiclo().getNombreMomentoDelDia() + ")");
		System.out.println("Modo Interior/Cueva  : " + (Globales.GESTOR_LUZ.isModoAmbienteFijo() ? "SI" : "NO (Exterior)"));
		System.out.println("=============================================================");
	}
}