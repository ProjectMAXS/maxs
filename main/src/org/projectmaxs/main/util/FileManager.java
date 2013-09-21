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
import java.io.IOException;
import java.util.Date;

import org.projectmaxs.shared.global.GlobalConstants;

public class FileManager {

	private static final File mMAXSSettingsDirectory = new File(
			GlobalConstants.MAXS_EXTERNAL_STORAGE, "Settings");

	public static File createFile(String file) throws IOException {
		File res = new File(file);
		if (res.exists()) {
			if (!res.delete()) throw new IOException("Can not delete " + res.getAbsolutePath());
		}
		if (!res.createNewFile())
			throw new IOException("Can not create file " + res.getAbsolutePath());
		return res;
	}

	public static File getTimestampedSettingsExportDir() {
		String dateString = Constants.ISO8601_DATE_FORMAT.format(new Date());
		dateString = dateString.replace(':', '-');
		File timestampedDir = new File(mMAXSSettingsDirectory, dateString);
		return timestampedDir;
	}
}
