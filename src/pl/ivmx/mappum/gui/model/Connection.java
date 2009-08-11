package pl.ivmx.mappum.gui.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jrubyparser.ast.CallNode;

import pl.ivmx.mappum.gui.utils.Pair;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class Connection extends ModelElement {

	/** Property ID to use when the line style of this connection is modified. */
	public static final String MAPPING_PROP = "Connection.Mapping";
	private static final IPropertyDescriptor[] descriptors = new IPropertyDescriptor[2];
	private static final String FROM_LEFT_TO_RIGHT_STR = "From left variable to right variable mapping";
	private static final String FROM_RIGHT_TO_LEFT_STR = "From right variable to left variable mapping";
	private static final String DUAL_SIDE_STR = "Dual side mapping";
	private static final String COMMENT_PROP = "Connection.comment";
	public static final int FROM_LEFT_TO_RIGHT = 0;
	public static final int FROM_RIGHT_TO_LEFT = 1;
	public static final int DUAL_SIDE = 2;
	private static final long serialVersionUID = 1;

	private boolean isConnected;
	private Shape source;
	private Shape target;
	private int mappingSide;
	private String comment;

	private CallNode rubyCodeNode;

	private Logger logger = Logger.getLogger(Connection.class);

	private static List<Connection> connections = new ArrayList<Connection>();

	static {
		descriptors[0] = new ComboBoxPropertyDescriptor(MAPPING_PROP,
				MAPPING_PROP, new String[] { FROM_LEFT_TO_RIGHT_STR,
						FROM_RIGHT_TO_LEFT_STR, DUAL_SIDE_STR });
		descriptors[1] = new TextPropertyDescriptor(COMMENT_PROP, "Comment");
	}

	/**
	 * Create a (solid) connection between two distinct shapes.
	 */
	public Connection(Shape source, Shape target, int side) {
		connections.add(this);
		source.addToParent();
		target.addToParent();
		this.comment = "";
		reconnect(source, target, side);
	}

	public Connection(Shape source, Shape target, int side, String comment) {
		connections.add(this);
		this.comment = comment;
		reconnect(source, target, side);
	}

	public Connection() {
		// TODO Auto-generated constructor stub
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
	public int getMappingSide() {
		return mappingSide;
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
			if (getMappingSide() == DUAL_SIDE)
				return 2;
			// FROM_RIGHT_TO_LEFT is the second value in the combo dropdown
			else if (getMappingSide() == FROM_RIGHT_TO_LEFT)
				return 1;
			else if (getMappingSide() == FROM_LEFT_TO_RIGHT)
				return 0;
			// Solid is the first value in the combo dropdown
			else
				return -1;
		} else if (id.equals(COMMENT_PROP)) {
			return comment;
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
	public void reconnect(Shape newSource, Shape newTarget, int side) {
		if (newSource == null || newTarget == null || newSource == newTarget) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.source = newSource;
		this.target = newTarget;
		this.mappingSide = side;
		reconnect();
	}

	public void setMappingSide(int mappingSide) {
		if (mappingSide != Connection.DUAL_SIDE
				&& mappingSide != Connection.FROM_LEFT_TO_RIGHT
				&& mappingSide != Connection.FROM_RIGHT_TO_LEFT) {
			throw new IllegalArgumentException();
		}
		this.mappingSide = mappingSide;
		firePropertyChange(MAPPING_PROP, null, new Integer(this.mappingSide));
	}

	/**
	 * Sets the mapping based on the String provided by the PropertySheet
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(MAPPING_PROP)) {
			RootNodeHolder.getInstance().changeMappingAtributes(this,
					translateSideFromIntToString((Integer) value), null);
			if (value.equals(new Integer(2))) {
				setMappingSide(DUAL_SIDE);
			} else if (value.equals(new Integer(1))) {
				setMappingSide(FROM_RIGHT_TO_LEFT);
			} else if (value.equals(new Integer(0))) {
				setMappingSide(FROM_LEFT_TO_RIGHT);
			}
		} else if (id.equals(COMMENT_PROP)) {
			if (value instanceof String) {
				RootNodeHolder.getInstance().changeMappingAtributes(this, null,
						(String) value);
				setComment((String) value);
			}
		} else
			super.setPropertyValue(id, value);

	}

	public static int translateSideFromStringToInt(String side) {
		if (side.equals("<=>")) {
			return Connection.DUAL_SIDE;
		} else if (side.equals("<<")) {
			return Connection.FROM_RIGHT_TO_LEFT;
		} else if (side.equals(">>")) {
			return Connection.FROM_LEFT_TO_RIGHT;
		} else {
			return -1;
		}
	}

	public static String translateSideFromIntToString(int side) {
		if (side == Connection.DUAL_SIDE) {
			return "<=>";
		} else if (side == Connection.FROM_RIGHT_TO_LEFT) {
			return "<<";
		} else if (side == Connection.FROM_LEFT_TO_RIGHT) {
			return ">>";
		} else {
			return "WRONG MAPPING";
		}
	}

	public CallNode getRubyCodeNode() {
		return rubyCodeNode;
	}

	public void setRubyCodeNode(CallNode rubyCodeNode) {
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
		CallNode oldRubyCodeNode = rubyCodeNode;
		rubyCodeNode.setName(Connection
				.translateSideFromIntToString(mappingSide));

		CallNode receiverChild = (CallNode) ((CallNode) rubyCodeNode)
				.getReceiverNode();
		receiverChild.setName(source.getName());
		CallNode senderChild = (CallNode) ((CallNode) rubyCodeNode)
				.getArgsNode().childNodes().get(0);
		senderChild.setName(target.getName());

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "|| Connection: left side = " + source.getName()
				+ ", right side = " + target.getName() + ", side = "
				+ mappingSide + " ||";
	}

	public static boolean connectionNotExists(Pair mapping, Pair parents) {
		for (Connection tmpConnetion : Connection.getConnections()) {
			if (tmpConnetion.getSource().getName().equals(
					mapping.getLeftShape().getName())
					&& tmpConnetion.getTarget().getName().equals(
							mapping.getRightShape().getName())) {

				if (tmpConnetion.getSource().getShapeStack().size() == mapping
						.getLeftShape().getShapeStack().size()
						&& tmpConnetion.getTarget().getShapeStack().size() == mapping
								.getRightShape().getShapeStack().size()) {

					for (int i = 0; i < tmpConnetion.getSource()
							.getShapeStack().size(); i++) {

						if (!tmpConnetion.getSource().getShapeStack().get(i)
								.getName().equals(
										mapping.getLeftShape().getShapeStack()
												.get(i).getName())) {
							return true;
						}

					}

					for (int i = 0; i < tmpConnetion.getTarget()
							.getShapeStack().size(); i++) {

						if (!tmpConnetion.getTarget().getShapeStack().get(i)
								.getName().equals(
										mapping.getRightShape().getShapeStack()
												.get(i).getName())) {
							return true;
						}

					}
				}
				return false;
			}
		}
		return true;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}