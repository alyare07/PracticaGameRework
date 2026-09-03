package principal.clima;

import principal.iluminacion.ZonaAmbiente;
import principal.utilidades.Globales;

/**
 * Gestor y mediador de volúmenes espaciales de biomas y áreas en el mundo.
 * Aplica transiciones progresivas suaves (Lerp) en 60 APS sin crear objetos en
 * memoria Heap (Zero-GC / O(1)).
 * 
 * @version 2.1 (Vanilla Java 8 - Robust State Transition)
 */
public class GestorZonasAmbiente {

	private static final int MAX_ZONAS = 32;
	private static final double VELOCIDAD_TRANSICION = 0.66; // ~1.5 segundos para inmersión total

	private final ZonaAmbiente[] zonas = new ZonaAmbiente[MAX_ZONAS];
	private int cantidadZonas = 0;
	private ZonaAmbiente zonaActual = null;

	public void registrarZona(final ZonaAmbiente zona) {
		if ((zona != null) && (this.cantidadZonas < MAX_ZONAS)) {
			this.zonas[this.cantidadZonas++] = zona;
		}
	}

	public void limpiarZonas() {
		for (int i = 0; i < this.cantidadZonas; i++) {
			this.zonas[i] = null;
		}
		this.cantidadZonas = 0;
		this.zonaActual = null;
	}

	public void actualizar(final double dt) {
		if (Globales.JUGADOR == null) {
			return;
		}

		final double jx = Globales.JUGADOR.getPosicionXInt();
		final double jy = Globales.JUGADOR.getPosicionYInt();

		ZonaAmbiente zonaEncontrada = null;

		// 1. Evaluación espacial en O(N) acotada a MAX_ZONAS
		for (int i = 0; i < this.cantidadZonas; i++) {
			final ZonaAmbiente z = this.zonas[i];

			if (z.contiene(jx, jy)) {
				zonaEncontrada = z;
				z.setFactorInmersion(z.getFactorInmersion() + (VELOCIDAD_TRANSICION * dt));
			} else {
				z.setFactorInmersion(z.getFactorInmersion() - (VELOCIDAD_TRANSICION * dt));
			}
		}

		// 2. Detección de cambio de umbral y modulación
		if (zonaEncontrada != this.zonaActual) {
			this.zonaActual = zonaEncontrada;

			if (zonaEncontrada != null) {
				if (zonaEncontrada.isEsInterior()) {
					Globales.GESTOR_LUZ.establecerAmbienteTransicion(zonaEncontrada.getColorAmbiente(), 1.5);
				} else {
					Globales.GESTOR_LUZ.restablecerModoExterior();
				}
			} else {
				Globales.GESTOR_LUZ.restablecerModoExterior();
			}
		}

		// 3. Modulación continua de tinte y niebla
		if (this.zonaActual != null) {
			final double f = this.zonaActual.getFactorInmersion();

			if (!this.zonaActual.isEsInterior()) {
				Globales.GESTOR_LUZ.setTinteBiomaExterior(this.zonaActual.getColorAmbiente(), f);
			}
			Globales.GESTOR_CLIMA.setNieblaBiomaLocal(this.zonaActual.getNivelNiebla(), f);
		} else {
			Globales.GESTOR_CLIMA.setNieblaBiomaLocal(null, 0.0);
		}
	}

	public ZonaAmbiente getZonaActual() {
		return this.zonaActual;
	}

	public boolean isEnZonaInterior() {
		return (this.zonaActual != null) && this.zonaActual.isEsInterior();
	}

	public int getCantidadZonas() {
		return this.cantidadZonas;
	}
}