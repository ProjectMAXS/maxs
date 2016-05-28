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

package org.projectmaxs.transport.xmpp.database;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.PacketParserUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Holds send but unacknowledged, by means of XEP-198: Stream Management, stanzas.
 */
public class SendUnackedStanzasTable {

	private static final Logger LOGGER = Logger.getLogger(SendUnackedStanzasTable.class.getName());

	private static final String TABLE_NAME = "sendunackedstanzas";
	private static final String COLUMN_NAME_STANZA_ID = "stanzaId";
	private static final String COLUMN_NAME_STANZA_XML = "stanzaXml";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_STANZA_ID + XMPPDatabase.TEXT_TYPE + XMPPDatabase.NOT_NULL + XMPPDatabase.COMMA_SEP +
		 COLUMN_NAME_STANZA_XML + XMPPDatabase.TEXT_TYPE + XMPPDatabase.NOT_NULL +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = XMPPDatabase.DROP_TABLE + TABLE_NAME;

	private static SendUnackedStanzasTable sXMPPMessageTable;

	public static SendUnackedStanzasTable getInstance(Context context) {
		if (sXMPPMessageTable == null) sXMPPMessageTable = new SendUnackedStanzasTable(context);
		return sXMPPMessageTable;
	}

	private final SQLiteDatabase mDatabase;

	private SendUnackedStanzasTable(Context context) {
		mDatabase = XMPPDatabase.getInstance(context).getWritableDatabase();
	}

	public void addStanza(Stanza stanza) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_STANZA_ID, stanza.getStanzaId());
		values.put(COLUMN_NAME_STANZA_XML, stanza.toXML().toString());

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert command in database");
	}

	public List<Stanza> getAllAndDelete() {
		List<Stanza> entries = new LinkedList<>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return entries;
		}

		do {
			byte[] stanzaBytes = c.getBlob(c.getColumnIndexOrThrow(COLUMN_NAME_STANZA_XML));
			Stanza stanza;
			try {
				stanza = PacketParserUtils.parseStanza(new String(stanzaBytes));
				entries.add(stanza);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "could not parse stanza", e);
			}
		} while (c.moveToNext());

		// Delete all rows
		mDatabase.delete(TABLE_NAME, null, null);

		c.close();
		return entries;
	}

	public boolean removeId(String id) {
		int res = mDatabase.delete(TABLE_NAME, COLUMN_NAME_STANZA_ID + "= ?", new String[] { id, });
		return res > 0;
	}
}