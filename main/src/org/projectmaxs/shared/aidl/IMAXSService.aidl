package org.projectmaxs.shared.aidl;

import org.projectmaxs.shared.Contact;
import org.projectmaxs.shared.xmpp.XMPPMessage;

interface IMAXSService {

    // recent contact
    Contact getRecentContact();
    void setRecentContact(in Contact contact);

    // alias
    Contact getContactFromAlias(String alias);

    // xmpp status
    void updateXMPPStatusInformation(String type, String info);

    // xmpp send
    void sendXMPPMessage(in XMPPMessage msg, int id);

}
