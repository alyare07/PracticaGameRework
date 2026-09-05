package principal.maquinaestado.estados.pantallaCarga;

public interface cargaMapa {
	void cargarMapa(final GestorCarga gc, final String nombreMapa, final String nombreMundo, final String nombreSpawn,
			final boolean reset);
}
