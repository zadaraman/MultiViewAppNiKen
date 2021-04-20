package com.nexstreaming.nexplayerengine;

import android.os.Parcel;

/**
 * \brief  This class defines a structure to handle CEA 708 closed captions in content. 
 * 
 * CEA 708 (also known as EIA 708) closed captions should be rendered seperately, using 
 * for example an instance of the NexEIA708CaptionView class.
 * 
 * Since CEA 708 closed captions include multiple services where different closed caption data is provided
 * for the same content, for example different languages, the method \link SetSourceByteStream\endlink can
 * be used to set which service should be used to receive CEA 708 closed caption data.
 * 
 * \see NexEIA708CaptionView
 * \since version 6.1.2
 */
public class NexEIA708Struct {
	private static String TAG = "EIA708Struct";
	
	static final int EIA708_WINDOW_MAX = 8;	
	static final int EIA708_SERVICE_MAX = 6;
	
	static final byte LEFT = 0;
	static final byte RIGHT = 1;
	static final byte CENTER =2;
	static final byte FULL = 3;
	

	static final byte LEFT_TO_RIGHT = 0;
	static final byte RIGHT_TO_LEFT = 1;
	static final byte TOP_TO_BOTTOM = 2;
	static final byte BOTTOM_TO_TOP = 3;

	private static final byte SNAP = 0;
	private static final byte FADE = 1;
	private static final byte WIPE = 2;

	static final byte SOLID = 0;
	static final byte FLASH = 1;
	static final byte TRANSLUCENT = 2;
	static final byte TRANSPARENT =3;

	static final byte SUBSCRIPT = 0;
	static final byte NORMAL = 1;
	static final byte SUPERSCRIPT = 2;
	
	static final byte ITALICS = 1;
	static final byte UNDERLINE = 1;
	
	static final byte NONE = 0;
	static final byte RAISED = 1;
	static final byte DEPRESSED = 2;
	static final byte UNIFORM = 3;
	static final byte SHADOW_LEFT = 4;
	static final byte SHADOW_RIGHT = 5;


	static final byte SMALL = 0;
	static final byte STANDARD =1;
	static final byte LARGE = 2;
	static final byte EXTRA_LARGE = 3;

	/**
	 * \brief  This class describes the window where CEA 708 closed captions will be displayed.
	 * 
	 * \since version 6.1.2
	 */
	public class EIA708Window{
		byte mPriority;
		byte mAanchorPoint;
		byte mRelativePosition;
		byte mAnchorVertical;
		byte mAnchorHorizontal;	
		byte mRowCount;
		byte mColCount;
		byte mRowLock;
		byte mColumnLock;
		byte mVisible;
		byte mJustify;
		byte mPrintDirection;
		byte mScrollDirection;
		byte mWordwrap;
		byte mDisplayEffect;
		byte mEffectDirection;
		byte mEffectSpeed;
		byte mFillOpacity;
		byte mBorderType;
		byte mFillColor;	
		byte mBorderColor;
		byte mPenStyle;
		byte mWinStyle;

		private NexLogStringQueue mTextBuffer;
		private boolean mDefined;
		private boolean mUpdate;
		
		EIA708Window()
		{
			mVisible = 0;
			mDefined = false;
			mUpdate = false;
			mTextBuffer = new NexLogStringQueue();
			ClearWindow();
		}
		
		/**
		 * \brief  This method gets the number of the current line of text in CEA 708 closed captions
		 * 
		 * \since version 6.1.2
		 */
		public int GetTextLineCount(){
			return mTextBuffer.mLineCount;
		}
		
                /**  \brief This method gets the attributes of a character in CEA 708 closed caption text. 
                 *
                 */
		public NexLogStringQueue.CharUnit GetCharAttr()
		{
			return mTextBuffer.mCharAttr;
		}
		
		/**
		 * \brief  This method gets a line of text in CEA 708 closed captions
		 * 
		 * \param cData  The caption data for the received line of text, as an \c array of characters.
		 * \param index  The index of the line of text, as an \c integer.
		 * 
		 * \since version 6.1.2
		 */
		public int GetTextLine(NexLogStringQueue.CharUnit[] cData, int index){
			return mTextBuffer.PeekCharUnit(cData, index);
		}
		
		/**
		 * \brief  This method gets the width of the window for CEA 708 closed captions.
		 * 
		 * \since version 6.1.2
		 */
		public int GetWidth(){
			return mTextBuffer.GetWidth();
		}
		
