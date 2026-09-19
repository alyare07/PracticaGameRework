package principal;

public class Main {
	public static GestorPrincipal gp;

	public static void main(final String[] args) {
		// 1. Evita que Windows envíe mensajes WM_ERASEBKGND que provocan parpadeos
		// negros en Canvas
		System.setProperty("sun.awt.noerasebackground", "true");

		// 2. Hack de precisión para Windows NT: fuerza la granularidad del timer del SO
		// a 1.0 ms
		final Thread timerPrecisionHack = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (final InterruptedException ignored) {
				}
			}
		}, "TemporizadorPrecisionWindows");
		timerPrecisionHack.setDaemon(true);
		timerPrecisionHack.start();

		// 3. Inicialización controlada
		gp = new GestorPrincipal();
		gp.iniciarJuego();
		gp.iniciarBuclePrincipal(true);
	}
}