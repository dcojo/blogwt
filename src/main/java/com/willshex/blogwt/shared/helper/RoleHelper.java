//
//  RoleHelper.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 11 May 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.shared.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.willshex.blogwt.shared.api.datatype.Permission;
import com.willshex.blogwt.shared.api.datatype.Role;

/**
 * @author William Shakour (billy1380)
 *
 */
public class RoleHelper {
	public static final String ADMIN = "ADM";
	public static final String ADMIN_NAME = "System Administrator";
	public static final String ADMIN_DESCRIPTION = "allows users to perform any action (implicitly contains all permissions)";

	public static final Collection<Permission> ADMIN_PERMISSIONS = Collections
			.unmodifiableCollection(new ArrayList<>());

	public static Role createAdmin () {
		return create(ADMIN);
	}

	public static Role createFullAdmin () {
		return createFull(ADMIN, ADMIN_NAME, ADMIN_DESCRIPTION);
	}

	public static Map<String, Role> toLookup (Collection<Role> roles) {
		Map<String, Role> lookup = new HashMap<String, Role>();

		for (Role role : roles) {
			lookup.put(role.code, role);
		}

		return lookup;
	}

	public static Role createFull (String code, String name, String description,
			String... permissionCodes) {
		return new Role().code(code).name(name).description(description)
				.permissions(
						permissionCodes == null || permissionCodes.length == 0
								? null
								: Arrays.stream(permissionCodes)
										.map(i -> new Permission().code(i))
										.collect(Collectors.toList()));
	}

	public static Role create (String code) {
		return new Role().code(code);
	}

	public static Collection<Role> createAll () {
		List<Role> all = new ArrayList<Role>();

		all.add(createAdmin());

		return all;
	}
}