		/**
		 *  \brief  This method gets the height of the window for CEA 708 closed captions
		 *  
		 *  \since version 6.1.2
		 */
		public int GetHeight(){
			return mTextBuffer.GetHeight();
		}
		
		/**
		 * \brief  This method gets the ARGB color windows for CEA 708 closed captions.
		 * 
		 * \since version 6.1.2
		 */
		
		public int GetARGBColorWindows(){
			return NexEIA708Struct.ConvARGBColor(mFillOpacity, mFillColor);
		}

		/**
		 * \brief  This method gets the border color of CEA 708 closed captions as an ARGB color.
		 * 
		 * \since version 6.1.2
		 */
		public int GetARGBColorBorderColor(){
			return NexEIA708Struct.ConvARGBColor(SOLID, mBorderColor);
		}		
		
		void DefineWindow(byte Priority
				,byte AanchorPoint
				,byte RelativePosition
				,byte AnchorVertical
				,byte AnchorHorizontal	
				,byte RowCount
				,byte ColCount
				,byte RowLock
				,byte ColumnLock
				,byte Visible
				,byte WinStyle
				,byte PenStyle
				)
		{
//			if( mDefined )
//				return;
			
			mPriority = Priority;
			mAanchorPoint = AanchorPoint; 
			mRelativePosition = RelativePosition;
			mAnchorVertical = AnchorVertical;
			mAnchorHorizontal = AnchorHorizontal; 
			mRowCount = RowCount; 
			mColCount = ColCount; 
			mRowLock = RowLock; 
			mColumnLock = ColumnLock; 
			mVisible = Visible; 
			mDefined = true;
			
			if(mVisible == 1)
				mUpdate = true;
			
			mTextBuffer.SetSize(mRowCount, mColCount, mRowLock, mColumnLock, 42);
			
			if (false == mDefined)
			{
				mTextBuffer.Reset();
				
				SetPerdefinedPenStyle(PenStyle);
				SetPerdefinedWinStyle(WinStyle);
				
				mPenStyle = PenStyle;
				mWinStyle = WinStyle;
			}
			else
			{
				if (mWinStyle != WinStyle)
					SetPerdefinedWinStyle(WinStyle);	
			}
		}
		
		/**
		 * \brief  This method sets the pen attributes and color based on the pen style in CEA 708 closed captions.
		 * 
		 * \since version 6.1.2
		 */
		public final void SetPerdefinedPenStyle(byte PenStyle)
		{
			SetPenAttr(STANDARD//char PenSize
					,(byte)0//char FontStyle
					,(byte)0//char TextTag
					,(byte)0//char Offset
					,(byte)0//char Italics
					,(byte)0//char Underline
					, NONE //char EdgeType
					);

			SetPenColor((byte)0x2a//char FGColor
					,SOLID//char FGOpacity
					,(byte)0x0//char BGColor
					,SOLID//char BGOpacity
					,(byte)0//char EdgeColor
					);			
			
			switch( PenStyle){
				case 2:
					mTextBuffer.mCharAttr.mFontStyle = 1;
				break;
				case 3:
					mTextBuffer.mCharAttr.mFontStyle = 2;
				break;
				case 4:
					mTextBuffer.mCharAttr.mFontStyle = 3;
				break;
				case 5:
					mTextBuffer.mCharAttr.mFontStyle = 4;
				break;
				case 6:
					mTextBuffer.mCharAttr.mFontStyle = 3;
					mTextBuffer.mCharAttr.mEdgeType = UNIFORM;
					mTextBuffer.mCharAttr.mBGOpacity = TRANSPARENT;
				break;
				case 7:
					mTextBuffer.mCharAttr.mFontStyle = 4;
					mTextBuffer.mCharAttr.mEdgeType = UNIFORM;
					mTextBuffer.mCharAttr.mBGOpacity = TRANSPARENT;
				break;
			}
		}
		
