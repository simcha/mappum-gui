package pl.ivmx.mappum.gui.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import pl.ivmx.mappum.gui.IMappumEditor;
import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.ShapesDiagram;

public class ShapesEditPartFactory implements EditPartFactory {

	private final IMappumEditor editor;

	public ShapesEditPartFactory(final IMappumEditor editor) {
		this.editor = editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart,
	 * java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object modelElement) {
		// get EditPart for model element
		EditPart part = getPartForElement(modelElement);
		// store model element in EditPart
		part.setModel(modelElement);
		return part;
	}

	/**
	 * Maps an object to an EditPart.
	 * 
	 * @throws RuntimeException
	 *             if no match was found (programming error)
	 */
	private EditPart getPartForElement(Object modelElement) {
		if (modelElement instanceof ShapesDiagram) {
			return new DiagramEditPart();
		}
		if (modelElement instanceof Shape) {
			return new ShapeEditPart(editor);
		}
		if (modelElement instanceof Connection) {
			return new ConnectionEditPart(editor);
		}
		throw new RuntimeException("Can't create part for model element: "
				+ ((modelElement != null) ? modelElement.getClass().getName()
						: "null"));
	}

}