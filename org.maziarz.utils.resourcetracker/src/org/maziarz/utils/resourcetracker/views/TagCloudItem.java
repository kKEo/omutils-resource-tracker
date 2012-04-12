package org.maziarz.utils.resourcetracker.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

public class TagCloudItem {
	private String path;
	private int style;
	private Object item;
	private String project;
	private String mdate;

	public TagCloudItem(String label) {
		this.path = label;
	}	
	
	public TagCloudItem(String path, String project, String mdate) {
		this.path = path;
		this.project = project;
		this.mdate = mdate;
	}

	public String getPath() {
		return path;
	}
	
	public String getProject() {
		return project;
	}
	
	public String getModificationDate() {
		return mdate;
	}

	public IResource getResource() {
		return ResourcesPlugin.getWorkspace()
				.getRoot().findMember(project + "/" + path);
	}
	
}