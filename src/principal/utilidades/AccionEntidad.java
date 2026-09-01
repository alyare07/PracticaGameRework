package principal.utilidades;

import principal.entes.Ente;

@FunctionalInterface
public interface AccionEntidad<T extends Ente> {
	void ejecutar(T entidad);
}