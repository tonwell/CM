package droid.crowdmap.basededados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ton
 */
public class DB {

    private SQLiteDatabase db;
    private static DB obj = null;
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
    private final String[] attr = {"_id", "lat", "lng", "signal", "operadora"};
    private DB(Context context){
        DBCore auxDB = new DBCore(context);
        db = auxDB.getWritableDatabase();
    }

    public static DB getInstance(Context context){
        if(obj==null){
            obj = new DB(context);
        }
        return obj;
    }

    public void insert(Dados d){
        Dados old = getOne(d.getLatitude(), d.getLongitude(), d.getOperadora());
        if(old.getSinal() == 99) {
            ContentValues val = new ContentValues();
            val.put("lat", d.getLatitude());
            val.put("lng", d.getLongitude());
            val.put("signal", d.getSinal());
            val.put("operadora", d.getOperadora());
            db.insert(tables[0], null, val);
        } else {
            update(d);
        }
        Log.d("Script", "BD => lat:"+d.getLatitude()+",lng:"+d.getLongitude()+",signal:"+d.getSinal()+",op:"+d.getOperadora());
    }

    public void insert(Dados d, String table){
        Dados old = getOne(d.getLatitude(), d.getLongitude(), d.getOperadora(), table);
        if(old.getSinal() == 99) {
            ContentValues val = new ContentValues();
            val.put("lat", d.getLatitude());
            val.put("lng", d.getLongitude());
            val.put("signal", expMovAvg(0, d.getSinal()));
            val.put("operadora", d.getOperadora());
            db.insert(table, null, val);
        } else {
            update(d, table);
        }
        Log.d("Script", "BD => lat:"+d.getLatitude()+",lng:"+d.getLongitude()+",signal:"+d.getSinal()+",op:"+d.getOperadora());
    }

    public void update(Dados d){
        ContentValues val = new ContentValues();
        Dados old = getOne(d.getLatitude(), d.getLongitude(), d.getOperadora());
        if(old.getSinal() != 99 && d.getSinal() != 99) {
            val.put("signal", expMovAvg(old.getSinal(), d.getSinal()));
            db.update(tables[0], val, "lat = ? and lng = ? and operadora = ?", new String[]{"" + d.getLatitude(), "" + d.getLongitude(), ""+d.getOperadora()});
        }
    }
    public void update(Dados d, String table){
        ContentValues val = new ContentValues();
        Dados old = getOne(d.getLatitude(), d.getLongitude(), d.getOperadora());
        if(old.getSinal() != 99 && d.getSinal() != 99) {
            val.put("signal", expMovAvg(old.getSinal(), d.getSinal()));
            db.update(table, val, "lat = ? and lng = ? and operadora = ?", new String[]{"" + d.getLatitude(), "" + d.getLongitude(), ""+d.getOperadora()});
        }
    }


    public void delete(Dados d){
        db.delete(tables[0], "lat = ? and lng = ? and operadora = ?", new String[]{""+d.getLatitude(), ""+ d.getLongitude(), ""+d.getOperadora()});
    }

    public void delete(Dados d, String table){
        db.delete(table, "lat = ? and lng = ? and operadora = ?", new String[]{""+d.getLatitude(), ""+ d.getLongitude(), ""+d.getOperadora()});
    }

    public List<Dados> getAll(){
        List<Dados> list = new ArrayList<Dados>();
        Cursor cursor = db.query(tables[0],attr,null,null,null,null,null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            do {
                Dados d = new Dados();
                d.setId(cursor.getLong(0));
                d.setLatitude(cursor.getDouble(1));
                d.setLongitude(cursor.getDouble(2));
                d.setSinal(cursor.getInt(3));
                d.setOperadora(cursor.getString(4));
                list.add(d);
            }while(cursor.moveToNext());
        }
        return list;
    }

    public Dados getOne(double lat, double lng, String op){
        Dados d = null;
        Cursor cursor = db.query(tables[0],attr,"lat = ? and lng = ? and operadora = ?", new String[]{""+lat, ""+lng, ""+op},null, null, null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            d = new Dados();
            d.setId(cursor.getLong(0));
            d.setLatitude(cursor.getDouble(1));
            d.setLongitude(cursor.getDouble(2));
            d.setSinal(cursor.getInt(3));
            d.setOperadora(cursor.getString(4));
        }
        if(d == null){
            d = new Dados();
            d.setLatitude(lat);
            d.setLongitude(lng);
            d.setOperadora(op);
            d.setSinal(99);
        }
        return d;
    }

    public Dados getOne(double lat, double lng, String op, String table){
        Dados d = null;
        Cursor cursor = db.query(table, attr,"lat = ? and lng = ? and operadora = ?", new String[]{""+lat, ""+lng, ""+op},null, null, null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            d = new Dados();
            d.setId(cursor.getLong(0));
            d.setLatitude(cursor.getDouble(1));
            d.setLongitude(cursor.getDouble(2));
            d.setSinal(cursor.getInt(3));
            d.setOperadora(cursor.getString(4));
        }
        if(d == null){
            d = new Dados();
            d.setLatitude(lat);
            d.setLongitude(lng);
            d.setOperadora(op);
            d.setSinal(99);
        }
        return d;
    }

    public Dados getBest(double lat, double lng, String[] op, String table){
        Dados d = null;
        Cursor cursor = db.query(table, attr,"lat = ? and lng = ? and operadora = ?", new String[]{""+lat, ""+lng, ""+op[0]},null, null, null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            d = new Dados();
            d.setId(cursor.getLong(0));
            d.setLatitude(cursor.getDouble(1));
            d.setLongitude(cursor.getDouble(2));
            d.setSinal(cursor.getInt(3));
            d.setOperadora(cursor.getString(4));
        }
        if(d == null){
            d = new Dados();
            d.setLatitude(lat);
            d.setLongitude(lng);
            d.setOperadora(op[0]);
            d.setSinal(99);
        }
        return d;
    }

    public double expMovAvg(double oldValue, double newValue){
        double alpha = 0.5;
        return oldValue * alpha + newValue * ( 1 - alpha );
    }
}