		/**
		 * \brief  This method sets the window attributes and color based on the window style in CEA 708 closed captions.
		 * 
		 * \since version 6.1.2
		 */
		public final void SetPerdefinedWinStyle(byte WinStyle)
		{
			SetWinAttr(LEFT //char Justify
					,LEFT_TO_RIGHT //char PrintDirection
					,BOTTOM_TO_TOP //char ScrollDirection
					,(byte)0 //char Wordwrap
					,SNAP //char DisplayEffect
					,(byte)0//char EffectDirection
					,(byte)0//char EffectSpeed
					,SOLID//char FillColor
					,SOLID//char FillOpacity
					,(byte)0//char BorderType
					,(byte)0//char BorderColor
					);
			
			switch( WinStyle){
				case 2:
					mFillOpacity= TRANSPARENT;
				break;
				case 3:
					mJustify= CENTER;
				break;
				case 4:
					mWordwrap= 1;
				break;
				case 5:
					mWordwrap= 1;
					mFillOpacity= TRANSPARENT;
				break;
				case 6:
					mJustify= CENTER;
					mWordwrap= 1;
				break;
				case 7:
					mPrintDirection = TOP_TO_BOTTOM;
					mScrollDirection = RIGHT_TO_LEFT;
				break;
			}
			
			mTextBuffer.SetAttr(mWordwrap, mJustify, mScrollDirection, mPrintDirection);
			
		}
		
		/**
		 * \brief  This method deletes the CEA 708 closed caption window.
		 * 
		 * \since version 6.1.2
		 */
		public void DeleteWindow()
		{
			mDefined = false;
			if( mVisible == 1 )
			{
				mUpdate = true;
				mVisible = 0;
			}
			
			mTextBuffer.Reset();
		}
		
		/**
		 * \brief  This method clears the CEA 708 closed caption window. 
		 * 
		 * \since version 6.1.2
		 */
		public void ClearWindow()
		{
//			if( !mDefined )
//				return;
			
			if( mVisible == 1 && GetTextLineCount() != 0 )
			{	
				mUpdate = true;
			}
			mTextBuffer.Reset();
		}
		
		/**
		 * \brief This method makes the CEA 708 closed caption window visible when called.
		 * 
		 * \since version 6.1.2
		 */
		public void ShowWindow()
		{
			if( mDefined )
			{
				if( mVisible == 0 )
					mUpdate = true;
				
				mVisible = 1;
			}
		}
		
		/**
		 * \brief This method hides the CEA 708 closed caption window when called.
		 * 
		 * \since version 6.1.2
		 */
		public void HideWindow()
		{
			if( mDefined )
			{
				if( mVisible == 1 )
					mUpdate = true;
				mVisible = 0;
			}
		}
		
		/**
		 * \brief This method toggles the CEA 708 closed caption window when called.
		 * 
		 * \since version 6.1.2
		 */
		public void ToggleWindow()
		{
			if( !mDefined )
				return;
			
			if( mVisible == 1 )
				mVisible = 0;
			else
				mVisible = 1;
			mUpdate = true;
		}
		
		/**
		 * \brief This method sets the CEA 708 closed caption window attributes.
		 * 
		 * \since version 6.1.2
		 */
		public void SetWinAttr(byte Justify
				,byte PrintDirection
				,byte ScrollDirection
				,byte Wordwrap
				,byte DisplayEffect
				,byte EffectDirection
				,byte EffectSpeed
				,byte FillColor
				,byte FillOpacity
				,byte BorderType
				,byte BorderColor
				)
		{
			if( !mDefined )
				return;
			
			mJustify = Justify;
			mPrintDirection = PrintDirection;
			mScrollDirection= ScrollDirection;
			mWordwrap= Wordwrap;
			mDisplayEffect= DisplayEffect;
			mEffectDirection= EffectDirection;
			mEffectSpeed= EffectSpeed;
			mFillOpacity= FillOpacity;
			mBorderType= BorderType;
			mFillColor= FillColor;
			mBorderColor= BorderColor;	
			mTextBuffer.SetAttr(mWordwrap, mJustify, mScrollDirection, mPrintDirection);
			//if( mVisible == 1 )mUpdate = true;
		}
		
		/**
		 * \brief This method sets the CEA 708 closed caption pen attributes.
		 * 
		 * \since version 6.1.2
		 */
		public void SetPenAttr(byte PenSize
				,byte FontStyle
				,byte TextTag
				,byte Offset
				,byte Italics
				,byte Underline
				,byte EdgeType
				)
		{
			if( !mDefined )
				return;
			
			mTextBuffer.mCharAttr.mPenSize = PenSize;
			mTextBuffer.mCharAttr.mFontStyle = FontStyle;
			mTextBuffer.mCharAttr.mTextTag = TextTag;
			mTextBuffer.mCharAttr.mOffset = Offset;
			mTextBuffer.mCharAttr.mItalics = Italics;
			mTextBuffer.mCharAttr.mUnderline = Underline;
			mTextBuffer.mCharAttr.mEdgeType = EdgeType;
			//if( mVisible == 1 )mUpdate = true;
		}
		
