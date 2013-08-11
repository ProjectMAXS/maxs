package org.projectmaxs.shared.global.aidl;

interface IFileWriteModuleService {

	String writeFileBytes(String file, in byte[] bytes);

}
