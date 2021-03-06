//
//  PostDetailPage.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 11 May 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.client.page.blog;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.willshex.blogwt.client.DefaultEventBus;
import com.willshex.blogwt.client.api.blog.event.DeletePostEventHandler;
import com.willshex.blogwt.client.api.blog.event.GetPostEventHandler;
import com.willshex.blogwt.client.cell.blog.PostSummaryCell;
import com.willshex.blogwt.client.cell.blog.TagCell;
import com.willshex.blogwt.client.controller.NavigationController;
import com.willshex.blogwt.client.controller.PostController;
import com.willshex.blogwt.client.controller.PropertyController;
import com.willshex.blogwt.client.controller.SessionController;
import com.willshex.blogwt.client.event.NavigationChangedEventHandler;
import com.willshex.blogwt.client.helper.PageTypeHelper;
import com.willshex.blogwt.client.helper.PostHelper;
import com.willshex.blogwt.client.helper.UiHelper;
import com.willshex.blogwt.client.page.Page;
import com.willshex.blogwt.client.part.AddToAny;
import com.willshex.blogwt.client.part.DisqusComments;
import com.willshex.blogwt.client.part.FooterPart;
import com.willshex.blogwt.client.part.InlineBootstrapGwtCellList;
import com.willshex.blogwt.client.part.LoadingPanel;
import com.willshex.blogwt.client.part.PostNavPart;
import com.willshex.blogwt.client.part.RelatedPart;
import com.willshex.blogwt.client.part.UserSummaryPart;
import com.willshex.blogwt.shared.api.blog.call.DeletePostRequest;
import com.willshex.blogwt.shared.api.blog.call.DeletePostResponse;
import com.willshex.blogwt.shared.api.blog.call.GetPostRequest;
import com.willshex.blogwt.shared.api.blog.call.GetPostResponse;
import com.willshex.blogwt.shared.api.datatype.Post;
import com.willshex.blogwt.shared.api.datatype.Tag;
import com.willshex.blogwt.shared.helper.DateTimeHelper;
import com.willshex.blogwt.shared.helper.PermissionHelper;
import com.willshex.blogwt.shared.helper.PropertyHelper;
import com.willshex.blogwt.shared.helper.UserHelper;
import com.willshex.blogwt.shared.page.PageType;
import com.willshex.gson.web.service.shared.StatusType;

/**
 * @author William Shakour (billy1380)
 *
 */
