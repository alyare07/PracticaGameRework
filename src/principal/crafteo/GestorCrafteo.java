package principal.crafteo;

import java.awt.Rectangle;
import java.util.EnumSet;

import principal.entes.criaturas.Jugador;
import principal.entes.objetos.Objeto;
import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.inventario.Inventario;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Gestor maestro del sistema de fabricación y detección de estaciones cercanas.
 * Opera con cero asignaciones en memoria en cada frame (Zero-GC).
 * 
 * @version 2.1 (Vanilla Java 8 - Zero-GC)
 */
public class GestorCrafteo implements AccionEntidad<Objeto> {

	private final EnumSet<EstacionCrafteo> estacionesDisponibles = EnumSet.of(EstacionCrafteo.MANUAL);
	private static final double RANGO_DETECCION_ESTACION = 48.0;
	private final Rectangle areaDeteccionEstaciones = new Rectangle();

	public GestorCrafteo() {
	}

	public void actualizar(final Mundo mundo) {
		this.estacionesDisponibles.clear();
		this.estacionesDisponibles.add(EstacionCrafteo.MANUAL);

		if ((mundo == null) || (Globales.JUGADOR == null)) {
			return;
		}

		final Jugador j = Globales.JUGADOR;
		final int centroX = j.getCentroX();
		final int centroY = j.getCentroY();
		final int radio = (int) RANGO_DETECCION_ESTACION;

		this.areaDeteccionEstaciones.setBounds(centroX - radio, centroY - radio, radio * 2, radio * 2);

		// 1. Escaneo espacial de objetos físicos con visitor Zero-GC
		mundo.paraCadaObjetoEn(this.areaDeteccionEstaciones, this);

		// 2. Detección de proximidad a fuentes de calor reales (Fogatas)
		if (Globales.GESTOR_LUZ != null) {
			this.detectarFogatasPorLuz(centroX, centroY);
		}
	}

	@Override
	public void ejecutar(final Objeto objeto) {
		if ((objeto instanceof EstacionInteractiva) && !objeto.estaEliminado()) {
			final EstacionInteractiva estacion = (EstacionInteractiva) objeto;
			this.estacionesDisponibles.add(estacion.getTipoEstacion());
		}
	}

	/**
	 * Verifica si hay una fuente de luz activa de tipo FOGATA dentro del rango
	 * físico de calor.
	 */
	private void detectarFogatasPorLuz(final int centroX, final int centroY) {
		if (this.estacionesDisponibles.contains(EstacionCrafteo.FOGATA)) {
			return;
		}

		final double rangoSq = RANGO_DETECCION_ESTACION * RANGO_DETECCION_ESTACION;
		final int totalLuces = Globales.GESTOR_LUZ.getCantidadActivas();

		// Búsqueda directa O(N) sobre luces activas
		for (int i = 0; i < totalLuces; i++) {
			final FuenteLuz luz = Globales.GESTOR_LUZ.getLuzPorIndice(i);
			if ((luz != null) && luz.isActiva() && (luz.getTipo() == TipoLuz.FOGATA)) {
				final double dx = centroX - luz.getPosX();
				final double dy = centroY - luz.getPosY();
				if (((dx * dx) + (dy * dy)) <= rangoSq) {
					this.estacionesDisponibles.add(EstacionCrafteo.FOGATA);
					break;
				}
			}
		}
	}

	public boolean fabricar(final RecetaCrafteo receta) {
		if (receta == null) {
			return false;
		}

		if (!this.estacionesDisponibles.contains(receta.getEstacionRequerida())) {
			return false;
		}

		final Inventario inventario = Globales.GESTOR_INVENTARIO.getInventarioJugador();
		if (inventario == null) {
			return false;
		}

		final boolean exito = receta.craftear(inventario);

		if (exito) {
			if ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null)) {
				GestorSonido.reproducirEnPosicion(IDSonido.GOLPE_1, Globales.JUGADOR.getCentroX(),
						Globales.JUGADOR.getCentroY(), Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionY());
			}

			Globales.GESTOR_TEXTOS.agregarTexto("¡" + receta.getNombreVisible() + "!", Globales.JUGADOR.getCentroX(),
					Globales.JUGADOR.getPosicionYInt() - 8, principal.igu.textos.TipoTextoFlotante.ORO_EXP);
		}

		return exito;
	}

	public boolean fabricarPorId(final String idReceta) {
		final RecetaCrafteo receta = CatalogoRecetas.getRecetaPorId(idReceta);
		return this.fabricar(receta);
	}

	public boolean isEstacionDisponible(final EstacionCrafteo estacion) {
		return this.estacionesDisponibles.contains(estacion);
	}

	public EnumSet<EstacionCrafteo> getEstacionesDisponibles() {
		return this.estacionesDisponibles;
	}
}