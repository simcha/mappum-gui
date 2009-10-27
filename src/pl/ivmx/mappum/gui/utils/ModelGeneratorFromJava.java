package pl.ivmx.mappum.gui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.ListNode;

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.Shape.Side;
import pl.ivmx.mappum.gui.utils.java.JavaModelGenerator;
import pl.ivmx.mappum.gui.utils.java.JavaTreeElement;

public class ModelGeneratorFromJava {
	// private Logger logger = Logger.getLogger(ModelGeneratorFromJava.class);

	private static final ModelGeneratorFromJava INSTANCE = new ModelGeneratorFromJava();

	private ModelGeneratorFromJava() {
	}

	public static final ModelGeneratorFromJava getInstance() {
		return INSTANCE;
	}

	private Shape checkAndAddShape(String name, Shape parent, Side side,
			boolean isArray, boolean isRecurrence) {
		System.out.println(name + ", recurrence?:" + isRecurrence);

		if (parent == null) {
			if (side == Shape.Side.LEFT) {
				if (name.equals(Shape.getRootShapes().get(0).getName())) {
					return Shape.getRootShapes().get(0);
				} else {
					throw new IllegalArgumentException(
							"There is no root element for arguments: Shape name: "
									+ name + ", side: " + side);
				}
			} else {
				if (name.equals(Shape.getRootShapes().get(1).getName())) {
					return Shape.getRootShapes().get(1);
				} else {
					throw new IllegalArgumentException(
							"There is no root element for arguments: Shape name: "
									+ name + ", side: " + side);
				}
			}
		} else {
			for (Shape shape : parent.getChildren()) {
				if (shape.getName().equals(name)) {
					if (isRecurrence) {
						shape.setReccuranceInstance(true);
					}
					return shape;
				}
			}
			Shape shape = Shape.createShape(name, null, parent, side,
					generateRubyModelForField(name, side));
			shape.setArrayType(isArray);
			shape.addToParent();
			if (isRecurrence) {
				shape.setReccuranceInstance(true);
			}
			return shape;
		}
	}

	private void addFieldsFromJavaModel0(final String clazz,
			final TreeElement el, final String sideElement,
			final List<JavaTreeElement> model, final Shape.Side side) {
		if (el.getClazz().equals(clazz)) {
			Shape parent = checkAndAddShape(sideElement, null, side, false,
					((JavaTreeElement) el).isMarkedAsComplex());
			if (el.getElements() != null) {
				for (TreeElement childElement : el.getElements()) {
					Shape child = checkAndAddShape(childElement.getName(),
							parent, side, childElement.getIsArray(),
							((JavaTreeElement) childElement)
									.isMarkedAsComplex());
					if (childElement.getClazz() != null) {
						getComplexField(childElement.getClazz(), child, side,
								model);

					}
				}
			}
		}
	}

	public void addFieldsFromJavaModel(String leftClazz, String rightClazz,
			String leftElement, String rightElement, final IProject project)
			throws IllegalArgumentException, ClassNotFoundException,
			JavaModelException {
		List<JavaTreeElement> model = new ArrayList<JavaTreeElement>();

		JavaModelGenerator.getInstance().generate(leftClazz, model, project);
		JavaModelGenerator.getInstance().generate(rightClazz, model, project);

		for (TreeElement element : model) {

			addFieldsFromJavaModel0(leftClazz, element, leftElement, model,
					Shape.Side.LEFT);
			addFieldsFromJavaModel0(rightClazz, element, rightElement, model,
					Shape.Side.RIGHT);
		}
	}

	private void getComplexField(String searchElement, Shape parent,
			final Shape.Side side, List<JavaTreeElement> model) {
		for (TreeElement element : model) {
			if (element.getClazz().equals(searchElement)) {
				for (TreeElement childElement : element.getElements()) {
					Shape child = checkAndAddShape(childElement.getName(),
							parent, side, childElement.getIsArray(),
							((JavaTreeElement) childElement)
									.isMarkedAsComplex());
					if (childElement.getClazz() != null) {
						getComplexField(childElement.getClazz(), child, side,
								model);
					}
				}
			}
		}
	}

	public CallNode generateRubyModelForField(String name, final Shape.Side side) {
		// String prefix = RootNodeHolder.getInstance().generateRandomIdent(
		// RootNodeHolder.IDENT_LENGTH);
		if (side == Shape.Side.LEFT) {
			ListNode listNode = new ListNode(new SourcePosition());
			DVarNode dVarNode = new DVarNode(new SourcePosition(), 0,
					"changeMe");
			return new CallNode(new SourcePosition(), dVarNode, name, listNode);
		} else {
			ListNode listNode = new ListNode(new SourcePosition());
			DVarNode dVarNode = new DVarNode(new SourcePosition(), 1,
					"changeMe");
			return new CallNode(new SourcePosition(), dVarNode, name, listNode);
		}

	}
}
