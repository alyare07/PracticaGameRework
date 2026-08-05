package principal.utilidades;

/**
 * Esta clase gestionara el tiempo teniendo como referencia 
 * cada actualizacion. Donde se estimara que cada actualizacion
 * tendra un tiempo de 16.66 milisegundos.
 *    Para gestionar los tiempos por System.nanoTime() usar {@link GestorTiempo}
 */
public class GestorTIempoActualizacion {
	public static final double TIEMPO_MS_POR_ACTUALIZACION = 16.66;
	protected double tiempoTranscurrido;
	protected boolean pausa;
	
	public GestorTIempoActualizacion() {
		
	}
	
	public GestorTIempoActualizacion(final GestorTIempoActualizacion gta) {
		this.tiempoTranscurrido = gta.tiempoTranscurrido;
	}
	
	public void actualizar() {
		if(pausa) {
			return;
		}
		if(this.tiempoTranscurrido<(Double.MAX_VALUE-20)) {
			this.tiempoTranscurrido+=TIEMPO_MS_POR_ACTUALIZACION;
		}else if(this.tiempoTranscurrido!=Double.MAX_VALUE) {
			this.tiempoTranscurrido = Double.MAX_VALUE;
		}
	}
	
	public void reiniciarTiempo() {
		this.tiempoTranscurrido = 0;
	}
	
	public void pausar() {
		this.pausa = true;
	}
	
	public void reanudar() {
		this.pausa = false;
	}
	
	public double getMSTranscurridos() {
		return this.tiempoTranscurrido;
	}
	
	public boolean enPausa() {
		return this.pausa;
	}
	
	public boolean transcurrioMS(final double ms) {
		return this.tiempoTranscurrido >= ms;
	}
	
	public boolean transcurrioSeg(final double seg) {
		return (this.tiempoTranscurrido/1000) >= seg;
	}

}
