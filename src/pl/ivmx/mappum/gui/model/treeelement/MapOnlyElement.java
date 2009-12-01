package pl.ivmx.mappum.gui.model.treeelement;

public class MapOnlyElement extends TypedTreeElement {
	
	private boolean isArray = false;
	
	public MapOnlyElement(String name) {
		super(name);
	}
	public MapOnlyElement(String name, String type) {
		super(name, type);
	}
	public boolean isArray() {
		return isArray;
	}
	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}
}
