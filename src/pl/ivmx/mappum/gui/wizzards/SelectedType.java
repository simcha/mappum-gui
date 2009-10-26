package pl.ivmx.mappum.gui.wizzards;

import org.eclipse.jdt.core.IType;

import pl.ivmx.mappum.gui.utils.java.IJavaModelGenerator;

public class SelectedType {
	private final String xmlName;
	private final IType javaType;

	public SelectedType(IType javaType) {
		assert javaType != null;
		this.javaType = javaType;
		this.xmlName = null;
	}

	public SelectedType(String xmlName) {
		assert xmlName != null;
		this.xmlName = xmlName;
		this.javaType = null;
	}

	public String getXmlName() {
		assert !isJavaType();
		return xmlName;
	}

	public IType getJavaType() {
		assert (isJavaType());
		return javaType;
	}

	public boolean isJavaType() {
		return javaType != null;
	}

	public String getName() {
		if (isJavaType()) {
			return javaType.getElementName();
		}
		return xmlName;
	}

	public String getFullName() {
		if (isJavaType()) {
			return javaType.getFullyQualifiedName();
		}
		return xmlName;
	}

	public String getPrefixedName() {
		if (isJavaType()) {
			return IJavaModelGenerator.JAVA_TYPE_PREFIX + getFullName();
		}
		return xmlName;
	}

	@Override
	public String toString() {
		return String.format("type=%s; name=%s", isJavaType() ? "Java" : "XML",
				isJavaType() ? javaType.getFullyQualifiedName() : xmlName);
	}
}
