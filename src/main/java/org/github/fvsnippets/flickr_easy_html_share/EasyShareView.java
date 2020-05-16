package org.github.fvsnippets.flickr_easy_html_share;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.github.fvsnippets.flickr_easy_html_share.model.Picture.SIZE_NOT_FOUND_URL;

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
				"  wget -O \"" + tempFile + "\" \"http" + picture.getThumbnailUrl(shareSizeEnum) + "\" && mv \"" + tempFile + "\" \"" + file + "\" \n" +
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
	
	public String albumHtml(Album album, ShareSizeEnum expectedThumbnailSize, String pathAlias) {
		StringBuilder html = new StringBuilder();
		
		html.append("<html> \n");
		html.append("<head> \n");
		html.append("<title>" + album.getName() + "</title> \n");
		html.append("<script type=\"text/javascript\"> \n");
		html.append("\n");
		for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
			html.append("var width_" + shareSizeEnum.name() + " = " + shareSizeEnum.getWidth() + "; \n");
		}
		html.append("function executeShareScript() { \n");
		html.append("  // var _protocol = document.getElementById('protocols').options[document.getElementById('protocols').selectedIndex].value; \n");
		html.append("  // only https is available\n");
		html.append("  var _protocol = 'https'; \n");
		html.append("\n");
		html.append("  var _shareScript = document.getElementById('custom_share').value; \n");
		html.append("  if (_shareScript.length == 0) { \n");
		html.append("    return; \n");
		html.append("  } \n");
		html.append("\n");
		html.append("  var _textareas = document.getElementsByTagName('textarea'); \n");
		html.append("  for (var _currentTextAreaIndex = 0; _currentTextAreaIndex < _textareas.length; _currentTextAreaIndex++) { \n"); 
		html.append("    var _pictureId = _textareas[_currentTextAreaIndex].id; \n");
		html.append("    if (_pictureId != 'custom_share') { \n");
		html.append("      var pictureUrl = _protocol + document.getElementById('hidden_' + _pictureId + '_url').value; \n"); 
		html.append("      var pictureTitle = document.getElementById('hidden_' + _pictureId + '_title').value; \n");
		html.append("\n");
		for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
			html.append("      var url_" + shareSizeEnum.name() + " = _protocol + document.getElementById('hidden_' + _pictureId + '_' + '" + shareSizeEnum.name() + "_url').value; \n");
			html.append("      var height_" + shareSizeEnum.name() + " = document.getElementById('hidden_' + _pictureId + '_' + '" + shareSizeEnum.name() + "_height').value; \n");
			html.append("\n");
		}
		html.append("      try { \n");
		html.append("        eval(_shareScript); \n");
		html.append("\n");
		html.append("        if (share.includes('" + SIZE_NOT_FOUND_URL + "')) { \n");
		html.append("          share = 'Size not available'; \n"); 
		html.append("        } \n");
		html.append("\n");
		html.append("      _textareas[_currentTextAreaIndex].value = unescape(share); \n");
		html.append("      } catch (err) { \n");
		html.append("        alert(err + '\\nDetail: Current pictureId was ' + _pictureId); \n");
		html.append("        throw err; \n");
		html.append("      } \n");
		html.append("    } \n");
		html.append("  } \n");
		html.append("} \n");
		html.append("// Taken from http://stackoverflow.com/questions/5796718/html-entity-decode \n");
		html.append("var decodeEntities = (function() { \n");
		html.append("  // this prevents any overhead from creating the object each time \n");
		html.append("  var element = document.createElement('div'); \n");
		html.append("\n");
		html.append("  function decodeHTMLEntities (str) { \n");
		html.append("    if(str && typeof str === 'string') { \n");
		html.append("      // strip script/html tags \n");
		html.append("      str = str.replace(/<script[^>]*>([\\S\\s]*?)<\\/script>/gmi, ''); \n");
		html.append("      str = str.replace(/<\\/?\\w(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>/gmi, ''); \n");
		html.append("      element.innerHTML = str; \n");
		html.append("      str = element.textContent; \n");
		html.append("      element.textContent = ''; \n");
		html.append("    } \n");
		html.append("\n");
		html.append("    return str; \n");
		html.append("  } \n");
		html.append("\n");
		html.append("  return decodeHTMLEntities; \n");
		html.append("})(); \n");
		html.append("</script> \n");
		html.append("</head> \n");
		
		html.append("<body> \n");
		html.append("<h1>" + escapeHtml4(album.getName()) + "</h1> \n");
		
		html.append("<!-- \n");
		html.append("BEGIN: only https is available \n"); 
		html.append("protocol: \n"); 
		html.append("<select id=\"protocols\" type=\"select\"> \n"); 
		html.append("  <option value=\"http\">http</option> \n");
		html.append("  <option value=\"https\" selected=\"selected\">https</option> \n");
		html.append("</select> \n");
		html.append("END: only https is available \n");
		html.append("--> \n");
		
		html.append("Build your own!:<br> \n");
		html.append("<textarea width=\"800\" height=\"200\" rows=\"4\" cols=\"100\" id=\"custom_share\"></textarea> \n");
		html.append("<br> \n");
		html.append("<button onclick=\"executeShareScript();\" type=\"button\">Update</button> \n");
		html.append("<br> \n");
		html.append("<br> \n");
		html.append("<u>Quick tutorial</u>:<br> \n");
		html.append("Build your own chain using javascript and leave it (the chain) inside variable &quot;share&quot;. URLs, widths and heights are available inside special predefined variables.<br> \n");
		html.append("<br> \n");
		html.append("<i>Predefined variables are</i>:<br> \n");
		html.append("pictureUrl ; pictureTitle ; url_${size} ; width_${size} ; height_${size}<br> \n");
		html.append("<br> \n");
		html.append("Where ${size} is one of: <br> \n");
		for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
			html.append("<b>" + shareSizeEnum.name() + "</b> (" + shareSizeEnum.getDescription() + "), ");
		}
		html.deleteCharAt(html.length() - 1);
		html.append("\n");
		html.append("<br> \n");
		html.append("i.e.: available data for 640 is inside predefined variables url_z, width_z and height_z \n");
		html.append("<br> \n");
		
		html.append("<div id=\"example1\" style=\"visibility: hidden; margin: 0; padding: 0; height: 0;\">share = &apos;&lt;a href=&quot;&apos; + pictureUrl + &apos;&quot; title=&quot;Expand &apos; + pictureTitle + &apos;&quot;&gt;&lt;img title=&quot;&apos; + pictureTitle + &apos;&quot; alt=&quot;&apos; + pictureTitle + &apos;&quot; src=&quot;&apos; + url_Z + &apos;&quot; /&gt;&lt;/a&gt&apos;;</div> \n");
		html.append("<div id=\"example2\" style=\"visibility: hidden; margin: 0; padding: 0; height: 0;\">share = &apos;&lt;a href=&quot;&apos; + pictureUrl + &apos;&quot; title=&quot;Expand &apos; + pictureTitle + &apos;&quot;&gt;&lt;img title=&quot;&apos; + pictureTitle + &apos;&quot; alt=&quot;&apos; + pictureTitle + &apos;&quot; src=&quot;&apos; + url_Z + &apos;&quot; widht=&quot;&apos; + width_Z + &apos;&quot; height=&quot;&apos; + height_Z + &apos;&quot; /&gt;&lt;/a&gt&apos;;</div> \n");
		html.append("<div id=\"example3\" style=\"visibility: hidden; margin: 0; padding: 0; height: 0;\">share = &apos;&lt;a href=&quot;&apos; + pictureUrl + &apos;&quot; title=&quot;Expand &apos; + pictureTitle + &apos;&quot;&gt;&lt;img title=&quot;&apos; + pictureTitle + &apos;&quot; alt=&quot;&apos; + pictureTitle + &apos;&quot; src=&quot;&apos; + url_Z + &apos;&quot; srcset=&quot;&apos; + url_S + &apos; &apos; + width_S + &apos;w, &apos; + url_N + &apos; &apos; + width_N + &apos;w, &apos; + url_M + &apos; &apos; + width_M + &apos;w, &apos; + url_Z + &apos; &apos; + width_Z + &apos;w, &apos; + url_C + &apos; &apos; + width_C + &apos;w&quot; sizes=&quot;(max-width: &apos; + width_Z + &apos;px): 100vw, (min-width: &apos; + (width_Z * 0.15625 + width_Z) + &apos;px): &apos; + width_C + &apos;px, &apos; + width_Z + &apos;px&quot; /&gt;&lt;/a&gt;&apos;;</div> \n");
		html.append("<div id=\"example4\" style=\"visibility: hidden; margin: 0; padding: 0; height: 0;\">share = &apos;&lt;center&gt;&lt;div style=&quot;margin: 0px; padding: 0px; max-width: 640px;&quot&gt;&lt;a href=&quot;&apos; + pictureUrl + &apos;&quot; title=&quot;Expand &apos; + pictureTitle + &apos;&quot; rel=&quot;nofollow&quot;&gt;&lt;img title=&quot;&apos; + pictureTitle + &apos;&quot; alt=&quot;&apos; + pictureTitle + &apos;&quot; src=&quot;&apos; + url_Z + &apos;&quot; srcset=&quot;&apos; + url_S + &apos; &apos; + width_S + &apos;w, &apos; + url_N + &apos; &apos; + width_N + &apos;w, &apos; + url_M + &apos; &apos; + width_M + &apos;w, &apos; + url_Z + &apos; &apos; + width_Z + &apos;w&quot; sizes=&quot;(max-width: &apos; + width_Z + &apos;px): 100vw, &apos; + width_Z + &apos;px&quot; /&gt;&lt;/a&gt;&lt;/div&gt;&lt;/center&gt;&apos;;</div> \n");
		html.append("<button onclick=\"document.getElementById('custom_share').value = decodeEntities(document.getElementById('example1').innerHTML);\" type=\"button\">Load example1</button> \n"); 
		html.append("<button onclick=\"document.getElementById('custom_share').value = decodeEntities(document.getElementById('example2').innerHTML);\" type=\"button\">Load example2</button> \n"); 
		html.append("<button onclick=\"document.getElementById('custom_share').value = decodeEntities(document.getElementById('example3').innerHTML);\" type=\"button\">Load example3</button> \n");
		html.append("<button onclick=\"document.getElementById('custom_share').value = decodeEntities(document.getElementById('example4').innerHTML);\" type=\"button\">Load example4</button> \n");
		
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
			html.append("  <div style=\"width: " + expectedThumbnailSize.getWidth() + "px; background: #000000; color: #ffffff; margin-top: -4px; text-align: center;\">" + pictureId + "</div> \n");
			html.append("  <input type=\"hidden\" value=\"" + escapeHtml4(picture.getPictureUrl(pathAlias)) + "\" id=\"hidden_" + pictureId + "_url\"> \n");
			html.append("  <input type=\"hidden\" value=\"" + escapeHtml4(picture.getTitle()) + "\" id=\"hidden_" + pictureId + "_title\"> \n");
			
			for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
				html.append("  <input type=\"hidden\" value=\"" + escapeHtml4(picture.getThumbnailUrl(shareSizeEnum)) + "\" id=\"hidden_" + pictureId + "_" + shareSizeEnum.name() + "_url\"> \n");
				html.append("  <input type=\"hidden\" value=\"" + picture.getHeight(shareSizeEnum) + "\" id=\"hidden_" + pictureId + "_" + shareSizeEnum.name() + "_height\"> \n");
			}
			
			html.append("  <textarea onclick=\"document.getElementById('" + pictureId + "').blur(); document.getElementById('" + pictureId + "').select();\" style=\"width: " + expectedThumbnailSize.getWidth() + ";\" rows=\"2\" id=\"" + pictureId + "\">&nbsp;</textarea> \n");
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
