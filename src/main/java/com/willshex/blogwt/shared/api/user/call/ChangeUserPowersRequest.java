//  
//  ChangeUserPowersRequest.java
//  xsdwsdl2code
//
//  Created by William Shakour on September 2, 2015.
//  Copyright © 2015 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.shared.api.user.call;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.willshex.blogwt.shared.api.Request;
import com.willshex.blogwt.shared.api.datatype.Permission;
import com.willshex.blogwt.shared.api.datatype.Role;
import com.willshex.blogwt.shared.api.datatype.User;

public class ChangeUserPowersRequest extends Request {

	public User user;
	public List<Role> roles;
	public List<Permission> permissions;
	public Boolean assign;

	@Override
	public JsonObject toJson () {
		JsonObject object = super.toJson();
		JsonElement jsonUser = user == null ? JsonNull.INSTANCE : user.toJson();
		object.add("user", jsonUser);
		JsonElement jsonRoles = JsonNull.INSTANCE;
		if (roles != null) {
			jsonRoles = new JsonArray();
			for (int i = 0; i < roles.size(); i++) {
				JsonElement jsonRolesItem = roles.get(i) == null ? JsonNull.INSTANCE
						: roles.get(i).toJson();
				((JsonArray) jsonRoles).add(jsonRolesItem);
			}
		}
		object.add("roles", jsonRoles);
		JsonElement jsonPermissions = JsonNull.INSTANCE;
		if (permissions != null) {
			jsonPermissions = new JsonArray();
			for (int i = 0; i < permissions.size(); i++) {
				JsonElement jsonPermissionsItem = permissions.get(i) == null ? JsonNull.INSTANCE
						: permissions.get(i).toJson();
				((JsonArray) jsonPermissions).add(jsonPermissionsItem);
			}
		}
		object.add("permissions", jsonPermissions);
		JsonElement jsonAssign = assign == null ? JsonNull.INSTANCE
				: new JsonPrimitive(assign);
		object.add("assign", jsonAssign);
		return object;
	}

	@Override
	public void fromJson (JsonObject jsonObject) {
		super.fromJson(jsonObject);
		if (jsonObject.has("user")) {
			JsonElement jsonUser = jsonObject.get("user");
			if (jsonUser != null) {
				user = new User();
				user.fromJson(jsonUser.getAsJsonObject());
			}
		}
		if (jsonObject.has("roles")) {
			JsonElement jsonRoles = jsonObject.get("roles");
			if (jsonRoles != null) {
				roles = new ArrayList<Role>();
				Role item = null;
				for (int i = 0; i < jsonRoles.getAsJsonArray().size(); i++) {
					if (jsonRoles.getAsJsonArray().get(i) != null) {
						(item = new Role()).fromJson(jsonRoles.getAsJsonArray()
								.get(i).getAsJsonObject());
						roles.add(item);
					}
				}
			}
		}

		if (jsonObject.has("permissions")) {
			JsonElement jsonPermissions = jsonObject.get("permissions");
			if (jsonPermissions != null) {
				permissions = new ArrayList<Permission>();
				Permission item = null;
				for (int i = 0; i < jsonPermissions.getAsJsonArray().size(); i++) {
					if (jsonPermissions.getAsJsonArray().get(i) != null) {
						(item = new Permission()).fromJson(jsonPermissions
								.getAsJsonArray().get(i).getAsJsonObject());
						permissions.add(item);
					}
				}
			}
		}

		if (jsonObject.has("assign")) {
			JsonElement jsonAssign = jsonObject.get("assign");
			if (jsonAssign != null) {
				assign = Boolean.valueOf(jsonAssign.getAsBoolean());
			}
		}
	}

	public ChangeUserPowersRequest user (User user) {
		this.user = user;
		return this;
	}

	public ChangeUserPowersRequest roles (List<Role> roles) {
		this.roles = roles;
		return this;
	}

	public ChangeUserPowersRequest permissions (List<Permission> permissions) {
		this.permissions = permissions;
		return this;
	}

	public ChangeUserPowersRequest assign (Boolean assign) {
		this.assign = assign;
		return this;
	}
}