public class PostDetailPage extends Page
		implements GetPostEventHandler, DeletePostEventHandler {

	private static PostDetailPageUiBinder uiBinder = GWT
			.create(PostDetailPageUiBinder.class);

	interface PostDetailPageUiBinder extends UiBinder<Widget, PostDetailPage> {}

	@UiField Element elTitle;
	@UiField Element elDate;
	@UiField Element elAuthor;
	@UiField LoadingPanel pnlLoading;

	@UiField HTMLPanel pnlContent;
	@UiField InlineHyperlink lnkEditPost;
	@UiField Button btnDeletePost;
	@UiField RelatedPart coRelated;

	@UiField(provided = true) DisqusComments dsqComments = new DisqusComments(
			PostController.categoryId(), PostController.disqusId());

	@UiField AddToAny ataShare;

	@UiField(provided = true) CellList<Tag> clTags = new CellList<Tag>(
			new TagCell(true, false), InlineBootstrapGwtCellList.INSTANCE);

	@UiField UserSummaryPart pnlUserSummary;
	@UiField HTMLPanel pnlUserSummaryRow;
	@UiField PostNavPart pnlPostNav;

	private ListDataProvider<Tag> tagList = new ListDataProvider<Tag>();
	private Post post;

	public PostDetailPage () {
		initWidget(uiBinder.createAndBindUi(this));

		tagList.addDataDisplay(clTags);

		if (!PropertyController.get()
				.booleanProperty(PropertyHelper.POST_SHOW_AUTHOR, false)) {
			elAuthor.removeFromParent();
		}
	}

	@UiHandler("btnDeletePost")
	void onBtnDeletePost (ClickEvent event) {
		if (post != null && Window.confirm(
				"Are you sure you want to delete \"" + post.title + "\"")) {
			PostController.get().deletePost(post);
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see com.willshex.blogwt.client.page.Page#onAttach() */
	@Override
	protected void onAttach () {
		register(DefaultEventBus.get().addHandlerToSource(
				NavigationChangedEventHandler.TYPE, NavigationController.get(),
				(p, c) -> {
					String slug;
					if ((slug = c.getAction()) != null) {
						PostController.get().getPost(slug);
						pnlLoading.setVisible(true);
						dsqComments.setVisible(false);
						ataShare.setVisible(false);
						coRelated.setVisible(false);
					}

					boolean canChange = SessionController.get()
							.isAuthorised(Arrays.asList(PermissionHelper
									.create(PermissionHelper.MANAGE_POSTS)));
					lnkEditPost.setVisible(canChange);
					btnDeletePost.setVisible(canChange);
				}));
		register(DefaultEventBus.get().addHandlerToSource(
				GetPostEventHandler.TYPE, PostController.get(), this));
		register(DefaultEventBus.get().addHandlerToSource(
				DeletePostEventHandler.TYPE, PostController.get(), this));

		register(PostHelper.handlePluginContentReady());

		super.onAttach();
	}

	/* (non-Javadoc)
	 * 
	 * @see com.willshex.blogwt.shared.api.blog.call.event.GetPostEventHandler#
	 * getPostSuccess(com.willshex.blogwt.shared.api.blog.call.GetPostRequest,
	 * com.willshex.blogwt.shared.api.blog.call.GetPostResponse) */
	@Override
	public void getPostSuccess (GetPostRequest input, GetPostResponse output) {
		if (output.status == StatusType.StatusTypeSuccess) {
			show(post = output.post);
		} else {
			NavigationController.get().lost();
		}
	}

	private void show (Post post) {
		elTitle.setInnerHTML(PostHelper.makeHeading(post.title));

		SafeHtml author = SafeHtmlUtils.EMPTY_SAFE_HTML;
		if (PropertyController.get()
				.booleanProperty(PropertyHelper.POST_SHOW_AUTHOR, false)) {
			author = PostSummaryCell.Templates.INSTANCE.author(
					UriUtils.fromString(post.author.avatar + "?s="
							+ UserHelper.AVATAR_HEADER_SIZE + "&default=retro"),
					UserHelper.handle(post.author));
		}

		if (PropertyController.get().booleanProperty(
				PropertyHelper.POST_SHOW_AUTHOR_SUMMARY, false)) {
			pnlUserSummary.setUser(post.author);
			pnlUserSummaryRow.setVisible(true);
		}

		elAuthor.setInnerSafeHtml(author);

		if (post.published != null) {
			elDate.setInnerSafeHtml(PostSummaryCell.Templates.INSTANCE
					.publishedDate(DateTimeHelper.ago(post.published)));
		} else {
			elDate.setInnerSafeHtml(PostSummaryCell.Templates.INSTANCE
					.notPublished(DateTimeHelper.ago(post.created)));
		}

		lnkEditPost.setTargetHistoryToken(
				PageType.EditPostPageType.asTargetHistoryToken(post.slug));

		tagList.getList().clear();
		coRelated.setVisible(Boolean.TRUE.equals(post.listed));

		if (post.tags != null) {
			for (String tag : post.tags) {
				tagList.getList().add(new Tag().name(tag));
			}
		}

		final String url = GWT.getHostPageBaseURL() + PageTypeHelper
				.asHref(PageType.PostDetailPageType, post.slug).asString();
		final String title = post.title;

		ataShare.setUrl(url);
		ataShare.setTitle(title);

		ataShare.setVisible(true);

		if (post.content != null) {
			String markup = PostHelper.makeMarkup(post.content.body);

			pnlContent.getElement().setInnerHTML(markup);

			String comments = PropertyController.get()
					.stringProperty(PropertyHelper.POST_COMMENTS_ENABLED);

			if (comments == null
					|| comments.equals(PropertyHelper.NONE_VALUE)) {
				dsqComments.removeFromParent();
			} else if (Boolean.TRUE.equals(post.commentsEnabled)) {
				final String identifier = "post" + post.id.toString();
				final String tag = post.tags == null || post.tags.size() == 0
						? "none"
						: post.tags.get(0);

				dsqComments.setUrl(url);
				dsqComments.setTitle(title);
				dsqComments.setIdentifier(identifier);
				dsqComments.setTag(tag);

				dsqComments.setVisible(true);
			} else {
				dsqComments.setVisible(false);
			}
		}

		pnlPostNav.setSlugs(post.previousSlug, post.nextSlug);

		pnlLoading.setVisible(false);

		FooterPart.get().scrollToTop();

		refreshTitle();
	}

	/* (non-Javadoc)
	 * 
	 * @see com.willshex.blogwt.shared.api.blog.call.event.GetPostEventHandler#
	 * getPostFailure(com.willshex.blogwt.shared.api.blog.call.GetPostRequest,
	 * java.lang.Throwable) */
	@Override
	public void getPostFailure (GetPostRequest input, Throwable caught) {}

	@Override
	public void deletePostSuccess (DeletePostRequest input,
			DeletePostResponse output) {
		if (output.status == StatusType.StatusTypeSuccess) {
			PageTypeHelper.show(PageType.PostsPageType);
		} else {

		}
	}

	@Override
	public void deletePostFailure (DeletePostRequest input, Throwable caught) {}

	/* (non-Javadoc)
	 * 
	 * @see com.willshex.blogwt.client.page.Page#reset() */
	@Override
	protected void reset () {
		super.reset();

		elTitle.setInnerHTML("");
		elAuthor.setInnerHTML("");
		elDate.setInnerHTML("");

		tagList.getList().clear();

		lnkEditPost.setTargetHistoryToken(
				PageType.EditPostPageType.asTargetHistoryToken(""));

		pnlContent.getElement().setInnerHTML("");
		pnlLoading.setVisible(true);

		dsqComments.setVisible(false);
		ataShare.setVisible(false);
		pnlPostNav.setVisible(false);

		pnlUserSummaryRow.setVisible(false);
	}

	/* (non-Javadoc)
	 * 
	 * @see com.willshex.blogwt.client.page.Page#getTitle() */
	@Override
	public String getTitle () {
		return post == null ? super.getTitle() : UiHelper.pageTitle(post.title);
	}
}
