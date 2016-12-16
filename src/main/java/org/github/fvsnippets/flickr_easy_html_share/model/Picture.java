package org.github.fvsnippets.flickr_easy_html_share.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.github.fvsnippets.flickr_easy_html_share.model.ShareSizeEnum.getByFlickr4JavaLabel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.flickr4java.flickr.photos.Photo;


public class Picture implements Comparable<Picture> {
	public static final String SIZE_NOT_FOUND_URL = "://www.google.com/images/errors/robot.png";
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
	
	private void addSize(Size size) {
    	sizes.put(size.shareSizeEnum, size);
	}
	
	public void clearSizes() {
		sizes.clear();
	}
	
	public String getPictureUrl(String pathAlias) {
		return "://www.flickr.com/photos/" + pathAlias + "/" + id + "/";
	}
	
	public int getHeight(ShareSizeEnum shareSizeEnum) {
		Size size = sizes.get(shareSizeEnum);
		if (size == null) {
			return -1;
		}
		
		return sizes.get(shareSizeEnum).getHeight();
	}
	
	private boolean hasSize(ShareSizeEnum shareSizeEnum) {
		return sizes.get(shareSizeEnum) != null;
	}
	
	public String getThumbnailUrl(ShareSizeEnum shareSizeEnum) {
		if (!hasSize(shareSizeEnum)) {
			return SIZE_NOT_FOUND_URL;
		}
		
		return "://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + shareSizeEnum.getSecret(this) + shareSizeEnum.getUrlSuffix() + "." + shareSizeEnum.getFormat(this);
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
	
	static class Size {
		private ShareSizeEnum shareSizeEnum;
		private int height;
		
		Size(int flickr4JavaLabel, int height) {
			checkArgument(height > 0);
			
			this.height = height;
			this.shareSizeEnum = getByFlickr4JavaLabel(flickr4JavaLabel);
		}
		
		private Size(com.flickr4java.flickr.photos.Size flickr4JavaSize) {
			this(flickr4JavaSize.getLabel(), flickr4JavaSize.getHeight());
		}
		
		private int getHeight() {
			return height;
		}
		
		protected Size() {
		}
	}
}
