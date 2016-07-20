//  
//  GetPostsActionHandler.java
//  xsdwsdl2code
//
//  Created by William Shakour on July 20, 2016.
//  Copyright © 2016 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.server.api.blog.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;
import com.willshex.blogwt.server.api.exception.AuthorisationException;
import com.willshex.blogwt.server.api.validation.ApiValidator;
import com.willshex.blogwt.server.api.validation.ArchiveEntryValidator;
import com.willshex.blogwt.server.api.validation.PostValidator;
import com.willshex.blogwt.server.api.validation.SessionValidator;
import com.willshex.blogwt.server.api.validation.UserValidator;
import com.willshex.blogwt.server.service.PersistenceService;
import com.willshex.blogwt.server.service.permission.PermissionServiceProvider;
import com.willshex.blogwt.server.service.post.PostServiceProvider;
import com.willshex.blogwt.server.service.tag.TagServiceProvider;
import com.willshex.blogwt.server.service.user.UserServiceProvider;
import com.willshex.blogwt.shared.api.SortDirectionType;
import com.willshex.blogwt.shared.api.blog.call.GetPostsRequest;
import com.willshex.blogwt.shared.api.blog.call.GetPostsResponse;
import com.willshex.blogwt.shared.api.datatype.Permission;
import com.willshex.blogwt.shared.api.datatype.Post;
import com.willshex.blogwt.shared.api.datatype.PostSortType;
import com.willshex.blogwt.shared.api.datatype.Tag;
import com.willshex.blogwt.shared.api.datatype.User;
import com.willshex.blogwt.shared.helper.PagerHelper;
import com.willshex.blogwt.shared.helper.PermissionHelper;
import com.willshex.blogwt.shared.helper.UserHelper;
import com.willshex.gson.web.service.server.ActionHandler;
import com.willshex.gson.web.service.server.InputValidationException;
import com.willshex.gson.web.service.shared.StatusType;

public final class GetPostsActionHandler extends ActionHandler {
	private static final Logger LOG = Logger
			.getLogger(GetPostsActionHandler.class.getName());

	public GetPostsResponse handle (GetPostsRequest input) {
		LOG.finer("Entering getPosts");
		GetPostsResponse output = new GetPostsResponse();
		try {
			ApiValidator.notNull(input, GetPostsRequest.class, "input");
			ApiValidator.accessCode(input.accessCode, "input.accessCode");

			Boolean showAll = Boolean.TRUE.equals(input.showAll) ? Boolean.TRUE
					: Boolean.FALSE;
			if (input.session != null) {
				try {
					output.session = input.session = SessionValidator
							.lookupAndExtend(input.session, "input.session");

					List<Permission> permissions = new ArrayList<Permission>();
					Permission postPermission = PermissionServiceProvider
							.provide()
							.getCodePermission(PermissionHelper.MANAGE_POSTS);
					permissions.add(postPermission);

					input.session.user = UserServiceProvider.provide().getUser(
							Long.valueOf(input.session.userKey.getId()));

					try {
						UserValidator.authorisation(input.session.user,
								permissions, "input.session.user");
					} catch (AuthorisationException aEx) {
						showAll = Boolean.FALSE;
					}
				} catch (InputValidationException ex) {
					output.session = input.session = null;
					showAll = Boolean.FALSE;
				}
			} else {
				showAll = Boolean.FALSE;
			}

			if (!showAll) {
				input.pager.sortBy = PostSortType.PostSortTypePublished
						.toString();
			}

			if (input.includePostContents == null) {
				input.includePostContents = Boolean.FALSE;
			}

			boolean postsForTag = false, postsForArchiveEntry = false,
					postsForQuery = false;
			if (input.tag != null && input.tag.length() > 0) {
				postsForTag = true;
				Tag tag = TagServiceProvider.provide().getSlugTag(input.tag);

				if (tag != null) {
					output.posts = PostServiceProvider.provide().getIdPostBatch(
							PersistenceService.keysToIds(tag.postKeys));
				}
			}

			if (!postsForTag && input.archiveEntry != null) {
				postsForTag = true;
				if (input.archiveEntry.posts != null) {
					output.posts = input.archiveEntry.posts = PostValidator
							.lookupAll(input.archiveEntry.posts,
									"input.archiveEntry.posts");
				} else {
					input.archiveEntry = ArchiveEntryValidator
							.lookup(input.archiveEntry, "input.archiveEntry");

					output.posts = PostServiceProvider.provide()
							.getIdPostBatch(PersistenceService
									.keysToIds(input.archiveEntry.postKeys));
				}
			}

			if (!postsForTag && !postsForArchiveEntry && input.query != null) {
				postsForQuery = true;
				if (input.session != null && input.session.user != null) {
					output.posts = PostServiceProvider.provide()
							.getUserViewablePartialSlugPosts(input.query,
									input.session.user, showAll,
									input.includePostContents,
									input.pager.start, input.pager.count,
									PostSortType.fromString(input.pager.sortBy),
									input.pager.sortDirection);
				} else {
					output.posts = PostServiceProvider.provide()
							.getPartialSlugPosts(input.query, showAll,
									input.includePostContents,
									input.pager.start, input.pager.count,
									PostSortType.PostSortTypePublished,
									SortDirectionType.SortDirectionTypeDescending);
				}
			}

			if (!postsForTag && !postsForArchiveEntry && !postsForQuery) {
				output.posts = PostServiceProvider.provide().getPosts(showAll,
						input.includePostContents, input.pager.start,
						input.pager.count,
						PostSortType.fromString(input.pager.sortBy),
						input.pager.sortDirection);
			}

			if (output.posts != null) {
				Map<Key<User>, User> users = new HashMap<Key<User>, User>();

				for (Post post : output.posts) {
					if (users.get(post.authorKey) == null) {
						users.put(post.authorKey, UserHelper.stripSensitive(
								UserServiceProvider.provide().getUser(
										Long.valueOf(post.authorKey.getId()))));
					}

					post.author = users.get(post.authorKey);
				}
			}

			output.pager = PagerHelper.moveForward(input.pager);

			UserHelper.stripPassword(
					output.session == null ? null : output.session.user);
			output.status = StatusType.StatusTypeSuccess;
		} catch (Exception e) {
			output.status = StatusType.StatusTypeFailure;
			output.error = convertToErrorAndLog(LOG, e);
		}
		LOG.finer("Exiting getPosts");
		return output;
	}
}