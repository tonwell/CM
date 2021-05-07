package droid.crowdmap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.LinkMovementMethod;

public class ConfiguracaoActivity extends Activity {

    private SharedPreferences sp;
    private String[] opcoes = { "Claro", "Oi", "Tim", "Vivo" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("coleta", MODE_PRIVATE);
        setContentView(R.layout.activity_configuracao);

        final View labtempoView = (View) findViewById(R.id.view_labtempo);
        final View icuffView = (View) findViewById(R.id.view_icuff);
        final View uffView = (View) findViewById(R.id.view_uff);

        final TextView labtempoTxtView = (TextView) findViewById(R.id.tv_labtempo);
        final TextView icuffTxtView = (TextView) findViewById(R.id.tv_icuff);
        final TextView uffTxtView = (TextView) findViewById(R.id.tv_uff);
        labtempoTxtView.setMovementMethod(LinkMovementMethod.getInstance());
        icuffTxtView.setMovementMethod(LinkMovementMethod.getInstance());
        uffTxtView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        super.onStart();

    }
}
