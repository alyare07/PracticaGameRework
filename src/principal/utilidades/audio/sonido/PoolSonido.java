package principal.utilidades.audio.sonido;

public class PoolSonido {

	private static final int CANTIDAD_VOCES_POR_DEFECTO = 6;
	private final SonidoJavaSound[] voces;
	private int punteroVoz = 0;

	public PoolSonido(final SonidoJavaSound sonidoBase) {
		this(sonidoBase, CANTIDAD_VOCES_POR_DEFECTO);
	}

	public PoolSonido(final SonidoJavaSound sonidoBase, final int cantidadVoces) {
		final int totalVoces = Math.max(1, cantidadVoces);
		this.voces = new SonidoJavaSound[totalVoces];

		if (sonidoBase != null) {
			this.voces[0] = sonidoBase;
			for (int i = 1; i < totalVoces; i++) {
				// Clona el sonido reutilizando el mismo buffer de bytes PCM en RAM (Zero-GC)
				this.voces[i] = sonidoBase.clonar();
			}
		}
	}

	public void reproducir() {
		final SonidoJavaSound voz = this.obtenerSiguienteVoz();
		if (voz != null) {
			voz.resetVolumen();
			voz.reproducir();
		}
	}

	public void reproducirConVolumen(final double volumen) {
		final SonidoJavaSound voz = this.obtenerSiguienteVoz();
		if (voz != null) {
			voz.setVolumen(volumen);
			voz.reproducir();
		}
	}

	private SonidoJavaSound obtenerSiguienteVoz() {
		if ((this.voces == null) || (this.voces.length == 0)) {
			return null;
		}
		final SonidoJavaSound voz = this.voces[this.punteroVoz];
		this.punteroVoz = (this.punteroVoz + 1) % this.voces.length;
		return voz;
	}

	public double getVolumenPorDefecto() {
		return ((this.voces != null) && (this.voces.length > 0) && (this.voces[0] != null))
				? this.voces[0].getVolumenPorDefecto()
				: 1.0;
	}
}