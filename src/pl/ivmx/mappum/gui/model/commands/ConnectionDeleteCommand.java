/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *******************************************************************************/
package pl.ivmx.mappum.gui.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

/**
 * A command to disconnect (remove) a connection from its endpoints. The command
 * can be undone or redone.
 * 
 * @author Elias Volanakis
 */
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
		connection.reconnect();
		createRubyMapping();
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
	}

	private void removeRubbyMapping() {
		RootNodeHolder.getInstance().removeMapping(
				connection.getSource(),
				connection.getTarget(),
				Connection.translateSideFromIntToString(connection
						.getMappingSide()), connection.getComment());
	}
}
