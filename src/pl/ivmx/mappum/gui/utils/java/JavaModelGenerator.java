package pl.ivmx.mappum.gui.utils.java;

import java.util.List;

import pl.ivmx.mappum.TreeElement;

public class JavaModelGenerator implements IJavaModelGenerator {

	private class Type {
		private final String name, type;

		public Type(final String name, final String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}
	}

	private static JavaModelGenerator instance;

	public synchronized static JavaModelGenerator getInstance() {
		if (instance == null) {
			instance = new JavaModelGenerator();
		}
		return instance;
	}

	@Override
	public boolean generate(final String classPrefixed,
			final List<TreeElement> model) throws ClassNotFoundException,
			IllegalArgumentException {

		return false;
	}

}
