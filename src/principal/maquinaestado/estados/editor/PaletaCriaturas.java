package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.enemigos.bandido.BandidoGarrote;
import principal.entes.criaturas.enemigos.bandido.BandidoGranadero;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.mapa.Tile;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class PaletaCriaturas extends Paleta {

	@FunctionalInterface
	public interface CreadorCriatura {
		Criatura crear(int x, int y);
	}

	public static class EntradaCriatura {
		public final String nombre;
		public final BufferedImage icono;
		public final CreadorCriatura creador;

		public EntradaCriatura(final String nombre, final BufferedImage icono, final CreadorCriatura creador) {
			this.nombre = nombre;
			this.icono = icono;
			this.creador = creador;
		}
	}

	private final ArrayList<EntradaCriatura> ENTRADAS = new ArrayList<EntradaCriatura>();
	private static final Font FUENTE_BADGE = new Font(Font.SANS_SERIF, Font.BOLD, 5);

	public PaletaCriaturas(final int x, final int y, final int ancho, final int alto, final int ladoSlot) {
		super(x, y, ancho, alto, ladoSlot);
		this.cargarCriaturas();
	}

	private void cargarCriaturas() {
		// 1. Bandido Pistolero
		final BufferedImage iconPistolero = Globales.LISTA_HOJAS_SPRITES.BANDIDO.PISTOLA_ESTANDAR_ABAJO.getSprite(0);
		this.agregarEntrada("Bandido Pistolero", iconPistolero,
				(x, y) -> new BandidoPistolero(x, y, 50, 50, null));

		// 2. Bandido Garrote
		final BufferedImage iconGarrote = Globales.LISTA_HOJAS_SPRITES.BANDIDO.GARROTE_ESTANDAR_ABAJO.getSprite(0);
		this.agregarEntrada("Bandido Garrote", iconGarrote,
				(x, y) -> new BandidoGarrote(x, y, 50, 50, null));

		// 3. Bandido Granadero
		final BufferedImage iconGranadero = Globales.LISTA_HOJAS_SPRITES.BANDIDO.ESTANDAR_ABAJO.getSprite(0);
		this.agregarEntrada("Bandido Granadero", iconGranadero,
				(x, y) -> new BandidoGranadero(x, y, 50, 50, null));
	}

	public void agregarEntrada(final String nombre, final BufferedImage icono, final CreadorCriatura creador) {
		if ((nombre != null) && (icono != null) && (creador != null)) {
			this.ENTRADAS.add(new EntradaCriatura(nombre, icono, creador));
		}
	}

	@Override
	public int getCantidadTotalElementos() {
		return this.ENTRADAS.size();
	}

	@Override
	protected void pintarElementoEnSlot(final Graphics2D g, final int index, final int slotX, final int slotY) {
		final EntradaCriatura entrada = this.ENTRADAS.get(index);
		if (entrada.icono != null) {
			Render2D.dibujarImagen(g, entrada.icono, slotX, slotY);
		}

		// Insignia [C] Roja para identificar criaturas / entidades vivas
		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_BADGE);
		Render2D.dibujarRectanguloRelleno(g, slotX + 1, slotY + 1, 6, 6, Color.BLACK);
		Render2D.dibujarString(g, "C", slotX + 2, slotY + 6, new Color(255, 60, 60));
		g.setFont(fontPrevia);
	}

	public Criatura crearCriaturaSeleccionada(final int x, final int y) {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.ENTRADAS.size())) {
			return this.ENTRADAS.get(this.indiceSeleccionado).creador.crear(x, y);
		}
		return null;
	}

	public EntradaCriatura getEntradaSeleccionada() {
		if ((this.indiceSeleccionado >= 0) && (this.indiceSeleccionado < this.ENTRADAS.size())) {
			return this.ENTRADAS.get(this.indiceSeleccionado);
		}
		return null;
	}

	@Override
	public String getNombreElemento(final int index) {
		return (index >= 0) && (index < this.ENTRADAS.size()) ? this.ENTRADAS.get(index).nombre : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		return false;
	}
}