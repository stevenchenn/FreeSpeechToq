package steven.example.com.fsmtoqapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class PickColor extends Activity {

    private ColorPicker picker;
    private SVBar svBar;
    private OpacityBar opacityBar;
    public static String color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_color);

        picker = (ColorPicker) findViewById(R.id.picker);
        svBar = (SVBar) findViewById(R.id.svbar);
        opacityBar = (OpacityBar) findViewById(R.id.opacitybar);

        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pick_color, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeColorOnClick(View v){
        color = picker.getColor()+"";
        Intent resultIntent = new Intent();
        resultIntent.putExtra("color", color);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
