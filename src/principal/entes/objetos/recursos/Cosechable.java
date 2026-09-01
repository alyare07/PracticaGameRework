package principal.entes.objetos.recursos;

import principal.entes.Ente;
import principal.entes.objetos.items.herramientas.TipoHerramienta;

public interface Cosechable {

	boolean golpear(TipoHerramienta tipo, double potencia, Ente causante);

	double getDurabilidad();

	double getDurabilidadMaxima();

	TipoHerramienta getHerramientaRequerida();
}