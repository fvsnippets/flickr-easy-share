package org.github.fvsnippets.flickr_easy_html_share.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.github.fvsnippets.flickr_easy_html_share.model.ShareSizeEnum.getByFlickr4JavaLabel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.flickr4java.flickr.photos.Photo;


public class Picture implements Comparable<Picture> {
	private static final String UNNAMED_PICTURE = "unnamed-picture";
	
	private String id;
	private String title;
	private String farm;
	private String server;
	private String secret;
	private String originalSecret;
	private String originalFormat;
	private Date dateTaken;
	private Date lastUpdate;
	private Date dateUpload;
	private Map<ShareSizeEnum, Size> sizes;
	
	Picture(Photo photo) {
		this.sizes = new HashMap<ShareSizeEnum, Picture.Size>();
		this.id = photo.getId();
		update(photo);
	}
	
	protected Picture() {
	}

	boolean update(Photo photo) {
		boolean update = !photo.getLastUpdate().equals(lastUpdate);
		
		if (update) {
			setTitle(photo.getTitle());
			setFarm(photo.getFarm());
			setServer(photo.getServer());
			setSecret(photo.getSecret());
			setOriginalSecret(photo.getOriginalSecret());
			setOriginalFormat(photo.getOriginalFormat());
			setDateTaken(photo.getDateTaken());
			setDateUpload(photo.getDatePosted());
			setLastUpdate(photo.getLastUpdate());
			
			clearSizes();
	        addSize(new Size(photo.getLargeSize()));
	        addSize(new Size(photo.getMedium640Size()));
	        addSize(new Size(photo.getMedium800Size()));
	        addSize(new Size(photo.getMediumSize()));
	        addSize(new Size(photo.getOriginalSize()));
	        addSize(new Size(photo.getSmall320Size()));
	        addSize(new Size(photo.getSmallSize()));
	        addSize(new Size(photo.getSquareLargeSize()));
	        addSize(new Size(photo.getSquareSize()));
	        addSize(new Size(photo.getThumbnailSize()));
		}
		
		return update;
	}
	
	@Override
	public int compareTo(Picture o) {
		return dateTaken.compareTo(o.dateTaken);
	}
	
	public String getId() {
		return id;
	}
	
	private void setTitle(String title) {
		checkNotNull(title);
		String trimmedTitle = title.trim();
		this.title = trimmedTitle.isEmpty() ? UNNAMED_PICTURE : trimmedTitle;
	}

	public String getTitle() {
		return title;
	}
	
	private void setFarm(String farm) {
		checkArgument(!checkNotNull(farm).trim().isEmpty());
		this.farm = farm;
	}
	
	private void setServer(String server) {
		checkArgument(!checkNotNull(server).trim().isEmpty());
		this.server = server;
	}

	private void setSecret(String secret) {
		checkArgument(!checkNotNull(secret).trim().isEmpty());
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}

	private void setOriginalSecret(String originalSecret) {
		checkArgument(!checkNotNull(originalSecret).trim().isEmpty());
		this.originalSecret = originalSecret;
	}
	
	public String getOriginalSecret() {
		return originalSecret;
	}

	private void setOriginalFormat(String originalFormat) {
		checkArgument(!checkNotNull(originalFormat).trim().isEmpty());
		this.originalFormat = originalFormat;
	}
	
	public String getOriginalFormat() {
		return originalFormat;
	}
	
	private void setDateTaken(Date dateTaken) {
		this.dateTaken = checkNotNull(dateTaken);
	}

	private void setDateUpload(Date dateUpload) {
		this.dateUpload = checkNotNull(dateUpload);
	}
	
	public Date getDateUpload() {
		return dateUpload;
	}
	
	private void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = checkNotNull(lastUpdate);
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	public void addSize(Size size) {
    	sizes.put(size.shareSizeEnum, size);
	}
	
	public void clearSizes() {
		sizes.clear();
	}
	
	private String getPictureUrl(String pathAlias) {
		return "http://www.flickr.com/photos/" + pathAlias + "/" + id + "/";
	}
	
	public String getCurrentPictureUrl(ShareSizeEnum shareSizeEnum) {
		return "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + shareSizeEnum.getSecret(this) + shareSizeEnum.getUrlSuffix() + "." + shareSizeEnum.getFormat(this);
	}
	
	public String getShare(ShareSizeEnum shareSizeEnum, String pathAlias, String linkTarget, String linkTitlePrefix) {
		Size size = sizes.get(shareSizeEnum);
		
		if (size == null) {
			return "Size not available.";
		}
		
		return "<a href=\"" + getPictureUrl(pathAlias) + "\" title=\"" + linkTitlePrefix + title + "\" target=\"" + linkTarget + "\"><img title=\""+ linkTitlePrefix + "\" alt=\"" + title + "\" width=\"" +
				size.width + "\" height=\"" + size.height + "\" src=\"" + getCurrentPictureUrl(shareSizeEnum) + "\"></a>";  
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (!(obj instanceof Picture)) { return false; }
			
		Picture rhs = (Picture) obj;
		
		return id.equals(rhs.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public static class Size {
		private ShareSizeEnum shareSizeEnum;
		private int width;
		private int height;
		
		Size(int flickr4JavaLabel, int width, int height) {
			checkArgument(width > 0);
			checkArgument(height > 0);
			
			this.width = width;
			this.height = height;
			this.shareSizeEnum = getByFlickr4JavaLabel(flickr4JavaLabel);
		}
		
		public Size(com.flickr4java.flickr.photos.Size flickr4JavaSize) {
			this(flickr4JavaSize.getLabel(), flickr4JavaSize.getWidth(), flickr4JavaSize.getHeight());
		}
		
		protected Size() {
		}
	}
}
