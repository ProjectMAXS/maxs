package org.projectmaxs.shared.mainmodule.aidl;

import org.projectmaxs.shared.mainmodule.Contact;

interface IMAXSRemoteService {

    // recent contact
    Contact getRecentContact();

    // alias
    Contact getContactFromAlias(String alias);

}
