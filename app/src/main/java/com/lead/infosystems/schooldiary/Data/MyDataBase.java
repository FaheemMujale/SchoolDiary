package com.lead.infosystems.schooldiary.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Faheem on 28-11-2016.
 */

public class MyDataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "schoolDiary.db";
    private static final String ID = "id";

    //chat contacts
    private static final String CHAT_CONTACT_TABLE = "chat_contact_table";

    private static final String CONTACT_USERID = "chat_userid";
    private static final String CONTACT_FIRST_NAME = "contact_first_name";
    private static final String CONTACT_LAST_NAME = "contact_last_name";

    String CREATE_CHAT_CONTACT = "create table "+CHAT_CONTACT_TABLE+" ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CONTACT_USERID+" TEXT, "+CONTACT_FIRST_NAME+" TEXT, "+CONTACT_LAST_NAME+" TEXT)";

    //active chats
    private static final String ACTIVE_CHAT_LIST_TABLE = "active_chat_table";
    private static final String CHAT_ID = "chat_id";
    private static final String USER1 = "user1";
    private static final String USER2 = "user2";
    private static final String LAST_MESSAGE = "message";
    private static final String DATE = "date";
    String CREATE_ACTIVE_CHATS = "create table "+ACTIVE_CHAT_LIST_TABLE+" ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CHAT_ID+" TEXT, "+USER1+" TEXT, "+USER2+" TEXT, "+LAST_MESSAGE+" TEXT, "+DATE+" TEXT)";

    //chat messages
    private static final String CHAT_MESSAFGE_TABLE = "chat_message_table";
    private static final String USER_ID = "user_id";
    private static final String MESSAGE = "message";
    String CREATE_CHAT_MESSAGE = "create table "+CHAT_MESSAFGE_TABLE+" ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CHAT_ID+" TEXT, "+USER_ID+" TEXT, "+MESSAGE+" TEXT, "+DATE+" TEXT)";




    SQLiteDatabase db;

    public MyDataBase(Context context) {
        super(context, DB_NAME, null, 1);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CHAT_CONTACT);
        db.execSQL(CREATE_ACTIVE_CHATS);
        db.execSQL(CREATE_CHAT_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+CHAT_CONTACT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ACTIVE_CHAT_LIST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+CHAT_MESSAFGE_TABLE);
        onCreate(db);
    }

/////////// working with chat contacts
    public void insertIntoCOntact(String userId,String firstName,String lastName){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACT_USERID,userId);
        contentValues.put(CONTACT_FIRST_NAME,firstName);
        contentValues.put(CONTACT_LAST_NAME,lastName);
        db.insert(CHAT_CONTACT_TABLE,null,contentValues);
    }
    public void clearContacts(){
        db.execSQL("DROP TABLE IF EXISTS " +CHAT_CONTACT_TABLE);
        db.execSQL(CREATE_CHAT_CONTACT);
    }
    public Cursor getContacts(){
        return db.rawQuery("select * from "+CHAT_CONTACT_TABLE,null);
    }

    ///////working with active chats
    public void newChat(String chatId,String user1,String user2,String date,String lastMessage){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHAT_ID,chatId);
        contentValues.put(USER1,user1);
        contentValues.put(USER2,user2);
        contentValues.put(LAST_MESSAGE,lastMessage);
        contentValues.put(DATE,date);
        int a = db.update(ACTIVE_CHAT_LIST_TABLE,contentValues,CHAT_ID+" = "+chatId,null);
        if(a==0){
           int b= (int) db.insert(ACTIVE_CHAT_LIST_TABLE,null,contentValues);
            Log.e("b",b+"");
        }
    }
    public Cursor getActiveChats(){
        return db.rawQuery("select * from "+ACTIVE_CHAT_LIST_TABLE,null);
    }

    //working with messages
    public void chatMessage(String chatId,String from,String message,String time){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHAT_ID,chatId);
        contentValues.put(USER_ID,from);
        contentValues.put(MESSAGE,message);
        contentValues.put(DATE,time);
        db.insert(CHAT_MESSAFGE_TABLE,null,contentValues);
    }

    public Cursor getChatMessages(String chatId){
        return db.rawQuery("select * from "+CHAT_MESSAFGE_TABLE+" where "+CHAT_ID+" = "+chatId,null);
    }
    public void getChatID(String myId, String userID) {
        Cursor data = db.rawQuery("select "+CHAT_ID+" from "+ACTIVE_CHAT_LIST_TABLE+" where ("+ USER1+" = "+myId+
                " and "+USER2+ " = "+userID+") or ("+ USER1+" = "+userID+ " and "+USER2+ " = "+myId+")",null);
        if(data.getCount()>0){
            data.moveToNext();
           Log.e( "mmmm", data.getString(0));
        }
    }
}
