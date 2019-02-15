package com.nicuz.rubrica.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private final Context mContext;

    public static final String DATABASE_NAME = "Rubrica.db";
    public static final int DATABASE_VERSION = 1;

    private static Database mInstance;

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static synchronized Database getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Database(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_CONTACTS_TABLE = "CREATE TABLE CONTACTS (" +
                "FIRSTNAME TEXT NOT NULL," +
                "LASTNAME TEXT NOT NULL," +
                "COMPANY TEXT," +
                "EMAIL TEXT," +
                "ADDRESS TEXT," +
                "PHOTO TEXT," +
                "BUSINESSCARD TEXT," +
                "PRIMARY KEY (FIRSTNAME, LASTNAME)" +
                ");";

        final String CREATE_PHONE_NUMBERS_TABLE = "CREATE TABLE NUMBERS (" +
                "ID INT NOT NULL," +
                "NUMBER TEXT NOT NULL," +
                "TYPE TEXT NOT NULL," +
                "INDEX_ORDER INT NOT NULL" +
                ");";

        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_PHONE_NUMBERS_TABLE);
    }

    public int getRowid(Contact contact) {
        SQLiteDatabase db = mInstance.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT rowid FROM CONTACTS WHERE FIRSTNAME=\"" + contact.getFirstName() + "\" AND LASTNAME=\"" + contact.getLastName() + "\";", null);
        try {
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndex("rowid"));
            }
            return -1;
        } finally {
            c.close();
            db.close();
        }
    }

    public ArrayList<Contact> getAllContacts() {
        SQLiteDatabase db = mInstance.getReadableDatabase();
        ArrayList<Contact> contactList = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM CONTACTS ORDER BY FIRSTNAME ASC, LASTNAME ASC", null);

        while(c.moveToNext()) {
            Contact contact = new Contact(
                    c.getString(c.getColumnIndex("FIRSTNAME")),
                    c.getString(c.getColumnIndex("LASTNAME")),
                    null,
                    null,
                    null,
                    null,
                    c.getString(c.getColumnIndex("PHOTO")),
                    null
            );
            contactList.add(contact);//add the item
        }
        c.close();
        db.close();
        return contactList;
    }

    public Contact getContact(int rowid) {
        SQLiteDatabase db = mInstance.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM CONTACTS WHERE rowid=\"" + rowid + "\";", null);
        try {
            if (c.moveToFirst()) {
                Contact contact = new Contact(
                        c.getString(c.getColumnIndex("FIRSTNAME")),
                        c.getString(c.getColumnIndex("LASTNAME")),
                        null,
                        c.getString(c.getColumnIndex("EMAIL")),
                        c.getString(c.getColumnIndex("COMPANY")),
                        c.getString(c.getColumnIndex("ADDRESS")),
                        c.getString(c.getColumnIndex("PHOTO")),
                        c.getString(c.getColumnIndex("BUSINESSCARD"))
                );
                contact.setPhoneNumbers(Database.getInstance(mContext).getPhoneNumbers(contact));
                return contact;
            }
            return null;
        } finally {
            c.close();
            db.close();
        }
    }

    public int addContact(Contact contact) {
        SQLiteDatabase db = mInstance.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FIRSTNAME", contact.getFirstName());
        cv.put("LASTNAME", contact.getLastName());

        if (contact.getEmail().length() != 0) {
            cv.put("EMAIL", contact.getEmail());
        }

        if (contact.getCompany().length() != 0) {
            cv.put("COMPANY", contact.getCompany());
        }

        if (contact.getAddress().length() != 0) {
            cv.put("ADDRESS", contact.getAddress());
        }

        cv.put("PHOTO", contact.getPhoto());
        cv.put("BUSINESSCARD", contact.getBusinessCardPhoto());

        if (db.insert("CONTACTS", null, cv) != -1) {
            for (int i = 0; i < contact.getPhoneNumbers().size(); i++) {
                db.execSQL("INSERT INTO NUMBERS VALUES (" +
                        "(SELECT rowid FROM CONTACTS WHERE FIRSTNAME=\"" + contact.getFirstName() + "\" AND LASTNAME=\"" + contact.getLastName() + "\")," +
                        "\"" + contact.getPhoneNumbers().get(i).getNumber() + "\"," +
                        "\"" + contact.getPhoneNumbers().get(i).getType() + "\"," +
                        i +
                        ");");
            }
            db.close();
            return 0;
        }

        db.close();
        return -1;
    }

    public void deleteContact(Contact contact) {
        SQLiteDatabase db = mInstance.getWritableDatabase();
        db.delete("NUMBERS",
                "ID = (SELECT rowid FROM CONTACTS WHERE FIRSTNAME = + \"" + contact.getFirstName() + "\" AND LASTNAME=\"" + contact.getLastName() + "\")",
                null);

        db.delete(
                "CONTACTS",
                "FIRSTNAME = + \"" + contact.getFirstName() + "\" AND LASTNAME=\"" + contact.getLastName() + "\"",
                null );

        db.close();
    }

    public void deletePhoneNumbers(int rowid) {
        SQLiteDatabase db = mInstance.getWritableDatabase();
        db.delete("NUMBERS",
                "ID = \"" + rowid + "\"",
                null);
    }

    public void updateContact(Contact contact, int rowid) {
        SQLiteDatabase db = mInstance.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FIRSTNAME", contact.getFirstName());
        cv.put("LASTNAME", contact.getLastName());

        if (contact.getEmail() != null) {
            cv.put("EMAIL", contact.getEmail());
        } else {
            cv.putNull("EMAIL");
        }

        if (contact.getCompany() != null) {
            cv.put("COMPANY", contact.getCompany());
        } else {
            cv.putNull("COMPANY");
        }

        if (contact.getAddress() != null) {
            cv.put("ADDRESS", contact.getAddress());
        } else {
            cv.putNull("ADDRESS");
        }

        cv.put("PHOTO", contact.getPhoto());
        cv.put("BUSINESSCARD", contact.getBusinessCardPhoto());

        deletePhoneNumbers(rowid);

        db.update("CONTACTS",
                cv,
                "rowid = \"" + rowid + "\"",
                null);

        for (int i=0; i<contact.getPhoneNumbers().size(); i++) {
            db.execSQL("INSERT INTO NUMBERS VALUES (" +
                    rowid + "," +
                    "\"" + contact.getPhoneNumbers().get(i).getNumber() + "\"," +
                    "\"" + contact.getPhoneNumbers().get(i).getType() + "\"," +
                    i +
                    ");");
        }

        db.close();
    }

    public ArrayList<PhoneNumber> getPhoneNumbers(Contact contact) {
        SQLiteDatabase db = mInstance.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT NUMBER, TYPE " +
                "FROM NUMBERS " +
                "WHERE ID = (SELECT rowid FROM CONTACTS WHERE FIRSTNAME=\"" + contact.getFirstName() + "\" AND LASTNAME=\"" + contact.getLastName() + "\") " +
                "ORDER BY INDEX_ORDER ASC;", null);

        ArrayList<PhoneNumber> phoneNumbers = new ArrayList<>();
        while (c.moveToNext()) {
            PhoneNumber phoneNumber = new PhoneNumber(
                    c.getString(c.getColumnIndex("NUMBER")),
                    c.getString(c.getColumnIndex("TYPE"))
                    );
            phoneNumbers.add(phoneNumber);
        }
        c.close();

        return phoneNumbers;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
