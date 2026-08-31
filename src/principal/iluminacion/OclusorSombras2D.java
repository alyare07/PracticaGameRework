package principal.iluminacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Motor de oclusión de sombras 2D activo exclusivamente en interiores,
 * mazmorras y cuevas (Zero-GC / O(1)).
 * 
 * @version 6.0
 */
public class OclusorSombras2D {

	// =========================================================================
	// === 1. BUFFERS GEOMÉTRICOS PRE-ASIGNADOS (ZERO-GC)
	// =========================================================================

	private static final int MAX_VERTICES = 8;
	private final int[] xBuffer = new int[MAX_VERTICES];
	private final int[] yBuffer = new int[MAX_VERTICES];

	private static final AlphaComposite COMPOSITE_NORMAL = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

	// =========================================================================
	// === 2. PROYECCIÓN DE SOMBRAS (PASE A Y B)
	// =========================================================================

	public void proyectarSombrasPaseA(final Graphics2D gLight, final FuenteLuz luz, final double centroMundoCamX,
			final double centroMundoCamY, final double zoom, final double shakeX, final double shakeY,
			final Color colorAmbiente) {
		// Descarte temprano estricto: 0% procesamiento si estamos en el exterior
		if (!this.estaEnInterior() || (luz == null) || (colorAmbiente == null)) {
			return;
		}

		gLight.setComposite(COMPOSITE_NORMAL);
		gLight.setColor(colorAmbiente);

		this.procesarSombrasMuros(gLight, luz, centroMundoCamX, centroMundoCamY, zoom, shakeX, shakeY);
	}

	public void proyectarSombrasPaseB(final Graphics2D gLight, final FuenteLuz luz, final double centroMundoCamX,
			final double centroMundoCamY, final double zoom, final double shakeX, final double shakeY,
			final Color colorAmbiente) {
		if (!this.estaEnInterior() || (luz == null) || (colorAmbiente == null)) {
			return;
		}

		gLight.setComposite(COMPOSITE_NORMAL);
		gLight.setColor(colorAmbiente);

		this.procesarSombrasMuros(gLight, luz, centroMundoCamX, centroMundoCamY, zoom, shakeX, shakeY);
	}

	/**
	 * Evalúa si el jugador se encuentra actualmente dentro de una cueva, mazmorra o
	 * interior.
	 */
	private boolean estaEnInterior() {
		final boolean porZona = (Globales.GESTOR_ZONAS_AMBIENTE != null)
				&& Globales.GESTOR_ZONAS_AMBIENTE.isEnZonaInterior();
		final boolean porLuz = (Globales.GESTOR_LUZ != null) && Globales.GESTOR_LUZ.isModoAmbienteFijo();
		return porZona || porLuz;
	}

	// =========================================================================
	// === 3. ESCANEO EXCLUSIVO DE MUROS SÓLIDOS (TILES 16x16)
	// =========================================================================

	private void procesarSombrasMuros(final Graphics2D gLight, final FuenteLuz luz, final double centroCamX,
			final double centroCamY, final double z, final double shakeX, final double shakeY) {

		if ((Globales.CAMARA == null) || (Globales.CAMARA.getEntidadEnfocada() == null)
				|| (Globales.CAMARA.getEntidadEnfocada().getMundo() == null)) {
			return;
		}

		final Mundo mundo = Globales.CAMARA.getEntidadEnfocada().getMundo();
		final Terreno terreno = mundo.getTerreno();
		if (terreno == null) {
			return;
		}

		final double lx = luz.getPosX();
		final double ly = luz.getPosY();
		final double radio = luz.getRadioActual();

		final int diametro = (int) Math.round(radio * 2.0);
		final int minX = (int) Math.round(lx - radio);
		final int minY = (int) Math.round(ly - radio);

		final int ladoTile = Constantes.LADO_TILE;
		final int tileMinX = Math.max(0, Math.floorDiv(minX, ladoTile));
		final int tileMaxX = Math.min((terreno.getAncho() / ladoTile) - 1, Math.floorDiv(minX + diametro, ladoTile));
		final int tileMinY = Math.max(0, Math.floorDiv(minY, ladoTile));
		final int tileMaxY = Math.min((terreno.getAlto() / ladoTile) - 1, Math.floorDiv(minY + diametro, ladoTile));

		for (int ty = tileMinY; ty <= tileMaxY; ty++) {
			for (int tx = tileMinX; tx <= tileMaxX; tx++) {
				final Tile tile = terreno.getTileGrid(tx, ty);
				if ((tile != null) && tile.esSolido()) {
					final int px = tx * ladoTile;
					final int py = ty * ladoTile;
					this.extruirSombraMuro(gLight, lx, ly, px, py, ladoTile, ladoTile, radio, centroCamX, centroCamY, z,
							shakeX, shakeY);
				}
			}
		}
	}

