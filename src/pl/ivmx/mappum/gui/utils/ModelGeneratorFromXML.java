package pl.ivmx.mappum.gui.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.ListNode;

import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.Shape.Side;

import com.sun.script.jruby.JRubyScriptEngineFactory;

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

	private RubyArray modelArray;

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

	public void generateAndRequire() throws ScriptException {
		// ScriptEngineFactory factory = (ScriptEngineFactory) new
		// com.sun.script.jruby.JRubyScriptEngineFactory();
		// ScriptEngine engine = factory.getScriptEngine();
		// if (manager == null)
		// manager = new ScriptEngineManager();
		// if (engine == null)
		// engine = manager.getEngineByName(language);
		ScriptEngineFactory factory = new JRubyScriptEngineFactory();
		System.out.println("Getting engine");
		final ClassLoader oldClassLoader = Thread.currentThread()
				.getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);
		ScriptEngine engine = factory.getScriptEngine();
		Thread.currentThread().setContextClassLoader(oldClassLoader);

		StringBuffer params = new StringBuffer();
		params.append("\n");
		params.append("require 'mappum/xml_transform'");
		params.append("\n\n");
		params.append("begin");
		params.append("\n");
		params.append("wl = Mappum::WorkdirLoader.new(\"");
		params.append(getSchemaFolder());
		params.append("\", \"");
		params.append(getMapFolder());
		params.append("\", \"");
		params.append(getGeneratedClassesFolder());
		params.append("\")");
		params.append("\n");
		params.append("wl.generate_and_require");
		params.append("\n");
		params.append("wl.defined_element_trees(nil)");
		params.append("\n");
		params.append("rescue => e");
		params.append("\n");
		params.append("puts e.backtrace");
		params.append("\n");
		params.append("raise e");
		params.append("\n");
		params.append("end");

		xsd2rubyScriptCode = params.toString();
		logger.debug("Generated ruby code for ruby script engine:"
				+ xsd2rubyScriptCode);
		xsd2rubyScript = new InputStreamReader(new ByteArrayInputStream(
				xsd2rubyScriptCode.getBytes()));

		// execute the script (non-compiled!)

		logger.debug("Starting generating classes...");
		modelArray = (RubyArray) engine.eval(xsd2rubyScript);
		logger.debug("Classes generated");
	}

	public RubyArray getModelArray() throws ScriptException {
		if (modelArray != null) {
			return modelArray;
		} else {
			throw new ScriptException(
					"Error while returning model. Model is null. Check logs for more details.");
		}
	}

	public void setModelArray(RubyArray modelArray) {
		this.modelArray = modelArray;
	}

	public RubyArray generateModel(IProject project) throws ScriptException {
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
		generateAndRequire();
		return getModelArray();
	}

	private Shape checkAndAddShape(String name, Shape parent, Side side) {
		if (parent == null) {
			if (side == Shape.Side.LEFT) {
				if (name.equals(Shape.getRootShapes().get(0).getFullName())) {
					return Shape.getRootShapes().get(0);
					// }
					// Shape shape = Shape.createShape(name, null, null, side,
					// generateRubyModelForField(name, side));
					// return shape;
				} else {
					throw new IllegalArgumentException(
							"There is no root element for arguments: Shape name: "
									+ name + ", side: " + side);
				}
			} else {
				if (name.equals(Shape.getRootShapes().get(1).getFullName())) {
					return Shape.getRootShapes().get(1);
					// }
					// Shape shape = Shape.createShape(name, null, null, side,
					// generateRubyModelForField(name, side));
					// return shape;
				} else {
					throw new IllegalArgumentException(
							"There is no root element for arguments: Shape name: "
									+ name + ", side: " + side);
				}
			}
		} else {
			for (Shape shape : parent.getChildren()) {
				if (shape.getName().equals(name)) {
					return shape;
				}
			}
			// TODO create ruby node for Shape
			Shape shape = Shape.createShape(name, null, parent, side,
					generateRubyModelForField(name, side));
			shape.addToParent();
			return shape;
		}
	}

	public void addFieldsFromRubyArray(String leftElement, String rightElement) {
		for (int i = 0; i < modelArray.size(); i++) {
			RubyArray rubyArray = (RubyArray) modelArray.get(i);
			if (((RubyClass) (rubyArray.get(0))).getName().equals(leftElement)) {
				System.out.println("Field: " + leftElement);
				Shape parent = checkAndAddShape(leftElement, null,
						Shape.Side.LEFT);
				for (int j = 0; j < rubyArray.size(); j++) {
					if (rubyArray.get(j) instanceof RubyArray) {
						RubyArray childArray = (RubyArray) rubyArray.get(j);
						for (int n = 0; n < childArray.size(); n++) {
							if (childArray.get(n) instanceof RubyArray) {
								RubyArray preChildArray = (RubyArray) childArray
										.get(n);
								String childElement = (String) preChildArray
										.get(0);
								System.out.println("Parent: " + leftElement
										+ ", Field: " + childElement);
								Shape child = checkAndAddShape(childElement,
										parent, Shape.Side.LEFT);
								if (preChildArray.get(1) != null) {
									getComplexField(((RubyClass) (preChildArray
											.get(1))).getName(), child,
											Shape.Side.LEFT);
								}

							}
						}
					}
				}

			}
			if (((RubyClass) (rubyArray.get(0))).getName().equals(rightElement)) {
				System.out.println("Field: " + rightElement);
				Shape parent = checkAndAddShape(rightElement, null,
						Shape.Side.RIGHT);
				for (int j = 0; j < rubyArray.size(); j++) {
					if (rubyArray.get(j) instanceof RubyArray) {
						RubyArray childArray = (RubyArray) rubyArray.get(j);
						for (int n = 0; n < childArray.size(); n++) {
							if (childArray.get(n) instanceof RubyArray) {
								RubyArray preChildArray = (RubyArray) childArray
										.get(n);
								String childElement = (String) preChildArray
										.get(0);
								System.out.println("Parent: " + rightElement
										+ ", Field: " + childElement);
								Shape child = checkAndAddShape(childElement,
										parent, Shape.Side.RIGHT);
								if (preChildArray.get(1) != null) {
									getComplexField(((RubyClass) (preChildArray
											.get(1))).getName(), child,
											Shape.Side.RIGHT);
								}

							}
						}
					}
				}

			}
		}
	}

	private void getComplexField(String searchElement, Shape parent,
			Shape.Side side) {
		for (int i = 0; i < modelArray.size(); i++) {
			RubyArray rubyArray = (RubyArray) modelArray.get(i);
			if (((RubyClass) (rubyArray.get(0))).getName()
					.equals(searchElement)) {
				for (int j = 0; j < rubyArray.size(); j++) {
					if (rubyArray.get(j) instanceof RubyArray) {
						RubyArray childArray = (RubyArray) rubyArray.get(j);
						for (int n = 0; n < childArray.size(); n++) {
							if (childArray.get(n) instanceof RubyArray) {
								RubyArray preChildArray = (RubyArray) childArray
										.get(n);
								String childElement = (String) preChildArray
										.get(0);
								System.out.println("Parent: " + parent
										+ ", Field: " + childElement);
								Shape child = checkAndAddShape(childElement,
										parent, side);
								if (preChildArray.get(1) != null) {
									getComplexField(((RubyClass) (preChildArray
											.get(1))).getName(), child, side);
								}

							}
						}
					}
				}

			}
		}
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

}
