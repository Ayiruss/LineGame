package himabindu.example.com.linegame;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import java.util.HashMap;

import utils.AppUtil;
import utils.DBManager;


@TargetApi(Build.VERSION_CODES.CUPCAKE) public class OpenGLES20Activity extends Activity {
    private MyGLSurfaceView mGLView;
    public int mGameLevel = 0;
    private Button butUndo;
    private Button butUndoAll;
    private Button butTrack;
    private Button butBestScore;
    private RelativeLayout rel_surface;
    int srcPressID;
    int srcLoopID;
    int srcTouchID;
    public static String GAME_LEVEL = "GAME_LEVEL";
    
    private SoundPool sndPool;
    private SoundPool sndLoopPool;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
        
        AppUtil.sharedObject().setScreenSize(display.getWidth(), display.getHeight());
        
        // get game level
        mGameLevel = getIntent().getIntExtra(GAME_LEVEL, 1);
        
        
        AppUtil.sharedObject().setLineNumber(mGameLevel);
        
        // sound
        sndPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sndLoopPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        srcPressID = sndPool.load(this, R.raw.press, 1);
        srcTouchID = sndPool.load(this, R.raw.sel, 1);
        srcLoopID = sndLoopPool.load(this, R.raw.looptrack, 1);
        
        sndLoopPool.stop(srcLoopID);
        sndLoopPool.play(srcLoopID, 1.0f, 1.0f, 1, -1, 0.0f);
        
        // open gl
        mGLView = new MyGLSurfaceView(this);
        mGLView.setBackgroundColor(Color.WHITE);
        rel_surface = (RelativeLayout)findViewById(R.id.mysurface);
        rel_surface.addView(mGLView);
        
        // event 
        butTrack = (Button)findViewById(R.id.but_track);
        butTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // undo operation
                mGLView.bIsTracked = !mGLView.bIsTracked;
                mGLView.nCurTrackPos = -1;
                
                if(mGLView.bIsTracked == true) butTrack.setText("Game");
                else butTrack.setText("Track");
                
                mGLView.invalidate();
                sndPool.play(srcPressID, 1f, 1f, 1, 0, 1.0f);
            }
        });
        
        butUndo = (Button)findViewById(R.id.but_undo);
        butUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // undo operation
                mGLView.undoOperation();
                sndPool.play(srcPressID, 1f, 1f, 1, 0, 1.0f);
            }
        });
        
        butUndoAll = (Button)findViewById(R.id.but_UndoAllME);
        butUndoAll.setOnClickListener(new View.OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				mGLView.undoAllOperation();
				sndPool.play(srcPressID, 1f, 1f, 1, 0, 1.0f);
			}
		});
        
        
        
        butBestScore = (Button)findViewById(R.id.but_bestscore);
        butBestScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	sndPool.play(srcPressID, 1f, 1f, 1, 0, 1.0f);
            	
                // undo operation

                HashMap<String, Integer> userInfo = mGLView.getBestScore();;
                if(userInfo == null)
                    return;
                int nTempMoves = userInfo.get(DBManager.KEY_MOVES + mGameLevel);
                int nTempLines = userInfo.get(DBManager.KEY_SCORE + mGameLevel);
                String strMessage = "Moves: " + nTempMoves + ", Score: " + nTempLines;
                AlertDialog alertDialog = new AlertDialog.Builder(OpenGLES20Activity.this).create();
                // Setting Dialog Title
                alertDialog.setTitle("Best Score");

                // Setting Dialog Message
                alertDialog.setMessage(strMessage);

                // Setting Icon to Dialog
                alertDialog.setIcon(R.drawable.ic_launcher);

                // Showing Alert Message
                alertDialog.show();

            }
        });
        
    }
    
    public void SoundTouch(){
    	sndPool.play(srcTouchID, 1f, 1f, 1, 0, 1.0f);
    }
    
   
    protected void onRestart(){
    	 super.onRestart();
    	 sndPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
         sndLoopPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
         srcPressID = sndPool.load(this, R.raw.press, 1);
         srcTouchID = sndPool.load(this, R.raw.sel, 1);
         srcLoopID = sndLoopPool.load(this, R.raw.looptrack, 1);
         
         sndLoopPool.stop(srcLoopID);
         sndLoopPool.play(srcLoopID, 1.0f, 1.0f, 1, -1, 0.0f);
    }
    protected void onPause(){
    	super.onPause();
    	sndLoopPool.stop(srcLoopID);
    } 
    protected void onStop(){
    	super.onStop();
    	sndLoopPool.stop(srcLoopID);
    } 
    protected void onDestory(){
    	super.onDestroy();
    	sndLoopPool.stop(srcLoopID);
    } 
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
