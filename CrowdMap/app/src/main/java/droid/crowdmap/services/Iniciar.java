package droid.crowdmap.services;

import android.app.IntentService;
import android.content.Intent;
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
		alarme.setMinutos(1);
		Log.i("Script","Service Iniciar iniciado");
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

}
