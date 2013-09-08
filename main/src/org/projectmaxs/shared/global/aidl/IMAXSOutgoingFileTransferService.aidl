package org.projectmaxs.shared.global.aidl;

interface IMAXSOutgoingFileTransferService {

    ParcelFileDescriptor outgoingFileTransfer(String filename, long size, String description, String toJID);

}

