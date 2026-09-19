package principal.comandos;

import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.desplegables.KitFogata;
import principal.entes.objetos.items.desplegables.KitFogataAzul;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoAislamiento;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.inventario.Inventario;
import principal.utilidades.Globales;

public class ComandoGive extends Comando {

	public ComandoGive() {
		super("give", "give <item_id | clear | all> [cantidad]",
				"Entrega cualquier arma, armadura, consumible, munición o material al inventario del jugador.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if ((Globales.GESTOR_INVENTARIO == null) || (Globales.GESTOR_INVENTARIO.getInventarioJugador() == null)) {
			this.enviarError(emisor, "El inventario del jugador no está disponible.");
			return;
		}

		final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();

		if (args.length == 0) {
			this.enviarInfo(emisor,
					"Uso: give <item> [cantidad] | give clear | give all\nEscribe 'give ayuda' para ver los ítems disponibles.");
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		if (sub.equals("clear") || sub.equals("vaciar") || sub.equals("limpiar")) {
			inv.vaciar();
			this.enviarInfo(emisor, "Inventario del jugador vaciado por completo.");
			return;
		}

		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.enviarInfo(emisor, "ÍTEMS DISPONIBLES:" + "\n -> Materiales  : madera, piedra"
					+ "\n -> Pociones    : pocion_roja / vida" + "\n -> Munición    : 9mm, 12cal, 762mm, pesada"
					+ "\n -> Armas Fuego : pistola, recortada, tactica, escopeta_auto, subfusil, rifle, ametralladora"
					+ "\n -> Herramientas: hacha, pico"
					+ "\n -> Equipo      : casco, armadura, botas, anillo_oro, anillo_plata"
					+ "\n -> Desplegables: fogata, fogata_azul, granada" + "\n -> Pack total  : give all");
			return;
		}

		final int cantidad = (args.length >= 2) ? Math.max(1, this.parsearEntero(args[1], 1)) : 1;

		if (sub.equals("all") || sub.equals("todo") || sub.equals("starterpack")) {
			inv.agregarObjeto(new Pistola(Pistola.COD_PISTOLA));
			inv.agregarObjeto(new RifleAsalto());
			inv.agregarObjeto(CajaMunicion.crear9mm(0, 0, 60));
			inv.agregarObjeto(CajaMunicion.crear762mm(0, 0, 90));
			inv.agregarObjeto(new PocionVidaMenor(10));
			inv.agregarObjeto(RecursoMaterial.crearMadera(0, 0, 50));
			inv.agregarObjeto(RecursoMaterial.crearPiedra(0, 0, 50));
			inv.agregarObjeto(new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0));
			inv.agregarObjeto(new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0));
			inv.agregarObjeto(new KitFogata(2));
			this.enviarInfo(emisor, "Pack completo de supervivencia entregado al inventario.");
			return;
		}

		final Item item = this.resolverItem(sub, cantidad);
		if (item != null) {
			final boolean agregado = inv.agregarObjeto(item);
			if (agregado) {
				this.enviarInfo(emisor, "Entregado: " + item.getNombre() + " (x" + cantidad + ")");
			} else {
				this.enviarError(emisor, "No hay espacio suficiente en el inventario para " + item.getNombre());
			}
		} else {
			this.enviarError(emisor, "Ítem desconocido: '" + args[0] + "'. Escribe 'give ayuda' para ver la lista.");
		}
	}

	private Item resolverItem(final String id, final int cant) {
		switch (id) {
		case "madera":
		case "wood":
			return RecursoMaterial.crearMadera(0, 0, cant);
		case "piedra":
		case "stone":
			return RecursoMaterial.crearPiedra(0, 0, cant);
		case "pocion":
		case "pocion_roja":
		case "vida":
		case "heal":
			return new PocionVidaMenor(cant);
		case "9mm":
		case "bala_pistola":
			return CajaMunicion.crear9mm(0, 0, cant);
		case "12cal":
		case "cartuchos":
		case "escopeta_ammo":
			return CajaMunicion.crearCartuchos12(0, 0, cant);
		case "762mm":
		case "rifle_ammo":
			return CajaMunicion.crear762mm(0, 0, cant);
		case "pesada":
		case "heavy_ammo":
			return CajaMunicion.crearPesada(0, 0, cant);
		case "granada":
		case "bomba":
			return new GranadaT1(cant);
		case "fogata":
		case "camp":
			return new KitFogata(cant);
		case "fogata_azul":
		case "fogata_mistica":
			return new KitFogataAzul(cant);
		case "pistola":
			return new Pistola(Pistola.COD_PISTOLA);
		case "recortada":
			return new EscopetaRecortada();
		case "tactica":
			return new EscopetaTactica();
		case "escopeta_auto":
		case "spas":
			return new EscopetaAutomatica();
		case "subfusil":
		case "smg":
			return new SubfusilLigero();
		case "rifle":
		case "ak47":
		case "m4":
			return new RifleAsalto();
		case "ametralladora":
		case "m249":
			return new AmetralladoraPesada();
		case "hacha":
			return new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0);
		case "pico":
			return new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0);
		case "casco":
			return new PiezaEquipo(PiezaEquipo.COD_CASCO_BASE, TipoEquipo.CASCO, 0, 0, 3, 5, TipoAislamiento.CALOR, 3);
		case "armadura":
		case "peto":
			return new PiezaEquipo(PiezaEquipo.COD_ARMADURA_BASE, TipoEquipo.TORSO, 4, 0, 0, 15, TipoAislamiento.CALOR,
					3);
		case "botas":
			return new PiezaEquipo(PiezaEquipo.COD_BOTAS_CUERO, TipoEquipo.BOTAS, 0, 6, 0, 3, TipoAislamiento.CALOR, 3);
		case "anillo_oro":
			return new PiezaEquipo(PiezaEquipo.COD_ANILLO_ORO, TipoEquipo.ANILLO, 2, 2, 2, 0);
		case "anillo_plata":
			return new PiezaEquipo(PiezaEquipo.COD_ANILLO_PLATA, TipoEquipo.ANILLO, 1, 1, 1, 0);
		default:
			return null;
		}
	}
}