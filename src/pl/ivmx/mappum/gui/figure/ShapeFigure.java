package pl.ivmx.mappum.gui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ShapeFigure extends RoundedRectangle {

	private final static Color COLOR_LIGHT = new Color(null, 240, 240, 240);
	private final static Color COLOR_DARK = new Color(null, 230, 230, 230);

	private final Label labelName = new Label();

	public ShapeFigure(final MouseListener ml, final boolean isDark) {
		setBorder(new MarginBorder(5));
		setCornerDimensions(new Dimension(20, 20));
		final ToolbarLayout layout = new ToolbarLayout(false);
		layout.setSpacing(5);
		layout.setStretchMinorAxis(true);
		// layout.set
		setLayoutManager(layout);
		//setSize(100, 40);
		setBackgroundColor(isDark ? COLOR_DARK : COLOR_LIGHT);

		labelName.setForegroundColor(ColorConstants.darkGray);
		final FontData fd = Display.getDefault().getSystemFont().getFontData()[0];
		labelName.setFont(new Font(null, fd.getName(), fd.getHeight(),
				SWT.NORMAL));
		addMouseListener(ml);
	}

	public void setStyleCollapsed() {
		final FontData fd = Display.getDefault().getSystemFont().getFontData()[0];
		labelName
				.setFont(new Font(null, fd.getName(), fd.getHeight(), SWT.BOLD));
	}

	public void setStyleRegular() {
		final FontData fd = Display.getDefault().getSystemFont().getFontData()[0];
		labelName.setFont(new Font(null, fd.getName(), fd.getHeight(),
				SWT.NORMAL));
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
