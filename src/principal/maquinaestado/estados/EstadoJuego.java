package principal.maquinaestado.estados;

import java.awt.Graphics2D;

public interface EstadoJuego {
	void actualizar();

	void pintar(final Graphics2D g);
}