		/**
		 * \brief This method sets the CEA 708 closed caption pen color.
		 * 
		 * \since version 6.1.2
		 */
		public void SetPenColor(byte FGColor
				,byte FGOpacity
				,byte BGColor
				,byte BGOpacity
				,byte EdgeColor
				)
		{
			if( !mDefined )
				return;

			mTextBuffer.mCharAttr.mFGOpacity = FGOpacity;
			mTextBuffer.mCharAttr.mBGOpacity = BGOpacity;
			mTextBuffer.mCharAttr.mFGColor = FGColor;
			mTextBuffer.mCharAttr.mBGColor = BGColor;
			mTextBuffer.mCharAttr.mEdgeColor = EdgeColor;
		}
		
		/**
		 * \brief This method sets the CEA 708 closed caption pen position.
		 * 
		 * \param Row  The row position.
		 * \param Col  The column position.
		 * 
		 * \since version 6.1.2
		 */
		public void SetPenLocation(int Row, int Col){
			if (mDefined)
				mTextBuffer.SetLocation(Row, Col);
		}
		
		/**
		 * \brief  This method appends a character to the end of a CEA 708 closed caption.
		 * 
		 * \since version 6.1.2
		 */
		public void ApendChar(int cc)
		{
			if (false == mDefined)
				return;
			
			if (1 == mVisible)
				mUpdate = true;
			
			mTextBuffer.PushChar(cc);
		}

		/**
		 * \brief  This method sets the end of text in CEA 708 closed captions.
		 * 
		 * \since version 6.1.2
		 */
		public void SetEndofText()
		{
			if( mVisible == 1 )
				mUpdate = true;
		}
		
		/**
		 * \brief  This method indicates if CEA 708 closed caption data has been updated.
		 * 
		 * \returns \c TRUE if caption data has been updated, otherwise \c FALSE.
		 * \since version 6.1.2
		 */
		public boolean IsUpdate()
		{
			return mUpdate;
		}
		
		/**
		 * \brief  This method resets the mUpdate paramater that indicates whether CEA 708 closed caption data has been updated or not.
		 * 
		 * \since version 6.1.2
		 */
		public void ClearUpdate()
		{
			mUpdate = false;
		}
	}//class EIA708Window
	
	/**
	 * \brief This class defines a CEA 708 service.
	 * 
	 * \since version 6.1.2
	 */
	public class EIA708Service {
		EIA708Window[] mWindow;
		boolean mEnable;
		boolean mUpdate;
		int mCurrentWindow;
		EIA708Service()	{
			mEnable = false;
			mUpdate = false;
			mWindow = new EIA708Window[EIA708_WINDOW_MAX];
			for(int i=0; i<EIA708_WINDOW_MAX; i++)
				mWindow[i] = new EIA708Window();
			mCurrentWindow = 0;
		}
		
		public void SetCurrentWindow(int WinID)
		{
			if(mWindow[WinID].mDefined)
				mCurrentWindow = WinID;
		}
	}//class EIA708Service
	
	public EIA708Service[] mService;
	public NexEIA708Struct()
	{
		mService = new EIA708Service[EIA708_SERVICE_MAX];
		for(int i = 0 ; i < EIA708_SERVICE_MAX ; i++) 
		{
			mService[i] = new EIA708Service();
		}
	}

	public int GetMaxServiceCount(){
		return EIA708_SERVICE_MAX;
	}
	
	/**
	 * \brief This method checks if the CEA 708 service number is enabled.
	 * 
	 * \param ServiceNO  The CEA 708 service number.
	 * 
	 * \returns \c TRUE if the CEA 708 service is enabled, otherwise \c FALSE.
	 * \since version 6.1.2
	 */
	public boolean IsEnableService(int ServiceNo){
		int i ;
		i = ServiceNo;
		return mService[i].mEnable;
	}
	
	void DisplayCtrlWindow(int serviceNO, byte cmd, byte map){
		int i;
		int updateCount = 0;
		for( i = 0 ; i < EIA708_WINDOW_MAX ; i++){
            mService[serviceNO].mWindow[i].ClearUpdate();
			if( ((map>>i)&0x1) == 1 ){
				if( mService[serviceNO].mWindow[i].mDefined ){
					if( (cmd&0xff) == 0x8c){ //DLW DeleteWindows
						mService[serviceNO].mWindow[i].DeleteWindow();
					}else if( (cmd&0xff) == 0x8A ){ //HDW HideWindows
						mService[serviceNO].mWindow[i].HideWindow();
					}else if ( (cmd&0xff) == 0x89 ){ //DSW DisplayWindows
						mService[serviceNO].mWindow[i].ShowWindow();
					}else if ( (cmd&0xff) == 0x8b ){	//TGW ToggleWindows
						mService[serviceNO].mWindow[i].ToggleWindow();
					}else if( (cmd&0xff) == 0x88 ){ //CLW ClearWindows
						mService[serviceNO].mWindow[i].ClearWindow();
					}
				}
				if( mService[serviceNO].mWindow[i].IsUpdate()){
					updateCount++;
					//mService[serviceNO].mWindow[i].ClearUpdate();
				}
			}
		}
		if( updateCount > 0 ){
			mService[serviceNO].mUpdate = true;
		}
	}
	
