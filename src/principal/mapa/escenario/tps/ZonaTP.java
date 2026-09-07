package principal.mapa.escenario.tps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.igu.textos.TipoTextoFlotante;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class ZonaTP extends Ente {

	private boolean eliminado;
	private final Rectangle AREA;
	private PuertaTP puertaTP;
	private CondicionTP condicion; // <-- Condición opcional

	private final GestorTiempo GT_COOLDOWN_TP = new GestorTiempo();
	private static final int COOLDOWN_TP_MS = 600;

	// Cooldown para no spamear el mensaje de rechazo 60 veces por segundo
	private final GestorTiempo GT_FEEDBACK = new GestorTiempo();
	private static final int COOLDOWN_FEEDBACK_MS = 1400;

	public ZonaTP(final Rectangle area, final PuertaTP puerta) {
		this(area, puerta, null);
	}

	public ZonaTP(final Rectangle area, final PuertaTP puerta, final CondicionTP condicion) {
		this.AREA = area;
		this.puertaTP = puerta;
		this.condicion = condicion;
	}

	@Override
	public void actualizar() {
		if (this.eliminado || (this.puertaTP == null) || (Globales.JUGADOR == null)) {
			return;
		}

		if (this.AREA.intersects(Globales.JUGADOR.getAreaInterseccionMovimiento())) {
			// 1. Validar si cumple la condición
			if ((this.condicion == null) || this.condicion.seCumple(Globales.JUGADOR)) {

				if (this.GT_COOLDOWN_TP.transcurrioMiliSegundos(COOLDOWN_TP_MS)) {
					this.GT_COOLDOWN_TP.establecerReferenciaTiempoActual();

					if (this.condicion != null) {
						this.condicion.alCruzar(Globales.JUGADOR);
					}

					this.teletransportar(Globales.JUGADOR);
				}

			} else // 2. Si no cumple, mostrar mensaje flotante y reproducir sonido de bloqueo
			if (this.GT_FEEDBACK.transcurrioMiliSegundos(COOLDOWN_FEEDBACK_MS)) {
				this.GT_FEEDBACK.establecerReferenciaTiempoActual();
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);

				final String msg = this.condicion.getMensajeRechazo();
				Globales.GESTOR_TEXTOS.agregarTexto(msg, this.getPosicionXInt() + (this.AREA.width / 2),
						this.getPosicionYInt() - 6, TipoTextoFlotante.ESTADO);
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.puertaTP instanceof PuertaArea) {
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, new Color(140, 134, 230, 110));
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, new Color(140, 134, 230, 220));
		} else if ((this.puertaTP instanceof PuertaMundo) || (this.puertaTP instanceof PuertaZona)) {
			// Si tiene candado/condición, la pintamos con borde dorado de alerta
			final Color cFondo = (this.condicion != null) ? new Color(255, 180, 40, 110) : new Color(245, 20, 243, 110);
			final Color cBorde = (this.condicion != null) ? new Color(255, 215, 50, 240) : new Color(245, 20, 243, 220);
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, cFondo);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, cBorde);
		} else if (this.puertaTP instanceof PuertaMapa) {
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, new Color(251, 20, 43, 110));
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, new Color(251, 20, 43, 220));
		}
	}

	public void setCondicion(final CondicionTP condicion) {
		this.condicion = condicion;
	}

	public CondicionTP getCondicion() {
		return this.condicion;
	}

	public PuertaTP getPuertaTP() {
		return this.puertaTP;
	}

	@Override
	public Rectangle getArea() {
		return this.AREA;
	}

	public void setPuertaTP(final PuertaTP puertaTP) {
		this.puertaTP = puertaTP;
	}

	public void teletransportar(final Criatura c) {
		if (this.puertaTP != null) {
			this.puertaTP.teletransportar(c);
		}
	}

	public int getCentroX(final Ente e) {
		return (this.AREA.x + (this.AREA.width / 2)) - (e.getArea().width / 2);
	}

	public int getCentroY(final Ente e) {
		return (this.AREA.y + (this.AREA.height / 2)) - (e.getArea().height / 2);
	}

	public boolean estaHabilitado() {
		return !this.eliminado;
	}

	@Override
	public void restaurar() {
		this.eliminado = false;
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public int getPosicionXInt() {
		return this.AREA.x;
	}

	@Override
	public int getPosicionYInt() {
		return this.AREA.y;
	}

	@Override
	public double getPosicionX() {
		return this.AREA.x;
	}

	@Override
	public double getPosicionY() {
		return this.AREA.y;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		this.AREA.x += (int) desplazamientoX;
		this.verificarZoneBox();
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.AREA.y += (int) desplazamientoY;
		this.verificarZoneBox();
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public int getAncho() {
		return this.AREA.width;
	}

	@Override
	public int getAlto() {
		return this.AREA.height;
	}

	@Override
	public void setPosicion(final double x, final double y) {
		this.AREA.x = (int) Math.round(x);
		this.AREA.y = (int) Math.round(y);
		this.verificarZoneBox();
	}
}