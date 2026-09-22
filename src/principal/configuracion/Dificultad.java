package principal.configuracion;

/**
 * Modos de dificultad con modulación de inercia térmica, metabolismo
 * (hambre/sed) y reglas de muerte permanente/dinástica (Zero-GC / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8 - Hardcore Dynastic & Metabolic Integration)
 */
public enum Dificultad {

	FACIL("Fácil", "Menor daño, mayor tolerancia térmica, sin muerte por inanición y rescate de inventario.",
			new double[] { 0.20, 0.50, 1.00 }, 0.70, 0.75, 0.75, 0.0, false, false),

	NORMAL("Normal", "Supervivencia equilibrada. Inanición letal y pérdida parcial de inventario al caer.",
			new double[] { 0.25, 1.00, 2.50 }, 1.00, 1.00, 1.00, 1.0, false, false),

	DIFICIL("Difícil", "Clima implacable, rápida deshidratación con calor y penalizaciones severas.",
			new double[] { 0.50, 2.00, 4.50 }, 1.30, 1.25, 1.30, 2.0, false, false),

	HARDCORE_RENACIMIENTO("Hardcore: Legado",
			"Mundo persistente. Al morir pasan 7 días, se erige un memorial y un nuevo pionero toma el relevo.",
			new double[] { 0.50, 2.00, 4.50 }, 1.30, 1.25, 1.30, 2.5, true, false),

	HARDCORE_REAL("Hardcore: Real", "Muerte definitiva sin red. Un error letal purga la partida por completo.",
			new double[] { 0.60, 2.50, 5.00 }, 1.40, 1.35, 1.40, 3.0, false, true);

	private final String nombre;
	private final String descripcion;
	private final double[] danioHipotermia;
	private final double factorInerciaTermica;

	// === MULTIPLICADORES METABÓLICOS (BLOQUE 2) ===
	private final double multGastoHambre;
	private final double multGastoSed;
	private final double danioInanicion; // Daño recibido cada 3 segundos al estar a 0% hambre

	// === BANDERAS DINÁSTICAS (BLOQUE 7) ===
	private final boolean muertePermaneceMundo;
	private final boolean muerteBorraPartida;

	Dificultad(final String nombre, final String descripcion, final double[] danioHipotermia,
			final double factorInercia, final double multHambre, final double multSed, final double danioInanicion,
			final boolean muertePermaneceMundo, final boolean muerteBorraPartida) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.danioHipotermia = danioHipotermia;
		this.factorInerciaTermica = factorInercia;
		this.multGastoHambre = multHambre;
		this.multGastoSed = multSed;
		this.danioInanicion = danioInanicion;
		this.muertePermaneceMundo = muertePermaneceMundo;
		this.muerteBorraPartida = muerteBorraPartida;
	}

	public String getNombre() {
		return this.nombre;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public double getDanioHipotermia(final int nivel) {
		final int idx = Math.max(1, Math.min(3, nivel)) - 1;
		return this.danioHipotermia[idx];
	}

	public double getFactorInerciaTermica() {
		return this.factorInerciaTermica;
	}

	public double getMultGastoHambre() {
		return this.multGastoHambre;
	}

	public double getMultGastoSed() {
		return this.multGastoSed;
	}

	public double getDanioInanicion() {
		return this.danioInanicion;
	}

	public boolean esHardcore() {
		return this.muertePermaneceMundo || this.muerteBorraPartida;
	}

	public boolean isMuertePermaneceMundo() {
		return this.muertePermaneceMundo;
	}

	public boolean isMuerteBorraPartida() {
		return this.muerteBorraPartida;
	}
}