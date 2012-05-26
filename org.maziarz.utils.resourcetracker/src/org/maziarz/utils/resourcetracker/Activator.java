package org.maziarz.utils.resourcetracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.maziarz.utils.resourcetracker.views.ResourcesCloudView;
import org.maziarz.utils.resourcetracker.views.TagCloudItem;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.maziarz.edu.resources"; //$NON-NLS-1$

	private static Activator plugin;

	private IResourceChangeListener resourceChangeListener;
	private ResourceHistoryStore store;
	
	private ResourcesCloudView view;

	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		IPath stateLocation = getStateLocation();
		store = new ResourceHistoryStore(stateLocation);
		
		resourceChangeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {

				if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
					return;
				}

				IResourceDelta delta = event.getDelta();

				final List<IResourceDelta> filesAffected = new ArrayList<IResourceDelta>();

				try {
					delta.accept(new IResourceDeltaVisitor() {

						@Override
						public boolean visit(IResourceDelta delta)
								throws CoreException {

							if (delta.getResource() instanceof IFile) {
								String ext = delta.getResource()
										.getFileExtension();
								if (ext != null && ext.equals("class")) {
									return false;
								}
								IFile f = ((IFile)delta.getResource());
								if ("bin".equals(f.getProjectRelativePath().segment(0))){
									return false;
								}
								filesAffected.add(delta);
							}

							return (delta.getResource() instanceof IContainer);
						}
					});
				} catch (CoreException e1) {
					e1.printStackTrace();
				}

				for (IResourceDelta f : filesAffected) {
					IFile file = ((IFile) f.getResource());

					store.addChange(file);
					
					Object o = store.getLastModified(null, null, 10);
					notifyChange(o);

					System.out.println("File: " + file + " - "
							+ (new Date(file.getLocalTimeStamp())).toString());

					// try {
					// for (IFileState h : file.getHistory(null)) {
					// System.out.println(h.getName()
					// + " - "
					// + (new Date(h.getModificationTime()))
					// .toString());
					// }
					// } catch (CoreException e) {
					// e.printStackTrace();
					// }
					// if ((f.getFlags() & IResourceDelta.CONTENT) == 0)
				}
			}

		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceChangeListener, IResourceChangeEvent.POST_CHANGE);

	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

		store.dispose();
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
				resourceChangeListener);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	public ResourceHistoryStore getStore() {
		return store;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void error(String message, Throwable t) {
		if (plugin != null) {
			plugin.getLog()
					.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK,
							message, t));
		} else {
			System.err.println(message);
			t.printStackTrace();
		}
	}

	public static void info(String message) {
		plugin.getLog().log(
				new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, message, null));
	}

	public static void warn(String message) {
		plugin.getLog().log(
				new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message,
						null));
	}

	public static void warn(String message, Throwable t) {
		plugin.getLog().log(
				new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, t));
	}

	public void setView(ResourcesCloudView view) {
		this.view = view;
	}
	
	public void notifyChange(final Object obj){
		view.notifyChange(obj);
	}
	
	public TagCloudItem[] getTagCloudItems(String pattern) {
		List<TagCloudItem> items= store.getLastModified(pattern, null, 10);
		return items.toArray(new TagCloudItem[0]);
	}

}
