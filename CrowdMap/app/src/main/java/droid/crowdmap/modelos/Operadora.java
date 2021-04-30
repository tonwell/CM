package droid.crowdmap.modelos;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Operadora {

    private final TelephonyManager telManager;
    private final String operadora;

    public Operadora(Context c) {
        telManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        operadora = telManager.getSimOperatorName();
    }

    public String getNome() {
        return operadora;
    }
}
