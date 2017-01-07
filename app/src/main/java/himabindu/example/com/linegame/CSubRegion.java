package himabindu.example.com.linegame;

import java.util.ArrayList;

/**
 * Created by WORYA on 1/15/2016.
 */
public class CSubRegion {
    public String name = "";
    public int regionIndex;
    public int regionType;  // 1 :source, 2 :target
    public ArrayList<CVertex> vertexList = new ArrayList<CVertex>();
    public ArrayList<Integer> connectedRegion = new ArrayList<Integer>();
    public CSubRegion()
    {
        regionType = 0;
        regionIndex = 0;
    }
    public void ClearVertexList()
    {
        vertexList.clear();
    }
    public String GetRegionName(ArrayList<CSubRegion> lstRegs){
    	String retStr = new String("");
    	ArrayList<CSubRegion> lstCons = GetConnectedRegions(lstRegs);
    	for(int i = 0; i < lstCons.size(); i++){
    		if(retStr.contains(lstCons.get(i).name) != true) retStr += lstCons.get(i).name;
    	}
    	return retStr;
    }
    
    public ArrayList<CSubRegion> GetConnectedRegions(ArrayList<CSubRegion> lstRegs){
    	ArrayList<CSubRegion> res = new ArrayList<CSubRegion>();
    	for(int i = 0; i < lstRegs.size(); i++){
    		if(lstRegs.get(i).regionType == regionType) res.add(lstRegs.get(i));
    	}
    	return res;
    }
    
    private int FindSplitLineOne(CSubRegion targetReg){
    	int nRet = 0;
    	for(int i = 0; i < vertexList.size() - 1; i++){
    		for(int j = 0; j < targetReg.vertexList.size() - 1; j++){
    			if((vertexList.get(i).x == targetReg.vertexList.get(j).x &&vertexList.get(i).y == targetReg.vertexList.get(j).y
    					&&vertexList.get(i+1).x == targetReg.vertexList.get(j+1).x &&vertexList.get(i+1).y == targetReg.vertexList.get(j+1).y) ||
    					(vertexList.get(i+1).x == targetReg.vertexList.get(j).x &&vertexList.get(i+1).y == targetReg.vertexList.get(j).y
    					&&vertexList.get(i).x == targetReg.vertexList.get(j+1).x &&vertexList.get(i).y == targetReg.vertexList.get(j+1).y)){
    			
    				if(((vertexList.get(i).l_num_1 == vertexList.get(i + 1).l_num_1) || (vertexList.get(i).l_num_1 == vertexList.get(i + 1).l_num_2)) &&
    					(targetReg.vertexList.get(j).l_num_1 == vertexList.get(i).l_num_1 || targetReg.vertexList.get(j).l_num_2 == vertexList.get(i).l_num_1)&&
    					(targetReg.vertexList.get(j + 1).l_num_1 == vertexList.get(i).l_num_1 || targetReg.vertexList.get(j + 1).l_num_2 == vertexList.get(i).l_num_1)&&vertexList.get(i).l_num_1 > 3){
	    				nRet = vertexList.get(i).l_num_1;
	    				return nRet;
	    			}
    				if(((vertexList.get(i).l_num_2 == vertexList.get(i + 1).l_num_1) || (vertexList.get(i).l_num_2 == vertexList.get(i + 1).l_num_2)) &&
        					(targetReg.vertexList.get(j).l_num_1 == vertexList.get(i).l_num_2 || targetReg.vertexList.get(j).l_num_2 == vertexList.get(i).l_num_2)&&
        					(targetReg.vertexList.get(j + 1).l_num_1 == vertexList.get(i).l_num_2 || targetReg.vertexList.get(j + 1).l_num_2 == vertexList.get(i).l_num_2)&&vertexList.get(i).l_num_2 > 3){
    	    				nRet = vertexList.get(i).l_num_2;
    	    				return nRet;
    				}
    	    			
    			}
    		}
    	}
    	return nRet;
    }
    public int FindSplitLine(CSubRegion targetReg, ArrayList<CSubRegion> lstRegs){
    	int nRet = 0;
    	ArrayList<CSubRegion> lstCons = GetConnectedRegions(lstRegs);
    	ArrayList<CSubRegion> lstTargetCons = targetReg.GetConnectedRegions(lstRegs);
    	
    	for(int i = 0; i < lstCons.size(); i++){
    		for(int j = 0; j < lstTargetCons.size(); j++){
    			nRet = lstCons.get(i).FindSplitLineOne(lstTargetCons.get(j));
    			if(nRet > 0) break;
    		}
    	}
    	
    	return nRet;
    }
}
