package principal.mapa.mapas;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.Random;

import org.json.simple.JSONObject;

import principal.entes.criaturas.enemigos.bandido.Bandido;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.entes.criaturas.neutrales.Comerciante;
import principal.entes.facciones.GestorFacciones;
import principal.entes.objetos.ArbolCofre;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.entes.objetos.recursos.ArbolCosechable;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.mapa.Mundo;
import principal.mapa.escenario.tps.PuertaArea;
import principal.maquinaestado.estados.GestorJuego;
import principal.maquinaestado.estados.GestorPartida;
import principal.maquinaestado.estados.pantallaCarga.GestorCarga;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;

public class Mapa1 extends Mapa {

	public static final String NOMBRE_MAPA = "Mapa1";
	public static final String EXTERIOR = "exterior";
	public static final String INTERIOR_CASA1 = "interior_casa1";
	public static final String EXTERIOR_DESIERTO = "ext_desierto";
	public static final String EXTERIOR_NIEVE = "ext_nieve";

	public Mapa1(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp) {
		super(gc, porcentajeCarga, gp);
	}

	public Mapa1(final GestorCarga gc, final int porcentajeCarga, final GestorPartida gp, final JSONObject jsonMapa) {
		super(gc, porcentajeCarga, gp, jsonMapa);
	}

	@Override
	protected void establecerMundos(final GestorCarga gc, final int porcentajeCarga) {
		final int cantMundos = 4; // 2 mundos: Exterior e Interior
		final int porcentajeCargaParcial = porcentajeCarga / cantMundos;
		final int porcentajeCargaEscenario = (75 * porcentajeCargaParcial) / 100;
		final int porcentajeCargaMundo = (25 * porcentajeCargaParcial) / 100;

		// 1. Exterior
		gc.setDetalleCarga("Generando mundo " + EXTERIOR);
		final Mundo mExterior = new Mundo(
				this.cargarEscenario(gc, porcentajeCargaEscenario, new File("mundos/escenario1.json")),
				new Point(1878, 1796), gc, porcentajeCargaMundo);
		mExterior.setNombreMundo(EXTERIOR);
		mExterior.setMapa(this);
		this.MUNDOS.put(EXTERIOR, mExterior);

		// 2. Interior de la Casa
		gc.setDetalleCarga("Generando mundo " + INTERIOR_CASA1);
		final Mundo mInterior = new Mundo(
				this.cargarEscenario(gc, porcentajeCargaEscenario, new File("mundos/interior_casa1.json")),
				new Point(1878, 1796), gc, porcentajeCargaMundo);
		mInterior.setNombreMundo(INTERIOR_CASA1);
		mInterior.setMapa(this);
		this.MUNDOS.put(INTERIOR_CASA1, mInterior);

		// 3. exterior desierto
		gc.setDetalleCarga("Generando mundo " + EXTERIOR_DESIERTO);
		final Mundo mExt_desierto = new Mundo(
				this.cargarEscenario(gc, porcentajeCargaEscenario, new File("mundos/ext_desierto.json")),
				new Point(1878, 1796), gc, porcentajeCargaMundo);
		mExt_desierto.setNombreMundo(EXTERIOR_DESIERTO);
		mExt_desierto.setMapa(this);
		this.MUNDOS.put(EXTERIOR_DESIERTO, mExt_desierto);

		// 4. exterior nieve
		gc.setDetalleCarga("Generando mundo " + EXTERIOR_NIEVE);
		final Mundo mExt_nieve = new Mundo(
				this.cargarEscenario(gc, porcentajeCargaEscenario, new File("mundos/ext_nieve.json")),
				new Point(1878, 1796), gc, porcentajeCargaMundo);
		mExt_nieve.setNombreMundo(EXTERIOR_NIEVE);
		mExt_nieve.setMapa(this);
		this.MUNDOS.put(EXTERIOR_NIEVE, mExt_nieve);
	}

	@Override
	public void establecerMundoActual(final String nombreMundo) {
		if ((nombreMundo != null) && this.MUNDOS.containsKey(nombreMundo)) {
			this.mundoActual = this.MUNDOS.get(nombreMundo);
			this.mundoActual.setNombreMundo(nombreMundo);
			this.mundoActual.aplicarMetadatosAtmosfericos();
		}
	}

	@Override
	protected void establecerMundoComienzo() {
		this.establecerMundoActual(EXTERIOR);
	}

