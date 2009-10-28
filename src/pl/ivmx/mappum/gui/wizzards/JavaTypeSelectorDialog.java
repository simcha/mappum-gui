package pl.ivmx.mappum.gui.wizzards;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

@SuppressWarnings("restriction")
public class JavaTypeSelectorDialog {

	private static IJavaProject getThisJavaProject() {
		IStructuredSelection is = (IStructuredSelection) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().getSelection();

		return (IJavaProject) is.getFirstElement();
	}

	public static SelectedType selectJavaType(final Shell shell) {

		try {

			final JavaSearchScope ss = new JavaSearchScope();
			

			ss.add(getThisJavaProject());
			final SelectionDialog sd = JavaUI
					.createTypeDialog(
							shell,
							null,
							ss,
							IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
							false, "");
			sd.setTitle("Select type");
			if (sd.open() == 0) {
				return new SelectedType((IType) sd.getResult()[0]);
			}
			return null;

		} catch (JavaModelException e) {
			final MessageBox mb = new MessageBox(shell, SWT.ERROR);
			mb.setMessage("Error");
			mb.setText("Operation failed: " + e.getMessage());
			mb.open();
			return null;
		}
	}
}
