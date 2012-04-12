package org.maziarz.utils.resourcetracker.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ManageTagsDialog extends Dialog {

	protected String result;
	protected Shell dialog;
	private Table table;
	private Button btnOk;

	public ManageTagsDialog(Shell parent) {
		super(parent, SWT.APPLICATION_MODAL);
	}

	public String open(String tags) {
		result = tags;
		dialog = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE
				| getStyle());
		dialog.setText("Assign tags");
		dialog.setLayout(new FormLayout());

		createDialogControls(tags);

		dialog.setDefaultButton(btnOk);
		dialog.pack();

		// put dialog on the center
		// Rectangle rect = getParent().getMonitor().getBounds();
		// Rectangle bounds = dialog.getBounds();
		// dialog.setLocation(rect.x + (rect.width - bounds.width) / 2, rect.y +
		// (rect.height - bounds.height) / 2);
		// dialog.setMinimumSize(bounds.width, bounds.height);

		dialog.open();

		Display display = getParent().getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return result;
	}

	private void createDialogControls(String tags) {
		Group grpTags = createTagsTable(tags);
		createButtons(grpTags);
	}

	private void createButtons(Group grpTags) {
		btnOk = new Button(dialog, SWT.NONE);
		FormData fd_btnOk = new FormData();
		fd_btnOk.top = new FormAttachment(0, 15);
		fd_btnOk.left = new FormAttachment(grpTags, 15);
		fd_btnOk.right = new FormAttachment(100, -15);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("OK");

		btnOk.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				result = getTags();
				dialog.close();
			}
		});

		Button btnCancel = new Button(dialog, SWT.NONE);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.top = new FormAttachment(btnOk, 5);
		fd_btnCancel.left = new FormAttachment(grpTags, 15);
		fd_btnCancel.right = new FormAttachment(100, -15);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");

		btnCancel.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				dialog.close();
			}
		});
	}

	private Group createTagsTable(String tags) {
		Group grpTags = new Group(dialog, SWT.NONE);
		FormData fd_grpTags = new FormData();
		fd_grpTags.top = new FormAttachment(0, 10);
		fd_grpTags.left = new FormAttachment(0, 10);
		fd_grpTags.bottom = new FormAttachment(100, -10);
		fd_grpTags.width = 200;
		grpTags.setLayoutData(fd_grpTags);
		grpTags.setText(" T a g s ");

		FillLayout fl_grpTags = new FillLayout(SWT.HORIZONTAL);
		fl_grpTags.marginHeight = fl_grpTags.marginWidth = 5;
		grpTags.setLayout(fl_grpTags);

		table = new Table(grpTags, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);

		if (tags != null) {
			for (String tag : tags.split(",")) {
				if (!tags.equals("")) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(tag);
				}
			}
		}

		final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;

		Menu menu = new Menu(table);
		table.setMenu(menu);

		MenuItem mi = new MenuItem(menu, SWT.PUSH);
		mi.setText("Add tag");
		mi.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText("    ");
				editItem(table, editor, item);
				table.showItem(item);
			}
		});

		MenuItem removeItem = new MenuItem(menu, SWT.PUSH);
		removeItem.setText("Remove Selected Rows");
		removeItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				table.remove(table.getSelectionIndices());
			}
		});

		table.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event e) {
				// Rectangle clientArea = list.getClientArea();
				Point pt = new Point(e.x, e.y);
				int index = table.getTopIndex();
				while (index < table.getItemCount()) {
					final TableItem item = table.getItem(index);
					Rectangle rect = item.getBounds();
					if (rect.contains(pt)) {
						editItem(table, editor, item);
					}
					index++;
				}
			}
		});
		return grpTags;
	}

	private void editItem(final Table list, final TableEditor editor,
			final TableItem item) {
		final Text text = new Text(list, SWT.None);
		Listener textListener = new Listener() {

			@Override
			public void handleEvent(final Event ev) {
				switch (ev.type) {
				case SWT.FocusOut:
					item.setText(0, text.getText());
					text.dispose();
					break;
				case SWT.Traverse:
					switch (ev.detail) {
					case SWT.TRAVERSE_RETURN:
						item.setText(0, text.getText());
					case SWT.TRAVERSE_ESCAPE:
						text.dispose();
						ev.doit = false;
					}
					break;
				}
			}

		};

		text.addListener(SWT.FocusOut, textListener);
		text.addListener(SWT.Traverse, textListener);
		editor.setEditor(text, item, 0);
		text.setText(item.getText(0));
		text.selectAll();
		text.setFocus();
	}

	private String getTags() {
		StringBuilder sb = new StringBuilder();
		for (TableItem item : table.getItems()) {
			sb.append(item.getText().trim()).append(',');
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);

		ManageTagsDialog f = new ManageTagsDialog(shell);

		String tags = f.open("asd,rew");

		System.out.println("Tags "
				+ ("asd,rew".equals(tags) ? "not changed." : "has changed."));

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}
}