	void ProcessCL(int serviceNo, byte[] Data, int len){
		int multibyte;
		int ServiceNo = serviceNo;
		ServiceNo--;

		switch (Data[0]&0xff){
			case 0x03: //ETX
				mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
						.SetEndofText();
				mService[ServiceNo].mUpdate = mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow].IsUpdate();
				mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow].ClearUpdate();
				break;
				
			case 0x08:	//BS
				mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
						.ApendChar(0x08);
				break;
			case 0x0D: //CR
				mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
						.ApendChar(0x0D);
				break;
			case 0x18: //P16
				multibyte = byte2int(Data,1,2);
				mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
						.ApendChar(multibyte);
				
				break;				
		}
	}
	
	void ProcessCR(int serviceNo, byte[] Data, int len){
		int ServiceNo = serviceNo;
		int winID;
		int Cmd = Data[0]&0xff; 
		ServiceNo--;
		
		switch (Cmd){
		case 0x80: //CW0  SetCurrentWindow
		case 0x81: //CW1
		case 0x82: //CW2
		case 0x83: //CW3
		case 0x84: //CW4
		case 0x85: //CW5
		case 0x86: //CW6
		case 0x87: //CW7
			winID = Cmd-0x80;
			mService[ServiceNo].SetCurrentWindow(winID);
			break;
		
		case 0x88: //CLW ClearWindows
		case 0x89: //DSW DisplayWindows	
		case 0x8a: //HDW HideWindows
		case 0x8b: //TGW ToggleWindows	
		case 0x8c: //DLW DeleteWindows	
			DisplayCtrlWindow(ServiceNo,Data[0],Data[1]);
			break;
		case 0x8d: //DLY Delay
		case 0x8e: //DLC DelayCancel
			// engin process
			break;
		case 0x8f: //RST Reset
			DisplayCtrlWindow(ServiceNo,(byte)0x88,(byte)0xff);
			DisplayCtrlWindow(ServiceNo,(byte)0x8a,(byte)0xff);
			DisplayCtrlWindow(ServiceNo,(byte)0x8c,(byte)0xff);
			break;
		case 0x90: //SPA SetPenAttributes
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.SetPenAttr(Data[1] //Size
					,Data[2] //FontStyle
					,Data[3] //TextTag
					,Data[4] //Offset
					,Data[5] //Italics
					,Data[6] //Underline
					,Data[7] //EdgeType
					);
			break;
		case 0x91: //SPC SetPenColor
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.SetPenColor(Data[1] //FGColor
							,Data[2] //FGOpacity
							,Data[3] //BGColor
							,Data[4] //BGOpacity
							,Data[5] //EdgeColor
							);
			break;
		case 0x92: //SPL SetPenLocation
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.SetPenLocation(Data[1] //row
							,Data[2] //col
									);
			break;
		case 0x97: //SWA SetWindowAttributes
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.SetWinAttr(Data[1]//Justify
							,Data[2] //PrintDirection
							,Data[3] //ScrollDirection
							,Data[4] //Wordwrap
							,Data[5] //DisplayEffect
							,Data[6] //EffectDirection
							,Data[7] //EffectSpeed
							,Data[8] //FillOpacity
							,Data[9] //BorderType
							,Data[10] //FillColor
							,Data[11] //BorderColor
							);
			break;
		case 0x98: //DF0 DefineWindow
		case 0x99: //DF1
		case 0x9a: //DF2
		case 0x9b: //DF3
		case 0x9c: //DF4
		case 0x9d: //DF5
		case 0x9e: //DF6
		case 0x9f: //DF7
			winID = Cmd-0x98;
			NexLog.e(TAG,"DefineWindow ID"+winID);
			
			mService[ServiceNo].mWindow[winID]
					.DefineWindow(Data[1]//Priority
							,Data[2] //AanchorPoint
							,Data[3] //RelativePosition
							,Data[4] //AnchorVertical
							,Data[5] //AnchorHorizontal
							,Data[6] //RowCount
							,Data[7] //ColCount
							,Data[8] //RowLock
							,Data[9] //ColumnLock
							,Data[10] //Visible
							,Data[11] //WinStyle
							,Data[12] //PenStyle
							);
			mService[ServiceNo].SetCurrentWindow(winID);
			
			mService[ServiceNo].mUpdate = mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow].IsUpdate();
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow].ClearUpdate();	

			break;
		default :
			break;					
		}
	}
	
	/**
	 * \brief This method sets the source byte stream for CEA 708 closed captions.
	 * 
	 * Like the different channels in CEA 608 closed captions, each CEA 708 service provides different 
	 * caption information for the same content, for example captions in different languages.  The option 
	 * to change the CEA 708 service may be beneficial to offer to application users.
	 * 
	 * \param serviceNO	The CEA 708 service number from which to receive CEA 708 closed caption data.
	 * \param Data		The CEA 708 closed caption data as an array of bytes.
	 * \param len		The size of the CEA 708 closed caption data array.
	 * 
	 * \returns \c TRUE is successful, otherwise \c FALSE. 
	 * \since version 6.1.2
	 */
	public boolean SetSourceByteStream(int serviceNo, byte[] Data, int len){
		int ServiceNo = serviceNo;
		boolean bUpdate;
		ServiceNo--;
		mService[ServiceNo].mEnable = true;
		if (len == 2 && 0x6A == (Data[0]&0xFF) && 0x26 == (Data[1]&0xFF)) // music symbol
		{			
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.ApendChar(0x266A);
		}
		else if( (Data[0]&0xff) >= 0x00 &&  (Data[0]&0xff) <= 0x1f ){
			//CL
			ProcessCL(serviceNo,Data,len);
		}else if( (Data[0]&0xff) >= 0x20 && (Data[0]&0xff) <=0x7f ){
			//GL
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.ApendChar(Data[0]&0xff);
		}else if( (Data[0]&0xff) >= 0x80 && (Data[0]&0xff) <=0x9f ){
			//CR
			ProcessCR(serviceNo,Data,len);
		}else if( (Data[0]&0xff) >= 0xa0 && (Data[0]&0xff) <=0xff ){
			//GR
			mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow]
					.ApendChar(Data[0]&0xff);			
		}else{
			mService[ServiceNo].mEnable = false;	
		}
		
		if (mService[ServiceNo].mWindow[mService[ServiceNo].mCurrentWindow].IsUpdate())
			mService[ServiceNo].mUpdate = true;
		
		bUpdate = mService[ServiceNo].mUpdate;
		mService[ServiceNo].mUpdate = false;
		return bUpdate;
	}

	/**
	 * \brief  This method sets the source byte stream for CEA 708 closed captions.
	 * 
	 * \param Data  The parcel of CEA 708 closed caption data.
	 * 
	 * \returns Always \c true.
	 * 
	 * \since version 6.1.2
	 */
	boolean SetSourceByteStream(Parcel Data){
		if (null != Data)
			Data.readByte();
		return true;
	}
	
	/**
	 * \brief This method handles color conversion of CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	final static public int ConvARGBColor(byte Opacity, byte Color)
	{
		byte R,G,B,A;
		int ARGBColor = 0;
		
		if( Opacity == SOLID )
			A = (byte)255;
		else if( Opacity == TRANSLUCENT )
			A = (byte)128;
		else if( Opacity ==  TRANSPARENT)
			A = (byte)0;
		else
			A = (byte)255;
		
		R = (byte) ((byte)((Color>>4)&0x3)*85);
		G = (byte) ((byte)((Color>>2)&0x3)*85);
		B = (byte) ((byte)((Color)&0x3)*85);
		
		ARGBColor |= (A&0xFF);	
		ARGBColor <<= 8;
		ARGBColor |= (R&0xFF);
		ARGBColor <<= 8;
		ARGBColor |= (G&0xFF);
		ARGBColor <<= 8;
		ARGBColor |= (B&0xFF);
		
		return ARGBColor;			
	}
	
	/**
	 * \brief This method handles byte of data in CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	final static public int byte2int(byte[] data, int offset ,int len){
		int rval = 0;
		for( int i = len - 1 ; i >= 0 ; i-- ){
			rval |= data[i]&0xff;
			rval <<= 8;
		}
		return rval;
	}
	
}
