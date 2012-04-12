package org.maziarz.utils.resourcetracker.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class TimelineComposite extends Composite {

	public TimelineComposite(Composite parent) {
		super(parent, SWT.NONE);
		
		this.setLayout(new GridLayout(1, false));
		
		addLabel(200);
		addLabel(100);
		addLabel(60);
		addLabel(60);
		addLabel(55);
		addLabel(0);
	}

	private void addLabel(int ident) {
		GridData gd = new GridData();
		gd.horizontalIndent = ident;
		final Label l = new Label(this, SWT.NONE);
		l.setText("History timeline");
		l.setLayoutData(gd);
		
		l.addListener(SWT.MouseDoubleClick, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				GridData gd = (GridData)l.getLayoutData();
				gd.horizontalIndent += 10;
				
				l.getParent().layout(true, true);
				l.getParent().redraw();
			}
		});
	}

	
}
