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

package org.projectmaxs.shared.global;

import android.annotation.TargetApi;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.os.Build;

public class CrossProcessCursorWrapper extends CursorWrapper implements CrossProcessCursor {

	private final Cursor mCursor;

	public CrossProcessCursorWrapper(Cursor cursor) {
		super(cursor);
		mCursor = cursor;
	}

	@Override
	public void fillWindow(int position, CursorWindow window) {
		if (mCursor instanceof CrossProcessCursor) {
			final CrossProcessCursor crossProcessCursor = (CrossProcessCursor) mCursor;
			crossProcessCursor.fillWindow(position, window);
			return;
		}

		cursorFillWindow(mCursor, position, window);
	}

	@Override
	public CursorWindow getWindow() {
		if (mCursor instanceof CrossProcessCursor) {
			final CrossProcessCursor crossProcessCursor = (CrossProcessCursor) mCursor;
			return crossProcessCursor.getWindow();
		}

		return null;
	}

	@Override
	public boolean onMove(int oldPosition, int newPosition) {
		if (mCursor instanceof CrossProcessCursor) {
			final CrossProcessCursor crossProcessCursor = (CrossProcessCursor) mCursor;
			return crossProcessCursor.onMove(oldPosition, newPosition);
		}

		return true;
	}

	/**
	 * Fills the specified cursor window by iterating over the contents of the cursor.
	 * The window is filled until the cursor is exhausted or the window runs out
	 * of space.
	 * 
	 * The original position of the cursor is left unchanged by this operation.
	 * 
	 * @param cursor
	 *            The cursor that contains the data to put in the window.
	 * @param position
	 *            The start position for filling the window.
	 * @param window
	 *            The window to fill.
	 */
	@TargetApi(11)
	private static void cursorFillWindow(final Cursor cursor, int position,
			final CursorWindow window) {
		if (position < 0 || position >= cursor.getCount()) {
			return;
		}
		final int oldPos = cursor.getPosition();
		final int numColumns = cursor.getColumnCount();
		window.clear();
		window.setStartPosition(position);
		window.setNumColumns(numColumns);
		if (cursor.moveToPosition(position)) {
			do {
				if (!window.allocRow()) {
					break;
				}
				for (int i = 0; i < numColumns; i++) {
					// Cursor.getType() is only available from API 11 on, throw at least a
					// meaningful error message.
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						throw new UnsupportedOperationException(
								"This method is only availble on devices running Honeycomb (API 11) or higher");
					}
					final int type = cursor.getType(i);
					final boolean success;
					switch (type) {
					case Cursor.FIELD_TYPE_NULL:
						success = window.putNull(position, i);
						break;

					case Cursor.FIELD_TYPE_INTEGER:
						success = window.putLong(cursor.getLong(i), position, i);
						break;

					case Cursor.FIELD_TYPE_FLOAT:
						success = window.putDouble(cursor.getDouble(i), position, i);
						break;

					case Cursor.FIELD_TYPE_BLOB:
						byte[] blob = cursor.getBlob(i);
						success = blob != null ? window.putBlob(blob, position, i) : window
								.putNull(position, i);
						break;

					default: // assume value is convertible to String
					case Cursor.FIELD_TYPE_STRING:
						String string = cursor.getString(i);
						success = string != null ? window.putString(string, position, i) : window
								.putNull(position, i);
						break;
					}
					if (!success) {
						window.freeLastRow();
						break;
					}
				}
				position++;
			} while (cursor.moveToNext());
		}
		cursor.moveToPosition(oldPos);
	}
}