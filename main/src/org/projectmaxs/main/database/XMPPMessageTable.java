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

package org.projectmaxs.main.database;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.projectmaxs.shared.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class XMPPMessageTable {

	private static final Log LOG = Log.getLog();

	private static final String TABLE_NAME = "xmppMessage";
	private static final String COLUMN_NAME_XMPP_MESSAGE = TABLE_NAME;

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_XMPP_MESSAGE + MAXSDatabase.TEXT_TYPE +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static XMPPMessageTable sXMPPMessageTable;

	public static XMPPMessageTable getInstance(Context context) {
		if (sXMPPMessageTable == null) sXMPPMessageTable = new XMPPMessageTable(context);
		return sXMPPMessageTable;
	}

	private final SQLiteDatabase mDatabase;

	private XMPPMessageTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void addMessage(Message message) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_XMPP_MESSAGE, message.toXML());

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert command in database");
	}

	public List<Packet> getAndDelete() {
		List<Packet> packetList = new LinkedList<Packet>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) return packetList;

		List<String> xml = new ArrayList<String>(c.getCount());
		do {
			String messageXML = c.getString(c.getColumnIndex(COLUMN_NAME_XMPP_MESSAGE));
			xml.add(messageXML);
		} while (!c.moveToNext());

		mDatabase.delete(TABLE_NAME, null, null);

		for (String s : xml) {
			XmlPullParser xmlPullParser;
			try {
				xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
				xmlPullParser.setInput(new StringReader(s));
				Packet packet = PacketParserUtils.parseMessage(xmlPullParser);
				packetList.add(packet);
			} catch (XmlPullParserException e) {
				LOG.e("getAndDelete()", e);
			} catch (Exception e) {
				LOG.e("getAndDelete()", e);
			}
		}
		return packetList;
	}
}
