package principal.entes.estructuras;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import principal.dialogos.MensajeDialogo;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.interaccion.Interactuable;
import principal.mapa.escenario.tps.PuertaMundo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Entidad física e interactuable que representa un edificio del mapa ($64 \times 64\text{ px}$).
 * Administra colisión sólida de muros, umbral transitable de puerta, teletransporte a interiores
 * y protección de la intemperie (Zero-GC / O(1)).
 */
public class Edificio extends Objeto implements Interactuable {

	private static final long serialVersionUID = 1L;

	private final TipoEdificio tipoEdificio;
	private String nombreMundoDestino;
	private String nombreSpawnDestino;
	private boolean bloqueada = false;

	private PuertaMundo puerta;

	// Geometría física Zero-GC
	private final Rectangle muroIzquierdo = new Rectangle();
	private final Rectangle muroDerecho = new Rectangle();
	private final Rectangle muroFondo = new Rectangle();
	private final Rectangle areaPuerta = new Rectangle();

	public Edificio(final int x, final int y, final TipoEdificio tipo, final String mundoDestino,
			final String spawnDestino, final boolean bloqueada) {
		super(x, y);
		this.tipoEdificio = (tipo != null) ? tipo : TipoEdificio.CASA_CAMPO;
		this.nombreMundoDestino = mundoDestino;
		this.nombreSpawnDestino = (spawnDestino != null) ? spawnDestino : "Entrada";
		this.bloqueada = bloqueada;

		if (this.nombreMundoDestino != null && !this.nombreMundoDestino.trim().isEmpty()) {
			this.puerta = new PuertaMundo(this.nombreMundoDestino, this.nombreSpawnDestino);
		}
	}

	public Edificio(final int x, final int y, final TipoEdificio tipo) {
		this(x, y, tipo, tipo != null ? tipo.getMundoInteriorPorDefecto() : null, "Entrada", false);
	}

	public Edificio(final int x, final int y) {
		this(x, y, TipoEdificio.CASA_CAMPO);
	}

	// =========================================================================
	// FÍSICA Y COLISIÓN EN "U" INVERTIDA
	// =========================================================================

	private void recalcularGeometria() {
		final int px = this.getPosicionXInt();
		final int py = this.getPosicionYInt();

		// Muro de fondo (pared trasera superior)
		this.muroFondo.setBounds(px + 4, py + 34, 56, 8);

		// Muros laterales
		this.muroIzquierdo.setBounds(px + 4, py + 42, 20, 18);
		this.muroDerecho.setBounds(px + 40, py + 42, 20, 18);

		// Umbral de la puerta (transitable e interactivo)
		this.areaPuerta.setBounds(px + 24, py + 46, 16, 16);
	}

	@Override
	public boolean intersecta(final Shape s) {
		this.recalcularGeometria();
		return s.intersects(this.muroFondo) || s.intersects(this.muroIzquierdo) || s.intersects(this.muroDerecho);
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt() + 4, this.getPosicionYInt() + 34, 56, 28);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	// =========================================================================
	// CONTRATO INTERACTUABLE [E]
	// =========================================================================

	@Override
	public String getTextoPrompt() {
		if (this.bloqueada) {
			return "Puerta Cerrada con Llave";
		}
		if (this.puerta != null) {
			return "Entrar a " + this.tipoEdificio.getNombre();
		}
		return "Examinar " + this.tipoEdificio.getNombre();
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		if (jugador == null || this.estaEliminado()) {
			return false;
		}
		this.recalcularGeometria();
		// Permite interacción si el jugador se para en el umbral de la puerta o frente a ella
		return jugador.getArea().intersects(this.areaPuerta) || this.getArea().intersects(jugador.getArea());
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		if (this.bloqueada) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			Globales.GESTOR_TEXTOS.agregarTexto("¡Cerrado con llave!", this.getCentroX(), this.getPosicionYInt() + 40,
					principal.igu.textos.TipoTextoFlotante.BLOQUEO);
			return;
		}

		// Si tiene asignado un interior, teletransporta al jugador y a su séquito
		if (this.puerta != null) {
			GestorSonido.reproducir(IDSonido.SELECT);
			this.puerta.teletransportar(jugador);
			return;
		}

		// Si es un edificio estético sin submundo asignado, muestra diálogo de inmersión RPG
		final MensajeDialogo dialogo = new MensajeDialogo(this.tipoEdificio.getNombre(), new Color(240, 200, 120),
				this.tipoEdificio.getDescripcionLore(), null);
		Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogo);
	}

	// =========================================================================
	// RENDERIZADO
	// =========================================================================

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage img = this.getTextura();
		if (img != null) {
			Render2D.dibujarImagenRefCamara(g, img, this.getPosicionXInt(), this.getPosicionYInt());
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			this.recalcularGeometria();
			Render2D.dibujarRectanguloContornoRefCamara(g, this.muroFondo, Color.ORANGE);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.muroIzquierdo, Color.ORANGE);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.muroDerecho, Color.ORANGE);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.areaPuerta, Color.CYAN);
		}
	}

	@Override
	public BufferedImage getTextura() {
		return this.tipoEdificio.getTextura();
	}

	@Override
	public int getAncho() {
		return TipoEdificio.LADO;
	}

	@Override
	public int getAlto() {
		return TipoEdificio.LADO;
	}

	@Override
	public Objeto copiar() {
		return new Edificio(this.getPosicionXInt(), this.getPosicionYInt(), this.tipoEdificio,
				this.nombreMundoDestino, this.nombreSpawnDestino, this.bloqueada);
	}

	public TipoEdificio getTipoEdificio() {
		return this.tipoEdificio;
	}

	public String getNombreMundoDestino() {
		return this.nombreMundoDestino;
	}

	public void setNombreMundoDestino(final String mundoDestino) {
		this.nombreMundoDestino = mundoDestino;
		this.puerta = (mundoDestino != null && !mundoDestino.trim().isEmpty())
				? new PuertaMundo(mundoDestino, this.nombreSpawnDestino)
				: null;
	}

	public String getNombreSpawnDestino() {
		return this.nombreSpawnDestino;
	}

	public void setNombreSpawnDestino(final String spawnDestino) {
		this.nombreSpawnDestino = (spawnDestino != null) ? spawnDestino : "Entrada";
		if (this.puerta != null) {
			this.puerta = new PuertaMundo(this.nombreMundoDestino, this.nombreSpawnDestino);
		}
	}

	public boolean isBloqueada() {
		return this.bloqueada;
	}

	public void setBloqueada(final boolean bloqueada) {
		this.bloqueada = bloqueada;
	}
}