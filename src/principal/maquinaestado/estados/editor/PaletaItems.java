package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.AmetralladoraPesada;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.RifleAsalto;
import principal.entes.objetos.items.armas.distancia.fuego.automaticas.SubfusilLigero;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaAutomatica;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaRecortada;
import principal.entes.objetos.items.armas.distancia.fuego.escopetas.EscopetaTactica;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.objetos.items.equipamiento.TipoEquipo;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.entes.objetos.items.municiones.CajaMunicion;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.mapa.Tile;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.recursos.TexturaItem;
import principal.utilidades.Globales;
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

		// Caja de texto numérica en la cabecera de la paleta
		this.ctCantidad = new CajaTextoPixel(new Rectangle(x + ancho - 45, y + 2, 40, 16), "10", 4, true);

		this.cargarCatalogoItems();
	}

	private void cargarCatalogoItems() {
		// 1. Pociones
		this.agregarEntrada("Poción de Vida Menor", Globales.GESTOR_TEXTURAS.get(TexturaItem.POCION_ROJA_INV), true,
				cant -> new PocionVidaMenor(cant));

		// 2. Materiales de Construcción
		this.agregarEntrada("Madera", Globales.GESTOR_TEXTURAS.get(TexturaItem.MADERA_INV), true,
				cant -> RecursoMaterial.crearMadera(0, 0, cant));
		this.agregarEntrada("Piedra", Globales.GESTOR_TEXTURAS.get(TexturaItem.PIEDRA_INV), true,
				cant -> RecursoMaterial.crearPiedra(0, 0, cant));

		// 3. Munición
		this.agregarEntrada("Munición 9mm", Globales.GESTOR_TEXTURAS.get(TexturaItem.CAJA_MUNICION_INV), true,
				cant -> CajaMunicion.crear9mm(0, 0, cant));
		this.agregarEntrada("Cartuchos Calibre 12", Globales.GESTOR_TEXTURAS.get(TexturaItem.CAJA_MUNICION_INV), true,
				cant -> CajaMunicion.crearCartuchos12(0, 0, cant));
		this.agregarEntrada("Munición 7.62mm", Globales.GESTOR_TEXTURAS.get(TexturaItem.CAJA_MUNICION_INV), true,
				cant -> CajaMunicion.crear762mm(0, 0, cant));
		this.agregarEntrada("Munición Pesada", Globales.GESTOR_TEXTURAS.get(TexturaItem.CAJA_MUNICION_INV), true,
				cant -> CajaMunicion.crearPesada(0, 0, cant));

		// 4. Arrojadizos
		this.agregarEntrada("Granada T1", Globales.GESTOR_TEXTURAS.get(TexturaItem.GRANADA_T1_INV), true,
				cant -> new GranadaT1(cant));

		// 5. Herramientas
		this.agregarEntrada("Hacha de Tala", Globales.GESTOR_TEXTURAS.get(TexturaItem.HACHA_BASICO_INV), false,
				cant -> new Herramienta(Herramienta.COD_HACHA, 8, 14, 350, TipoHerramienta.HACHA, 35.0));
		this.agregarEntrada("Pico de Minería", Globales.GESTOR_TEXTURAS.get(TexturaItem.PICO_BASICO_INV), false,
				cant -> new Herramienta(Herramienta.COD_PICO, 6, 14, 400, TipoHerramienta.PICO, 30.0));

		// 6. Armas de Fuego
		this.agregarEntrada("Pistola 9mm", Globales.GESTOR_TEXTURAS.get(TexturaItem.PISTOLA_INV), false,
				cant -> new Pistola(Pistola.COD_PISTOLA));
		this.agregarEntrada("Escopeta Recortada", Globales.GESTOR_TEXTURAS.get(TexturaItem.ESCOPETA_RECORTADA_INV), false,
				cant -> new EscopetaRecortada());
		this.agregarEntrada("Escopeta Táctica", Globales.GESTOR_TEXTURAS.get(TexturaItem.ESCOPETA_TACTICA_INV), false,
				cant -> new EscopetaTactica());
		this.agregarEntrada("Escopeta Automática", Globales.GESTOR_TEXTURAS.get(TexturaItem.ESCOPETA_AUTOMATICA_INV), false,
				cant -> new EscopetaAutomatica());
		this.agregarEntrada("Subfusil Ligero", Globales.GESTOR_TEXTURAS.get(TexturaItem.SUBFUSIL_LIGERO_INV), false,
				cant -> new SubfusilLigero());
		this.agregarEntrada("Rifle de Asalto", Globales.GESTOR_TEXTURAS.get(TexturaItem.RIFLE_ASALTO_INV), false,
				cant -> new RifleAsalto());
		this.agregarEntrada("Ametralladora Pesada", Globales.GESTOR_TEXTURAS.get(TexturaItem.AMETRALLADORA_PESADA_INV), false,
				cant -> new AmetralladoraPesada());

		// 7. Equipamiento y Joyería
		this.agregarEntrada("Casco Ligero", Globales.GESTOR_TEXTURAS.get(TexturaItem.CASCO_BASE_INV), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_CASCO_BASE, TipoEquipo.CASCO, 0, 0, 3, 5));
		this.agregarEntrada("Armadura Ligera", Globales.GESTOR_TEXTURAS.get(TexturaItem.ARMADURA_BASE_INV), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_ARMADURA_BASE, TipoEquipo.TORSO, 4, 0, 0, 15));
		this.agregarEntrada("Botas de Cuero", Globales.GESTOR_TEXTURAS.get(TexturaItem.BOTAS_CUERO_INV), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_BOTAS_CUERO, TipoEquipo.BOTAS, 0, 6, 0, 3));
		this.agregarEntrada("Anillo de Oro", Globales.GESTOR_TEXTURAS.get(TexturaItem.ANILLO_ORO_INV), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_ANILLO_ORO, TipoEquipo.ANILLO, 2, 2, 2, 0));
		this.agregarEntrada("Anillo de Plata", Globales.GESTOR_TEXTURAS.get(TexturaItem.ANILLO_PLATA_INV), false,
				cant -> new PiezaEquipo(PiezaEquipo.COD_ANILLO_PLATA, TipoEquipo.ANILLO, 1, 1, 1, 0));
	}

	public void agregarEntrada(final String nombre, final BufferedImage icono, final boolean esConsumible,
			final CreadorItem creador) {
		if ((nombre != null) && (icono != null) && (creador != null)) {
			this.ENTRADAS.add(new EntradaItem(nombre, icono, esConsumible, creador));
		}
	}

	@Override
	public void actualizar(final Raton raton) {
		this.ctCantidad.actualizar(raton);

		if ((raton == null) || !raton.presionadoClickIzqUnicaAct()) {
			return;
		}

		final Point pClick = raton.getPuntoPosicionEscalado();

		// Clic en paginación
		if (this.botonPaginaAnterior.contains(pClick)) {
			this.anteriorPagina();
			return;
		}
		if (this.botonPaginaSiguiente.contains(pClick)) {
			this.siguientePagina();
			return;
		}

		// Clic en una casilla de ítem para tomarlo con el cursor (ItemPuntero)
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
		// 1. Cabecera con selector de cantidad
		final int cabY = this.AREA.y - 20;
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, cabY, this.AREA.width, 20, new Color(35, 35, 40));
		Render2D.dibujarRectanguloContorno(g, this.AREA.x, cabY, this.AREA.width, 20, Color.BLACK);

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_LABEL);
		Render2D.dibujarStringConSombra(g, "Cant. Spawn:", this.AREA.x + 4, cabY + 12, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);

		this.ctCantidad.pintar(g);

		// 2. Grilla de slots
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