package pl.ivmx.mappum.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;

import javax.script.ScriptException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;
import pl.ivmx.mappum.gui.model.ShapesDiagram;
import pl.ivmx.mappum.gui.parts.ShapesEditPartFactory;
import pl.ivmx.mappum.gui.utils.ModelGenerator;
import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;
import pl.ivmx.mappum.gui.utils.TestNodeTreeWindow;

public class MappumEditor extends GraphicalEditorWithFlyoutPalette {

	/** This is the root of the editor's model. */
	private ShapesDiagram diagram;
	/** Palette component, holding the tools and shapes. */
	private static PaletteRoot PALETTE_MODEL;

	/** Create a new ShapesEditor instance. This is called by the Workspace. */
	public MappumEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	protected void setInput(IEditorInput input) {
		System.out.println("SET INPUT");
		super.setInput(input);
		RootNodeHolder.getInstance().setRootNode(null);
		Connection.getConnections().clear();
		Shape.getRootShapes().clear();
		Shape.getShapes().clear();
		ModelGeneratorFromXML.getInstance().setModelArray(null);
		final IFile file = ((IFileEditorInput) input).getFile();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getSite()
				.getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Generating model...", 100);
					try {
						file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
						monitor.worked(15);
						ModelGenerator.getInstance().generateModelRootElements(file);
						monitor.worked(25);
						ModelGeneratorFromXML.getInstance().generateModel(
								file.getProject());
						monitor.worked(35);
						ModelGeneratorFromXML.getInstance()
						.addFieldsFromRubyArray(
								Shape.getRootShapes().get(0)
										.getFullName(),
								Shape.getRootShapes().get(1)
										.getFullName());
						monitor.worked(75);
						ModelGenerator.getInstance().generateModelChildElements(file);
						monitor.worked(85);
						RootNodeHolder.generateRootBlockNode(RootNodeHolder
								.getInstance().getRootNode());

//						new TestNodeTreeWindow(RootNodeHolder.getInstance()
//								.getRootNode());

					} catch (CoreException e) {
						e.printStackTrace();
					} catch (ScriptException e) {
						e.printStackTrace();
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(),
					"Error while generating model", e.getCause().getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(),
					"Error while generating model", e.getCause().getMessage());
		}
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ShapesEditPartFactory());
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));

		// configure the context menu provider
		ContextMenuProvider cmProvider = new MappumEditorContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu(cmProvider, viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#commandStackChanged(java.util
	 * .EventObject)
	 */
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * createPaletteViewerProvider()
	 */
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer
						.addDragSourceListener(new TemplateTransferDragSourceListener(
								viewer));
			}
		};
	}

	/**
	 * Create a transfer drop target listener. When using a
	 * CombinedTemplateCreationEntry tool in the palette, this will enable model
	 * element creation by dragging from the palette.
	 * 
	 * @see #createPaletteViewerProvider()
	 */
	private TransferDropTargetListener createTransferDropTargetListener() {
		return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
			protected CreationFactory getFactory(Object template) {
				return new SimpleFactory((Class) template);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor
	 * )
	 */
	public void doSave(IProgressMonitor monitor) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(ModelGenerator.getInstance().generateRubyCode()
					.getBytes());
			out.close();
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true, // keep
					// saving,
					// even
					// if
					// IFile
					// is
					// out
					// of
					// sync
					// with
					// the
					// Workspace
					false, // dont keep history
					monitor); // progress monitor
			getCommandStack().markSaveLocation();
		} catch (CoreException ce) {
			ce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		// Show a SaveAs dialog
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();

		IPath path = dialog.getResult();
		if (path != null) {
			// try to save the editor's contents under a different file name
			final IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(path);
			try {
				new ProgressMonitorDialog(shell).run(false, // don't fork
						false, // not cancelable
						new WorkspaceModifyOperation() { // run this operation
							public void execute(final IProgressMonitor monitor) {
								try {
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									out.write(ModelGenerator.getInstance()
											.generateRubyCode().getBytes());
									out.close();
									file.create(new ByteArrayInputStream(out
											.toByteArray()), // contents
											true, // keep saving, even if IFile
											// is out of sync with the
											// Workspace
											monitor); // progress monitor
								} catch (CoreException ce) {
									ce.printStackTrace();
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							}
						});
				// set input to the new file
				setInput(new FileEditorInput(file));
				getCommandStack().markSaveLocation();
			} catch (InterruptedException ie) {
				// should not happen, since the monitor dialog is not cancelable
				ie.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}
	}

	ShapesDiagram getModel() {
		return diagram;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot
	 * ()
	 */
	protected PaletteRoot getPaletteRoot() {
		if (PALETTE_MODEL == null)
			PALETTE_MODEL = MappumEditorPaletteFactory.createPalette();
		return PALETTE_MODEL;
	}

	/**
	 * Set up the editor's inital content (after creation).
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
	 */
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		diagram = new ShapesDiagram();
		diagram.addChild(Shape.getRootShapes().get(0));
		diagram.addChild(Shape.getRootShapes().get(1));
		viewer.setContents(getModel()); // set the contents of this editor

		// listen for dropped parts
		viewer.addDropTargetListener(createTransferDropTargetListener());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	public void dispose() {
		RootNodeHolder.getInstance().setRootNode(null);
		Connection.getConnections().clear();
		Shape.getRootShapes().clear();
		Shape.getShapes().clear();
		ModelGeneratorFromXML.getInstance().setModelArray(null);
		// dispose
		super.dispose();
	}

}