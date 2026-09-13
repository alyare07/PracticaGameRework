package principal.iluminacion;

import java.awt.Color;

/**
 * Catálogo maestro de estaciones para el calendario canónico RPG (112 días / 16 semanas).
 * Cada estación dura exactamente 28 días (4 semanas exactas de 7 días).
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Flyweight)
 */
public enum Estacion {

	PRIMAVERA("Primavera", new Color(110, 240, 130), 0.0),
	VERANO("Verano", new Color(255, 215, 65), 6.5),
	OTONO("Otoño", new Color(245, 140, 50), -1.5),
	INVIERNO("Invierno", new Color(130, 210, 255), -9.0);

	// Arreglo estático pre-asignado para erradicar clonaciones de Heap por .values()
	public static final Estacion[] VALORES = Estacion.values();

	private final String nombre;
	private final Color colorIdentificador;
	private final double deltaTemperaturaCelsius;

	Estacion(final String nombre, final Color colorIdentificador, final double deltaTemperaturaCelsius) {
		this.nombre = nombre;
		this.colorIdentificador = colorIdentificador;
		this.deltaTemperaturaCelsius = deltaTemperaturaCelsius;
	}

	/**
	 * Mapea en O(1) un día del año (0 a 111) a su estación correspondiente.
	 * 
	 * @param diaAnio Día del año continuo (0-indexado: 0 a 111).
	 * @return Instancia de Estacion inmutable.
	 */
	public static Estacion obtenerPorDiaAnio(final int diaAnio) {
		final int indice = Math.max(0, Math.min(3, (diaAnio % 112) / 28));
		return VALORES[indice];
	}

	public String getNombre() {
		return this.nombre;
	}

	public Color getColorIdentificador() {
		return this.colorIdentificador;
	}

	public double getDeltaTemperaturaCelsius() {
		return this.deltaTemperaturaCelsius;
	}
}