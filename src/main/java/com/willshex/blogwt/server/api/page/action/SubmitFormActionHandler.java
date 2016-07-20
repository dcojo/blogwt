//  
//  SubmitFormActionHandler.java
//  xsdwsdl2code
//
//  Created by William Shakour on July 20, 2016.
//  Copyright © 2016 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.server.api.page.action;

import java.util.logging.Logger;

import com.willshex.blogwt.server.api.validation.ApiValidator;
import com.willshex.blogwt.server.api.validation.FormValidator;
import com.willshex.blogwt.server.api.validation.SessionValidator;
import com.willshex.blogwt.server.helper.EmailHelper;
import com.willshex.blogwt.server.service.property.PropertyServiceProvider;
import com.willshex.blogwt.shared.api.datatype.Field;
import com.willshex.blogwt.shared.api.datatype.FieldTypeType;
import com.willshex.blogwt.shared.api.datatype.Property;
import com.willshex.blogwt.shared.api.page.call.SubmitFormRequest;
import com.willshex.blogwt.shared.api.page.call.SubmitFormResponse;
import com.willshex.blogwt.shared.api.validation.ApiError;
import com.willshex.blogwt.shared.helper.PropertyHelper;
import com.willshex.blogwt.shared.helper.UserHelper;
import com.willshex.gson.web.service.server.ActionHandler;
import com.willshex.gson.web.service.server.ServiceException;
import com.willshex.gson.web.service.shared.StatusType;

public final class SubmitFormActionHandler extends ActionHandler {
	private static final Logger LOG = Logger
			.getLogger(SubmitFormActionHandler.class.getName());

	public SubmitFormResponse handle (SubmitFormRequest input) {
		LOG.finer("Entering submitForm");
		SubmitFormResponse output = new SubmitFormResponse();
		try {
			// send an email with the submitted fields
			ApiValidator.notNull(input, SubmitFormRequest.class, "input");
			ApiValidator.accessCode(input.accessCode, "input.accessCode");

			if (input.session != null) {
				output.session = input.session = SessionValidator
						.lookupAndExtend(input.session, "input.session");
			}

			input.form = FormValidator.validate(input.form, "input.form");

			Property email = PropertyServiceProvider.provide()
					.getNamedProperty(PropertyHelper.OUTGOING_EMAIL);

			Property title = PropertyServiceProvider.provide()
					.getNamedProperty(PropertyHelper.TITLE);

			StringBuffer body = new StringBuffer();
			for (Field field : input.form.fields) {
				if (field.type != FieldTypeType.FieldTypeTypeCaptcha) {
					body.append(field.name);
					body.append(":");
					body.append(field.value);
					body.append("\n\n");
				}
			}

			if (!EmailHelper.sendEmail(email.value, title.value, "Submit form",
					body.toString(), false))
				ApiValidator.throwServiceError(ServiceException.class,
						ApiError.FailedToSendEmail, "input.form");

			UserHelper.stripPassword(output.session == null ? null
					: output.session.user);

			output.status = StatusType.StatusTypeSuccess;
		} catch (Exception e) {
			output.status = StatusType.StatusTypeFailure;
			output.error = convertToErrorAndLog(LOG, e);
		}
		LOG.finer("Exiting submitForm");
		return output;

	}
}