package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import org.json.simple.JSONObject;

import principal.animaciones.Animacion;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.modelos.item.ModeloGranada;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.arrojadizos.granadas.Granada;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Proyectil balístico parabólico de área (Bézier). Gestiona el arco de
 * elevación, daño radial por área y animación de explosión.
 * 
 * @version 2.5 (Java 8 Compatible - Zero-GC Architecture)
 */
public class ProyectilGranada extends ProyectilGeneral {

	private static final long serialVersionUID = 9083899449947637545L;
	private static final int TIEMPO_MS_FRAMES_EXPLOSION = 35;

	protected final Ellipse2D.Double AREA_DESTINO;
	protected boolean realizoImpacto;
	protected int[][] trayectoria; // [0] = X, [1] = Y
	private int posTrayectoria;

	private final Granada GRANADA;
	private final Animacion ANIMACION_EXPLOSION;

	public ProyectilGranada(final int xDestino, final int yDestino, final Mundo mundo, final Ente causante,
			final Granada granada, final boolean soloContraJugador) {
		super(granada.getDamage(), 1.5, false, 0, mundo, (causante != null) ? causante.getCentroX() : xDestino,
				(causante != null) ? causante.getCentroY() : yDestino, xDestino, yDestino, 10, 10, causante,
				soloContraJugador);

		this.GRANADA = granada;
		this.AREA_DESTINO = new Ellipse2D.Double(xDestino - (granada.getDiamentroAreaCaida() / 2.0),
				yDestino - (granada.getDiamentroAreaCaida() / 2.0), granada.getDiamentroAreaCaida(),
				granada.getDiamentroAreaCaida());

		final ModeloGranada modelo = (ModeloGranada) ListaModelosItem
				.getModeloConsumible(this.GRANADA.getCodigoModelo());
		this.ANIMACION_EXPLOSION = new Animacion(new HojaSprite(modelo.getTexturaExplosion(), 50, false), false,
				TIEMPO_MS_FRAMES_EXPLOSION);

		this.generarTrayectoria();
	}

	public ProyectilGranada(final double damage, final Ellipse2D.Double areaDestino, final Mundo mundo,
			final int xOrigen, final int yOrigen, final String codModelo, final boolean soloContraJugador) {
		super(damage, 1.0, false, 0, mundo, xOrigen, yOrigen, (int) areaDestino.getCenterX(),
				(int) areaDestino.getCenterY(), 10, 10, null, soloContraJugador);

		this.GRANADA = new Granada(1, (int) areaDestino.width, damage, codModelo) {
			private static final long serialVersionUID = -6508234289982231948L;

			@Override
			public Objeto copiar() {
				return null;
			}

			@Override
			protected JSONObject exportarParaJSON() {
				return null;
			}
		};

		this.AREA_DESTINO = areaDestino;
		final ModeloGranada modelo = (ModeloGranada) ListaModelosItem
				.getModeloConsumible(this.GRANADA.getCodigoModelo());
		this.ANIMACION_EXPLOSION = new Animacion(new HojaSprite(modelo.getTexturaExplosion(), 50, false), false,
				TIEMPO_MS_FRAMES_EXPLOSION);

		this.generarTrayectoria();
	}

	@Override
	public void actualizar() {
		if (!this.eliminado) {
			this.mover();
			this.verificarImpacto();
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.realizoImpacto) {
			this.ANIMACION_EXPLOSION.pintar(g, this.AREA_DESTINO.getX(), this.AREA_DESTINO.getY() - 16, true);
		} else {
			Render2D.dibujarImagenRefCamara(g, Textura.getTextura(Textura.TEXTURA_X10_GRANADA_1), (int) this.x,
					(int) this.y);
			Render2D.dibujarFiguraEllipseRefCamara(g, this.AREA_DESTINO.getBounds(), Color.CYAN);
			super.pintar(g);
		}
	}

	@Override
	protected void mover() {
		if (this.realizoImpacto || (this.trayectoria == null)) {
			return;
		}

		if (this.posTrayectoria < this.trayectoria[0].length) {
			this.x = this.trayectoria[0][this.posTrayectoria];
			this.y = this.trayectoria[1][this.posTrayectoria];
			this.posTrayectoria++;
		} else {
			// Llegó al final de la parábola
			this.x = this.AREA_DESTINO.getCenterX();
			this.y = this.AREA_DESTINO.getCenterY();
		}
	}

	@Override
	protected void verificarImpacto() {
		// El impacto se produce al completarse el recorrido parabólico
		if (!this.realizoImpacto && (this.trayectoria != null) && (this.posTrayectoria >= this.trayectoria[0].length)) {
			if (!this.SOLO_CONTRA_JUGADOR) {
				for (final Criatura c : this.mundo.getCriaturasIntersectadas(this.AREA_DESTINO,
						!(this.CAUSANTE instanceof Jugador))) {
					this.impactar(c);
				}
			} else if (Globales.JUGADOR != this.CAUSANTE) {
				if (this.AREA_DESTINO.intersects(Globales.JUGADOR.getArea())) {
					this.impactar(Globales.JUGADOR);
					if (!this.PENETRANTE) {
						this.eliminar();
						return;
					}
				}
			}

			if ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null)) {
				GestorSonido.reproducirEnPosicion(IDSonido.EXPLOSION_1, this.AREA_DESTINO.getCenterX(),
						this.AREA_DESTINO.getCenterY(), Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
						Globales.CAMARA.getEntidadEnfocada().getPosicionY());
			}

			this.realizoImpacto = true;
		}

		if (this.realizoImpacto && !this.eliminado && this.ANIMACION_EXPLOSION.animacionFinalizada()) {
			this.eliminar();
		}
	}

	@Override
	protected void impactar(final Criatura c) {
		if ((c == null) || this.perforados.contains(c)) {
			return;
		}
		this.perforados.add(c);
		c.recibirAtaque(this.DAMAGE, this.CAUSANTE);
	}

	private void generarTrayectoria() {
		final int origenX = (int) Math.round(this.x);
		final int origenY = (int) Math.round(this.y);
		final int destinoX = (int) Math.round(this.AREA_DESTINO.getCenterX());
		final int destinoY = (int) Math.round(this.AREA_DESTINO.getCenterY());

		this.trayectoria = Globales.FUNCIONES.GENERADOR_TRAYECTORIAS.getTrayectoiaBezier(origenX, origenY, destinoX,
				destinoY, this.GRANADA.getTiempoMsCaidaEnAnchoPantalla());
		this.posTrayectoria = 0;
	}
}