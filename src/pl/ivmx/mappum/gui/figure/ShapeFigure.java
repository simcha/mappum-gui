package pl.ivmx.mappum.gui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class ShapeFigure extends RoundedRectangle {

	private final static Color COLOR_LIGHT = new Color(null, 240, 240, 240);
	private final static Color COLOR_DARK = new Color(null, 230, 230, 230);

	private final Label labelName = new Label();
	private final Label labelType = new Label();

	public ShapeFigure(final boolean isDark) {
		setCornerDimensions(new Dimension(20, 20));
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		labelName.setIcon(new Image(null, 16, 16));
		labelName.setForegroundColor(ColorConstants.darkGray);
		add(labelName, ToolbarLayout.ALIGN_CENTER);
		setConstraint(labelName, new Rectangle(5, 3, -1, -1));
		labelType.setForegroundColor(ColorConstants.black);
		add(labelType, ToolbarLayout.ALIGN_CENTER);
		setConstraint(labelType, new Rectangle(24, 16, -1, -1));

		// setBorder(new LineBorder(1));

		setBackgroundColor(isDark ? COLOR_DARK : COLOR_LIGHT);

		// setForegroundColor(new Color(null, 0, 0, 0));

	}

	public void setName(String text) {
		if (text != null)
			labelName.setText("Name: " + text);
	}

	public void setType(String text) {
		if (text != null)
			labelType.setText("Type: " + text);
	}

	public String getName() {
		return labelName.getText();
	}

	public String getType() {
		return labelType.getText();
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}

	public void setImage(Image image) {
		labelName.setIcon(image);

	}
}
