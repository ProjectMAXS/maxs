package org.projectmaxs.shared.aidl;

import org.projectmaxs.shared.Contact;

interface IMAXSRemoteService {

    // recent contact
    Contact getRecentContact();

    // alias
    Contact getContactFromAlias(String alias);

}
