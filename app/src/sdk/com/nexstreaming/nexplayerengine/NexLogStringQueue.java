package com.nexstreaming.nexplayerengine;

/**
 * \brief  This class defines the log string queue in CEA 708 closed captions.
 * 
 * To implement CEA 708 closed captions, see the methods \link NexEIA708CaptionView.setEIA708CC \endlink,
 * \link NexEIA708CaptionView.setValidateUpdate \endlink, and \link NexEIA708Struct.SetSourceByteStream\endlink.
 * 
 * \see NexEIA708CaptionView
 * \see NexEIA708Struct
 * 
 * \since version 6.1.2
 */

/** \deprecated  For internal use only.  Please do not use. */
public class NexLogStringQueue {
	
	public static final int LOGSTRQ_MAX_LINE = 15;
	public static final int LOGSTRQ_MAX_COUNT_IN_A_ROW = 42;
	
	private static final int LOGSTRQ_MAX_SIZE = 630;	
	private final static String TAG = "LogStringQueue";
	
	/**
	 * \brief  This class defines a character unit in CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	 /** \deprecated  For internal use only.  Please do not use. */
	public class CharUnit{
		public byte mPenSize;
		public byte mFontStyle;
		public byte mTextTag;
		public byte mOffset;
		public byte mItalics;
		public byte mUnderline;
		public byte mEdgeType;
		public byte mFGOpacity;
		public byte mBGOpacity;
		public byte mFGColor;
		public byte mBGColor;
		public byte mEdgeColor;
		public int mCChar;
		
		void CopyUnit( CharUnit c){
			mPenSize = c.mPenSize;
			mFontStyle = c.mFontStyle;
			mTextTag = c.mTextTag;
			mOffset = c.mOffset;
			mItalics = c.mItalics;
			mUnderline = c.mUnderline;
			mFGOpacity = c.mFGOpacity;
			mBGOpacity = c.mBGOpacity;
			mFGColor = c.mFGColor;
			mBGColor = c.mBGColor;
			mEdgeColor = c.mEdgeColor;
			mCChar = c.mCChar;
		}
		
		public void InitUnit(){
			
			mPenSize = NexEIA708Struct.STANDARD;
			mFontStyle = 0;
			mTextTag = 0;
			mOffset = 1;
			mItalics = 0;
			mUnderline = 0;
			mEdgeType = 0;
			mFGOpacity = NexEIA708Struct.SOLID;
			mBGOpacity = NexEIA708Struct.SOLID;
			mFGColor = 0x3f;
			mBGColor = 0;
			mEdgeColor = 0;
			mCChar = 0;
		}
		
		/**
		 * \brief This method gets the background color of EIA 708 closed captions as an ARGB color.
		 * 
		 * \returns 0 if the background is transparent, or the background ARGB color as an integer.
		 * \since version 6.1.2
		 */
		public int GetARGBBGColor()
		{
			if( mBGOpacity == NexEIA708Struct.TRANSPARENT )
				return 0;
			return NexEIA708Struct.ConvARGBColor(mBGOpacity, mBGColor);
		}
		/**
		 * \brief This method gets the text color of CEA 708 closed captions as an ARGB color.
		 * 
		 * \returns 0 if the text is transparent, or the text ARGB color as an integer.
		 * \since version 6.1.2
		 */
		public int GetARGBTextColor()
		{
			if( mFGOpacity == NexEIA708Struct.TRANSPARENT )
				return 0;
			return NexEIA708Struct.ConvARGBColor(mFGOpacity, mFGColor);
		}
		/**
		 * \brief This method gets the edge color of CEA 708 closed captions as an ARGB color.
		 * 
		 * \returns 0 if the edge is transparent, or the edge ARGB color as an integer.
		 * \since version 6.1.2
		 */
		public int GetARGBEdgeColor()
		{
			if( mEdgeType == NexEIA708Struct.NONE )
				return 0;
			return NexEIA708Struct.ConvARGBColor(mFGOpacity, mEdgeColor);
		}
		
		/**
		 * \brief  This method defines a character unit in CEA 708 closed captions.
		 * 
		 * \since version 6.1.2
		 */
		public CharUnit(){
			mPenSize = NexEIA708Struct.STANDARD;
			mFontStyle = 0;
			mTextTag = 0;
			mOffset = 1;
			mItalics = 0;
			mUnderline = 0;
			mEdgeType = 0;
			mFGOpacity = NexEIA708Struct.SOLID;
			mBGOpacity = NexEIA708Struct.SOLID;
			mFGColor = 0x3f;
			mBGColor = 0;
			mEdgeColor = 0;
			mCChar = 0;
		}
	}
	
