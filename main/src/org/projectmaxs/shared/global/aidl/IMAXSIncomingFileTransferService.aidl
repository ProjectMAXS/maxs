package org.projectmaxs.shared.global.aidl;

interface IMAXSIncomingFileTransferService {

    ParcelFileDescriptor incomingFileTransfer(String filename, long size, String description);

}
