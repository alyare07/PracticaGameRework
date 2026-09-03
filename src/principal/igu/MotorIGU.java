package principal.igu;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;

/**
 * Motor central de interfaz de usuario del juego (HUD 1:1). Integra barras de
 * estado, dial astrológico, termómetro y la fila de efectos de estado activos.
 * 
 * @version 2.1 (Vanilla Java 8 - Status Effect HUD Integration)
 */
public class MotorIGU {

	private final BarraVida BARRA_VIDA;
	private final BarraEstamina BARRA_ESTAMINA;
	private final BarraJefe BARRA_JEFE;
	private final RelojCiclo RELOJ_CICLO;
	private final TermometroIGU TERMOMETRO;
	private final EfectosEstadoIGU EFECTOS_ESTADO;

	public MotorIGU() {
		this.BARRA_VIDA = new BarraVida(new Rectangle(6, 336, 84, 9));
		this.BARRA_ESTAMINA = new BarraEstamina(new Rectangle(6, 347, 84, 9));
		this.BARRA_JEFE = new BarraJefe();
		this.RELOJ_CICLO = new RelojCiclo();
		this.TERMOMETRO = new TermometroIGU();
		this.EFECTOS_ESTADO = new EfectosEstadoIGU();
	}

	public void actualizar() {
		this.BARRA_VIDA.actualizar();
		this.BARRA_ESTAMINA.actualizar();
		this.BARRA_JEFE.actualizar();
		this.RELOJ_CICLO.actualizar();
		this.TERMOMETRO.actualizar();
		this.EFECTOS_ESTADO.actualizar();
	}

	public void pintar(final Graphics2D g) {
		this.BARRA_VIDA.pintar(g);
		this.BARRA_ESTAMINA.pintar(g);
		this.BARRA_JEFE.pintar(g);
		this.RELOJ_CICLO.pintar(g);
		this.TERMOMETRO.pintar(g);
		this.EFECTOS_ESTADO.pintar(g);
		this.EFECTOS_ESTADO.pintarTooltips(g);
	}

	public void fijarJefe(final Criatura jefe) {
		this.BARRA_JEFE.asignarJefe(jefe);
	}

	public void desvincularJefe() {
		this.BARRA_JEFE.desvincularJefe();
	}

	public BarraJefe getBarraJefe() {
		return this.BARRA_JEFE;
	}

	public RelojCiclo getRelojCiclo() {
		return this.RELOJ_CICLO;
	}

	public TermometroIGU getTermometro() {
		return this.TERMOMETRO;
	}

	public EfectosEstadoIGU getEfectosEstado() {
		return this.EFECTOS_ESTADO;
	}
}