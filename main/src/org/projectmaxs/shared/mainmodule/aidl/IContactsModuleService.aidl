package org.projectmaxs.shared.mainmodule.aidl;

import org.projectmaxs.shared.mainmodule.Contact;

interface IContactsModuleService {

    List<Contact> lookupContact(String lookupInfo);
    
    List<Contact> lookupContactFromNumber(String number);

	Contact lookupOneContactFromNumber(String number);
}
