package principal.utilidades;

/**
 * Esta Clase gestionara el tiempo teniendo en cuenta
 * los valores System.nanoTime().
 *   Para gestionar los tiempos por actualizacion usar {@link GestorTIempoActualizacion}.
 */
public class GestorTiempo {
	public static final int NS_POR_SEGUNDO = 1000000000;
	public static final int NS_POR_MILISEGUNDO = 1000000;
	public long tiempoEstablecido;

	public GestorTiempo(final long t) {
		this.tiempoEstablecido = t;
	}

	public GestorTiempo() {
		this.tiempoEstablecido = -1*System.nanoTime();
	}

	public boolean transcurrioTiempo() {
		return this.tiempoEstablecido < System.nanoTime();
	}

	public boolean transcurrioSegundos(final int segundos) {
		return ((System.nanoTime() - this.tiempoEstablecido) / NS_POR_SEGUNDO) >= segundos;
	}

	public boolean transcurrioMiliSegundos(final int miliSegundos) {
		return ((System.nanoTime() - this.tiempoEstablecido) / NS_POR_MILISEGUNDO) >= miliSegundos;
	}

	public long getDiferenciaTranscurrida() {
		return System.nanoTime() - this.tiempoEstablecido;
	}

	public int getDiferenciaTranscurridaEnSegundos() {
		return (int) ((System.nanoTime() - this.tiempoEstablecido) / NS_POR_SEGUNDO);
	}

	public int getDiferenciaTranscurridaEnMiliSegundos() {
		return (int) ((System.nanoTime() - this.tiempoEstablecido) / NS_POR_MILISEGUNDO);
	}

	public void establecerReferenciaTiempoActual() {
		this.tiempoEstablecido = System.nanoTime();
	}

	public void establecerReferenciaTiempo(final long t) {
		this.tiempoEstablecido = t;
	}

}
