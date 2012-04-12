package org.maziarz.utils.resourcetracker.views;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.maziarz.utils.resourcetracker.Activator;
import org.maziarz.utils.resourcetracker.IResourceChangeView;
import org.maziarz.utils.resourcetracker.ResourceHistoryStore;
import org.maziarz.utils.resourcetracker.views.TagCloudComposite.TagCloudDataProvider;

public class ResourcesCloudView extends ViewPart {

	public static final String ID = "org.maziarz.edu.resources.views.resourcesCloud";

	private Action action1;
	private Action action2;

	private Composite parent;

	private IResourceChangeView[] views = new IResourceChangeView[0];
	private int activeView = 1;

	private TagCloudDataProvider dataProvider;

	private FormData show;
	private FormData hide;

	public ResourcesCloudView() {
		Activator.getDefault().setView(this);
	}

	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new FormLayout());

		dataProvider = new TagCloudDataProvider() {

			private String pattern;

			@Override
			public TagCloudItem[] getItems() {
				return Activator.getDefault().getTagCloudItems(pattern);
			}

			@Override
			public void setPhrase(String s) {
				this.pattern = "*" + s + "*";
			}

			@Override
			public String getTags(IResource resource) {
				return Activator.getDefault().getStore().getTags(resource);
			}

			@Override
			public void updateTags(IResource resource, String currentTags,
					String newTags) {
				Activator.getDefault().getStore().updateTags(resource, newTags);
			}
		};

		FilterPanel filter = new FilterPanel(parent, dataProvider);
		filter.addFilterListener(new IFilterListener() {

			@Override
			public void refresh() {
				views[activeView].refreshItems();
			}

		});
		FormData fd = new FormData();
		fd.top = fd.left = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		filter.setLayoutData(fd);

		show = new FormData();
		show.left = new FormAttachment(0, 5);
		show.top = new FormAttachment(filter, 5);
		show.bottom = show.right = new FormAttachment(100, -5);

		hide = new FormData();
		hide.top = hide.left = hide.bottom = hide.right = new FormAttachment(0);

		TagCloudComposite tagCloud = new TagCloudComposite(parent, dataProvider);
		registerView(tagCloud);
		tagCloud.setLayoutData(hide);

		TopResourceChangedTable table = new TopResourceChangedTable(parent,
				dataProvider);
		registerView(table);
		table.setLayoutData(hide);

		makeActions();
		contributeToActionBars();

		switchView(activeView);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				switchView(0);
			}

		};
		action1.setText("Cloud View");
		action1.setToolTipText("View changed resources in cloud");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				switchView(1);
			}
		};
		action2.setText("Table View");
		action2.setToolTipText("View changed resources in table");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

	}

	private void switchView(int index) {
		views[activeView].setLayoutData(hide);
		activeView = index;
		views[activeView].setLayoutData(show);
		views[activeView].refreshItems();
		parent.layout(true, true);
		parent.redraw();
	}

	public void setFocus() {
		views[activeView].setFocus();
	}

	public void registerView(IResourceChangeView newView) {
		views = Arrays.copyOf(views, views.length + 1);
		views[views.length - 1] = newView;
	}

	public void notifyChange(final Object obj) {
		views[activeView].getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				views[activeView].refreshItems();
			}
		});
	}

}