package principal.configuracion;

public enum Dificultad {

	FACIL("Fácil", "Menor daño recibido, mayor tolerancia térmica y rescate de aliados.",
			new double[] { 0.20, 0.50, 1.00 }, 0.70),

	NORMAL("Normal", "Experiencia equilibrada y desafiante estándar.", new double[] { 0.25, 1.00, 2.50 }, 1.00),

	DIFICIL("Difícil", "Mayor daño recibido, climatología implacable y penalizaciones severas.",
			new double[] { 0.50, 2.00, 4.50 }, 1.30);

	private final String nombre;
	private final String descripcion;
	private final double[] danioHipotermia; // Daño final exacto en [Nivel 1, Nivel 2, Nivel 3]
	private final double factorInerciaTermica; // Qué tan rápido cambia la temperatura corporal

	Dificultad(final String nombre, final String descripcion, final double[] danioHipotermia,
			final double factorInercia) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.danioHipotermia = danioHipotermia;
		this.factorInerciaTermica = factorInercia;
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
}