//
//  StaticPosts.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 18 Sep 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.server.page;

import org.markdown4j.server.IncludePlugin;
import org.markdown4j.server.MarkdownProcessor;

import com.willshex.blogwt.server.api.blog.BlogApi;
import com.willshex.blogwt.server.helper.UserHelper;
import com.willshex.blogwt.shared.api.blog.call.GetPostsRequest;
import com.willshex.blogwt.shared.api.blog.call.GetPostsResponse;
import com.willshex.blogwt.shared.api.datatype.Post;
import com.willshex.blogwt.shared.helper.DateTimeHelper;
import com.willshex.blogwt.shared.helper.PagerHelper;
import com.willshex.blogwt.shared.helper.PostHelper;
import com.willshex.blogwt.shared.page.PageType;
import com.willshex.blogwt.shared.page.Stack;
import com.willshex.gson.json.service.shared.StatusType;

/**
 * @author William Shakour (billy1380)
 *
 */
class StaticPosts extends StaticTemplate {

	public StaticPosts (Stack stack) {
		super(stack);
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * com.willshex.blogwt.server.page.StaticTemplate#appendPage(java.lang.
	 * StringBuffer) */
	@Override
	protected void appendPage (StringBuffer markup) {
		BlogApi api = new BlogApi();

		GetPostsRequest input = input(GetPostsRequest.class).pager(
				PagerHelper.createDefaultPager());

		GetPostsResponse output = api.getPosts(input);

		if (output.status == StatusType.StatusTypeSuccess
				&& output.posts != null) {
			MarkdownProcessor processor = new MarkdownProcessor();
			processor.registerPlugins(new IncludePlugin());

			String link, body;
			for (Post post : output.posts) {
				body = "Empty... :imp:";

				if (post.summary != null && post.summary.length() > 0) {
					body = post.summary;
				}

				link = "#"
						+ PageType.PostDetailPageType
								.asTargetHistoryToken(PostHelper.getSlug(post));

				markup.append("<div><a href=\"");
				markup.append(link);
				markup.append("\">");
				markup.append(process("##" + post.title));
				markup.append("</a><div><span>");
				markup.append(DateTimeHelper.ago(post.published));
				markup.append("</span> by <img src=\"");
				markup.append(post.author.avatar);
				markup.append("?s=");
				markup.append(UserHelper.AVATAR_HEADER_SIZE);
				markup.append("&default=retro");
				markup.append("\" class=\"img-circle\" /> ");
				markup.append(UserHelper.handle(post.author));
				markup.append("</div><div>");
				markup.append(process(body));
				markup.append("</div><a href=\"");
				markup.append(link);
				markup.append("\">Read More</a></div>");
			}
		} else {
			markup.append(output.error.toString());
		}
	}

}