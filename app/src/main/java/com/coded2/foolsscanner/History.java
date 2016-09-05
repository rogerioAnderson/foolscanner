package com.coded2.foolsscanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rogerio on 8/25/16.
 */
public class
History {
    private final static String ID_COLUMN_NAME="rowid";
    private final static String CONTENT_COLUMN_NAME="content";
    private final static String TYPE_COLUMN_NAME ="type";
    private final static String TABLE_NAME="history";
    private final static String SQL_INSERT_RECORD = "INSERT INTO "+TABLE_NAME+"("+CONTENT_COLUMN_NAME+","+TYPE_COLUMN_NAME+") VALUES (?,?)";
    private final static String SQL_LIST ="select rowid, content, type from history order by rowid desc";
    private final static String SQL_DELETE = "delete from history where rowid = ?";
    private final String [] DB_COLUMNS = {ID_COLUMN_NAME,CONTENT_COLUMN_NAME,TYPE_COLUMN_NAME};

    long rowid;
    String type;
    String content;

    void save(Context ctx){
            DBHelper dbHelper = new DBHelper(ctx);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            String[] params = {content,type};
            db.execSQL(SQL_INSERT_RECORD, params);
        }catch(Exception e){
            e.printStackTrace();
            Log.d("com.coded2.foolsscanner",e.getLocalizedMessage());
        }
        finally{
            db.close();
            dbHelper.close();
        }
    }

    static List<History> list(Context ctx){
        DBHelper dbHelper = new DBHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<History>result = new ArrayList<History>();
        try{
            Cursor cursor = db.rawQuery(SQL_LIST,null);
            try{
                if(cursor.moveToFirst()){
                    do{
                        History history = new History();
                        history.rowid = cursor.getLong(cursor.getColumnIndex(ID_COLUMN_NAME));
                        history.type = cursor.getString(cursor.getColumnIndex(TYPE_COLUMN_NAME));
                        history.content= cursor.getString(cursor.getColumnIndex(CONTENT_COLUMN_NAME));
                        result.add(history);
                    }while(cursor.moveToNext());

                }
            }finally {
                cursor.close();
            }

        }finally {
            db.close();
            dbHelper.close();
        }
        return result;
    }



    void delete(Context ctx){
        DBHelper dbHelper = new DBHelper(ctx);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            String[] params = {Long.toString(rowid)};
            db.execSQL(SQL_DELETE, params);
        }catch(Exception e){
            e.printStackTrace();
            Log.d("com.coded2.foolsscanner",e.getLocalizedMessage());
        }finally{
            db.close();
            dbHelper.close();
        }
    }
    @Override
    public String toString(){
        String retorno = "";
        if(content!=null || type!=null){
            if(content.length()>30){
                retorno = content.substring(0,26);
                retorno+="...";
                retorno = retorno.replaceAll("\n"," ");
            }else{
                retorno = content;
            }
        }
        return retorno;
    }
}
