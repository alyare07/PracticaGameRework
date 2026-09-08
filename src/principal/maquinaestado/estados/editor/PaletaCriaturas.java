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
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

public class PaletaCriaturas extends Paleta {

	@FunctionalInterface
	public interface CreadorCriatura {
		Criatura crear(int x, int y);
	}

	public static class EntradaCriatura {
		public final String nombre;
		public final BufferedImage icono;
		public final int margenX;
		public final int margenY;
		public final int anchoHitbox;
		public final int altoHitbox;
		public final CreadorCriatura creador;

		public EntradaCriatura(final String nombre, final BufferedImage icono, final int margenX, final int margenY,
				final int anchoHitbox, final int altoHitbox, final CreadorCriatura creador) {
			this.nombre = nombre;
			this.icono = icono;
			this.margenX = margenX;
			this.margenY = margenY;
			this.anchoHitbox = anchoHitbox;
			this.altoHitbox = altoHitbox;
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
		final HojaSprite hojaBandido = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.BANDIDO);

		final BufferedImage iconPistolero = (hojaBandido != null) ? hojaBandido.getSprite(24) : null;
		this.agregarEntrada("Bandido Pistolero", iconPistolero, 10, 6, 12, 20,
				(x, y) -> new BandidoPistolero(x, y, 50, 50, null));

		final BufferedImage iconGarrote = (hojaBandido != null) ? hojaBandido.getSprite(48) : null;
		this.agregarEntrada("Bandido Garrote", iconGarrote, 10, 6, 12, 20,
				(x, y) -> new BandidoGarrote(x, y, 50, 50, null));

		final BufferedImage iconGranadero = (hojaBandido != null) ? hojaBandido.getSprite(0) : null;
		this.agregarEntrada("Bandido Granadero", iconGranadero, 10, 6, 12, 20,
				(x, y) -> new BandidoGranadero(x, y, 50, 50, null));
	}

	public void agregarEntrada(final String nombre, final BufferedImage icono, final int margenX, final int margenY,
			final int anchoHitbox, final int altoHitbox, final CreadorCriatura creador) {
		if ((nombre != null) && (icono != null) && (creador != null)) {
			this.ENTRADAS.add(new EntradaCriatura(nombre, icono, margenX, margenY, anchoHitbox, altoHitbox, creador));
		}
	}

	public boolean seleccionarPorNombre(final String nombre) {
		if ((nombre == null) || nombre.isEmpty()) {
			return false;
		}
		final String buscado = normalizar(nombre);
		for (int i = 0; i < this.ENTRADAS.size(); i++) {
			final String entradaNorm = normalizar(this.ENTRADAS.get(i).nombre);
			if (entradaNorm.equals(buscado) || entradaNorm.contains(buscado) || buscado.contains(entradaNorm)) {
				this.indiceSeleccionado = i;
				this.paginaActual = i / this.ELEMENTOS_POR_PAGINA;
				return true;
			}
		}
		return false;
	}

	private static String normalizar(final String s) {
		if (s == null) {
			return "";
		}
		return s.toLowerCase().replace(" ", "").replace("_", "").replace("á", "a").replace("é", "e").replace("í", "i")
				.replace("ó", "o").replace("ú", "u").replace("ñ", "n");
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
		return ((index >= 0) && (index < this.ENTRADAS.size())) ? this.ENTRADAS.get(index).nombre : "";
	}

	@Override
	public boolean valoresYaEstablecidosPreviamente(final Tile tileEvaluar) {
		return false;
	}
}