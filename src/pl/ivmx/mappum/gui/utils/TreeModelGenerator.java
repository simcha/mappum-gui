package pl.ivmx.mappum.gui.utils;

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.model.Shape;

public interface TreeModelGenerator {

	public void getComplexField(TreeElement element, Shape parent,
			final Shape.Side side);

}