//
//  CookieNoticePart.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 11 Sep 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.client.part;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.willshex.blogwt.client.controller.PropertyController;
import com.willshex.blogwt.shared.helper.PropertyHelper;

/**
 * @author William Shakour (billy1380)
 *
 */
public class CookieNoticePart extends Composite {

	private static CookieNoticePartUiBinder uiBinder = GWT
			.create(CookieNoticePartUiBinder.class);

	interface CookieNoticePartUiBinder extends
			UiBinder<Widget, CookieNoticePart> {}

	interface Style extends CssResource {
		String fixed ();
	}

	private static final String EU_COOKIE_NOTICE_KEY = "eu.cookie.notice";

	@UiField Element elTitle;
	@UiField InlineHyperlink btnSeeDetails;

	@UiField Style style;

	private HandlerRegistration registration = null;
	private Timer toggleFix = new Timer() {

		@Override
		public void run () {
			if (shouldFix()) {
				CookieNoticePart.this.addStyleName(style.fixed());
			} else {
				CookieNoticePart.this.removeStyleName(style.fixed());
			}
		}
	};

	public CookieNoticePart () {
		initWidget(uiBinder.createAndBindUi(this));

		elTitle.setInnerText(PropertyController.get().title());

		String detailsPageSlug = PropertyController.get().stringProperty(
				PropertyHelper.COOKIE_DETAILS_PAGE_SLUG);

		if (detailsPageSlug == null
				|| PropertyHelper.NONE_VALUE.equals(detailsPageSlug)) {
			btnSeeDetails.setVisible(false);
		} else {
			btnSeeDetails.setTargetHistoryToken("!" + detailsPageSlug);
		}

		if (isCookie()) {
			setVisible(false);
		}
	}

	@UiHandler("btnGotIt")
	void btnGotItClicked (ClickEvent ce) {
		setCookie();
		setVisible(false);
		CookieNoticePart.this.removeStyleName(style.fixed());
		if (registration != null) {
			registration.removeHandler();
			registration = null;
		}
	}

	private boolean isCookie () {
		return Cookies.getCookie(EU_COOKIE_NOTICE_KEY) != null;
	}

	private void setCookie () {
		Cookies.setCookie(EU_COOKIE_NOTICE_KEY, "Got It!");
	}

	/* (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.Composite#onAttach() */
	@Override
	protected void onAttach () {
		super.onAttach();

		if (isVisible()) {
			registration = Window.addWindowScrollHandler(new ScrollHandler() {

				@Override
				public void onWindowScroll (ScrollEvent event) {
					toggleFix.cancel();
					toggleFix.schedule(150);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.Composite#onDetach() */
	@Override
	protected void onDetach () {
		super.onDetach();

		if (registration != null) {
			registration.removeHandler();
			registration = null;
		}

		toggleFix.cancel();
	}

	private boolean shouldFix () {
		return Window.getScrollTop() > this.getOffsetHeight() / 2;
	}

}