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

package org.projectmaxs.main.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.projectmaxs.shared.util.Log;

import android.content.Context;
import android.os.Environment;

public class FileManager {

	private static final Log LOG = Log.getLog();
	private static FileManager sFileManager = null;

	private final Context mContext;
	private final File mMAXSExternalStorageDirectory = new File(Environment.getExternalStorageDirectory(), "MAXS");
	private final File mMAXSSettingsDirectory = new File(mMAXSExternalStorageDirectory, "Settings");

	private FileManager(Context context) {
		mContext = context;
	}

	public static synchronized FileManager getInstance(Context context) {
		if (sFileManager == null) sFileManager = new FileManager(context);
		return sFileManager;
	}

	public static File createFile(String file) throws IOException {
		File res = new File(file);
		if (res.exists()) {
			if (!res.delete()) throw new IOException("Can not delete " + res.getAbsolutePath());
		}
		if (!res.createNewFile()) throw new IOException("Can not create file " + res.getAbsolutePath());
		return res;
	}

	public static void saveToFile(String file, String content) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(content);
		} catch (IOException e) {
			LOG.w("saveToFile", e);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					LOG.w("saveToFile", e);
				}
			}
		}
	}

	public File getTimestampedSettingsExportDir() throws IOException {
		String dateString = Constants.ISO8601_DATE_FORMAT.format(new Date());
		dateString = dateString.replace(':', '-');
		File timestampedDir = new File(mMAXSSettingsDirectory, dateString);
		checkCreateDir(mMAXSSettingsDirectory);
		checkCreateDir(timestampedDir);
		return timestampedDir;
	}

	private static void checkCreateDir(File dir) throws IOException {
		if (!dir.exists()) dir.mkdirs();

		if (!dir.isDirectory()) throw new IOException(dir.getAbsolutePath() + " is not a directory");
	}
}
