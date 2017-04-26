//  
//  RoleService.java
//  blogwt
//
//  Created by William Shakour on May 12, 2015.
//  Copyrights © 2015 WillShex Limited. All rights reserved.
//  
//
package com.willshex.blogwt.server.service.role;

import static com.willshex.blogwt.server.helper.PersistenceHelper.keyToId;
import static com.willshex.blogwt.server.service.persistence.PersistenceServiceProvider.provide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.willshex.blogwt.server.helper.SearchHelper;
import com.willshex.blogwt.shared.api.SortDirectionType;
import com.willshex.blogwt.shared.api.datatype.Role;
import com.willshex.blogwt.shared.api.datatype.RoleSortType;

final class RoleService implements IRoleService {

	public String getName () {
		return NAME;
	}

	@Override
	public Role getRole (Long id) {
		return provide().load().type(Role.class).id(id.longValue()).now();
	}

	@Override
	public Role addRole (Role role) {
		if (role.created == null) {
			role.created = new Date();
		}

		Key<Role> key = provide().save().entity(role).now();
		role.id = keyToId(key);

		return role;
	}

	@Override
	public Role updateRole (Role role) {
		provide().save().entity(role).now();
		return role;
	}

	@Override
	public void deleteRole (Role role) {
		provide().delete().entity(role);
	}

	@Override
	public List<Role> getRoles (Integer start, Integer count,
			RoleSortType sortBy, SortDirectionType sortDirection) {
		Query<Role> query = provide().load().type(Role.class);

		if (start != null) {
			query = query.offset(start.intValue());
		}

		if (count != null) {
			query = query.limit(count.intValue());
		}

		if (sortBy != null) {
			String condition = sortBy.toString();

			if (sortDirection != null) {
				switch (sortDirection) {
				case SortDirectionTypeDescending:
					condition = "-" + condition;
					break;
				default:
					break;
				}
			}

			query = query.order(condition);
		}

		return query.list();
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * com.willshex.blogwt.server.service.role.IRoleService#getRolesCount() */
	@Override
	public Long getRolesCount () {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * 
	 * @see com.willshex.blogwt.server.service.role.IRoleService#getIdRoles(java
	 * .util.Collection) */
	@Override
	public List<Role> getIdRoleBatch (Collection<Long> roleIds) {
		return new ArrayList<Role>(
				provide().load().type(Role.class).ids(roleIds).values());
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * com.willshex.blogwt.server.service.role.IRoleService#getCodeRole(java
	 * .lang.String) */
	@Override
	public Role getCodeRole (String code) {
		return provide().load().type(Role.class).filter("code", code).first()
				.now();
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * com.willshex.blogwt.server.service.role.IRoleService#getPartialNameRoles
	 * (java.lang.String, java.lang.Integer, java.lang.Integer,
	 * com.willshex.blogwt.shared.api.datatype.RoleSortType,
	 * com.willshex.blogwt.shared.api.SortDirectionType) */
	@Override
	public List<Role> getPartialNameRoles (String partialName, Integer start,
			Integer count, RoleSortType sortBy,
			SortDirectionType sortDirection) {
		Query<Role> query = provide().load().type(Role.class);

		if (start != null) {
			query = query.offset(start.intValue());
		}

		if (count != null) {
			query = query.limit(count.intValue());
		}

		if (sortBy != null) {
			String condition = sortBy.toString();

			if (sortDirection != null) {
				switch (sortDirection) {
				case SortDirectionTypeDescending:
					condition = "-" + condition;
					break;
				default:
					break;
				}
			}

			query = query.order(condition);
		}

		query = SearchHelper.addStartsWith("name", partialName, query);

		return query.list();
	}

}