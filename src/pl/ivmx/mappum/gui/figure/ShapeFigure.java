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

	private final static Color[] COLOR = {
		new Color(null, 240, 240, 240),
		new Color(null, 225, 225, 220),
		new Color(null, 225, 220, 225),
		new Color(null, 220, 225, 225),
		new Color(null, 210, 210, 205),
		new Color(null, 235, 235, 235),
		new Color(null, 220, 220, 215),
		new Color(null, 220, 215, 220),
		new Color(null, 215, 220, 220),
		new Color(null, 210, 205, 210)	
	};

	private final Label labelName = new Label();

	public ShapeFigure(final MouseListener ml, final int depht) {
		setBorder(new MarginBorder(3,20,3,3));
		setCornerDimensions(new Dimension(3, 3));
		final ToolbarLayout layout = new ToolbarLayout(false);
		layout.setSpacing(3);
		layout.setStretchMinorAxis(true);
		// layout.set
		setLayoutManager(layout);

		//setSize(100, 40);
		setBackgroundColor(COLOR[depht % 10]);
		
		labelName.setLabelAlignment(Label.LEFT);
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

	public void setStyleComplex() {
		final FontData fd = Display.getDefault().getSystemFont().getFontData()[0];
		labelName.setFont(new Font(null, fd.getName(), fd.getHeight(),
				SWT.ITALIC));
	}

	public void setName(String text) {
		labelName.setText(text);
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
		if(getChildren().size()>1){
			setStyleComplex();
		}
		add(labelName, 0);
	}
}
