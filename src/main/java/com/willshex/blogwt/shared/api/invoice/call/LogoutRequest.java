//  
//  LogoutRequest.java
//  xsdwsdl2code
//
//  Created by William Shakour on April 30, 2015.
//  Copyright © 2015 SPACEHOPPER STUDIOS. All rights reserved.
//
package com.willshex.blogwt.shared.api.invoice.call;

import com.google.gson.JsonObject;
import com.willshex.blogwt.shared.api.Request;

public class LogoutRequest extends Request {
	@Override
	public JsonObject toJson () {
		JsonObject object = super.toJson();
		return object;
	}

	@Override
	public void fromJson (JsonObject jsonObject) {
		super.fromJson(jsonObject);
	}
}