package utils;


import android.graphics.Color;


import himabindu.example.com.linegame.CLine;
import himabindu.example.com.linegame.Point;

public class AppUtil {
	private static AppUtil m_hInstance = null;
	private int SCREEN_WIDTH = 0;
	private int SCREEN_HEIGHT = 0;
	private int NUMBER_OF_LINE = 0;
	public  int MAX_LINE_NUMBER = 10;
	public  int SRC_COLOR = 0;
	public  int TARGET_COLOR = 0;
	public  int COLOR_ARRAY[] = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};
	public  String TAG_GAME_LEVEL = "GAME_LEVEL";

	public void setScreenSize(int nWidth, int nHeight) {
		// set screen size and measure rates for components... 
		SCREEN_WIDTH = nWidth;
		SCREEN_HEIGHT = nHeight;
	}
	public int getScreenWidth()
	{
		return SCREEN_WIDTH;
	}
	public int getScreenHeight()
	{
		return SCREEN_HEIGHT;
	}
	public static AppUtil sharedObject() {
		if (m_hInstance == null)
			m_hInstance = new AppUtil();

		return m_hInstance;
	}
	public void setLineNumber(int nNum)
	{
		NUMBER_OF_LINE = nNum;
	}
	public int getLineNumber()
	{
		return NUMBER_OF_LINE;
	}
	// get intersect point between two lines
	public Point getLineIntersect(CLine line1, CLine line2) {
		double x1 = line1.x1;
		double y1 = line1.y1;
		double x2 = line1.x2;
		double y2 = line1.y2;
		double x3 = line2.x1;
		double y3 = line2.y1;
		double x4 = line2.x2;
		double y4 = line2.y2;
		
		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0.0) { // Lines are parallel.
			return null;
		}
		double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
		double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersection point.
			return new Point((x1 + ua * (x2 - x1)), (y1 + ua * (y2 - y1)));
		}

		return null;
	}

}
