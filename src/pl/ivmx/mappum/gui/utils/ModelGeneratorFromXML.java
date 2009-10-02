package pl.ivmx.mappum.gui.utils;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.jruby.RubyArray;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.ListNode;

import pl.ivmx.mappum.MappumApi;
import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.WorkdirLoader;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.Shape.Side;

public class ModelGeneratorFromXML {
	public static final String LANGUAGE_RUBY = "jruby";

	public static final String DEFAULT_MAP_FOLDER = ".map";
	public static final String DEFAULT_WORKING_MAP_FOLDER = "map";
	public static final String DEFAULT_SCHEMA_FOLDER = "schema";
	public static final String DEFAULT_GENERATED_CLASSES_FOLDER = ".classes";
	public static final String DEFAULT_PROJECT_PROPERTIES_FILE = "project.properties";

	private String mapFolder;
	private String schemaFolder;
	private String generatedClassesFolder;

	private String xsd2rubyScriptCode;
	private InputStreamReader xsd2rubyScript;

	private List<TreeElement> modelArray;

	private Logger logger = Logger.getLogger(ModelGeneratorFromXML.class);

	private static final ModelGeneratorFromXML INSTANCE = new ModelGeneratorFromXML();

	private ModelGeneratorFromXML() {
	}

	public static final ModelGeneratorFromXML getInstance() {
		return INSTANCE;
	}

	public String getMapFolder() {
		return mapFolder;
	}

	public void setMapFolder(String mapFolder) {
		this.mapFolder = mapFolder;
	}

	public String getSchemaFolder() {
		return schemaFolder;
	}

	public void setSchemaFolder(String schemaFolder) {
		this.schemaFolder = schemaFolder;
	}

	public String getGeneratedClassesFolder() {
		return generatedClassesFolder;
	}

	public void setGeneratedClassesFolder(String generatedClassesFolder) {
		this.generatedClassesFolder = generatedClassesFolder;
	}

	private List<TreeElement> generateAndRequire2() throws ScriptException {
		final MappumApi mp = new MappumApi();
		final WorkdirLoader wl = mp.getWorkdirLoader(getSchemaFolder(),
				getMapFolder(), getGeneratedClassesFolder());
		wl.generateAndRequire();
		return wl.definedElementTrees();
	}

	public List<TreeElement> getModelArray() throws ScriptException {
		if (modelArray == null) {
			modelArray = generateAndRequire2();
		}
		return modelArray;
	}

	public void setModelArray(RubyArray modelArray) {
		this.modelArray = modelArray;
	}

	public List<TreeElement> generateModel(IProject project)
			throws ScriptException {
		final String classesFolder = project.getFolder(
				ModelGeneratorFromXML.DEFAULT_GENERATED_CLASSES_FOLDER)
				.getLocation().toPortableString();
		final String mapFolder = project.getFolder(
				ModelGeneratorFromXML.DEFAULT_MAP_FOLDER).getLocation()
				.toPortableString();
		final String schemaFolder = project.getFolder(
				ModelGeneratorFromXML.DEFAULT_SCHEMA_FOLDER).getLocation()
				.toPortableString();

		setGeneratedClassesFolder(classesFolder);
		setMapFolder(mapFolder);
		setSchemaFolder(schemaFolder);
		// generateAndRequire();
		return getModelArray();
	}

	private Shape addShape(String name, Shape parent, Side side) {
		// TODO create ruby node for Shape
		Shape shape = Shape.createShape(name, null, parent, side,
				generateRubyModelForField(name, side));
		shape.addToParent();
		return shape;
	}

	private void addFieldsFromRubyArray0(final List<TreeElement> elementList,
			final Shape parent, final Shape.Side side) {
		if (elementList != null) {

			for (final TreeElement te : elementList) {
				final Shape newShape = addShape(te.getName(), parent, side);
				addFieldsFromRubyArray0(te.getElements(), newShape, side);
			}
		}
	}

	private static class ElementNameComparator implements
			Comparator<TreeElement> {

		@Override
		public int compare(final TreeElement o1, final TreeElement o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}

	public void addFieldsFromRubyArray(final Shape leftShape,
			final Shape rightShape) {

		Collections.sort(modelArray, new ElementNameComparator());

		addFieldsFromRubyArray0(modelArray.get(
				Collections.binarySearch(modelArray, new NamedElement(leftShape
						.getFullName()), new ElementNameComparator()))
				.getElements(), leftShape, Shape.Side.LEFT);

		addFieldsFromRubyArray0(modelArray.get(
				Collections.binarySearch(modelArray, new NamedElement(
						rightShape.getFullName()), new ElementNameComparator()))
				.getElements(), rightShape, Shape.Side.RIGHT);
	}

	public CallNode generateRubyModelForField(String name, final Shape.Side side) {
		// String prefix = RootNodeHolder.getInstance().generateRandomIdent(
		// RootNodeHolder.IDENT_LENGTH);
		if (side == Shape.Side.LEFT) {
			ListNode listNode = new ListNode(new SourcePosition());
			DVarNode dVarNode = new DVarNode(new SourcePosition(), 0,
					"changeMe");
			return new CallNode(new SourcePosition(), dVarNode, name, listNode);
		} else {
			ListNode listNode = new ListNode(new SourcePosition());
			DVarNode dVarNode = new DVarNode(new SourcePosition(), 1,
					"changeMe");
			return new CallNode(new SourcePosition(), dVarNode, name, listNode);
		}

	}

	private static class NamedElement implements TreeElement {

		private final String name;

		public NamedElement(final String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getClazz() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<TreeElement> getElements() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getIsArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setClazz(String arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setElements(List<TreeElement> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setIsArray(boolean arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String arg0) {
			throw new UnsupportedOperationException();
		}
	}
}
