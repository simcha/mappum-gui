package pl.ivmx.mappum.gui.utils.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaModelManager;

import pl.ivmx.mappum.TreeElement;

@SuppressWarnings("restriction")
public class JavaModelGenerator implements IJavaModelGenerator {

	private final static Map<String, String> PRIMITIVE_TYPES_MAPPING = new HashMap<String, String>();
	private final static Map<String, String> PRIMITIVE_OBJECTS_MAPPING = new HashMap<String, String>();
	static {

		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.String", "String");
		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.Byte", "Fixnum");
		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.Short", "Fixnum");
		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.Character", "Fixnum");
		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.Integer", "Fixnum");
		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.Long", "Fixnum");
		PRIMITIVE_OBJECTS_MAPPING.put("java.Lang.Float", "Float");
		PRIMITIVE_OBJECTS_MAPPING.put("java.lang.Double", "Float");

		PRIMITIVE_TYPES_MAPPING.put("byte", "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put("short", "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put("char", "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put("int", "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put("long", "Fixnum");
		PRIMITIVE_TYPES_MAPPING.put("float", "Float");
		PRIMITIVE_TYPES_MAPPING.put("double", "Float");
	}

	private static JavaModelGenerator instance;

	public synchronized static JavaModelGenerator getInstance() {
		if (instance == null) {
			instance = new JavaModelGenerator();
		}
		return instance;
	}

	public void generate(final String classPrefixed,
			final List<JavaTreeElement> model, final IProject project)
			throws JavaModelException, IllegalArgumentException {

		generate0(classPrefixed, model, null, false, project);
	}

	private JavaTreeElement generate0(final String classPrefixed,
			final List<JavaTreeElement> model, final String name,
			final boolean isArray, final IProject project)
			throws JavaModelException, IllegalArgumentException {

		final JavaTreeElement te = findByType(model, classPrefixed);
		if (te != null) {
			return new JavaTreeElement(null, null, isArray, name, false, te
					.isMarkedAsComplex());
		}

		if (!classPrefixed.startsWith(IJavaModelGenerator.JAVA_TYPE_PREFIX)) {
			throw new IllegalArgumentException(String.format(
					"Type name %s must be prefixed with %s.", classPrefixed,
					IJavaModelGenerator.JAVA_TYPE_PREFIX));
		}
		final String classWithoutPrefix = classPrefixed
				.substring(IJavaModelGenerator.JAVA_TYPE_PREFIX.length());

		final IType type = JavaModelManager.getJavaModelManager()
				.getJavaModel().getJavaProject(project.getName()).findType(
						classWithoutPrefix);
		final List<TreeElement> subElements = new ArrayList<TreeElement>();
		for (final IMethod m : type.getMethods()) {
			if (isValidSetter(m) && hasMatchingGetter(type, m)) {
				final String parameterType = m.getParameterTypes()[0];
				String flatType;
				boolean isParameterArray;
				if (Signature.getTypeSignatureKind(parameterType) == Signature.ARRAY_TYPE_SIGNATURE) {
					isParameterArray = true;
				} else {
					isParameterArray = false;
				}
				flatType = Signature.getSignatureSimpleName(Signature
						.getElementType(parameterType));

				final String resolved = resolve(type, parameterType);
				if (resolved == null) {
					if (PRIMITIVE_TYPES_MAPPING.containsKey(flatType)) {
						subElements.add(new JavaTreeElement(
								PRIMITIVE_TYPES_MAPPING.get(flatType), null,
								isParameterArray, m.getElementName().substring(
										3)));
					} else {
						throw new IllegalArgumentException(String.format(
								"Parameter type=%s cannot be resolved",
								flatType));
					}
				} else if (PRIMITIVE_OBJECTS_MAPPING.containsKey(resolved)) {
					subElements.add(new JavaTreeElement(
							PRIMITIVE_OBJECTS_MAPPING.get(resolved), null,
							isParameterArray, m.getElementName().substring(3)));
				} else {
					add(model, new JavaTreeElement(classPrefixed, null,
							isArray, type.getElementName(), false, false));
					subElements.add(generate0(
							IJavaModelGenerator.JAVA_TYPE_PREFIX + resolved,
							model, m.getElementName().substring(3),
							isParameterArray, project));
				}
			}
		}
		final JavaTreeElement el = new JavaTreeElement(classPrefixed,
				subElements.isEmpty() ? null : subElements, isArray,
				name != null ? name : type.getElementName());

		add(model, el);
		return el;
	}

	private String resolve(final IType type, final String name)
			throws JavaModelException, IllegalArgumentException {
		final String[][] resolved = type.resolveType(Signature
				.getSignatureSimpleName(Signature.getElementType(name)));

		if (resolved == null) {
			return null;
		}

		assert resolved.length == 1;
		assert resolved[0].length == 2;

		return resolved[0][0] + "." + resolved[0][1];
	}

	private JavaTreeElement findByType(final List<JavaTreeElement> model,
			final String clazz) {
		for (final JavaTreeElement te : model) {
			if (te.getClazz().equals(clazz)) {
				return te;
			}
		}
		return null;
	}

	private void add(final List<JavaTreeElement> model,
			final JavaTreeElement element) {
		for (int i = 0; i < model.size(); i++) {
			final JavaTreeElement te = model.get(i);
			if (te.getName().equals(element.getName())) {
				if (te.isComplete() || !element.isComplete()) {
					return;
				} else {
					model.remove(i);
					break;
				}
			}
		}
		model.add(element);
	}

	private boolean hasMatchingGetter(final IType type, final IMethod setter)
			throws JavaModelException {
		assert setter.getNumberOfParameters() == 1;
		for (final IMethod m : type.getMethods()) {
			if (isValidGetter(m)
					&& setter.getElementName().substring(3).equals(
							m.getElementName().substring(3))
					&& setter.getParameterTypes()[0].equals(m.getReturnType())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasValidDeclaration(final IMethod m)
			throws JavaModelException {
		return m.getElementName().length() > 3 && Flags.isPublic(m.getFlags());
	}

	private boolean isValidGetter(final IMethod m) throws JavaModelException {
		return hasValidDeclaration(m) && m.getElementName().startsWith("get");
	}

	private boolean isValidSetter(final IMethod m) throws JavaModelException {
		return hasValidDeclaration(m) && m.getElementName().startsWith("set")
				&& m.getParameterTypes().length == 1
				&& m.getReturnType().equals(String.valueOf(Signature.C_VOID));
	}
}
