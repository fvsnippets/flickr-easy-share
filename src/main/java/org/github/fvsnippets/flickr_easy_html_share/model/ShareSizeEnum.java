package org.github.fvsnippets.flickr_easy_html_share.model;

import static com.flickr4java.flickr.photos.Size.LARGE;
import static com.flickr4java.flickr.photos.Size.LARGE_1600;
import static com.flickr4java.flickr.photos.Size.LARGE_2048;
import static com.flickr4java.flickr.photos.Size.MEDIUM;
import static com.flickr4java.flickr.photos.Size.MEDIUM_640;
import static com.flickr4java.flickr.photos.Size.MEDIUM_800;
import static com.flickr4java.flickr.photos.Size.ORIGINAL;
import static com.flickr4java.flickr.photos.Size.SMALL;
import static com.flickr4java.flickr.photos.Size.SMALL_320;
import static com.flickr4java.flickr.photos.Size.SQUARE;
import static com.flickr4java.flickr.photos.Size.SQUARE_LARGE;
import static com.flickr4java.flickr.photos.Size.THUMB;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

public enum ShareSizeEnum {
	SQ(SQUARE, "_s", "75x75", 75),
	T(THUMB, "_t", "100x20", 100),
	Q(SQUARE_LARGE, "_q", "150x150", 150),
	S(SMALL, "_m", "240", 240),
	N(SMALL_320, "_n", "320", 320),
	M(MEDIUM, "", "500", 500),
	Z(MEDIUM_640, "_z", "640", 640),
	C(MEDIUM_800, "_c", "800", 800),
	L(LARGE, "_b", "1024", 1024),
	H(LARGE_1600, "_h", "1600", 1600) {
		@Override
		public String getSecret(Picture picture) {
			return picture.getOriginalSecret();
		}
	},
	K(LARGE_2048, "_k", "2048", 2048) {
		@Override
		public String getSecret(Picture picture) {
			return picture.getOriginalSecret();
		}
	},
	O(ORIGINAL, "_o", "Original", 0) {
		@Override
		public String getSecret(Picture picture) {
			return picture.getOriginalSecret();
		}
		
		@Override
		public String getFormat(Picture picture) {
			return picture.getOriginalFormat();
		}
	};
	
	
	private final int flickr4JavaLabel;
	private final String urlSuffix;
	private final String description;
	private final int width;
	
	private ShareSizeEnum(int flickr4JavaLabel, String urlSuffix, String description, int width) {
		this.flickr4JavaLabel = flickr4JavaLabel;
		this.urlSuffix = urlSuffix;
		this.description = description;
		this.width = width;
	}
	
	public String getUrlSuffix() {
		return urlSuffix;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getWidth() {
		return width;
	}
	
	public String getSecret(Picture picture) {
		return picture.getSecret();
	}
	
	public String getFormat(Picture picture) {
		return "jpg";
	}
	
	private static final Map<Integer, ShareSizeEnum> FLICKR4JAVA_LABEL_TO_SHARESIZEENUM = new HashMap<Integer, ShareSizeEnum>();
	static {
		for (ShareSizeEnum shareSizeEnum : values()) {
			FLICKR4JAVA_LABEL_TO_SHARESIZEENUM.put(shareSizeEnum.flickr4JavaLabel, shareSizeEnum);			
		}
	}
	
	public static ShareSizeEnum getByFlickr4JavaLabel(int flickr4JavaLabel) {
		return checkNotNull(FLICKR4JAVA_LABEL_TO_SHARESIZEENUM.get(flickr4JavaLabel));
	}
}
