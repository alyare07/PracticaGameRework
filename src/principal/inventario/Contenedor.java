package principal.inventario;

import principal.entes.Ente;
import principal.inventario.vault.InventarioVault;

/**
 * Contrato arquitectónico fundamental (<i>Interface Segregation Principle</i>)
 * para cualquier objeto o entidad física en el mundo del juego que posea y
 * exponga un almacenamiento interactivo.
 * 
 * <p>
 * <b>Propósito y Desacoplamiento:</b>
 * </p>
 * <ul>
 * <li><b>Abstracción Universal de Almacenamiento:</b> Permite que entidades
 * dispares (como cofres, barriles destructibles, cadáveres saqueables,
 * vehículos o NPCs comerciantes) se integren con la misma interfaz gráfica
 * {@link InventarioVault} sin acoplarse a clases concretas.</li>
 * <li><b>Anclaje Espacial:</b> Mediante {@link #getEntePropietario()}, el
 * sistema de inventario puede consultar en tiempo real las coordenadas
 * espaciales, el área de colisión y el {@code Mundo} asociado a la entidad para
 * calcular rangos de apertura o arrojar ítems al suelo de forma segura.</li>
 * </ul>
 * 
 * @author Copiloto Técnico / Arquitectura del Motor
 * @version 1.0 (Vanilla Java 8)
 * @see InventarioVault
 * @see Ente
 */
public interface Contenedor {

	/***/
	/* ========================================================================= */
	/* 1. CONTRATO DE IDENTIFICACIÓN Y TÍTULO */
	/* ========================================================================= */
	/***/

	/**
	 * Obtiene el nombre o título descriptivo que se mostrará en la barra superior
	 * de la ventana del inventario (ej: "Cofre de Madera", "Alijo Oculto",
	 * "Cadáver").
	 * 
	 * @return Cadena de texto con el nombre del contenedor (no nula).
	 */
	String getNombreContenedor();

	/***/
	/* ========================================================================= */
	/* 2. CONTRATO DE ACCESO AL INVENTARIO */
	/* ========================================================================= */
	/***/

	/**
	 * Obtiene la instancia de gestión del inventario asociada a este contenedor.
	 * 
	 * @return El objeto {@link InventarioVault} correspondiente al contenedor.
	 */
	InventarioVault getInventario();

	/***/
	/* ========================================================================= */
	/* 3. CONTRATO DE IDENTIDAD ESPACIAL EN EL MUNDO */
	/* ========================================================================= */
	/***/

	/**
	 * Obtiene la entidad física en el mundo (instancia de {@link Ente}) que es
	 * dueña de este inventario.
	 * 
	 * <p>
	 * Utilizado para comprobar distancias de interacción con el jugador y resolver
	 * el contexto del {@link principal.mapa.Mundo} al cerrar o destruir el
	 * contenedor.
	 * </p>
	 * 
	 * @return La entidad propietaria en el mapa.
	 */
	Ente getEntePropietario();
}