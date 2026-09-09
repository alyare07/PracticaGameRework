package principal.construccion;

import principal.mapa.Mundo;

/**
 * Contrato funcional para el despacho e instanciación de entidades
 * colocadas mediante el GestorConstruccion (Zero-GC).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
@FunctionalInterface
public interface AccionColocacion {
	void colocar(int x, int y, Mundo mundo);
}