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
package pl.ivmx.mappum.gui.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.ModelElement;
import pl.ivmx.mappum.gui.model.commands.ConnectionDeleteCommand;

/**
 * Edit part for Connection model elements.
 * <p>
 * This edit part must implement the PropertyChangeListener interface, so it can
 * be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 */
class ConnectionEditPart extends AbstractConnectionEditPart implements
		PropertyChangeListener {

	/**
	 * Upon activation, attach to the model element as a property change
	 * listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		// Selection handle edit policy.
		// Makes the connection show a feedback, when selected by the user.
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
		// Allows the removal of the connection model element
		installEditPolicy(EditPolicy.CONNECTION_ROLE,
				new ConnectionEditPolicy() {
					protected Command getDeleteCommand(GroupRequest request) {
						return new ConnectionDeleteCommand(getCastedModel());
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		PolylineConnection connection = (PolylineConnection) super
				.createFigure();
		if (getCastedModel().getMappingSide() == Connection.DUAL_SIDE) {
			connection.setTargetDecoration(new PolygonDecoration()); // arrow at
			// target
			// endpoint
			connection.setSourceDecoration(new PolygonDecoration());
		} else if (getCastedModel().getMappingSide() == Connection.FROM_LEFT_TO_RIGHT) {
			connection.setTargetDecoration(new PolygonDecoration());
		} else {
			connection.setSourceDecoration(new PolygonDecoration());
		}
		return connection;
	}

	/**
	 * Upon deactivation, detach from the model element as a property change
	 * listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	private Connection getCastedModel() {
		return (Connection) getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getPropertyName();
		if (Connection.MAPPING_PROP.equals(property)) {
			if (getCastedModel().getMappingSide() == Connection.DUAL_SIDE) {
				((PolylineConnection) getFigure())
						.setTargetDecoration(new PolygonDecoration());
				((PolylineConnection) getFigure())
						.setSourceDecoration(new PolygonDecoration());
			} else if (getCastedModel().getMappingSide() == Connection.FROM_LEFT_TO_RIGHT) {
				((PolylineConnection) getFigure())
						.setTargetDecoration(new PolygonDecoration());
				((PolylineConnection) getFigure()).setSourceDecoration(null);
			} else {
				((PolylineConnection) getFigure()).setTargetDecoration(null);
				((PolylineConnection) getFigure())
						.setSourceDecoration(new PolygonDecoration());
			}
		}
	}
}