package pl.ivmx.mappum.gui.model.commands;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class ConnectionCreateCommand extends Command {
	private Logger logger = Logger.getLogger(ConnectionCreateCommand.class);

	/** The connection instance. */
	private Connection connection;
	/** The desired line style for the connection (dashed or solid). */
	private final int mappingSide;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {

		// the same connection
		if (source.equals(target)) {
			return false;
		}

		if (source != null && target != null) {

			// the same side
			if (source.getSide() == target.getSide()) {

				return false;

			}
			// they are complex elements
			if (source.getShapeChildren().size() != 0
					|| target.getShapeChildren().size() != 0) {
				return false;
			}
		}
		// connection already exists
		for (Iterator<Connection> iter = source.getSourceConnections()
				.iterator(); iter.hasNext();) {
			if (iter.next().getTarget().equals(target)) {
				return false;
			}
		}
		return true;
	}

	public void execute() {
		// create a new connection between source and target
		if (source.getSide() == Shape.RIGHT_SIDE) {
			connection = new Connection(target, source, mappingSide,
					Connection.VAR_TO_VAR_CONN);
		}

		else {
			connection = new Connection(source, target, mappingSide,
					Connection.VAR_TO_VAR_CONN);
		}
		createRubyMapping();

	}

	public void redo() {
		connection.reconnect();
		createRubyMapping();
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

	private void createRubyMapping() {
		RootNodeHolder.getInstance().addMapping(
				connection.getSource(),
				connection.getTarget(),
				Connection.translateSideFromIntToString(connection
						.getMappingSide()), connection.getComment());

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
