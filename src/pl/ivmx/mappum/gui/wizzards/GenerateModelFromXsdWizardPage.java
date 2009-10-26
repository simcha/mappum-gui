package pl.ivmx.mappum.gui.wizzards;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import pl.ivmx.mappum.TreeElement;

public class GenerateModelFromXsdWizardPage extends WizardPage implements
		Listener {

	private Tree leftTree;
	private Tree rightTree;

	private SelectedType leftSelectedType = null;
	private SelectedType rightSelectedType = null;

	protected GenerateModelFromXsdWizardPage(String arg0) {
		super(arg0);
		setTitle("Generate model from XSD schema");
		setDescription("Specify left and right side of mapping model");
	}

	private Listener createTreeSelChangeListener(final boolean left) {
		return new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (left) {
					leftSelectedType = new SelectedType(
							leftTree.getSelection()[0].getText());
				} else {
					rightSelectedType = new SelectedType(rightTree
							.getSelection()[0].getText());
				}
			}
		};
	}

	private Tree createTree(final Composite c, final boolean left) {
		final Tree tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
		final GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true);
		tree.setLayoutData(gd_list);
		tree.addListener(SWT.Selection, createTreeSelChangeListener(left));
		tree.addListener(SWT.Selection, this);

		return tree;
	}

	private Set<String> getRoots(List<TreeElement> model) {

		final Set<String> roots = new HashSet<String>(model.size());
		for (final TreeElement tn : model) {
			if (tn.getName() != null) {
				roots.add(tn.getName());
			}
		}

		for (final TreeElement tn : model) {

			for (final TreeElement child : tn.getElements()) {
				if (child.getClazz() != null) {
					roots.remove(child.getClazz());
				}
			}
		}
		return roots;
	}

	private void fillTree(final Tree tree) {
		final List<TreeElement> model = ((GenerateModelFromXsdWizard) getWizard())
				.getModel();
		final Map<String, TreeElement> elementMap = getElementMap(model);

		final Set<String> roots = getRoots(model);

		for (final String root : roots) {
			final TreeItem ti = new TreeItem(tree, SWT.NONE);
			ti.setText(root);
			fillTree(ti, root, elementMap);
		}
	}

	private Map<String, TreeElement> getElementMap(final List<TreeElement> model) {
		final Map<String, TreeElement> elementMap = new HashMap<String, TreeElement>(
				model.size());
		for (final TreeElement te : model) {
			if (te.getName() != null) {
				elementMap.put(te.getName(), te);
			}
		}
		return Collections.unmodifiableMap(elementMap);
	}

	private void fillTree(final TreeItem parentTi, final String clazz,
			final Map<String, TreeElement> elementMap) {
		final TreeElement currentElement = elementMap.get(clazz);
		if (currentElement != null) {
			final Set<String> duplicateCheck = new HashSet<String>();
			for (final TreeElement el : currentElement.getElements()) {
				final String clz = el.getClazz();
				if (clz != null && !clz.startsWith("SOAP::")
						&& duplicateCheck.add(clz)) {
					final TreeItem ti = new TreeItem(parentTi, SWT.NONE);
					ti.setText(clz);
					fillTree(ti, clz, elementMap);
				}
			}
		}
	}

	private Text createSelectedTypeText(final Composite c) {

		final Text t = new Text(c, SWT.SINGLE | SWT.BORDER);

		t.setEnabled(false);
		t.setEditable(false);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return t;
	}

	private Listener createChecboxListener(final Button c, final Button b,
			final Text text, final Tree tree) {
		return new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (c.getSelection()) {
					tree.setEnabled(false);
					b.setEnabled(true);
					text.setEnabled(true);
				} else {
					tree.setEnabled(true);
					b.setEnabled(false);
					text.setEnabled(false);
				}
			}
		};
	}

	private Button createCheckbox(final Composite c) {
		final Button b = new Button(c, SWT.CHECK);
		b.setText("Java type");
		return b;
	}

	private Button createSelectTypeButton(final Composite c) {
		final Button b = new Button(c, SWT.NONE);
		b.setText("Select type");
		b.setEnabled(false);
		return b;
	}

	private Listener createSelectTypeListener(final Text text,
			final boolean left) {
		return new Listener() {
			@Override
			public void handleEvent(Event event) {
				final SelectedType t = JavaTypeSelectorDialog
						.selectJavaType(getShell());
				if (t != null) {
					if (left) {
						leftSelectedType = t;
					} else {
						rightSelectedType = t;
					}
					text.setText(t.getFullName());
					getContainer().updateButtons();
				}
			}
		};
	}

	private Label createLabel(final Composite c) {
		final Label l = new Label(c, SWT.NONE);
		l.setText("Left mapping model side:");
		return l;
	}

	Button leftJavaCheckbox;
	Button rightJavaCheckbox;

	Text leftSelectedTypeText;
	Text rightSelectedTypeText;

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		composite.setLayout(gl);

		leftJavaCheckbox = createCheckbox(composite);
		rightJavaCheckbox = createCheckbox(composite);

		final Button leftSelectTypeButton = createSelectTypeButton(composite);
		final Button rightSelectTypeButton = createSelectTypeButton(composite);

		leftSelectedTypeText = createSelectedTypeText(composite);
		rightSelectedTypeText = createSelectedTypeText(composite);

		createLabel(composite);
		createLabel(composite);

		leftTree = createTree(composite, true);
		rightTree = createTree(composite, false);

		leftSelectTypeButton.addListener(SWT.MouseUp, createSelectTypeListener(
				leftSelectedTypeText, true));
		rightSelectTypeButton.addListener(SWT.MouseUp,
				createSelectTypeListener(rightSelectedTypeText, false));

		leftJavaCheckbox.addListener(SWT.MouseUp, createChecboxListener(
				leftJavaCheckbox, leftSelectTypeButton, leftSelectedTypeText,
				leftTree));
		leftJavaCheckbox.addListener(SWT.Selection, this);
		rightJavaCheckbox.addListener(SWT.MouseUp, createChecboxListener(
				rightJavaCheckbox, rightSelectTypeButton,
				rightSelectedTypeText, rightTree));
		rightJavaCheckbox.addListener(SWT.Selection, this);

		setControl(composite);
		onEnterPage();
		setPageComplete(false);
	}

	public boolean canFlipToNextPage() {
		return false;
	}

	/*
	 * Process the events: when the user has entered all information the wizard
	 * can be finished
	 */
	public void handleEvent(Event e) {
		// setPageComplete(isPageComplete()); //?
		getContainer().updateButtons();
		// getWizard().getContainer().updateButtons();
	}

	private boolean isPageComplete0(final Button checkbox, final Text text,
			final Tree tree) {
		if (checkbox.getSelection()) {
			if ("".equals(text.getText())) {
				return false;
			}
		} else if (tree.getSelectionCount() == 0) {
			return false;
		}
		return true;
	}

	public boolean isPageComplete() {
		if (!isPageComplete0(leftJavaCheckbox, leftSelectedTypeText, leftTree)
				|| !isPageComplete0(rightJavaCheckbox, rightSelectedTypeText,
						rightTree)) {
			return false;
		}

		return true;
	}

	private void onEnterPage() {
		fillTree(leftTree);
		fillTree(rightTree);
	}

	public SelectedType getLeftSelectedType() {
		return leftSelectedType;
	}

	public SelectedType getRightSelectedType() {
		return rightSelectedType;
	}
}
