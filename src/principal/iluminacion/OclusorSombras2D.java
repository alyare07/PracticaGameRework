package principal.iluminacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Motor de oclusión de sombras dinámicas 2D mediante extrusión de siluetas de
 * tiles sólidos sobre el Lightmap (Zero-GC / O(1)).
 * 
 * @author Copiloto Técnico
 * @version 1.0
 */
public class OclusorSombras2D {

	// =========================================================================
	// === 1. BUFFERS GEOMÉTRICOS PRE-ASIGNADOS (ZERO-GC)
	// =========================================================================

	private static final int MAX_VERTICES = 8;
	private final int[] xBuffer = new int[MAX_VERTICES];
	private final int[] yBuffer = new int[MAX_VERTICES];

	private static final AlphaComposite COMPOSITE_NORMAL = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	private static final AlphaComposite COMPOSITE_LIMPIEZA = AlphaComposite.getInstance(AlphaComposite.CLEAR);

	// =========================================================================
	// === 2. EXTRUSIÓN Y PROYECCIÓN DE SOMBRAS (PASE A Y B)
	// =========================================================================

	/**
	 * Proyecta polígonos de sombra sobre el Lightmap para bloquear la perforación
	 * de penumbra (Pase A).
	 */
	public void proyectarSombrasPaseA(final Graphics2D gLight, final FuenteLuz luz, final double centroMundoCamX,
			final double centroMundoCamY, final double zoom, final double shakeX, final double shakeY,
			final Color colorAmbiente) {
		if ((luz == null) || (colorAmbiente == null)) {
			return;
		}

		gLight.setComposite(COMPOSITE_NORMAL);
		gLight.setColor(colorAmbiente);

		this.procesarSombrasLuz(gLight, luz, centroMundoCamX, centroMundoCamY, zoom, shakeX, shakeY);
	}

	/**
	 * Limpia el tinte de color de las linternas detrás de los muros opacos (Pase
	 * B).
	 */
	public void proyectarSombrasPaseB(final Graphics2D gLight, final FuenteLuz luz, final double centroMundoCamX,
			final double centroMundoCamY, final double zoom, final double shakeX, final double shakeY) {
		if (luz == null) {
			return;
		}

		gLight.setComposite(COMPOSITE_LIMPIEZA);

		this.procesarSombrasLuz(gLight, luz, centroMundoCamX, centroMundoCamY, zoom, shakeX, shakeY);
	}

	// =========================================================================
	// === 3. ESCANEO LOCAL DE TILES Y CÁLCULO POLIGONAL
	// =========================================================================

	private void procesarSombrasLuz(final Graphics2D gLight, final FuenteLuz luz, final double centroCamX,
			final double centroCamY, final double z, final double shakeX, final double shakeY) {

		if ((Globales.CAMARA == null) || (Globales.CAMARA.getEntidadEnfocada() == null)
				|| (Globales.CAMARA.getEntidadEnfocada().getMundo() == null)) {
			return;
		}

		final Terreno terreno = Globales.CAMARA.getEntidadEnfocada().getMundo().getTerreno();
		if (terreno == null) {
			return;
		}

		final double lx = luz.getPosX();
		final double ly = luz.getPosY();
		final double radio = luz.getRadioActual();

		// Rango discreto de tiles a escanear alrededor de la luz (O(Radio))
		final int tileMinX = (int) Math.floor((lx - radio) / Constantes.LADO_TILE);
		final int tileMaxX = (int) Math.floor((lx + radio) / Constantes.LADO_TILE);
		final int tileMinY = (int) Math.floor((ly - radio) / Constantes.LADO_TILE);
		final int tileMaxY = (int) Math.floor((ly + radio) / Constantes.LADO_TILE);

		final double factorExtrusion = radio * 1.8;

		for (int ty = tileMinY; ty <= tileMaxY; ty++) {
			for (int tx = tileMinX; tx <= tileMaxX; tx++) {
				final int pixelX = tx * Constantes.LADO_TILE;
				final int pixelY = ty * Constantes.LADO_TILE;

				final Tile tile = terreno.getTileReferenciado(pixelX, pixelY);
				if ((tile != null) && tile.esSolido()) {
					this.extruirSombraTile(gLight, lx, ly, pixelX, pixelY, Constantes.LADO_TILE, factorExtrusion,
							centroCamX, centroCamY, z, shakeX, shakeY);
				}
			}
		}
	}

