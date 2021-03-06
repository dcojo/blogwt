//
//  ApiHelper.java
//  blogwt
//
//  Created by billy1380 on 1 Aug 2013.
//  Copyright © 2013 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.client.helper;

import static com.google.gwt.user.client.Window.Location.getHost;

import com.google.gwt.core.shared.GWT;
import com.willshex.blogwt.client.DefaultEventBus;
import com.willshex.blogwt.client.api.blog.BlogService;
import com.willshex.blogwt.client.api.download.DownloadService;
import com.willshex.blogwt.client.api.notification.NotificationService;
import com.willshex.blogwt.client.api.page.PageService;
import com.willshex.blogwt.client.api.search.SearchService;
import com.willshex.blogwt.client.api.user.UserService;
import com.willshex.blogwt.shared.api.Request;
import com.willshex.blogwt.shared.api.blog.Blog;
import com.willshex.blogwt.shared.api.datatype.DataType;
import com.willshex.blogwt.shared.api.download.Download;
import com.willshex.blogwt.shared.api.notification.Notification;
import com.willshex.blogwt.shared.api.page.Page;
import com.willshex.blogwt.shared.api.search.Search;
import com.willshex.blogwt.shared.api.upload.Upload;
import com.willshex.blogwt.shared.api.user.User;
import com.willshex.blogwt.shared.api.validation.ApiError;
import com.willshex.gson.shared.Jsonable;
import com.willshex.gson.web.service.client.event.CallFailureEventHandler;
import com.willshex.gson.web.service.client.event.CallStartEventHandler;
import com.willshex.gson.web.service.client.event.CallSuccessEventHandler;
import com.willshex.gson.web.service.shared.Error;

/**
 * @author billy1380
 * 
 */
public class ApiHelper {

	public static final String ACCESS_CODE = "2bfe5f0e-9138-401c-8619-9a66f6367c9a";
	public static final String HOST = getHost();
	public static final String BASE_URL = "//" + HOST;
	public static final String BLOG_END_POINT = BASE_URL + Blog.PATH;
	public static final String USER_END_POINT = BASE_URL + User.PATH;
	public static final String PAGE_END_POINT = BASE_URL + Page.PATH;
	public static final String SEARCH_END_POINT = BASE_URL + Search.PATH;
	public static final String NOTIFICATION_END_POINT = BASE_URL
			+ Notification.PATH;
	public static final String UPLOAD_END_POINT = BASE_URL + Upload.PATH;
	public static final String DOWNLOAD_END_POINT = BASE_URL + Download.PATH;

	static {
		DefaultEventBus.get().addHandler(CallStartEventHandler.TYPE,
				(origin, name, i, handle) -> {
					GWT.log("Calling " + origin.getUrl() + "." + name
							+ " with input [" + pretty(i) + "].");
				});
		DefaultEventBus.get().addHandler(CallSuccessEventHandler.TYPE,
				(origin, name, i, o) -> {
					GWT.log("Call to " + origin.getUrl() + "." + name
							+ " with input [" + pretty(i)
							+ "] succeeded with output [" + pretty(o) + "].");
				});
		DefaultEventBus.get().addHandler(CallFailureEventHandler.TYPE,
				(origin, name, i, caught) -> GWT.log(
						"Call to " + origin.getUrl() + "." + name
								+ " with input [" + pretty(i) + "] failed.",
						caught));
	}

	public static BlogService createBlogClient () {
		BlogService service = new BlogService();
		service.setUrl(BLOG_END_POINT);
		service.setBus(DefaultEventBus.get());
		return service;
	}

	private static String pretty (Jsonable i) {
		return i == null ? null :
		//			JsonUtils.beautifyJson(
				i.toString()
		//		)
		;
	}

	public static UserService createUserClient () {
		UserService service = new UserService();
		service.setUrl(USER_END_POINT);
		service.setBus(DefaultEventBus.get());
		return service;
	}

	public static PageService createPageClient () {
		PageService service = new PageService();
		service.setUrl(PAGE_END_POINT);
		service.setBus(DefaultEventBus.get());
		return service;
	}

	public static SearchService createSearchClient () {
		SearchService service = new SearchService();
		service.setUrl(SEARCH_END_POINT);
		service.setBus(DefaultEventBus.get());
		return service;
	}

	public static NotificationService createNotificationClient () {
		NotificationService service = new NotificationService();
		service.setUrl(NOTIFICATION_END_POINT);
		service.setBus(DefaultEventBus.get());
		return service;
	}

	public static DownloadService createDownloadClient () {
		DownloadService service = new DownloadService();
		service.setUrl(DOWNLOAD_END_POINT);
		service.setBus(DefaultEventBus.get());
		return service;
	}

	public static <T extends Request> T setAccessCode (T input) {
		input.accessCode = ACCESS_CODE;
		return input;
	}

	/**
	 * @param error
	 * @param apiError
	 * @return
	 */
	public static boolean isError (Error error, ApiError apiError) {
		return error != null && error.code != null
				&& error.code.intValue() == apiError.getCode();
	}

	public static <T extends DataType> T dataType (T instance, Long id) {
		instance.id(id);
		return instance;
	}

	public static <T extends DataType> T idOnly (T forCall, T full) {
		forCall.id = full.id;
		return forCall;
	}
}
