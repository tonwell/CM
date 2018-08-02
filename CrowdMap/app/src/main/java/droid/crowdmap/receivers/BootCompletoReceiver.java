package droid.crowdmap.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import droid.crowdmap.services.AlarmeColeta;
import droid.crowdmap.services.Iniciar;


public class BootCompletoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
        SharedPreferences sp = arg0.getSharedPreferences("coleta", Context.MODE_PRIVATE);
		Log.d("BootReceiver", "Iniciando");
		AlarmeColeta alarme = new AlarmeColeta(arg0);
		alarme.setMinutos(sp.getInt("minutos", 10));
		arg0.startService(new Intent(arg0, Iniciar.class));
	}

}
