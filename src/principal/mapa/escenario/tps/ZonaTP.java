package principal.mapa.escenario.tps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.utilidades.Render2D;

public class ZonaTP extends Ente {
	private boolean eliminado;
	private final Rectangle AREA;
	private PuertaTP puertaTP;
//	private final HashMap<Criatura, Criatura> ENTES_TELETRANSPORTADOS_HACIA_ACA = new HashMap<Criatura, Criatura>();

	public ZonaTP(final Rectangle area, final PuertaTP puerta) {
		this.AREA = area;
		this.puertaTP = puerta;
	}

	@Override
	public void actualizar() {
//		if (!this.ENTES_TELETRANSPORTADOS_HACIA_ACA.isEmpty()) {
//			final Iterator<Map.Entry<Criatura, Criatura>> it = this.ENTES_TELETRANSPORTADOS_HACIA_ACA.entrySet()
//					.iterator();
//			while (it.hasNext()) {
//				final Criatura c = it.next().getKey();
//				if (c instanceof Jugador) {
//					if (!((Jugador) c).getAreaInterseccionMovimiento().intersects(this.AREA)) {
//						it.remove();
//					}
//				}
//			}
//		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.puertaTP instanceof PuertaArea) {
			// Azul / Violeta para TP Local
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, new Color(140, 134, 230, 110));
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, new Color(140, 134, 230, 220));
		} else if ((this.puertaTP instanceof PuertaMundo) || (this.puertaTP instanceof PuertaZona)) {
			// Magenta / Rosa para TP Entre Mundos
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, new Color(245, 20, 243, 110));
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, new Color(245, 20, 243, 220));
		} else if (this.puertaTP instanceof PuertaMapa) {
			// Rojo para TP A Otro Mapa
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, new Color(251, 20, 43, 110));
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, new Color(251, 20, 43, 220));
		} else {
			// Fallback (amarillo) por si la puerta es null o de otro tipo
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.AREA, new Color(255, 220, 0, 110));
			Render2D.dibujarRectanguloContornoRefCamara(g, this.AREA, new Color(255, 220, 0, 220));
		}
	}

	public PuertaTP getPuertaTP() {
		return this.puertaTP;
	}

	@Override
	public Rectangle getArea() {
		return this.AREA;
	}

//	public void meterCriaturaTeletransportadoParaAca(final Criatura c) {
//		if (!this.ENTES_TELETRANSPORTADOS_HACIA_ACA.containsKey(c)) {
//			this.ENTES_TELETRANSPORTADOS_HACIA_ACA.put(c, c);
//		}
//	}

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

//	public boolean disponibleParaTP(final Criatura c) {
//		if (this.estaHabilitado()) {
//			return !this.ENTES_TELETRANSPORTADOS_HACIA_ACA.containsKey(c);
//		}
//		return false;
//	}

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