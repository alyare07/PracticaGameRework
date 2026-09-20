package principal.astronomia;

import java.awt.Color;

/**
 * Catálogo de estados y eventos extraordinarios de la bóveda celeste.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum FenomenoAstronomico {

	NORMAL("Cielo Despejado", null, 0.0, false, false, 1.0),
	LUNA_ROJA("Luna de Sangre", new Color(145, 15, 20, 185), 0.85, false, true, 1.5),
	ECLIPSE_SOLAR("Eclipse Solar", new Color(110, 20, 35, 210), 0.90, true, false, 1.2),
	LLUVIA_ESTRELLAS("Lluvia de Meteoros", new Color(20, 35, 65, 140), 0.60, false, true, 1.0),
	AURORA_BOREAL("Aurora Boreal", new Color(20, 75, 65, 175), 0.70, false, true, 0.9),
	CONJUNCION_ASTRAL("Conjunción Astral", new Color(70, 25, 110, 180), 0.70, false, true, 1.1);

	private final String nombreVisible;
	private final Color tinteLuz;
	private final double intensidadTinte;
	private final boolean esDiurno;
	private final boolean esNocturno;
	private final double multiplicadorHostilidadIA;

	FenomenoAstronomico(final String nombreVisible, final Color tinteLuz, final double intensidadTinte,
			final boolean esDiurno, final boolean esNocturno, final double multiplicadorHostilidadIA) {
		this.nombreVisible = nombreVisible;
		this.tinteLuz = tinteLuz;
		this.intensidadTinte = intensidadTinte;
		this.esDiurno = esDiurno;
		this.esNocturno = esNocturno;
		this.multiplicadorHostilidadIA = multiplicadorHostilidadIA;
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public Color getTinteLuz() {
		return this.tinteLuz;
	}

	public double getIntensidadTinte() {
		return this.intensidadTinte;
	}

	public boolean esDiurno() {
		return this.esDiurno;
	}

	public boolean esNocturno() {
		return this.esNocturno;
	}

	public double getMultiplicadorHostilidadIA() {
		return this.multiplicadorHostilidadIA;
	}
}