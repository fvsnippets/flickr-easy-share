package org.github.fvsnippets.flickr_easy_html_share.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.flickr4java.flickr.photosets.Photoset;
import com.google.common.collect.Ordering;

public class Album implements Iterable<Picture>, Comparable<Album> {
	private String id;
	private String name;
	private List<Picture> pictures;
	private Set<Picture> picturesSet;
	private Date dateCreate;
	
	Album(Photoset photoset) {
		this.id = photoset.getId();

		this.pictures = newArrayList();
		this.picturesSet = newHashSet();
		
		setName(photoset.getTitle());
		setDateCreate(photoset.getDateCreate());
	}
	
	protected Album() {
	}

	@Override
	public Iterator<Picture> iterator() {
		return Ordering.natural().sortedCopy(pictures).iterator();
	}

	@Override
	public int compareTo(Album o) {
		return -dateCreate.compareTo(o.dateCreate);
	}
	
	public String getId() {
		return id;
	}

	private void setName(String name) {
		checkArgument(!checkNotNull(name).trim().isEmpty());
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	private void setDateCreate(long dateCreate) {
		setDateCreate(new Date(dateCreate));
	}
	
	private void setDateCreate(String dateCreate) {
		if (dateCreate == null || "".equals(dateCreate)) {
			return;
		}
		setDateCreate(Long.parseLong(dateCreate) * 1000);
	}
	
	private void setDateCreate(Date dateCreate) {
		this.dateCreate = checkNotNull(dateCreate);
	}
	
	public void addPicture(Picture picture) {
		checkArgument(!picturesSet.contains(picture));
		pictures.add(picture);
		picturesSet.add(picture);
	}
	
	// XStream
	private Object readResolve() {
		picturesSet = new HashSet<Picture>(pictures);
		
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (!(obj instanceof Album)) { return false; }
			
		Album rhs = (Album) obj;
		
		return id.equals(rhs.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
