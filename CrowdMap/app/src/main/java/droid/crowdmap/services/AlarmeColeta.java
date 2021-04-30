package droid.crowdmap.services;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmeColeta {

	private final Calendar cal;
	private final PendingIntent pintent;
	private final AlarmManager alarm;
	private static final int MINUTO = 10 * 1000;

	public AlarmeColeta(Context contexto) {
		cal = Calendar.getInstance();
		Intent coletarDados = new Intent(contexto, ColetaDadosService.class);
		pintent = PendingIntent.getService(contexto, 0, coletarDados, 0);
		alarm = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
	}

	public void setMinutos(int minutos) {
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), minutos * MINUTO, pintent);
		Log.d("CMAlarmeColeta", "Service setado para " + minutos + " minuto(s)");
	}

	public void desligarAlarme() {
		alarm.cancel(pintent);
		Log.d("CMAlarmeColeta", "Service encerrado");
	}
}