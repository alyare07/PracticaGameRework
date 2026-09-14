package principal.ia.arbol.condiciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Globales;

/**
 * Condición sensorial de amenaza que programa el tiempo de espera en el reloj
 * solar.
 * 
 * @version 2.0 (Vanilla Java 8 - In-Game Time Postponement)
 */
public class CondicionAmenazaCercana implements NodoBT {

	private final double rangoDeteccion;
	private final double rangoDeteccionSq;
	private final double horasEsperaInGame;

	public CondicionAmenazaCercana(final double rangoDeteccion, final double horasEsperaInGame) {
		this.rangoDeteccion = Math.max(32.0, rangoDeteccion);
		this.rangoDeteccionSq = this.rangoDeteccion * this.rangoDeteccion;
		this.horasEsperaInGame = Math.max(0.25, horasEsperaInGame);
	}

	public CondicionAmenazaCercana() {
		this(140.0, 1.5); // 1 hora y media de juego por defecto
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Mundo mundo = criatura.getMundo();
		if (mundo == null) {
			return EstadoBT.FRACASO;
		}

		final double miX = criatura.getCentroX();
		final double miY = criatura.getCentroY();

		// 1. Amenaza ya fijada
		final Ente amenazaActual = bb.getObjetivoActual();
		if ((amenazaActual != null) && !amenazaActual.estaEliminado()) {
			final double dx = amenazaActual.getCentroX() - miX;
			final double dy = amenazaActual.getCentroY() - miY;
			final double distSq = (dx * dx) + (dy * dy);

			if (distSq <= (200.0 * 200.0)) {
				bb.setEnPanico(true);
				this.posponerRetorno(bb);
				return EstadoBT.EXITO;
			}
		}

		// 2. Escaneo espacial de agresores
		final int ladoZB = mundo.getLadoZoneBox();
		final int minGX = Math.max(0, Math.floorDiv((int) (miX - this.rangoDeteccion), ladoZB));
		final int maxGX = Math.min(mundo.getCantZonasX() - 1, Math.floorDiv((int) (miX + this.rangoDeteccion), ladoZB));
		final int minGY = Math.max(0, Math.floorDiv((int) (miY - this.rangoDeteccion), ladoZB));
		final int maxGY = Math.min(mundo.getCantZonasY() - 1, Math.floorDiv((int) (miY + this.rangoDeteccion), ladoZB));

		Criatura amenazaCercana = null;
		double menorDistSq = this.rangoDeteccionSq;

		for (int gy = minGY; gy <= maxGY; gy++) {
			for (int gx = minGX; gx <= maxGX; gx++) {
				final ZoneBox zb = mundo.getZonaGrid(gx, gy);
				if (zb == null) {
					continue;
				}

				final int total = zb.getCriaturas().size();
				for (int i = 0; i < total; i++) {
					final Criatura candidata = zb.getCriaturas().get(i);

					if ((candidata == criatura) || candidata.estaEliminado() || !candidata.esHostilHacia(criatura)) {
						continue;
					}

					final double dx = candidata.getCentroX() - miX;
					final double dy = candidata.getCentroY() - miY;
					final double distSq = (dx * dx) + (dy * dy);

					if (distSq < menorDistSq) {
						menorDistSq = distSq;
						amenazaCercana = candidata;
					}
				}
			}
		}

		if (amenazaCercana != null) {
			bb.setObjetivoActual(amenazaCercana);
			bb.setEnPanico(true);
			this.posponerRetorno(bb);
			return EstadoBT.EXITO;
		}

		bb.setEnPanico(false);
		bb.setObjetivoActual(null);
		return EstadoBT.FRACASO;
	}

	private void posponerRetorno(final BlackboardIA bb) {
		if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
			final double ahoraHoras = Globales.GESTOR_LUZ.getCiclo().getHorasTotalesJuego();
			bb.setTimestampRetornoJuegoHoras(ahoraHoras + this.horasEsperaInGame);
		}
	}
}