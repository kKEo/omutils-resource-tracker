package org.maziarz.utils.resourcetracker.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.maziarz.utils.resourcetracker.views.TagCloudComposite.TagCloudDataProvider;

public class FilterPanel extends Composite {

	private IFilterListener filterListener;
	
	public FilterPanel(Composite parent, final TagCloudDataProvider dataProvider) {
		super(parent, SWT.NONE);
		
		this.setLayout(new FormLayout());
		
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 10);
		fd.left = new FormAttachment(0, 5);
		Label l = new Label(this, SWT.None);
		l.setText("Filter by: ");
		l.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(l, 5);
		fd.width = 160;

		final Text t = new Text(this, SWT.BORDER);
		t.setText("criteria..");
		t.setLayoutData(fd);

		t.addListener(SWT.KeyUp, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (event.keyCode == SWT.CR) {
					String phrase = t.getText();
					dataProvider.setPhrase(phrase);
					refreshItems();
				}
			}
		});
		
		t.addListener(SWT.FOCUSED, new Listener() {
			@Override
			public void handleEvent(Event event) {
				t.selectAll();
			}
		});
		
		fd = new FormData();
		fd.top = new FormAttachment(t, 5);
		fd.left = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		fd.bottom = new FormAttachment(100, -5);
	}

	protected void refreshItems() {
		filterListener.refresh();
	}

	public void addFilterListener(IFilterListener filterListener) {
		this.filterListener = filterListener;
	}

}
