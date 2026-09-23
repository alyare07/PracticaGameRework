package principal.igu;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;
import principal.utilidades.Globales;

/**
 * Motor central de interfaz de usuario del juego (HUD 1:1). Integra viñeta
 * climática periférica de pantalla completa, barras de estado, dial
 * astrológico, termómetro con monitoreo térmico avanzado, fila de efectos de
 * estado y control maestro de visibilidad cinemática (Zero-GC / O(1)).
 * 
 * @version 3.0 (Vanilla Java 8 - Atmospheric Screen Vignette Integration)
 */
public class MotorIGU {

	private final VinetaTermicaIGU VINETA_TERMICA;
	private final BarraVida BARRA_VIDA;
	private final BarraEstamina BARRA_ESTAMINA;
	private final BarraJefe BARRA_JEFE;
	private final RelojCiclo RELOJ_CICLO;
	private final TermometroIGU TERMOMETRO;
	private final EfectosEstadoIGU EFECTOS_ESTADO;
	private final MetabolismoIGU METABOLISMO;
	private final BarraEstabilidadCueva BARRA_CUEVA;
	private boolean visible = true;

	public MotorIGU() {
		this.VINETA_TERMICA = new VinetaTermicaIGU();
		this.BARRA_VIDA = new BarraVida(new Rectangle(6, 336, 84, 9));
		this.BARRA_ESTAMINA = new BarraEstamina(new Rectangle(6, 347, 84, 9));
		this.BARRA_JEFE = new BarraJefe();
		this.RELOJ_CICLO = new RelojCiclo();
		this.TERMOMETRO = new TermometroIGU();
		this.EFECTOS_ESTADO = new EfectosEstadoIGU();
		this.METABOLISMO = new MetabolismoIGU();
		this.BARRA_CUEVA = new BarraEstabilidadCueva();
	}

	public void actualizar() {
		this.VINETA_TERMICA.actualizar();
		this.BARRA_VIDA.actualizar();
		this.BARRA_ESTAMINA.actualizar();
		this.BARRA_JEFE.actualizar();
		this.RELOJ_CICLO.actualizar();
		this.TERMOMETRO.actualizar();
		this.EFECTOS_ESTADO.actualizar();
		this.METABOLISMO.actualizar();
		if (this.esAmbienteCueva()) {
			this.BARRA_CUEVA.actualizar();
		}
	}

	public void pintar(final Graphics2D g) {
		// Supresión automática en cinemáticas, diálogos o si el HUD está desactivado
		if (!this.visible || ((Globales.GESTOR_EVENTOS != null) && Globales.GESTOR_EVENTOS.haySecuenciaEnCurso())) {
			return;
		}
		final boolean enCueva = this.esAmbienteCueva();
		// 1. Capa inferior del HUD: Viñeta atmosférica de pantalla completa
		this.VINETA_TERMICA.pintar(g);

		// 2. Capa intermedia: Widgets y Barras de estado
		this.BARRA_VIDA.pintar(g);
		this.BARRA_ESTAMINA.pintar(g);
		this.BARRA_JEFE.pintar(g);
		this.RELOJ_CICLO.pintar(g);
		this.TERMOMETRO.pintar(g);
		this.METABOLISMO.pintar(g);
		this.EFECTOS_ESTADO.pintar(g);
		if (enCueva) {
			this.BARRA_CUEVA.pintar(g);
		}

		// 3. Capa final superior: Tooltips flotantes
		this.EFECTOS_ESTADO.pintarTooltips(g);
		this.TERMOMETRO.pintarTooltips(g);
		this.METABOLISMO.pintarTooltips(g);
		if (enCueva) {
			this.BARRA_CUEVA.pintarTooltip(g);
		}
	}

	public void fijarJefe(final Criatura jefe) {
		this.BARRA_JEFE.asignarJefe(jefe);
	}

	public void desvincularJefe() {
		this.BARRA_JEFE.desvincularJefe();
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public void conmutarVisibilidad() {
		this.visible = !this.visible;
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

	private boolean esAmbienteCueva() {
		return (Globales.JUGADOR != null) && (Globales.JUGADOR.getMundo() != null)
				&& (Globales.JUGADOR.getMundo().getEscenario() != null)
				&& (Globales.JUGADOR.getMundo().getEscenario().getMetadatos() != null)
				&& Globales.JUGADOR.getMundo().getEscenario().getMetadatos().esCueva();
	}

	public VinetaTermicaIGU getVinetaTermica() {
		return this.VINETA_TERMICA;
	}

	public MetabolismoIGU getMetabolismo() {
		return this.METABOLISMO;
	}
}