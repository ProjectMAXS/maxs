package org.projectmaxs.shared.aidl;

import org.projectmaxs.shared.Contact;

interface IMAXSService {

    // recent contact
    Contact getRecentContact();
    void setRecentContact(in Contact contact);

    // alias
    Contact getContactFromAlias(String alias);

    // xmpp status
    void updateXMPPStatusInformation(String type, String info);

}
