package principal.mapa.mapas;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.Random;

import org.json.simple.JSONObject;

import principal.entes.criaturas.enemigos.bandido.Bandido;
import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.eventos.EventoJugadorZonaTP;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;
import principal.utilidades.Textura;

public class Mapa1 extends Mapa {
	public static final String NOMBRE_MAPA = "Mapa1";
	public static final String EXTERIOR = "Exterior";

	public Mapa1(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp) {
		super(gc, porcentajeCarga, gp);
	}

	public Mapa1(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp, final JSONObject jsonMapa) {
		super(gc, porcentajeCarga, gp, jsonMapa);
	}

	@Override
	protected void establecerMundos(final GestorCarga gc, final int porcentajeCarga) {
		final int cantMundos = 1;
		final int porcentajeCargaParcial = porcentajeCarga / cantMundos;
		final int porcentajeCargaEscenario = (75 * porcentajeCargaParcial) / 100;
		final int porcentajeCargaMundo = (25 * porcentajeCargaParcial) / 100;

		gc.setDetalleCarga("Generando mundo " + EXTERIOR);
		this.MUNDOS.put(EXTERIOR,
				new Mundo(this.cargarEscenario(gc, porcentajeCargaEscenario, new File("escenario1.json")),
						new Point(697, 437), gc, porcentajeCargaMundo));
	}

	@Override
	protected void establecerMundoActual() {
		this.mundoActual = this.MUNDOS.get(EXTERIOR);
	}

	@Override
	protected void cargarFuncionalidadesPropias() {
		final GestorJuego jg = this.GP.getGestorJuego();
		final ZonaTP zonaTP2 = new ZonaTP(new Rectangle(684, 215, 20, 20), null);
		final ZonaTP zonaTP = new ZonaTP(new Rectangle(878, 173, 20, 20),
				new PuertaArea(new Rectangle(832, 333, 16, 16)));
		zonaTP2.setPuertaTP(new PuertaMapa(MapaManager.MAPA_0, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, false, this.GP));
		this.mundoActual.meterEntidad(zonaTP);
		this.mundoActual.meterEntidad(zonaTP2);
		jg.meterEvento(new EventoJugadorZonaTP(zonaTP, jg, true));
		jg.meterEvento(new EventoJugadorZonaTP(zonaTP2, jg, true));

		// Enemigos iniciales
		this.mundoActual.meterEntidad(new BandidoGarrote(890, 220, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoGarrote(897, 220, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoGarrote(876, 220, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoGranadero(927, 64, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoPistolero(670, 121, 50, 50, this.mundoActual));

		// Recursos Cosechables de prueba cerca del spawn
		this.mundoActual.meterEntidad(new ArbolCosechable(720, 420, Textura.TEXTURA_x32_ARBOL_1));
		this.mundoActual.meterEntidad(new ArbolCosechable(750, 400, Textura.TEXTURA_x32_ARBOL_2));
		this.mundoActual.meterEntidad(new RocaCosechable(710, 460, Textura.TEXTURA_x16_MURO_PIEDRA_NEGRA));

		// Cofre con herramientas y armamento
		final ArbolCofre arbolcofre1 = new ArbolCofre(767, 424);
		arbolcofre1.getInventario().agregarItem(
				new Herramienta(ListaModelosItem.COD_HERRAMIENTA_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0));
		arbolcofre1.getInventario().agregarItem(
				new Herramienta(ListaModelosItem.COD_HERRAMIENTA_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0));
		arbolcofre1.getInventario().agregarItem(new EscopetaAutomatica());
		arbolcofre1.getInventario().agregarItem(new EscopetaRecortada());
		arbolcofre1.getInventario().agregarItem(new EscopetaTactica());
		arbolcofre1.getInventario().agregarItem(new SubfusilLigero());
		arbolcofre1.getInventario().agregarItem(new RifleAsalto());
		arbolcofre1.getInventario().agregarItem(new AmetralladoraPesada());

		arbolcofre1.getInventario().agregarItem(CajaMunicion.crear762mm(0, 0, 100));
		arbolcofre1.getInventario().agregarItem(CajaMunicion.crear9mm(0, 0, 100));
		arbolcofre1.getInventario().agregarItem(CajaMunicion.crearCartuchos12(0, 0, 100));
		this.mundoActual.meterEntidad(arbolcofre1);
		this.generarEnemigosParaPrueba(5);
		Globales.JUGADOR.setModoDios(true);

		arbolcofre1.getInventario()
				.agregarItem(new principal.entes.objetos.items.equipamiento.PiezaEquipo(
						ListaModelosItem.COD_EQUIPABLE_CASCO_LIGERA,
						principal.entes.objetos.items.equipamiento.TipoEquipo.CASCO, 0, 0, 3, 5)); // Casco: +3 INT, +5
																									// DEF

		arbolcofre1.getInventario()
				.agregarItem(new principal.entes.objetos.items.equipamiento.PiezaEquipo(
						ListaModelosItem.COD_EQUIPABLE_ARMADURA_LIGERA,
						principal.entes.objetos.items.equipamiento.TipoEquipo.TORSO, 4, 0, 0, 15)); // Armadura: +4 FUE,
																									// +15 DEF

		arbolcofre1.getInventario()
				.agregarItem(new principal.entes.objetos.items.equipamiento.PiezaEquipo(
						ListaModelosItem.COD_PORTABLE_BOTAS_CUERO,
						principal.entes.objetos.items.equipamiento.TipoEquipo.BOTAS, 0, 6, 0, 3)); // Botas: +6 AGI, +3
																									// DEF

		arbolcofre1.getInventario()
				.agregarItem(new principal.entes.objetos.items.equipamiento.PiezaEquipo(
						ListaModelosItem.COD_EQUIPABLE_ANILLO_ORO,
						principal.entes.objetos.items.equipamiento.TipoEquipo.ANILLO, 2, 2, 2, 0)); // Anillo Oro: +2 a
																									// todo
	}

	public void generarEnemigosParaPrueba(final int cantidadDeseada) {
		if (this.mundoActual == null) {
			return;
		}

		final Random random = new Random();
		final int anchoLimite = Math.max(1, this.mundoActual.getTerreno().getAncho() - 50);
		final int altoLimite = Math.max(1, this.mundoActual.getTerreno().getAlto() - 50);
		final int anchoBandido = 12;
		final int altoBandido = 20;
		final Rectangle areaPrueba = new Rectangle(0, 0, anchoBandido, altoBandido);

		int generados = 0;
		int intentos = 0;
		final int intentosMaximos = cantidadDeseada * 100;

		while ((generados < cantidadDeseada) && (intentos < intentosMaximos)) {
			intentos++;
			final int posX = random.nextInt(anchoLimite);
			final int posY = random.nextInt(altoLimite);
			areaPrueba.setLocation(posX, posY);

			final boolean colisionaTerreno = this.mundoActual.getTerreno().intersectaSolidoDijkstra(areaPrueba);
			final boolean colisionaObjeto = this.mundoActual.colisionaConObjetoSolido(areaPrueba);

			if (!colisionaTerreno && !colisionaObjeto) {
				final Bandido enemigo = new BandidoPistolero(posX, posY, 50, 50, this.mundoActual);
				this.mundoActual.meterEntidad(enemigo);
				generados++;
			}
		}
	}

	@Override
	public String[] getNombreMundos() {
		final String[] lista = { EXTERIOR };
		return lista;
	}

	@Override
	public String getNombre() {
		return NOMBRE_MAPA;
	}
}