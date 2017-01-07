package himabindu.example.com.linegame;

/**
 * Created by WORYA on 1/15/2016.
 */
public class CLine {
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public boolean touched;
    public int nLineNumber;
    
    public CVertex realVert1;
    public CVertex realVert2;
    public CLine()
    {
        x1 = y1 = 0;
        x2 = y2 = 0;
        touched = false;
        nLineNumber = 0;
        
        realVert1 = null;
        realVert2 = null;
    }
}
