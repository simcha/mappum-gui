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

	public boolean isArray() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isComplex() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isFolded() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setArray(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setComplex(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setFolded(boolean arg0) {
		// TODO Auto-generated method stub
		
	}
}
