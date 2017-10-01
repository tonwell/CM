package  droid.crowdmap.basededados;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ton
 */

public class DBCore extends SQLiteOpenHelper {

    private static final String DB_NAME = "CrowdMap.db";
    private static final int DB_VERSION = 4;
    private final String[] tables = {
            "zoom0",
            "zoom1",
            "zoom2",
            "zoom3",
            "zoom4",
            "zoom5",
            "zoom6",
            "zoom7",
            "zoom8",
            "zoom9",
            "zoom10",
            "zoom11",
            "zoom12",
            "zoom13",
            "zoom14",
            "zoom15"
    };
    public DBCore(Context context){
        super(context, DB_NAME ,null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(int i=0; i<tables.length; i++)
            db.execSQL("CREATE TABLE "+tables[i]+"(_id integer primary key autoincrement,lat double, lng double, signal double, operadora text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i=0; i<tables.length; i++)
            db.execSQL("DROP TABLE IF EXISTS "+tables[i]+";");
        onCreate(db);
    }
}


