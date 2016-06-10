package org.github.fvsnippets.flickr_easy_html_share;

import static com.flickr4java.flickr.Flickr.PRIVACY_LEVEL_NO_FILTER;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.logging.LogFactory.getLog;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.github.fvsnippets.flickr_easy_html_share.model.Album;
import org.github.fvsnippets.flickr_easy_html_share.model.Book;
import org.github.fvsnippets.flickr_easy_html_share.model.Picture;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;

public class UserAlbumsReader {
	private static final Log LOGGER = getLog(UserAlbumsReader.class);
	private static final int MAX_NUMBER_OF_PHOTOS_PER_PAGES = 500;
	
	private final Flickr flickr;
	
	UserAlbumsReader(Flickr flickr) {
		this.flickr = checkNotNull(flickr);
	}
	
	private void checkPictureBelongsToBook(String pictureId, Album album, Book book) {
		if (!book.knowsPicture(pictureId)) {
			throw new IllegalArgumentException("Album knows pictureId: " + pictureId + " but doesn't belong to Book");
		}
	}
	
	private boolean isLastPage(PhotoList<Photo> photoList) {
		return photoList.getPage() >= photoList.getPages();
	}
	
	private void logGetAlbumPictures(Photoset photoset, PhotoList<Photo> photoList) {
		LOGGER.info("Getting page #" + (photoList == null ? 1 : photoList.getPage() + 1) + " (pageSize:" + MAX_NUMBER_OF_PHOTOS_PER_PAGES + ") of photos from albumId:" 
				+ photoset.getId() + " (albumName: " + photoset.getTitle() + ")");
	}
	
	private void readPicturesOnAlbum(Book book, Photoset photoset, Album album) throws FlickrException {
		PhotoList<Photo> photoList = null;
		
		boolean thereAreMorePictures = true;
		while (thereAreMorePictures) {
			logGetAlbumPictures(photoset, photoList);
			photoList = flickr.getPhotosetsInterface().getPhotos(photoset.getId(), Collections.<String>emptySet(), PRIVACY_LEVEL_NO_FILTER, MAX_NUMBER_OF_PHOTOS_PER_PAGES, photoList == null ? 1 : photoList.getPage() + 1);

			for (Photo photo : photoList) {
				String pictureId = photo.getId();
				checkPictureBelongsToBook(pictureId, album, book);
				
				Picture picture = book.getPicture(pictureId).get();
				album.addPicture(picture);
			}
			
			thereAreMorePictures = !isLastPage(photoList);
		}
	}
	
	private boolean isLastPage(Photosets photosets) {
		return photosets.getPage() >= photosets.getPages();
	}
	
	private void logGetAlbums(Book book, Photosets photosets) {
		LOGGER.info("Getting page #" + (photosets == null ? 1 : photosets.getPage() + 1) + " (pageSize:" + MAX_NUMBER_OF_PHOTOS_PER_PAGES + ") of albums from user:" 
				+ book.getUsername());
	}
	
	void readAlbums(User user, Book book) throws FlickrException {
		book.clearAlbums();
		
		Photosets photosets = null;
		
		boolean thereAreMoreAlbums = true;
		while (thereAreMoreAlbums) {
		
			logGetAlbums(book, photosets);
			photosets = flickr.getPhotosetsInterface().getList(user.getId(), MAX_NUMBER_OF_PHOTOS_PER_PAGES, photosets == null ? 1 : photosets.getPage() + 1, "");
			
			for (Photoset photoset : photosets.getPhotosets()) {
				Album album = book.createAlbum(photoset);
				readPicturesOnAlbum(book, photoset, album);
			}
			
			thereAreMoreAlbums = !isLastPage(photosets);
		}
	}
}
