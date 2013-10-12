package org.projectmaxs.shared.global.aidl;

interface IFileReadModuleService {

    byte[] readFileBytes(String file);
    boolean isFile(String file);

}
