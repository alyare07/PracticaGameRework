package principal.comandos;

import java.util.List;

import principal.crafteo.CatalogoRecetas;
import principal.crafteo.EstacionCrafteo;
import principal.crafteo.Ingrediente;
import principal.crafteo.RecetaCrafteo;
import principal.inventario.Inventario;
import principal.utilidades.Globales;

public class ComandoCrafteo extends Comando {

	public ComandoCrafteo() {
		super("craft", "craft <id_receta | lista | estaciones | ayuda>",
				"Consulta recetas de crafteo, ingredientes requeridos y fabrica ítems.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.GESTOR_CRAFTEO == null) || (Globales.GESTOR_INVENTARIO == null)) {
			this.enviarError(emisor, "El subsistema de crafteo o el inventario no estan listos.");
			return;
		}

		final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();

		// 1. Listar todas las recetas si no hay argumentos o se escribe 'lista'
		if ((args.length == 0) || args[0].equalsIgnoreCase("lista") || args[0].equalsIgnoreCase("list")) {
			this.mostrarListaRecetas(inv, emisor);
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.enviarInfo(emisor, "=== AYUDA: COMANDO CRAFT ==="
					+ "\n -> craft lista          : Muestra todas las recetas y estado de materiales"
					+ "\n -> craft estaciones     : Lista estaciones de trabajo cercanas detectadas"
					+ "\n -> craft <id_receta>    : Fabrica el ítem si posees materiales y estación"
					+ "\n (Ejemplo: 'craft hacha_tala_madera' o 'craft pico_mineria_piedra')");
			return;
		}

		// 3. Consultar Estaciones Cercanas
		if (sub.equals("estaciones") || sub.equals("estacion")) {
			final StringBuilder sb = new StringBuilder("Estaciones de crafteo activas: [ ");
			for (final EstacionCrafteo est : Globales.GESTOR_CRAFTEO.getEstacionesDisponibles()) {
				sb.append(est.getNombre()).append(" | ");
			}
			sb.append("]");
			this.enviarInfo(emisor, sb.toString());
			return;
		}

		// 4. Intentar Craftear por ID
		final RecetaCrafteo receta = CatalogoRecetas.getRecetaPorId(sub);
		if (receta == null) {
			this.enviarError(emisor, "Receta no encontrada: '" + args[0] + "'. Escribe 'craft lista' para ver las IDs validas.");
			return;
		}

		if (!Globales.GESTOR_CRAFTEO.isEstacionDisponible(receta.getEstacionRequerida())) {
			this.enviarError(emisor, "Requiere la estacion: '" + receta.getEstacionRequerida().getNombre() + "'. No hay ninguna cerca.");
			return;
		}

		if (!receta.puedeCraftear(inv)) {
			final StringBuilder sb = new StringBuilder("Faltan materiales para '").append(receta.getNombreVisible()).append("': ");
			for (final Ingrediente ing : receta.getIngredientes()) {
				final int disponible = inv.contarMunicionTotal(ing.getCodModeloItem());
				sb.append("\n -> ").append(ing.getCodModeloItem()).append(": ").append(disponible).append("/").append(ing.getCantidad());
			}
			this.enviarError(emisor, sb.toString());
			return;
		}

		final boolean exito = Globales.GESTOR_CRAFTEO.fabricar(receta);
		if (exito) {
			this.enviarInfo(emisor, "¡Has fabricado exitosamente: " + receta.getNombreVisible() + "!");
		} else {
			this.enviarError(emisor, "No se pudo fabricar (inventario lleno o sin espacio).");
		}
	}

	private void mostrarListaRecetas(final Inventario inv, final EmisorRespuesta emisor) {
		final List<RecetaCrafteo> recetas = CatalogoRecetas.getRecetas();
		final StringBuilder sb = new StringBuilder();
		sb.append("\n=================== CATALOGO DE CRAFTEO ===================\n");

		for (int i = 0; i < recetas.size(); i++) {
			final RecetaCrafteo r = recetas.get(i);
			final boolean puede = r.puedeCraftear(inv);
			final boolean estacionOk = Globales.GESTOR_CRAFTEO.isEstacionDisponible(r.getEstacionRequerida());

			final String estado = (puede && estacionOk) ? "[LISTO PARA CREAR]"
					: (!estacionOk ? "[REQUIERE " + r.getEstacionRequerida().name() + "]" : "[FALTAN MATERIALES]");

			sb.append(String.format(" -> %-20s (ID: %s) %s\n", r.getNombreVisible(), r.getIdReceta(), estado));
			sb.append("    Estacion: ").append(r.getEstacionRequerida().getNombre()).append(" | Ingredientes: ");

			final Ingrediente[] ings = r.getIngredientes();
			for (int j = 0; j < ings.length; j++) {
				final Ingrediente ing = ings[j];
				final int disp = (inv != null) ? inv.contarMunicionTotal(ing.getCodModeloItem()) : 0;
				sb.append(ing.getCodModeloItem()).append(" (").append(disp).append("/").append(ing.getCantidad()).append(") ");
			}
			sb.append("\n");
		}
		sb.append("===========================================================");

		this.enviarInfo(emisor, sb.toString());
	}
}