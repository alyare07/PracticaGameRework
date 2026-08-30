package principal.comandos;

import principal.clima.TipoClima;
import principal.utilidades.Globales;

/**
 * Comando para consultar o modificar el estado meteorológico activo del mundo.
 * <p>
 * Compatible tanto con la consola local (Eclipse) como con terminales remotas
 * (Termux / Netcat).
 * </p>
 */
public class ComandoClima extends Comando {

	public ComandoClima() {
		super("clima", "clima <nombre_clima>", "Cambia el clima actual (ej: DESPEJADO, LLUVIA_TORMENTA, NIEVE, etc.)");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_CLIMA == null) {
			this.enviarError(emisor, "El subsistema de clima no esta inicializado.");
			return;
		}

		// Si no se pasaron argumentos, mostramos el clima actual
		if (args.length == 0) {
			this.enviarInfo(emisor, "Clima actual: " + Globales.GESTOR_CLIMA.getNombreClimaActual());
			return;
		}

		final String nombreClima = args[0].toUpperCase();
		try {
			final TipoClima tipo = TipoClima.valueOf(nombreClima);
			Globales.GESTOR_CLIMA.setCicloAutomaticoHabilitado(false); // Detiene el timer automático
			Globales.GESTOR_CLIMA.setClima(tipo, 1.5); // Transición suave en 1.5s
			this.enviarInfo(emisor, "Clima cambiado exitosamente a: " + tipo.getNombre());
		} catch (final IllegalArgumentException e) {
			// Construimos la lista completa de opciones en un único mensaje
			final StringBuilder sb = new StringBuilder("Tipo de clima '").append(nombreClima)
					.append("' no existe. Opciones validas:\n");

			for (final TipoClima t : TipoClima.values()) {
				sb.append(t.name()).append(" ");
			}

			this.enviarError(emisor, sb.toString());
		}
	}
}