package principal.mapa.persistencia;

public final class IdentificadorEspacial {

	private IdentificadorEspacial() {
	}

	public static String generarClave(final int x, final int y) {
		return "E_" + x + "_" + y;
	}

	public static long generarIdNumerico(final int x, final int y) {
		return (((long) x) << 32) | (y & 0xFFFFFFFFL);
	}
}