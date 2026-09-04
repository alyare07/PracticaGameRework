package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.controles.Raton;
import principal.mapa.Mundo;
import principal.mapa.Tile;
import principal.maquinaestado.estados.menu.herramientas.CajaTextoPixel;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class PaletaSpawns extends Paleta {

	public static class EntradaSpawn {
		public final String nombrePreset;
		public final BufferedImage icono;
		public final Color colorDistintivo;

		public EntradaSpawn(final String nombrePreset, final BufferedImage icono, final Color colorDistintivo) {
			this.nombrePreset = nombrePreset;
			this.icono = icono;
			this.colorDistintivo = colorDistintivo;
		}
	}

	private final ArrayList<EntradaSpawn> PRESETS = new ArrayList<EntradaSpawn>();
	private final CajaTextoPixel ctNombreSpawn;

	private static final Font FUENTE_LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 6);
	private static final Font FUENTE_BADGE = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public PaletaSpawns(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y + 20, ancho, alto - 20, ladoSlot);

		// Campo de texto interactivo para nombrar el Spawn antes de colocarlo
		this.ctNombreSpawn = new CajaTextoPixel(new Rectangle(x + ancho - 75, y + 2, 70, 16),
				Mundo.CLAVE_PUNTO_SPAWN_COMIENZO, 18, false);

		this.inicializarPresets();
	}

	private void inicializarPresets() {
		// 1. Spawn Comienzo (Principal del mapa)
		this.PRESETS.add(new EntradaSpawn(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO,
				this.crearIconoSpawn(new Color(255, 215, 0), "C"), new Color(255, 215, 0)));

		// 2. Spawn Entrada
		this.PRESETS.add(new EntradaSpawn("Entrada", this.crearIconoSpawn(new Color(60, 240, 80), "IN"),
				new Color(60, 240, 80)));

		// 3. Spawn Salida
		this.PRESETS.add(new EntradaSpawn("Salida", this.crearIconoSpawn(new Color(255, 60, 60), "OUT"),
				new Color(255, 60, 60)));

		// 4. Spawn Puerta
		this.PRESETS.add(new EntradaSpawn("Puerta_1", this.crearIconoSpawn(new Color(70, 180, 255), "P"),
				new Color(70, 180, 255)));

		// 5. Spawn Cueva / Subterráneo
		this.PRESETS.add(new EntradaSpawn("Cueva", this.crearIconoSpawn(new Color(200, 100, 255), "CV"),
				new Color(200, 100, 255)));

		// 6. Spawn Personalizado
		this.PRESETS.add(new EntradaSpawn("Punto_A", this.crearIconoSpawn(new Color(255, 140, 40), "*"),
				new Color(255, 140, 40)));
	}

	private BufferedImage crearIconoSpawn(final Color colorBase, final String texto) {
		final int lado = this.LADO_SLOT;
		final BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();

		// Fondo de ranura
		g.setColor(new Color(25, 28, 35));
		g.fillRect(0, 0, lado, lado);

		// Marco distintivo
		g.setColor(colorBase);
		g.drawRect(2, 2, lado - 5, lado - 5);

		// Letra / Sigla centrada
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 8f));
		final int ancho = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int alto = Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
		final int tx = (lado - ancho) / 2;
		final int ty = (lado + (alto / 2)) / 2;

		Render2D.dibujarStringConSombra(g, texto, tx, ty, colorBase, Color.BLACK);
		g.dispose();
		return img;
	}

	@Override
	public void actualizar(final Raton raton) {
		this.ctNombreSpawn.actualizar(raton);

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
				if (indiceGlobal < this.PRESETS.size()) {
					this.indiceSeleccionado = indiceGlobal;
					final EntradaSpawn entrada = this.PRESETS.get(indiceGlobal);

					// Copia el nombre del preset a la caja de texto para editarlo o confirmarlo
					this.ctNombreSpawn.setTexto(entrada.nombrePreset);
					GestorSonido.reproducir(IDSonido.GOLPE_1);
				}
			}
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		// 1. Cabecera con selector de nombre
		final int cabY = this.AREA.y - 20;
		Render2D.dibujarRectanguloRelleno(g, this.AREA.x, cabY, this.AREA.width, 20, new Color(35, 35, 40));
		Render2D.dibujarRectanguloContorno(g, this.AREA.x, cabY, this.AREA.width, 20, Color.BLACK);

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_LABEL);
		Render2D.dibujarStringConSombra(g, "Nombre Spawn:", this.AREA.x + 4, cabY + 12, Color.WHITE, Color.BLACK);
		g.setFont(fontPrevia);

		this.ctNombreSpawn.pintar(g);

		// 2. Grilla de slots
		super.pintar(g);
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final EntradaSpawn entrada = this.PRESETS.get(index);
		if (entrada.icono != null) {
			this.dibujarIconoAjustadoAlSlot(g, entrada.icono, slotX, slotY);
		}

		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_BADGE);
		Render2D.dibujarRectanguloRelleno(g, slotX + 1, slotY + 1, 6, 6, Color.BLACK);
		Render2D.dibujarString(g, "S", slotX + 2, slotY + 6, entrada.colorDistintivo);
		g.setFont(fontPrevia);
	}

	public String getNombreSpawnSeleccionado() {
		final String texto = this.ctNombreSpawn.getTexto().trim();
		return !texto.isEmpty() ? texto : Mundo.CLAVE_PUNTO_SPAWN_COMIENZO;
	}

	public EntradaSpawn getEntradaSeleccionada() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.PRESETS.size())) {
			return this.PRESETS.get(this.indiceSeleccionado);
		}
		return null;
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.PRESETS.size();
	}

	@Override
	public String getNombreElemento(final int index) {
		return ((index >= 0) && (index < this.PRESETS.size())) ? this.PRESETS.get(index).nombrePreset : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		return false;
	}
}