package com.ecchilon.sadpanda.menu;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static android.view.MenuItem.*;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.view.menu.MenuBuilder;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

/**
 * Implementation of the {@link android.view.ContextMenu} interface.
 * Most clients of the menu framework will never need to touch this class.  However, if the client has a window that is
 * not a content view of a Dialog or Activity (for example, the view was added directly to the window manager) and needs
 * to show context menus, it will use this class.
 * To use this class, instantiate it via {@link #ContextMenuBuilder(Context)}, and optionally populate it with any of
 * your custom items.  Finally, call {@link #show()} which will populate the menu with a view's context
 * menu items and show the context menu.
 */

public class ContextMenuBuilder extends MenuBuilder implements ContextMenu {

	public interface OnCreateMenuListener {
		void onCreateMenu(ContextMenu contextMenu, Context context);
	}

	private OnCreateMenuListener mCreateListener;
	private OnMenuItemClickListener mSelectListener;

	public ContextMenuBuilder(Context context) {
		super(context);

		setCallback(new MenuCallback());
	}

	public ContextMenuBuilder setOnCreateMenuListener(OnCreateMenuListener listener) {
		mCreateListener = listener;
		return this;
	}

	public ContextMenuBuilder setOnMenuItemClickListener(OnMenuItemClickListener listener) {
		mSelectListener = listener;
		return this;
	}

	public ContextMenu setHeaderIcon(Drawable icon) {
		return (ContextMenu) super.setHeaderIconInt(icon);
	}

	public ContextMenu setHeaderIcon(int iconRes) {
		return (ContextMenu) super.setHeaderIconInt(iconRes);
	}

	public ContextMenu setHeaderTitle(CharSequence title) {
		return (ContextMenu) super.setHeaderTitleInt(title);
	}

	public ContextMenu setHeaderTitle(int titleRes) {
		return (ContextMenu) super.setHeaderTitleInt(titleRes);
	}

	public ContextMenu setHeaderView(View view) {
		return (ContextMenu) super.setHeaderViewInt(view);
	}

	/**
	 * Shows this context menu, allowing the optional original view (and its ancestors) to add items.
	 */
	public Void show() {
		if(mCreateListener != null) {
			mCreateListener.onCreateMenu(this, getContext());
		}

		if (getVisibleItems().size() > 0) {
			//FIXME no more MenuDialogHelper
		}
		return null;
	}

	private class MenuCallback implements Callback {
		@Override
		public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
			if(mSelectListener != null) {
				mSelectListener.onMenuItemClick(menuItem);
			}
			return false;
		}

		@Override
		public void onMenuModeChange(MenuBuilder menuBuilder) {

		}
	}
}
