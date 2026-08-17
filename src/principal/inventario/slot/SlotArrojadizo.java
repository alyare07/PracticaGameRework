package principal.inventario.slot;

import principal.controles.Raton;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.utilidades.Constantes;

/**
 * Slot virtual especializado para almacenar y gestionar el {@link Arrojadizo}
 * que el jugador tiene preparado (cebado) para apuntar y lanzar en tiempo real.
 * 
 * <p>
 * <b>Comportamiento y Reglas de Ciclo de Vida:</b>
 * </p>
 * <ul>
 * <li><b>Slot Virtual / Sin Representación Visual Directa:</b> Se inicializa en
 * coordenadas {@code (0, 0)} ya que no pertenece a una cuadrícula de interfaz
 * estándar, sino que actúa como un contenedor de estado para la mecánica de
 * lanzamiento.</li>
 * <li><b>Restricción Estricta de Tipo (Polimorfismo OCP):</b> Únicamente admite
 * instancias de {@link Arrojadizo}.</li>
 * <li><b>Cancelación Reactiva de Apuntado:</b> Si el jugador abre el menú del
 * inventario mientras sostiene un arrojadizo listo para lanzar, el slot se
 * vacía automáticamente para evitar conflictos entre el clic de lanzamiento y
 * la interacción con la interfaz gráfica.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see Slot
 * @see Arrojadizo
 */
public class SlotArrojadizo extends Slot {

	/**
	 * Construye el slot virtual para arrojadizos sin posición fija en pantalla.
	 */
	public SlotArrojadizo() {
		super(0, 0);
	}

	/***/
	/* ========================================================================= */
	/* 1. REGLAS POLIMÓRFICAS DE ADMISIÓN (OCP) */
	/* ========================================================================= */
	/***/

	/**
	 * Valida que el ítem sea estrictamente un objeto arrojadizo (granada, cuchillo
	 * arrojadizo, etc.).
	 * 
	 * @param itemAColocar Ítem candidato a ser preparado para lanzamiento.
	 * @return {@code true} solo si el ítem es una instancia de {@link Arrojadizo};
	 *         {@code false} en caso contrario.
	 */
	@Override
	public boolean puedeAceptar(final Item itemAColocar) {
		return (itemAColocar instanceof Arrojadizo);
	}

	/***/
	/* ========================================================================= */
	/* 2. ACTUALIZACIÓN LÓGICA Y CANCELACIÓN AUTOMÁTICA */
	/* ========================================================================= */
	/***/

	/**
	 * Actualiza el estado del slot arrojadizo a 60 APS.
	 * 
	 * <p>
	 * <b>Regla de Limpieza:</b> Si la ventana del inventario principal se vuelve
	 * visible, se desactiva y elimina el ítem preparado para devolver el control
	 * del ratón a la interfaz.
	 * </p>
	 * 
	 * @param raton Instancia del controlador de entrada del ratón.
	 */
	@Override
	public void actualizar(final Raton raton) {
		this.verificarEliminacion();

		// Si el jugador abre su inventario mientras apuntaba un arrojadizo, cancelamos
		// la acción
		if ((Constantes.GESTOR_INVENTARIO.getInventarioJugador() != null)
				&& Constantes.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
			this.eliminarObjeto();
		}
	}

	/***/
	/* ========================================================================= */
	/* 3. MUTADORES DE ESTADO SEGUROS */
	/* ========================================================================= */
	/***/

	/**
	 * Establece el arrojadizo activo tras validar el tipo de ítem mediante
	 * {@link #puedeAceptar(Item)}.
	 * 
	 * @param obj Instancia de {@link Arrojadizo} a cebar, o {@code null} para
	 *            desarmar el lanzamiento.
	 */
	@Override
	public void establecerObjeto(final Item obj) {
		if ((obj == null) || this.puedeAceptar(obj)) {
			this.item = obj;
		}
	}
}