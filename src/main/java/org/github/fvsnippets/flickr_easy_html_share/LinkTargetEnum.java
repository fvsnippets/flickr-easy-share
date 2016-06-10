package org.github.fvsnippets.flickr_easy_html_share;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.out;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public enum LinkTargetEnum {
	BLANK("B", "_blank"),
	SELF("S", "_self"),
	PARENT("P", "_parent"),
	TOP("T", "_top"),
	FRAMENAME("F", "\"framename\"") {
		@Override
		public String asHtmlRepresentation(Scanner scanner) {
			String frameName = "";
			while (frameName.trim().isEmpty()) {
				out.println("Enter framename: ");
				frameName = scanner.nextLine();
			}
			
			return frameName;
		}
	};
	
	private final String label;
	private final String htmlReferenceRepresentation;
	
	private LinkTargetEnum(String label, String htmlReferenceRepresentation) {
		this.label = label;
		this.htmlReferenceRepresentation = htmlReferenceRepresentation;
	}
	
	public String getHtmlReferenceRepresentation() {
		return htmlReferenceRepresentation;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String asHtmlRepresentation(Scanner scanner) {
		return htmlReferenceRepresentation;
	}
	
	private static final Map<String, LinkTargetEnum> LABEL_TO_LINK_TARGET = new HashMap<String, LinkTargetEnum>();
	static {
		for (LinkTargetEnum linkTargetEnum : values()) {
			LABEL_TO_LINK_TARGET.put(linkTargetEnum.label, linkTargetEnum);	
		}
	}
	
	public static LinkTargetEnum getByLabel(String label) {
		return checkNotNull(LABEL_TO_LINK_TARGET.get(label));
	}
	
}
