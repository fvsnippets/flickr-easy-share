package org.github.fvsnippets.flickr_easy_html_share;

import static com.flickr4java.flickr.auth.Permission.READ;
import static java.lang.System.in;
import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.github.fvsnippets.flickr_easy_html_share.model.Book;
import org.github.fvsnippets.flickr_easy_html_share.model.config.Config;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth1Token;

public class FlickrReader {
	private static final String AUTH_STORE_FILENAME = "AuthStore.flickr";
	private final Flickr flickr;
	private final AuthStore authStore;
	private final UserPicturesReader userPicturesReader;
	private final UserAlbumsReader userAlbumsReader;
	
	FlickrReader(Config config) throws FlickrException, IOException {
		flickr = new Flickr(config.getApiKey(), config.getSharedSecret(), new REST());
		authStore = new FileAuthStore(new File(config.getWorkingDirectory(), AUTH_STORE_FILENAME));
		auth(config.getUsername());
		userPicturesReader = new UserPicturesReader(flickr);
		userAlbumsReader = new UserAlbumsReader(flickr);
	}
	
    private Auth authorize() throws FlickrException {
    	Scanner scanner = new Scanner(in);

    	AuthInterface authInterface = flickr.getAuthInterface();
        OAuth1RequestToken accessToken = authInterface.getRequestToken();

        String url = authInterface.getAuthorizationUrl(accessToken, READ);
        out.println("Follow this URL to authorise yourself on Flickr");
        out.println(url);
        out.print("Paste in the token it gives you: ");

        try {
	        String tokenKey = scanner.nextLine();
	        out.println();
	        OAuth1Token requestToken = authInterface.getAccessToken(accessToken, tokenKey);
	        return authInterface.checkToken(requestToken);
        } finally {
        	scanner.close();
        }
    }
	
	void auth(String username) throws FlickrException, IOException {
		PeopleInterface peopleInterface = flickr.getPeopleInterface();

		out.println("Authorizing to read information of user: " + username);
		User user = peopleInterface.getInfo(peopleInterface.findByUsername(username).getId());
		
        Auth auth = this.authStore.retrieve(user.getId());
        if (auth == null) {
            auth = authorize();
	        authStore.store(auth);
        }
        
       	RequestContext.getRequestContext().setAuth(auth);
	}
	
	void update(String username, Book book) throws FlickrException {
		out.println("Getting userinfo of user: " + username);
		User user = flickr.getPeopleInterface().findByUsername(username);
		
		userPicturesReader.readOrUpdatePictures(user, book);
		
		out.println();
		userAlbumsReader.readAlbums(user, book);
		// XXX: Error if no pictures (but thats a pretty stupid use of this program)
		book.setPathAlias(userPicturesReader.pathAlias(username));
	}
}
