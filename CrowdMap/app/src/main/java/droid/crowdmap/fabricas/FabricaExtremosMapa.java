package droid.crowdmap.fabricas;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

public class FabricaExtremosMapa {

	public static LatLng nordeste(GoogleMap mapa) {
        VisibleRegion vr = mapa.getProjection().getVisibleRegion();
        return vr.latLngBounds.northeast;
	}

	public static LatLng noroeste(GoogleMap mapa) {
        VisibleRegion vr = mapa.getProjection().getVisibleRegion();
		return new LatLng(vr.latLngBounds.southwest.latitude, vr.latLngBounds.northeast.longitude);
	}

	public static LatLng sudeste(GoogleMap mapa) {
        VisibleRegion vr = mapa.getProjection().getVisibleRegion();
        return new LatLng(vr.latLngBounds.northeast.latitude, vr.latLngBounds.southwest.longitude);
	}

	public static LatLng sudoeste(GoogleMap mapa) {
		VisibleRegion vr = mapa.getProjection().getVisibleRegion();
		return vr.latLngBounds.southwest;
	}
}

