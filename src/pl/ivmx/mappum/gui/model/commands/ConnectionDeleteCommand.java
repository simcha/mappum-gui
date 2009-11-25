package pl.ivmx.mappum.gui.model.commands;

import org.eclipse.gef.commands.Command;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class ConnectionDeleteCommand extends Command {

	/** Connection instance to disconnect. */
	private final Connection connection;

	/**
	 * Create a command that will disconnect a connection from its endpoints.
	 * 
	 * @param conn
	 *            the connection instance to disconnect (non-null)
	 * @throws IllegalArgumentException
	 *             if conn is null
	 */
	public ConnectionDeleteCommand(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}
		setLabel("connection deletion");
		this.connection = conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		removeRubbyMapping();
		connection.disconnect();
		// RootNodeHolder.getInstance().removeMapping(connection);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		createRubyMapping();
		connection.reconnect();

	}

	private void createRubyMapping() {
		if (connection.getArrayNumber() > -1) {
			RootNodeHolder.getInstance().addMapping(
					connection.getSource(),
					connection.getTarget(),
					Connection.translateSideFromIntToString(connection
							.getMappingSide()), connection.getComment(),
					connection.getArrayNumber());
		} else {
			RootNodeHolder.getInstance().addMapping(
					connection.getSource(),
					connection.getTarget(),
					Connection.translateSideFromIntToString(connection
							.getMappingSide()), connection.getComment(), 0);
		}

	}

	private void removeRubbyMapping() {
		if (connection.getArrayNumber() > -1) {
			RootNodeHolder.getInstance().removeMapping(
					connection.getSource(),
					connection.getTarget(),
					Connection.translateSideFromIntToString(connection
							.getMappingSide()), connection.getComment(),
					connection.getArrayNumber());
		} else {
			RootNodeHolder.getInstance().removeMapping(
					connection.getSource(),
					connection.getTarget(),
					Connection.translateSideFromIntToString(connection
							.getMappingSide()), connection.getComment(), null);
		}
	}
}
