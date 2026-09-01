package principal.construccion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.controles.Raton;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.Textura;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class GestorConstruccion {

	private boolean activo = false;
	private TipoEstructura estructuraSeleccionada = TipoEstructura.MURO_MADERA;

	private final Rectangle areaPreview = new Rectangle(0, 0, 16, 16);
	private boolean posicionValida = false;
	private boolean tieneMateriales = false;

	private static final double RANGO_MAXIMO_CONSTRUCCION = 96.0;
	private final GestorTiempo GT_COLOCACION = new GestorTiempo();
	private static final int COOLDOWN_COLOCACION_MS = 200;

	public GestorConstruccion() {
	}

	public void actualizar(final Raton raton, final Mundo mundo) {
		if (!this.activo || (raton == null) || (mundo == null) || (this.estructuraSeleccionada == null)) {
			return;
		}

		// 1. Proyección de ratón a coordenadas continuas y Snapping a la grilla de 16x16
		final Point pMouse = raton.getPuntoPosicionEscaladoConDesplazamientoCamara();
		final int snapX = Math.floorDiv(pMouse.x, Constantes.LADO_TILE) * Constantes.LADO_TILE;
		final int snapY = Math.floorDiv(pMouse.y, Constantes.LADO_TILE) * Constantes.LADO_TILE;

		this.areaPreview.setBounds(snapX, snapY, this.estructuraSeleccionada.getAncho(),
				this.estructuraSeleccionada.getAlto());

		// 2. Validación de Rango respecto al Jugador
		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();
		final double dist = Math.hypot(pMouse.x - jx, pMouse.y - jy);
		final boolean dentroDeRango = dist <= RANGO_MAXIMO_CONSTRUCCION;

		// 3. Validación de Colisiones (Terreno, Objetos y Criaturas)
		final boolean libreDeColision = !mundo.colisionaConZonaUObjetoSolido(this.areaPreview)
				&& !mundo.intersectaAlgunaCriatura(this.areaPreview, true);

		// 4. Validación de Materiales en Inventario
		final int materialDisponible = Globales.GESTOR_INVENTARIO.getInventarioJugador()
				.contarMunicionTotal(this.estructuraSeleccionada.getCodMaterialRequerido());
		this.tieneMateriales = materialDisponible >= this.estructuraSeleccionada.getCantidadMaterialRequerido();

		this.posicionValida = dentroDeRango && libreDeColision && this.tieneMateriales;

		// 5. Colocación al hacer Clic Izquierdo
		if (this.posicionValida && raton.presionadoClickIzq()
				&& this.GT_COLOCACION.transcurrioMiliSegundos(COOLDOWN_COLOCACION_MS)) {

			this.GT_COLOCACION.establecerReferenciaTiempoActual();

			// Descuenta materiales
			Globales.GESTOR_INVENTARIO.getInventarioJugador().extraerMunicion(
					this.estructuraSeleccionada.getCodMaterialRequerido(),
					this.estructuraSeleccionada.getCantidadMaterialRequerido());

			// Instancia la estructura en el mundo
			final EstructuraConstruible nuevaEstructura = new EstructuraConstruible(snapX, snapY,
					this.estructuraSeleccionada);
			mundo.meterEntidad(nuevaEstructura);

			// Actualiza el grafo de navegación de los enemigos
			mundo.forzarActDijkstra();

			GestorSonido.reproducirEnPosicion(IDSonido.GOLPE_1, snapX, snapY,
					Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
					Globales.CAMARA.getEntidadEnfocada().getPosicionY());
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.activo || (this.estructuraSeleccionada == null)) {
			return;
		}

		final int x = this.areaPreview.x;
		final int y = this.areaPreview.y;
		final int w = this.areaPreview.width;
		final int h = this.areaPreview.height;

		// 1. Textura translúcida de previsualización
		Render2D.dibujarImagenConTransparenciaRefCamara(g,
				Textura.getTextura(this.estructuraSeleccionada.getCodTextura()), x, y, 0.60f);

		// 2. Borde de validación (Verde = Válido / Rojo = Bloqueado)
		final Color colorBorde = this.posicionValida ? new Color(60, 255, 60, 200) : new Color(255, 60, 60, 200);
		Render2D.dibujarRectanguloContornoRefCamara(g, x, y, w, h, colorBorde);
	}

	public boolean isActivo() {
		return this.activo;
	}

	public void setActivo(final boolean activo) {
		this.activo = activo;
	}

	public void conmutarModoConstruccion() {
		this.activo = !this.activo;
	}

	public TipoEstructura getEstructuraSeleccionada() {
		return this.estructuraSeleccionada;
	}

	public void setEstructuraSeleccionada(final TipoEstructura tipo) {
		if (tipo != null) {
			this.estructuraSeleccionada = tipo;
		}
	}
}