	// =========================================================================
	// === 4. EXTRUSIÓN VECTORIAL DE MUROS DE CUEVA (ZERO-GC)
	// =========================================================================

	private void extruirSombraMuro(final Graphics2D gLight, final double lx, final double ly, final int rx,
			final int ry, final int rw, final int rh, final double radioLuz, final double centroCamX,
			final double centroCamY, final double z, final double shakeX, final double shakeY) {

		final double tcx = rx + (rw / 2.0);
		final double tcy = ry + (rh / 2.0);

		final double distCentro = Math.hypot(tcx - lx, tcy - ly);
		if ((distCentro < 4.0) || (distCentro > radioLuz)) {
			return;
		}

		final double factorExtrusion = Math.max(8.0, (radioLuz - distCentro) + 10.0);

		final double x0 = rx;
		final double y0 = ry;
		final double x1 = rx + rw;
		final double y1 = ry;
		final double x2 = rx + rw;
		final double y2 = ry + rh;
		final double x3 = rx;
		final double y3 = ry + rh;

		double ax, ay, bx, by;

		if (lx < x0) {
			if (ly < y0) {
				ax = x1;
				ay = y1;
				bx = x3;
				by = y3;
			} else if (ly > y2) {
				ax = x0;
				ay = y0;
				bx = x2;
				by = y2;
			} else {
				ax = x0;
				ay = y0;
				bx = x3;
				by = y3;
			}
		} else if (lx > x1) {
			if (ly < y0) {
				ax = x0;
				ay = y0;
				bx = x2;
				by = y2;
			} else if (ly > y2) {
				ax = x1;
				ay = y1;
				bx = x3;
				by = y3;
			} else {
				ax = x1;
				ay = y1;
				bx = x2;
				by = y2;
			}
		} else if (ly < y0) {
			ax = x0;
			ay = y0;
			bx = x1;
			by = y1;
		} else {
			ax = x3;
			ay = y3;
			bx = x2;
			by = y2;
		}

		final double dAx = ax - lx;
		final double dAy = ay - ly;
		final double lenA = Math.hypot(dAx, dAy);
		if (lenA < 0.001) {
			return;
		}

		final double extAx = ax + ((dAx / lenA) * factorExtrusion);
		final double extAy = ay + ((dAy / lenA) * factorExtrusion);

		final double dBx = bx - lx;
		final double dBy = by - ly;
		final double lenB = Math.hypot(dBx, dBy);
		if (lenB < 0.001) {
			return;
		}

		final double extBx = bx + ((dBx / lenB) * factorExtrusion);
		final double extBy = by + ((dBy / lenB) * factorExtrusion);

		this.xBuffer[0] = (int) Math.round(Constantes.CENTROX + shakeX + ((ax - centroCamX) * z));
		this.yBuffer[0] = (int) Math.round(Constantes.CENTROY + shakeY + ((ay - centroCamY) * z));

		this.xBuffer[1] = (int) Math.round(Constantes.CENTROX + shakeX + ((extAx - centroCamX) * z));
		this.yBuffer[1] = (int) Math.round(Constantes.CENTROY + shakeY + ((extAy - centroCamY) * z));

		this.xBuffer[2] = (int) Math.round(Constantes.CENTROX + shakeX + ((extBx - centroCamX) * z));
		this.yBuffer[2] = (int) Math.round(Constantes.CENTROY + shakeY + ((extBy - centroCamY) * z));

		this.xBuffer[3] = (int) Math.round(Constantes.CENTROX + shakeX + ((bx - centroCamX) * z));
		this.yBuffer[3] = (int) Math.round(Constantes.CENTROY + shakeY + ((by - centroCamY) * z));

		gLight.fillPolygon(this.xBuffer, this.yBuffer, 4);
	}
}