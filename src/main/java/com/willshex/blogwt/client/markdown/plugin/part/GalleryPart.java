//
//  GalleryPart.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 15 Jan 2016.
//  Copyright © 2016 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.client.markdown.plugin.part;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author William Shakour (billy1380)
 *
 */
public class GalleryPart extends Composite {

	private static GalleryPartUiBinder uiBinder = GWT
			.create(GalleryPartUiBinder.class);

	interface GalleryPartUiBinder extends UiBinder<Widget, GalleryPart> {}

	private static class ConfigLine {
		public String url;
		public Map<String, String> parameters;
	}

	private static final String URL = "url";

	public GalleryPart () {
		initWidget(uiBinder.createAndBindUi(this));
	}

	/**
	 * @param params
	 */
	public void setParams (Map<String, String> params) {

	}

	/**
	 * @param line
	 */
	public void addImageWithLine (String line) {
		if (line.length() > 0) {
			ConfigLine config = parseConfigLine(line);

			if (config.url != null && config.url.length() > 0) {
				Image image = new Image(config.url);

				((HTMLPanel) this.getWidget()).add(image);
			}
		}
	}

	private ConfigLine parseConfigLine (String line) {
		String[] params = line.split("/");
		ConfigLine config = new ConfigLine();

		String[] splitParam;
		Map<String, String> parameters = new HashMap<String, String>();
		for (String param : params) {
			splitParam = param.split("=");

			if (splitParam.length == 2) {
				parameters.put(splitParam[0], splitParam[1]);

				switch (splitParam[0]) {
				case URL:
					config.url = splitParam[1];
					break;
				}
			}
		}

		config.parameters = parameters;

		return config;
	}
}