	public CharUnit mCharAttr;
	CharUnit mStringQ[];
	int mStringQStartPos;
	int mStringQEndPos;
	int mLineStartPos[];
	int mLineEndPos[];
	int mLineIndex;
	int mLineCount;
	int mLineStart;
	int mWidth;
	int mHeight;
	int mRowCount;
	int mColCount;
	boolean mRowLock;
	boolean mColLock;
	boolean mWordwrap;
	int mJustify;
	int mPrintDirection;
	int mScrollDirection;
	int mOffset;
	int mLastSpacePos;
	int mMaxCol;
	int mMaxRow;
	
	private void createMemberArray(){
		mCharAttr = new CharUnit();
		mStringQ = new CharUnit[LOGSTRQ_MAX_SIZE];
		mLineStartPos = new int[LOGSTRQ_MAX_LINE];
		mLineEndPos  = new int[LOGSTRQ_MAX_LINE];
		
		for(int i = 0 ; i < LOGSTRQ_MAX_SIZE ; i++ ){
			mStringQ[i] = new CharUnit();
		}	

		for(int i = 0 ; i < LOGSTRQ_MAX_LINE ; i++ ){
			mLineStartPos[i] = 0;
			mLineEndPos[i] = 0;
		}		
	}
	
	NexLogStringQueue(){
		createMemberArray();
		mStringQStartPos = 0;
		mStringQEndPos = 0;
		
		mLineIndex = 0;
		mLineCount = 0;
		mLineStart = 0;
		mRowCount = 0;
		mColCount = 31;
		mWidth = mColCount + 1;
		mHeight = mRowCount +1;

		mRowLock = true;
		mColLock = true;
		mWordwrap = false;

		mJustify = NexEIA708Struct.LEFT;
		mPrintDirection = NexEIA708Struct.LEFT_TO_RIGHT;
		mScrollDirection = NexEIA708Struct.BOTTOM_TO_TOP;		
		mMaxCol = LOGSTRQ_MAX_COUNT_IN_A_ROW;
		mMaxRow = LOGSTRQ_MAX_LINE;
		mOffset = 0;
		mLastSpacePos = 0;		
	}
	
