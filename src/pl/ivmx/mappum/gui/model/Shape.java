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

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.utils.TreeModelGenerator;

public class Shape extends ModelElement {

	public static enum Side {
		LEFT, RIGHT;
	}

	public static enum SourceType {
		RUBY, JAVA;
	}
	
	@SuppressWarnings("unused")
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

	private final List<Connection> sourceConnections = new ArrayList<Connection>();
	private final List<Connection> targetConnections = new ArrayList<Connection>();

	private final TreeElement element;
	private final Shape parent;
	private final List<Shape> children;
	private final Side side;

	private List<Integer> arrayCounters = new ArrayList<Integer>();
	private SourceType sourceType;
	private String optionalJavaPackage;
	private boolean reccuranceInstance;

	private CallNode shapeNode;
	private TreeModelGenerator modelGenerator;
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

	private Shape(final TreeModelGenerator mg, final TreeElement element,
			final Shape shapeParent, final Side side, final CallNode shapeNode) {
		this.shapeNode = shapeNode;
		children = new ArrayList<Shape>();
		if (shapeParent == null)
			rootShapes.add(this);
		this.element = element;
		this.parent = shapeParent;
		this.side = side;
		this.modelGenerator = mg;
		//logger.trace("Created shape: " + this);
	}

	public boolean addToParent() {
		if (parent != null && parent.children != null) {
			if (parent.children.contains(this)) {
				return false;
			} else {
				return parent.addChild(this);
			}
		}
		return false;

	}

	public boolean addChild(Shape s) {
		if (s != null && children.add(s)) {
			firePropertyChange(CHILD_ADDED_PROP, null, s);
			return true;
		}
		return false;
	}

	public boolean removeChild(Shape s) {
		if (s != null && children.remove(s)) {
			firePropertyChange(CHILD_REMOVED_PROP, null, s);
			return true;
		}
		return false;
	}

	public static Shape createShape(TreeModelGenerator mg, TreeElement element,
			Shape shapeParent, Side side, CallNode shapeNode) {
		if (shapeParent == null) {
			for (Shape shape : Shape.getRootShapes()) {
				if (shape.getName().equals(element.getName()) && shape.getSide() == side)
					return shape;
			}
		}
		return new Shape(mg, element, shapeParent, side, shapeNode);
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

	public String getType() {
		return element.getClazz();
	}

	public Object getPropertyValue(Object propertyId) {
		if (NAME_PROP.equals(propertyId)) {
			return element.getName();
		}
		if (TYPE_PROP.equals(propertyId)) {
			return element.getClazz();
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

	public String getName() {
		return element.getName();
	}

	public Shape getParent() {
		return parent;
	}

	public List<Shape> getChildren() {
		if(!isFolded() && children.isEmpty()) {
			if(element.getElements() == null||
					element.getElements().isEmpty()) {
				return new ArrayList<Shape>();
			} else {
				//Lazy Unfolding
				modelGenerator.getComplexField(element, this, side);
			}
		}
		return children;
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

	private static List<Shape> rootShapes = new ArrayList<Shape>();

	public static List<Shape> getRootShapes() {
		return rootShapes;
	}

	@Override
	public String toString() {
		if (parent != null)
			return "|| Variable: " + getName() + ", type: " + getType() + ", parent: "
					+ parent.getName() + ", array: " + isArrayType()
					+ ", array counters: " + arrayCounters + " ||";
		else
			return "|| Variable: " + getName() + ", type: " + getType() + ", no parent "
					+ ", array: " + isArrayType() + ", array counters: "
					+ arrayCounters + " ||";

	}

	public List<Shape> getShapeStack() {
		return checkShapeStack(this, null);
	}

	public String getFullName() {
		if (this.getType() != null)
			return getType() + "::" + getName();
		else
			return getName();
	}

	public String getPackageAndName() {
		if (optionalJavaPackage != null) {
			if (!optionalJavaPackage.equals("")) {
				return "Java::" + optionalJavaPackage + "." + getName();
			}
		}
		return getName();
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

	public boolean isArrayType() {
		return element.isArray();
	}
	
	public void setArrayType(boolean isArray) {
		element.setArray(isArray);
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

	public void setReccuranceInstance(boolean reccuranceInstance) {
		this.reccuranceInstance = reccuranceInstance;
	}

	public boolean isReccuranceInstance() {
		return reccuranceInstance;
	}

	public boolean isFolded() {
		return element.isFolded();
	}
	
	public boolean isComplex() {
		return element.isComplex();
	}
	
	public void setFolded(boolean folded) {
		element.setFolded(folded);
	}
}