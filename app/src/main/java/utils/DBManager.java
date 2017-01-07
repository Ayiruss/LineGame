package utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by WORYA on 1/26/2016.
 */
public class DBManager {
    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    int TOTAL_LEVEL = 100;

    // Sharedpref file name
    private static final String PREF_NAME = "line_games";
    public  static final String KEY_MOVES = "moves";
    public  static final String KEY_SCORE = "score";
    // Constructor
    public DBManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void saveDBInfo(int moves, int score, int level) {
        editor.putInt(KEY_MOVES + level, moves);
        editor.putInt(KEY_SCORE + level, score);
        // commit changes
        editor.commit();
    }
    public HashMap<String, Integer> getDBInfo() {
        HashMap<String, Integer> user = new HashMap<String, Integer>();
        // user name
        for(int i = 0; i < TOTAL_LEVEL; i++){
	        user.put(KEY_MOVES + i,	pref.getInt(KEY_MOVES + i, 0));
	        user.put(KEY_SCORE + i,	pref.getInt(KEY_SCORE + i, 0));
        }
        return user;
    }
}
