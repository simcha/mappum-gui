package pl.ivmx.mappum.gui.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

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
			createRubyMapping(target, source, mappingSide, null);
			if (source.isArrayType() != target.isArrayType()) {
				connection = new Connection(target, source, mappingSide,
						connectionInfo.getType(), 0);
			} else {
				connection = new Connection(target, source, mappingSide,
						connectionInfo.getType());
			}
		}

		else {
			createRubyMapping(source, target, mappingSide, null);
			if (source.isArrayType() != target.isArrayType()) {
				connection = new Connection(source, target, mappingSide,
						connectionInfo.getType(), 0);
			} else {
				connection = new Connection(source, target, mappingSide,
						connectionInfo.getType());
			}
		}
	}

	public void redo() {
		createRubyMapping(connection.getSource(), connection.getTarget(),
				connection.getMappingSide(), connection.getComment());
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

	private void createRubyMapping(Shape source, Shape target,
			final Connection.Side mappingSide, String comment) {
		RootNodeHolder.getInstance().addMapping(source, target,
				Connection.translateSideFromIntToString(mappingSide), comment);

		String viewId = "org.eclipse.ui.views.PropertySheet";

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(viewId);
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(RootNodeHolder.getInstance().findMappingPath(source,
				target));
	}

	private void removeRubbyMapping() {
		RootNodeHolder.getInstance().removeMapping(
				connection.getSource(),
				connection.getTarget(),
				Connection.translateSideFromIntToString(connection
						.getMappingSide()), connection.getComment());
	}
}
