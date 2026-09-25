package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.objetos.items.Antorcha;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.comidas.BayaSilvestre;
import principal.entes.objetos.items.comidas.CarnePolloCocida;
import principal.entes.objetos.items.comidas.CarnePolloCruda;
import principal.entes.objetos.items.comidas.CuencoAguaHervida;
import principal.entes.objetos.items.comidas.CuencoAguaSucia;
import principal.entes.objetos.items.comidas.CuencoVacio;
import principal.entes.objetos.items.desplegables.KitCama;
import principal.entes.objetos.items.desplegables.KitCarpa;
import principal.entes.objetos.items.desplegables.KitFogata;
import principal.entes.objetos.items.desplegables.KitFogataAzul;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.monedas.ItemMoneda;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.mapa.Tile;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class PaletaItems extends Paleta {

	@FunctionalInterface
	public interface CreadorItem {
		Item crear(int cantidad);
	}

	public static class EntradaItem {
		public final String nombre;
		public final BufferedImage icono;
		public final CreadorItem creador;
		public final boolean esConsumible;

		public EntradaItem(final String nombre, final BufferedImage icono, final boolean esConsumible,
				final CreadorItem creador) {
			this.nombre = nombre;
			this.icono = icono;
			this.esConsumible = esConsumible;
			this.creador = creador;
		}
	}

	private final ArrayList<EntradaItem> ENTRADAS = new ArrayList<EntradaItem>();
	private final CajaTextoPixel ctCantidad;
	private final EditorMapa editor;

	private static final Font FUENTE_LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 6);
	private static final Font FUENTE_BADGE = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public PaletaItems(final int x, final int y, final int ancho, final int alto, final int ladoSlot,
			final EditorMapa editor) {
		super(x, y + 20, ancho, alto - 20, ladoSlot);
		this.editor = editor;
		this.ctCantidad = new CajaTextoPixel(new Rectangle((x + ancho) - 45, y + 2, 40, 16), "10", 4, true);

		this.cargarCatalogoItems();
	}

	private void registrarEntradaItem(final Item muestra, final boolean esConsumible, final CreadorItem creador) {
		if (muestra != null) {
			this.ENTRADAS
					.add(new EntradaItem(muestra.getNombre(), muestra.getTexturaInventario(), esConsumible, creador));
		}
	}

	private void cargarCatalogoItems() {
		// 1. POCIONES Y CURACIÓN
		this.registrarEntradaItem(new PocionVidaMenor(1), true, cant -> new PocionVidaMenor(cant));

		// 2. MATERIAS PRIMAS Y MENAS DE MINERÍA
		this.registrarEntradaItem(RecursoMaterial.crearMadera(0, 0, 1), true,
				cant -> RecursoMaterial.crearMadera(0, 0, cant));
		this.registrarEntradaItem(RecursoMaterial.crearPiedra(0, 0, 1), true,
				cant -> RecursoMaterial.crearPiedra(0, 0, cant));
		this.registrarEntradaItem(new RecursoMaterial(1, RecursoMaterial.COD_CARBON), true,
				cant -> new RecursoMaterial(0, 0, cant, RecursoMaterial.COD_CARBON));
		this.registrarEntradaItem(new RecursoMaterial(1, RecursoMaterial.COD_COBRE), true,
				cant -> new RecursoMaterial(0, 0, cant, RecursoMaterial.COD_COBRE));
		this.registrarEntradaItem(new RecursoMaterial(1, RecursoMaterial.COD_HIERRO), true,
				cant -> new RecursoMaterial(0, 0, cant, RecursoMaterial.COD_HIERRO));
		this.registrarEntradaItem(new RecursoMaterial(1, RecursoMaterial.COD_ORO), true,
				cant -> new RecursoMaterial(0, 0, cant, RecursoMaterial.COD_ORO));
		this.registrarEntradaItem(new RecursoMaterial(1, RecursoMaterial.COD_CRISTAL), true,
				cant -> new RecursoMaterial(0, 0, cant, RecursoMaterial.COD_CRISTAL));

		// 3. COMIDAS Y VÍVERES
		this.registrarEntradaItem(new BayaSilvestre(1), true, cant -> new BayaSilvestre(cant));
		this.registrarEntradaItem(new CarnePolloCruda(1), true, cant -> new CarnePolloCruda(cant));
		this.registrarEntradaItem(new CarnePolloCocida(1), true, cant -> new CarnePolloCocida(cant));
		this.registrarEntradaItem(new CuencoVacio(1), true, cant -> new CuencoVacio(cant));
		this.registrarEntradaItem(new CuencoAguaSucia(1), true, cant -> new CuencoAguaSucia(cant));
		this.registrarEntradaItem(new CuencoAguaHervida(1), true, cant -> new CuencoAguaHervida(cant));

		// 4. MUNICIONES
		this.registrarEntradaItem(CajaMunicion.crear9mm(0, 0, 1), true, cant -> CajaMunicion.crear9mm(0, 0, cant));
		this.registrarEntradaItem(CajaMunicion.crearCartuchos12(0, 0, 1), true,
				cant -> CajaMunicion.crearCartuchos12(0, 0, cant));
		this.registrarEntradaItem(CajaMunicion.crear762mm(0, 0, 1), true, cant -> CajaMunicion.crear762mm(0, 0, cant));
		this.registrarEntradaItem(CajaMunicion.crearPesada(0, 0, 1), true,
				cant -> CajaMunicion.crearPesada(0, 0, cant));

		// 5. EXPLOSIVOS Y ARROJADIZOS
		this.registrarEntradaItem(new GranadaT1(1), true, cant -> new GranadaT1(cant));

		// 6. HERRAMIENTAS
		this.registrarEntradaItem(new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0),
				false, cant -> new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0));
		this.registrarEntradaItem(new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0), false,
				cant -> new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0));

		// 7. ARMAS DE FUEGO
		this.registrarEntradaItem(new Pistola(Pistola.COD_PISTOLA), false, cant -> new Pistola(Pistola.COD_PISTOLA));
		this.registrarEntradaItem(new EscopetaRecortada(), false, cant -> new EscopetaRecortada());
		this.registrarEntradaItem(new EscopetaTactica(), false, cant -> new EscopetaTactica());
		this.registrarEntradaItem(new EscopetaAutomatica(), false, cant -> new EscopetaAutomatica());
		this.registrarEntradaItem(new SubfusilLigero(), false, cant -> new SubfusilLigero());
		this.registrarEntradaItem(new RifleAsalto(), false, cant -> new RifleAsalto());
		this.registrarEntradaItem(new AmetralladoraPesada(), false, cant -> new AmetralladoraPesada());

		// 8. EQUIPAMIENTO Y JOYERÍA
		this.registrarEntradaItem(new PiezaEquipo(PiezaEquipo.COD_CASCO_BASE, TipoEquipo.CASCO, 0, 0, 3, 5), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_CASCO_BASE, TipoEquipo.CASCO, 0, 0, 3, 5));
		this.registrarEntradaItem(new PiezaEquipo(PiezaEquipo.COD_ARMADURA_BASE, TipoEquipo.TORSO, 4, 0, 0, 15), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_ARMADURA_BASE, TipoEquipo.TORSO, 4, 0, 0, 15));
		this.registrarEntradaItem(new PiezaEquipo(PiezaEquipo.COD_BOTAS_CUERO, TipoEquipo.BOTAS, 0, 6, 0, 3), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_BOTAS_CUERO, TipoEquipo.BOTAS, 0, 6, 0, 3));
		this.registrarEntradaItem(new PiezaEquipo(PiezaEquipo.COD_ANILLO_ORO, TipoEquipo.ANILLO, 2, 2, 2, 0), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_ANILLO_ORO, TipoEquipo.ANILLO, 2, 2, 2, 0));
		this.registrarEntradaItem(new PiezaEquipo(PiezaEquipo.COD_ANILLO_PLATA, TipoEquipo.ANILLO, 1, 1, 1, 0), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_ANILLO_PLATA, TipoEquipo.ANILLO, 1, 1, 1, 0));

		// 9. KITS DESPLEGABLES Y PORTÁTILES
		this.registrarEntradaItem(new Antorcha(), false, cant -> new Antorcha());
		this.registrarEntradaItem(new KitFogata(1), true, cant -> new KitFogata(cant));
		this.registrarEntradaItem(new KitFogataAzul(1), true, cant -> new KitFogataAzul(cant));
		this.registrarEntradaItem(new KitCarpa(1), true, cant -> new KitCarpa(cant));
		this.registrarEntradaItem(new KitCama(1), true, cant -> new KitCama(cant));

		// 10. MONEDAS
		this.registrarEntradaItem(ItemMoneda.crearPlata(0, 0, 1), true, cant -> ItemMoneda.crearPlata(0, 0, cant));
		this.registrarEntradaItem(ItemMoneda.crearOro(0, 0, 1), true, cant -> ItemMoneda.crearOro(0, 0, cant));
	}

	@Override
	public void actualizar(final Raton raton) {
		this.ctCantidad.actualizar(raton);

		if ((raton == null) || !raton.presionadoClickIzqUnicaAct()) {
			return;
		}

		final Point pClick = raton.getPuntoPosicionEscalado();

		if (this.botonPaginaAnterior.contains(pClick)) {
			this.anteriorPagina();
			return;
		}
		if (this.botonPaginaSiguiente.contains(pClick)) {
			this.siguientePagina();
			return;
		}

		if (this.AREA.contains(pClick)) {
			final int relX = pClick.x - (this.AREA.x + this.MARGEN);
			final int relY = pClick.y - (this.AREA.y + this.MARGEN);

			if ((relX < 0) || (relY < 0)) {
				return;
			}

			final int paso = this.LADO_SLOT + this.MARGEN;
			final int col = relX / paso;
			final int fila = relY / paso;

			if ((col >= 0) && (col < this.COLUMNAS) && (fila >= 0) && (fila < this.FILAS)) {
				final int indiceGlobal = (this.paginaActual * this.ELEMENTOS_POR_PAGINA) + (fila * this.COLUMNAS) + col;
				if (indiceGlobal < this.ENTRADAS.size()) {
					this.indiceSeleccionado = indiceGlobal;
					final EntradaItem entrada = this.ENTRADAS.get(indiceGlobal);

					final int cant = entrada.esConsumible ? Math.max(1, this.ctCantidad.getNumeroEntero(1)) : 1;
					final Item nuevoItem = entrada.creador.crear(cant);

					if (this.editor != null) {
						this.editor.getItemPuntero().setItemDirecto(nuevoItem);
						GestorSonido.reproducir(IDSonido.GOLPE_1);
					}
				}
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int cabY = this.AREA.y - 20;
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, cabY, this.AREA.width, 20, new Color(35, 35, 40));
		Render2D.dibujarRectanguloContorno(g, this.AREA.x, cabY, this.AREA.width, 20, Color.BLACK);

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_LABEL);
		Render2D.dibujarStringConSombra(g, "Cant. Spawn:", this.AREA.x + 4, cabY + 12, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);

		this.ctCantidad.pintar(g);
		super.pintar(g);
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final EntradaItem entrada = this.ENTRADAS.get(index);
		if (entrada.icono != null) {
			this.dibujarIconoAjustadoAlSlot(g, entrada.icono, slotX, slotY);
		}

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_BADGE);
		final String badge = entrada.esConsumible ? "C" : "P";
		final Color colorBadge = entrada.esConsumible ? new Color(255, 200, 60) : new Color(80, 210, 255);

		Render2D.dibujarRectanguloRelleno(g, slotX + 1, slotY + 1, 6, 6, Color.BLACK);
		Render2D.dibujarString(g, badge, slotX + 2, slotY + 6, colorBadge);
		g.setFont(fontPrevia);
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.ENTRADAS.size();
	}

	@Override
	public String getNombreElemento(final int index) {
		return ((index >= 0) && (index < this.ENTRADAS.size())) ? this.ENTRADAS.get(index).nombre : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		return false;
	}
}