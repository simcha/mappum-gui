package pl.ivmx.mappum.gui.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jrubyparser.ast.CallNode;

import pl.ivmx.mappum.gui.MappumPlugin;

public class Shape extends ModelElement {

	private Logger logger = Logger.getLogger(Shape.class);
	private static final Image RECTANGLE_ICON = createImage("icons/rectangle16.gif");
	private static IPropertyDescriptor[] descriptors;

	private static final long serialVersionUID = 1;

	public static final String LAYOUT_PROP = "Shape.Layout";

	public static final String NAME_PROP = "Shape.Name";

	public static final String TYPE_PROP = "Shape.TYPE";

	public static final String CHILD_ADDED_PROP = "Shape.ChildAdded";
	public static final String CHILD_REMOVED_PROP = "Shape.ChildRemoved";

	public static final String SOURCE_CONNECTIONS_PROP = "Shape.SourceConn";

	public static final String TARGET_CONNECTIONS_PROP = "Shape.TargetConn";

	private List sourceConnections = new ArrayList();

	private List targetConnections = new ArrayList();

	public static final int LEFT_SIDE = 1;
	public static final int RIGHT_SIDE = 2;

	public static final int SMALLER_ELEMENT_HEIGHT = 40;
	public static final int SMALLER_ELEMENT_WIDTH = 200;
	public static final int SPACE_BETWEEN_ELEMENTS = 10;
	public static final int CEIL_SPACE = 30;

	public int sizeHeight;
	public int sizeWidth;

	private String type;
	private Shape shapeParent;
	private List<Shape> shapeChildren;
	private int side;
	private static List<Shape> shapes = new ArrayList<Shape>();

	private Rectangle layout;
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

	protected static Image createImage(String name) {
		InputStream stream = MappumPlugin.class.getResourceAsStream(name);
		Image image = new Image(null, stream);
		try {
			stream.close();
		} catch (IOException ioe) {
		}
		return image;
	}

	public Shape(String name, String type, Shape shapeParent, int side,
			CallNode shapeNode) {
		this.shapeNode = shapeNode;
		shapeChildren = new ArrayList<Shape>();
		this.layout = new Rectangle();
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
		return shapeParent.addChild(this);
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

	public Shape() {
		// TODO Auto-generated constructor stub
	}

	public static Shape createShape(String name, String type,
			Shape shapeParent, int side, CallNode shapeNode) {
//		for (int i = 0; i < Shape.getShapes().size(); i++) {
//			if (Shape.getShapes().get(i).getName().equals(name)
//					&& Shape.getShapes().get(i).getSide() == side) {
//				if (Shape.getShapes().get(i).getShapeParent() != null
//						&& Shape.getShapes().get(i).getShapeParent().equals(
//								shapeParent)) {
//					return Shape.getShapes().get(i);
//				}
//
//			}
//		}
		if (shapeParent == null) {
			for (Shape shape : Shape.getRootShapes()) {
				if (shape.getName().equals(name) && shape.getSide() == side)
					return shape;
			}
		} else {
			for (Shape child : shapeParent.getShapeChildren()) {
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

	public Image getIcon() {
		return RECTANGLE_ICON;
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

	public List getSourceConnections() {
		return new ArrayList(sourceConnections);
	}

	public List getTargetConnections() {
		return new ArrayList(targetConnections);

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

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Shape getShapeParent() {
		return shapeParent;
	}

	public void setShapeParent(Shape shapeParent) {
		this.shapeParent = shapeParent;
	}

	public void addShapeChild(Shape shape) {
		if (shape != null && shapeChildren.add(shape)) {
			firePropertyChange(CHILD_ADDED_PROP, null, shape);
		}
	}

	public List<Shape> getShapeChildren() {
		return shapeChildren;
	}

	public void setShapeChildren(List<Shape> shapeChildren) {
		this.shapeChildren = shapeChildren;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public Rectangle getLayout() {
		setLayout();
		return layout;
	}

	public void setLayout(Rectangle layout) {
		this.layout = layout;
		firePropertyChange(LAYOUT_PROP, null, layout);
	}

	public void setLayout() {

		int x = 0;
		int y = 0;

		int tmpDepth = 0;
		if (shapeParent != null) {
			for (Shape node : shapeParent.getShapeChildren()) {
				if (tmpDepth < getChildrenDepth((Shape) node)) {
					tmpDepth = getChildrenDepth((Shape) node);
				}
			}
		} else {
			tmpDepth = getChildrenDepth(this);
		}
		sizeWidth = SMALLER_ELEMENT_WIDTH + 2 * SPACE_BETWEEN_ELEMENTS
				* tmpDepth - SPACE_BETWEEN_ELEMENTS;
		sizeHeight = getHeight(this);
		if (shapeParent == null) {

			if (side == Shape.LEFT_SIDE) {
				x = 10;
				y = 40;
			} else {
				x = 500;
				y = 40;

			}

		} else {
			x = SPACE_BETWEEN_ELEMENTS;
			y = CEIL_SPACE;
			for (Shape node : shapeParent.getShapeChildren()) {
				if (((Shape) node).equals(this)) {
					break;
				} else {
					y = y + getHeight((Shape) node) + SPACE_BETWEEN_ELEMENTS;
				}
			}

		}
		setLayout(new Rectangle(x, y, sizeWidth, sizeHeight));
		logger.debug("Shape layout info: " + this.getName() + " height: "
				+ sizeHeight + " width:" + sizeWidth + " y:" + y + " x:" + x);
	}

	private static int getHeight(Shape shape) {
		int i = 0;
		if (shape.getShapeChildren().size() != 0) {
			for (Shape node : shape.getShapeChildren()) {
				i = i + getHeight((Shape) node);
			}
			i = i + CEIL_SPACE + shape.getShapeChildren().size()
					* SPACE_BETWEEN_ELEMENTS;
			return i;
		}
		return SMALLER_ELEMENT_HEIGHT;
	}

	private static int getChildrenDepth(Shape shape) {
		int i = 0;
		if (shape.getShapeChildren().size() != 0) {
			for (Shape node : shape.getShapeChildren()) {
				i = i + getChildrenDepth((Shape) node);
			}
			return ++i;
		}
		return 0;
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
					if (shapes.get(i).getShapeChildren().size() > 0) {
						for (int j = 0; j < shapes.get(i).getShapeChildren()
								.size(); j++) {
							removeShape(shapes.get(i).getShapeChildren().get(j));
						}
					}
					for (int j = 0; j < shape.getSourceConnections().size(); j++) {
						Connection.removeConnection((Connection) shape
								.getSourceConnections().get(j));
					}
					for (int j = 0; j < shape.getTargetConnections().size(); j++) {
						Connection.removeConnection((Connection) shape
								.getTargetConnections().get(j));
					}
					Shape.getShapes().remove(shape);
					shape.getShapeParent().removeChild(shape);
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
					+ shapeParent.getName() + " ||";
		else
			return "|| Variable: " + name + ", type: " + type
					+ ", no parent ||";

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
		if (shape.getShapeParent() != null) {
			initialShapeList.add(shape);
			if (!shape.getShapeParent().equals(Shape.getRootShapes().get(0))
					&& !shape.getShapeParent().equals(
							Shape.getRootShapes().get(1))) {
				checkShapeStack(shape.getShapeParent(), initialShapeList);
			}
		}
		return initialShapeList;
	}

}