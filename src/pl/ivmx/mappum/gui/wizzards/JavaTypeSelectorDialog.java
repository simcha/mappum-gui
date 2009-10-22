package pl.ivmx.mappum.gui.wizzards;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

@SuppressWarnings("restriction")
public class JavaTypeSelectorDialog {

	public static String JAVA_PROJECT_NAME = "java";

	public static String selectJavaType(final Shell shell) {
		try {
			final JavaSearchScope ss = new JavaSearchScope();
			final IJavaProject p = JavaModelManager.getJavaModelManager()
					.getJavaModel().getJavaProject(JAVA_PROJECT_NAME);
			ss.add(p);
			final SelectionDialog sd = JavaUI
					.createTypeDialog(
							shell,
							null,
							ss,
							IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
							false, "");
			sd.setTitle("Select type");
			if (sd.open() == 0) {
				final BinaryType type = (BinaryType) sd.getResult()[0];
				return type.getFullyQualifiedName();
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
