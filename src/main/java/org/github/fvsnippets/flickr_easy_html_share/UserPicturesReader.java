package org.github.fvsnippets.flickr_easy_html_share;

import static com.flickr4java.flickr.photos.Extras.DATE_TAKEN;
import static com.flickr4java.flickr.photos.Extras.DATE_UPLOAD;
import static com.flickr4java.flickr.photos.Extras.LAST_UPDATE;
import static com.flickr4java.flickr.photos.Extras.ORIGINAL_FORMAT;
import static com.flickr4java.flickr.photos.Extras.PATH_ALIAS;
import static com.flickr4java.flickr.photos.Extras.URL_L;
import static com.flickr4java.flickr.photos.Extras.URL_M;
import static com.flickr4java.flickr.photos.Extras.URL_O;
import static com.flickr4java.flickr.photos.Extras.URL_S;
import static com.flickr4java.flickr.photos.Extras.URL_SQ;
import static com.flickr4java.flickr.photos.Extras.URL_T;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.System.out;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.github.fvsnippets.flickr_easy_html_share.model.Book;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

public class UserPicturesReader {
	private static final int MAX_NUMBER_OF_PHOTOS_PER_PAGES = 100;
	private static final int FLICKR_MAX_NUMBER_OF_PHOTOS_TO_ITERATE_PER_SEARCH = 4000;
	private static final Set<String> EXTRAS = newHashSet(LAST_UPDATE, DATE_TAKEN, DATE_UPLOAD, ORIGINAL_FORMAT, URL_T, URL_S, URL_SQ, URL_M, URL_L, URL_O, "url_q", "url_n", "url_z", "url_c", "url_h", "url_k");
	
	private final Flickr flickr;
	
	UserPicturesReader(Flickr flickr) {
		this.flickr = checkNotNull(flickr);
	}

	private boolean isLastPage(PhotoList<Photo> photoList) {
		return photoList.getPage() >= photoList.getPages();
	}
	
	private boolean isLowerThanPageLimit(PhotoList<Photo> photoList) {
		return photoList.getPage() * photoList.getPerPage() < FLICKR_MAX_NUMBER_OF_PHOTOS_TO_ITERATE_PER_SEARCH - photoList.getPerPage() + 1;
	}
	
	private boolean continueExploringPages(PhotoList<Photo> photoList) {
		if (photoList == null) {
			return true;
		}
		
		return !isLastPage(photoList) && isLowerThanPageLimit(photoList);  
	}
	
	private String showDate(Date date) {
		return date == null ? "(none)" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}
	
	private void logSearchPictures(User user, PhotoList<Photo> photoList, Date minUploadDateLimit, Date maxUploadDateLimit) {
		out.println("Getting page #" + (photoList == null ? "1" : photoList.getPage() + 1) + " (pageSize: " + (MAX_NUMBER_OF_PHOTOS_PER_PAGES) + ") of photos "
				+ "from " + user.getUsername() + " uploaded after " + showDate(minUploadDateLimit)  + " and before " + showDate(maxUploadDateLimit));
	}
	
	private Date maxUploadDateLimit(Date currentMaxUploadDateLimit, Photo photo) {
		Date maxUploadDateLimit = currentMaxUploadDateLimit;
		if (maxUploadDateLimit == null || photo.getDatePosted().compareTo(maxUploadDateLimit) < 0) {
			maxUploadDateLimit = photo.getDatePosted(); 
		}
		
		return maxUploadDateLimit;
	}

	private void readPictures(User user, Book book, Date minUploadDateLimit) throws FlickrException {
		// @See comment on #readOrUpdatePictures(User,Book)

		boolean thereAreMorePictures = true;
		Date nextMaxUploadDateLimit = null;
		while (thereAreMorePictures) {
			Date maxUploadDateLimit = nextMaxUploadDateLimit;
			
			PhotoList<Photo> photoList = null;
			
			while (continueExploringPages(photoList)) {
				
				logSearchPictures(user, photoList, minUploadDateLimit, maxUploadDateLimit);
				SearchParameters params = new SearchParameters();
				params.setUserId(user.getId());;
				params.setMinUploadDate(minUploadDateLimit);
				params.setMaxUploadDate(maxUploadDateLimit);
				params.setExtras(EXTRAS);
				photoList = flickr.getPhotosInterface().search(params, MAX_NUMBER_OF_PHOTOS_PER_PAGES, photoList == null ? 1 : photoList.getPage() + 1);

				for (Photo photo : photoList) {
					nextMaxUploadDateLimit = maxUploadDateLimit(maxUploadDateLimit, photo);
					
					book.createOrUpdatePicture(photo);	
				}
				thereAreMorePictures = !isLastPage(photoList);
			}
		}
	}
	
	private void logSearchRecentlyUpdatedPictures(User user, PhotoList<Photo> photoList, Date minLastUpdateLimit) {
		out.println("Getting page #" + (photoList == null ? "1" : photoList.getPage() + 1) + " (pageSize: " + MAX_NUMBER_OF_PHOTOS_PER_PAGES + ") of photos "
				+ "from " + user.getUsername() + " updated after " + showDate(minLastUpdateLimit));
	}
	
	private boolean doUpdatePictures(User user, Book book) throws FlickrException {
		// @See comment on #readOrUpdatePictures(User,Book)
		
		Date minLastUpdateLimit = book.getLastUpdate();
		PhotoList<Photo> photoList = null;
		
		boolean thereAreMorePictures = true;
		while (thereAreMorePictures) {
			logSearchRecentlyUpdatedPictures(user, photoList, minLastUpdateLimit);
			photoList = flickr.getPhotosInterface().recentlyUpdated(minLastUpdateLimit, EXTRAS, MAX_NUMBER_OF_PHOTOS_PER_PAGES, photoList == null ? 1 : photoList.getPage() + 1);
			
			for (Photo photo : photoList) {
				book.createOrUpdatePicture(photo);	
			}
			thereAreMorePictures = !isLastPage(photoList);
		}
		
		return photoList.getTotal() < FLICKR_MAX_NUMBER_OF_PHOTOS_TO_ITERATE_PER_SEARCH;
	}
	
	void readOrUpdatePictures(User user, Book book) throws FlickrException {
		// The intricate algorithm to search and explore has to do with:
		// - The flickr API limits the number of results per search to 4000 (in theory). After this number the last page of photos starts repeating.
		// - There are reports in forums that using a sorting that is not the default (date-posted-desc) produce incorrect results.
		// - There are reports in forums that using a pageSize that is not the default (quantity varies by method) produce incorrect results.
		
		Date initialMaxUploadDate = book.getMaxUploadDate();
		
		if (book.getPictures().isEmpty()) {
			readPictures(user, book, book.getMaxUploadDate());
		} else {
			boolean completelyUpdated = doUpdatePictures(user, book);
			if (!completelyUpdated) {
				readPictures(user, book, initialMaxUploadDate);
			}	
		}
	}
	
	String pathAlias(String username) throws FlickrException {
		out.println("Getting pathAlias for user: " + username);
		return flickr.getPhotosInterface().recentlyUpdated(new Date(48 * 60 * 60 * 1024), newHashSet(PATH_ALIAS), 1, 1).get(0).getPathAlias();
	}
}
