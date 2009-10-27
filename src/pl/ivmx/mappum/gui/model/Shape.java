package pl.ivmx.mappum.gui.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jrubyparser.ast.CallNode;

public class Shape extends ModelElement {

	public static enum Side {
		LEFT, RIGHT;
	}

	public static enum SourceType {
		RUBY, JAVA;
	}

	private final Logger logger = Logger.getLogger(Shape.class);
	private static IPropertyDescriptor[] descriptors;

	private static final long serialVersionUID = 1;

	public static final String LAYOUT_PROP = "Shape.Layout";

	private static final String NAME_PROP = "Shape.Name";

	private static final String TYPE_PROP = "Shape.TYPE";

	public static final String CHILD_ADDED_PROP = "Shape.ChildAdded";
	public static final String CHILD_REMOVED_PROP = "Shape.ChildRemoved";

	public static final String SOURCE_CONNECTIONS_PROP = "Shape.SourceConn";

	public static final String TARGET_CONNECTIONS_PROP = "Shape.TargetConn";

	private List<Connection> sourceConnections = new ArrayList<Connection>();

	private List<Connection> targetConnections = new ArrayList<Connection>();

	private String type;
	private final Shape shapeParent;
	private List<Shape> shapeChildren;
	private Side side;
	private static List<Shape> shapes = new ArrayList<Shape>();

	private boolean arrayType = false;
	private List<Integer> arrayCounters = new ArrayList<Integer>();
	private SourceType sourceType;
	private String optionalJavaPackage;
	private boolean reccuranceInstance;

	private CallNode shapeNode;
	static {
		descriptors = new IPropertyDescriptor[] {
				new TextPropertyDescriptor(NAME_PROP, "Name"),
				new TextPropertyDescriptor(TYPE_PROP, "Type"), };
		// use a custom cell editor validator for all four array entries
		for (int i = 0; i < descriptors.length; i++) {
			((PropertyDescriptor) descriptors[i])
					.setValidator(new ICellEditorValidator() {
						public String isValid(Object value) {
							int intValue = -1;
							try {
								intValue = Integer.parseInt((String) value);
							} catch (NumberFormatException exc) {
								return "Not a number";
							}
							return (intValue >= 0) ? null
									: "Value must be >=  0";
						}
					});
		}
	}

	private Shape(final String name, final String type,
			final Shape shapeParent, final Side side, final CallNode shapeNode) {
		this.shapeNode = shapeNode;
		shapeChildren = new ArrayList<Shape>();
		if (shapeParent == null)
			rootShapes.add(this);
		shapes.add(this);
		this.name = name;
		this.type = type;
		this.shapeParent = shapeParent;
		// if (this.shapeParent != null)
		// this.shapeParent.addChild(this);
		this.side = side;
		logger.debug("Created shape: " + this);
	}

	public boolean addToParent() {
		if (shapeParent != null) {
			if (shapeParent.getChildren().contains(this)) {
				return false;
			} else {
				return shapeParent.addChild(this);
			}
		}
		return false;

	}

	public boolean addChild(Shape s) {
		if (s != null && shapeChildren.add(s)) {
			firePropertyChange(CHILD_ADDED_PROP, null, s);
			return true;
		}
		return false;
	}

	public boolean removeChild(Shape s) {
		if (s != null && shapeChildren.remove(s)) {
			firePropertyChange(CHILD_REMOVED_PROP, null, s);
			return true;
		}
		return false;
	}

	public static Shape createShape(String name, String type,
			Shape shapeParent, Side side, CallNode shapeNode) {
		if (shapeParent == null) {
			for (Shape shape : Shape.getRootShapes()) {
				if (shape.getName().equals(name) && shape.getSide() == side)
					return shape;
			}
		} else {
			for (Shape child : shapeParent.getChildren()) {
				if (child.getName().equals(name) && child.getSide() == side)
					return child;
			}

		}
		return new Shape(name, type, shapeParent, side, shapeNode);
	}

