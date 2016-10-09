package pl.marchuck.eeeeeeee;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Project "EEEEeeee"
 * <p/>
 * Created by Lukasz Marczak
 * on 09.10.16.
 */
public class H extends SQLiteOpenHelper {

    public H(Context context,
             String name,
             SQLiteDatabase.CursorFactory factory,
             int version) {
        super(context, name, factory, version);
    }

    public H(Context context,
             String name,
             SQLiteDatabase.CursorFactory factory,
             int version,
             DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
