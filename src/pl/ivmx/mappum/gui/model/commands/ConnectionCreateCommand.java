package pl.ivmx.mappum.gui.model.commands;

import org.eclipse.gef.commands.Command;
import org.jrubyparser.ast.NewlineNode;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class ConnectionCreateCommand extends Command {
	// private Logger logger = Logger.getLogger(ConnectionCreateCommand.class);

	/** The connection instance. */
	private Connection connection;
	/** The desired line style for the connection (dashed or solid). */
	private final Connection.Info connectionInfo;

	/** Start endpoint for the connection. */
	private final Shape source;
	/** Target endpoint for the connection. */
	private Shape target;

	public ConnectionCreateCommand(final Shape source,
			final Connection.Info info) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		setLabel("connection creation");
		this.source = source;
		this.connectionInfo = info;
	}

	@Override
	public boolean canExecute() {

		assert source != null;

		// the same connection
		if (source.equals(target)) {
			return false;
		}

		if (target != null) {
			// the same side
			if (source.getSide() == target.getSide()) {
				return false;
			}
		}
		return true;
	}

	public void execute() {

		Connection.Side mappingSide = connectionInfo.getSide();

		if (source.getSide() == Shape.Side.RIGHT
				&& mappingSide == Connection.Side.LEFT_TO_RIGHT) {
			mappingSide = Connection.Side.RIGHT_TO_LEFT;
		}

		// create a new connection between source and target
		if (source.getSide() == Shape.Side.RIGHT) {
			
			if (source.isArrayType() != target.isArrayType()) {
				NewlineNode node1 = RootNodeHolder.getInstance().addMapping(target, source,
				Connection.translateSideFromIntToString(mappingSide), null, 0);
				NewlineNode node = node1;
				connection = new Connection(target, source, mappingSide,
						connectionInfo.getType(), 0);
				connection.setRubyCodeNode(node);
			} else {
				NewlineNode node1 = RootNodeHolder.getInstance().addMapping(target, source,
				Connection.translateSideFromIntToString(mappingSide), null, null);
				NewlineNode node = node1;
				connection = new Connection(target, source, mappingSide,
						connectionInfo.getType());
				connection.setRubyCodeNode(node);
			}
		}

		else {
			
			if (source.isArrayType() != target.isArrayType()) {
				NewlineNode node1 = RootNodeHolder.getInstance().addMapping(source, target,
				Connection.translateSideFromIntToString(mappingSide), null, 0);
				NewlineNode node = node1;
				connection = new Connection(source, target, mappingSide,
						connectionInfo.getType(), 0, node);
			} else {
				NewlineNode node1 = RootNodeHolder.getInstance().addMapping(source, target,
				Connection.translateSideFromIntToString(mappingSide), null, null);
				NewlineNode node = node1;
				connection = new Connection(source, target, mappingSide,
						connectionInfo.getType(), node);
			}
		}
	}

	public void redo() {
		NewlineNode node = RootNodeHolder.getInstance().addMapping(
				connection.getSource(),
				connection.getTarget(),
				Connection.translateSideFromIntToString(connection
						.getMappingSide()), connection.getComment(),
				connection.getArrayNumber());
		connection.setRubyCodeNode(node);
		connection.reconnect();
	}

	public void setTarget(Shape target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.target = target;
	}

	public void undo() {
		removeRubbyMapping();
		connection.disconnect();
	}

	private void removeRubbyMapping() {
		RootNodeHolder.getInstance().removeMapping(connection.getRubyCodeNode());
	}
}
