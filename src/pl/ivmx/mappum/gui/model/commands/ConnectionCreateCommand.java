package pl.ivmx.mappum.gui.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
				NewlineNode node = createRubyMapping(target, source, mappingSide, null, 0);
				connection = new Connection(target, source, mappingSide,
						connectionInfo.getType(), 0);
				connection.setRubyCodeNode(node);
			} else {
				NewlineNode node = createRubyMapping(target, source, mappingSide, null, null);
				connection = new Connection(target, source, mappingSide,
						connectionInfo.getType());
				connection.setRubyCodeNode(node);
			}
		}

		else {
			
			if (source.isArrayType() != target.isArrayType()) {
				NewlineNode node = createRubyMapping(source, target, mappingSide, null, 0);
				connection = new Connection(source, target, mappingSide,
						connectionInfo.getType(), 0);
				connection.setRubyCodeNode(node);
			} else {
				NewlineNode node = createRubyMapping(source, target, mappingSide, null, null);
				connection = new Connection(source, target, mappingSide,
						connectionInfo.getType());
				connection.setRubyCodeNode(node);
			}
		}
	}

	public void redo() {
		NewlineNode node = createRubyMapping(connection.getSource(), connection.getTarget(),
				connection.getMappingSide(), connection.getComment(), connection.getArrayNumber());
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

	private NewlineNode createRubyMapping(Shape source, Shape target,
			final Connection.Side mappingSide, String comment, Integer arrayNumber) {
		NewlineNode node = RootNodeHolder.getInstance().addMapping(source, target,
				Connection.translateSideFromIntToString(mappingSide), comment, arrayNumber);

		String viewId = "org.eclipse.ui.views.PropertySheet";

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(viewId);
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return node;
	}

	private void removeRubbyMapping() {
		if(connection.getArrayNumber()>-1){
			RootNodeHolder.getInstance().removeMapping(
					connection.getSource(),
					connection.getTarget(),
					Connection.translateSideFromIntToString(connection
							.getMappingSide()), connection.getComment(), connection.getArrayNumber());
		}else{
			RootNodeHolder.getInstance().removeMapping(
					connection.getSource(),
					connection.getTarget(),
					Connection.translateSideFromIntToString(connection
							.getMappingSide()), connection.getComment(), null);
		}

	}
}
