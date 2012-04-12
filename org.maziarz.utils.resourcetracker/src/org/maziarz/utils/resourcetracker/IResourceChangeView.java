package org.maziarz.utils.resourcetracker;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;

public interface IResourceChangeView {
	Display getDisplay();
	void refreshItems();
	boolean setFocus();
	void setLayoutData(FormData formData);
}
