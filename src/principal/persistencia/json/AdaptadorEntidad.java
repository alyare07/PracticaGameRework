package principal.persistencia.json;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.mapa.Mundo;

/**
 * Contrato formal para serializar y reconstruir entidades polimórficas del
 * motor sin acoplar las clases base a implementaciones concretas de guardado.
 *
 * @param <T> Tipo de entidad derivada de Ente.
 */
public interface AdaptadorEntidad<T extends Ente> {

	/**
	 * Identificador textual canónico inmutable de la entidad en el archivo JSON
	 * (ej. "FOGATA", "BANDIDO_PISTOLERO", "COFRE_MEDIANO").
	 */
	String getId();

	/**
	 * Clase concreta Java de la entidad que maneja este adaptador.
	 */
	Class<T> getClaseEntidad();

	/**
	 * Extrae el estado interno de la entidad hacia un payload JSON limpio.
	 *
	 * @param entidad Instancia viva de la entidad a exportar.
	 * @return Objeto JSON con los atributos propios de la entidad.
	 */
	JSONObject serializar(T entidad);

	/**
	 * Reconstruye la entidad a partir de su payload JSON inyectando el contexto del
	 * mundo.
	 *
	 * @param datos JSON correspondiente a los atributos de la entidad.
	 * @param mundo Mundo activo donde residirá la entidad (puede ser null si está en un contenedor).
	 * @return Nueva instancia viva de la entidad, o null si los datos son inválidos.
	 */
	T deserializar(JSONObject datos, Mundo mundo);
}