	/**
	 * \brief  This method sets the size of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public void SetSize(int rc, int cc, int rl, int cl, int maxcol ){
		mWidth = cc +1;
		mHeight = rc +1;
		mRowCount = rc;
		mColCount = cc;
		if( rl == 0 )
			mRowLock = false;
		else
			mRowLock = true;

		if( cl == 0 )
			mColLock = false;
		else
			mColLock = true;
		mMaxCol = maxcol;
	}
	
	/** 
	 * \brief This method sets the attributes of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public void SetAttr(byte ww, byte j, byte sd , byte pd){
		boolean bWordWrap = false;
		
		if(ww == 1)
			bWordWrap = true;
		
		mWordwrap = bWordWrap;
		mJustify = j;
		mPrintDirection = pd;
		mScrollDirection = sd;		
	}
	
	/**
	 * \brief  This method resets CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public void Reset(){
		int i;
		for( i = 0 ; i < LOGSTRQ_MAX_SIZE ; i++ ){
			mStringQ[i].mCChar = 0;
		}

		for( i = 0 ; i < LOGSTRQ_MAX_LINE ; i++ ){
			mLineStartPos[i] = 0;
			mLineEndPos[i] = 0;
		}
		mStringQStartPos = 0;
		mStringQEndPos = 0;
		mLineIndex = 0;
		mLineCount = 0;
		mLineStart = 0;
		mOffset = 0;
		mLastSpacePos = 0;
		
		mWidth = mColCount +1;
		mHeight = mRowCount +1;
		if( mPrintDirection == NexEIA708Struct.TOP_TO_BOTTOM || mPrintDirection == NexEIA708Struct.BOTTOM_TO_TOP ){
			mWidth = mRowCount +1;
			mHeight = mColCount +1;
		}
	}
	
	/**
	 * \brief  This method sets the position of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public void SetLocation(int row, int col)
		{
		//NexLog.d(TAG, "mLineCount: " + mLineCount + "mHeight: " + mHeight);
		//NexLog.d(TAG, " location= " + "row: " + row + " col: " + col);	
		//NexLog.d(TAG, "before location= " + "mLineStartPos[mLineIndex]: " + mLineStartPos[mLineIndex] + 
		//		   " mLineEndPos[mLineIndex]: : " + mLineEndPos[mLineIndex] + 
		//		   " mStringQStartPos: " + mStringQStartPos + " mStringQEndPos: " + mStringQEndPos + 
		//		   " mLineIndex: " + mLineIndex + " mOffset: " + mOffset + " mLineCount: " + mLineCount);
		
		if (NexEIA708Struct.LEFT == mJustify)
		{
			if (row < mMaxRow)
			{
				if (mLineEndPos[mLineIndex] < mStringQEndPos)
					mLineEndPos[mLineIndex] = mStringQEndPos;

				if (0 >= mLineEndPos[row] - mLineStartPos[row])
				{
					int nTotalLineLen = 0;
					for (int i = 0; i < row; ++i)
						nTotalLineLen += mLineEndPos[i] - mLineStartPos[i];

					if (mStringQEndPos > nTotalLineLen && 0 < row)
					{
						mLineStartPos[row] = mLineEndPos[row - 1];
					}
					else
					{	
						mLineStartPos[row] = nTotalLineLen;	
					}
				}

				if (mLineCount <= row)
				{
					mLineCount = row+1;
					mHeight = row+1;
					mRowCount = row;
				}

				mLineIndex = row;
			}

			if (col < mMaxCol && row < mMaxRow)
			{
				if (mStringQEndPos < mLineStartPos[row] + col) 
				{
					mLineEndPos[row] = mLineStartPos[row] + col;
					mStringQEndPos = mLineEndPos[row];
					mStringQStartPos = mLineStartPos[row];
					mOffset = col;
				} 
				else 
				{
					mLineEndPos[row] = mStringQEndPos;
					mStringQEndPos = mLineStartPos[row] + col;
					mOffset = col;
				}
			}
			else
			{
				NexLog.d(TAG, " SetLocation failed " + "row: " + row + " col: " + col);
			}
		}
		else
		{
			if (NexEIA708Struct.LEFT_TO_RIGHT == mPrintDirection || NexEIA708Struct.RIGHT_TO_LEFT == mPrintDirection)
			{
				// column will be ignored
				if (row < mMaxRow)
				{
					if (mLineEndPos[mLineIndex] < mStringQEndPos)
						mLineEndPos[mLineIndex] = mStringQEndPos;

					if (0 >= mLineEndPos[row] - mLineStartPos[row])
					{
						int nTotalLineLen = 0;
						for (int i = 0; i < row; ++i)
							nTotalLineLen += mLineEndPos[i] - mLineStartPos[i];

						if (mStringQEndPos > nTotalLineLen && 0 < row)
						{
							mLineStartPos[row] = mLineEndPos[row - 1];
						}
						else
						{	
							mLineStartPos[row] = nTotalLineLen;	
						}
					}

					if (mLineCount <= row)
					{
						mLineCount = row+1;
						mHeight = row+1;
						mRowCount = row;
					}

					mLineIndex = row;
					mOffset = 0;
				}
			}
			else if (NexEIA708Struct.TOP_TO_BOTTOM == mPrintDirection || NexEIA708Struct.BOTTOM_TO_TOP == mPrintDirection)
			{
				// row will be ignored
			}
		}		
		
		//	NexLog.d(TAG, "after location= " + "mLineStartPos[row]: " + mLineStartPos[mLineIndex] + 
		//			   " mLineEndPos[row]: : " + mLineEndPos[mLineIndex] + 
		//			   " mStringQStartPos: " + mStringQStartPos + " mStringQEndPos: " + mStringQEndPos + 
		//			   " mLineIndex: " + mLineIndex + " mOffset: " + mOffset + " mLineCount: " + mLineCount);
	}

	/**
	 * \brief  This method pushes a character in the display of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public void PushChar(int nChar)
	{
		boolean bFulledColumn = false;
		boolean bNewLine = false;
		
		if (0 == mLineCount)
		{	
			mLineCount++;
			mLineStartPos[mLineIndex] = mStringQStartPos;
		}
		
		//handle : mStringQ buffer < mStringQEndPos
		if (LOGSTRQ_MAX_SIZE <= mStringQEndPos)
		{
			NexLog.d(TAG, "PushChar() buffer is max. mStringQEndPos: " + mStringQEndPos + " mOffset: " + mOffset);
			
			for (int i = LOGSTRQ_MAX_SIZE - mOffset, j = 0; i < LOGSTRQ_MAX_SIZE; ++i, ++j)
				mStringQ[j].mCChar = mStringQ[i].mCChar;
		
			mLineStartPos[mLineIndex] = 0;
			mStringQEndPos = mOffset;
		}
		
		//handle : nChar == 0x0d
		if (0x0d == nChar) 
		{
			if (mLineIndex + 1 < mHeight || (mLineIndex == 0)) 
			{
				NexLog.d(TAG, "PushChar input 0x0D: mLineIndex: " + mLineIndex
						+ " mHeight: " + mHeight);
				SetLocation(mLineIndex + 1, 0);
			}
			else 
			{
				if (mStringQEndPos >= mLineEndPos[mLineIndex])
					mLineEndPos[mLineIndex] = mStringQEndPos;

				for (int i = 0; i < mHeight; ++i) 
				{
					if (0 <= i - 1) 
					{
						mLineStartPos[i - 1] = mLineStartPos[i];
						mLineEndPos[i - 1] = mLineEndPos[i];
					} 
					else 
					{
						for (int j = mLineStartPos[i]; j < mLineEndPos[i]; ++j)
							mStringQ[j].mCChar = 0;
						
						mLineStartPos[i] = 0;
						mLineEndPos[i] = 0;
					}
				}

				mOffset = 0;
				mLineStartPos[mLineIndex] = mLineEndPos[mLineIndex];
				mLineEndPos[mLineIndex] = mLineEndPos[mLineIndex];
				mStringQEndPos = mLineStartPos[mLineIndex];
			}
			
		//	NexLog.d(TAG, "PushChar input 0x0D: mLineIndex: " + mLineIndex
		//			+ " mLineStartPos[mLineIndex]: "
		//			+ mLineStartPos[mLineIndex] + " mLineEndPos[mLineIndex]:"
		//			+ mLineEndPos[mLineIndex]);
		} 
		else if (0x08 == nChar)	//Backspace 
		{
			NexLog.d(TAG, "PushChar input 0x08");
			//mStringQ[mStringQEndPos].InitUnit();

            if (0 < mStringQEndPos)
				mStringQEndPos--;

			if (0 < mOffset)
				mOffset--;
			
			mLineEndPos[mLineIndex] = mStringQEndPos;
		}
		else 
		{
			//handle : lock
			if (mOffset == mWidth)
			{
				if( mColLock )
				{
					//add row
					bFulledColumn = true;
				}
				else
				{
					if (mOffset < mMaxCol)
					{	
						mWidth++;
					}
					else
					{	
						//add row
						bFulledColumn = true;
					}
				}
			}
			
			if (false == bFulledColumn)
			{
				//put char
				mCharAttr.mCChar = nChar;
				mStringQ[mStringQEndPos].CopyUnit(mCharAttr);
				mStringQEndPos++;
				
				mLineEndPos[mLineIndex] = mStringQEndPos;
				
				mOffset++;	
			}
		}
		
		//handle : newLine
		if (bFulledColumn)
		{
			if (mWordwrap)
			{
				//TBD. handle wordwrap: the value should be always disable.
				if (mLineCount == mHeight)
				{
					if (mRowLock)
					{
						//TBD.
						mHeight++;
						bNewLine = true;					
					}	
					else
					{
						if (mLineCount < mMaxRow)
						{
							mHeight++;
							bNewLine = true;
						}
						else
						{
							//TBD.
						}
					}
				}
				else if (mLineCount < mHeight)
				{
					bNewLine = true;
				}	
			}
			
			if (bNewLine)
			{
				int nNewIndex = mLineIndex + 1;
				nNewIndex %= LOGSTRQ_MAX_LINE;
				
				mLineStartPos[nNewIndex] = mStringQEndPos;
				mLineEndPos[nNewIndex] = mStringQEndPos;
				
				mLineIndex++;
				mLineCount++;
				mOffset = 0;
				
				if (mLineCount > mHeight)
				{	
					mHeight = mLineCount;
					if (mHeight >= mMaxRow)
						mHeight = mMaxRow;
				}
			}	
		}
	}
	
	public int PeekLineCharUnit(CharUnit[] cUnit, int nLineIndex)
	{
		int nLineCount = 0;
			
		if (null == mLineEndPos || null == mLineStartPos)
			return nLineCount;
		
		try
		{
			if (nLineIndex < mLineCount)
			{
				if (mScrollDirection == NexEIA708Struct.TOP_TO_BOTTOM || 
					mScrollDirection == NexEIA708Struct.LEFT_TO_RIGHT)
				{
					nLineIndex = mLineCount - nLineIndex - 1;
				}
				
				if (nLineIndex > LOGSTRQ_MAX_LINE)
				{
					NexLog.w(TAG, "PeekLineCharUnit - exceed the max line: " + nLineIndex);
					nLineIndex %= LOGSTRQ_MAX_LINE;
				}
				
				nLineCount = mLineEndPos[nLineIndex] - mLineStartPos[nLineIndex];
				if (0 < nLineCount && nLineCount <= LOGSTRQ_MAX_COUNT_IN_A_ROW)
				{
					int nOffset = 1;
					int nStringIndex = mLineStartPos[nLineIndex];
					if (mPrintDirection == NexEIA708Struct.RIGHT_TO_LEFT || 
						mPrintDirection == NexEIA708Struct.BOTTOM_TO_TOP)
					{
						nStringIndex = mLineEndPos[nLineIndex];
						nOffset *= -1;
					}
					
					for (int i = 0; i < nLineCount; ++i, nStringIndex += nOffset)
					{
						if (LOGSTRQ_MAX_SIZE >= nStringIndex)
							nStringIndex %= LOGSTRQ_MAX_SIZE;
						
						cUnit[i] = mStringQ[nStringIndex];
					}
				}
				else
				{
					NexLog.e(TAG, "PeekLineCharUnit - invalid line count : " + nLineCount);
					nLineCount = 0;
				}
			}
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return nLineCount;
	}
	
	int PeekCharUnit(CharUnit[] cUnit, int top_row_index)
	{
		int nResult = 0;
		if (null != cUnit && 0 <= top_row_index)
			nResult = PeekLineCharUnit(cUnit, top_row_index);
		
		return nResult;
	}
	
	/**
	 * \brief  This method gets the width of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public int GetWidth()
	{
		return mWidth;
	}

	/**
	 * \brief  This method gets the height of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	public int GetHeight()
	{
		return mHeight;
	}
	
	void debugPrint()
	{
		int y, x;
		int col;
		CharUnit[] cUnit = new CharUnit[LOGSTRQ_MAX_COUNT_IN_A_ROW];
		for( x = 0 ; x < LOGSTRQ_MAX_COUNT_IN_A_ROW ; x++ ){
			cUnit[x] = new CharUnit();
		}
		
		NexLog.d(TAG," row="+GetHeight()+", col="+GetWidth());
		NexLog.d(TAG,"  0         1         2         3         4");
		NexLog.d(TAG,"  0123456789012345678901234567890123456789012");
		NexLog.d(TAG,"  -------------------------------------------");
		for( y = 0 ; y < GetHeight() ; y++  )	
		{
			col = PeekCharUnit(cUnit,y );
			NexLog.d(TAG,"new col="+col);
			String convertedChar ="";
			for( x = 0 ; x < col ; x++){
				//TODO : print
				if(cUnit[x].mCChar == 0x00 )
				{	
					convertedChar += " ";
				}
				else
				{	
					convertedChar += Character.toString((char)cUnit[x].mCChar);
				}
			}
			NexLog.d(TAG,y+"|"+convertedChar);	
		}
		NexLog.d(TAG,"-------------------------------------------");
	}
	
	public void testModule(){
		String str = "ROWS AND COLUMNS ARE NOT LOCKED FOR EVER AND EVER AND EVER 1234567890 new line none QWERTY UHBGFVC Yoon jeong wook Max string scroll up down call ~!@#$%^&*()_+ ROUND INDEX OK!? MoreData indeed ZXCVBNM";
		int i;
		NexLog.e(TAG,"----------------testModule---------------------------");
		
		for( i = 0 ; i < str.length() ; i++ ){
			PushChar(str.charAt(i));
			debugPrint();
		}
	}
}
