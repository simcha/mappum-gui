package pl.ivmx.mappum.gui.utils.java;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;

import pl.ivmx.mappum.gui.model.treeelement.JavaTreeElement;

public interface IJavaModelGenerator {

	public final String JAVA_TYPE_PREFIX = "Java::";

	/**
	 * 
	 * Adds <code>clazz</code> structure to <code>model</code> if it is not
	 * already added.
	 * 
	 * @param clazz
	 *            full java class name prefixed with "Java::"
	 * @param model
	 *            model to add elements to
	 * @throws JavaModelException
	 *             when <code>clazz</code> is not found on classpath
	 * @throws IllegalArgumentException
	 *             when <code>clazz</code> is not prefixed with "Java::"
	 */
	public JavaTreeElement generate(final String clazz, final IProject project) throws JavaModelException,
			IllegalArgumentException;
}
