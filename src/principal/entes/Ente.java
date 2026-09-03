package principal.entes;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.iluminacion.FuenteLuz;
import principal.mapa.Mundo;
import principal.mapa.renderEntidades.ZoneBox;

/**
 * Clase base abstracta para todas las entidades del mundo. Gestiona
 * identificadores de frame, sincronización de celdas espaciales ZoneBox y
 * anclaje de fuentes de luz (Zero-GC).
 * 
 * @version 2.2 (Vanilla Java 8)
 */
public abstract class Ente {

	protected boolean eliminado;
	protected Mundo mundo;
	protected FuenteLuz luzAsignada;

	protected int codUltimaActualizacion = Integer.MIN_VALUE;
	protected int codUltimoPintado = Integer.MIN_VALUE;
	protected boolean posicionModificada = true;

	protected final ArrayList<ZoneBox> zonasOcupadas = new ArrayList<>(4);
	protected final Rectangle AREA_ENTE_RETORNO = new Rectangle();

	public abstract void actualizar();

	public abstract void pintar(final Graphics2D g);

	public void eliminar() {
		if (this.eliminado) {
			return;
		}
		this.eliminado = true;
		this.desvincularLuz();
		this.desvincularDeZonas();
	}

	public abstract int getPosicionXInt();

	public abstract int getPosicionYInt();

	public abstract double getPosicionX();

	public abstract double getPosicionY();

	public abstract void modificarPosicionX(final double desplazamientoX);

	public abstract void modificarPosicionY(final double desplazamientoY);

	public abstract void setPosicion(final double x, final double y);

	public boolean estaEliminado() {
		return this.eliminado;
	}

	public abstract int getAncho();

	public abstract int getAlto();

	public boolean haCambiadoPosicion() {
		return this.posicionModificada;
	}

	public void limpiarFlagMovimiento() {
		this.posicionModificada = false;
	}

	public void marcarPosicionModificada() {
		this.posicionModificada = true;
	}

	public ArrayList<ZoneBox> getZonasOcupadas() {
		return this.zonasOcupadas;
	}

	public void desvincularDeZonas() {
		for (int i = 0; i < this.zonasOcupadas.size(); i++) {
			this.zonasOcupadas.get(i).eliminarEntidad(this);
		}
		this.zonasOcupadas.clear();
	}

	public void verificarZoneBox() {
		if ((this.mundo == null) || !this.posicionModificada) {
			return;
		}
		this.posicionModificada = false;

		final int ladoZB = this.mundo.getLadoZoneBox();
		final int cantZX = this.mundo.getCantZonasX();
		final int cantZY = this.mundo.getCantZonasY();

		if ((cantZX <= 0) || (cantZY <= 0)) {
			return;
		}

		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();
		final int ancho = this.getAncho();
		final int alto = this.getAlto();

		final int minGX = Math.max(0, Math.floorDiv(posX, ladoZB));
		final int maxGX = Math.min(cantZX - 1, Math.floorDiv((posX + Math.max(1, ancho)) - 1, ladoZB));
		final int minGY = Math.max(0, Math.floorDiv(posY, ladoZB));
		final int maxGY = Math.min(cantZY - 1, Math.floorDiv((posY + Math.max(1, alto)) - 1, ladoZB));

		// 1. Desvincular de celdas espaciales que ya no solapa (en reversa)
		for (int i = this.zonasOcupadas.size() - 1; i >= 0; i--) {
			final ZoneBox zb = this.zonasOcupadas.get(i);
			final int zgx = Math.floorDiv(zb.getPosicionXInt(), ladoZB);
			final int zgy = Math.floorDiv(zb.getPosicionYInt(), ladoZB);

			if ((zgx < minGX) || (zgx > maxGX) || (zgy < minGY) || (zgy > maxGY)) {
				zb.eliminarEntidad(this);
				this.zonasOcupadas.remove(i);
			}
		}

		// 2. Vincular a las nuevas celdas sin allocaciones
		for (int gy = minGY; gy <= maxGY; gy++) {
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = this.mundo.getZonaGrid(gx, gy);
				if ((zb != null) && !this.zonasOcupadas.contains(zb)) {
					this.zonasOcupadas.add(zb);
					zb.addEntidad(this);
				}
			}
		}

		// 3. Auto-eliminación segura si sale por completo del mundo
		if (this.zonasOcupadas.isEmpty()) {
			this.eliminar();
		}
	}

	public boolean estaActualizado(final int codFrameAct) {
		return this.codUltimaActualizacion == codFrameAct;
	}

	public void marcarActualizado(final int codFrameAct) {
		this.codUltimaActualizacion = codFrameAct;
	}

	public boolean estaPintado(final int codFramePaint) {
		return this.codUltimoPintado == codFramePaint;
	}

	public void marcarPintado(final int codFramePaint) {
		this.codUltimoPintado = codFramePaint;
	}

	public void asignarLuz(final FuenteLuz luz) {
		if (this.luzAsignada == luz) {
			return;
		}

		final FuenteLuz luzPrevia = this.luzAsignada;
		this.luzAsignada = luz;

		if (luzPrevia != null) {
			luzPrevia.apagar();
		}
	}

	public void desvincularLuz() {
		if (this.luzAsignada != null) {
			final FuenteLuz luzPrevia = this.luzAsignada;
			this.luzAsignada = null;
			luzPrevia.apagar();
		}
	}

	public FuenteLuz getLuzAsignada() {
		return this.luzAsignada;
	}

	public boolean tieneLuzAsignada() {
		return (this.luzAsignada != null) && this.luzAsignada.isActiva();
	}

	public int getPosicionYBase() {
		return this.getPosicionYInt() + this.getAlto();
	}

	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

	public int getCentroX() {
		return this.getPosicionXInt() + (this.getAncho() / 2);
	}

	public int getCentroY() {
		return this.getPosicionYInt() + (this.getAlto() / 2);
	}

	public void setMundo(final Mundo mundo) {
		this.mundo = mundo;
	}

	public Mundo getMundo() {
		return this.mundo;
	}
}