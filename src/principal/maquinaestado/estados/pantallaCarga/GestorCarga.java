package principal.maquinaestado.estados.pantallaCarga;

/**
 * Controla el estado de progreso (0% a 100%) y detalle textual de una tarea de
 * carga asíncrona. Usamos variables {@code volatile} para lectura/escritura
 * thread-safe instantánea entre hilos.
 */
public abstract class GestorCarga {

	protected volatile int porcentaje;
	protected volatile boolean completo;
	protected volatile String detalleCarga = "Iniciando carga...";
	protected Thread hiloCarga;

	public int getPorcentaje() {
		return this.porcentaje;
	}

	public boolean isCompleto() {
		return this.completo;
	}

	public void setCompleto(final boolean completo) {
		this.completo = completo;
		if (completo) {
			this.porcentaje = 100;
		}
	}

	public String getDetalleCarga() {
		return this.detalleCarga;
	}

	public void setPorcentajeCarga(final int porcentaje) {
		this.porcentaje = Math.min(100, Math.max(0, porcentaje));
	}

	public void setDetalleCarga(final String detalleCarga) {
		this.detalleCarga = (detalleCarga != null) ? detalleCarga : "";
	}
}