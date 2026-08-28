package principal.comandos;

import principal.clima.TipoClima;
import principal.utilidades.Globales;

public class ComandoClima extends Comando {

	public ComandoClima() {
		super("clima", "clima <nombre_clima>", "Cambia el clima actual (ej: DESPEJADO, LLUVIA_TORMENTA, NIEVE, etc.)");
	}

	@Override
	public void ejecutar(final String[] args) {
		if (args.length == 0) {
			System.out.println("[Consola] Clima actual: " + Globales.GESTOR_CLIMA.getNombreClimaActual());
			return;
		}

		final String nombreClima = args[0].toUpperCase();
		try {
			final TipoClima tipo = TipoClima.valueOf(nombreClima);
			Globales.GESTOR_CLIMA.setCicloAutomaticoHabilitado(false); // Detiene el timer automático
			Globales.GESTOR_CLIMA.setClima(tipo, 1.5); // Transición suave en 1.5s
			System.out.println("[Consola] Clima cambiado exitosamente a: " + tipo.getNombre());
		} catch (final IllegalArgumentException e) {
			System.err.println("[Consola] Tipo de clima '" + nombreClima + "' no existe. Opciones validas:");
			for (final TipoClima t : TipoClima.values()) {
				System.err.print(t.name() + " ");
			}
			System.err.println();
		}
	}
}