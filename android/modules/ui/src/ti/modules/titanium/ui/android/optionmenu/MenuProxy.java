/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.android.optionmenu;

import java.util.ArrayList;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiProxy;

public class MenuProxy extends TiProxy
{
	protected ArrayList<MenuItemProxy> menuItems;
	private TiContext context;

	public MenuProxy(TiContext tiContext, Object[] args) {
		super(tiContext);
		menuItems = new ArrayList<MenuItemProxy>();
		this.context = tiContext;
	}

	public void add(MenuItemProxy mip) {
		menuItems.add(mip);
	}

	protected ArrayList<MenuItemProxy> getMenuItems() {
		return menuItems;
	}
}
