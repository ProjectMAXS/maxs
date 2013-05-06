package org.projectmaxs.shared.aidl;

import org.projectmaxs.shared.xmpp.XMPPMessage;

interface IMAXSModuleService {
    XMPPMessage executeCommand(String cmd, String subCmd, String args, int cmdID);
}

