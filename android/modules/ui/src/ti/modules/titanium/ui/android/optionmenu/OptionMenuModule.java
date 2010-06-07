/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.android.optionmenu;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiFileHelper;

import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

public class OptionMenuModule extends TiModule {

	private static final String LCAT = "OptionMenuModule";
	private static final boolean DBG = TiConfig.LOGD;
	private TiContext context; 
	
	public OptionMenuModule(TiContext tiContext) {
		super(tiContext);
		this.context = tiContext;
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy)
	{
		Log.d(LCAT, "Key is " + key);
		
		if ("menu".equals(key)) {
			setMenuListener();
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}
	
	public TiContext getContext() {
		return context;
	}

	private void setMenuListener()
	{
		final OptionMenuModule me = this;

		context.setOnMenuEventListener(new TiContext.OnMenuEvent()
		{

			private HashMap<Integer, MenuItemProxy> itemMap;

			private MenuProxy getMenuProxy() {
				return (MenuProxy) me.getDynamicValue("menu");
			}

			@Override
			public boolean hasMenu()
			{
				Log.d(LCAT, "hasMenu result is " + TiConvert.toString(getMenuProxy() != null));				
				return getMenuProxy() != null;
			}

			@Override
			public boolean menuItemSelected(MenuItem item) {
				MenuItemProxy mip = itemMap.get(item.getItemId());
				if (mip != null) {
					mip.fireEvent("click", null);
					return true;
				}
				return false;
			}

			@Override
			public boolean prepareMenu(Menu menu)
			{
				Log.d(LCAT, "prepareMenu result is " + TiConvert.toString(menu != null));

				menu.clear();
				MenuProxy mp = getMenuProxy();
				if (mp != null) {
					Log.d(LCAT, "prepareMenu mp isn't null.");
					ArrayList<MenuItemProxy> menuItems = mp.getMenuItems();
					itemMap = new HashMap<Integer, MenuItemProxy>(menuItems.size());
					int id = 0;

					for (MenuItemProxy mip : menuItems) {
						String title = TiConvert.toString(mip.getDynamicValue("title"));
						if (title != null) {
							MenuItem mi = menu.add(0, id, 0, title);
							itemMap.put(id, mip);
							id += 1;

							String iconPath = TiConvert.toString(mip.getDynamicValue("icon"));
							if (iconPath != null) {
				     			Drawable d = null;
								TiFileHelper tfh = new TiFileHelper(getTiContext().getActivity());
								d = tfh.loadDrawable(iconPath, false);
								if (d != null) {
									mi.setIcon(d);
								}
							}
						}
					}
					return true;
				} else {
					Log.d(LCAT, "prepareMenu mp IS null.");
					return false;
				}
			}
		});
	}
}
