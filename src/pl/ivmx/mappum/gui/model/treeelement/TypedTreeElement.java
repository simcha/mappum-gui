package pl.ivmx.mappum.gui.model.treeelement;

public class TypedTreeElement extends TreeElementAdapter {
	private final String name, type;

	public TypedTreeElement(String name) {
		this.name = name;
		this.type = null;
	}

	public TypedTreeElement(String name, final String type) {
		this.name = name;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}
}
