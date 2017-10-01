package droid.crowdmap.services;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmeColeta {

	private Calendar cal;
	private Intent coletarDados;
	private PendingIntent pintent;
	private AlarmManager alarm;
	private static int MINUTO = 60 * 1000;

	public AlarmeColeta(Context contexto) {
		cal = Calendar.getInstance();
		coletarDados = new Intent(contexto, ColetaDadosService.class);
		pintent = PendingIntent.getService(contexto, 0, coletarDados, 0);
		alarm = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);
	}

	public void setMinutos(int minutos) {
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				minutos * MINUTO, pintent);
		Log.d("Alarme", "Service setado para "+minutos+" minuto(s)");
	}

	public void desligarAlarme() {
		alarm.cancel(pintent);
		Log.d("Alarme", "Service encerrado");
	}
}