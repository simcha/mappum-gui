package pl.ivmx.mappum.gui.utils.java;

import java.util.List;

import pl.ivmx.mappum.TreeElement;

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
	 * @throws ClassNotFoundException
	 *             when <code>clazz</code> is not found on classpath
	 * @throws IllegalArgumentException
	 *             when <code>clazz</code> is not prefixed with "Java::"
	 */
	public void generate(final String clazz, final List<TreeElement> model)
			throws ClassNotFoundException, IllegalArgumentException;
}
