package org.chat21.android.core.contacts.synchronizers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactFetcher {

    private final Context context;

    public ContactFetcher(Context c) {
        this.context = c;
    }


    public ArrayList<Contact> fetchAll() {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI
        };
        ArrayList<Contact> listContacts = new ArrayList<>();
        CursorLoader cursorLoader = new CursorLoader(context,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields, // the columns to retrieve
                null, // the selection criteria (none)
                null, // the selection args (none)
                null // the sort order (default)
        );

        Cursor c = cursorLoader.loadInBackground();

        final Map<String, Contact> contactsMap = new HashMap<>(c.getCount());

        if (c.moveToFirst()) {

            int idIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int imageUri = c.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI);

            do {
                String contactId = c.getString(idIndex);
                String contactDisplayName = c.getString(nameIndex);
                String contactAvatarImage = c.getString(imageUri);
                Contact contact = new Contact(contactId, contactDisplayName, contactAvatarImage);
                contactsMap.put(contactId, contact);
                listContacts.add(contact);
            } while (c.moveToNext());
        }

        c.close();

        matchContactNumbers(contactsMap);
        matchContactEmails(contactsMap);

        return listContacts;
    }

    public void matchContactNumbers(Map<String, Contact> contactsMap) {
        // Get numbers
        final String[] numberProjection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        };

        Cursor phone = new CursorLoader(context,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                numberProjection,
                null,
                null,
                null).loadInBackground();

        if (phone.moveToFirst()) {
            final int contactNumberColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            final int contactTypeColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            final int contactIdColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);

            while (!phone.isAfterLast()) {
                final String number = phone.getString(contactNumberColumnIndex);
                final String contactId = phone.getString(contactIdColumnIndex);
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    continue;
                }
                final int type = phone.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                CharSequence phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel);
                contact.addNumber(number, phoneType.toString());
                phone.moveToNext();
            }
        }

        phone.close();
    }

    public void matchContactEmails(Map<String, Contact> contactsMap) {
        // Get email
        final String[] emailProjection = new String[]{
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
        };

        Cursor email = new CursorLoader(context,
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                emailProjection,
                null,
                null,
                null).loadInBackground();

        if (email.moveToFirst()) {
            final int contactEmailColumnIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            final int contactTypeColumnIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
            final int contactIdColumnsIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);

            while (!email.isAfterLast()) {
                final String address = email.getString(contactEmailColumnIndex);
                final String contactId = email.getString(contactIdColumnsIndex);
                final int type = email.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    continue;
                }
                CharSequence emailType = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.getResources(), type, customLabel);
                contact.addEmail(address, emailType.toString());
                email.moveToNext();
            }
        }

        email.close();
    }
}