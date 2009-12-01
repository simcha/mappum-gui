package pl.ivmx.mappum.gui.model.treeelement;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.utils.java.JavaModelGenerator;

public class JavaTreeElement extends TreeElementAdapter {

	private IProject project;

	public JavaTreeElement(final IProject project, final String clazz,
			final List<TreeElement> elements, final boolean isArray,
			final String name) {
		this(project, clazz, elements, isArray, name, false, false);
	}

	public JavaTreeElement(final IProject project, final String clazz,
			final List<TreeElement> elements, final boolean isArray,
			final String name, final boolean folded,
			final boolean markAsComplex) {
		this.project = project;
		this.clazz = clazz;
		this.elements = elements;
		this.isArray = isArray;
		this.name = name;
		setFolded(folded);
		this.markAsComplex = markAsComplex;
	}

	private final String clazz;
	private List<TreeElement> elements;
	private final boolean isArray;
	private final String name;
	private boolean markAsComplex;
	private boolean array;
	private boolean folded;

	@Override
	public String getClazz() {
		return clazz;
	}

	@Override
	public List<TreeElement> getElements() {
 		if(markAsComplex && !isFolded() && elements == null){
 	       //Lazy loading
 			JavaModelGenerator jmg = JavaModelGenerator.getInstance();
 			try {
				elements = jmg.generate(clazz, project).elements;
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			
		}
		return elements;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public boolean isComplex() {
		return markAsComplex;
	}

	@Override
	public String toString() {
		return String.format(
				"[clazz=%s;isArray=%b;name=%s;elements=%s;folded=%b]", clazz,
				isArray, name, elements, isFolded());
	}

	public boolean isArray() {
		return array;
	}

	public boolean isFolded() {
		return folded;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public void setComplex(boolean markAsComplex) {
		this.markAsComplex = markAsComplex;
	}

	public void setFolded(boolean folded) {
		this.folded = folded;
	}
}
