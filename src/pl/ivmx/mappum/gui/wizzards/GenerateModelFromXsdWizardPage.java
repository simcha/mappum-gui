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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import pl.ivmx.mappum.TreeElement;

public class GenerateModelFromXsdWizardPage extends WizardPage implements
		Listener {

	private Tree leftTree;
	private Tree rightTree;

	protected GenerateModelFromXsdWizardPage(String arg0) {
		super(arg0);
		setTitle("Generate model from XSD schema");
		setDescription("Specify left and right side of mapping model");
	}

	private Tree createTree(final Composite c) {
		final Tree tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
		final GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true);
		tree.setLayoutData(gd_list);
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

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		composite.setLayout(gl);

		final Label leftLabel = new Label(composite, SWT.NONE);
		leftLabel.setText("Left mapping model side:");

		final Label rightLabel = new Label(composite, SWT.NONE);
		rightLabel.setText("Right mapping model side:");

		leftTree = createTree(composite);
		rightTree = createTree(composite);

		setControl(composite);
		onEnterPage();
		setPageComplete(false);
	}

	public boolean canFlipToNextPage() {
		// no next page for this path through the wizard
		return false;
	}

	/*
	 * Process the events: when the user has entered all information the wizard
	 * can be finished
	 */
	public void handleEvent(Event e) {
		setPageComplete(isPageComplete());
		getWizard().getContainer().updateButtons();
	}

	/*
	 * Sets the completed field on the wizard class when all the information is
	 * entered and the wizard can be completed
	 */
	public boolean isPageComplete() {
		if (leftTree.getSelectionCount() == 0
				&& rightTree.getSelectionCount() == 0) {
			return false;
		}
		GenerateModelFromXsdWizard wizard = (GenerateModelFromXsdWizard) getWizard();
		wizard.setLeftChoosenElement(leftTree.getSelection()[0].getText());
		wizard.setRightChoosenElement(rightTree.getSelection()[0].getText());
		return true;
	}

	void onEnterPage() {
		fillTree(leftTree);
		fillTree(rightTree);
	}
}
