package pl.ivmx.mappum.gui.utils;

import java.util.ArrayList;
import java.util.List;

import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.ListNode;

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.Shape.Side;
import pl.ivmx.mappum.gui.utils.java.JavaModelGenerator;

public class ModelGeneratorFromJava {
//	private Logger logger = Logger.getLogger(ModelGeneratorFromJava.class);

	private static final ModelGeneratorFromJava INSTANCE = new ModelGeneratorFromJava();

	private ModelGeneratorFromJava() {
	}

	public static final ModelGeneratorFromJava getInstance() {
		return INSTANCE;
	}


	private Shape checkAndAddShape(String name, Shape parent, Side side,
			boolean isArray) {
		if (parent == null) {
			if (side == Shape.Side.LEFT) {
				if (name.equals(Shape.getRootShapes().get(0).getFullName())) {
					return Shape.getRootShapes().get(0);
				} else {
					throw new IllegalArgumentException(
							"There is no root element for arguments: Shape name: "
									+ name + ", side: " + side);
				}
			} else {
				if (name.equals(Shape.getRootShapes().get(1).getFullName())) {
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
					return shape;
				}
			}
			Shape shape = Shape.createShape(name, null, parent, side,
					generateRubyModelForField(name, side));
			shape.setArrayType(isArray);
			shape.addToParent();
			return shape;
		}
	}

	public void addFieldsFromJavaModel(String leftElement, String rightElement) throws IllegalArgumentException, ClassNotFoundException {
		List<TreeElement> leftModel = new ArrayList<TreeElement>();
		List<TreeElement> rightModel = new ArrayList<TreeElement>();
		JavaModelGenerator.getInstance().generate(leftElement, leftModel);
		JavaModelGenerator.getInstance().generate(leftElement, rightModel);
		for (TreeElement element : leftModel) {
			if (element.getName().equals(leftElement)) {
				Shape parent = checkAndAddShape(leftElement, null,
						Shape.Side.LEFT, false);
				for (TreeElement childElement : element.getElements()) {
					Shape child = checkAndAddShape(childElement.getName(),
							parent, Shape.Side.LEFT, childElement.getIsArray());
					if (childElement.getClazz() != null) {
						getComplexField(childElement.getClazz(), child,
								Shape.Side.LEFT, leftModel);
					}
				}
			}
		}
		for (TreeElement element : rightModel) {
			if (element.getName().equals(rightElement)) {
				Shape parent = checkAndAddShape(rightElement, null,
						Shape.Side.RIGHT, false);
				for (TreeElement childElement : element.getElements()) {
					Shape child = checkAndAddShape(childElement.getName(),
							parent, Shape.Side.RIGHT, childElement.getIsArray());
					if (childElement.getClazz() != null) {
						getComplexField(childElement.getClazz(), child,
								Shape.Side.RIGHT, rightModel);
					}
				}
			}
		}
	}

	private void getComplexField(String searchElement, Shape parent,
			final Shape.Side side, List<TreeElement> model) {
		for (TreeElement element : model) {
			if (element.getName().equals(searchElement)) {
				for (TreeElement childElement : element.getElements()) {
					Shape child = checkAndAddShape(childElement.getName(),
							parent, side, childElement.getIsArray());
					if (childElement.getClazz() != null) {
						getComplexField(childElement.getClazz(), child, side, model);
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
