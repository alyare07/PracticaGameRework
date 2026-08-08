package principal.ia;

import java.util.ArrayList;

public class Lista<T> extends ArrayList<T> {

	private static final long serialVersionUID = -2227937676810489865L;

	private int pos = 0;

	public T getNext() {
		if (this.pos < this.size()) {
			return this.get(this.pos++);
		}
		return null;
	}

	public T getFirst() {
		return this.get(0);
	}

	public T getLast() {
		return this.get(this.size() - 1);
	}

	public boolean hasNext() {
		return this.pos < this.size();
	}

	public void reload() {
		this.pos = 0;
	}

	public boolean isActual(final T t) {
		if ((this.pos >= 0) && (this.pos < this.size())) {
			return this.get(this.pos) == t;
		}
		return false;
	}

}
