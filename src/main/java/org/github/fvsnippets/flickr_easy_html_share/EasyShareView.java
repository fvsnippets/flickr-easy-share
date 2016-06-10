package org.github.fvsnippets.flickr_easy_html_share;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.github.fvsnippets.flickr_easy_html_share.model.Album;
import org.github.fvsnippets.flickr_easy_html_share.model.Book;
import org.github.fvsnippets.flickr_easy_html_share.model.Picture;
import org.github.fvsnippets.flickr_easy_html_share.model.ShareSizeEnum;

public class EasyShareView {
	private static final String THUMBNAILS_DIR = "thumbnails";
	private static final String ALBUMS_HTML_DIR = "albums";
	private static final int THUMBNAIL_SUBDIR_CHARCOUNT = 3;
	private static final int ALBUM_COLUMNS = 3;
	private final File workingDirectory;
	private final ShareSizeEnum shareSizeEnum;
	private final Book book;

	public EasyShareView(File workingDirectory, ShareSizeEnum shareSizeEnum, Book book) {
		this.workingDirectory = checkNotNull(workingDirectory);
		this.shareSizeEnum = checkNotNull(shareSizeEnum);
		this.book = checkNotNull(book);
	}
	
	private String getDirEnsureBashScript(File directory) {
		String absolutePath = directory.getAbsolutePath();
		return
				"if [ -e  \"" + absolutePath + "\" ]; then \n" +
				"  if [ ! -d  \"" + absolutePath + "\" ]; then \n" +
				"    echo \"Path " + absolutePath + " exists and is not a directory.\" \n" +
				"    exit 1 \n" +
				"  fi \n" +
				"else \n" +
				"  mkdir \"" + absolutePath + "\" \n" +
				" if [ $? -ne 0 ]; then \n" +
				"   echo \"Can't create " + absolutePath + " dir.\" \n" +
				"   exit 1 \n" +
				" fi \n" +
				"fi \n";
	}
	
	private String getThumbnailsSubdirName(Picture picture) {
		String pictureId = picture.getId();
		
		int size = pictureId.length() >= THUMBNAIL_SUBDIR_CHARCOUNT ? THUMBNAIL_SUBDIR_CHARCOUNT : pictureId.length();
		
		return pictureId.substring(pictureId.length() - size, pictureId.length());
	}
	
	
	private String getWgetScript(File thumbnailsDirectory, Picture picture, int picturesLeft) {
		String thumbnailsSubdirName = getThumbnailsSubdirName(picture);
		File thumbnailsSubdir = new File(thumbnailsDirectory, thumbnailsSubdirName);
		String file = thumbnailsSubdir.getAbsolutePath() + "/" + picture.getId() + ".jpg";
		String tempFile = thumbnailsSubdir + "/deletemesafely.jpg";
		return 
				"if [ ! -e \"" + file + "\" ]; then \n" +
				"  echo \"Pictures left to download: " + picturesLeft + ".\" \n" +
				"  wget -O \"" + tempFile + "\" \"" + picture.getCurrentPictureUrl(shareSizeEnum) + "\" && mv \"" + tempFile + "\" \"" + file + "\" \n" +
				"fi \n";
	}
	
	public String bashDownloadScript() {
		StringBuilder script = new StringBuilder();
		
		File thumbnailsDirectory = new File(workingDirectory, THUMBNAILS_DIR);
		script.append("#!/bin/bash \n");
		script.append("if [ ! -d \"" + workingDirectory + "\" ]; then \n");
		script.append("  echo \"Path " + workingDirectory + " is not a directory.\" \n");
		script.append("  exit 1 \n");
		script.append("fi \n");
		script.append(getDirEnsureBashScript(thumbnailsDirectory));

		Set<String> thumbnailsSubdirNames = new HashSet<String>();
		
		for (Picture picture : book.getPictures()) {
			String thumbnailsSubdirName = getThumbnailsSubdirName(picture);
			thumbnailsSubdirNames.add(thumbnailsSubdirName);
		}
		
		for (String thumbnailsSubdirName : thumbnailsSubdirNames) {
			File thumbnailsSubdir = new File(thumbnailsDirectory, thumbnailsSubdirName);
			script.append(getDirEnsureBashScript(thumbnailsSubdir));	
		}
		
		int picturesLeft = book.getPictures().size();
		for (Picture picture : book.getPictures()) {
			script.append(getWgetScript(thumbnailsDirectory, picture, picturesLeft));
			picturesLeft--;
		}
		
		return script.toString();
	}
	
