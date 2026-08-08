package principal.utilidades.funciones;

import java.awt.Point;
import java.util.EnumSet;

import principal.entes.criaturas.Criatura.Direccion;

public class Funciones {
	public final Trayectorias GENERADOR_TRAYECTORIAS = new Trayectorias();
	public final MedidorStrings MEDIDOR_STRING = new MedidorStrings();
	public final GeneradorTooltip GENERADOR_TOOLTIP = new GeneradorTooltip();
	public final CargadorRecursos CARGADOR_RECURSOS = new CargadorRecursos();
	public final EncriptadorString ENCRIPTADOR_STRING = new EncriptadorString();
	public final GestorTipoEnCarga GESTOR_TIPOS_EN_CARGA = new GestorTipoEnCarga();
	public final TempManager TEMP_MANAGER = new TempManager();
	public final TexturaTools TEXTURAS_TOOLS = new TexturaTools();

	public Funciones() {

	}

	public String pointToString(final Point p) {
		return p.x + "," + p.y;
	}

	public Point stringToPoint(final String s) {
		final int posComa = s.indexOf(',');
		if (posComa == -1) {
			return new Point(0, 0); // O manejo de error/excepción
		}

		final int x = Integer.parseInt(s.substring(0, posComa));
		final int y = Integer.parseInt(s.substring(posComa + 1));
		return new Point(x, y);
	}

	// ANTES: Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() -
	// p1.getY(), 2));
	// DESPUÉS (Mucho más rápido):
	public double getDistanciaEntrePuntos(final Point p1, final Point p2) {
		final int dx = p2.x - p1.x;
		final int dy = p2.y - p1.y;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	public Direccion getDireccionMirando(final int xI, final int yI, final int xF, final int yF) {
		final int dx = xF - xI;
		final int dy = yF - yI;

		// Si el movimiento horizontal es mayor o igual que el vertical
		if (Math.abs(dx) >= Math.abs(dy)) {
			return (dx >= 0) ? Direccion.ESTE : Direccion.OESTE;
		}
		return (dy >= 0) ? Direccion.SUR : Direccion.NORTE;
	}

	public Direccion getDireccionMirando(final int xI, final int yI, final int xF, final int yF,
			final boolean prioridadHorizontal) {
		final int dx = xF - xI;
		final int dy = yF - yI;

		if (prioridadHorizontal) {
			if (dx != 0) {
				return (dx > 0) ? Direccion.ESTE : Direccion.OESTE;
			}
			return (dy >= 0) ? Direccion.SUR : Direccion.NORTE;
		}
		if (dy != 0) {
			return (dy > 0) ? Direccion.SUR : Direccion.NORTE;
		}
		return (dx >= 0) ? Direccion.ESTE : Direccion.OESTE;
	}

	public EnumSet<Direccion> getDireccionesMirando(final int xI, final int yI, final int xF, final int yF) {
		final Direccion vertical = (yI <= yF) ? Direccion.SUR : Direccion.NORTE;
		final Direccion horizontal = (xI <= xF) ? Direccion.ESTE : Direccion.OESTE;

		return EnumSet.of(vertical, horizontal);
	}

}
