package org.projectmaxs.shared.aidl;

import org.projectmaxs.shared.Contact;

interface IFileWriteModuleService {

	String writeFileBytes(String file, in byte[] bytes);

}