	public String albumHtml(Album album, ShareSizeEnum expectedThumbnailSize, String pathAlias, String linkTarget, String linkTitlePrefix) {
		StringBuilder html = new StringBuilder();
		
		html.append("<html> \n");
		html.append("<head> \n");
		html.append("<title>" + album.getName() + "</title> \n");
		html.append("<script type=\"text/javascript\"> \n");
		html.append("function loadShares() { \n");
		html.append("  var size = document.getElementById(\"sizes\").options[document.getElementById(\"sizes\").selectedIndex].value; \n");
		html.append("  var textareas = document.getElementsByTagName(\"textarea\"); \n");
		html.append("  for (var i = 0; i < textareas.length; i++) { \n");
		html.append("    var pictureId = textareas[i].id; \n");
		html.append("    textareas[i].value = unescape(document.getElementById(\"hidden_\" + pictureId + \"_\" + size).value); \n");
		html.append("  } \n");
		html.append("} \n");
		html.append("</script> \n");
		html.append("</head> \n");
		html.append("<body onload=\"loadShares();\"> \n");
		html.append("<h1>" + escapeHtml4(album.getName()) + "</h1> \n");
		html.append("<select id=\"sizes\" type=\"select\" onchange=\"loadShares();\"> \n");
		for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
			html.append("<option value=\"" + shareSizeEnum.name() + "\">" + shareSizeEnum.getDescription() + "</option> \n");
		}
		html.append("</select> \n");
		html.append("<p>&nbsp;</p> \n");
		
		html.append("<div style=\"display: table; width: 100%; \"> \n");
		html.append("<div style=\"display: table-row; width: 100%; \"> \n");
		int pictureNumber = 0;
		for (Picture picture : album) {
			String pictureId = picture.getId();
			
			String thumbnailsSubdirName = getThumbnailsSubdirName(picture);
			String thumbnailPath = "../" + THUMBNAILS_DIR + "/" + thumbnailsSubdirName + "/" + pictureId + ".jpg"; 
			
			html.append("<div style=\"display: table-cell; width: 33%;\"> \n");
			html.append("  <img alt=\"" + escapeHtml4(picture.getTitle()) + "\" onclick=\"document.getElementById('" + pictureId + "').select();\" src=\"" + thumbnailPath + "\"> \n");
			html.append("  <br> \n");
			for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
				html.append("  <input type=\"hidden\" value=\"" + escapeHtml4(picture.getShare(shareSizeEnum, pathAlias, linkTarget, linkTitlePrefix)) + "\" id=\"hidden_" + pictureId + "_" + shareSizeEnum.name() + "\"> \n");
			}
			html.append("  <textarea onclick=\"document.getElementById('" + pictureId + "').blur(); document.getElementById('" + pictureId + "').select();\" style=\"width: " + expectedThumbnailSize.getMaxWidth() + ";\" rows=\"2\" id=\"" + pictureId + "\">&nbsp;</textarea> \n");
			html.append("  <br><br> \n");
			html.append("</div> \n");
			
			pictureNumber++;
			if (pictureNumber % ALBUM_COLUMNS == 0) {
				html.append("</div> \n");
				html.append("<div style=\"display: table-row;\"> \n");
			}
		}

		html.append("</div> \n");
		html.append("</div> \n");
		html.append("</body> \n");
		html.append("</html> \n");
		
		return html.toString();
	}
	
	public String albumsIndexHtml() {
		StringBuilder html = new StringBuilder();
		
		html.append("<html> \n");
		html.append("<head> \n");
		html.append("<title>Albums by " + book.getUsername() + "</title> \n");
		html.append("</head> \n");
		html.append("<body> \n");
		
		for (Album album : book) {
			html.append("<p style=\"margin-bottom: 0; margin-top: 0;\"> \n");
			html.append("<a href=\"" + ALBUMS_HTML_DIR + "/" + album.getId() + ".html\">" + escapeHtml4(album.getName()) + "</a> \n");
			html.append("</p> \n");
		}

		html.append("</body> \n");
		html.append("</html> \n");
		
		return html.toString();
	}
}
