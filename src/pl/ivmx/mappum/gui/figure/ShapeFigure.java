package pl.ivmx.mappum.gui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class ShapeFigure extends RoundedRectangle {

	private final static Color COLOR_LIGHT = new Color(null, 240, 240, 240);
	private final static Color COLOR_DARK = new Color(null, 230, 230, 230);

	private final Label labelName = new Label();

	public ShapeFigure(final boolean isDark) {
		setBorder(new MarginBorder(5));
		setCornerDimensions(new Dimension(20, 20));
		final FlowLayout layout = new FlowLayout(false);
		layout.setStretchMinorAxis(true);
		setLayoutManager(layout);
		setSize(50,20); 
		setBackgroundColor(isDark ? COLOR_DARK : COLOR_LIGHT);

		labelName.setForegroundColor(ColorConstants.darkGray);
	}

	public void setName(String text) {
		labelName.setText("Name: " + text);
	}

	public String getName() {
		return labelName.getText();
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}

	public void setImage(Image image) {
		labelName.setIcon(image);

	}

	public void recreateLabel() {
		if (getChildren().contains(labelName)) {
			remove(labelName);
		}
		add(labelName, 0);
	}
}