	/**
	 * Calcula los 6 vértices de la silueta extruida de un tile sólido y dibuja el
	 * polígono de sombra en espacio de pantalla.
	 */
	private void extruirSombraTile(final Graphics2D gLight, final double lx, final double ly, final int tx,
			final int ty, final int lado, final double factorExtrusion, final double centroCamX,
			final double centroCamY, final double z, final double shakeX, final double shakeY) {

		// Centro del tile
		final double tcx = tx + (lado / 2.0);
		final double tcy = ty + (lado / 2.0);

		// Vector director de la luz al centro del tile
		final double dirX = tcx - lx;
		final double dirY = tcy - ly;
		final double distSq = (dirX * dirX) + (dirY * dirY);

		if (distSq < 1.0) {
			return; // La luz está adentro del muro
		}

		// 4 esquinas del tile en coordenadas de mundo
		final double x0 = tx;
		final double y0 = ty;
		final double x1 = tx + lado;
		final double y1 = ty;
		final double x2 = tx + lado;
		final double y2 = ty + lado;
		final double x3 = tx;
		final double y3 = ty + lado;

		// Determinación de aristas frontales mediante producto cruz / normales
		// Vértices frontales A y B
		double ax, ay, bx, by;

		if (Math.abs(dirX) > Math.abs(dirY)) {
			if (dirX > 0) {
				// Luz a la izquierda: arista frontal es borde izquierdo (x0,y0 a x3,y3)
				ax = x0;
				ay = y0;
				bx = x3;
				by = y3;
			} else {
				// Luz a la derecha: arista frontal es borde derecho (x1,y1 a x2,y2)
				ax = x1;
				ay = y1;
				bx = x2;
				by = y2;
			}
		} else if (dirY > 0) {
			// Luz arriba: arista frontal es borde superior (x0,y0 a x1,y1)
			ax = x0;
			ay = y0;
			bx = x1;
			by = y1;
		} else {
			// Luz abajo: arista frontal es borde inferior (x3,y3 a x2,y2)
			ax = x3;
			ay = y3;
			bx = x2;
			by = y2;
		}

		// Extrusión de los vértices hacia el infinito
		final double dAx = ax - lx;
		final double dAy = ay - ly;
		final double lenA = Math.hypot(dAx, dAy);
		final double extAx = ax + ((dAx / lenA) * factorExtrusion);
		final double extAy = ay + ((dAy / lenA) * factorExtrusion);

		final double dBx = bx - lx;
		final double dBy = by - ly;
		final double lenB = Math.hypot(dBx, dBy);
		final double extBx = bx + ((dBx / lenB) * factorExtrusion);
		final double extBy = by + ((dBy / lenB) * factorExtrusion);

		// Transformación a espacio de pantalla 1:1
		this.xBuffer[0] = (int) Math.round(Constantes.CENTROX + shakeX + ((ax - centroCamX) * z));
		this.yBuffer[0] = (int) Math.round(Constantes.CENTROY + shakeY + ((ay - centroCamY) * z));

		this.xBuffer[1] = (int) Math.round(Constantes.CENTROX + shakeX + ((extAx - centroCamX) * z));
		this.yBuffer[1] = (int) Math.round(Constantes.CENTROY + shakeY + ((extAy - centroCamY) * z));

		this.xBuffer[2] = (int) Math.round(Constantes.CENTROX + shakeX + ((extBx - centroCamX) * z));
		this.yBuffer[2] = (int) Math.round(Constantes.CENTROY + shakeY + ((extBy - centroCamY) * z));

		this.xBuffer[3] = (int) Math.round(Constantes.CENTROX + shakeX + ((bx - centroCamX) * z));
		this.yBuffer[3] = (int) Math.round(Constantes.CENTROY + shakeY + ((by - centroCamY) * z));

		// Dibujar polígono trapezoidal de sombra
		gLight.fillPolygon(this.xBuffer, this.yBuffer, 4);
	}
}