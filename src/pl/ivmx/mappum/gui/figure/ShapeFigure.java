package pl.ivmx.mappum.gui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

public class ShapeFigure extends Figure {
	private Label labelName = new Label();
	private Label labelType = new Label();

	public ShapeFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
		labelName.setIcon(new Image(null, 16, 16));
		labelName.setForegroundColor(ColorConstants.darkGray);
		add(labelName, ToolbarLayout.ALIGN_CENTER);
		setConstraint(labelName, new Rectangle(5, 3, -1, -1));
		labelType.setForegroundColor(ColorConstants.black);
		add(labelType, ToolbarLayout.ALIGN_CENTER);
		setConstraint(labelType, new Rectangle(24, 16, -1, -1));

		/** Just for Fun :) **/
		setBorder(new LineBorder(1));
		//setBackgroundColor(new Color(null, 255, 255, 206));
		//setForegroundColor(new Color(null, 0, 0, 0));

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
