package himabindu.example.com.linegame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import java.util.ArrayList;

import utils.AppUtil;


public class MainActivity extends Activity {
    Button but_start;
    EditText text_level;    
    Spinner spinner_color_src;
    Spinner spinner_color_target;
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayList<String> listIColor=new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControl();

    }
    private void initControl()
    {
        but_start = (Button)findViewById(R.id.but_start);
        but_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGameRoomActivity();
            }
        });
        text_level = (EditText)findViewById(R.id.edit_level);
        spinner_color_src = (Spinner)findViewById(R.id.spinner_color_src);
        spinner_color_target = (Spinner)findViewById(R.id.spinner_color_dst);
        // initialize level spinner

        // initialize color spinner
        listIColor.add("RED");
        listIColor.add("GREEN");
        listIColor.add("BLUE");
        listIColor.add("YELLOW");
        listIColor.add("CYAN");
        listIColor.add("MAGENTA");
        ArrayAdapter<String> adapter_color_src = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listIColor);
        spinner_color_src.setAdapter(adapter_color_src);
        spinner_color_src.setSelection(0);
        ArrayAdapter<String> adapter_color_target = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listIColor);
        spinner_color_target.setAdapter(adapter_color_target);
        spinner_color_target.setSelection(2);
    }
    private void launchGameRoomActivity()
    {
        int nGameLevel = 1;
    
        if(!text_level.getText().toString().equalsIgnoreCase("")) nGameLevel = Integer.valueOf(text_level.getText().toString());
        
        if(nGameLevel < 1) {
        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            // Setting Dialog Title
            alertDialog.setTitle("Warning");

            // Setting Dialog Message
            alertDialog.setMessage("Line Count must even, greater than 2, and less than 30");

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.ic_launcher);

                      // Showing Alert Message
            alertDialog.show();
            return;
        }
        
        AppUtil.sharedObject().SRC_COLOR = spinner_color_src.getSelectedItemPosition();
        AppUtil.sharedObject().TARGET_COLOR = spinner_color_target.getSelectedItemPosition();
        Intent intent = new Intent(this, OpenGLES20Activity.class);
        intent.putExtra(OpenGLES20Activity.GAME_LEVEL, nGameLevel);
        startActivity(intent);
    }

}
