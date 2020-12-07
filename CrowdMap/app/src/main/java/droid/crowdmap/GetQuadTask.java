package droid.crowdmap;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import droid.crowdmap.basededados.DB;
import droid.crowdmap.basededados.Dados;
import droid.crowdmap.fabricas.DrawAPI;

class GetQuadTask {
//class GetQuadTask extends AsyncTask<Dados, Void, Dados> {
//    DB dao;
//    MainActivity activity;
//    ArrayList<PolylineOptions> horizontals, verticals;
//
//    double scale;
//
//    public GetQuadTask(MainActivity activity, double scale, ArrayList<PolylineOptions> horizontals,
//                       ArrayList<PolylineOptions> verticals) {
//        this.activity = activity;
//        this.scale = scale;
//        this.horizontals = horizontals;
//        this.verticals = verticals;
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        // handleDrawLines(horizontals);
//        // handleDrawLines(verticals);
//        dao = DB.getInstance(activity);
//
//    }
//
//    @Override
//    protected void onPostExecute(Dados dados) {
//        super.onPostExecute(dados);
//        Log.d("CMMainActivity", "dados :: " + dados.toString());
//        activity.fillQuads(dados, scale);
//    }
//
//    @Override
//    protected Dados doInBackground(Dados... dados) {
//        Dados dp = dados[0];
//        int index = (int) (Math.log(scale / 0.002) / Math.log(2));
//        String table = "zoom" + index;
//        Dados d = dao.getOne(dp.getLatitude(), dp.getLongitude(), dp.getOperadora(), table);
//
//        double lat = DrawAPI.getIdCoord(d.getLatitude(), scale);
//        double lng = DrawAPI.getIdCoord(d.getLongitude(), scale);
//        Log.d("CMMainActivity", "DrawAPI.getIdCoord :: lat: " + lat + ", lng:" + lng);
//
//        d.setLatitude(lat);
//        d.setLongitude(lng);
//
//        return d;
//    }
}