package principal.comandos;

import java.awt.Color;

import principal.dialogos.MensajeDialogo;
import principal.utilidades.Globales;

public class ComandoDialogo extends Comando {

	public ComandoDialogo() {
		super("dialogo", "dialogo <hablante> <texto...>",
				"Lanza una caja de diálogo interactiva en pantalla para testear el typewriter y el visor de texto.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.GESTOR_DIALOGOS == null) {
			this.enviarError(emisor, "El gestor de diálogos no está inicializado.");
			return;
		}

		if (args.length < 2) {
			this.enviarError(emisor, "Uso: dialogo <hablante> <mensaje...>\nEjemplo: dialogo Anciano ¡Cuidado con los bandidos del norte!");
			return;
		}

		final String hablante = args[0];
		final StringBuilder sb = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			sb.append(args[i]).append(" ");
		}

		final MensajeDialogo mensaje = new MensajeDialogo(hablante, new Color(255, 215, 80), sb.toString().trim(), null);
		Globales.GESTOR_DIALOGOS.iniciarDialogo(mensaje);
		this.enviarInfo(emisor, "Diálogo lanzado con éxito.");
	}
}