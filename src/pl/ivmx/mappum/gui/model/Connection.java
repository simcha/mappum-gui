package pl.ivmx.mappum.gui.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.NewlineNode;

import pl.ivmx.mappum.gui.utils.ModelGenerator;
import pl.ivmx.mappum.gui.utils.Pair;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class Connection extends ModelElement {

	public static enum Type {
		CONST_TO_VAR_CONN, VAR_TO_VAR_CONN, FUN_TO_VAR_CONN
	}

	public static enum Side {
		LEFT_TO_RIGHT("From left variable to right variable mapping"), RIGHT_TO_LEFT(
				"From right variable to left variable mapping"), DUAL(
				"Dual side mapping");

		private final static Map<Integer, Side> lookup = new HashMap<Integer, Side>();
		private final String desc;

		private Side(final String desc) {
			this.desc = desc;
		}

		static {
			for (final Side s : EnumSet.allOf(Side.class)) {
				lookup.put(s.ordinal(), s);
			}
		}

		public static Side forOrdinal(final int ordinal) {
			return lookup.get(ordinal);
		}

		public String getDesc() {
			return desc;
		}
	}

	public static class Info {

		private final Connection.Side side;
		private final Connection.Type type;

		public Info(Side side, Type type) {
			this.side = side;
			this.type = type;
		}

		public Side getSide() {
			return side;
		}

		public Type getType() {
			return type;
		}
	}

	/** Property ID to use when the line style of this connection is modified. */
	public static final String MAPPING_PROP = "Connection.Mapping";
	private static final IPropertyDescriptor[] descriptors = new IPropertyDescriptor[3];
	public static final String COMMENT_PROP = "Connection.Comment";
	private static final String CODE_PROP = "Connection.code";
	private static final long serialVersionUID = 1;

	private Type connectionType;
	private String constantName;
	private ArrayList<String> functions = new ArrayList<String>();

	private boolean isConnected;
	private Shape source;
	private Shape target;
	private Side mappingSide;
	private String comment;

	private NewlineNode rubyCodeNode;

	private Logger logger = Logger.getLogger(Connection.class);

	private static List<Connection> connections = new ArrayList<Connection>();

	private int arrayNumber = -1;

	static {
		descriptors[0] = new ComboBoxPropertyDescriptor(MAPPING_PROP,
				"Mapping side", new String[] { Side.LEFT_TO_RIGHT.getDesc(),
						Side.RIGHT_TO_LEFT.getDesc(), Side.DUAL.getDesc() });
		descriptors[1] = new TextPropertyDescriptor(COMMENT_PROP, "Comment");
		descriptors[2] = new TextPropertyDescriptor(CODE_PROP, "Code");
	}

	/**
	 * Create a (solid) connection between two distinct shapes.
	 */
	public Connection(Shape source, Shape target, final Side side,
			final Type type) {
		this(source, target, side, type, (NewlineNode)null);
	}
	/**
	 * Create a (solid) connection between two distinct shapes.
	 */
	public Connection(Shape source, Shape target, final Side side,
			final Type type, final NewlineNode node) {
		this(source, target, side, type, -1, node);
	}

	/**
	 * Create a (solid) connection between two distinct shapes.
	 */
	public Connection(Shape source, Shape target, final Side side,
			final Type type, int arrayNumber) {
		this(source, target, side, type, arrayNumber, (NewlineNode)null);
	}
	
	/**
	 * Create a (solid) connection between two distinct shapes.
	 */
	public Connection(Shape source, Shape target, final Side side,
			final Type type, int arrayNumber, NewlineNode node) {
		this(source, target, side, type, arrayNumber, node, true);
	}	
	/**
	 * Create a (solid) connection between two distinct shapes.
	 */
	private Connection(Shape source, Shape target, final Side side,
			final Type type, int arrayNumber, NewlineNode rubySrcNode, boolean reconnect ) {
		this.arrayNumber = arrayNumber;
		connectionType = type;
		connections.add(this);
		source.addToParent();
		target.addToParent();
		this.comment = "";
		this.rubyCodeNode = rubySrcNode;
		if(reconnect) {
		  reconnect(source, target, side);
	    }
	}

	/**
	 * Disconnect this connection from the shapes it is attached to.
	 */
	public void disconnect() {
		if (isConnected) {
			source.removeConnection(this);
			target.removeConnection(this);
			isConnected = false;
		}
	}

	/**
	 * Returns the line drawing style of this connection.
	 * 
	 * @return an int value (Graphics.LINE_DASH or Graphics.LINE_SOLID)
	 */
	public Side getMappingSide() {
		return mappingSide;
	}

	private NewlineNode getMappingNode() {
		final RootNodeHolder h = RootNodeHolder.getInstance();
		return h.findMappingNode(this, h.findRootBlockNode(h.getRootNode()));
	}

	public String getMappingCode() {
		if (rubyCodeNode == null){
			rubyCodeNode = getMappingNode();
		}
		return ModelGenerator.getInstance().generateRubyCodeFromNode(
				rubyCodeNode);
	}

	/**
	 * Returns the descriptor for the lineStyle property
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	/**
	 * Returns the lineStyle as String for the Property Sheet
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
	 */
	public Object getPropertyValue(Object id) {
		if (id.equals(MAPPING_PROP)) {
			// DUAL_SIDE is the third value in the combo dropdown
			if (getMappingSide() == Side.DUAL)
				return 2;
			// FROM_RIGHT_TO_LEFT is the second value in the combo dropdown
			else if (getMappingSide() == Side.RIGHT_TO_LEFT)
				return 1;
			else if (getMappingSide() == Side.LEFT_TO_RIGHT)
				return 0;
			// Solid is the first value in the combo dropdown
			else
				return -1;
		} else if (id.equals(COMMENT_PROP)) {
			return comment;
		} else if (id.equals(CODE_PROP)) {
			return getMappingCode();
		}
		return super.getPropertyValue(id);
	}

	/**
	 * Returns the source endpoint of this connection.
	 * 
	 * @return a non-null Shape instance
	 */
	public Shape getSource() {
		return source;
	}

	/**
	 * Returns the target endpoint of this connection.
	 * 
	 * @return a non-null Shape instance
	 */
	public Shape getTarget() {
		return target;
	}

	/**
	 * Reconnect this connection. The connection will reconnect with the shapes
	 * it was previously attached to.
	 */
	public void reconnect() {
		if (!isConnected) {
			source.addConnection(this);
			target.addConnection(this);
			isConnected = true;

			logger.debug("Created or reconnected connection:" + this);
		}
	}

	/**
	 * Reconnect to a different source and/or target shape. The connection will
	 * disconnect from its current attachments and reconnect to the new source
	 * and target.
	 * 
	 * @param newSource
	 *            a new source endpoint for this connection (non null)
	 * @param newTarget
	 *            a new target endpoint for this connection (non null)
	 * @throws IllegalArgumentException
	 *             if any of the paramers are null or newSource == newTarget
	 */
	public void reconnect(Shape newSource, Shape newTarget, final Side side) {
		if (newSource == null || newTarget == null || newSource == newTarget) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.source = newSource;
		this.target = newTarget;
		this.mappingSide = side;
		reconnect();
	}

	public void setMappingSide(final Side mappingSide) {
		if (mappingSide != Side.DUAL && mappingSide != Side.LEFT_TO_RIGHT
				&& mappingSide != Side.RIGHT_TO_LEFT) {
			throw new IllegalArgumentException();
		}
		this.mappingSide = mappingSide;
		firePropertyChange(MAPPING_PROP, null, this.mappingSide);
	}

	/**
	 * Sets the mapping based on the String provided by the PropertySheet
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void setPropertyValue(final Object id, final Object value) {
		if (id.equals(MAPPING_PROP)) {
			final Side side = Side.forOrdinal((Integer) value);
			RootNodeHolder.getInstance().changeMappingAtributes(this,
					translateSideFromIntToString(side), null);
			setMappingSide(side);
		} else if (id.equals(COMMENT_PROP)) {
			if (value instanceof String) {
				RootNodeHolder.getInstance().changeMappingAtributes(this, null,
						(String) value);
				setComment((String) value);
			}
		} else
			super.setPropertyValue(id, value);

	}

	public static Side translateSideFromStringToInt(String side) {
		if (side.equals("<=>")) {
			return Side.DUAL;
		} else if (side.equals("<<")) {
			return Side.RIGHT_TO_LEFT;
		} else if (side.equals(">>")) {
			return Side.LEFT_TO_RIGHT;
		} else {
			return null;
		}
	}

	public static String translateSideFromIntToString(final Side side) {
		if (side == Side.DUAL) {
			return "<=>";
		} else if (side == Side.RIGHT_TO_LEFT) {
			return "<<";
		} else if (side == Side.LEFT_TO_RIGHT) {
			return ">>";
		} else {
			return "WRONG MAPPING";
		}
	}

	public NewlineNode getRubyCodeNode() {
		if (rubyCodeNode == null){
			rubyCodeNode = getMappingNode();
		}
		return rubyCodeNode;
	}

	public void setRubyCodeNode(NewlineNode rubyCodeNode) {
		this.rubyCodeNode = rubyCodeNode;
	}

	public static List<Connection> getConnections() {
		return connections;
	}

	public static Connection getConnection(Connection connection) {
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).equals(connection)) {
				return connections.get(i);
			}
		}
		return null;
	}

	public static boolean removeConnection(Connection connection) {
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).equals(connection)) {
				connections.get(i).getSource().removeConnection(
						connections.get(i));
				connections.get(i).getTarget().removeConnection(
						connections.get(i));
				connections.get(i).disconnect();
				connections.remove(i);
				connection = null;
				return true;
			}
		}
		return false;
	}

	public void refreshMappingNode() {
		CallNode nextNode = (CallNode)rubyCodeNode.getNextNode();
		nextNode.setName(Connection
				.translateSideFromIntToString(mappingSide));

		CallNode receiverChild = (CallNode) (nextNode).getReceiverNode();
		receiverChild.setName(source.getName());
		CallNode senderChild = (CallNode) (nextNode).getArgsNode()
				.childNodes().get(0);
		senderChild.setName(target.getName());

	}

	@Override
	public String toString() {
		return "|| Connection: left side = " + source.getName()
				+ ", right side = " + target.getName() + ", side = "
				+ mappingSide + " ||";
	}

	public static boolean connectionNotExists(Pair mapping) {
		// ModelGenerator.setLastUsedElementNumberInArray(-1);
		boolean result = true;
		for (Connection tmpConnetion : Connection.getConnections()) {
			if (tmpConnetion.getSource().getName().equals(
					mapping.getLeftShape().getName())
					&& tmpConnetion.getTarget().getName().equals(
							mapping.getRightShape().getName())) {

				if (tmpConnetion.getSource().getShapeStack().size() == mapping
						.getLeftShape().getShapeStack().size()
						&& tmpConnetion.getTarget().getShapeStack().size() == mapping
								.getRightShape().getShapeStack().size()) {
					boolean firstLeftArrayElementFinded = false;
					boolean firstRightArrayElementFinded = false;
					boolean leftMatch = false;
					boolean rightMatch = false;
					int elementNumber;
					for (int i = 0; i < tmpConnetion.getSource()
							.getShapeStack().size(); i++) {

						if (tmpConnetion.getSource().getShapeStack().get(i)
								.getName().equals(
										mapping.getLeftShape().getShapeStack()
												.get(i).getName())) {
							if (!firstLeftArrayElementFinded
									&& (mapping.getLeftShape().getShapeStack()
											.get(i).isArrayType() && mapping
											.getLeftShape().getShapeStack()
											.get(i).getArrayCounters().size() > 0)) {
								elementNumber = mapping.getLeftShape()
										.getShapeStack().get(i)
										.getArrayCounters().get(
												mapping.getLeftShape()
														.getShapeStack().get(i)
														.getArrayCounters()
														.size() - 1);
								if (elementNumber == tmpConnetion
										.getArrayNumber()) {
									leftMatch = true;
								}
								firstLeftArrayElementFinded = true;
							}
							result = false;
						} else {
							result = true;
							break;
						}

					}

					for (int i = 0; i < tmpConnetion.getTarget()
							.getShapeStack().size(); i++) {

						if (tmpConnetion.getTarget().getShapeStack().get(i)
								.getName().equals(
										mapping.getRightShape().getShapeStack()
												.get(i).getName())) {
							if (!firstRightArrayElementFinded
									&& (mapping.getRightShape().getShapeStack()
											.get(i).isArrayType() && mapping
											.getRightShape().getShapeStack()
											.get(i).getArrayCounters().size() > 0)) {
								elementNumber = mapping.getRightShape()
										.getShapeStack().get(i)
										.getArrayCounters().get(
												mapping.getRightShape()
														.getShapeStack().get(i)
														.getArrayCounters()
														.size() - 1);
								if (elementNumber == tmpConnetion
										.getArrayNumber()) {
									rightMatch = true;
								}
								firstRightArrayElementFinded = true;
							}
							result = false;
						} else {
							result = true;
							break;
						}

					}
					if (result == false) {
						if (firstLeftArrayElementFinded != firstRightArrayElementFinded) {
							if (leftMatch || rightMatch)
								return false;
							else
								result = true;
						} else {
							return false;
						}
					}
				}
				result = true;
			}
		}
		return result;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setConstantName(String constantName) {
		this.constantName = constantName;
	}

	public String getConstantName() {
		return constantName;
	}

	public Type getConnectionType() {
		return connectionType;
	}

	public void setFunctions(ArrayList<String> functions) {
		this.functions = functions;
	}

	public void addFunction(String function) {
		this.functions.add(function);
	}

	public ArrayList<String> getFunctions() {
		return functions;
	}

	public void setArrayNumber(int arrayNumber) {
		this.arrayNumber = arrayNumber;
	}

	public int getArrayNumber() {
		return arrayNumber;
	}

	public String getFullSourceName() {
		return getFullName(source);
	}

	public String getFullTargetName() {
		return getFullName(target);
	}

	private String getFullName(final Shape shape) {
		StringBuilder sb = new StringBuilder();
		Shape currentShape = shape;
		while (currentShape != null && currentShape.getParent() != null) {
			if (sb.length() > 0) {
				sb.insert(0, '.');
			}
			sb.insert(0, currentShape.getName());
			currentShape = currentShape.getParent();
		}
		return sb.toString();
	}
}