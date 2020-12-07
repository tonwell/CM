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
        // ActionBar actionBar = getActionBar();
        // actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
        // ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
        sp = getSharedPreferences("coleta", MODE_PRIVATE);
        setContentView(R.layout.activity_configuracao);
        final EditText edtColeta = (EditText) findViewById(R.id.edt_coleta);
        edtColeta.setText(String.valueOf(sp.getInt("minutos", 0)), TextView.BufferType.EDITABLE);

        final TextView txtColeta = (TextView) findViewById(R.id.txt_coleta);
        final TextView txtSqlite = (TextView) findViewById(R.id.txt_sqlite);
        final String txtTVSqlite = txtSqlite.getText().toString();
        final SeekBar sbSqlite = (SeekBar) findViewById(R.id.sb_sqlite);
        sbSqlite.setMax((int) Util.getFreeInternalMemory());
        sbSqlite.setProgress(sp.getInt("sqlite", 0));

        final TextView txtCache = (TextView) findViewById(R.id.txt_cache);
        final String txtTVCache = txtCache.getText().toString();
        final SeekBar sbCache = (SeekBar) findViewById(R.id.sb_cache);
        sbCache.setProgress(sp.getInt("cache", 0));

        final TextView descColeta = (TextView) findViewById(R.id.desc_coleta);
        final TextView descSqlite = (TextView) findViewById(R.id.desc_sqlite);
        final TextView descCache = (TextView) findViewById(R.id.desc_cache);

        final View labtempoView = (View) findViewById(R.id.view_labtempo);
        final View icuffView = (View) findViewById(R.id.view_icuff);
        final View uffView = (View) findViewById(R.id.view_uff);

        txtColeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDesc(descColeta);
            }
        });

        txtSqlite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDesc(descSqlite);
            }
        });

        txtCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDesc(descCache);
            }
        });

        Button btnApply = (Button) findViewById(R.id.btn_apply);

        sbSqlite.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtSqlite.setText(txtTVSqlite + Util.bytesToHuman(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbCache.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtCache.setText(txtTVCache + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edtTxt = edtColeta.getText().toString();
                int sqlite = sbSqlite.getProgress();
                int cache = sbCache.getProgress();
                if (!edtTxt.isEmpty() && Integer.valueOf(edtTxt) != 0 && sqlite != 0 && cache != 0) {
                    sp.edit().putInt("minutos", Integer.valueOf(edtTxt)).putInt("sqlite", sqlite).putInt("cache", cache)
                            .commit();
                    startActivity(new Intent(ConfiguracaoActivity.this, MainActivity.class));
                } else
                    Toast.makeText(ConfiguracaoActivity.this, "Preencher parâmetros", Toast.LENGTH_SHORT).show();
            }
        });
        final TextView labtempoTxtView = (TextView) findViewById(R.id.tv_labtempo);
        final TextView icuffTxtView = (TextView) findViewById(R.id.tv_icuff);
        final TextView uffTxtView = (TextView) findViewById(R.id.tv_uff);
        labtempoTxtView.setMovementMethod(LinkMovementMethod.getInstance());
        icuffTxtView.setMovementMethod(LinkMovementMethod.getInstance());
        uffTxtView.setMovementMethod(LinkMovementMethod.getInstance());

        /*
         * labtempoView.setOnClickListener(new View.OnClickListener() {
         * 
         * @Override public void onClick(View v) { Log.i("CMCfg", "clicked"); Intent
         * browser = new Intent(Intent.ACTION_VIEW, Uri.parse("www.uff.br"));
         * startActivity(browser); } });
         */
    }

    public void toggleDesc(View v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        super.onStart();

    }

    /*
     * public void onCheckboxClicked(View view) { // Is the view now checked?
     * boolean checked = ((CheckBox) view).isChecked();
     * 
     * // Check which checkbox was clicked switch(view.getId()) { case
     * R.id.cb_claro: if (checked){} else {} break; case R.id.cb_tim: if (checked){}
     * else {} break; case R.id.cb_vivo: if (checked) {} else {} break; } }
     * 
     * public void onRadioButtonClicked(View view) { // Is the button now checked?
     * boolean checked = ((RadioButton) view).isChecked();
     * 
     * // Check which radio button was clicked switch(view.getId()) { case
     * R.id.best_op: if (checked) {} break; case R.id.worst_op: if (checked) {}
     * break; } }
     * 
     * public void ativarColeta(View v) { /* Editor editor = sp.edit(); AlarmeColeta
     * alarme = new AlarmeColeta(this); int min =
     * Integer.parseInt(minutos.getText().toString()); int r =
     * Integer.parseInt(raio.getText().toString()); if (!coletar.isChecked()) {
     * alarme.desligarAlarme(); editor.putBoolean("ativado", false);
     * Toast.makeText(this, "Coleta desligada", Toast.LENGTH_LONG).show(); } else {
     * alarme.setMinutos(min); editor.putBoolean("ativado", true);
     * Toast.makeText(this, "Coleta programada a cada "+min+" minutos",
     * Toast.LENGTH_LONG).show(); } editor.putInt("minutos", min);
     * editor.putInt("raio", r); editor.commit(); finish();
     * 
     * }
     */
}
