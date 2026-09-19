package principal.comandos;

import principal.entes.Ente;
import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.entes.criaturas.grupo.TipoVinculo;
import principal.entes.criaturas.mascotas.Mascota;
import principal.entes.criaturas.neutrales.Comerciante;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.Fogata;
import principal.entes.objetos.cofres.CofreMediano;
import principal.entes.objetos.cofres.CofrePequeño;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.mapa.Mundo;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;

public class ComandoSpawn extends Comando {

	public ComandoSpawn() {
		super("spawn", "spawn <tipo> [subtipo/nombre] [x] [y] [cant]",
				"Genera enemigos, NPCs comerciantes, mascotas, cofres, fogatas o recursos cosechables en el mapa.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null || Globales.JUGADOR.getMundo() == null) {
			this.enviarError(emisor, "El mundo actual no está disponible para generar entidades.");
			return;
		}

		final Mundo mundo = Globales.JUGADOR.getMundo();

		if (args.length == 0) {
			this.enviarInfo(emisor, "Uso: spawn <tipo> [subtipo] [x] [y]\nOpciones de tipo: bandido, comerciante, mascota, arbol, roca, cofre, fogata");
			return;
		}

		final String tipo = args[0].toLowerCase().trim();
		double x = Globales.JUGADOR.getCentroX();
		double y = Globales.JUGADOR.getCentroY();

		if (tipo.equals("bandido") || tipo.equals("enemigo")) {
			final String subtipo = (args.length >= 2) ? args[1].toLowerCase() : "pistolero";
			final int cant = (args.length >= 3) ? this.parsearEntero(args[2], 1) : 1;

			for (int i = 0; i < cant; i++) {
				final double offsetX = (cant > 1) ? ((Math.random() * 64) - 32) : 0.0;
				final double offsetY = (cant > 1) ? ((Math.random() * 64) - 32) : 0.0;
				Ente e = null;

				if (subtipo.contains("garrote") || subtipo.contains("mele")) {
					e = new BandidoGarrote(x + offsetX, y + offsetY, 50, 50, mundo);
				} else if (subtipo.contains("granada") || subtipo.contains("granadero")) {
					e = new BandidoGranadero(x + offsetX, y + offsetY, 50, 50, mundo);
				} else {
					e = new BandidoPistolero(x + offsetX, y + offsetY, 50, 50, mundo);
				}
				mundo.meterEntidad(e);
			}
			this.enviarInfo(emisor, "Generado(s) " + cant + " Bandido(s) [" + subtipo + "] cerca del jugador.");
			return;
		}

		if (tipo.equals("comerciante") || tipo.equals("mercader") || tipo.equals("npc")) {
			final String nombre = (args.length >= 2) ? args[1] : "Mercader Ambulante";
			final Comerciante com = new Comerciante(x, y, nombre, 120.0);
			mundo.meterEntidad(com);
			this.enviarInfo(emisor, "Comerciante '" + nombre + "' generado en (" + (int) x + ", " + (int) y + ")");
			return;
		}

		if (tipo.equals("mascota") || tipo.equals("pet") || tipo.equals("companero")) {
			final String nombre = (args.length >= 2) ? args[1] : "Lobo";
			final Mascota mas = new Mascota(x, y, nombre, 100.0, TipoVinculo.MASCOTA);
			mundo.meterEntidad(mas);
			this.enviarInfo(emisor, "Mascota compañera '" + nombre + "' generada.");
			return;
		}

		if (tipo.equals("arbol") || tipo.equals("tree")) {
			final ArbolCosechable arbol = new ArbolCosechable((int) x - 16, (int) y - 16, ClaveHoja.ARBOLES_32, 0);
			mundo.meterEntidad(arbol);
			this.enviarInfo(emisor, "Árbol talable generado en la posición.");
			return;
		}

		if (tipo.equals("roca") || tipo.equals("rock") || tipo.equals("mineral")) {
			final RocaCosechable roca = new RocaCosechable((int) x - 16, (int) y - 16, ClaveHoja.DUNGEON_16, 813);
			mundo.meterEntidad(roca);
			this.enviarInfo(emisor, "Roca minable generada en la posición.");
			return;
		}

		if (tipo.equals("cofre") || tipo.equals("chest")) {
			final String tam = (args.length >= 2) ? args[1].toLowerCase() : "mediano";
			final Ente cofre = tam.contains("peq") ? new CofrePequeño((int) x, (int) y)
					: (tam.contains("arbol") ? new ArbolCofre((int) x, (int) y) : new CofreMediano((int) x, (int) y));
			mundo.meterEntidad(cofre);
			this.enviarInfo(emisor, "Cofre [" + tam + "] generado.");
			return;
		}

		if (tipo.equals("fogata") || tipo.equals("fire")) {
			final boolean azul = (args.length >= 2) && args[1].toLowerCase().contains("azul");
			final Fogata fog = new Fogata((int) x - 8, (int) y - 8, 3, true, azul);
			mundo.meterEntidad(fog);
			this.enviarInfo(emisor, "Fogata [" + (azul ? "Fuego Azul Mística" : "Normal") + "] generada.");
			return;
		}

		this.enviarError(emisor, "Tipo de spawn no reconocido: '" + tipo + "'. Opciones: bandido, comerciante, mascota, arbol, roca, cofre, fogata");
	}
}