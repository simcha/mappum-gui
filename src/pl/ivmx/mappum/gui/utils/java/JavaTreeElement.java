package pl.ivmx.mappum.gui.utils.java;

import java.util.List;

import pl.ivmx.mappum.TreeElement;

public class JavaTreeElement implements TreeElement {

	public JavaTreeElement(final String clazz,
			final List<TreeElement> elements, final boolean isArray,
			final String name) {
		this.clazz = clazz;
		this.elements = elements;
		this.isArray = isArray;
		this.name = name;
	}

	private final String clazz;
	private final List<TreeElement> elements;
	private final boolean isArray;
	private final String name;

	@Override
	public String getClazz() {
		return clazz;
	}

	@Override
	public List<TreeElement> getElements() {
		return elements;
	}

	@Override
	public boolean getIsArray() {
		return isArray;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setClazz(String arg0) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setElements(List<TreeElement> arg0) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setIsArray(boolean arg0) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setName(String arg0) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public String toString() {
		return String.format("[clazz=%s;isArray=%b;name=%s;elements=%s]",
				clazz, isArray, name, elements);
	}
}
