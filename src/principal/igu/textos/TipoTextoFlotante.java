package principal.igu.textos;

import java.awt.Color;

/**
 * Catálogo maestro de presets visuales, cromáticos y temporales para textos
 * flotantes de combate y notificaciones fijas de interfaz (Zero-GC).
 * 
 * @version 3.0 (Vanilla Java 8 - Extended 16-Preset Catalog)
 */
public enum TipoTextoFlotante {

	// =========================================================================
	// 1. COMBATE, BALÍSTICA Y DAÑO
	// =========================================================================
	/** Daño físico o balístico normal recibido o infligido. */
	DANIO_NORMAL(new Color(255, 255, 255), 9f, false, 900),

	/** Golpe crítico demoledor o tiro a la cabeza con salto amplificado. */
	CRITICO(new Color(255, 55, 55), 13f, true, 1300),

	/** Ataque completamente bloqueado por armadura o escudo. */
	BLOQUEO(new Color(170, 185, 205), 9f, true, 900),

	/** Ataque fallido o esquiva en combate ("¡FALLO!"). */
	FALLO(new Color(150, 150, 160), 9f, false, 800),

	/** Sanación de vida, pociones curativas o descanso reparador. */
	CURACION(new Color(50, 240, 95), 10f, false, 1000),

	/** Recuperación o absorción de maná / éter. */
	MANA(new Color(60, 200, 255), 9f, false, 900),

	// =========================================================================
	// 2. ESTADOS ALTERADOS Y TOXINAS
	// =========================================================================
	/** Control de masas y aturdimiento ("¡STUN!", "¡DESARME!"). */
	ESTADO(new Color(255, 190, 40), 9f, true, 1100),

	/** Daño continuo por veneno, toxinas o comida podrida. */
	VENENO(new Color(175, 60, 240), 9f, true, 1100),

	/** Daño continuo por fuego, brasas o quemaduras. */
	QUEMADURA(new Color(255, 110, 30), 9f, true, 1100),

	// =========================================================================
	// 3. METABOLISMO, CLIMA Y FISIOLOGÍA
	// =========================================================================
	/** Aporte calórico e ingesta de alimentos ("+25 Nutrición"). */
	COMIDA_HAMBRE(new Color(255, 175, 50), 9f, false, 1000),

	/** Hidratación y saciedad de sed ("+35 Hidratación"). */
	AGUA_SED(new Color(60, 210, 255), 9f, false, 1000),

	/** Alerta fisiológica de frío glacial o congelamiento. */
	FRIO_HIPOTERMIA(new Color(130, 225, 255), 9f, true, 1200),

	/** Alerta fisiológica de bochorno o golpe de calor. */
	CALOR_HIPERTERMIA(new Color(255, 130, 80), 9f, true, 1200),

	// =========================================================================
	// 4. ECONOMÍA, RECOLECCIÓN Y PROGRESIÓN
	// =========================================================================
	/** Recolección de monedas, botín de oro o experiencia (XP). */
	ORO_EXP(new Color(255, 225, 30), 9f, false, 1000),

	/** Fabricación exitosa de herramientas, armas o recetas en taller. */
	CRAFTEO_EXITO(new Color(80, 220, 160), 10f, true, 1200),

	// =========================================================================
	// 5. SISTEMA, MENÚS Y AVISOS FIJOS EN PANTALLA
	// =========================================================================
	/** Notificaciones generales del sistema, guardados o avisos de red. */
	AVISO_SISTEMA(new Color(240, 245, 255), 10f, true, 1400);

	private final Color color;
	private final float tamanoFuente;
	private final boolean esCritico;
	private final int duracionMs;

	TipoTextoFlotante(final Color color, final float tamanoFuente, final boolean esCritico, final int duracionMs) {
		this.color = color;
		this.tamanoFuente = tamanoFuente;
		this.esCritico = esCritico;
		this.duracionMs = duracionMs;
	}

	public Color getColor() {
		return this.color;
	}

	public float getTamanoFuente() {
		return this.tamanoFuente;
	}

	public boolean isCritico() {
		return this.esCritico;
	}

	public int getDuracionMs() {
		return this.duracionMs;
	}
}