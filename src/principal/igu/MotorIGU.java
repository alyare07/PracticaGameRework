package principal.igu;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;

public class MotorIGU {

	private final BarraVida BARRA_VIDA;
	private final BarraEstamina BARRA_ESTAMINA;
	private final BarraJefe BARRA_JEFE;

	public MotorIGU() {
		// Barras de 84 px de ancho x 9 px de alto ubicadas en la esquina inferior
		// izquierda
		this.BARRA_VIDA = new BarraVida(new Rectangle(6, 336, 84, 9));
		this.BARRA_ESTAMINA = new BarraEstamina(new Rectangle(6, 347, 84, 9));
		this.BARRA_JEFE = new BarraJefe();
	}

	public void actualizar() {
		this.BARRA_VIDA.actualizar();
		this.BARRA_ESTAMINA.actualizar();
		this.BARRA_JEFE.actualizar();
	}

	public void pintar(final Graphics2D g) {
		this.BARRA_VIDA.pintar(g);
		this.BARRA_ESTAMINA.pintar(g);
		this.BARRA_JEFE.pintar(g);
	}

	// =========================================================================
	// === ACCESO Y GESTIÓN DEL JEFE ACTIVO
	// =========================================================================

	public void fijarJefe(final Criatura jefe) {
		this.BARRA_JEFE.asignarJefe(jefe);
	}

	public void desvincularJefe() {
		this.BARRA_JEFE.desvincularJefe();
	}

	public BarraJefe getBarraJefe() {
		return this.BARRA_JEFE;
	}
}