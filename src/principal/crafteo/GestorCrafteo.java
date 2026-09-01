package principal.crafteo;

import java.awt.Rectangle;
import java.util.EnumSet;

import principal.entes.criaturas.Jugador;
import principal.inventario.Inventario;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class GestorCrafteo {

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

		// Detección de fogatas / mesas de trabajo cercanas mediante Visitor Zero-GC
		mundo.paraCadaObjetoEn(this.areaDeteccionEstaciones, objeto -> {
			// Las fogatas, mesas o cofres de trabajo habilitan nuevas recetas
			this.estacionesDisponibles.add(EstacionCrafteo.FOGATA);
			this.estacionesDisponibles.add(EstacionCrafteo.MESA_TRABAJO);
		});
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

			Globales.GESTOR_TEXTOS.agregarTexto("¡" + receta.getNombreVisible() + "!",
					Globales.JUGADOR.getCentroX(), Globales.JUGADOR.getPosicionYInt() - 8,
					principal.igu.textos.TipoTextoFlotante.ORO_EXP);
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