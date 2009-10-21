package pl.ivmx.mappum.gui.utils.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Modifier;

import pl.ivmx.mappum.TreeElement;

public class JavaModelGenerator implements IJavaModelGenerator {

	private final static Map<Class<?>, String> PRIMITIVE_TYPES_MAPPING = new HashMap<Class<?>, String>();
	static {
		PRIMITIVE_TYPES_MAPPING.put(String.class, "String");
		PRIMITIVE_TYPES_MAPPING.put(Byte.class, "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put(Short.class, "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put(Character.class, "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put(Integer.class, "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put(Long.class, "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put(Float.class, "Float");
		PRIMITIVE_TYPES_MAPPING.put(Double.class, "Float");
	}

	private static JavaModelGenerator instance;

	public synchronized static JavaModelGenerator getInstance() {
		if (instance == null) {
			instance = new JavaModelGenerator();
		}
		return instance;
	}

	@Override
	public void generate(final String classPrefixed,
			final List<TreeElement> model) throws ClassNotFoundException,
			IllegalArgumentException {

		generate0(classPrefixed, model, null, false);
	}

	private TreeElement generate0(final String classPrefixed,
			final List<TreeElement> model, final String name,
			final boolean isArray) throws ClassNotFoundException,
			IllegalArgumentException {

		final TreeElement te = findByName(model, classPrefixed);
		if (te != null) {
			return te;
		}

		if (!classPrefixed.startsWith(IJavaModelGenerator.JAVA_TYPE_PREFIX)) {
			throw new IllegalArgumentException(String.format(
					"Type name %s must be prefixed with %s.", classPrefixed,
					IJavaModelGenerator.JAVA_TYPE_PREFIX));
		}
		final String classWithoutPrefix = classPrefixed
				.substring(IJavaModelGenerator.JAVA_TYPE_PREFIX.length());

		final Class<?> clazz = Class.forName(classWithoutPrefix);
		final List<TreeElement> subElements = new ArrayList<TreeElement>();
		for (final Method m : clazz.getMethods()) {
			if (isValidSetter(m) && hasMatchingGetter(clazz, m)) {
				final Class<?> parameterType = m.getParameterTypes()[0];
				Class<?> flatType;
				boolean isParameterArray;
				if (parameterType.isArray()) {
					isParameterArray = true;
					flatType = parameterType.getComponentType();
				} else {
					isParameterArray = false;
					flatType = parameterType;
				}

				if (PRIMITIVE_TYPES_MAPPING.containsKey(flatType)) {
					subElements.add(new JavaTreeElement(PRIMITIVE_TYPES_MAPPING
							.get(flatType), null, isParameterArray, m.getName()
							.substring(3)));
				} else {
					subElements.add(generate0(
							IJavaModelGenerator.JAVA_TYPE_PREFIX
									+ flatType.getName(), model, m.getName()
									.substring(3), isParameterArray));
				}
			}
		}
		final JavaTreeElement el = new JavaTreeElement(classPrefixed,
				subElements.isEmpty() ? null : subElements, isArray,
				name != null ? name : clazz.getName());

		model.add(el);
		return el;
	}

	private TreeElement findByName(final List<TreeElement> model,
			final String name) {
		for (final TreeElement te : model) {
			if (te.getName().equals(name)) {
				return te;
			}
		}
		return null;
	}

	private boolean hasMatchingGetter(final Class<?> clazz, final Method setter) {
		assert setter.getParameterTypes().length == 1;
		for (final Method m : clazz.getMethods()) {
			if (isValidGetter(m)
					&& setter.getName().substring(3).equals(
							m.getName().substring(3))
					&& setter.getParameterTypes()[0].equals(m.getReturnType())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasValidDeclaration(final Method m) {
		return m.getName().length() > 3 && Modifier.isPublic(m.getModifiers());
	}

	private boolean isValidGetter(final Method m) {
		return hasValidDeclaration(m) && m.getName().startsWith("get");
	}

	private boolean isValidSetter(final Method m) {
		return hasValidDeclaration(m) && m.getName().startsWith("set")
				&& m.getParameterTypes().length == 1
				&& m.getReturnType().equals(Void.TYPE);
	}
}
