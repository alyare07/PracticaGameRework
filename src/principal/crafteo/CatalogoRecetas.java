package principal.crafteo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.desplegables.KitCama;
import principal.entes.objetos.items.desplegables.KitFogata;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.utilidades.Globales;

public final class CatalogoRecetas {

	private static final ArrayList<RecetaCrafteo> RECETAS = new ArrayList<RecetaCrafteo>();

	static {
		inicializarRecetas();
	}

	private CatalogoRecetas() {
	}

	private static void inicializarRecetas() {
		// 1. Cuenco de Madera (A Mano: 2 Madera | Sin stats requeridos)
		registrarReceta(new RecetaCrafteo("cuenco_madera", "Cuenco de Madera", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 2) },
				new principal.entes.objetos.items.comidas.CuencoVacio(1)));

		// 2. Hacha de Tala (A Mano: 5 Madera | Pide Fuerza: 11)
		registrarReceta(new RecetaCrafteo("hacha_tala_madera", "Hacha de Tala", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 5) },
				new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0), 11, 0, 0));

		// 3. Pico de Minería (A Mano: 4 Madera + 4 Piedra | Pide Fuerza: 12)
		registrarReceta(new RecetaCrafteo("pico_mineria_piedra", "Pico de Minería", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 4),
						new Ingrediente(RecursoMaterial.COD_PIEDRA, 4) },
				new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0), 12, 0, 0));

		// 4. Poción de Vida Menor (A Mano: 3 Madera + 2 Piedra | Pide Inteligencia: 11)
		registrarReceta(new RecetaCrafteo("pocion_vida_menor", "Poción de Vida Menor", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 3),
						new Ingrediente(RecursoMaterial.COD_PIEDRA, 2) },
				new PocionVidaMenor(1), 0, 0, 11));

		// 5. Kit de Fogata (A Mano: 5 Madera + 2 Piedra | Sin requisitos)
		registrarReceta(new RecetaCrafteo("fogata_kit", "Kit de Fogata", EstacionCrafteo.MANUAL, new Ingrediente[] {
				new Ingrediente(RecursoMaterial.COD_MADERA, 5), new Ingrediente(RecursoMaterial.COD_PIEDRA, 2) },
				new KitFogata(1)));

		// 6. Pollo Asado al Fuego (Estación: FOGATA | 1 Pollo Crudo | Sin stats)
		registrarReceta(new RecetaCrafteo("pollo_asado_fogata", "Pollo Asado", EstacionCrafteo.FOGATA,
				new Ingrediente[] { new Ingrediente(principal.entes.objetos.items.comidas.CarnePolloCruda.CODIGO, 1) },
				new principal.entes.objetos.items.comidas.CarnePolloCocida(1)));

		// 7. Hervir Agua al Fuego (Estación: FOGATA | 1 Agua Turbia | Sin stats)
		registrarReceta(new RecetaCrafteo("hervir_agua_fogata", "Hervir Agua", EstacionCrafteo.FOGATA,
				new Ingrediente[] { new Ingrediente(principal.entes.objetos.items.comidas.CuencoAguaSucia.CODIGO, 1) },
				new principal.entes.objetos.items.comidas.CuencoAguaHervida(1)));

		// 8. Granada T1 (Mesa de Trabajo: 8 Piedra | Pide Inteligencia: 12)
		registrarReceta(new RecetaCrafteo("granada_t1", "Granada T1", EstacionCrafteo.MESA_TRABAJO,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_PIEDRA, 8) }, new GranadaT1(1), 0, 0, 12));
		// 9. Cama (A mano)
		registrarReceta(new RecetaCrafteo("cama_base", "Cama", EstacionCrafteo.MANUAL,
				new Ingrediente[] { new Ingrediente(RecursoMaterial.COD_MADERA, 3) }, new KitCama(1), 0, 0, 0));

		// --------------------EJEMPLO TEMPORAL Y DE PRUEBA -----------------------
		// Ejemplo: Caldo Lunar Nocturno (Exige cocinar de noche: 21:00 a 04:30)
		registrarReceta(new RecetaCrafteo("caldo_nocturno", "Caldo de Medianoche", EstacionCrafteo.FOGATA,
				new Ingrediente[] { new Ingrediente(principal.entes.objetos.items.comidas.CarnePolloCocida.CODIGO, 1),
						new Ingrediente(principal.entes.objetos.items.comidas.CuencoAguaHervida.CODIGO, 1) },
				new principal.entes.objetos.items.comidas.CarnePolloCocida(1), 0, 0, 10,
				(jugador, mundo) -> (Globales.GESTOR_ASTRONOMICO != null)
						&& ((Globales.GESTOR_ASTRONOMICO.getHoraActual() >= 21.0)
								|| (Globales.GESTOR_ASTRONOMICO.getHoraActual() <= 4.5)),
				"Cocinar exclusivamente de Noche (21:00 - 04:30)"));

		// --------------------FIN EJEMPLO-----------------------
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