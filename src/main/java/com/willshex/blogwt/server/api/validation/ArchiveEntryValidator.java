//
//  ArchiveEntryValidator.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 26 Aug 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.server.api.validation;

import com.willshex.blogwt.server.service.archiveentry.ArchiveEntryServiceProvider;
import com.willshex.blogwt.shared.api.datatype.ArchiveEntry;
import com.willshex.blogwt.shared.api.validation.ApiError;
import com.willshex.gson.web.service.server.InputValidationException;
import com.willshex.gson.web.service.server.ServiceException;

/**
 * @author William Shakour (billy1380)
 *
 */
public class ArchiveEntryValidator extends ApiValidator {

	private static final String TYPE = ArchiveEntry.class.getSimpleName();

	public static ArchiveEntry validate (ArchiveEntry archiveEntry, String name)
			throws InputValidationException {
		return archiveEntry;
	}

	public static <T extends Iterable<ArchiveEntry>> T validateAll (
			T archiveEntries, final String name) throws ServiceException {
		return processAll(false, archiveEntries,
				ArchiveEntryValidator::validate, TYPE, name);
	}

	public static ArchiveEntry lookup (ArchiveEntry archiveEntry, String name)
			throws InputValidationException {
		if (archiveEntry == null)
			throwServiceError(InputValidationException.class,
					ApiError.InvalidValueNull, TYPE + ": " + name);

		boolean isIdLookup = false, isYearMonthLookup = false;

		if (archiveEntry.id != null) {
			isIdLookup = true;
		} else if (archiveEntry.year != null && archiveEntry.month != null) {
			isYearMonthLookup = true;
		}

		if (!(isIdLookup || isYearMonthLookup))
			throwServiceError(InputValidationException.class,
					ApiError.DataTypeNoLookup, TYPE + ": " + name);

		ArchiveEntry lookupArchiveEntry = null;
		if (isIdLookup) {
			lookupArchiveEntry = ArchiveEntryServiceProvider.provide()
					.getArchiveEntry(archiveEntry.id);
		} else if (isYearMonthLookup) {
			lookupArchiveEntry = ArchiveEntryServiceProvider.provide()
					.getMonthArchiveEntry(archiveEntry.month,
							archiveEntry.year);
		}

		if (lookupArchiveEntry == null)
			throwServiceError(InputValidationException.class,
					ApiError.DataTypeNotFound, TYPE + ": " + name);

		return lookupArchiveEntry;
	}

	public static <T extends Iterable<ArchiveEntry>> T lookupAll (
			T archiveEntries, final String name) throws ServiceException {
		return processAll(false, archiveEntries, ArchiveEntryValidator::lookup,
				TYPE, name);
	}
}
