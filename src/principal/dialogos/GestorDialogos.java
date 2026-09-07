package principal.dialogos;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class GestorDialogos {

	private final CajaDialogo cajaVisual = new CajaDialogo();
	private final Queue<MensajeDialogo> colaMensajes = new ArrayDeque<MensajeDialogo>();
	private boolean activo = false;
	private Runnable alCerrarTodos = null;

	public void iniciarDialogo(final MensajeDialogo mensaje) {
		this.colaMensajes.clear();
		this.colaMensajes.add(mensaje);
		this.activo = true;
		this.cajaVisual.mostrarMensaje(this.colaMensajes.poll());
	}

	public void iniciarConversacion(final List<MensajeDialogo> mensajes, final Runnable alFinalizar) {
		this.colaMensajes.clear();
		this.colaMensajes.addAll(mensajes);
		this.alCerrarTodos = alFinalizar;
		this.activo = true;
		this.siguienteMensaje();
	}

	public void siguienteMensaje() {
		if (!this.colaMensajes.isEmpty()) {
			this.cajaVisual.mostrarMensaje(this.colaMensajes.poll());
		} else {
			this.cerrarDialogo();
		}
	}

	public void cerrarDialogo() {
		this.activo = false;
		this.colaMensajes.clear();
		if (this.alCerrarTodos != null) {
			final Runnable r = this.alCerrarTodos;
			this.alCerrarTodos = null;
			r.run();
		}
	}

	public void actualizar(final double dt) {
		if (this.activo) {
			this.cajaVisual.actualizar(dt);
		}
	}

	public void pintar(final Graphics2D g) {
		if (this.activo) {
			this.cajaVisual.pintar(g);
		}
	}

	public boolean isActivo() {
		return this.activo;
	}
}