package org.github.fvsnippets.flickr_easy_html_share;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.System.in;
import static java.lang.System.out;
import static org.github.fvsnippets.flickr_easy_html_share.LinkTargetEnum.BLANK;
import static org.github.fvsnippets.flickr_easy_html_share.model.ShareSizeEnum.N;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import org.github.fvsnippets.flickr_easy_html_share.model.ShareSizeEnum;
import org.github.fvsnippets.flickr_easy_html_share.model.config.Config;

import com.thoughtworks.xstream.XStream;

public class ConfigReader {
	private static Config readConfigFromUser(String defaultWorkingDirectory, ShareSizeEnum defaultThumbnailSize, LinkTargetEnum defaultLinkTarget, String defaultLinkTitlePrefix) {
		Scanner scanner = new Scanner(in);
		
		try {
			String username = "";
			while (username.trim().isEmpty()) {
				out.print("Username: ");
				username = scanner.nextLine();
			}
			
			out.println("As you may have read, this app is not finished. You will need to provide an API and a Shared Secret from "
					+ "https://www.flickr.com/services/apps/create/ . Just create (if you don't have one) a random app and provide "
					+ "here Api Key and Shared Secret when asked. These codes will be sent only to flickr through Flickr4Java (but "
					+ "you can see the code if you don't trust me)." );
			
			String apiKey = "";
			while (apiKey.trim().isEmpty()) {
				out.print("Api Key: ");
				apiKey = scanner.nextLine();
			}
	
			String sharedSecret = "";
			while (sharedSecret.trim().isEmpty()) {
				out.print("Shared Secret: ");
				sharedSecret = scanner.nextLine();
			}
			
			out.print("Working directory [" + defaultWorkingDirectory + "]: ");
			String workingDirectoryPath = scanner.nextLine();
			if (workingDirectoryPath.trim().isEmpty()) {
				workingDirectoryPath = defaultWorkingDirectory;
			}
			
			out.println("Choose thumbnail size:");
			for (ShareSizeEnum shareSizeEnum : ShareSizeEnum.values()) {
				out.println(shareSizeEnum.name() + ") " + shareSizeEnum.getDescription());
			}
			ShareSizeEnum thumbnailSize = null;
			while (thumbnailSize == null) {
				out.print("pick an option [" + defaultThumbnailSize.name() + "]: ");
				String line = scanner.nextLine().trim().toUpperCase();
				if (line.isEmpty()) {
					thumbnailSize = defaultThumbnailSize;
				} else {
					try {
						thumbnailSize = ShareSizeEnum.valueOf(line);
					} catch (IllegalArgumentException e) {
						// expected
					}
				}
			}
			
			out.println("Choose link target:");
			for (LinkTargetEnum linkTargetEnum : LinkTargetEnum.values()) {
				out.println(linkTargetEnum.getLabel() + ") " + linkTargetEnum.getHtmlReferenceRepresentation());
			}
			LinkTargetEnum linkTarget = null;
			while (linkTarget == null) {
				out.print("pick an option [" + defaultLinkTarget.getLabel() + "]: ");
				String line = scanner.nextLine().trim().toUpperCase();
				if (line.isEmpty()) {
					linkTarget = defaultLinkTarget;
				} else {
					try {
						linkTarget = LinkTargetEnum.getByLabel(line);
					} catch (IllegalArgumentException e) {
						// expected
					}
				}
			}
			String linkTargetValue = linkTarget.asHtmlRepresentation(scanner);
			
			out.print("Link title prefix, insert a single space for empty or blank for default [\"" + defaultLinkTitlePrefix + "\"]: ");
			String linkTitlePrefix = scanner.nextLine();
			if (!linkTitlePrefix.isEmpty()) {
				if (linkTitlePrefix.equals(" ")) {
					linkTitlePrefix = defaultLinkTitlePrefix;
				} else {
					if (!linkTitlePrefix.endsWith(" ")) {
						linkTitlePrefix += " ";
					}
				}
			}

			return new Config(username, apiKey, sharedSecret, workingDirectoryPath, thumbnailSize, linkTargetValue, linkTitlePrefix);
		} finally {
			scanner.close();
		}
	}
	
	public static Config loadConfig(String configFilePath) throws FileNotFoundException {
		File configFile = new File(configFilePath);
		Config config;
		XStream xstream = new XStream();
		if (configFile.exists()) {
			checkArgument(configFile.isFile());

			config = (Config)xstream.fromXML(configFile);
			
			out.println("Found existing config:");
			out.println("- User: " + config.getUsername());
			out.println("- ApiKey: [hidden, but you can see it in xml]");
			out.println("- SharedSecret: [hidden, but you can see it in xml]");
			out.println("- WorkingDirectory: " + config.getWorkingDirectory().getAbsolutePath());
			out.println("- ThumbnailSize: " + config.getThumbnailSize() + " (" + config.getThumbnailSize().getDescription() + ")");
			out.println("- LinkTarget: " + config.getLinkTarget());
			out.println("- LinkTitlePrefix: " + config.getLinkTitlePrefix());
			out.println();
		} else {
			config = readConfigFromUser(configFile.getParent(), N, BLANK, "Enlarge ");

			xstream.toXML(config, new FileOutputStream(configFile));
		}

		return config;
	}
}
