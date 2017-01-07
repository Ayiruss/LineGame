package himabindu.example.com.linegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Path;

import android.opengl.GLSurfaceView;


import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import utils.AppUtil;
import utils.DBManager;


class MyGLSurfaceView extends GLSurfaceView {
	
	public boolean bIsTracked = false;
	public boolean bIsHang = false;
	public int nCurTrackPos = -1;
    private final int RANGE = 3;
    public int nPowerLevel = 0;
    public int nStorageSpace = 0;
    int mMargin = 15;
    int mRectLeft;
    int mRectRight;
    int mRectTop;
    int mRectBottom;
    int mLineNumber;
    int mSelectedLineNumber = -1;
    boolean isTouchedDown = false;
    boolean isFoundLine1 = false;
    boolean isFoundLine2 = false;
    CVertex pubV1 = new CVertex();
    CVertex pubV2 = new CVertex(); 
    int mNumberOfLines = 0;
    int mNumberOfMoves = 0;
    float mZoomVal = 1.0f;
    float xPrev;
    float yPrev;

    
    boolean isGameEnd = false;
    private ArrayList<CLine> vLineList = new ArrayList<CLine>();
    private ArrayList<CLine> hLineList = new ArrayList<CLine>();
    private ArrayList<CVertex> vertexList = new ArrayList<CVertex>();
    private ArrayList<CSubRegion> regionList = new ArrayList<CSubRegion>();

    float xStartPos = 0.0f;
    float yStartPos = 0.0f;
    // array list for undo operation
    class OneState{
    	public ArrayList<CLine> vPrevLineList = new ArrayList<CLine>();
    	public ArrayList<CLine> hPrevLineList = new ArrayList<CLine>();
    	public ArrayList<CSubRegion> prevRegionList = new ArrayList<CSubRegion>();
    	public int nLineNumebrs = 0;
    }
    

    private ArrayList<OneState> lstHistState = new ArrayList<OneState>();
    
