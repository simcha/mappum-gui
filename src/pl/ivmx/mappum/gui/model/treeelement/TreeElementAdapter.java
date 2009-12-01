package pl.ivmx.mappum.gui.model.treeelement;

import java.util.List;

import pl.ivmx.mappum.TreeElement;

public abstract class TreeElementAdapter implements TreeElement{

	public String getClazz() {
		return null;
	}

	public List<TreeElement> getElements() {
		return null;
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

	public void setName(String arg0) {
		throw new UnsupportedOperationException("Operation is not supported");
	}
	
	public int compareTo(TreeElement that) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    
	    if ( this == that ) return EQUAL;
	    if ( that == null ) return AFTER;
	    if ( this.getName() == null ) return BEFORE;
	    if ( that.getName() == null ) return AFTER;
	    
	    return this.getName().compareTo(that.getName());
	}
}