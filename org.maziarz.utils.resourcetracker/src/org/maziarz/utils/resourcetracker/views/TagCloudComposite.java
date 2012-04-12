package org.maziarz.utils.resourcetracker.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.maziarz.utils.resourcetracker.IResourceChangeView;

public class TagCloudComposite extends Composite implements IResourceChangeView{

	private TagCloudDataProvider dataProvider;

	private Composite container;

	private Color bgColor = new Color(this.getDisplay(), 255, 255, 255);

	private final Cursor cursor = new Cursor(this.getDisplay(), SWT.CURSOR_HAND);

	private Font[] fonts = new Font[] {
			new Font(this.getDisplay(), new FontData[] { new FontData("Sans",
					10, SWT.NORMAL) }),
			new Font(this.getDisplay(), new FontData[] { new FontData("Sans",
					12, SWT.NORMAL) }),
			new Font(this.getDisplay(), new FontData[] { new FontData("Sans",
					14, SWT.NORMAL) }),
			new Font(this.getDisplay(), new FontData[] { new FontData("Sans",
					16, SWT.NORMAL) }), };

	interface TagCloudDataProvider {
		void setPhrase(String s);
		TagCloudItem[] getItems();
		String getTags(IResource s);
		void updateTags(IResource s, String currentTags, String newTagList);
	}

	public TagCloudComposite(Composite parent, final TagCloudDataProvider dataProvider) {
		super(parent, SWT.NONE);
		this.dataProvider = dataProvider;

		this.setLayout(new FillLayout());

		container = new Composite(this, SWT.BORDER);
		RowLayout layout = new RowLayout();
		layout.justify = true;
		layout.center = true;
		container.setLayout(layout);

		container.setBackground(bgColor);
		container.setBackgroundMode(SWT.INHERIT_DEFAULT);

		refreshItems();
	}

	@Override
	public void dispose() {
		super.dispose();
		cursor.dispose();
	}

	public void refreshItems() {
		
		for (Control c: container.getChildren()){
			c.dispose();
		}
		
		for (TagCloudItem item : dataProvider.getItems()) {
			final Label l = new Label(container, SWT.NONE);
			l.setText(item.getPath());
			// l.setBackground(bgColor);
			l.setCursor(cursor);
			l.setFont(fonts[0]);

			l.addListener(SWT.MouseUp, new Listener() {
				@Override
				public void handleEvent(Event event) {
					l.setFont(fonts[2]);
					l.getParent().layout(true, true);
					l.getParent().redraw();
				}
			});
		}
		container.layout(true,true);
		container.redraw();
	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(400, 300);
		shell.setText("Button Example");

		shell.setLayout(new FillLayout());

		TagCloudDataProvider dataProvider = new TagCloudDataProvider() {

			@Override
			public TagCloudItem[] getItems() {
				return new TagCloudItem[] { //
				new TagCloudItem("ProjectList.java"),//
						new TagCloudItem("ProjectList.html"),//
						new TagCloudItem("IssueDetailsView.java"),//
						new TagCloudItem("Activator.java"),//
						new TagCloudItem("ResourceHistoryStore.java"),//
						new TagCloudItem("initDb.sql"),//
						new TagCloudItem("plugin.xml"),//
						new TagCloudItem("contexts.xml"),//
						new TagCloudItem("ExemplaryListener.java"),//
						new TagCloudItem(
								"ExemplaryListenerWithExtremallyLongName.java"),//

				};
			}

			@Override
			public void setPhrase(String s) {
				
			}

			@Override
			public String getTags(IResource s) {
				return null;
			}

			@Override
			public void updateTags(IResource s, String currentTags,
					String newTagList) {
			}
		};

		new TagCloudComposite(shell, dataProvider);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	@Override
	public void setLayoutData(FormData formData) {
		setLayoutData((Object)formData);
	}

}
