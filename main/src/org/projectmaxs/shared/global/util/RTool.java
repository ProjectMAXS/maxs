/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.projectmaxs.shared.global.util;

import java.lang.reflect.Field;

import android.content.Context;

/**
 * A tool for Android R (resource) files wich allows one to retrieve resources and their IDs via
 * reflection. This is mostly useful if multiple packages ("Apps") share some resource definitions.
 *
 * @author Florian Schmaus flo@geekplace.eu
 *
 */
public class RTool {

	public static String getString(Context context, String resourceName) {
		int resId = getStringId(context, resourceName);
		return context.getResources().getString(resId);
	}

	public static int getStringId(Context context, String resourceName) {
		String rClassString = context.getPackageName() + ".R";
		Class<?> rclass;
		try {
			rclass = Class.forName(rClassString);
		} catch (ClassNotFoundException e) {
			// There must be an R class. If not something went wrong.
			throw new IllegalStateException(e);
		}
		return getStringId(rclass, resourceName);
	}

	public static int getStringId(Class<?> rclass, String resourceName) {
		return getId(rclass, "string", resourceName);
	}

	private static int getId(Class<?> rClass, String resourceType, String resourceName) {
		Class<?>[] declaredClasses = rClass.getDeclaredClasses();
		Class<?> resourceTypeClass = null;
		for (Class<?> clazz : declaredClasses) {
			if (clazz.getSimpleName().equals(resourceType)) {
				resourceTypeClass = clazz;
				break;
			}
		}
		if (resourceTypeClass == null) {
			throw new IllegalArgumentException();
		}
		Field resourceField;
		try {
			resourceField = resourceTypeClass.getField(resourceName);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
		int id;
		try {
			id = resourceField.getInt(null);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
		return id;
	}
}
