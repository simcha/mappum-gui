package pl.ivmx.mappum.gui.model.commands;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.commands.Command;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.utils.ModelGenerator;
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
		for (Iterator iter = source.getSourceConnections().iterator(); iter
				.hasNext();) {
			Connection conn = (Connection) iter.next();
			if (conn.getTarget().equals(target)) {
				return false;
			}
		}
		return true;
	}

	public void execute() {
		// create a new connection between source and target
		if (source.getSide() == Shape.RIGHT_SIDE) {
			connection = new Connection(target, source, mappingSide);
		}

		else {
			connection = new Connection(source, target, mappingSide);
		}

		RootNodeHolder.getInstance().addMapping(
				connection.getSource(),
				connection.getTarget(),
				Connection.translateSideFromIntToString(connection
						.getMappingSide()), connection.getComment());

		// TEST GENERACJI KODU
		String code = "ERROR WHILE GENERATING CODE";
		try {
			code = new String("GENERATED CODE: "
					+ ModelGenerator.getInstance().generateRubyCode());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Generated ruby code"+code);
		System.out.println(RootNodeHolder.getInstance().findMappingPath(source, target));
		// *
	}

	public void redo() {
		connection.reconnect();
	}

	public void setTarget(Shape target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.target = target;
	}

	public void undo() {
		connection.disconnect();
	}
}
