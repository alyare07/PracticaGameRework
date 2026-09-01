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
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.eventos.EventoJugadorZonaTP;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.ZonaTP;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.utilidades.Globales;

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
//	this.mundo = new Mundo(esc, new Point(776, 300), gc, 25);
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
//	this.mundoActual.eliminarCriaturas();
		final ZonaTP zonaTP = new ZonaTP(new Rectangle(878, 173, 20, 20),
				new PuertaArea(new Rectangle(832, 333, 16, 16)));
		zonaTP2.setPuertaTP(new PuertaMapa(MapaManager.MAPA_0, Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, false, this.GP));
		this.mundoActual.meterEntidad(zonaTP);
		this.mundoActual.meterEntidad(zonaTP2);
		jg.meterEvento(new EventoJugadorZonaTP(zonaTP, jg, true));
		jg.meterEvento(new EventoJugadorZonaTP(zonaTP2, jg, true));
		this.mundoActual.meterEntidad(new BandidoGarrote(890, 220, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoGarrote(897, 220, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoGarrote(876, 220, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoGranadero(927, 64, 50, 50, this.mundoActual));
		this.mundoActual.meterEntidad(new BandidoPistolero(670, 121, 50, 50, this.mundoActual));
		this.generarEnemigosParaPrueba(3000);
		final ArbolCofre arbolcofre1 = new ArbolCofre(767, 424);
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
		Globales.JUGADOR.setModoDios(true);
	}

	/***
	 * METODO DE PRUEBA PARA PROBAR RENDIMIENTO CON X CANTIDAD DE CRIATURAS EN EL
	 * MUNDO
	 * 
	 * @param cantidadDeseada
	 */
	public void generarEnemigosParaPrueba(final int cantidadDeseada) {
		if (this.mundoActual == null) {
			return;
		}

		final Random random = new Random();

		// Límites del mapa restando 50px de seguridad
		final int anchoLimite = Math.max(1, this.mundoActual.getTerreno().getAncho() - 50);
		final int altoLimite = Math.max(1, this.mundoActual.getTerreno().getAlto() - 50);
		System.out.println(anchoLimite);
		System.out.println(altoLimite);
		// Dimensiones de la colisión del Bandido
		final int anchoBandido = 12;
		final int altoBandido = 20;

		// Rectangle auxiliar reutilizable para validar posición sin instanciar objetos
		// en el Heap
		final Rectangle areaPrueba = new Rectangle(0, 0, anchoBandido, altoBandido);

		int generados = 0;
		int intentos = 0;
		// Evita bucle infinito si el mapa estuviera casi cubierto de sólidos
		final int intentosMaximos = cantidadDeseada * 100;

		while ((generados < cantidadDeseada) && (intentos < intentosMaximos)) {
			intentos++;

			// Coordenadas aleatorias entre 0 y los límites del mapa
			final int posX = random.nextInt(anchoLimite);
			final int posY = random.nextInt(altoLimite);

			// Posicionamos la caja de colisión auxiliar en la posición aleatoria
			areaPrueba.setLocation(posX, posY);

			// Verificamos si en esa posición colisiona con el terreno o con un objeto
			// sólido (árbol, etc.)
			final boolean colisionaTerreno = this.mundoActual.getTerreno().intersectaSolidoDijkstra(areaPrueba);
			final boolean colisionaObjeto = this.mundoActual.colisionaConObjetoSolido(areaPrueba);

			// Si la zona está libre de colisiones
			if (!colisionaTerreno && !colisionaObjeto) {
				// Instanciamos y agregamos la entidad
				final Bandido enemigo = new BandidoPistolero(posX, posY, anchoBandido, altoBandido, this.mundoActual);
				this.mundoActual.meterEntidad(enemigo);
				generados++;
			}
		}

		System.out.println("--- PRUEBA DE RENDIMIENTO ---");
		System.out.println("Enemigos solicitados: " + cantidadDeseada);
		System.out.println("Enemigos colocados exitosamente: " + generados);
		System.out.println("Intentos totales: " + intentos);
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
