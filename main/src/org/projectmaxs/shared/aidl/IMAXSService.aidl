package org.projectmaxs.shared.aidl

import org.projectmaxs.shared.Contact;
import org.projectmax.shared.xmpp.XMPPStatusType;

interface IMAXSService {

    // recent contact
    Contact getRecentContact();
    void setRecentContact(Contact);
    
    // alias
    Contact getContactFromAlias(String alias);
    
    // xmpp status
    void updateXMPPStatusInformation(String type, String info);
    
}