    private Point tleft = new Point();
    private Point tright = new Point();
    private Point bleft = new Point();
    private Point bright = new Point();
    private Context mContext;
    private final MyGLRenderer mRenderer;
    DBManager dbManager;
    public MyGLSurfaceView(Context context){
        super(context);
        mContext = context;
        setEGLContextClientVersion(2);
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);
        initRect();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	if(bIsTracked) drawTrack(canvas);
    	else drawBackRect(canvas);
    }

    private void drawTrack(Canvas canvas){

        Paint backFill = new Paint();
        Paint paintBorderPoly = new Paint();
        Paint mainboardFill = new Paint();

        backFill.setColor(Color.DKGRAY);
        backFill.setStyle(Paint.Style.FILL);
        paintBorderPoly.setColor(Color.GRAY);
        paintBorderPoly.setStyle(Paint.Style.STROKE);
        paintBorderPoly.setStrokeWidth(4);
        mainboardFill.setColor(Color.WHITE);
        mainboardFill.setStyle(Paint.Style.FILL);

            
        // draw border with black color
        Paint backPaint = new Paint();
        backPaint.setColor(Color.BLACK);
        backPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(tleft.x, tleft.y, tright.x, tright.y, backPaint);
        canvas.drawLine(tleft.x, tleft.y, bleft.x, bleft.y, backPaint);
        canvas.drawLine(bright.x, bright.y, tright.x, tright.y, backPaint);
        canvas.drawLine(bleft.x, bleft.y, bright.x, bright.y, backPaint);
        
        // draw region
        
        OneState curStateSave = new OneState();
        curStateSave.hPrevLineList = hLineList;
        curStateSave.nLineNumebrs = mNumberOfLines;
        curStateSave.vPrevLineList = vLineList;
        curStateSave.prevRegionList = regionList;
        
        if(nCurTrackPos >= 0 && nCurTrackPos < lstHistState.size()){
        	curStateSave = lstHistState.get(nCurTrackPos);
        }else nCurTrackPos = lstHistState.size();
        
        
        
        OneState curState = new OneState();
        // temp save
        for (int i = 0;i<curStateSave.prevRegionList.size();i++)
        {
            CSubRegion saveRegion = new CSubRegion();
            CSubRegion mSubRegion = curStateSave.prevRegionList.get(i);
            saveRegion.regionType = mSubRegion.regionType;
            saveRegion.regionIndex = mSubRegion.regionIndex;
            saveRegion.name = mSubRegion.name;
            saveRegion.vertexList = mSubRegion.vertexList;
            ArrayList<Integer> newList = new ArrayList<Integer>(mSubRegion.connectedRegion);
            saveRegion.connectedRegion = newList;
            curState.prevRegionList.add(saveRegion);
        }
        for (int i = 0; i<curStateSave.vPrevLineList.size();i++)
        {
            CLine saveLine = new CLine();
            CLine temp =curStateSave.vPrevLineList.get(i);
            saveLine.x1 = temp.x1;
            saveLine.y1 = temp.y1;
            saveLine.x2 = temp.x2;
            saveLine.y2 = temp.y2;
            saveLine.touched = temp.touched;
            saveLine.nLineNumber = temp.nLineNumber;
            curState.vPrevLineList.add(saveLine);
        }
        for (int i = 0;i<curStateSave.hPrevLineList.size();i++)
        {
            CLine saveLine = new CLine();
            CLine temp = curStateSave.hPrevLineList.get(i);
            saveLine.x1 = temp.x1;
            saveLine.y1 = temp.y1;
            saveLine.x2 = temp.x2;
            saveLine.y2 = temp.y2;
            saveLine.touched = temp.touched;
            saveLine.nLineNumber = temp.nLineNumber;
            curState.hPrevLineList.add(saveLine);
        }
        
        ArrayList<CSubRegion> curRegList = curState.prevRegionList;
        curState.nLineNumebrs = mNumberOfLines;
        // merge
        ArrayList<CSubRegion> tmpRegList = new ArrayList<CSubRegion>();
        for(int i = 0; i < curState.prevRegionList.size(); i++){
        	//found
        	boolean bIsFounded = false;
        	for(int j = 0; j < tmpRegList.size(); j++){
        		if(tmpRegList.get(j).GetRegionName(curRegList).contains(curState.prevRegionList.get(i).name) == true){
        			bIsFounded = true;
        			break;
        		}
        	}
        	
        	if(!bIsFounded) tmpRegList.add(curState.prevRegionList.get(i));
        }
        
        curState.prevRegionList = tmpRegList;
        
        // draw
        int nCols = 1;
        for(int i = 0; i < curState.hPrevLineList.size(); i++){
        	if(!curState.hPrevLineList.get(i).touched) nCols++;
        }
        int nRows = curState.prevRegionList.size() / nCols + 1;
        
        for(int a1 = 0; a1 < curState.prevRegionList.size() - 1; a1++){
        	for(int a2 = a1 + 1; a2 < curState.prevRegionList.size(); a2++){
        		int nSplitPos = curState.prevRegionList.get(a1).FindSplitLine(curState.prevRegionList.get(a2), curRegList);
        		if(nSplitPos > 0){
        			Paint itemCircle = new Paint();
                	itemCircle.setColor(Color.BLACK);
                	itemCircle.setStyle(Paint.Style.FILL);
                	
                	canvas.drawLine((mRectRight-mRectLeft) * (a1 % nCols + 1) / (nCols + 1) + mMargin / 2, 
                			(mRectBottom - mRectTop) * (a1 / nCols + 1) / (nRows + 1) + mMargin + 20,
                			(mRectRight-mRectLeft) * (a2 % nCols + 1) / (nCols + 1) + mMargin / 2, 
                			(mRectBottom - mRectTop) * (a2 / nCols + 1) / (nRows + 1) + mMargin + 20,
                			itemCircle);
                	
                	itemCircle.setTextSize((mRectRight-mRectLeft) / (nCols + 1) / 8);
                	
                	canvas.drawText("" + nSplitPos,((mRectRight-mRectLeft) * (a1 % nCols + 1) * 2 / (nCols + 1) + (mRectRight-mRectLeft) * (a2 % nCols + 1) / (nCols + 1))/3 + mMargin / 2, 
                			(((mRectBottom - mRectTop) * (a1 / nCols + 1) / (nRows + 1) + mMargin + 20) * 2 + ((mRectBottom - mRectTop) * (a2 / nCols + 1) / (nRows + 1) + mMargin + 20))/3, itemCircle);
        		}
        	}
        }
        
        for(int i = 0; i < curState.prevRegionList.size(); i++){
        	CSubRegion reg = curState.prevRegionList.get(i);
        	Paint itemCircle = new Paint();
        	if(reg.regionType == 1) itemCircle.setColor(AppUtil.sharedObject().COLOR_ARRAY[AppUtil.sharedObject().SRC_COLOR]);
        	else if(reg.regionType == 2) itemCircle.setColor(AppUtil.sharedObject().COLOR_ARRAY[AppUtil.sharedObject().TARGET_COLOR]);
        	else itemCircle.setColor(Color.YELLOW);
        	itemCircle.setStyle(Paint.Style.FILL);
        	
        	canvas.drawCircle((mRectRight-mRectLeft) * (i % nCols + 1) / (nCols + 1) + mMargin / 2, 
        			(mRectBottom - mRectTop) * (i / nCols + 1) / (nRows + 1) + mMargin + 20,
        			(mRectRight - mRectLeft) / (nCols + 1) / 4, itemCircle);
        	
        	
        	itemCircle.setColor(Color.BLACK);
        	itemCircle.setTextSize((mRectRight-mRectLeft) / (nCols + 1) / 8);
        	
        	canvas.drawText(reg.GetRegionName(curRegList),(mRectRight-mRectLeft) * (i % nCols + 1) / (nCols + 1) - (mRectRight-mRectLeft) / (nCols + 1) / 16 * (reg.name.length() + 1) + mMargin / 2, 
        			(mRectBottom - mRectTop) * (i / nCols + 1) / (nRows + 1) + mMargin + 20, itemCircle);
        	
        }

        // header
        Path headerPath = new Path();
        headerPath.moveTo(0, 0);
        headerPath.lineTo(mRectRight + mMargin, 0);
        headerPath.lineTo(mRectRight + mMargin, mRectTop);
        headerPath.lineTo(0, mRectTop);
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.CYAN);
        headerPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawPath(headerPath, headerPaint);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize((mRectRight-mRectLeft) / 20);
        canvas.drawText("<<", mMargin, mMargin + 15, paint);
        canvas.drawText("Number:" + (mLineNumber), mMargin + 30, mMargin + 15, paint);
        canvas.drawText("Score:" + mNumberOfLines, (mRectRight-mRectLeft)/4 + 45,mMargin+15, paint);
        canvas.drawText("No.Moves:" + mNumberOfMoves, (mRectRight-mRectLeft) / 2 + 30,mMargin+15, paint);
        canvas.drawText(">>", mRectRight - mMargin, mMargin+15, paint);
        
        // background
        backFill.setColor(Color.BLACK);
        Path backPath = new Path();
        backPath.moveTo(0, mRectTop);
        backPath.lineTo(mMargin, mRectTop);
        backPath.lineTo(mMargin, mRectBottom);
        backPath.lineTo(0, mRectBottom);
        
        canvas.drawPath(backPath, backFill);
        
        backPath = new Path();
        backPath.moveTo(mRectRight, mRectTop);
        backPath.lineTo(mMargin + mRectRight, mRectTop);
        backPath.lineTo(mMargin + mRectRight, mRectBottom);
        backPath.lineTo(mRectRight, mRectBottom);
        
        canvas.drawPath(backPath, backFill);
        
        backPath = new Path();
        backPath.moveTo(0, mRectBottom);
        backPath.lineTo(mMargin + mRectRight, mRectBottom);
        backPath.lineTo(mMargin + mRectRight, mRectBottom + mMargin * 2);
        backPath.lineTo(0, mRectBottom + mMargin * 2);
        
        canvas.drawPath(backPath, backFill);
    }
    
    
    private float getXZoom(float xPos){
    	return (xPos - (mRectLeft + mMargin)) * mZoomVal + mRectLeft + mMargin - xStartPos;
    }
    
    private float getYZoom(float yPos){
    	return (yPos - mRectTop) * mZoomVal + mRectTop - yStartPos;
    }
    
    private void drawBackRect(Canvas canvas)
    {
    	if(xStartPos < 0) xStartPos = 0;
    	if(yStartPos < 0) yStartPos = 0;
    	if(xStartPos > (mRectRight - mRectLeft - mMargin) * (mZoomVal - 1)) xStartPos = (mRectRight - mRectLeft - mMargin) * (mZoomVal - 1);
    	if(yStartPos > (mRectBottom - mRectTop) * (mZoomVal - 1)) yStartPos = (mRectBottom - mRectTop) * (mZoomVal - 1);
    	
    	
        // fill background
        Paint backFill = new Paint();
        Paint paintBorderPoly = new Paint();
        Paint mainboardFill = new Paint();

        backFill.setColor(Color.DKGRAY);
        backFill.setStyle(Paint.Style.FILL);
        paintBorderPoly.setColor(Color.GRAY);
        paintBorderPoly.setStyle(Paint.Style.STROKE);
        paintBorderPoly.setStrokeWidth(4);
        mainboardFill.setColor(Color.WHITE);
        mainboardFill.setStyle(Paint.Style.FILL);

        // float 

        this.mRectLeft=0;
     
        // draw border with black color
        Paint backPaint = new Paint();
        backPaint.setColor(Color.BLACK);
        backPaint.setStyle(Paint.Style.STROKE);
           
        int nRegionSize = regionList.size();
        if(nRegionSize>0)
        {
            for(int i = 0;i<nRegionSize;i++)
            {
                CSubRegion subDrawRegion = regionList.get(i);
                Paint paintFillPoly = new Paint();
                if(subDrawRegion.regionType == 1) {
                    paintFillPoly.setColor(AppUtil.sharedObject().COLOR_ARRAY[AppUtil.sharedObject().SRC_COLOR]);
                    paintFillPoly.setStyle(Paint.Style.FILL);
                }
                else if(subDrawRegion.regionType == 2)
                {
                    paintFillPoly.setColor(AppUtil.sharedObject().COLOR_ARRAY[AppUtil.sharedObject().TARGET_COLOR]);
                    paintFillPoly.setStyle(Paint.Style.FILL);
                }
                else {
                	paintFillPoly.setColor(0xFFFFFFFF);
                    paintFillPoly.setStyle(Paint.Style.FILL);
                }

                Path drawPath = new Path();
                CVertex firstVertex = subDrawRegion.vertexList.get(0);
                
                
                drawPath.moveTo(getXZoom(firstVertex.x), getYZoom(firstVertex.y));
                for (int k = 0;k<subDrawRegion.vertexList.size();k++)
                {
                    CVertex drawVertex = subDrawRegion.vertexList.get(k);
                    drawPath.lineTo(getXZoom(drawVertex.x), getYZoom(drawVertex.y));
                }
                
                canvas.drawPath(drawPath, paintFillPoly);
            }
        }
        
        // draw line
        backPaint.setStrokeWidth(4);
        Paint dottedPaint = new Paint();
        dottedPaint.setColor(Color.DKGRAY);
        dottedPaint.setStyle(Paint.Style.STROKE);
        dottedPaint.setStrokeWidth(4);
        dottedPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 5));
        dottedPaint.setAlpha(255);
        for(int j = 0;j<hLineList.size();j++) {
            CLine tItem = hLineList.get(j);
            Path tPath = new Path();
            tPath.moveTo(getXZoom(tItem.x1), getYZoom(tItem.y1));
            tPath.lineTo(getXZoom(tItem.x2), getYZoom(tItem.y2));
            
           
            if(tItem.touched)
                canvas.drawPath(tPath, paintBorderPoly);
            else
                canvas.drawPath(tPath, backPaint);
            if(isTouchedDown)
                if(mSelectedLineNumber == tItem.nLineNumber)
                    canvas.drawLine(getXZoom(tItem.x1), getYZoom(tItem.y1),getXZoom(tItem.x2), getYZoom(tItem.y2), dottedPaint);

        }
        for(int j = 0;j<vLineList.size();j++) {
            CLine tItem = vLineList.get(j);
            Path tPath = new Path();
            tPath.moveTo(getXZoom(tItem.x1), getYZoom(tItem.y1));
            tPath.lineTo(getXZoom(tItem.x2), getYZoom(tItem.y2));
            if(tItem.touched)
                canvas.drawPath(tPath, paintBorderPoly);
            else
                canvas.drawPath(tPath, backPaint);
            if(isTouchedDown)
                if(mSelectedLineNumber == tItem.nLineNumber)
                	canvas.drawLine(getXZoom(tItem.x1), getYZoom(tItem.y1),getXZoom(tItem.x2), getYZoom(tItem.y2), dottedPaint);
        }

        
        // header
        Path headerPath = new Path();
        headerPath.moveTo(0, 0);
        headerPath.lineTo(mRectRight + mMargin, 0);
        headerPath.lineTo(mRectRight + mMargin, mRectTop);
        headerPath.lineTo(0, mRectTop);
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.CYAN);
        headerPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawPath(headerPath, headerPaint);

        
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize((mRectRight-mRectLeft) / 20);
        
             
        canvas.drawText("-", mMargin, mMargin + 15, paint);
        canvas.drawText("Number:" + (mLineNumber), mMargin + 30, mMargin + 15, paint);
        canvas.drawText("Score:" + mNumberOfLines, (mRectRight-mRectLeft)/4 + 45,mMargin+15, paint);
        canvas.drawText("No.Moves:" + mNumberOfMoves, (mRectRight-mRectLeft) / 2 + 30,mMargin+15, paint);
        canvas.drawText("+", mRectRight - mMargin, mMargin+15, paint);
        
        // background
        backFill.setColor(Color.BLACK);
        Path backPath = new Path();
        backPath.moveTo(0, mRectTop);
        backPath.lineTo(mMargin, mRectTop);
        backPath.lineTo(mMargin, mRectBottom);
        backPath.lineTo(0, mRectBottom);
        
        canvas.drawPath(backPath, backFill);
        
        backPath = new Path();
        backPath.moveTo(mRectRight, mRectTop);
        backPath.lineTo(mMargin + mRectRight, mRectTop);
        backPath.lineTo(mMargin + mRectRight, mRectBottom);
        backPath.lineTo(mRectRight, mRectBottom);
        
        canvas.drawPath(backPath, backFill);
        
        backPath = new Path();
        backPath.moveTo(0, mRectBottom);
        backPath.lineTo(mMargin + mRectRight, mRectBottom);
        backPath.lineTo(mMargin + mRectRight, mRectBottom + mMargin * 2);
        backPath.lineTo(0, mRectBottom + mMargin * 2);
        
        canvas.drawPath(backPath, backFill);
    }

    private void RefreshName(){
    	String strName;
    	int nCount = 0;
        for (int j = 0;j<regionList.size();j++) {
        	CSubRegion tempRegion = regionList.get(j);
        	if(tempRegion.regionType > 0) {
        		nCount = (tempRegion.regionType - 1);
        	}
        	else nCount = (-tempRegion.regionType + 2);
        	strName = String.format("A%d", nCount);
            tempRegion.name = strName;
        }
    }
    
    @Override
    public boolean onTouchEvent( MotionEvent e) {
   
        float x = e.getX();
        float y = e.getY();

        
        if(bIsTracked == true) {
        	if(e.getAction() == MotionEvent.ACTION_UP){
        		if(x > 0 && y > 0 && y < mRectTop && x < mRectLeft + 2 * mMargin){
        			nCurTrackPos--; invalidate();
        			((OpenGLES20Activity)mContext).SoundTouch();
        		}else if(x > mRectRight - mMargin && y > 0 && x < mRectRight + mMargin && y < mRectTop){
        			nCurTrackPos++; invalidate();
        			((OpenGLES20Activity)mContext).SoundTouch();
        		}
        	}
        	return true;
        }
        
        else{
        	if(e.getAction() == MotionEvent.ACTION_UP){
        		if(x > 0 && y > 0 && y < mRectTop && x < mRectLeft + 2 * mMargin){
        			if(mZoomVal > 1.0f) mZoomVal = mZoomVal / 2;
        			invalidate();
        			((OpenGLES20Activity)mContext).SoundTouch();
        			return true;
        		}else if(x > mRectRight - mMargin && y > 0 && x < mRectRight + mMargin && y < mRectTop){
        			if(mZoomVal < 8.0f) mZoomVal = mZoomVal * 2;
        			invalidate();
        			((OpenGLES20Activity)mContext).SoundTouch();
        			return true;
        		}
        	}
        	
        	
	        switch (e.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                isTouchedDown = true;
	                mSelectedLineNumber = getSelectedLineNumber(x, y);
	                if(mSelectedLineNumber == -1){
		                xPrev = x;
		                yPrev = y;
	                }else{
	                	xPrev = -1.0f; yPrev = -1.0f;
	                }
	                invalidate();
	                break;
	                
	            case MotionEvent.ACTION_MOVE:
	            	if(xPrev != -1.0f && yPrev != -1.0f){
		            	xStartPos = xStartPos + xPrev - x;
		            	yStartPos = yStartPos + yPrev - y;
		            	xPrev = x;
			            yPrev = y;
			            invalidate();
	            	}
	            	break;
	            case MotionEvent.ACTION_UP:
	            	if(mSelectedLineNumber >= 0) ((OpenGLES20Activity)mContext).SoundTouch();
	            	
	            	isTouchedDown = false;
	                updateGameScreen();
	                invalidate();
	                break;
	        }
        }
        return true;
    }
    private void initRect()
    {
        // get number of line
        mLineNumber = AppUtil.sharedObject().getLineNumber();
        bIsHang =  true;
        
        // initialize coordinate
        mRectLeft = mMargin;
        mRectRight = AppUtil.sharedObject().getScreenWidth() - mMargin;
        mRectTop = mMargin+30;
        mRectBottom = AppUtil.sharedObject().getScreenHeight() - mMargin- 5 * mMargin;
        tleft.x = bleft.x = mRectLeft;
        tleft.y = tright.y = mRectTop;
        tright.x =bright.x =  mRectRight;
        bleft.y = bright.y = mRectBottom;
        
        while(true){
        	vertexList.clear(); vLineList.clear();hLineList.clear(); regionList.clear();
        	
	        // add corner vertex
	        CVertex vertex1 = new CVertex();
	        vertex1.x = mRectLeft;
	        vertex1.y = mRectTop;
	        vertex1.l_num_1 = 0;
	        vertex1.l_num_2 = 2;
	        vertexList.add(vertex1);
	        CVertex vertex2 = new CVertex();
	        vertex2.x = mRectLeft;
	        vertex2.y = mRectBottom;
	        vertex2.l_num_1 = 0;
	        vertex2.l_num_2 = 3;
	        vertexList.add(vertex2);
	        CVertex vertex3 = new CVertex();
	        vertex3.x = mRectRight;
	        vertex3.y = mRectTop;
	        vertex3.l_num_1 = 1;
	        vertex3.l_num_2 = 2;
	        vertexList.add(vertex3);
	        CVertex vertex4 = new CVertex();
	        vertex4.x = mRectRight;
	        vertex4.y = mRectBottom;
	        vertex4.l_num_1 = 1;
	        vertex4.l_num_2 = 3;
	        vertexList.add(vertex4);
	        // generate random lines
	        generateRandomLine();
	        // get all vertex
	        getAllVertex();
	        boolean bHasEqual = false;
	        for(int i = 0; i < vertexList.size() - 1; i++){
	        	if(bHasEqual) break;
	        	for(int j = i + 1; j < vertexList.size(); j++){
	        		if(compareVertex(vertexList.get(i), vertexList.get(j))) {
	        			bHasEqual = true;
	        		}
	        	}
	        }
	        if(bHasEqual) {
	        	continue;
	        }
	        
	        // generate region
	        generateRegion();
	        
	        for(int i = 0; i < regionList.size(); i++){
	        	if(regionList.get(i).regionType == 0) regionList.get(i).regionType = -i;
	        }
	        dbManager = new DBManager(mContext);
	        mNumberOfMoves = 0;
	        mNumberOfLines = 0;
	        isGameEnd = false;
	        
	        mergeRegion();
	        
	        // check has type
	        boolean bHasStart = false;
	        boolean bHasEnd = false;
	        
	        for(int i = 0; i < regionList.size(); i++){
	        	CSubRegion reg = regionList.get(i);
	        	if(reg.regionType == 1) bHasStart  = true;
	        	if(reg.regionType == 2) bHasEnd = true;
	        }
	        
	        if(bHasEnd && bHasStart){
	        	RefreshName();
	        	break;
	        }
        }
        
        for(int i = 0; i < vLineList.size(); i++){
        	CLine line = vLineList.get(i);
        	if(line.realVert1 != null){
        		line.x1 = line.realVert1.x; line.y1 = line.realVert1.y;
        		}
        	if(line.realVert2 != null){
        		line.x2 = line.realVert2.x; line.y2 = line.realVert2.y;
        		}
        }
        for(int i = 0; i < hLineList.size(); i++){
        	CLine line = hLineList.get(i);
        	if(line.realVert1 != null){line.x1 = line.realVert1.x; line.y1 = line.realVert1.y;}
        	if(line.realVert2 != null){line.x2 = line.realVert2.x; line.y2 = line.realVert2.y;}
        }
        
       
        invalidate();
    }
    
    private void mergeRegion()
    {
    		
	    	boolean bIsHori = true;
	    	for(int i = 0; i < vLineList.size(); i++){
	        	CLine line = vLineList.get(i);
	        	bIsHori = false;
	        	
	        	if(line.realVert1 != null){
	        		mSelectedLineNumber = line.nLineNumber;
	        		RemoveLine(line.y1, line.realVert1.y, bIsHori, true);
	        	}
	        	if(line.realVert2 != null){
	        		mSelectedLineNumber = line.nLineNumber;
	        		RemoveLine(line.realVert2.y, line.y2, bIsHori, true);
	        	}
	        	
	        }
	        for(int i = 0; i < hLineList.size(); i++){
	        	CLine line = hLineList.get(i);
	        	bIsHori = true;
	        	
	        	if(line.realVert1 != null){
	        		mSelectedLineNumber = line.nLineNumber;
	        		RemoveLine(line.x1, line.realVert1.x, bIsHori, true);
	        	}
	        	if(line.realVert2 != null){
	        		mSelectedLineNumber = line.nLineNumber;
	        		RemoveLine(line.realVert2.x, line.x2,bIsHori, true);
	        	}
	        }
	     
    }
    
    private int getEqualNumber(CVertex vert1, CVertex vert2){
    	int nRet = -1;
    	if(vert1.l_num_1 == vert2.l_num_1 || vert1.l_num_1 == vert2.l_num_2) nRet = vert1.l_num_1;
    	if(vert1.l_num_2 == vert2.l_num_1 || vert1.l_num_2 == vert2.l_num_2) nRet = vert1.l_num_2;
    	return nRet;
    }
    private boolean isContain(int noLine, CVertex vert1){
    	boolean bRet = false;
    	if(vert1.l_num_1 == noLine || vert1.l_num_2 == noLine) bRet = true;
    	return bRet;
    }
    
    private int getVVector(CVertex vert1, CVertex vert2, CVertex targVert){
    	int nRet = 0;
    	
    	float constVal = 0.0f;
    	if(vert1.x == vert2.x){ 
    		constVal = -vert1.x;
    	}
    	else if(vert1.y == vert2.y) {
    		constVal = -vert1.y;
    	}
    	else{
    		constVal += -vert1.x / (vert2.x - vert1.x);
    		constVal += vert1.y / (vert2.y - vert1.y);
    	}
    	
    	float varVal = 0.0f;
    	if(vert1.x == vert2.x){
    		varVal = targVert.x;
    	}
    	else if(vert1.y == vert2.y) 
    	{
    		varVal = targVert.y;
    	}
    	else{
    		varVal += targVert.x / (vert2.x - vert1.x);
    		varVal += -targVert.y / (vert2.y - vert1.y);
    	}
    	
    	if(varVal + constVal > 0) nRet = 1;
    	else if(varVal + constVal < 0) nRet = -1;
    	
    	return nRet;
    }
    
    private void refreshRegion(CSubRegion reg){
    	while(true){
    		if(compareVertex(reg.vertexList.get(0), reg.vertexList.get(reg.vertexList.size() - 1))) break;
    		
	    	CVertex vert1 = reg.vertexList.get(reg.vertexList.size() - 2);
	    	CVertex vert2 = reg.vertexList.get(reg.vertexList.size() - 1);
	    	ArrayList<CVertex> lstNeibourVertexs = getNeibourVertex(vert2);
	    	
	    	int noLine = getEqualNumber(vert1, vert2);
	    	
	    	// get the nearest 
	    	for(int i = 0; i < lstNeibourVertexs.size(); i++){
	    		CVertex curVert = lstNeibourVertexs.get(i);
	    		if(isContain(noLine, curVert)) continue;
	    		
	    		if(getVVector(vert1, vert2, reg.vertexList.get(0)) == getVVector(vert1, vert2, lstNeibourVertexs.get(i))){
	    			reg.vertexList.add(lstNeibourVertexs.get(i));
	    			break;
	    		}
	    	}
	   }
    }
    private ArrayList<CSubRegion> getRegionsForLine(CVertex vert1, CVertex vert2){
    	ArrayList<CSubRegion> lstRetRegions = new ArrayList<CSubRegion>();
    	ArrayList<CVertex> lstNeibourVertexs = getNeibourVertex(vert2);
    	
    	int noLine = getEqualNumber(vert1, vert2);
    	for(int i = 0; i < lstNeibourVertexs.size(); i++){
    		CVertex curVert = lstNeibourVertexs.get(i);
    		if(isContain(noLine, curVert)) continue;
    		
    		CSubRegion newReg = new CSubRegion();
    		newReg.vertexList.add(vert1);
    		newReg.vertexList.add(vert2);
    		newReg.vertexList.add(curVert);
    		
    		refreshRegion(newReg);
    		lstRetRegions.add(newReg);
    	}
    	return lstRetRegions;
    }
    
    private ArrayList<CSubRegion> getRegionsForVertex(CVertex vertex){
    	ArrayList<CSubRegion> lstRetRegs = new ArrayList<CSubRegion>();
    	ArrayList<CVertex> lstNeibourVertexs = getNeibourVertex(vertex);
    	
    	for(int i = 0; i < lstNeibourVertexs.size(); i++){
    		lstRetRegs.addAll(getRegionsForLine(vertex, lstNeibourVertexs.get(i)));
    	}
    	
    	return lstRetRegs;
    }
    
    private void generateRegion(){
    	for(int i = 0; i < vertexList.size(); i++){
    		CVertex curVert = vertexList.get(i);
    		ArrayList<CSubRegion> lstRegsForVertext = getRegionsForVertex(curVert);
    		for(int j = 0; j < lstRegsForVertext.size(); j++){
    			CSubRegion neibourRegion = lstRegsForVertext.get(j);
                if(regionList.size() == 0)
                    regionList.add(neibourRegion);
                else {
                    boolean bExist = false;
                    for (int k = 0; k < regionList.size(); k++) {
                        CSubRegion regionItem = regionList.get(k);
                        if (compareRegion(neibourRegion, regionItem)) {
                            bExist = true;
                            break;
                        }
                    }
                    if(!bExist)
                        regionList.add(neibourRegion);
                }
    		}
    	}
    	
    	// initialize region
        String strName;
        for (int j = 0;j<regionList.size();j++) {
            strName = String.format("%c", 'A'+j);
            CSubRegion tempRegion = regionList.get(j);
            tempRegion.name = strName;
            tempRegion.regionIndex = j;
            for (int k = 0;k<tempRegion.vertexList.size();k++)
            {
                if(tempRegion.vertexList.get(k).l_num_1 == 1 && tempRegion.vertexList.get(k).l_num_2 == 3)
                {
                    tempRegion.regionType = 2;
                    break;
                }
            }
        }
        regionList.get(0).regionType = 1;
    }
    private void getAllVertex(){
    	for(int i = 0; i < vLineList.size(); i++){
    		CLine vItem = vLineList.get(i);
            if(vItem.y1 == mRectTop){
            	 CVertex vertex = new CVertex();
                 vertex.x = vItem.x1;
                 vertex.y = vItem.y1;
                 vertex.l_num_1 = vItem.nLineNumber;
                 vertex.l_num_2 = 2;
                 vertexList.add(vertex);
            }
            if(vItem.y2 == mRectBottom){
           	 	CVertex vertex = new CVertex();
                vertex.x = vItem.x2;
                vertex.y = vItem.y2;
                vertex.l_num_1 = vItem.nLineNumber;
                vertex.l_num_2 = 3;
                vertexList.add(vertex);
           }
           
           for(int j = i + 1; j < vLineList.size(); j++){
        	   CLine tItem = vLineList.get(j);
        	   Point pt = AppUtil.sharedObject().getLineIntersect(vItem, tItem);
               if(pt != null)
               {
                   CVertex vertex = new CVertex();
                   vertex.x = pt.x;
                   vertex.y = pt.y;
                   vertex.l_num_1 = vItem.nLineNumber;
                   vertex.l_num_2 = tItem.nLineNumber;
                   vertexList.add(vertex);
               }
           }
           
           for(int j = 0; j < hLineList.size(); j++){
        	   CLine tItem = hLineList.get(j);
        	   Point pt = AppUtil.sharedObject().getLineIntersect(vItem, tItem);
               if(pt != null)
               {
                   CVertex vertex = new CVertex();
                   vertex.x = pt.x;
                   vertex.y = pt.y;
                   vertex.l_num_1 = vItem.nLineNumber;
                   vertex.l_num_2 = tItem.nLineNumber;
                   vertexList.add(vertex);
               }
           }
    	}
    	
    	 for (int i = 0;i<hLineList.size();i++)
         {
             CLine hItem = hLineList.get(i);
             if(hItem.x1 == mRectLeft){
            	    CVertex vertex = new CVertex();
                 vertex.x = hItem.x1;
                 vertex.y = hItem.y1;
                 vertex.l_num_2 = hItem.nLineNumber;
                 vertex.l_num_1 = 0;
                 vertexList.add(vertex);
            }
            if(hItem.x2 == mRectRight){
           	   CVertex vertex = new CVertex();
                vertex.x = hItem.x2;
                vertex.y = hItem.y2;
                vertex.l_num_2 = hItem.nLineNumber;
                vertex.l_num_1 = 1;
                vertexList.add(vertex);
           }
            
            for(int j = i + 1; j < hLineList.size(); j++){
         	   CLine tItem = hLineList.get(j);
         	   Point pt = AppUtil.sharedObject().getLineIntersect(hItem, tItem);
                if(pt != null)
                {
                    CVertex vertex = new CVertex();
                    vertex.x = pt.x;
                    vertex.y = pt.y;
                    vertex.l_num_1 = hItem.nLineNumber;
                    vertex.l_num_2 = tItem.nLineNumber;
                    vertexList.add(vertex);
                }
            }
         }
    }
  
    private void generateRandomLine()
    {
       Random random = new Random();
       
        // horizontal part
        for (int i = 0;i<mLineNumber/2;i++)
        {
            CLine item = new CLine();
            item.x1 = mRectLeft;
            item.y1 = random.nextInt(mRectBottom-mRectTop-80) + mRectTop+40;
            item.nLineNumber = i + (mLineNumber + 1)/2 + 4;
       
            item.y2 = random.nextInt(mRectBottom-mRectTop-80) + mRectTop+40;
            item.x2 = mRectRight;
          
            if(bIsHang && random.nextInt(6) < 3){

            	if(random.nextInt(17) < 10){
            		item.realVert1 = new CVertex();
                	item.realVert1.l_num_2 = item.nLineNumber;
                	
                	int nRaito = random.nextInt(4) + 1;
                	item.realVert1.x = item.x1 + (item.x2 - item.x1) * nRaito / 10;
                	item.realVert1.y = item.y1 + (item.y2 - item.y1) * nRaito / 10;
            	}
            	
            	if(random.nextInt(17) < 10){
	            	item.realVert2 = new CVertex();
	            	item.realVert2.l_num_2 = item.nLineNumber;
	            	
	            	int nRaito = random.nextInt(4) + 6;
	            	if(item.realVert1 == null){
	            		nRaito = random.nextInt(4) + 2;
	            	}
	            	item.realVert2.x = item.x1 + (item.x2 - item.x1) * nRaito / 10;
	            	item.realVert2.y = item.y1 + (item.y2 - item.y1) * nRaito / 10;
            	}
            }
            
            hLineList.add(item);
        }
        // vertical part
        for (int i = 0;i<(mLineNumber + 1) / 2;i++)
        {
            CLine item = new CLine();
            item.x1 = random.nextInt(mRectRight-mRectLeft - 80) + mRectLeft + 40;
            item.y1 = mRectTop;
            item.nLineNumber = i+4;
           
            item.x2 = random.nextInt(mRectRight-mRectLeft-80) + mRectLeft + 40;
            item.y2 = mRectBottom;
            
            if(bIsHang && random.nextInt(6) < 3){
            	if(random.nextInt(17) < 10){
            		item.realVert1 = new CVertex();
                	item.realVert1.l_num_1 = item.nLineNumber;
                	
                	int nRaito = random.nextInt(4) + 1;
                	item.realVert1.x = item.x1 + (item.x2 - item.x1) * nRaito / 10;
                	item.realVert1.y = item.y1 + (item.y2 - item.y1) * nRaito / 10;
            	}
            	
            	if(random.nextInt(17) < 10){
	            	item.realVert2 = new CVertex();
	            	item.realVert2.l_num_1 = item.nLineNumber;
	            	
	            	int nRaito = random.nextInt(4) + 6;
	            	if(item.realVert1 == null){
	            		nRaito = random.nextInt(4) + 2;
	            	}
	            	item.realVert2.x = item.x1 + (item.x2 - item.x1) * nRaito / 10;
	            	item.realVert2.y = item.y1 + (item.y2 - item.y1) * nRaito / 10;
            	}
            }
            vLineList.add(item);
        }
    }
    boolean compareVertex(CVertex comp1, CVertex comp2)
    {
        boolean isEqual = false;
        if(comp1.x == comp2.x && comp1.y == comp2.y){
            isEqual = true;
        }
        return isEqual;
    }
    boolean compareRegion(CSubRegion comp1, CSubRegion comp2)
    {
        int comp1_cnt = comp1.vertexList.size();
        int comp2_cnt = comp2.vertexList.size();
        boolean isEqual = false;
        if(comp1_cnt != comp2_cnt)
            return  false;
        for(int i = 0;i<comp1_cnt;i++) {
            CVertex c1 = comp1.vertexList.get(i);
            isEqual = false;
            for(int j = 0;j<comp2_cnt;j++)
            {
                CVertex c2 = comp2.vertexList.get(j);
                if(c1.x == c2.x && c1.y == c2.y) {
                    isEqual = true;
                    break;
                }
            }
            if(!isEqual)
                return false;
        }
        return true;
    }

    ArrayList<CVertex> getNeibourVertex(CVertex vertex)
    {
        ArrayList<CVertex> lstRetVerts = new ArrayList<CVertex>();
        ArrayList<ArrayList<CVertex>> aLstNeiVerts = new ArrayList<ArrayList<CVertex>>();
        for(int i = 0; i < 8; i++){ aLstNeiVerts.add(new ArrayList<CVertex>()); }
        
        for(int i = 0; i < vertexList.size(); i++){
        	CVertex curVert = vertexList.get(i);
        	if(compareVertex(vertex, curVert)) continue;
        	if(vertex.l_num_1 == curVert.l_num_1 || vertex.l_num_1 == curVert.l_num_2){
        		if(curVert.x >= vertex.x && curVert.y >= vertex.y) aLstNeiVerts.get(0).add(curVert);
        		else if(curVert.x >= vertex.x && curVert.y < vertex.y) aLstNeiVerts.get(1).add(curVert);
        		else if(curVert.x < vertex.x && curVert.y < vertex.y) aLstNeiVerts.get(2).add(curVert);
        		else aLstNeiVerts.get(3).add(curVert);
        	}else if(vertex.l_num_2 == curVert.l_num_1 || vertex.l_num_2 == curVert.l_num_2){
        		if(curVert.x >= vertex.x && curVert.y >= vertex.y) aLstNeiVerts.get(4).add(curVert);
        		else if(curVert.x >= vertex.x && curVert.y < vertex.y) aLstNeiVerts.get(5).add(curVert);
        		else if(curVert.x < vertex.x && curVert.y < vertex.y) aLstNeiVerts.get(6).add(curVert);
        		else aLstNeiVerts.get(7).add(curVert);
        	}
        	
        }
        
        for(int i = 0; i < aLstNeiVerts.size(); i++){
	        double distMax = 0;
	        double dist = 0;
	        int nMinIndex = 0;
	        ArrayList<CVertex> curLst = aLstNeiVerts.get(i);
	        if(curLst.size() > 0) {
	            distMax = getDistBetweenVertex(curLst.get(0), vertex);
	            for (int j = 1; j < curLst.size(); j++) {
	                dist = getDistBetweenVertex(curLst.get(j), vertex);
	                if (distMax > dist) {
	                    distMax = dist;
	                    nMinIndex = j;
	                }
	            }
	            CVertex vertexMin = curLst.get(nMinIndex);
	            lstRetVerts.add(vertexMin);
	        }
        }
        return lstRetVerts;
    }
    private double getDistBetweenVertex(CVertex c1, CVertex c2)
    {
        double dist = 0;
        dist = Math.pow(c1.x-c2.x, 2.0f)+Math.pow(c1.y-c2.y, 2.0f);
        return dist;
    }
    private int getSelectedLineNumber(float x, float y)
    {
    	int nSelectedLineNum = -1;
    	
    	int nAngleCount = 12;
    	
    	for(int j = 0; j < vLineList.size(); j++){
    		CLine line = vLineList.get(j);
    		CLine tstLine = new CLine();
    		tstLine.x1 = getXZoom(line.x1);
    		tstLine.x2 = getXZoom(line.x2);
    		tstLine.y1 = getYZoom(line.y1);
    		tstLine.y2 = getYZoom(line.y2);
	    	for(int i = 0; i < nAngleCount; i++){
	    		CLine testLine = new CLine();
	    		testLine.x1 = x + RANGE * (float)Math.cos(3.1425926 * (double)i / (double)nAngleCount);
	    		testLine.y1 = y + RANGE * (float)Math.sin(3.1425926 * (double)i / (double)nAngleCount);
	    		
	    		testLine.x2 = x - RANGE * (float)Math.cos(3.1425926 * (double)i / (double)nAngleCount);
	    		testLine.y2 = y - RANGE * (float)Math.sin(3.1425926 * (double)i / (double)nAngleCount);
	    		
	    		
	    		if(AppUtil.sharedObject().getLineIntersect(tstLine, testLine) != null){
	    			return line.nLineNumber;
	    		}
	    	}
    	}
    	
    	for(int j = 0; j < hLineList.size(); j++){
    		CLine line = hLineList.get(j);
    		CLine tstLine = new CLine();
    		tstLine.x1 = getXZoom(line.x1);
    		tstLine.x2 = getXZoom(line.x2);
    		tstLine.y1 = getYZoom(line.y1);
    		tstLine.y2 = getYZoom(line.y2);
	    	for(int i = 0; i < nAngleCount; i++){
	    		CLine testLine = new CLine();
	    		testLine.x1 = x + RANGE * (float)Math.cos(3.1425926 * (double)i / (double)nAngleCount);
	    		testLine.y1 = y + RANGE * (float)Math.sin(3.1425926 * (double)i / (double)nAngleCount);
	    		
	    		testLine.x2 = x - RANGE * (float)Math.cos(3.1425926 * (double)i / (double)nAngleCount);
	    		testLine.y2 = y - RANGE * (float)Math.sin(3.1425926 * (double)i / (double)nAngleCount);
	    		
	    		if(AppUtil.sharedObject().getLineIntersect(tstLine, testLine) != null){
	    			return line.nLineNumber;
	    		}
	    	}
    	}
    	
        return nSelectedLineNum;
    }
  
    private boolean IsJoined(float a1, float a2, float b1, float b2){
    	if(b2 == Float.MAX_VALUE) return true;
    	float nSum = Math.abs(a1 - a2) + Math.abs(b1 - b2);
    	
    	float fMin = Float.MAX_VALUE;
    	float fMax = Float.MIN_VALUE;
    	
    	if(fMin > a1) fMin = a1; if(fMax < a1) fMax = a1;
    	if(fMin > a2) fMin = a2; if(fMax < a2) fMax = a2;
    	if(fMin > b1) fMin = b1; if(fMax < b1) fMax = b1;
    	if(fMin > b2) fMin = b2; if(fMax < b2) fMax = b2;
    
    	if(fMax - fMin < nSum) return true;
    	return false;
    }
    
    
    private void RemoveLine(float a1, float a2, boolean bIsHori, boolean bIsTouched){
    	 int i, j, k;
    	isFoundLine1 = false;
        isFoundLine2 = false;
        
        CVertex pubV1 = new CVertex();
        CVertex pubV2 = new CVertex();
        boolean bFoundRegion = false;
        for (i = 0;i<regionList.size()-1;i++) {
            CSubRegion subRegion1 = regionList.get(i);
            bFoundRegion = false;
            for (k = 0;k<subRegion1.vertexList.size() - 1;k++)
            {
                pubV1 = subRegion1.vertexList.get(k);
                
                pubV2 = subRegion1.vertexList.get(k+1);
                if((!bIsHori && ((pubV1.l_num_1 == pubV2.l_num_1 && pubV1.l_num_1 == mSelectedLineNumber)
                		||(pubV1.l_num_1 == pubV2.l_num_2 && pubV1.l_num_1 == mSelectedLineNumber) || 
                		(pubV1.l_num_2 == pubV2.l_num_1 && pubV1.l_num_2 == mSelectedLineNumber)
                		||(pubV1.l_num_2 == pubV2.l_num_2 && pubV1.l_num_2 == mSelectedLineNumber)) &&
                		IsJoined(pubV1.y, pubV2.y, a1, a2))
                		|| (bIsHori && ((pubV1.l_num_1 == pubV2.l_num_1 && pubV1.l_num_1 == mSelectedLineNumber)
                        		||(pubV1.l_num_1 == pubV2.l_num_2 && pubV1.l_num_1 == mSelectedLineNumber) || 
                        		(pubV1.l_num_2 == pubV2.l_num_1 && pubV1.l_num_2 == mSelectedLineNumber)
                        		||(pubV1.l_num_2 == pubV2.l_num_2 && pubV1.l_num_2 == mSelectedLineNumber)) && 
                				IsJoined(pubV1.x, pubV2.x, a1, a2)))
                {
                    bFoundRegion = true;
                    break;
                }
            }
            if(bFoundRegion) {
                boolean bFoundSecRegion = false;
                for (j = 0; j < regionList.size(); j++) {
                    if(i == j)
                        continue;
                    CSubRegion subRegion2 = regionList.get(j);
                    for (k = 0; k < subRegion2.vertexList.size() - 1; k++) {
                        CVertex tVertex1 = subRegion2.vertexList.get(k);
                        CVertex tVertex2 = subRegion2.vertexList.get(k + 1);
                        if ((compareVertex(tVertex1, pubV1) && compareVertex(tVertex2, pubV2)) || (compareVertex(tVertex1, pubV2) && compareVertex(tVertex2, pubV1))) 
                        {
                            {
                                bFoundSecRegion = true;
                                break;
                            }
                        }
                    }
                    if(bFoundSecRegion)
                    {
                        subRegion1.connectedRegion.add(subRegion2.regionIndex);
                        subRegion2.connectedRegion.add(subRegion1.regionIndex);
                        if((subRegion1.regionType == 1 && subRegion2.regionType == 2) || (subRegion1.regionType == 2 && subRegion2.regionType == 1)) {
                            Toast.makeText(mContext, "Game is end", Toast.LENGTH_SHORT).show();
                            HashMap<String, Integer> userInfo = dbManager.getDBInfo();
                            
                            int nLevel = ((OpenGLES20Activity)mContext).mGameLevel;
                            int nTempScores = userInfo.get(DBManager.KEY_SCORE + nLevel);
                            
                            UpdateScores();
                            // if score is better save into db
                            if(nTempScores == 0 || mNumberOfLines < nTempScores)
                            {
                                dbManager.saveDBInfo(mNumberOfMoves, mNumberOfLines, nLevel);
                            }
                            isGameEnd = true;
                        }
                                               
                        //merge
                        
                        if(subRegion1.regionType == 1 || subRegion2.regionType == 1)
                        {
                        	ArrayList<CSubRegion> lstTmpRegs = subRegion1.GetConnectedRegions(regionList);
                        	for (k = 0;k<lstTmpRegs.size();k++)
                            {
                        		lstTmpRegs.get(k).regionType = 1;
                            }
                        	lstTmpRegs = subRegion2.GetConnectedRegions(regionList);
                        	for (k = 0;k<lstTmpRegs.size();k++)
                            {
                        		lstTmpRegs.get(k).regionType = 1;
                            }

                        }
                        else if(subRegion1.regionType == 2 || subRegion2.regionType == 2)
                        {
                        	ArrayList<CSubRegion> lstTmpRegs = subRegion1.GetConnectedRegions(regionList);
                        	for (k = 0;k<lstTmpRegs.size();k++)
                            {
                        		lstTmpRegs.get(k).regionType = 2;
                            }
                        	lstTmpRegs = subRegion2.GetConnectedRegions(regionList);
                        	for (k = 0;k<lstTmpRegs.size();k++)
                            {
                        		lstTmpRegs.get(k).regionType = 2;
                            }
                        }else{
                        	ArrayList<CSubRegion> lstTmpRegs = subRegion1.GetConnectedRegions(regionList);
                        	for (k = 0;k<lstTmpRegs.size();k++)
                            {
                        		lstTmpRegs.get(k).regionType = subRegion1.regionType;
                            }
                        	lstTmpRegs = subRegion2.GetConnectedRegions(regionList);
                        	for (k = 0;k<lstTmpRegs.size();k++)
                            {
                        		lstTmpRegs.get(k).regionType = subRegion1.regionType;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private void UpdateScores(){
    	mNumberOfLines = 0;
    	for(int i = 0; i < hLineList.size(); i++){
    		if(hLineList.get(i).touched == true) mNumberOfLines++;
    	}
    	
    	for(int i = 0; i < vLineList.size(); i++){
    		if(vLineList.get(i).touched == true) mNumberOfLines++;
    	}
    }

    
    private void updateGameScreen()
    {
        int i, j;
        
        if(mSelectedLineNumber == -1)
            return;
       
        
        saveCurrentInfo();  // save for undo operation
        
        mNumberOfMoves++;
        
        boolean bIsTouched = false;
        boolean bIsHori = true;
        
        for(j = 0;j<hLineList.size();j++) {
            CLine tItem = hLineList.get(j);
            if(mSelectedLineNumber == tItem.nLineNumber){
                tItem.touched = !tItem.touched;
                bIsTouched = tItem.touched;
                bIsHori = true;
            }
        }
        for(j = 0;j<vLineList.size();j++) {
            CLine tItem = vLineList.get(j);
            if(mSelectedLineNumber == tItem.nLineNumber){
                tItem.touched = !tItem.touched;
                bIsTouched = tItem.touched;
                bIsHori = false;
            }
        }
        
        if(bIsTouched == false){
        	OneState curState = new OneState();
            
            curState.prevRegionList.clear();
            curState.vPrevLineList.clear();
            curState.hPrevLineList.clear();
            for (i = 0;i<vLineList.size();i++)
            {
                CLine saveLine = new CLine();
                CLine temp = vLineList.get(i);
                saveLine.x1 = temp.x1;
                saveLine.y1 = temp.y1;
                saveLine.x2 = temp.x2;
                saveLine.y2 = temp.y2;
                saveLine.touched = temp.touched;
                saveLine.nLineNumber = temp.nLineNumber;
                curState.vPrevLineList.add(saveLine);
            }
            for (i = 0;i<hLineList.size();i++)
            {
                CLine saveLine = new CLine();
                CLine temp = hLineList.get(i);
                saveLine.x1 = temp.x1;
                saveLine.y1 = temp.y1;
                saveLine.x2 = temp.x2;
                saveLine.y2 = temp.y2;
                saveLine.touched = temp.touched;
                saveLine.nLineNumber = temp.nLineNumber;
                curState.hPrevLineList.add(saveLine);
            }
        	
        	if(lstHistState.size() == 0) return;
            
            OneState prevState = lstHistState.get(0);
            
            int tmpScores = mNumberOfMoves;
            GoToPrevState(prevState);
            mNumberOfMoves = mNumberOfLines = tmpScores;
            
            for(j = 0;j<hLineList.size();j++){
            	hLineList.get(j).touched = curState.hPrevLineList.get(j).touched;
            }

            for(j = 0;j<vLineList.size();j++){
            	vLineList.get(j).touched = curState.vPrevLineList.get(j).touched;
            }
            
            for(j = 0;j<hLineList.size();j++) {
                CLine tItem = hLineList.get(j);
                if(tItem.touched){
                	mSelectedLineNumber = tItem.nLineNumber;
                	float a1 = 0; float a2 = Float.MAX_VALUE;
         	        RemoveLine(a1, a2, true, true);
                }
            }
            for(j = 0;j<vLineList.size();j++) {
                CLine tItem = vLineList.get(j);
                if(tItem.touched){
                	mSelectedLineNumber = tItem.nLineNumber;
                	float a1 = 0; float a2 = Float.MAX_VALUE;
         	        RemoveLine(a1, a2, false, true);
                }
            }
          
            UpdateScores();
            invalidate();
        }
        else{
	        float a1 = 0; float a2 = Float.MAX_VALUE;
	        RemoveLine(a1, a2, bIsHori, true);
	        UpdateScores();
	        invalidate();
        }
        
    }

    public HashMap<String, Integer>  getBestScore() {
        HashMap<String, Integer> userInfo = dbManager.getDBInfo();
        return userInfo;
    }
    
    public void undoAllOperation(){
    	 if(bIsTracked) return;
    	
         if(lstHistState.size() == 0) return;
         
         OneState prevState = lstHistState.get(0);
         
         GoToPrevState(prevState);
      
         lstHistState.clear();
         UpdateScores();         
         invalidate();
    }
    
    public void undoOperation()
    {
    	if(bIsTracked) return;
    	if(lstHistState.size() == 0) return;
        
        OneState prevState = lstHistState.get(lstHistState.size() - 1);
        GoToPrevState(prevState);
      
        lstHistState.remove(prevState);
        
        UpdateScores();
        invalidate();
    }
    
    public void GoToPrevState(OneState prevState){
    	  int i;
    	  regionList.clear();
          vLineList.clear();
          hLineList.clear();
          
          mNumberOfMoves = prevState.nLineNumebrs;
          for (i = 0;i<prevState.prevRegionList.size();i++)
          {
              CSubRegion saveRegion = new CSubRegion();
              CSubRegion mSubRegion = prevState.prevRegionList.get(i);
              saveRegion.regionType = mSubRegion.regionType;
              saveRegion.regionIndex = mSubRegion.regionIndex;
              saveRegion.name = mSubRegion.name;
              saveRegion.vertexList = mSubRegion.vertexList;
              ArrayList<Integer> newList = new ArrayList<Integer>(mSubRegion.connectedRegion);
              saveRegion.connectedRegion = newList;
              regionList.add(saveRegion);
          }
          for (i = 0;i<prevState.vPrevLineList.size();i++)
          {
              CLine saveLine = new CLine();
              CLine temp = prevState.vPrevLineList.get(i);
              saveLine.x1 = temp.x1;
              saveLine.y1 = temp.y1;
              saveLine.x2 = temp.x2;
              saveLine.y2 = temp.y2;
              saveLine.touched = temp.touched;
              saveLine.nLineNumber = temp.nLineNumber;
              vLineList.add(saveLine);
          }
          for (i = 0;i<prevState.hPrevLineList.size();i++)
          {
              CLine saveLine = new CLine();
              CLine temp = prevState.hPrevLineList.get(i);
              saveLine.x1 = temp.x1;
              saveLine.y1 = temp.y1;
              saveLine.x2 = temp.x2;
              saveLine.y2 = temp.y2;
              saveLine.touched = temp.touched;
              saveLine.nLineNumber = temp.nLineNumber;
              hLineList.add(saveLine);
          }
       
          
    }
    private void saveCurrentInfo()
    {
        int i;
        
        OneState curState = new OneState();
        
        curState.prevRegionList.clear();
        curState.vPrevLineList.clear();
        curState.hPrevLineList.clear();
        for (i = 0;i<regionList.size();i++)
        {
            CSubRegion saveRegion = new CSubRegion();
            CSubRegion mSubRegion = regionList.get(i);
            saveRegion.regionType = mSubRegion.regionType;
            saveRegion.regionIndex = mSubRegion.regionIndex;
            saveRegion.name = mSubRegion.name;
            saveRegion.vertexList = mSubRegion.vertexList;
            ArrayList<Integer> newList = new ArrayList<Integer>(mSubRegion.connectedRegion);
            saveRegion.connectedRegion = newList;
            curState.prevRegionList.add(saveRegion);
        }
        for (i = 0;i<vLineList.size();i++)
        {
            CLine saveLine = new CLine();
            CLine temp = vLineList.get(i);
            saveLine.x1 = temp.x1;
            saveLine.y1 = temp.y1;
            saveLine.x2 = temp.x2;
            saveLine.y2 = temp.y2;
            saveLine.touched = temp.touched;
            saveLine.nLineNumber = temp.nLineNumber;
            curState.vPrevLineList.add(saveLine);
        }
        for (i = 0;i<hLineList.size();i++)
        {
            CLine saveLine = new CLine();
            CLine temp = hLineList.get(i);
            saveLine.x1 = temp.x1;
            saveLine.y1 = temp.y1;
            saveLine.x2 = temp.x2;
            saveLine.y2 = temp.y2;
            saveLine.touched = temp.touched;
            saveLine.nLineNumber = temp.nLineNumber;
            curState.hPrevLineList.add(saveLine);
        }
        
        curState.nLineNumebrs = mNumberOfMoves;
        lstHistState.add(curState);
        
        invalidate();
    }

}
