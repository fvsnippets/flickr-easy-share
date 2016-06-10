package org.github.fvsnippets.flickr_easy_html_share.model;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableList;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photosets.Photoset;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

public class Book implements Iterable<Album> {
	private List<Album> albums;
	private List<Picture> pictures;
	private transient Map<String, Album> albumsMap;
	private transient Map<String, Picture> picturesMap;
	private Date lastUpdate;
	private Date maxUploadDate;
	private String username;
	private String pathAlias;
	
	public Book(String username) {
		this.albums = newArrayList();
		this.albumsMap = newHashMap();
		this.pictures = newArrayList();
		this.picturesMap = newHashMap();
		
		this.lastUpdate = new Date(0);
		this.maxUploadDate = new Date(0);
		
		this.username = checkNotNull(username);
		checkArgument(!username.trim().isEmpty());
	}
	
	protected Book() {
	}
	
	public Picture createOrUpdatePicture(Photo photo) {
		checkArgument(!checkNotNull(checkNotNull(photo).getId()).trim().isEmpty());
		
		Picture picture;
		if (knowsPicture(photo.getId())) {
			picture = getPicture(photo.getId()).get();
			picture.update(photo);
		} else {
			picture = new Picture(photo);
			add(picture);
		}
		
		return picture;
	}
	
	public Album createAlbum(Photoset photoset) {
		checkArgument(!checkNotNull(checkNotNull(photoset).getId()).trim().isEmpty());
		checkArgument(!knowsAlbum(photoset.getId()));
		
		Album album = new Album(photoset);
		add(album);

		return album;
	}
	
	private boolean knowsAlbum(String albumId) {
		return albumsMap.containsKey(checkNotNull(albumId));
	}

	public boolean knowsPicture(String pictureId) {
		return picturesMap.containsKey(checkNotNull(pictureId));
	}
	
	public Optional<Picture> getPicture(String pictureId) {
		return fromNullable(picturesMap.get(pictureId));
	}
	
	public Optional<Album> getAlbum(String albumId) {
		return fromNullable(albumsMap.get(albumId));
	}
	
	public String getUsername() {
		return username;
	}

	public void setPathAlias(String pathAlias) {
		this.pathAlias = checkNotNull(pathAlias);
		checkArgument(!pathAlias.trim().isEmpty());
	}
	
	public String getPathalias() {
		return pathAlias;
	}
	
	public List<Picture> getPictures() {
		return unmodifiableList(pictures);
	}
	
	private void add(Album album) {
		checkNotNull(album);
		checkArgument(!knowsAlbum(album.getId()));
		
		albums.add(album);
		albumsMap.put(album.getId(), album);
	}
	
	private void tryUpdateLastUpdate(Picture picture) {
		if (picture.getLastUpdate().compareTo(this.lastUpdate) > 0) {
			this.lastUpdate = picture.getLastUpdate();
		}
	}
	
	private void tryUpdateMaxUploadDate(Picture picture) {
		if (picture.getDateUpload().compareTo(this.maxUploadDate) > 0) {
			this.maxUploadDate = picture.getDateUpload(); 
		}
	}
	
	private void add(Picture picture) {
		checkNotNull(picture);
		checkArgument(!knowsAlbum(picture.getId()));
		
		tryUpdateLastUpdate(picture);
		tryUpdateMaxUploadDate(picture);

		pictures.add(picture);
		picturesMap.put(picture.getId(), picture);
	}

	public void clearAlbums() {
		albums.clear();
		albumsMap.clear();
	}
	
	@Override
	public Iterator<Album> iterator() {
		return Ordering.natural().sortedCopy(albums).iterator();
	}
	
	// XStream
	private Object readResolve() {
		picturesMap = new HashMap<String, Picture>();
		for (Picture picture : pictures) {
			picturesMap.put(picture.getId(), picture);
		}
		
		albumsMap = new HashMap<String, Album>();
		for (Album album : albums) {
			albumsMap.put(album.getId(), album);
		}
		
		return this;
	}
	
	public Date getMaxUploadDate() {
		return maxUploadDate;
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}
}
