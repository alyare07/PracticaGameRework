package principal.dialogos;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MensajeDialogo {

	private final String nombreHablante;
	private final Color colorNombre;
	private final String textoCompleto;
	private final BufferedImage retrato;
	private final List<OpcionDialogo> opciones;

	public MensajeDialogo(final String nombreHablante, final Color colorNombre, final String textoCompleto,
			final BufferedImage retrato) {
		this.nombreHablante = (nombreHablante != null) ? nombreHablante : "";
		this.colorNombre = (colorNombre != null) ? colorNombre : new Color(255, 215, 80);
		this.textoCompleto = (textoCompleto != null) ? textoCompleto : "";
		this.retrato = retrato;
		this.opciones = new ArrayList<OpcionDialogo>();
	}

	public MensajeDialogo(final String nombreHablante, final String textoCompleto) {
		this(nombreHablante, new Color(255, 215, 80), textoCompleto, null);
	}

	public MensajeDialogo agregarOpcion(final String texto, final Runnable accion) {
		this.opciones.add(new OpcionDialogo(texto, accion));
		return this;
	}

	public String getNombreHablante() {
		return this.nombreHablante;
	}

	public Color getColorNombre() {
		return this.colorNombre;
	}

	public String getTextoCompleto() {
		return this.textoCompleto;
	}

	public BufferedImage getRetrato() {
		return this.retrato;
	}

	public List<OpcionDialogo> getOpciones() {
		return Collections.unmodifiableList(this.opciones);
	}

	public boolean tieneOpciones() {
		return !this.opciones.isEmpty();
	}
}