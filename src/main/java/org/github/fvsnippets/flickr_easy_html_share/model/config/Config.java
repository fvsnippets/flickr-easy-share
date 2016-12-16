package org.github.fvsnippets.flickr_easy_html_share.model.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.github.fvsnippets.flickr_easy_html_share.model.ShareSizeEnum;

public class Config {
	private String username;
	private String apiKey;
	private String sharedSecret;
	private String workingDirectoryPath;
	private ShareSizeEnum thumbnailSize;
	
	private boolean isValidDirectory(File directory) {
		return directory.isDirectory() && directory.exists();
	}
	
	public Config(String username, String apiKey, String sharedSecret, String workingDirectoryPath, ShareSizeEnum thumbnailSize) {
		checkArgument(!checkNotNull(username).trim().isEmpty());
		checkArgument(!checkNotNull(workingDirectoryPath).trim().isEmpty());
		checkArgument(!checkNotNull(apiKey).trim().isEmpty());
		checkArgument(!checkNotNull(sharedSecret).trim().isEmpty());
		
		this.username = username;
		this.apiKey = apiKey;
		this.sharedSecret = sharedSecret;
		this.workingDirectoryPath = workingDirectoryPath;
		this.thumbnailSize = checkNotNull(thumbnailSize);
		
		checkArgument(isValidDirectory(getWorkingDirectory()));
	}
	
	protected Config() {
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public String getSharedSecret() {
		return sharedSecret;
	}
	
	public File getWorkingDirectory() {
		return new File(workingDirectoryPath);
	}
	
	public ShareSizeEnum getThumbnailSize() {
		return thumbnailSize;
	}
}
