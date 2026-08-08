package principal.maquinaestado.estados.pantallaCarga;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class GestorCarga {
	protected int porcentaje;
	protected boolean completo;
	protected Thread hiloCarga;
	protected Lock lock = new ReentrantLock();
	protected String detalleCarga = "";
	public int getPorcentaje() {
		return this.porcentaje;
	}
	
	public boolean isCompleto() {
		return this.completo;
	}
	
	public void setCompleto(final boolean completo) {
		this.lock.lock();
		try {
			this.completo = completo;
			if(completo) this.porcentaje = 100;
		} finally {
			this.lock.unlock();
		}
	}
	
	public String getDetalleCarga() {
		return this.detalleCarga;
	}
	
	public void setPorcentajeCarga(final int porcentaje) {
		this.lock.lock();
		try {
			this.porcentaje = porcentaje;
		} finally {
			this.lock.unlock();
		}
	}
	
	public void setDetalleCarga(final String detalleCarga) {
		this.lock.lock();
		try {
			this.detalleCarga = detalleCarga;
		} finally {
			this.lock.unlock();
		}
		
	}

}
