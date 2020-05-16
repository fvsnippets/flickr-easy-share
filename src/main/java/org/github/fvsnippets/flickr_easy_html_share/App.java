package org.github.fvsnippets.flickr_easy_html_share;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.System.out;
import static org.github.fvsnippets.flickr_easy_html_share.ConfigReader.loadConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

import org.github.fvsnippets.flickr_easy_html_share.model.Album;
import org.github.fvsnippets.flickr_easy_html_share.model.Book;
import org.github.fvsnippets.flickr_easy_html_share.model.Picture;
import org.github.fvsnippets.flickr_easy_html_share.model.config.Config;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photosets.Photoset;
import com.thoughtworks.xstream.XStream;


public class App {
	private static final String ALBUMS_INDEX_FILE = "index.html";
	private static final String DOWNLOAD_PICTURES_SH = "downloadPictures.sh";
	private static final String ALBUMS_HTML_DIR = "albums";
	
	private final Config config;
	private final FlickrReader flickrReader;
	
	public App(String configPath) throws FlickrException, IOException {
		out.println();
		out.println("flickr-easy-share");
		out.println("Get flickr photo links, in HTML <img> and <anchor> tags format, without accesing flickr web site, and offline disponible.");
		out.println("Copyright (C) 2016-2020 Federico Valido");
		out.println();
		out.println("This program is free software: you can redistribute it and/or modify");
		out.println("it under the terms of the GNU General Public License as published by");
		out.println("the Free Software Foundation, either version 3 of the License, or");
		out.println("(at your option) any later version.");
		out.println();
		out.println("This program is distributed in the hope that it will be useful,");
		out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		out.println("GNU General Public License for more details.");
		out.println();
		out.println("You should have received a copy of the GNU General Public License");
		out.println("along with this program.  If not, see <http://www.gnu.org/licenses/>.");
		out.println();
		out.println();
		
		config = loadConfig(configPath);
		flickrReader = new FlickrReader(config);
	}

	
	private void dehydrateBook(Book book, OutputStream destination) {
		XStream xstream = new XStream();
		xstream.toXML(book, destination);
	}
	
	private String getXmlBookFileName() {
		return Book.class.getSimpleName() + ".xml";
	}

	private void saveBook(File directory, Book book) throws FileNotFoundException {
		File xmlBook = new File(directory, getXmlBookFileName());
		dehydrateBook(book, new FileOutputStream(xmlBook));
	}
	
	private Book hydrateBook(InputStream origin) {
		XStream xstream = new XStream();

		return (Book)xstream.fromXML(origin);		
	}
	
	private Book loadBook(File directory) throws FileNotFoundException {
		File xmlBook = new File(directory, getXmlBookFileName());
		if (!xmlBook.exists()) {
			return new Book(config.getUsername());
		}
		
		return hydrateBook(new FileInputStream(xmlBook));
	}
	
	public Book updateBook() throws FlickrException, FileNotFoundException {
		File directory = config.getWorkingDirectory();
		Book book = loadBook(directory);

		flickrReader.update(config.getUsername(), book);
		
		saveBook(directory, book);
		
		return book;
	}
	private static void saveToFile(File file, String content) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(file);
		try {
			out.println(content);
		} finally {
			out.close();
		}
	}
	
	private static void ensureDirectory(File directory) {
		checkArgument(!directory.exists() || directory.isDirectory());
		if (!directory.exists()) {
			directory.mkdir();
		}
	}
	
	private Album generatePicturesThatDontBelongToAnyAlbumAlbum(Book book) {
		Photoset fakePhotoset = new Photoset();
		fakePhotoset.setId("pictures-that-dont-belong-to-any-album");
		fakePhotoset.setTitle("Pictures that don't belong to any album");
		fakePhotoset.setDateCreate(Integer.valueOf(24 * 60 * 60).toString());
		Album picturesThatDontBelongToAnyAlbum = book.createAlbum(fakePhotoset);
		
		HashSet<Picture> pictures = newHashSet(book.getPictures());
		for (Album album : book) {
			for (Picture picture : album) {
				pictures.remove(picture);
			}
		}
		for (Picture picture : pictures) {
			picturesThatDontBelongToAnyAlbum.addPicture(picture);
		}
		
		return picturesThatDontBelongToAnyAlbum;
	}
	
	public void generateView(Book book) throws FileNotFoundException {
		File workingDirectory = config.getWorkingDirectory();
		EasyShareView easyShare = new EasyShareView(workingDirectory, config.getThumbnailSize(), book);
		
		File downloadScript = new File(workingDirectory, DOWNLOAD_PICTURES_SH); 
		saveToFile(downloadScript, easyShare.bashDownloadScript());
		downloadScript.setExecutable(true);
		
		File htmlAlbumsDirectory = new File(workingDirectory, ALBUMS_HTML_DIR);
		ensureDirectory(htmlAlbumsDirectory);
		
		generatePicturesThatDontBelongToAnyAlbumAlbum(book);
		for (Album album : book) {
			File htmlAlbum = new File(htmlAlbumsDirectory, album.getId() + ".html");
			saveToFile(htmlAlbum, easyShare.albumHtml(album, config.getThumbnailSize(), book.getPathalias()));
		}
		
		File index = new File(workingDirectory, ALBUMS_INDEX_FILE);
		saveToFile(index, easyShare.albumsIndexHtml());		
	}
	
	private Config getConfig() {
		return config;
	}
	
	public static void main(String[] args) throws FlickrException, IOException {
		if (args.length != 1) {
			out.println("Usage: " + App.class.getName() + " \"/path/to/config/file.xml\"");
			return;
		}

		App app = new App(args[0]);
		
		out.println("Reading information from cache (maybe) and flickr. Please wait.");
		out.println();
		Book book = app.updateBook();
		
		out.println();
		out.println("Generating view.");
		app.generateView(book);
		
		out.println();
		out.println("Generated. Please execute " + app.getConfig().getWorkingDirectory() + DOWNLOAD_PICTURES_SH +
				" and open file://" + app.getConfig().getWorkingDirectory() + "/" + ALBUMS_INDEX_FILE + " in your browser.");
		out.println();
	}
}
