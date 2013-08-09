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

package org.projectmaxs.module.filewrite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.projectmaxs.shared.util.Log;

public class FileManager {
	private static final Log LOG = Log.getLog();

	public static boolean saveToFile(String file, String content) {
		return saveToFile(file, content.getBytes());
	}

	public static boolean saveToFile(String file, byte[] bytes) {
		boolean success = true;
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(bytes);
		} catch (IOException e) {
			LOG.w("saveToFile", e);
			success = false;
		}
		finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					LOG.w("saveToFile", e);
					success = false;
				}
			}
		}
		return success;
	}
}
