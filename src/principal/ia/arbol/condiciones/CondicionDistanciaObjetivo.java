package principal.ia.arbol.condiciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;

/**
 * Condición que evalúa si la distancia al objetivo activo (o a su última posición
 * memorizada) se encuentra dentro de un rango determinado.
 * 
 * @version 1.0 (Vanilla Java 8 - Fast Squared Distance Metric)
 */
public class CondicionDistanciaObjetivo implements NodoBT {

	public enum ModoComparacion {
		MENOR_O_IGUAL, MAYOR_O_IGUAL, ENTRE
	}

	private final ModoComparacion modo;
	private final double distMinimaSq;
	private final double distMaximaSq;

	public CondicionDistanciaObjetivo(final ModoComparacion modo, final double distMinima, final double distMaxima) {
		this.modo = modo;
		this.distMinimaSq = distMinima * distMinima;
		this.distMaximaSq = distMaxima * distMaxima;
	}

	public static CondicionDistanciaObjetivo menorQue(final double distancia) {
		return new CondicionDistanciaObjetivo(ModoComparacion.MENOR_O_IGUAL, 0.0, distancia);
	}

	public static CondicionDistanciaObjetivo mayorQue(final double distancia) {
		return new CondicionDistanciaObjetivo(ModoComparacion.MAYOR_O_IGUAL, distancia, Double.MAX_VALUE);
	}

	public static CondicionDistanciaObjetivo entre(final double minDist, final double maxDist) {
		return new CondicionDistanciaObjetivo(ModoComparacion.ENTRE, minDist, maxDist);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		double targetX;
		double targetY;

		final Ente objetivo = bb.getObjetivoActual();
		if ((objetivo != null) && !objetivo.estaEliminado()) {
			targetX = objetivo.getCentroX();
			targetY = objetivo.getCentroY();
		} else if (bb.tienePosicionObjetivoRecordada()) {
			targetX = bb.getUltimoTargetX();
			targetY = bb.getUltimoTargetY();
		} else {
			return EstadoBT.FRACASO;
		}

		final double dx = targetX - criatura.getCentroX();
		final double dy = targetY - criatura.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		switch (this.modo) {
		case MENOR_O_IGUAL:
			return (distSq <= this.distMaximaSq) ? EstadoBT.EXITO : EstadoBT.FRACASO;
		case MAYOR_O_IGUAL:
			return (distSq >= this.distMinimaSq) ? EstadoBT.EXITO : EstadoBT.FRACASO;
		case ENTRE:
			return ((distSq >= this.distMinimaSq) && (distSq <= this.distMaximaSq)) ? EstadoBT.EXITO : EstadoBT.FRACASO;
		default:
			return EstadoBT.FRACASO;
		}
	}
}