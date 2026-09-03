package principal.entes.efectos;

import java.awt.Color;

/**
 * Catálogo central de tipos de efectos de estado (Buffs y Debuffs). Define su
 * naturaleza (positivo/negativo), si permite acumular cargas (stacks), su color
 * identificativo en el HUD y el intervalo de tick para efectos periódicos.
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public enum TipoEfectoEstado {

	// =========================================================================
	// === 1. BUFFS (EFECTOS POSITIVOS)
	// =========================================================================

	REGENERACION("Regeneración", "Restaura salud periódicamente.", true, false, new Color(70, 240, 120), 1.0),
	CELERIDAD("Celeridad", "Aumenta la velocidad de movimiento (+25%).", true, false, new Color(80, 210, 255), 0.0),
	FUERZA("Fuerza Titánica", "Aumenta el daño de los ataques (+30%).", true, false, new Color(255, 180, 50), 0.0),
	RESISTENCIA("Piel de Piedra", "Aumenta la defensa y absorción de daño.", true, false, new Color(220, 225, 240), 0.0),

	// =========================================================================
	// === 2. DEBUFFS TEMPORALES DE COMBATE
	// =========================================================================

	VENENO("Veneno", "Inflige daño tóxico periódico por segundo.", false, true, new Color(130, 240, 60), 1.0),
	SANGRADO("Sangrado", "Pérdida rápida de sangre tras un corte crítico.", false, true, new Color(240, 30, 40), 0.75),
	QUEMADURA("Quemadura", "Daño de fuego continuo e iluminación.", false, false, new Color(255, 110, 25), 0.8),
	ATURDIMIENTO("Aturdimiento", "Incapacitado para moverse o atacar.", false, false, new Color(255, 215, 60), 0.0),

	// =========================================================================
	// === 3. DEBUFFS AMBIENTALES (INFINITOS CONDICIONADOS CON TIEMPO RESIDUAL)
	// =========================================================================

	HIPOTERMIA("Hipotermia", "Entumecimiento por frío (-20% velocidad y temblores).", false, false,
			new Color(90, 190, 255), 2.5),
	HIPERTERMIA("Hipertermia", "Golpe de calor (+Consumo de estamina y mareos).", false, false,
			new Color(255, 75, 65), 3.0);

	private final String nombre;
	private final String descripcion;
	private final boolean esBuff;
	private final boolean acumulable;
	private final Color colorIdentificativo;
	private final double intervaloTick;

	TipoEfectoEstado(final String nombre, final String descripcion, final boolean esBuff, final boolean acumulable,
			final Color colorIdentificativo, final double intervaloTick) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.esBuff = esBuff;
		this.acumulable = acumulable;
		this.colorIdentificativo = colorIdentificativo;
		this.intervaloTick = intervaloTick;
	}

	public String getNombre() {
		return this.nombre;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public boolean isBuff() {
		return this.esBuff;
	}

	public boolean isAcumulable() {
		return this.acumulable;
	}

	public Color getColorIdentificativo() {
		return this.colorIdentificativo;
	}

	public double getIntervaloTick() {
		return this.intervaloTick;
	}

	public boolean tieneTickPeriodico() {
		return this.intervaloTick > 0.0;
	}
}