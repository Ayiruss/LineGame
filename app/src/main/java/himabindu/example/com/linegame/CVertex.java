package himabindu.example.com.linegame;

/**
 * Created by WORYA on 1/15/2016.
 */
public class CVertex {
    public float x;
    public float y;
    public int l_num_1;
    public int l_num_2;
    
    public CVertex prevVertex;
    public CVertex()
    {
        x = y = 0;
        l_num_1 = l_num_2 = 0;
        prevVertex = null;
    }
}
