package principal.entes.objetos.fabricables;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.facciones.GestorFacciones;
import principal.entes.objetos.Objeto;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Clase base para estructuras y objetos construibles o desplegables por entidades.
 * Provee barra de durabilidad táctica, delegación dinámica de facciones y ciclo de destrucción unificado (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8 - Faction-Delegating Deployables)
 */
public abstract class ObjetoFabricable extends Objeto implements Serializable {

	private static final long serialVersionUID = 1L;

	protected double vida;
	protected double vidaMaxima;
	protected Ente propietario;

	protected static final int TIEMPO_MS_FLASH_DANIO = 65;
	protected final GestorTiempo GT_FLASH_DANIO;

	// Paleta cromática de durabilidad táctica (Gris Acero / Pizarra)
	private static final Color COLOR_FONDO_BARRA = new Color(15, 18, 24, 230);
	private static final Color COLOR_DURABILIDAD = new Color(175, 185, 200); // Acero claro
	private static final Color COLOR_DURABILIDAD_CRITICA = new Color(220, 75, 60); // Rojo anaranjado

	public ObjetoFabricable(final int x, final int y, final double vidaMaxima, final Ente propietario) {
		super(x, y);
		this.vidaMaxima = Math.max(1.0, vidaMaxima);
		this.vida = this.vidaMaxima;
		this.propietario = (propietario != null) ? propietario : Globales.JUGADOR;
		this.GT_FLASH_DANIO = new GestorTiempo();
	}

	public ObjetoFabricable(final int x, final int y, final double vidaMaxima) {
		this(x, y, vidaMaxima, Globales.JUGADOR);
	}

	/**
	 * Delega dinámicamente la facción a su propietario para que las IAs enemigas
	 * puedan identificarlo y atacarlo de forma orgánica (SSOT).
	 */
	public int getFaccionBit() {
		if (this.propietario instanceof Criatura) {
			return ((Criatura) this.propietario).getFaccionBit();
		}
		return GestorFacciones.FACCION_JUGADOR;
	}

	public Ente getPropietario() {
		return this.propietario;
	}

	public void setPropietario(final Ente propietario) {
		this.propietario = propietario;
	}

	// =========================================================================
	// SISTEMA DE DAÑO Y DESTRUCCIÓN TEMPLATE METHOD
	// =========================================================================

	public void recibirAtaque(final double damage, final Ente causante) {
		if (this.eliminado || (damage <= 0.0)) {
			return;
		}

		this.vida = Math.max(0.0, this.vida - damage);
		this.GT_FLASH_DANIO.establecerReferenciaTiempoActual();
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		if (this.mundo != null) {
			Globales.GESTOR_TEXTOS.agregarDanio((int) Math.ceil(damage), this.getPosicionX(), this.getPosicionY(), false);
		}

		this.alRecibirDanio(damage, causante);

		if (this.vida <= 0.0) {
			this.destruir(causante);
		}
	}

	protected void destruir(final Ente causante) {
		if (this.mundo != null) {
			Globales.GESTOR_PARTICULAS.emitirExplosion(this.getCentroX(), this.getCentroY(), 8);
			Globales.GESTOR_PARTICULAS.emitirPolvo(this.getCentroX(), this.getCentroY(), 10);

			if (Globales.GESTOR_DELTAS != null) {
				Globales.GESTOR_DELTAS.registrarDestruccion(this.mundo, this.getPosicionXInt(), this.getPosicionYInt());
			}

			this.alDestruir(causante);
			this.mundo.notificarModificacionEstructura();
		}
		this.eliminar();
	}

	/** Hook opcional ejecutado al recibir impacto sin morir. */
	protected void alRecibirDanio(final double damage, final Ente causante) {
	}

	/** Hook obligatorio para soltar recursos, expulsar ocupantes o emitir efectos propios. */
	protected abstract void alDestruir(final Ente causante);

	// =========================================================================
	// BARRA DE DURABILIDAD TÁCTICA (ZERO-GC)
	// =========================================================================

	public void pintarIndicadorVida(final Graphics2D g) {
		if (this.eliminado) {
			return;
		}

		// Se dibuja si está dañada o si el jugador pasa el cursor por encima
		final boolean mouseEncima = (Globales.RATON != null) && Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea());
		final boolean recientementeDanado = !this.GT_FLASH_DANIO.transcurrioMiliSegundos(3500) && (this.vida < this.vidaMaxima);

		if (!mouseEncima && !recientementeDanado) {
			return;
		}

		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt() - 5;
		final int anchoBarra = this.getAncho();

		// Fondo
		Render2D.dibujarRectanguloRellenoRefCamara(g, posX - 1, posY, anchoBarra + 2, 3, COLOR_FONDO_BARRA);

		// Progreso
		final double ratio = Math.max(0.0, Math.min(1.0, this.vida / this.vidaMaxima));
		final int anchoProgreso = (int) Math.round(ratio * anchoBarra);

		if (anchoProgreso > 0) {
			final Color colorBarra = (ratio <= 0.25) ? COLOR_DURABILIDAD_CRITICA : COLOR_DURABILIDAD;
			Render2D.dibujarRectanguloRellenoRefCamara(g, posX, posY, anchoProgreso, 2, colorBarra);
		}
	}

	public boolean estaEnFlashDanio() {
		return !this.GT_FLASH_DANIO.transcurrioMiliSegundos(TIEMPO_MS_FLASH_DANIO);
	}

	public double getVida() {
		return this.vida;
	}

	public double getVidaMaxima() {
		return this.vidaMaxima;
	}

	public void setVida(final double vida) {
		this.vida = Math.max(0.0, Math.min(this.vidaMaxima, vida));
	}
}