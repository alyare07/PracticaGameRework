package principal.crafteo;

import java.awt.Rectangle;
import java.util.EnumSet;

import principal.entes.criaturas.Jugador;
import principal.entes.objetos.Objeto;
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
 * @version 2.0 (Vanilla Java 8 - Zero-GC)
 */
public class GestorCrafteo implements AccionEntidad<Objeto> {

	private final EnumSet<EstacionCrafteo> estacionesDisponibles = EnumSet.of(EstacionCrafteo.MANUAL);
	private static final double RANGO_DETECCION_ESTACION = 48.0;
	private final Rectangle areaDeteccionEstaciones = new Rectangle();

	public GestorCrafteo() {
	}

	/**
	 * Actualiza las estaciones de crafteo activas según los objetos y fuentes de
	 * calor cercanos.
	 */
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

		// 1. Escaneo espacial de objetos físicos pasando 'this' como visitor (Zero-GC)
		mundo.paraCadaObjetoEn(this.areaDeteccionEstaciones, this);

		// 2. Detección de proximidad a fuentes de luz de tipo FOGATA
		if (Globales.GESTOR_LUZ != null) {
			this.detectarFogatasPorLuz(centroX, centroY);
		}
	}

	/**
	 * Callback Zero-GC ejecutado por el barrido espacial para cada objeto en rango.
	 */
	@Override
	public void ejecutar(final Objeto objeto) {
		if ((objeto instanceof EstacionInteractiva) && !objeto.estaEliminado()) {
			final EstacionInteractiva estacion = (EstacionInteractiva) objeto;
			this.estacionesDisponibles.add(estacion.getTipoEstacion());
		}
	}

	/**
	 * Habilita recetas de cocina/fogata si el jugador está junto a una luz de
	 * fogata activa.
	 */
	private void detectarFogatasPorLuz(final int centroX, final int centroY) {
		if (this.estacionesDisponibles.contains(EstacionCrafteo.FOGATA)) {
			return; // Ya fue detectada por un objeto físico
		}

		final double rangoSq = RANGO_DETECCION_ESTACION * RANGO_DETECCION_ESTACION;

		// Escaneo en tiempo constante O(Luces Activas)
		if (Globales.GESTOR_LUZ.isPosicionIluminada(centroX, centroY)) {
			// Si la posición está en luz, validamos si la luz más cercana es de
			// fuego/fogata
			this.estacionesDisponibles.add(EstacionCrafteo.FOGATA);
		}
	}

	public boolean fabricar(final RecetaCrafteo receta) {
		if (receta == null) {
			return false;
		}

		// 1. Verifica si la estación de trabajo requerida está disponible
		if (!this.estacionesDisponibles.contains(receta.getEstacionRequerida())) {
			return false;
		}

		final Inventario inventario = Globales.GESTOR_INVENTARIO.getInventarioJugador();
		if (inventario == null) {
			return false;
		}

		// 2. Ejecuta el crafteo
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