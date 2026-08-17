package principal.utilidades.inventario;

import java.awt.Graphics2D;
import java.awt.Point;

import principal.controles.Raton;
import principal.inventario.Inventario;
import principal.inventario.vault.InventarioVault;
import principal.mapa.Mundo;

public final class GestorInventario {

	private final Inventario INVENTARIO_JUGADOR;
	private InventarioVault inventarioTercero;
	private final ItemPuntero ITEM_PUNTERO;

	public GestorInventario() {
		this.INVENTARIO_JUGADOR = new Inventario();
		this.ITEM_PUNTERO = new ItemPuntero();
	}

	public void actualizar(final Raton raton, final Mundo mundo) {
		if (raton == null) {
			return;
		}

		// 1. Actualizar lógica del inventario del jugador
		this.INVENTARIO_JUGADOR.actualizar(raton, this.ITEM_PUNTERO, mundo);

		// 2. Actualizar lógica del inventario de tercero si está abierto
		if (this.hayInventarioTerceroAbierto()) {
			this.inventarioTercero.actualizar(raton, this.ITEM_PUNTERO, mundo);
		}

		// 3. Manejo de soltar ítem al mundo si se hace clic fuera de cualquier ventana
		if (this.ITEM_PUNTERO.contieneItem() && raton.presionadoClickIzq() && this.INVENTARIO_JUGADOR
				.getGestorTiempoRaton().transcurrioMiliSegundos(Inventario.TIEMPO_ACTUALIZACION_RATON_PRESIONADO)) {

			final Point puntoRaton = raton.getPuntoPosicionEscalado();
			final boolean sobreJugador = this.INVENTARIO_JUGADOR.esVisible()
					&& this.INVENTARIO_JUGADOR.getArea().contains(puntoRaton);
			final boolean sobreTercero = this.hayInventarioTerceroAbierto()
					&& this.inventarioTercero.getArea().contains(puntoRaton);

			if (!sobreJugador && !sobreTercero) {
				this.INVENTARIO_JUGADOR.getGestorTiempoRaton().establecerReferenciaTiempoActual();
				this.ITEM_PUNTERO.soltarItemEnMundo(mundo);
			}
		}
	}

	/**
	 * Capa 1: Fondos y slots de todas las ventanas abiertas.
	 */
	public void pintar(final Graphics2D g) {
		if (this.hayInventarioTerceroAbierto()) {
			this.inventarioTercero.pintar(g);
		}
		this.INVENTARIO_JUGADOR.pintar(g);
	}

	/**
	 * Capas 2 y 3: Tooltips y renderizado superior del ítem sostenido por el
	 * puntero.
	 */
	public void pintarTooltipsYPuntero(final Graphics2D g, final Point posicionPuntero) {
		if (this.hayInventarioTerceroAbierto()) {
			this.inventarioTercero.pintarTooltips(g);
		}
		this.INVENTARIO_JUGADOR.pintarTooltips(g);

		// Capa 3: El ítem en tránsito SIEMPRE por encima de todo
		this.ITEM_PUNTERO.pintar(g, posicionPuntero);
	}

	public Inventario getInventarioJugador() {
		return this.INVENTARIO_JUGADOR;
	}

	public boolean hayInventarioTerceroAbierto() {
		return this.inventarioTercero != null;
	}

	public void abrirInventarioTercero(final InventarioVault vault) {
		this.inventarioTercero = vault;
	}

	public void eliminarInventarioTercero(final Mundo mundo) {
		if (this.inventarioTercero != null) {
			// Si el jugador tenía un ítem tomado de este cofre al cerrarse, hacemos
			// rollback/drop seguro
			if (this.ITEM_PUNTERO.contieneItem() && (this.ITEM_PUNTERO.getSlotOrigen() != null)
					&& this.inventarioTercero.contieneSlot(this.ITEM_PUNTERO.getSlotOrigen())) {
				this.ITEM_PUNTERO.cancelarItemAgarrado(mundo);
			}
			this.inventarioTercero = null;
		}
	}

	public InventarioVault getInventarioTercero() {
		return this.inventarioTercero;
	}

	public ItemPuntero getItemPuntero() {
		return this.ITEM_PUNTERO;
	}
}