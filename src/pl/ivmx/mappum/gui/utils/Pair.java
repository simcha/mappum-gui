package pl.ivmx.mappum.gui.utils;

import pl.ivmx.mappum.gui.model.Shape;

public class Pair {
	private Shape leftShape;
	private Shape rightShape;

	public Pair() {

	}

	public Pair(Shape leftParent, Shape rightParent) {
		this.leftShape = leftParent;
		this.rightShape = rightParent;
	}

	public Shape getLeftShape() {
		return leftShape;
	}

	public void setLeftShape(Shape leftShape) {
		this.leftShape = leftShape;
	}

	public Shape getRightShape() {
		return rightShape;
	}

	public void setRightShape(Shape rightShape) {
		this.rightShape = rightShape;
	}

	@Override
	public String toString() {
		return "Shapes: " + leftShape.getName() + ", " + rightShape.getName();
	}

}
