package principal.crafteo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;

public final class CatalogoRecetas {

	private static final ArrayList<RecetaCrafteo> RECETAS = new ArrayList<RecetaCrafteo>();

	static {
		inicializarRecetas();
	}

	private CatalogoRecetas() {
	}

	private static void inicializarRecetas() {
		// 1. Hacha de Tala Básica (A Mano: 5 Madera)
		registrarReceta(new RecetaCrafteo("hacha_tala_madera", "Hacha de Tala", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 5) },
				new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0)));

		// 2. Pico de Minería Básico (A Mano: 4 Madera + 4 Piedra)
		registrarReceta(new RecetaCrafteo("pico_mineria_piedra", "Pico de Minería", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 4),
						new Ingrediente(RecursoMaterial.COD_PIEDRA, 4) },
				new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0)));

		// 3. Poción de Vida Menor (A Mano: 3 Madera + 2 Piedra)
		registrarReceta(new RecetaCrafteo("pocion_vida_menor", "Poción de Vida Menor", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 3),
						new Ingrediente(RecursoMaterial.COD_PIEDRA, 2) },
				new PocionVidaMenor(1)));

		// 4. Paquete de Munición 9mm (Mesa de Trabajo: 6 Piedra)
		registrarReceta(new RecetaCrafteo("caja_municion_9mm", "Caja Munición 9mm", EstacionCrafteo.MESA_TRABAJO,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_PIEDRA, 6) }, CajaMunicion.crear9mm(0, 0, 15)));

		// 5. Granada T1 (Mesa de Trabajo: 8 Piedra)
		registrarReceta(new RecetaCrafteo("granada_t1", "Granada T1", EstacionCrafteo.MESA_TRABAJO,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_PIEDRA, 8) }, new GranadaT1(1)));
	}

	public static void registrarReceta(final RecetaCrafteo receta) {
		if (receta != null) {
			RECETAS.add(receta);
		}
	}

	public static List<RecetaCrafteo> getRecetas() {
		return Collections.unmodifiableList(RECETAS);
	}

	public static RecetaCrafteo getRecetaPorId(final String idReceta) {
		if (idReceta == null) {
			return null;
		}
		for (int i = 0; i < RECETAS.size(); i++) {
			final RecetaCrafteo r = RECETAS.get(i);
			if (idReceta.equalsIgnoreCase(r.getIdReceta())) {
				return r;
			}
		}
		return null;
	}
}