/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.zendesk;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import com.zendesk.*;

public class ZendeskModule extends TiModule {

	TiContext context;
	
	public ZendeskModule(TiContext context) {
		super(context);
		this.context = context;
	}


	public void createDialog() {
				
		ZendeskDialog.Builder(this.context.getActivity().getApplicationContext())
        .setTitle("custom title")
        .setDescription("custom description")
        .setUrl("subdomain.zendesk.com")
    .setTag("dropbox")
        .create();
	}


}
