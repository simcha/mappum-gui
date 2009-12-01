package pl.ivmx.mappum.gui.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import pl.ivmx.mappum.gui.IMappumEditor;
import pl.ivmx.mappum.gui.ImageFactory;
import pl.ivmx.mappum.gui.MappumEditorPaletteFactory;
import pl.ivmx.mappum.gui.figure.ShapeFigure;
import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.ModelElement;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.commands.ConnectionCreateCommand;
import pl.ivmx.mappum.gui.model.commands.ConnectionReconnectCommand;

public class ShapeEditPart extends AbstractGraphicalEditPart implements
		PropertyChangeListener, NodeEditPart, MouseListener {
	private ConnectionAnchor anchor;
	private final IMappumEditor editor;

	public ShapeEditPart(final IMappumEditor editor) {
		this.editor = editor;
	}

	@Override
	protected IFigure createFigure() {
		ShapeFigure shapeFigure = new ShapeFigure(this, getCastedModel().getDepth());
		if(modelIsFolded() && modelIsComplex()){
			shapeFigure.setStyleCollapsed();
		}
			
		return shapeFigure;
	}

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
		// allow the creation of connections and
		// and the reconnection of connections between Shape instances
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new GraphicalNodeEditPolicy() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
					 * getConnectionCompleteCommand
					 * (org.eclipse.gef.requests.CreateConnectionRequest)
					 */
					protected Command getConnectionCompleteCommand(
							CreateConnectionRequest request) {
						ConnectionCreateCommand cmd = (ConnectionCreateCommand) request
								.getStartCommand();
						cmd.setTarget((Shape) getHost().getModel());
						return cmd;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
					 * getConnectionCreateCommand
					 * (org.eclipse.gef.requests.CreateConnectionRequest)
					 */
					protected Command getConnectionCreateCommand(
							final CreateConnectionRequest request) {
						Shape source = (Shape) getHost().getModel();
						final Connection.Info info = (Connection.Info) request
								.getNewObjectType();
						ConnectionCreateCommand cmd = new ConnectionCreateCommand(
								source, info);
						request.setStartCommand(cmd);
						return cmd;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
					 * getReconnectSourceCommand
					 * (org.eclipse.gef.requests.ReconnectRequest)
					 */
					protected Command getReconnectSourceCommand(
							ReconnectRequest request) {
						Connection conn = (Connection) request
								.getConnectionEditPart().getModel();
						Shape newSource = (Shape) getHost().getModel();
						ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(
								conn);
						cmd.setNewSource(newSource);
						return cmd;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
					 * getReconnectTargetCommand
					 * (org.eclipse.gef.requests.ReconnectRequest)
					 */
					protected Command getReconnectTargetCommand(
							ReconnectRequest request) {
						Connection conn = (Connection) request
								.getConnectionEditPart().getModel();
						Shape newTarget = (Shape) getHost().getModel();
						ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(
								conn);
						cmd.setNewTarget(newTarget);
						return cmd;
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */

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

	private Shape getCastedModel() {
		return (Shape) getModel();
	}

	public List<Shape> getModelChildren() {
		if (modelIsFolded()) {
			return Collections.emptyList();
		}
		return ((Shape) getModel()).getChildren();
	}

	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) {
			if (getModel() instanceof Shape)
				anchor = new ChopboxAnchor(getFigure());
			else
				// if Shapes gets extended the conditions above must be updated
				throw new IllegalArgumentException("unexpected model");
		}
		return anchor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections
	 * ()
	 */
	protected List<Connection> getModelSourceConnections() {
		return getCastedModel().getSourceConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections
	 * ()
	 */
	protected List<Connection> getModelTargetConnections() {
		return getCastedModel().getTargetConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (Shape.LAYOUT_PROP.equals(prop)) {
			//
		} else if (Shape.SOURCE_CONNECTIONS_PROP.equals(prop)) {
			refreshSourceConnections();
		} else if (Shape.TARGET_CONNECTIONS_PROP.equals(prop)) {
			refreshTargetConnections();
		} else if (Shape.CHILD_ADDED_PROP.equals(prop)) {
			refreshChildren();
		} else if (Shape.CHILD_REMOVED_PROP.equals(prop)) {
			refreshChildren();
		}
	}

	@Override
	public ShapeFigure getFigure() {
		return (ShapeFigure) super.getFigure();
	}

	protected void refreshVisuals() {

		final ShapeFigure figure = getFigure();
		Shape model = (Shape) getModel();
		if (model.isArrayType()) {
			figure.setName(model.getName() + "[]");
		} else {
			figure.setName(model.getName());
		}
		// figure.setLayout(model.getLayout());

		if (model.getParent() == null) {
			figure.setImage(ImageFactory
					.getImage(ImageFactory.ImageType.CLASS_IMAGE));
		} else if(model.isReccuranceInstance()){
			figure.setImage(ImageFactory
					.getImage(ImageFactory.ImageType.RECURRENCE_IMAGE));
		}	else {
			figure.setImage(ImageFactory
					.getImage(ImageFactory.ImageType.FIELD_IMAGE));
		}
	}

	@Override
	protected void refreshChildren() {
		super.refreshChildren();
		getFigure().recreateLabel();
	}

	public void mouseDoubleClicked(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
		if (modelIsComplex() && editor.getCurrentPaletteTool().equals(
						MappumEditorPaletteFactory.SELECTION_TOOL)) {
			if (modelIsFolded() == false && childrenConnected(true)) {
				return;
			}
			modelSetFolded(!modelIsFolded());
			if (modelIsFolded()) {
				getFigure().setStyleCollapsed();
			} else {
				getFigure().setStyleRegular();
			}
			refreshChildren();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ShapeEditPart> getChildren() {
		return super.getChildren();
	}

	public void mouseReleased(MouseEvent me) {
	}

	private boolean modelIsFolded() {
		return ((Shape) getModel()).isFolded();
	}

	private void modelSetFolded(boolean boll) {
		((Shape) getModel()).setFolded(boll);
	}
	private boolean modelIsComplex() {
		return ((Shape) getModel()).isComplex();
	}
	
	private boolean childrenConnected(final boolean allowConnections) {
		for (final ShapeEditPart p : getChildren()) {
			if (p.childrenConnected(false)) {
				return true;
			}
		}
		return allowConnections ? false : getSourceConnections().size()
				+ getTargetConnections().size() > 0;
	}
}