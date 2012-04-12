package org.maziarz.utils.resourcetracker.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.maziarz.utils.resourcetracker.Activator;
import org.maziarz.utils.resourcetracker.IResourceChangeView;
import org.maziarz.utils.resourcetracker.views.TagCloudComposite.TagCloudDataProvider;
import org.eclipse.ui.part.FileEditorInput;

public class TopResourceChangedTable extends Composite implements
		IResourceChangeView {

	private TableViewer viewer;

	private Action doubleClickAction;

	private TagCloudDataProvider dataProvider;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(final Viewer v, Object oldInput,
				Object newInput) {
			v.refresh();
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return (TagCloudItem[]) parent;
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	public TopResourceChangedTable(Composite parent,
			final TagCloudDataProvider dataProvider) {
		super(parent, SWT.None);
		this.dataProvider = dataProvider;

		this.setLayout(new FillLayout());

		viewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// viewer.setSorter(new NameSorter());

		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setWidth(300);
		col.getColumn().setText("Resource path");
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TagCloudItem p = (TagCloudItem) element;
				return p.getPath();
			}
		});

		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(100);
		col2.getColumn().setText("Project");
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TagCloudItem p = (TagCloudItem) element;
				return p.getProject();
			}
		});

		final Table table = viewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		Menu menu = new Menu(table);
		table.setMenu(menu);

		MenuItem addTag = new MenuItem(menu, SWT.NONE);
		addTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				int selected = table.getSelectionIndex();
				Object s = table.getItem(selected);

				if (s instanceof TableItem
						&& ((TableItem) s).getData() instanceof TagCloudItem) {
					TagCloudItem item = (TagCloudItem) ((TableItem) s)
							.getData();
					
					IResource resource = item.getResource();

					ManageTagsDialog tags = new ManageTagsDialog(getShell());
					String currentTagList = dataProvider.getTags(resource);
					String newTagList = tags.open(currentTagList);

					if (!currentTagList.equals(newTagList)) {
						dataProvider.updateTags(resource, currentTagList, newTagList);
					}

				}
			}
		});
		addTag.setText("Add tag");

		hookDoubleClickAction();
	}

	private void hookDoubleClickAction() {

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object selectedObject = ((IStructuredSelection) selection)
						.getFirstElement();

				if (selectedObject instanceof TagCloudItem) {
					IResource resource = ((TagCloudItem) selectedObject).getResource();
					if (resource != null) {
						IEditorDescriptor desc = PlatformUI.getWorkbench()
								.getEditorRegistry()
								.getDefaultEditor(((IFile) resource).getName());
						try {
							PlatformUI
									.getWorkbench()
									.getActiveWorkbenchWindow()
									.getActivePage()
									.openEditor(
											new FileEditorInput(
													(IFile) resource),
											desc.getId());
						} catch (PartInitException e) {
							Activator.error(e.getMessage(), e);
						}
					}
				}
			}
		};

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	// private void showMessage(String message) {
	// MessageDialog.openInformation(viewer.getControl().getShell(),
	// "Resources Cloud", message);
	// }

	@Override
	public void refreshItems() {
		viewer.setInput(dataProvider.getItems());
	}

	@Override
	public void setLayoutData(FormData formData) {
		setLayoutData((Object) formData);
	}

}
