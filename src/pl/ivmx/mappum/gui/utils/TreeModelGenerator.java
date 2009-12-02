package pl.ivmx.mappum.gui.utils;

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.Shape.Side;

public abstract class TreeModelGenerator {

	public void getComplexField(TreeElement element, Shape parent,
			final Shape.Side side) {
		if (element.getElements() != null){
			for (TreeElement childElement : element.getElements()) {
				Shape child = checkAndAddShape(childElement, parent, side,
						childElement.isArray());
				if (childElement.getClazz() != null
						&& !childElement.isFolded()) {
					
					getComplexField(childElement, child, side);
				}
			}
		}
	}

	protected abstract Shape checkAndAddShape(TreeElement childElement, Shape parent,
			Side side, boolean array);

}