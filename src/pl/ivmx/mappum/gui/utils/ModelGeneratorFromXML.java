package pl.ivmx.mappum.gui.utils;

import java.io.InputStreamReader;
import java.util.List;

import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
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
	
	private List<TreeElement> model;

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

	private  List<TreeElement> evalMappumApi(){
		MappumApi mp = new MappumApi();
		WorkdirLoader wl = mp.getWorkdirLoader(getSchemaFolder(),getMapFolder(),getGeneratedClassesFolder());
		wl.generateAndRequire();
		return wl.definedElementTrees();
	}
	

	public List<TreeElement> getModel() throws ScriptException {
		if (model == null) {
			if((model = evalMappumApi()) == null)
			throw new ScriptException(
					"Error while returning model. Model is null. Check logs for more details.");
		}
		return model;
	}

	public void setModel(List<TreeElement> model) {
		this.model = model;
	}

	public List<TreeElement> generateModel(IProject project) throws ScriptException {
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
		getModel();
		for(TreeElement element: model){
			System.out.println(element.getName());
		}
		return getModel();
	}

	private Shape checkAndAddShape(String name, Shape parent, Side side) {
		if (parent == null) {
			if (side == Shape.Side.LEFT) {
				String fullName = Shape.getRootShapes().get(0).getFullName();
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
	//TODO
	public void addFieldsFromRubyModel(String leftElement, String rightElement){
		for(TreeElement element:model){
			if(element.getName().equals(leftElement)){
				Shape parent = checkAndAddShape(leftElement, null,
						Shape.Side.LEFT);
				for(TreeElement childElement: element.getElements()){
					childElement.getElements();
				}
			}
			if(element.getName().equals(rightElement)){
				
			}
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

	private void getComplexField(String searchElement, Shape parent, final Shape.Side side) {
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
