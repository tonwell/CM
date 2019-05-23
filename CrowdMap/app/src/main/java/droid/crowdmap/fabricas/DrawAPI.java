package droid.crowdmap.fabricas;

import android.graphics.Color;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

/**
 * Created by ton
 */
public class DrawAPI {

    private static final double trunc = 100000;

    public static ArrayList<PolylineOptions> drawLinesY(GoogleMap map, double scale) {
        LatLng top_right = FabricaExtremosMapa.nordeste(map);
        LatLng bottom_left = FabricaExtremosMapa.sudoeste(map);

        double iLat = Math.floor(bottom_left.latitude * trunc) / trunc;
        double iLng = Math.floor(bottom_left.longitude * trunc) / trunc;
        double fLat = Math.floor(top_right.latitude * trunc) / trunc;
        double fLng = Math.floor(top_right.longitude * trunc) / trunc;

        double y = iLng - ((iLng * trunc) % (scale * trunc)) / trunc;
        double fY = fLng + scale - ((fLng * trunc) % (scale * trunc)) / trunc;

        double iX = iLat - ((iLat * trunc) % (scale * trunc)) / trunc - scale;
        double fX = fLat + scale - ((fLat * trunc) % (scale * trunc)) / trunc;

        iX = getIdCoord(iX, 0.002);
        fX = getIdCoord(fX, 0.002);
        y = getIdCoord(y, 0.002);
        fY = getIdCoord(fY, 0.002);

        ArrayList<PolylineOptions> verticals = new ArrayList<PolylineOptions>();
        y -= scale;
        while (y <= fY) {
            PolylineOptions po = new PolylineOptions();
            // po.add(new LatLng(iLat, y), new LatLng(fLat, y));
            po.add(new LatLng(iX, y), new LatLng(fX, y));
            po.width(1);
            po.color(Color.BLACK);
            po.geodesic(false);
            verticals.add(po);
            y += scale;
        }
        return verticals;
    }

    public static ArrayList<PolylineOptions> drawLinesX(GoogleMap map, double scale) {
        LatLng top_right = FabricaExtremosMapa.nordeste(map);
        LatLng bottom_left = FabricaExtremosMapa.sudoeste(map);

        double iLat = Math.floor(bottom_left.latitude * trunc) / trunc;
        double iLng = Math.floor(bottom_left.longitude * trunc) / trunc;
        double fLat = Math.floor(top_right.latitude * trunc) / trunc;
        double fLng = Math.floor(top_right.longitude * trunc) / trunc;

        double iY = iLng - ((iLng * trunc) % (scale * trunc)) / trunc - scale;
        double fY = fLng + scale - ((fLng * trunc) % (scale * trunc)) / trunc;

        double x = iLat - ((iLat * trunc) % (scale * trunc)) / trunc;
        double fX = fLat + scale - ((fLat * trunc) % (scale * trunc)) / trunc;

        x = getIdCoord(x, 0.002);
        fX = getIdCoord(fX, 0.002);
        iY = getIdCoord(iY, 0.002);
        fY = getIdCoord(fY, 0.002);

        ArrayList<PolylineOptions> horizontals = new ArrayList<PolylineOptions>();

        x -= scale;
        while (x <= fX) {
            PolylineOptions po = new PolylineOptions();
            // po.add(new LatLng(x, iLng), new LatLng(x, fLng));
            po.add(new LatLng(x, iY), new LatLng(x, fY));
            po.width(1);
            po.color(Color.BLACK);
            po.geodesic(false);
            horizontals.add(po);
            x += scale;
        }

        return horizontals;
    }

    public static PolygonOptions fillQuad(LatLng p, double scale, double signal) {
        ArrayList<LatLng> ls = new ArrayList<LatLng>(); // Lista com os quatro cantos do quadrante

        int color;
        if (signal <= 0) {
            color = 0x99FF0000; // vermelho
        } else if (signal > 0 && signal <= 8) {
            color = 0x99FFA500; // laranja
        } else if (signal > 8 && signal <= 16) {
            color = 0x99FFFF00; // amarelo
        } else if (signal > 16 && signal <= 24) {
            color = 0x99CCFF00; // entre amarelo e verde
        } else if (signal > 24 && signal <= 31) {
            color = 0x9900FF00; // verde
        } else {
            color = -1;
        }

        double iLat = Math.floor(p.latitude * trunc) / trunc;
        double iLng = Math.floor(p.longitude * trunc) / trunc;

        iLat = getIdCoord(iLat, scale);
        iLng = getIdCoord(iLng, scale);

        Log.i("CMDrawAPI", "PONTO P iLat: " + iLat + ", iLng: " + iLng);

        ls.add(new LatLng(iLat, iLng));
        ls.add(new LatLng(iLat + scale, iLng));
        ls.add(new LatLng(iLat + scale, iLng + scale));
        ls.add(new LatLng(iLat, iLng + scale));

        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(ls); // Adiciona os pontos para criação do polígono com
                                   // map.addPolygon(polygonOptions);
        polygonOptions.strokeColor(Color.BLACK); // cor do traço
        polygonOptions.strokeWidth(0); // largura do traço
        if (color != -1)
            polygonOptions.fillColor(color);
        polygonOptions.geodesic(false);

        return polygonOptions;
    }

    // Gera o parte do id correspondente a dada coordenada seja latitude, seja
    // longitude
    public static double getIdCoord(double val, double scale) {
        double id;
        double nScale = scale * trunc;
        /*
         * / double nVal = Math.floor( val * trunc ); return (nVal >= 0) ? (nVal - nVal
         * % nScale) / trunc : (nVal + nVal % nScale) / trunc; /
         */
        double nVal = val * trunc;

        if (val >= 0) {
            nVal = Math.floor(nVal);
            id = (nVal - (nVal % nScale)) / trunc;
        } else {

            nVal = Math.abs(Math.ceil(nVal));
            if (nVal % nScale == 0)
                id = -nVal / trunc;
            else
                id = -(nVal + nScale - (nVal % nScale)) / trunc;

        }
        return id;
        // */
    }

    // retorna o id geral daquela coordenada
    public static LatLng getLatLngId(LatLng latLng) {
        return new LatLng(getIdCoord(latLng.latitude, 0.002), getIdCoord(latLng.longitude, 0.002));
    }

    // retorna o id para uma determinada tabela de zoom
    public static LatLng getLatLngId(LatLng latLng, double scale) {
        return new LatLng(getIdCoord(latLng.latitude, scale), getIdCoord(latLng.longitude, scale));
    }

    public static double rounding(double val, int dec) {
        return val < 0 ? Math.ceil(val * Math.pow(10, dec)) / Math.pow(10, dec)
                : Math.floor(val * Math.pow(10, dec)) / Math.pow(10, dec);
    }
}
