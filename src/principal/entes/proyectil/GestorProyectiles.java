package principal.entes.proyectil;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import principal.entes.Ente;
import principal.mapa.Mundo;

/**
 * Gestor maestro y Pool de alto rendimiento para proyectiles (Zero-GC / O(1)).
 * Utiliza compactación Swap-and-Pop y reciclado de instancias en caliente.
 * 
 * @version 1.1 (Vanilla Java 8 - Zero-GC)
 */
public class GestorProyectiles {

	private static final int MAX_PROYECTILES = 256;
	private static final int POOL_BALAS = 128;
	private static final int POOL_PERDIGONES = 128;

	private final Proyectil[] activos = new Proyectil[MAX_PROYECTILES];
	private int cantidadActivos = 0;

	private final ProyectilBala[] poolBalas = new ProyectilBala[POOL_BALAS];
	private int topeBalas = 0;

	private final ProyectilPerdigon[] poolPerdigones = new ProyectilPerdigon[POOL_PERDIGONES];
	private int topePerdigones = 0;

	public GestorProyectiles() {
		for (int i = 0; i < POOL_BALAS; i++) {
			this.poolBalas[i] = new ProyectilBala(0, 0, false, 0, null, 0, 0, 0, 0, 0, 0, null);
		}
		this.topeBalas = POOL_BALAS;

		for (int i = 0; i < POOL_PERDIGONES; i++) {
			this.poolPerdigones[i] = new ProyectilPerdigon(0, 0, false, 0, null, 0, 0, 0, 0, null);
		}
		this.topePerdigones = POOL_PERDIGONES;
	}

	// =========================================================================
	// === DISPARO DIRECTO DESDE POOL (ZERO-ALLOCATION)
	// =========================================================================

	public void dispararBala(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino,
			final double yDestino, final int ancho, final int alto, final Ente causante) {

		if (this.cantidadActivos >= MAX_PROYECTILES) {
			return;
		}

		ProyectilBala p;
		if (this.topeBalas > 0) {
			p = this.poolBalas[--this.topeBalas];
		} else {
			p = new ProyectilBala(0, 0, false, 0, null, 0, 0, 0, 0, 0, 0, null);
		}

		p.reiniciar(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, ancho, alto,
				causante);
		this.activos[this.cantidadActivos++] = p;
	}

	public void dispararPerdigon(final double damage, final double velocidad, final boolean penetrante,
			final double alcance, final Mundo mundo, final double xOrigen, final double yOrigen, final double xDestino,
			final double yDestino, final Ente causante) {

		if (this.cantidadActivos >= MAX_PROYECTILES) {
			return;
		}

		ProyectilPerdigon p;
		if (this.topePerdigones > 0) {
			p = this.poolPerdigones[--this.topePerdigones];
		} else {
			p = new ProyectilPerdigon(0, 0, false, 0, null, 0, 0, 0, 0, null);
		}

		p.reiniciar(damage, velocidad, penetrante, alcance, mundo, xOrigen, yOrigen, xDestino, yDestino, 2, 2,
				causante);
		this.activos[this.cantidadActivos++] = p;
	}

	public void agregarProyectil(final Proyectil p) {
		if ((p != null) && (this.cantidadActivos < MAX_PROYECTILES)) {
			this.activos[this.cantidadActivos++] = p;
		}
	}

	// =========================================================================
	// === CICLO LÓGICO Y RENDERIZADO (SWAP-AND-POP O(1))
	// =========================================================================

	public void actualizar() {
		int i = 0;
		while (i < this.cantidadActivos) {
			final Proyectil p = this.activos[i];
			p.actualizar();

			if (p.estaEliminado()) {
				if (p instanceof ProyectilPerdigon) {
					if (this.topePerdigones < POOL_PERDIGONES) {
						this.poolPerdigones[this.topePerdigones++] = (ProyectilPerdigon) p;
					}
				} else if (p instanceof ProyectilBala) {
					if (this.topeBalas < POOL_BALAS) {
						this.poolBalas[this.topeBalas++] = (ProyectilBala) p;
					}
				}

				// Swap-and-Pop O(1)
				this.activos[i] = this.activos[this.cantidadActivos - 1];
				this.activos[this.cantidadActivos - 1] = null;
				this.cantidadActivos--;
			} else {
				i++;
			}
		}
	}

	public void pintar(final Graphics2D g) {
		for (int i = 0; i < this.cantidadActivos; i++) {
			this.activos[i].pintar(g);
		}
	}

	public void agregarIntersecciones(final Rectangle area, final ArrayList<Ente> listaDestino) {
		if ((area == null) || (listaDestino == null)) {
			return;
		}
		for (int i = 0; i < this.cantidadActivos; i++) {
			final Proyectil p = this.activos[i];
			if (area.intersects(p.getArea())) {
				listaDestino.add(p);
			}
		}
	}

	public void limpiar() {
		for (int i = 0; i < this.cantidadActivos; i++) {
			final Proyectil p = this.activos[i];
			if (p instanceof ProyectilPerdigon) {
				if (this.topePerdigones < POOL_PERDIGONES) {
					this.poolPerdigones[this.topePerdigones++] = (ProyectilPerdigon) p;
				}
			} else if (p instanceof ProyectilBala) {
				if (this.topeBalas < POOL_BALAS) {
					this.poolBalas[this.topeBalas++] = (ProyectilBala) p;
				}
			}
			this.activos[i] = null;
		}
		this.cantidadActivos = 0;
	}

	public int getCantidadActivos() {
		return this.cantidadActivos;
	}
}