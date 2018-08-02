package droid.crowdmap.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class Iniciar extends IntentService {

	public Iniciar() {
		super("Iniciar");
		// TODO Auto-generated constructor stub
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		AlarmeColeta alarme = new AlarmeColeta(this);
		SharedPreferences sp = this.getSharedPreferences("coleta", Context.MODE_PRIVATE);
		alarme.setMinutos(sp.getInt("minutos", 10));
		Log.i("Script","Service Iniciar iniciado");
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

}