	void addConnection(Connection conn) {
		if (conn == null || conn.getSource() == conn.getTarget()) {
			throw new IllegalArgumentException();
		}
		if (conn.getSource() == this) {
			sourceConnections.add(conn);
			firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
		} else if (conn.getTarget() == this) {
			targetConnections.add(conn);
			firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
		}
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	public Object getPropertyValue(Object propertyId) {
		if (NAME_PROP.equals(propertyId)) {
			return name;
		}
		if (TYPE_PROP.equals(propertyId)) {
			return type;
		}
		return super.getPropertyValue(propertyId);
	}

	public List<Connection> getSourceConnections() {
		return new ArrayList<Connection>(sourceConnections);
	}

	public List<Connection> getTargetConnections() {
		return new ArrayList<Connection>(targetConnections);

	}

	void removeConnection(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}
		if (conn.getSource() == this) {
			sourceConnections.remove(conn);
			firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
		} else if (conn.getTarget() == this) {
			targetConnections.remove(conn);
			firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
		}
	}

	private String name;

	public String getName() {
		return name;
	}

	public Shape getParent() {
		return shapeParent;
	}

	public List<Shape> getChildren() {
		return shapeChildren;
	}

	public Side getSide() {
		return side;
	}

	public void setLayout(Rectangle layout) {
		firePropertyChange(LAYOUT_PROP, null, layout);
	}

	public int getDepth() {
		int depth = 0;
		Shape node = getParent();
		while (node != null) {
			depth++;
			node = node.getParent();
		}
		return depth;
	}

	public CallNode getShapeNode() {
		return shapeNode;
	}

	public static List<Shape> getShapes() {
		return shapes;
	}

	private static List<Shape> rootShapes = new ArrayList<Shape>();

	public static List<Shape> getRootShapes() {
		return rootShapes;
	}

	public static boolean removeShape(Shape shape) {
		if (rootShapes.get(0).equals(shape) || rootShapes.get(1).equals(shape)) {
			return false;
		} else {
			for (int i = 0; i < shapes.size(); i++) {
				if (shapes.get(i).equals(shape)) {
					if (shapes.get(i).getChildren().size() > 0) {
						for (int j = 0; j < shapes.get(i).getChildren().size(); j++) {
							removeShape(shapes.get(i).getChildren().get(j));
						}
					}
					for (int j = 0; j < shape.getSourceConnections().size(); j++) {
						Connection.removeConnection(shape
								.getSourceConnections().get(j));
					}
					for (int j = 0; j < shape.getTargetConnections().size(); j++) {
						Connection.removeConnection(shape
								.getTargetConnections().get(j));
					}
					Shape.getShapes().remove(shape);
					shape.getParent().removeChild(shape);
					shape = null;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if (shapeParent != null)
			return "|| Variable: " + name + ", type: " + type + ", parent: "
					+ shapeParent.getName() + ", array: " + arrayType
					+ ", array counters: " + arrayCounters + " ||";
		else
			return "|| Variable: " + name + ", type: " + type + ", no parent "
					+ ", array: " + arrayType + ", array counters: "
					+ arrayCounters + " ||";

	}

	public List<Shape> getShapeStack() {
		return checkShapeStack(this, null);
	}

	public String getFullName() {
		if (this.type != null)
			return type + "::" + name;
		else
			return name;
	}

	public String getPackageAndName() {
		if (optionalJavaPackage != null) {
			if (!optionalJavaPackage.equals("")) {
				return "Java::" + optionalJavaPackage + "." + name;
			}
		}
		return name;
	}

	/**
	 * Returns all parents of the shape without rootParent
	 * 
	 * @return
	 */
	private static List<Shape> checkShapeStack(Shape shape,
			List<Shape> initialShapeList) {
		if (initialShapeList == null) {
			initialShapeList = new ArrayList<Shape>();
		}
		if (shape.getParent() != null) {
			initialShapeList.add(shape);
			if (!shape.getParent().equals(Shape.getRootShapes().get(0))
					&& !shape.getParent().equals(Shape.getRootShapes().get(1))) {
				checkShapeStack(shape.getParent(), initialShapeList);
			}
		}
		return initialShapeList;
	}

	public void setArrayType(boolean arrayType) {
		this.arrayType = arrayType;
	}

	public boolean isArrayType() {
		return arrayType;
	}

	public void addArrayCounter(int arrayCounter) {
		this.arrayCounters.add(arrayCounter);
	}

	public List<Integer> getArrayCounters() {
		return arrayCounters;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setOptionalJavaPackage(String optionalJavaPackage) {
		this.optionalJavaPackage = optionalJavaPackage;
	}

	public String getOptionalJavaPackage() {
		return optionalJavaPackage;
	}

	public void setReccuranceInstance(boolean reccuranceInstance) {
		this.reccuranceInstance = reccuranceInstance;
	}

	public boolean isReccuranceInstance() {
		return reccuranceInstance;
	}

}