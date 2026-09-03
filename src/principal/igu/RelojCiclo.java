package principal.igu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import principal.iluminacion.CicloDiaNoche;
import principal.recursos.ClaveHoja;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

/**
 * Componente visual del HUD que renderiza el dial astrológico rotatorio de 24
 * horas libre de vibración tipográfica (Zero-GC / O(1)).
 * 
 * @version 1.1 (Vanilla Java 8 - Anti-Jitter Matrix Restore)
 */
public class RelojCiclo {

	private static final int ANCHO_MARCO = 44;
	private static final int ALTO_MARCO = 44;
	private static final int RADIO_DISCO = 20;

	private final Rectangle areaMarco;
	private final int centroX;
	private final int centroY;

	private final BufferedImage imgDisco;
	private final BufferedImage imgMarco;

	// Caché de texto (Zero-GC)
	private int lastMinuto = -1;
	private int lastDia = -1;
	private String cachedHora = "";
	private String cachedDia = "";

	public RelojCiclo() {
		final int posX = Constantes.ANCHO_JUEGO - ANCHO_MARCO - 6;
		final int posY = 6;

		this.areaMarco = new Rectangle(posX, posY, ANCHO_MARCO, ALTO_MARCO);
		this.centroX = posX + (ANCHO_MARCO / 2);
		this.centroY = posY + (ALTO_MARCO / 2);

		final HojaSprite hojaDisco = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.IGU_DISCO_CICLO_TIME);
		final HojaSprite hojaMarco = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.IGU_MARCO_TIME);

		this.imgDisco = (hojaDisco != null) ? hojaDisco.getSprite(0) : Globales.GESTOR_TEXTURAS.getTexturaError();
		this.imgMarco = (hojaMarco != null) ? hojaMarco.getSprite(0) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	public void actualizar() {
		if ((Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			return;
		}

		final CicloDiaNoche ciclo = Globales.GESTOR_LUZ.getCiclo();
		final int minutoActual = (int) Math.round(ciclo.getHoraActual() * 60.0);
		final int diaActual = ciclo.getDiaActual();

		if ((minutoActual != this.lastMinuto) || (diaActual != this.lastDia)) {
			this.lastMinuto = minutoActual;
			this.lastDia = diaActual;
			this.cachedHora = ciclo.getHoraFormato24h();
			this.cachedDia = ciclo.getTextoDia();
		}
	}

	public void pintar(final Graphics2D g) {
		if ((Globales.GESTOR_LUZ == null) || (Globales.GESTOR_LUZ.getCiclo() == null)) {
			return;
		}

		final double horaActual = Globales.GESTOR_LUZ.getCiclo().getHoraActual();
		final double anguloRadianes = -((horaActual / 24.0) * (Math.PI * 2.0));

		// 1. Dibujado del Disco Rotatorio aislando la matriz para evitar contaminación
		// de sub-píxel
		final AffineTransform transformOriginal = g.getTransform();
		try {
			g.translate(this.centroX, this.centroY);
			g.rotate(anguloRadianes);
			Render2D.dibujarImagen(g, this.imgDisco, -RADIO_DISCO, -RADIO_DISCO);
		} finally {
			// Restaura la matriz 100% limpia para que las letras no vibren
			g.setTransform(transformOriginal);
		}

		// 2. Dibujado del Marco Fijo Ornamental con Marcador
		Render2D.dibujarImagen(g, this.imgMarco, this.areaMarco.x, this.areaMarco.y);

		// 3. Texto descriptivo nítido y estático a la izquierda del reloj
		this.pintarInformacionTexto(g);
	}

	private void pintarInformacionTexto(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 10f));

		final int xTexto = this.areaMarco.x - 6;

		// Línea 1: [Día X]
		final int anchoDia = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedDia);
		Render2D.dibujarStringConSombra(g, this.cachedDia, xTexto - anchoDia, this.areaMarco.y + 16, Color.WHITE,
				Color.BLACK, 10f, true);

		// Línea 2: [HH:MM]
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Font.BOLD, 12f));
		final int anchoHora = Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, this.cachedHora);
		Render2D.dibujarStringConSombra(g, this.cachedHora, xTexto - anchoHora, this.areaMarco.y + 30,
				new Color(255, 215, 90), Color.BLACK, 12f, true);

		g.setFont(fontPrevia);
	}

	public Rectangle getArea() {
		return this.areaMarco;
	}
}