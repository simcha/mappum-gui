package pl.ivmx.mappum.gui.model.treeelement;

import java.util.List;

import pl.ivmx.mappum.TreeElement;

public abstract class TreeElementAdapter implements TreeElement {

	public String getClazz() {
		return null;
	}

	public List<TreeElement> getElements() {
		return null;
	}

	public boolean getIsArray() {
		return false;
	}

	public String getName() {
		return null;
	}

	public void setClazz(String arg0) {
		throw new UnsupportedOperationException("Operation is not supported");
	}

	public void setElements(List<TreeElement> arg0) {
		throw new UnsupportedOperationException("Operation is not supported");
	}

	public void setIsArray(boolean arg0) {
		throw new UnsupportedOperationException("Operation is not supported");
	}

	public void setName(String arg0) {
		throw new UnsupportedOperationException("Operation is not supported");
	}
}