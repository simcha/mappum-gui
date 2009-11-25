package pl.ivmx.mappum.gui.model.commands;

import java.util.Iterator;

import org.eclipse.gef.commands.Command;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class ConnectionReconnectCommand extends Command {

	/** The connection instance to reconnect. */
	private Connection connection;
	/** The new source endpoint. */
	private Shape newSource;
	/** The new target endpoint. */
	private Shape newTarget;
	/** The original source endpoint. */
	private final Shape oldSource;
	/** The original target endpoint. */
	private final Shape oldTarget;
	private final String oldComment;
	private final Connection.Side oldSide;
	private Integer oldArrayNumber;

	/**
	 * Instantiate a command that can reconnect a Connection instance to a
	 * different source or target endpoint.
	 * 
	 * @param conn
	 *            the connection instance to reconnect (non-null)
	 * @throws IllegalArgumentException
	 *             if conn is null
	 */
	public ConnectionReconnectCommand(Connection conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}
		this.connection = conn;
		this.oldSource = conn.getSource();
		this.oldTarget = conn.getTarget();
		this.oldComment = conn.getComment();
		this.oldSide = conn.getMappingSide();
		if (conn.getArrayNumber() > -1)
			this.oldArrayNumber = conn.getArrayNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		if (connection.getConnectionType() == Connection.Type.CONST_TO_VAR_CONN) {
			return false;
		}
		if (newSource != null) {
			return checkSourceReconnection();
		} else if (newTarget != null) {
			return checkTargetReconnection();
		}
		return false;
	}

	/**
	 * Return true, if reconnecting the connection-instance to newSource is
	 * allowed.
	 */
	private boolean checkSourceReconnection() {
		// connection endpoints must be different Shapes
		if (newSource.equals(oldTarget)) {
			return false;
		}
		if (newSource != null && oldTarget != null) {
			// the same side
			if (newSource.getSide() == oldTarget.getSide()) {
				return false;
			}
			// they are classes
			if (newSource.getParent() == null || oldTarget.getParent() == null) {
				return false;
			}
		}

		// return false, if the connection exists already
		for (final Iterator<Connection> iter = newSource.getSourceConnections()
				.iterator(); iter.hasNext();) {
			final Connection conn = iter.next();
			// return false if a newSource -> oldTarget connection exists
			// already
			// and it is a different instance than the connection-field
			if (conn.getTarget().equals(oldTarget) && !conn.equals(connection)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return true, if reconnecting the connection-instance to newTarget is
	 * allowed.
	 */
	private boolean checkTargetReconnection() {
		// connection endpoints must be different Shapes
		if (newTarget.equals(oldSource)) {
			return false;
		}
		if (newTarget != null && oldSource != null) {
			// the same side
			if (newTarget.getSide() == oldSource.getSide()) {
				return false;
			}
			// they are complex elements
			if (newTarget.getChildren().size() != 0
					|| oldSource.getChildren().size() != 0) {
				return false;
			}
		}
		// return false, if the connection exists already
		for (final Iterator<Connection> iter = newTarget.getTargetConnections()
				.iterator(); iter.hasNext();) {
			final Connection conn = iter.next();
			// return false if a oldSource -> newTarget connection exists
			// already
			// and it is a differenct instance that the connection-field
			if (conn.getSource().equals(oldSource) && !conn.equals(connection)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reconnect the connection to newSource (if setNewSource(...) was invoked
	 * before) or newTarget (if setNewTarget(...) was invoked before).
	 */
	public void execute() {
		if (newSource != null) {
			removeRubbyMapping(oldSource, oldTarget, oldSide, oldComment, oldArrayNumber);
			if (newSource.isArrayType() && !oldTarget.isArrayType()){
				createRubyMapping(newSource, oldTarget, oldSide, oldComment, 0);
			}else{
				createRubyMapping(newSource, oldTarget, oldSide, oldComment, null);
			}
			connection.reconnect(newSource, oldTarget, oldSide);

		} else if (newTarget != null) {
			removeRubbyMapping(oldSource, oldTarget, oldSide, oldComment, oldArrayNumber);
			if(newTarget.isArrayType() && !oldSource.isArrayType()){
				createRubyMapping(oldSource, newTarget, oldSide, oldComment, 0);
			}else{
				createRubyMapping(oldSource, newTarget, oldSide, oldComment, null);
			}
			connection.reconnect(oldSource, newTarget, oldSide);
		} else {
			throw new IllegalStateException("Should not happen");
		}
	}

	/**
	 * Set a new source endpoint for this connection. When execute() is invoked,
	 * the source endpoint of the connection will be attached to the supplied
	 * Shape instance.
	 * <p>
	 * Note: Calling this method, deactivates reconnection of the <i>target</i>
	 * endpoint. A single instance of this command can only reconnect either the
	 * source or the target endpoint.
	 * </p>
	 * 
	 * @param connectionSource
	 *            a non-null Shape instance, to be used as a new source endpoint
	 * @throws IllegalArgumentException
	 *             if connectionSource is null
	 */
	public void setNewSource(Shape connectionSource) {
		if (connectionSource == null) {
			throw new IllegalArgumentException();
		}
		setLabel("move connection startpoint");
		newSource = connectionSource;
		newTarget = null;
	}

	/**
	 * Set a new target endpoint for this connection When execute() is invoked,
	 * the target endpoint of the connection will be attached to the supplied
	 * Shape instance.
	 * <p>
	 * Note: Calling this method, deactivates reconnection of the <i>source</i>
	 * endpoint. A single instance of this command can only reconnect either the
	 * source or the target endpoint.
	 * </p>
	 * 
	 * @param connectionTarget
	 *            a non-null Shape instance, to be used as a new target endpoint
	 * @throws IllegalArgumentException
	 *             if connectionTarget is null
	 */
	public void setNewTarget(Shape connectionTarget) {
		if (connectionTarget == null) {
			throw new IllegalArgumentException();
		}
		setLabel("move connection endpoint");
		newSource = null;
		newTarget = connectionTarget;
	}

	/**
	 * Reconnect the connection to its original source and target endpoints.
	 */
	public void undo() {
		if (connection.getArrayNumber() > -1)
			removeRubbyMapping(connection.getSource(), connection.getTarget(),
					connection.getMappingSide(), connection.getComment(),
					connection.getArrayNumber());
		else
			removeRubbyMapping(connection.getSource(), connection.getTarget(),
					connection.getMappingSide(), connection.getComment(), null);
		connection.reconnect(oldSource, oldTarget, oldSide);
		connection.setComment(oldComment);
		createRubyMapping(oldSource, oldTarget, oldSide, oldComment,
				oldArrayNumber);
	}

	private void createRubyMapping(Shape source, Shape target,
			final Connection.Side side, String comment, Integer arrayNumber) {
		RootNodeHolder.getInstance().addMapping(source, target,
				Connection.translateSideFromIntToString(side), comment,
				arrayNumber);

	}

	private void removeRubbyMapping(Shape source, Shape target,
			final Connection.Side side, String comment, Integer arrayNumber) {
		RootNodeHolder.getInstance().removeMapping(source, target,
				Connection.translateSideFromIntToString(side), comment,
				arrayNumber);
	}

}