	@Override
	protected void cargarFuncionalidadesPropias() {
		final GestorJuego jg = this.GP.getGestorJuego();
		Globales.JUGADOR.setFaccion(GestorFacciones.FACCION_BANDIDOS);
		new PuertaArea(new Rectangle(832, 333, 16, 16));

		// Recursos Cosechables en el exterior
		final Mundo mExt = this.MUNDOS.get(EXTERIOR);
		if (mExt != null) {
			mExt.meterEntidad(new ArbolCosechable(1789, 1854, ClaveHoja.ARBOLES_32, 0));
			mExt.meterEntidad(new ArbolCosechable(1777, 1854, ClaveHoja.ARBOLES_32, 1));
			mExt.meterEntidad(new RocaCosechable(1954, 1777, ClaveHoja.DUNGEON_16, 813));

			// Cofre con herramientas y armamento
			final ArbolCofre arbolcofre1 = new ArbolCofre(1800, 1900);
			arbolcofre1.getInventario()
					.agregarItem(new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0));
			arbolcofre1.getInventario()
					.agregarItem(new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0));
			arbolcofre1.getInventario().agregarItem(new EscopetaAutomatica());
			arbolcofre1.getInventario().agregarItem(new EscopetaRecortada());
			arbolcofre1.getInventario().agregarItem(new EscopetaTactica());
			arbolcofre1.getInventario().agregarItem(new SubfusilLigero());
			arbolcofre1.getInventario().agregarItem(new RifleAsalto());
			arbolcofre1.getInventario().agregarItem(new AmetralladoraPesada());

			arbolcofre1.getInventario().agregarItem(CajaMunicion.crear762mm(0, 0, 100));
			arbolcofre1.getInventario().agregarItem(CajaMunicion.crear9mm(0, 0, 100));
			arbolcofre1.getInventario().agregarItem(CajaMunicion.crearCartuchos12(0, 0, 100));

			arbolcofre1.getInventario()
					.agregarItem(new PiezaEquipo(PiezaEquipo.COD_CASCO_BASE, TipoEquipo.CASCO, 0, 0, 3, 5, 10));
			arbolcofre1.getInventario()
					.agregarItem(new PiezaEquipo(PiezaEquipo.COD_ARMADURA_BASE, TipoEquipo.TORSO, 4, 0, 0, 15, 10));
			arbolcofre1.getInventario()
					.agregarItem(new PiezaEquipo(PiezaEquipo.COD_BOTAS_CUERO, TipoEquipo.BOTAS, 0, 6, 0, 3, 10));
			arbolcofre1.getInventario()
					.agregarItem(new PiezaEquipo(PiezaEquipo.COD_ANILLO_ORO, TipoEquipo.ANILLO, 2, 2, 2, 0));

			mExt.meterEntidad(arbolcofre1);
			this.generarEnemigosParaPrueba(5);
			mExt.notificarModificacionEstructura();

			// Creamos al comerciante en el pueblo o cerca de una casa
			final Comerciante mercader = new Comerciante(1850, 1750, "Mercader Aldeano", 120.0);

			// Le asignamos su inventario inicial de venta
			mercader.getInventario().agregarItem(new PocionVidaMenor(10));
			mercader.getInventario().agregarItem(CajaMunicion.crear9mm(0, 0, 60));
			mercader.getInventario().agregarItem(CajaMunicion.crearCartuchos12(0, 0, 30));
			mercader.getInventario()
					.agregarItem(new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0));
			mercader.getInventario()
					.agregarItem(new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0));
			mercader.getInventario()
					.agregarItem(new PiezaEquipo(PiezaEquipo.COD_CASCO_BASE, TipoEquipo.CASCO, 0, 0, 3, 5));

			// Stock finito que se repone automáticamente cada 4 días de juego:
			mercader.setStockInfinito(false);
			mercader.setRenovacionAutomatica(true, 4);

			// Registra la mercancía tanto para la venta como para su plantilla de
			// reposición:
			mercader.registrarMercanciaInicial(new PocionVidaMenor(10));
			mercader.registrarMercanciaInicial(CajaMunicion.crear9mm(0, 0, 60));

			// Lo añadimos al mundo exterior
			this.mundoActual.meterEntidad(mercader);

		}

		Globales.JUGADOR.setModoDios(true);
	}

	public void generarEnemigosParaPrueba(final int cantidadDeseada) {
		final Mundo mExt = this.MUNDOS.get(EXTERIOR);
		if (mExt == null) {
			return;
		}

		final Random random = new Random();
		final int anchoLimite = Math.max(1, mExt.getTerreno().getAncho() - 50);
		final int altoLimite = Math.max(1, mExt.getTerreno().getAlto() - 50);
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

			final boolean colisionaTerreno = mExt.getTerreno().intersectaSolidoDijkstra(areaPrueba);
			final boolean colisionaObjeto = mExt.colisionaConObjetoSolido(areaPrueba);

			if (!colisionaTerreno && !colisionaObjeto) {
				final int vida = 150;
				final Bandido enemigo = new BandidoPistolero(posX, posY, vida, vida, mExt);
				mExt.meterEntidad(enemigo);
				generados++;
			}
		}
	}

	@Override
	public String[] getNombreMundos() {
		return new String[] { EXTERIOR, INTERIOR_CASA1 };
	}

	@Override
	public String getNombre() {
		return NOMBRE_MAPA;
	}
}