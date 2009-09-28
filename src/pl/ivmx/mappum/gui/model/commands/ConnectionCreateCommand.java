package pl.ivmx.mappum.gui.model.commands;

import java.util.List;

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
	private int mappingSide;

	/** Start endpoint for the connection. */
	private final Shape source;
	/** Target endpoint for the connection. */
	private Shape target;

	public ConnectionCreateCommand(Shape source, int mappingSide) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		setLabel("connection creation");
		this.source = source;
		this.mappingSide = mappingSide;
	}

	private boolean connected(final Shape a, final Shape b) {
		return connectedChildren(a, b) || connectedParents(a, b);
	}

	private boolean connectedChildren(final Shape a, final Shape b) {
		if (connected0(a, b, false)) {
			return true;
		}
		for (final Shape s : a.getChildren()) {
			if (connectedChildren(b, s)) {
				return true;
			}
			if (connectedChildren(s, b)) {
				return true;
			}
		}
		return false;
	}

	private boolean connectedParents(final Shape a, final Shape b) {
		Shape aParent = a;
		while (aParent != null) {
			if (connected0(aParent, b, true)) {
				return true;
			}
			aParent = aParent.getParent();
		}
		return false;
	}

	private boolean connected0(final Shape a, final Shape b,
			final boolean checkParents) {
		if (connected1(b, a.getSourceConnections(), true, checkParents)
				|| connected1(b, a.getTargetConnections(), false, checkParents)) {
			return true;
		}
		return false;
	}

	private boolean connected1(final Shape b,
			final List<Connection> connections, final boolean sourceSide,
			final boolean checkParents) {
		Shape bParent = b;
		for (final Connection c : connections) {
			while (bParent != null) {
				if ((sourceSide && c.getTarget().equals(bParent))
						|| c.getSource().equals(bParent)) {
					return true;
				}
				if (checkParents) {
					bParent = bParent.getParent();
				} else {
					bParent = null;
				}
			}
		}
		return false;
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
			return !connected(source, target);
		}
		return true;
	}

	public void execute() {

		if (source.getSide() == Shape.Side.RIGHT
				&& mappingSide == Connection.FROM_LEFT_TO_RIGHT) {
			mappingSide = Connection.FROM_RIGHT_TO_LEFT;
		}
		// create a new connection between source and target
		if (source.getSide() == Shape.Side.RIGHT) {
			createRubyMapping(target, source, mappingSide, null);
			connection = new Connection(target, source, mappingSide,
					Connection.Type.VAR_TO_VAR_CONN);
		}

		else {
			createRubyMapping(source, target, mappingSide, null);
			connection = new Connection(source, target, mappingSide,
					Connection.Type.VAR_TO_VAR_CONN);
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

	private void createRubyMapping(Shape source, Shape target, int mappingSide,
			String comment) {
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
