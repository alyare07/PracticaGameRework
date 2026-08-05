package principal.utilidades.funciones;

import java.awt.Point;

public class Funciones {
	public final Trayectorias GENERADOR_TRAYECTORIAS = new Trayectorias();
	public final MedidorStrings MEDIDOR_STRING = new MedidorStrings();
	public final GeneradorTooltip GENERADOR_TOOLTIP = new GeneradorTooltip();
	public final CargadorRecursos CARGADOR_RECURSOS = new CargadorRecursos();
	public final EncriptadorString ENCRIPTADOR_STRING = new EncriptadorString();
	public final GestorTipoEnCarga GESTOR_TIPOS_EN_CARGA = new GestorTipoEnCarga();
	public Funciones() {

	}

	public String pointToString(final Point p) {
		return p.x + "," + p.y;
	}

	public Point stringToPoint(final String s) {
		String[] valores = s.split(",");
		return new Point(Integer.parseInt(valores[0]), Integer.parseInt(valores[1]));
	}
	
	public double getDistanciaEntrePuntos(final Point p1, final Point p2) {

		return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
	}
}
