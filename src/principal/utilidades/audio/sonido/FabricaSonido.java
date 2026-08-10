package principal.utilidades.audio.sonido;

public class FabricaSonido {

	/**
	 * Crea la instancia cargando el archivo e inicializando su volumen por defecto.
	 */
	public static SonidoJavaSound crearSonido(final String ruta, final double volumenPorDefecto) {
		if ((ruta == null) || ruta.trim().isEmpty()) {
			return null;
		}

		return new SonidoJavaSound(ruta, volumenPorDefecto);
	}
}