package org.projectmaxs.shared.aidl;

import org.projectmaxs.shared.Contact;

interface IContactsModuleService {

    List<Contact> lookupContact(String lookupInfo);
    
    List<Contact> lookupContactFromNumber(String number);

	Contact lookupOneContactFromNumber(String number);
}
