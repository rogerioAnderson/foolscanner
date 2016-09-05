package com.coded2.foolsscanner;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.common.HybridBinarizer;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by rogerio on 8/25/16.
 */
public class DBTest extends AndroidTestCase {



    private void deleteAllRows(){
        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM history");
        db.close();
        dbHelper.close();
    }

    public void testCrud(){

        deleteAllRows();
        assertTrue("Banco Preenchido",History.list(getContext()).isEmpty());

        History history = new History();
        history.content = "FODA-SE";
        history.type = ParsedResultType.ADDRESSBOOK.toString();

        history.save(getContext());
        history.save(getContext());
        history.save(getContext());
        history.save(getContext());


        List<History> list = History.list(getContext());
        assertFalse("Banco vazio", list.isEmpty());


        int size = list.size();

        assertEquals("Records not corretly inserted",4, size);

        for (History historyData:list) {
            Log.d("TEST_DB",Long.toString(historyData.rowid));
            historyData.delete(getContext());
        }

        assertTrue("O banco nao foi apagado",History.list(getContext()).isEmpty());
    }



}
