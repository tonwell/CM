package droid.crowdmap;

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(icicle: Bundle?){
        super.onCreate(icicle)
        setContentView(R.layout.activity_main)
        toast("Aqui ta funfando com kotlin")
    }

    fun toast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }
}