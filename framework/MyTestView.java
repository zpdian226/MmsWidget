package android.widget;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.Xfermode;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.RemoteViews.RemoteView;


@RemoteView
public class MyTestView extends View /*
									 * SurfaceView implements
									 * SurfaceHolder.Callback
									 */{
	public interface OnProgressChangeListener {
		void onProgressChanged(View v, int progress, boolean fromUser);

		void onStartTrackingTouch(View v);

		void onStopTrackingTouch(View v);
	}

	public interface OnListItemClickListener {
		 void onItemClick(int index);
	}

	public interface RepeatListener {
		void onRepeat(View v, long duration, int repeatcount);
	}
	
	public interface RepeatOnOffListener {
		void onRepeatOn(View v);
		void onRepeatOff(View v);
	}
	
	public enum VerticalAlign {
		 TOP	 (0), 
		 MIDDLE  (1), 
		 BUTTOM  (2);
		 private VerticalAlign(int nativeInt) {
			 this.nativeInt = nativeInt;
		 }
		 final int nativeInt;
	 }

	private abstract class MyInternalView {
		protected int nPosX;
		protected int nPosY;
		protected boolean bInitPos;
		protected boolean bVisible;
		public void initData() {}
		public void setPos(int nPosX, int nPosY){this.nPosX=nPosX; this.nPosY=nPosY;bInitPos=true;}
		public void setVisible(boolean bFlag) {this.bVisible = bFlag;}
		public abstract void draw(Canvas canvas);
	}
	
	
	
	class MyImage extends MyInternalView implements Drawable.Callback {
		private Paint.Align mAlign;
		private VerticalAlign mVerticalAlign;
		private int vwidth;
		private int vheight;
		private int dwidth;
    	private int dheight;
		private int mLevel = 0;		
		private Drawable mDrawable = null;
		private Matrix mMatrix = new Matrix();;
		private Matrix mDrawMatrix = null;
		private RectF mTempSrc = new RectF();
    	private RectF mTempDst = new RectF();
		protected RectF dst = new RectF();
		protected boolean bInit;

		public MyImage(Paint.Align mAlign, VerticalAlign mVerticalAlign) {
			this.vwidth = 0;
			this.vheight = 0;
			this.mAlign = mAlign;
			this.mVerticalAlign = mVerticalAlign;
			this.bVisible = true;
			this.bInitPos = false;
			this.bInit = false;
			this.mDrawable = null;
		}

		public MyImage(int nPosX, int nPosY, int nWidth, int nHeight, 
			Paint.Align mAlign, VerticalAlign mVerticalAlign) {
			this.nPosX = nPosX;
			this.nPosY = nPosY;
			this.vwidth = nWidth;
			this.vheight = nHeight;
			this.mAlign = mAlign;
			this.mVerticalAlign = mVerticalAlign;
			this.bVisible = true;
			this.bInitPos = true;
			this.bInit = false;
			this.mDrawable = null;
		}
		
		public void initData() {
			if (!this.bInitPos)
				return;
			
			int nLeft;
			int nTop;

			if (Paint.Align.CENTER == this.mAlign) {
				nLeft = nPosX - vwidth/2;
			} else if (Paint.Align.RIGHT== this.mAlign) {
				nLeft = nPosX - vwidth;
			} else {
				nLeft = nPosX;
			}
			
			if (VerticalAlign.MIDDLE == this.mVerticalAlign) {
				nTop = nPosY - vheight/2;
			} else if (VerticalAlign.BUTTOM== this.mVerticalAlign) {
				nTop = nPosY - vheight;
			} else {
				nTop = nPosY;
			}
			dst.set(nLeft, nTop, nLeft+vwidth, nTop+vheight);
			this.bInit = true;

		}
		public void invalidateDrawable(Drawable who) {
		}
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
			MyTestView.this.scheduleDrawable(who, what, when);
		}
        public void unscheduleDrawable(Drawable who, Runnable what) {
			MyTestView.this.unscheduleDrawable(who, what);
		}
		private void updateDrawable(Drawable d) {
	        if (mDrawable != null) {
	            mDrawable.setCallback(null);
	            MyTestView.this.unscheduleDrawable(mDrawable);
	        }
	        mDrawable = d;
	        if (d != null) {
	            d.setCallback(this);
	            if (d.isStateful()) {
	                d.setState(MyTestView.this.getDrawableState());
	            }
	            d.setLevel(mLevel);
	            dwidth = d.getIntrinsicWidth();
	            dheight = d.getIntrinsicHeight();
				if (0 == vwidth && 0 == vheight) {
					vwidth = dwidth;
					vheight = dheight;
					this.initData();
				}
	            configureBounds();
	        }
	    }
		private void configureBounds() {
			if (mDrawable == null) {
				return;
			}		
			if (dwidth <= 0 || dheight <= 0 || (mDrawable instanceof NinePatchDrawable)) {
				mDrawable.setBounds(0, 0, vwidth, vheight);
				mDrawMatrix = null;
			} else {
				mDrawable.setBounds(0, 0, dwidth, dheight);
				mTempSrc.set(0, 0, dwidth, dheight);
				mTempDst.set(0, 0, vwidth, vheight);
				mDrawMatrix = mMatrix;
				mDrawMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
			}
		}
		public boolean isEnableDrawable() {return (null != mDrawable && 0 != dwidth && 0 != dheight);}
		public void setImageBitmap(Bitmap bm) {setImageDrawable(new BitmapDrawable(mResources, bm));}
		public void setImageResource(int resId) {setImageDrawable(loadDrawable(resId));}
		public void setImageDrawable(Drawable drawable) {
	        if (mDrawable != drawable) {
	            updateDrawable(drawable);
	        }
	    }
		public void setImageMatrix(Matrix matrix) {
	        if (matrix != null && matrix.isIdentity()) {
	            matrix = null;
	        }
	        if (matrix == null && !mMatrix.isIdentity() ||
	                matrix != null && !mMatrix.equals(matrix)) {
	            mMatrix.set(matrix);
	            configureBounds();
	        }
	    }
		public void setWH(int width, int height) {
			this.vwidth = width;
			this.vheight = height;
			this.initData();
			this.configureBounds();
		}
		public Bitmap getBitmap() {
			Bitmap bm = null;
			if (mDrawable instanceof BitmapDrawable) {
				bm = ((BitmapDrawable)mDrawable).getBitmap();

			}
			return bm;
		}
		public void draw(Canvas canvas) {
			if (this.bInit && this.bVisible) {
				if (null == mDrawable || 0 == dwidth || 0 == dheight) {
					return;
				}	
				if (mDrawMatrix == null && 0 == dst.left && 0 == dst.top) {
					mDrawable.draw(canvas);
				} else {
					int saveCount = canvas.getSaveCount();
					canvas.save();
					canvas.translate(dst.left, dst.top);
					if (mDrawMatrix != null) {
						canvas.concat(mDrawMatrix);
					}
					mDrawable.draw(canvas);
					canvas.restoreToCount(saveCount);
				}
			}
		}
	}

	class MyImageButton extends MyInternalView {
		private boolean 		bInit;
		private boolean 		mIsActivate;
		private boolean 		mIsLastActivate;
		private boolean 		mHasDrawActivate;
		private boolean 		mHasPerformedLongPress;
		private MyImage 		mImage;
		private MyImage 		mSelectImage;
		private OnClickListener mOnClickListener;
		private OnLongClickListener mOnLongClickListener;
		private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		public MyImageButton(Paint.Align mAlign, VerticalAlign mVerticalAlign) {
			bInit = false;
			bInitPos = false;
			mIsActivate = false;
			mIsLastActivate = false;
			mOnClickListener = null;
			mOnLongClickListener = null;
			mHasPerformedLongPress = false;
			mPaint.setColor(0xFFF1A807);
			mPaint.setStyle(Style.FILL);
			this.bVisible = true;
			mImage = new MyImage(mAlign, mVerticalAlign);
			mSelectImage = new MyImage(mAlign, mVerticalAlign);
		}
		
		public MyImageButton(int nPosX, int nPosY, int nWidth, int nHeight, 
			Paint.Align mAlign, VerticalAlign mVerticalAlign) {
			bInit = false;
			bInitPos = true;
			mIsActivate = false;
			mIsLastActivate = false;
			mOnClickListener = null;
			mOnLongClickListener = null;
			mHasPerformedLongPress = false;
			mPaint.setColor(0xFFF1A807);
			mPaint.setStyle(Style.FILL);
			this.bVisible = true;
			mImage = new MyImage(nPosX, nPosY, nWidth, nHeight, mAlign, mVerticalAlign);
			mSelectImage = new MyImage(nPosX, nPosY, nWidth, nHeight, mAlign, mVerticalAlign);
		}
		public void initData() {
			if (bInitPos) {
				mImage.initData();
				mSelectImage.initData();
				bInit = true;
			}
		}
		public void onClick(View v){
			if (!mHasPerformedLongPress && null != mOnClickListener) {
				MyTestView.this.playSoundEffect(SoundEffectConstants.CLICK);
				mOnClickListener.onClick(v);
			}
		}
		public void setPos(int nPosX, int nPosY){
			super.setPos(nPosX, nPosY);
			mImage.setPos(nPosX, nPosY);
			mSelectImage.setPos(nPosX, nPosY);
		}
		public void setWH(int width, int height) {
			mImage.setWH(width, height);
			mSelectImage.setWH(width, height);
		}
		public void setOnClickListener(OnClickListener l){this.mOnClickListener = l;}
		public void setOnLongClickListener(OnLongClickListener l){this.mOnLongClickListener = l;}
		public void setHasPerformedLongPress(boolean bFlag){this.mHasPerformedLongPress = bFlag;}
		public void setActivate(boolean bFlag) {
			this.mIsActivate = bFlag;
			if (!this.mIsLastActivate) {
				this.mIsLastActivate=bFlag;
			} else {
				if (mHasDrawActivate) {
					this.mIsLastActivate=bFlag;
				}	
			}
			if (bFlag) {
				mHasDrawActivate = false;
			}
		}
		public void setImageBitmap(Bitmap bm) {mImage.setImageBitmap(bm);}
		public void setImageResource(int resId) {mImage.setImageResource(resId);}	
		public void setImageDrawable(Drawable drawable) {mImage.setImageDrawable(drawable);}	
		public void setSelectImageBitmap(Bitmap bm) {mSelectImage.setImageBitmap(bm);}
		public void setSelectImageResource(int resId) {mSelectImage.setImageResource(resId);}	
		public void setSelectImageDrawable(Drawable drawable) {mSelectImage.setImageDrawable(drawable);}
		public boolean checkInRange(int x1, int y1) { return mImage.dst.contains(x1, y1); }
		public void draw(Canvas canvas) {
			if (this.bInit && this.bVisible) {
				if (this.mIsLastActivate) {
					if (mSelectImage.isEnableDrawable()) {
						mSelectImage.draw(canvas);
					} else {
						canvas.drawRoundRect(mImage.dst, 3, 3, mPaint);
						mImage.draw(canvas);
					}
					mHasDrawActivate = true;
				} else {
					mImage.draw(canvas);
				}
				mIsLastActivate = mIsActivate;				
			}
		}
	}
	
	class MyRepeatingImageButton extends MyImageButton implements OnLongClickListener {
		private long mStartTime;
	    private int mRepeatCount;
	    private RepeatListener mListener;
		private RepeatOnOffListener mOnOffListener;
	    private long mInterval = 500;

		MyRepeatingImageButton(Paint.Align mAlign, VerticalAlign mVerticalAlign) {
			super(mAlign, mVerticalAlign);
			setOnLongClickListener(this);
		}

		MyRepeatingImageButton(int nPosX, int nPosY, int nWidth, int nHeight, 
			Paint.Align mAlign, VerticalAlign mVerticalAlign) {
			super(nPosX, nPosY, nWidth, nHeight, mAlign, mVerticalAlign);
			setOnLongClickListener(this);
		}

	    public boolean onLongClick(View v) {
	        mStartTime = SystemClock.elapsedRealtime();
	        mRepeatCount = 0;
			if (mOnOffListener != null) {
				mOnOffListener.onRepeatOn(MyTestView.this);
			}
	        post(mRepeater);
			setHasPerformedLongPress(true);
	        return true;
	    }

		public void setRepeatListener(RepeatListener l, long interval) {
			mListener = l;
			mInterval = interval;
		}

		public void setRepeatOnOffListener(RepeatOnOffListener l) {
			mOnOffListener = l;
		}

		public void setActivate(boolean bFlag) {
			if (!bFlag) {
	            removeCallbacks(mRepeater);
				if (mOnOffListener != null) {
					mOnOffListener.onRepeatOff(MyTestView.this);
				}
	            if (mStartTime != 0) {
	                doRepeat(true);
	                mStartTime = 0;
	            }
			}
			super.setActivate(bFlag);
		}
    
	    private Runnable mRepeater = new Runnable() {
	        public void run() {
	            doRepeat(false);
	            if (isPressed()) {
	                postDelayed(this, mInterval);
	            }
	        }
	    };

	    private  void doRepeat(boolean last) {
	        long now = SystemClock.elapsedRealtime();
	        if (mListener != null) {
	            mListener.onRepeat(MyTestView.this, now - mStartTime, last ? -1 : mRepeatCount++);
	        }
	    }
	}
	
	class MyLabel extends MyInternalView {
		private String mSrcText;	
		private String mDestText;
		private int nMaxWidth;
		private int nLeft;
		private int nTop;
		private int nWidth;
		private int nHeight;
		private float fRealTextWidth;
		private boolean bInit;
		private boolean bInitFont;
		private boolean bMustAdjustText;
		private Paint.Align mAlign;
		private Paint mPaint;

		public MyLabel(Paint.Align mAlign) {
			this.mAlign = mAlign;
			this.bVisible = true;
			this.bInit = false;
			this.bInitFont = false;
			this.bInitPos = false;
			this.bMustAdjustText = true;
			this.fRealTextWidth = 0f;
			this.nWidth = 0;
			this.nHeight = 0;

			this.mPaint = new Paint();
			mPaint.setAntiAlias(true);
		}
		public MyLabel(int nMaxWidth, int nPosX, int nPosY, int nSize, int nColor, 
			Paint.Align mAlign, String text) {
			this.nMaxWidth = nMaxWidth;
			this.nPosX = nPosX;
			this.nPosY = nPosY;
			this.mAlign = mAlign;
			this.mSrcText = text;
			this.bVisible = true;
			this.bInit = false;
			this.bInitFont = true;
			this.bInitPos = true;
			this.bMustAdjustText = true;
			this.fRealTextWidth = 0f;
			this.nWidth = 0;
			this.nHeight = 0;

			this.mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(nSize);
			mPaint.setColor(nColor);
		}
		public void initData() {
			if (bInitFont) {
				if (bMustAdjustText) {
					if (null == mSrcText) {
						mSrcText = "";
					}
					
					String tmp = "...";
					int nTmpTextLength = tmp.length();

					float[] tmpwidths = new float[nTmpTextLength];
					int nTmpCount = mPaint.getTextWidths(tmp, 0, nTmpTextLength, tmpwidths);

					int nTextLength = mSrcText.length();			
					float[] widths = new float[nTextLength];
					int nCount = mPaint.getTextWidths(mSrcText, 0, nTextLength, widths);

					fRealTextWidth = 0;
					float fTmpMaxLength = nMaxWidth;
					int i;
					for (i=0; i<nCount; i++ ) {
						fRealTextWidth += widths[i];
					}

					if (fRealTextWidth > nMaxWidth)
					{
						for (i=0; i<nTmpCount; i++ ) {
							fTmpMaxLength = fTmpMaxLength - tmpwidths[i];
						}

						for (i=nTextLength-1; i>=0; i-- ) {
							fRealTextWidth = fRealTextWidth - widths[i];
							if (fRealTextWidth<fTmpMaxLength) {
								break;
							}
						}
						mDestText = mSrcText.substring(0, i+1);
						mDestText = mDestText + tmp;

						for (i=0; i<nTmpCount; i++ ) {
							fRealTextWidth = fRealTextWidth + tmpwidths[i];
						}
						nWidth = nMaxWidth;
					}
					else {
						mDestText = mSrcText;
						nWidth = (int)fRealTextWidth;
					}
					nHeight = mPaint.getFontMetricsInt(null);
					if (nMaxWidth > 0) {
						bMustAdjustText = false;
					}
				}
			}
			if (bInitPos && bMustAdjustText == false) {
				this.nTop = nPosY;
				if (Paint.Align.CENTER == this.mAlign) {
					nLeft = (int)(nPosX-fRealTextWidth/2);
				} else if (Paint.Align.RIGHT== this.mAlign) {
					nLeft = (int)(nPosX-fRealTextWidth);
				} else {
					nLeft = nPosX;
				}
				this.bInit = true;
			}
		}

		public void setText(int resid) {setText(mResources.getText(resid).toString());}
		public void setText(String text){this.mSrcText=text; this.bMustAdjustText=true; initData();}
		public void setTextSize(int nSize) {mPaint.setTextSize(nSize);bInitFont=true;}
		public void setTextColor(int nColor) {mPaint.setColor(nColor);}
		public void setMaxWidth(int nMaxWidth){this.nMaxWidth = nMaxWidth;}
		public int getTextWidth(){return this.nWidth;}
		public int getTextHeight(){return this.nHeight;}
		public void draw(Canvas canvas) {
			if (this.bInit && this.bVisible) {
				canvas.drawText(mDestText, nLeft, nTop + this.nHeight, mPaint);
			}
		}
	}
	
	class MyToolTip extends MyInternalView {
		private int nPaddingLeft;
		private int nPaddingTop;
		private int nPaddingRight;
		private int nPaddingBottom;
		private int nMinWidth;
		private boolean bInit;
		private RectF dst = new RectF();
		private Paint.Align mAlign;
		private MyLabel myLabel = null;
		private MyImage myImage = null;
		private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		public MyToolTip(Paint.Align align) {
			nPaddingLeft = 3;
			nPaddingTop = 3;
			nPaddingRight = 3;
			nPaddingBottom = 3;
			mAlign = align;
			
			myLabel = new MyLabel(align);
			bInit = false;
			bInitPos = false;
			nMinWidth = -1;
			mPaint.setColor(0xff1cd975);
		}

		public MyToolTip(int nMaxWidth, int nPosX, int nPosY, int nSize, int nColor, 
			Paint.Align align, String text) {
			nPaddingLeft = 3;
			nPaddingTop = 3;
			nPaddingRight = 3;
			nPaddingBottom = 3;
			mAlign = align;
			
			int posX;
			if (Paint.Align.CENTER == mAlign) {
				posX = nPosX;
			} else if (Paint.Align.RIGHT== mAlign) {
				posX = nPosX - nPaddingRight;
			} else {
				posX = nPosX + nPaddingLeft;
			}
			myLabel = new MyLabel(nMaxWidth, posX, nPosY+nPaddingTop, nSize, nColor, align, text);
			bInit = false;
			bInitPos = true;
			nMinWidth = -1;
			mPaint.setColor(0xff1cd975);
		}
		public void initData() {if (bInitPos) {myLabel.initData();adjustToolTip(); bInit=true;}}
		public void setPos(int x, int y){
			super.setPos(x, y);
			int posX;
			if (Paint.Align.CENTER == mAlign) {
				posX = nPosX;
			} else if (Paint.Align.RIGHT== mAlign) {
				posX = nPosX - nPaddingRight;
			} else {
				posX = nPosX + nPaddingLeft;
			}			
			myLabel.setPos(posX, y+nPaddingTop);
			if (null != myImage) {
				myImage.setPos(x, y);
			}
		}
		public void setText(int resid) {setText(mResources.getText(resid).toString());}
		public void setText(String text){
			myLabel.setText(text);
			if (bInitPos) {
				adjustToolTip();
				bInit=true;
			}
		}
		public void setTextSize(int nSize) {myLabel.setTextSize(nSize);}
		public void setTextColor(int nColor) {myLabel.setTextColor(nColor);}
		public void setPadding(int left, int top, int right, int bottom){
			nPaddingLeft = left;
			nPaddingTop = top;
			nPaddingRight = right;
			nPaddingBottom = bottom;
			int posX;
			if (Paint.Align.CENTER == mAlign) {
				posX = nPosX;
			} else if (Paint.Align.RIGHT== mAlign) {
				posX = nPosX - nPaddingRight;
			} else {
				posX = nPosX + nPaddingLeft;
			}			
			myLabel.setPos(posX, nPosY+nPaddingTop);
			myLabel.initData();
			if (bInitPos) {
				adjustToolTip();
				bInit=true;	
			}
		}
		public void setMinWidth(int nMinWidth){this.nMinWidth = nMinWidth;}
		public void setMaxWidth(int nMaxWidth){myLabel.setMaxWidth(nMaxWidth);}
		public void setImageResource(int resId) {setImageDrawable(loadDrawable(resId));}
		public void setImageDrawable(Drawable drawable) {
			if (null == myImage) {
				myImage = new MyImage(0, 0, 1, 1, mAlign, VerticalAlign.TOP);
				myImage.setPos(nPosX, nPosY);
				if (this.bInit) {
					int width = myLabel.getTextWidth() + nPaddingLeft + nPaddingRight;
					width = width < nMinWidth ? nMinWidth : width;
					myImage.setWH(width, myLabel.getTextHeight() + nPaddingTop + nPaddingBottom);
				}
			}
			myImage.setImageDrawable(drawable);
	    }
		private void adjustToolTip() {
			int width = myLabel.getTextWidth() + nPaddingLeft + nPaddingRight;
			width = width < nMinWidth ? nMinWidth : width;
			if (null != myImage) {
				myImage.setWH(width, myLabel.getTextHeight() + nPaddingTop + nPaddingBottom);
			} else {
				int left;
				if (Paint.Align.CENTER == mAlign) {
					left = nPosX - width/2;
				} else if (Paint.Align.RIGHT== mAlign) {
					left = nPosX - width;
				} else {
					left = nPosX;
				}
				dst.set(left, nPosY, left + width, 
					nPosY + myLabel.getTextHeight() + nPaddingTop + nPaddingBottom);
			}
		}
		public void draw(Canvas canvas) {
			if (this.bInit && this.bVisible) {
				if (null == myImage) {
					canvas.drawRect(dst, mPaint);
				} else {
					myImage.draw(canvas);
				}
				myLabel.draw(canvas);
			}
		}
	}


	public class MyListView extends MyInternalView {
		
		int l,t,r,b;
		
		float scrollY = 0;
		float offsetY = 0;
		final static int ITEM_HEIGHT = 44;
		final static int SELECT_ITEM_HEIGHT = 76;
		final static int DEFAULT_LIST_HEIGHT = 228;
		final static int DEFAULT_LIST_WIDTH = 296;
		final static int SPACEING_HEIGHT = 5;
		final static int BIG_FONT_SIZE = 24;
		final static int NORMAL_FONT_SIZE = 16;
		final static int OFFSET_TO_CENTER_Y = 7;
		final static int BORDER_HEIGHT = 50;

		int nCurrentItemHeight;
		int nCurrentSelectItemHeight;
		int nCurrentSpaceingHeight;
		int nCurrentBigFontSize;
		int nCurrentBigFontOffsetX;
		int nCurrentNormalFontSize;
		int nCurrentNormalFontOffsetX;
		int nCurrentOffsetToCenterY;
		int nCurrentBorderHeight;
		int nNormalLeft;
		int nNormalTop;
		int nBigLeft;
		int nBigTop;
		int nFocusIndex = -1;
		int nSelectedIndex = -1;
		int nListHeight = DEFAULT_LIST_HEIGHT;
		boolean bHasSetSelImage = false;
		boolean bInit = false;
		boolean bMustSetScrollY = false;
		TextPaint normalPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		TextPaint bigPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		Drawable selectBackDraw;
		Rect topBorderRect = new Rect();
		Rect bottomBorderRect = new Rect();
		Shader topShader = null;
		Shader bottomShader = null;
		private OnListItemClickListener mOnListItemClickListener= null;	
		private OnLongClickListener mOnListLongClickListener = new OnLongClickListener() {
			public boolean onLongClick(View v) {
				return true;
			}
		};
		
		public MyListView(Context con){
			normalPaint.setColor(Color.WHITE);
			bigPaint.setColor(Color.WHITE);
		}

		public void initData() {
			nNormalLeft = (int)(fCurrentGlobalScale * 28);
			nNormalTop = (int)(fCurrentGlobalScale * 1);
			nBigLeft = (int)(fCurrentGlobalScale * 16);
			nBigTop = (int)(fCurrentGlobalScale * 5);

			nCurrentItemHeight = (int)(fCurrentGlobalScale * ITEM_HEIGHT);
			nCurrentSelectItemHeight = (int)(fCurrentGlobalScale * SELECT_ITEM_HEIGHT);
			nCurrentSpaceingHeight = (int)(fCurrentGlobalScale * SPACEING_HEIGHT);
			nCurrentBigFontSize = (int)(fCurrentGlobalScale * BIG_FONT_SIZE);
			nCurrentBigFontOffsetX = (int)(fCurrentGlobalScale * 95);
			nCurrentNormalFontSize = (int)(fCurrentGlobalScale * NORMAL_FONT_SIZE);
			nCurrentNormalFontOffsetX = (int)(fCurrentGlobalScale * 85);
			nCurrentOffsetToCenterY = (int)(fCurrentGlobalScale * OFFSET_TO_CENTER_Y);
			nCurrentBorderHeight = (int)(fCurrentGlobalScale * BORDER_HEIGHT);

			normalPaint.setTextSize(nCurrentNormalFontSize);
			bigPaint.setTextSize(nCurrentBigFontSize);

			r = nViewWidth - (int)(fCurrentGlobalScale * 12);
			l = nPosX;
			t = nPosY;
			r = l + (int)(fCurrentGlobalScale * DEFAULT_LIST_WIDTH);
			b = t + nListHeight;

			topBorderRect.set(l, t, r, t+nCurrentBorderHeight);
			bottomBorderRect.set(l, b-nCurrentBorderHeight, r, b);

			topShader = new LinearGradient(l, t, l, t+nCurrentBorderHeight, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
			bottomShader = new LinearGradient(l, b-nCurrentBorderHeight, l, b, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);

			if (bMustSetScrollY) {
				float nNewOffsetY = nSelectedIndex * (nCurrentItemHeight + nCurrentSpaceingHeight) + nCurrentSelectItemHeight * 0.5f;
				if (nCurrentStatus == STATUS_LIBRARY) {
					scrollY = nNewOffsetY;
					isScroll = true;
				} else {
					scrollY = 0;
					isScroll = false;
				}
				offsetY = nNewOffsetY;
				bMustSetScrollY = false;
			}
			bInit = true;
		}
		
		public void setHeight(int h) {
	        nListHeight = h;
	    }

		public void setOnListItemClickListener(OnListItemClickListener listener) {
	        mOnListItemClickListener = listener;
	    }

		public void setFocusIndex(int index) {
			if (index < -1 || index >= mMusicList.size()) {
				Log.w(TAG, "setFocusIndex:index out of range; index="+index);
				return;
			}
				
			if (nFocusIndex != index) {				
				MusicInfo info = null;
				if (-1 != nFocusIndex) {
					info = mMusicList.get(nFocusIndex);  // lost focus
					info.adjustedTitle = null;
				}
				
				bHasSetSelImage = false;
				nFocusIndex = index;
				nSelectedIndex = index;

				if (-1 == index) {
					offsetY = 0;
				} else {
					if (bInit) {
						float nNewOffsetY = nSelectedIndex * (nCurrentItemHeight + nCurrentSpaceingHeight) + nCurrentSelectItemHeight * 0.5f;
						if (nCurrentStatus == STATUS_LIBRARY) {
							scrollY = nNewOffsetY - offsetY + scrollY;
							isScroll = true;
						} else {
							scrollY = 0;
							isScroll = false;
						}
						offsetY = nNewOffsetY;	
					} else {
						bMustSetScrollY = true;
					}
	
					info = mMusicList.get(nFocusIndex);  // get focus
					info.adjustedTitle = null;
				}
			}
		}

		public void resetSelImageInitFlag() {
			bHasSetSelImage = true;
		}

		public void resetScroll() {
			scrollY = 0;
			isScroll = false;
		}

		public int getFocusIndex() {
			return nFocusIndex;
		}

		public int getSelectedIndex() {
			return nSelectedIndex;
		}

		public Bitmap getIndexBitmap(int index) {
			if (index >=0 && index < mMusicList.size()) {
				return mMusicList.get(index).getBitmap();
			} else {
				return null;
			}
		}
		
		public void draw(Canvas c){
			if (bInit) {
				c.save();
				//Log.d("AAA", "spreed = " + spreed + "; scrollY = " + scrollY + " moveY = " + moveY + " isScroll = " + isScroll);
				if (selectBackDraw == null && mSelectListBackRes != 0){
					selectBackDraw = getContext().getResources().getDrawable(mSelectListBackRes);
					Rect padding = new Rect();
					selectBackDraw.getPadding(padding);				
					selectBackDraw.setBounds(-padding.left, -padding.top, r - l + padding.right, nCurrentSelectItemHeight + padding.bottom);
				}
				c.clipRect(l, t, r, b);
				run();
	//			  c.drawColor(Color.BLUE);
				c.translate(l ,t + scrollY - (offsetY - nListHeight * 0.5f) - nCurrentOffsetToCenterY);
				float offsetTop = offsetY - nListHeight * 0.5f - scrollY;
				float offsetBottom = offsetY + nListHeight * 0.5f - scrollY;
				int itemY = 0;		
				int itemTop;
				int itemBottom;
				int size = 0;
				synchronized (mMusicList) {
					size = mMusicList.size();

					for (int i = 0; i < size; i++){
						boolean isSelect = i == nFocusIndex;
						int itemHight = isSelect ? nCurrentSelectItemHeight : nCurrentItemHeight ;
						itemTop = itemY;
						itemBottom = itemTop + itemHight;
						
						if (itemTop > offsetBottom){
							break;
						}
						if (itemBottom > offsetTop){
							drawItem(mMusicList.get(i), isSelect, c);
						}
						/*if (isSelect) {
							int selleft = l+nBigLeft;
							int seltop = (int)(t + scrollY - (offsetY - nListHeight * 0.5f) - nCurrentOffsetToCenterY + itemY + nBigTop);
							Log.v(TAG, "myListView:selleft="+selleft+"; seltop="+seltop );
						}*/
						itemY += itemHight + nCurrentSpaceingHeight;
						c.translate(0 , itemHight + nCurrentSpaceingHeight);
					}
				}
				c.restore();
				
				if (size > 0) {
					mPaint.setShader(topShader);
					c.drawRect(topBorderRect, mPaint);
					mPaint.setShader(bottomShader);
					c.drawRect(bottomBorderRect, mPaint);
					mPaint.setShader(null);
				}
			}
		}
		
		public void drawAnimation(Rect cdRect){
			MusicInfo info = mMusicList.get(nFocusIndex);
		}
		public void drawAnimationListMove(int frame, Canvas c){
		}
		
		public void drawItem(MusicInfo info, boolean isSelect, Canvas c) {
			c.save();
			Paint paint;
			float stringX;
			float stringY;
			
			if (isSelect){
				stringX = nCurrentBigFontOffsetX;
				stringY = (nCurrentSelectItemHeight + nCurrentBigFontSize/2)/2;
				paint = bigPaint;

				if (selectBackDraw != null){
					selectBackDraw.draw(c);
				}

				if (bHasSetSelImage) {
					if (null != mSelScaleCDBitmap) {
						c.drawBitmap(mSelScaleCDBitmap, nBigLeft, nBigTop, paint);
					} else {
						if (null != mDefaultCDBitmap) {
							final int nShort = 2*nCurrentSelScaleCDRadius;
							mDrawSrcRect.set(0, 0, mDefaultCDBitmap.getWidth(), mDefaultCDBitmap.getHeight());
							mDrawDstRectF.set(nBigLeft, nBigTop, nBigLeft+nShort, nBigTop+nShort);
							c.drawBitmap(mDefaultCDBitmap, mDrawSrcRect, mDrawDstRectF, paint);
							if (null != mLightBitmap) {
								mDrawSrcRect.set(0, 0, mLightBitmap.getWidth(), mLightBitmap.getHeight());
								c.drawBitmap(mLightBitmap, mDrawSrcRect, mDrawDstRectF, paint);
							}
						}
					}
				} else {
					Bitmap bm = info.getBitmap();
					if (null != bm) {
						final int nShort = 2*nCurrentSelScaleCDRadius;
						mDrawSrcRect.set(0, 0, bm.getWidth(), bm.getHeight());
						mDrawDstRectF.set(nBigLeft, nBigTop, nBigLeft+nShort, nBigTop+nShort);
						c.drawBitmap(bm, mDrawSrcRect, mDrawDstRectF, null);
					}
				}
			}else{
				stringX = nCurrentNormalFontOffsetX;
				stringY = (nCurrentItemHeight + nCurrentNormalFontSize/2)/2;
				paint = normalPaint;
				
				Bitmap bm = info.getBitmap();
				if (null != bm) {
					c.drawBitmap(bm, nNormalLeft, nNormalTop, null);
				}
			}

			if (null == info.adjustedTitle) {
				info.adjustedTitle = getAdjustedText(info.title, paint, 0.9f * (r-l-stringX));
			}
			if (null != info.adjustedTitle) {
				c.drawText(info.adjustedTitle, stringX, stringY, paint);
			}
			c.restore();
		}
		
		float moveY = Float.NaN;
		float clickY = Float.NaN;		
		float speed;

		boolean isScroll;
		boolean isTap;
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			if ((x < l || x > r || y < t || y > b) && Float.isNaN(moveY)) {
				return false;
			}
			//Log.d("AAA", event.toString());

			switch (event.getAction()) {
				case MotionEvent.ACTION_CANCEL:
					mOnLongClickListener = null;
					break;
					
				case MotionEvent.ACTION_DOWN: {
					mOnLongClickListener = this.mOnListLongClickListener;
					isScroll = false;
					isTap = true;
					speed = 0;
					moveY = y;
					clickY = y;
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					float realTop = nListHeight * 0.5f - offsetY + scrollY - nCurrentOffsetToCenterY; 
					float realBottom = realTop + getEndY();
					if ((realTop >= 0 && y > moveY) || (realBottom<= nListHeight && y < moveY)) {
					} else {
						if (Float.isNaN(moveY)){
							isScroll = false;
							speed = 0;
						} else {
							if (y > moveY && realTop + y - moveY >= 0) {
								scrollY += -realTop;
								speed = -realTop;
							} else if (y < moveY && realBottom  + y - moveY <= nListHeight) {
								scrollY += nListHeight - realBottom;
								speed = nListHeight - realBottom;
							} else {
								scrollY += (y - moveY);
								speed = y - moveY;
							}
						}
					}
					if (!Float.isNaN(moveY) && Math.abs(y - moveY) > nTouchSlop){
						isTap = false;
					}
					if (!Float.isNaN(clickY) && Math.abs(y - clickY) > nTouchSlop) {
						isTap = false;
					}
					moveY = y;

					break;
				}
				case MotionEvent.ACTION_UP: {
					mOnLongClickListener = null;
					if (Float.isNaN(moveY)){
						return false;
					}
					if (isTap) {
						click(x, y);
					}
					isScroll = true;
					isTap = false;
					moveY = Float.NaN;
					clickY = Float.NaN;
					break;
				}

			}
			return true;
		}
		
		public void click(float x, float y){
			float realY = offsetY - nListHeight * 0.5f - scrollY - t + y + nCurrentOffsetToCenterY;
			int itemY = 0;		
			int itemTop;
			int itemBottom;
			int size = mMusicList.size();
			nSelectedIndex  = -1;
			
			for (int i = 0; i < size; i++){
				boolean isSelect = i == nFocusIndex;
				int itemHight = isSelect ? nCurrentSelectItemHeight : nCurrentItemHeight ;
				itemTop = itemY;
				itemBottom = itemTop + itemHight;
				//Log.v(TAG, "Click: Index = "+i+"; realY ="+realY+"; itemTop ="+itemTop+"; itemBottom ="+itemBottom);
				if (realY >= itemTop && realY < itemBottom) {
					nSelectedIndex = i; 
					break;
				}
				itemY += itemHight + nCurrentSpaceingHeight;
			}

			if (nSelectedIndex == nFocusIndex && -1 != nSelectedIndex) {
				scrollY = 0;
				bPlayingAnimation = false;
				nCurrentStatus = STATUS_PANEL;
				nAnimCurrentHalfPanelWidth = nAnimMaxHalfPanelWidth;
				startDelayedClosePanelHandler();
			} else if (-1 == nSelectedIndex) {
				nSelectedIndex = nFocusIndex;
			} else {
				setFocusIndex(nSelectedIndex);
				if (null != mOnListItemClickListener) {
					mOnListItemClickListener.onItemClick(nSelectedIndex);
				}
			}
		}
		public void run(){
			if (!isScroll){
				return;
			}

			if (speed == 0) {
				if (scrollY > 0) {
					scrollY -= Math.max(scrollY/6, 2);
					scrollY = Math.max(0, scrollY);
				} else if (scrollY < 0) {
					scrollY -= Math.min(scrollY/6, -2);
					scrollY = Math.min(0, scrollY);
				}else{
					isScroll = false;
					scrollY = 0;
					return;
				}
				return;
			}
			int sign = speed > 0 ? 1: -1;
			scrollY += sign * (Math.abs(speed) > 30 ? 30 : Math.abs(speed));
			speed -= sign  * (Math.abs(speed) > 5 ? 3 : 1);
			if (Math.abs(speed) < 1 || getEndY() < -scrollY || scrollY > -nListHeight){
				speed = 0;
				isScroll = false;
			}
		}
		
		private int getEndY(){
		  return mMusicList.size() * (nCurrentItemHeight + nCurrentSpaceingHeight) + (nFocusIndex >= 0 ? nCurrentSelectItemHeight - nCurrentItemHeight : 0);  
		}

	}




	public static final int STATUS_PLAYING = 0x00010000;
	public static final int STATUS_PANEL = 0x00020000;
	public static final int STATUS_LIBRARY = 0x00040000;
	public static final int STATUS_CHOOSE = 0x00080000;

	public static final int ANIMATION_MASK = 0x0000ffff;
	public static final int ANIMATION_0 = 0x00000001;
	public static final int ANIMATION_1 = 0x00000002;
	public static final int ANIMATION_2 = 0x00000003;
	public static final int ANIMATION_3 = 0x00000004;
	public static final int ANIMATION_4 = 0x00000005;
	public static final int ANIMATION_5 = 0x00000006;

	private static final double dTwoPI = Math.PI * 2;
	private static final double dRotatePer = 1.0f;


	private static final int nAnimDefaultMinHalfPanelWidth = 58;
	private static final int nAnimDefaultMaxHalfPanelWidth = 145;
	private static final int nAnimDefaultMaxHalfPanelHeight = 29;
	private static final int nAnimDefaultPanelMoveSpeed = 25;

	private static final int nAnimation3DefaultCDScaleSpeed = 10;
	
	private static final int nAnimation4DefaultCDOffsetX = 99;
	private static final int nAnimation4DefaultCDOffsetSpeed = 20;

	private static final int nAnimation5DefaultCDOffsetY = 66;
	private static final int nAnimation5DefaultCDOffsetSpeed = 10;

	private static final int PANEL_IDLE_DELAY = 6000;
	private static final int nDefaultCDMargin = 5;
	private static final int nDefaultSelScaleCDRadius = 33;
	private static final int nDefaultScaleCDRadius = 21;
	private static final int nDefaultCDRadius = 125;
	private static final int nRadiusLists[] = new int[] {320, 125, 240, 95, 500, 190, 480, 190};
	private static final int nBackButtonRadiusLists[] = new int[] {15, 11, 22, 22};
	private static final String strMyLauncherAppWidgetHostViewClassName = "class com.android.launcher.LauncherAppWidgetHostView";
	private static final String strMyCellLayoutClassName = "class com.android.launcher.CellLayout";
	private static final String strMyWorkspaceClassName = "class com.android.launcher.Workspace";
	private static final String strMyDragLayerClassName = "class com.android.launcher.DragLayer";
	private static final String strMyLauncherClassName = "class com.android.launcher.Launcher";
	private static final String TAG = "Music3_MyTestView";
	
	private static final int ADD_MUSIC_INFO = 1;
	private static final int LOAD_CURRENT_MUSIC_INFO = 2;
	private static final int SET_CD_BG = 3;
	private static final int SET_CD_DEFAULT = 4;
	private static final int SET_CD_LIGHT = 5;
	private static final int SET_CD_MASK = 6;
	private static final int CLEAR_ARTWORK = 7;
	private static final int SET_PLAY_POS = 8;

	private static String mUpdateAction = null;
	private static String mStopServiceAction = null;
	private static int nLastViewMode = 0;  // 0 - uninit, 1 - vertical, 2 - Horizontal
	private static ArrayList<MyTestView> mInstances;


	private ViewGroup mLauncherApp = null;
	private ViewGroup mCellLayout = null;
	private ViewGroup mWorkspace = null;
	private ViewGroup mDragLayer = null;
	
	private Canvas mCanvas = null;
	private Paint mPaint= null;
	private Resources mResources = null;
	private Matrix mMatrix = null;
	private final Xfermode mModes = new PorterDuffXfermode(PorterDuff.Mode.XOR);
	private final Rect mAdjustSrcRect = new Rect(); // for create or adjust bitmap thread
	private final RectF mAdjustDstRectF = new RectF();
	private final Rect mDrawSrcRect = new Rect(); // for draw bitmap thread
	private final RectF mDrawDstRectF = new RectF();
	private final Path mPath = new Path();
	private final Path mScalePath = new Path();
	private final Path mSelScalePath = new Path();
	
	private Bitmap mSCBitmap = null;	
	private Bitmap mAlbumBackgroundBitmap = null;
	//private Bitmap mAlbumBackgroundScaleBitmap = null;
	private Bitmap mDefaultCDBitmap = null;
	private Bitmap mDefaultCDScaleBitmap = null;
	private Bitmap mLightBitmap = null;
	private Bitmap mLightScaleBitmap = null;
	private Bitmap mMaskBitmap = null;
	private Bitmap mMaskScaleBitmap = null;
	private Bitmap mSelScaleCDBitmap = null;

	private boolean bIsHorizontal = false;
	private boolean bSlip = false;
	private boolean bAllowDrag = false;
	private boolean bStartDrag = false;	
	private boolean bCreatedWindow = false;
	private boolean bInit = false;
	private boolean bPlaying = false;
	private boolean bPlayingAnimation = false;
	private boolean bPlaybackComplete = true;
	private boolean bMustDraw = false;
	private	int nTouchSlop;
	private int nDrawCount;
	private int nCurrentGraphMode;
	private int nCurrentStatus;
	private int nCurrentBgAngle;
	private int nCurrentRotationalSpeed;
	private int nViewWidth = 0;
	private int nViewHeight = 0;
	private int nViewShort;
	private int nCurrentSelScaleCDRadius = 0;
	private int nCurrentScaleCDRadius = 0;
	private int nCurrentCDRadius = 0;
	private int nCenterX;
	private int nCenterY;
	private int nMaxProgress;
	private int nLastScrollX;
	private int nScrollDirection;	
	
	private double dCurrentProgress = 0;

	private float fCurrentGlobalScale = 0f;

	private int nBackButtonVerticalOffsetX;
	private int nBackButtonVerticalOffsetY;
	private int nBackButtonHorizontalOffsetX;
	private int nBackButtonHorizontalOffsetY;
	private int nBackButtonRadius;
	private int nBackButtonCheckRange;

	private float fCircleProgressBarWidth;
	private float fCircleProgressBarRadius;
	private int nCDCenterImageRadius;
	
	private int nAnimMinHalfPanelWidth;
	private int nAnimMaxHalfPanelWidth;
	private int nAnimMaxHalfPanelHeight;
	private int nAnimCurrentHalfPanelWidth;
	private int nAnimCurrentPanelMoveSpeed;

	private int nAnimation0Left;
	private int nAnimation0ImgOffsetX;
	private int nAnimation0ImgOffsetY;
	private int nAnimation0ImgHeight;
	private int nAnimation0HalfImgWidth;
	private int nAnimation0ImgWidth;
	
	private float fAnimation3CDRadiusScale = 1.0f;
	private int nAnimation3CDScaleSpeed;
	private int nAnimation3CDRadius;
	
	private int nAnimation4MaxCDOffsetX;
	private int nAnimation4CDOffsetX;
	private int nAnimation4CDOffsetSpeed;

	private int nAnimation5MaxCDOffsetY;
	private int nAnimation5CDOffsetY;
	private int nAnimation5CDOffsetSpeed;
	
	private OnProgressChangeListener mOnProgressChangeListener;
	private OnClickListener mOnPlayPauseClickListener;
	private OnLongClickListener mOnLongClickListener;
	private OnLongClickListener mDefaultOnLongClickListener;
	private RepeatOnOffListener mPreviousOnOffListener = new RepeatOnOffListener() {
		public void onRepeatOn(View v) {nCurrentRotationalSpeed=-20;mProgressMsg.setVisible(true);}
		public void onRepeatOff(View v) {nCurrentRotationalSpeed=3;mProgressMsg.setVisible(false);}
	};
	private RepeatOnOffListener mNextOnOffListener = new RepeatOnOffListener() {
		public void onRepeatOn(View v) {nCurrentRotationalSpeed=20;mProgressMsg.setVisible(true);}
		public void onRepeatOff(View v) {nCurrentRotationalSpeed=3;mProgressMsg.setVisible(false);}
	};

	private MusicInfo mCurrCDInfo;
	private MusicInfo mLastCDInfo;

	private MyLabel mTitle;
	private MyLabel mArtist;
	private MyLabel mLibraryButtonTxt;
	private MyLabel mCurrTitle;

	private MyToolTip mPlayHelperMsg;
	private MyToolTip mProgressMsg;

	private MyImage mPlayCtrlImage;
	private MyImage mPlayPanelImage;
	private MyImage mProgressBarImage;
	private MyImage mProgressBar2BgImage;
	private MyImage mProgressBar2Image;

	private MyImageButton mPlayCtrlButton;
	private MyImageButton mPlayListButton;
	private MyImageButton mShuffleButton;
	private MyImageButton mRepeatButton;
	private MyImageButton mLibraryButton;
	private MyImageButton mBackButton;
	private MyRepeatingImageButton mPreviousButton;
	private MyRepeatingImageButton mNextButton;

	private MyListView mlist;

	private MyMusicUtils myUtils = new MyMusicUtils();
	private Vector<MusicInfo> mMusicList = new Vector<MusicInfo>();
	private int mSelectListBackRes;
	private long nLastSongId = -1;
	


	public MyTestView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		// getHolder().addCallback(this);
		setDrawingCacheEnabled(true);
		initRes(context);
	}

	public MyTestView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		// getHolder().addCallback(this);
		setDrawingCacheEnabled(true);
		initRes(context);
	}

	public MyTestView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		// getHolder().addCallback(this);
		setDrawingCacheEnabled(true);
		initRes(context);
	}
	

	private void requestUpdate(Context context) {
		int nNewViewMode = bIsHorizontal ? 2 : 1;
		if (0 != nLastViewMode && nLastViewMode != nNewViewMode) {
			nLastViewMode = nNewViewMode;
			if (null !=mUpdateAction && !mUpdateAction.equals("")) {
				Intent updateIntent = new Intent(mUpdateAction);
				updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
				context.sendBroadcast(updateIntent);
				Log.v(TAG, "requestUpdate:action=[" + mUpdateAction+"]");
			}
		} else {
			nLastViewMode = nNewViewMode;
		}
	}

	private void printStack() {
		try{
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Bitmap loadBitmap(int resId) {
		Bitmap b = null;
		if (null != mResources) {
			b = BitmapFactory.decodeResource(mResources, resId);
		}
		return b;
	}
	private Drawable loadDrawable(int resId) {
		Drawable d = null;
		if (null != mResources) {
            try {
                d = mResources.getDrawable(resId);
            } catch (Exception e) {
                Log.v(TAG, "Unable to find resource: " + resId, e);
            }

		}
		return d;
	}
	
	private void initRes(Context context) {
		Log.v(TAG, "initRes:"+context.getClass().toString());
		mResources = context.getResources();

		mMatrix = new Matrix();
		mTitle = new MyLabel(Paint.Align.CENTER);
		mArtist = new MyLabel(Paint.Align.CENTER);
		mPlayHelperMsg = new MyToolTip(Paint.Align.CENTER);
		mPlayHelperMsg.setVisible(false);		
		mProgressMsg = new MyToolTip(Paint.Align.LEFT) {
			public void setVisible(boolean bFlag) {
				if (bFlag) {
					resetProgressMsgInfo();
				}
				this.bVisible = bFlag;
			}
		};
		mProgressMsg.setVisible(false);
		mCurrTitle = new MyLabel(Paint.Align.CENTER);
		mPlayPanelImage = new MyImage(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mPlayCtrlImage = new MyImage(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mProgressBarImage = new MyImage(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mPlayCtrlButton = new MyImageButton(Paint.Align.CENTER, VerticalAlign.MIDDLE) {
			public boolean checkInRange(int x1, int y1) { 
				final int x = x1 > nCenterX ? x1 - nCenterX : nCenterX - x1;
				final int y = y1 > nCenterY ? y1 - nCenterY : nCenterY - y1;

				if (x * x + y * y < nCDCenterImageRadius* nCDCenterImageRadius) {
					return true;
				} else {
					return false;
				}
			}
		};
		mPlayCtrlButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if (!bPlaybackComplete) {
					nCurrentStatus = STATUS_PANEL | ANIMATION_0;
					bPlayingAnimation = true;
				} else {
					nCurrentStatus = STATUS_LIBRARY;
					bPlayingAnimation = false;
				}
			}
		});
		mPlayListButton = new MyImageButton(Paint.Align.CENTER, VerticalAlign.MIDDLE) {
			public boolean checkInRange(int x1, int y1) { 
				final int x = x1 > nCenterX ? x1 - nCenterX : nCenterX - x1;
				final int y = y1 > nCenterY ? y1 - nCenterY : nCenterY - y1;

				if (x * x + y * y < nCDCenterImageRadius * nCDCenterImageRadius) {
					return true;
				} else {
					return false;
				}
			}
		};
		mPlayListButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				stopDelayedClosePanelHandler();
				nCurrentStatus = STATUS_LIBRARY | ANIMATION_2;
				bPlayingAnimation = true;
			}
		});
		mShuffleButton = new MyImageButton(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mPreviousButton = new MyRepeatingImageButton(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mPreviousButton.setRepeatOnOffListener(mPreviousOnOffListener);
		mNextButton = new MyRepeatingImageButton(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mNextButton.setRepeatOnOffListener(mNextOnOffListener);
		mRepeatButton = new MyImageButton(Paint.Align.CENTER, VerticalAlign.MIDDLE);
		mLibraryButton = new MyImageButton(Paint.Align.CENTER, VerticalAlign.TOP);
		mLibraryButtonTxt = new MyLabel(Paint.Align.CENTER);
		mProgressBar2BgImage = new MyImage(Paint.Align.CENTER, VerticalAlign.TOP);
		mProgressBar2Image = new MyImage(Paint.Align.CENTER, VerticalAlign.TOP);
		mBackButton = new MyImageButton(Paint.Align.CENTER, VerticalAlign.TOP) {
			public boolean checkInRange(int x1, int y1) {
				int x2;
				int y2;
				if (bIsHorizontal) {
					x2 = nBackButtonHorizontalOffsetX;
					y2 = nBackButtonHorizontalOffsetY + nBackButtonRadius;
				} else {
					x2 = nBackButtonVerticalOffsetX;
					y2 = nBackButtonVerticalOffsetY + nBackButtonRadius;
				}

				int x = x1 > x2 ? x1 - x2 : x2 - x1;
				int y = y1 > y2 ? y1 - y2 : y2 - y1;

				if (x * x + y * y < nBackButtonCheckRange * nBackButtonCheckRange) {
					return true;
				} else {
					return false;
				}
			}
		};
		mBackButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				nCurrentStatus = STATUS_PLAYING;
				bPlayingAnimation = false;
				mlist.resetScroll();
			}
		});
		
		mlist = new MyListView(context);
		
		nScrollDirection = 0;

		final ViewConfiguration configuration = ViewConfiguration.get(context);
        nTouchSlop = configuration.getScaledTouchSlop();

		bPlaying = false;
		bPlayingAnimation = false;
		bPlaybackComplete = true;
		bMustDraw = false;
		nDrawCount = 1;
		nCurrentStatus = STATUS_PLAYING;
		nCurrentBgAngle = 0;
		nCurrentRotationalSpeed = 3;

		myPlayingInterface.addView(mTitle);
		myPlayingInterface.addView(mArtist);
		myPlayingInterface.addView(mPlayHelperMsg);
		myPlayingInterface.addView(mPlayCtrlImage);
		myPlayingInterface.addView(mPlayCtrlButton);
		myPlayingInterface.addView(mProgressBarImage);

		myPanelInterface.addView(mTitle);
		myPanelInterface.addView(mArtist);
		myPanelInterface.addView(mPlayHelperMsg);
		myPanelInterface.addView(mPlayPanelImage);
		myPanelInterface.addView(mPlayListButton);		
		myPanelInterface.addView(mProgressBarImage);
		myPanelInterface.addView(mShuffleButton);
		myPanelInterface.addView(mPreviousButton);
		myPanelInterface.addView(mNextButton);
		myPanelInterface.addView(mRepeatButton);

		myLibraryInterface.addView(mProgressBar2BgImage);
		myLibraryInterface.addView(mProgressBar2Image);
		myLibraryInterface.addView(mLibraryButton);
		myLibraryInterface.addView(mLibraryButtonTxt);
		myLibraryInterface.addView(mBackButton);
		myLibraryInterface.addView(mlist);

		myAnimation0.addView(mTitle);
		myAnimation0.addView(mArtist);
		myAnimation0.addView(mPlayHelperMsg);
		myAnimation0.addView(mPlayCtrlImage);
		myAnimation0.addView(mPlayCtrlButton);
		myAnimation0.addView(mProgressBarImage);

		myAnimation1.addView(mTitle);
		myAnimation1.addView(mArtist);
		myAnimation1.addView(mPlayHelperMsg);
		myAnimation1.addView(mPlayCtrlImage);
		myAnimation1.addView(mPlayCtrlButton);
		myAnimation1.addView(mProgressBarImage);
	}
		
	
	

	private void initData() {
		nCenterX = nViewWidth / 2;
		nCenterY = nViewHeight / 2;

		nViewShort = nViewWidth > nViewHeight ? nViewHeight
				: nViewWidth;

		int nShort = nViewShort - 2*nDefaultCDMargin;
		nCurrentCDRadius = (nShort > 2*nCurrentCDRadius ? nCurrentCDRadius : nShort/2);

		mPath.addRect(nCenterX - nCurrentCDRadius, nCenterY - nCurrentCDRadius,
			nCenterX + nCurrentCDRadius, nCenterY + nCurrentCDRadius, Path.Direction.CW);

		mScalePath.addCircle(nCurrentScaleCDRadius, nCurrentScaleCDRadius, 
			nCurrentScaleCDRadius, Path.Direction.CW);
		
		mSelScalePath.addCircle(nCurrentSelScaleCDRadius, nCurrentSelScaleCDRadius, 
			nCurrentSelScaleCDRadius, Path.Direction.CW);

		bCreatedWindow = true;
		mAlbumBackgroundBitmap = adjustToCurrentView(mAlbumBackgroundBitmap);
		mDefaultCDBitmap = adjustToCurrentView(mDefaultCDBitmap);
		mLightBitmap = adjustToCurrentView(mLightBitmap);
		mMaskBitmap = adjustToCurrentView(mMaskBitmap);
		if (null != mCurrCDInfo) {
			mCurrCDInfo.bitmap = adjustCDCover(mCurrCDInfo.bitmap);
		}

		mSCBitmap = Bitmap.createBitmap(nViewWidth, nViewHeight, Config.ARGB_8888);
		mCanvas = new Canvas();
		mCanvas.setBitmap(mSCBitmap);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		final int mTitleTextSize = (int)(fCurrentGlobalScale * 20);
		final int mTitleOffsetY = (int)(fCurrentGlobalScale * (-95));
		
		final int mArtistTextSize = (int)(fCurrentGlobalScale * 15);
		final int mArtistOffsetY = (int)(fCurrentGlobalScale * (-70));

		final int mPlayHelperMsgTextSize = (int)(fCurrentGlobalScale * 14);
		final int mPlayHelperMsgOffsetY = (int)(fCurrentGlobalScale * (55));

		final int mProgressMsgTextSize = (int)(fCurrentGlobalScale * 14);
		final int mProgressMsgMinWidth = (int)(fCurrentGlobalScale * 40);

		final int mShuffleButtonOffsetX = (int)(fCurrentGlobalScale * (-112));
		final int mShuffleButtonOffsetY = (int)(fCurrentGlobalScale * 4);


		final int mPreviousButtonOffsetX = (int)(fCurrentGlobalScale * (-70));
		final int mPreviousButtonOffsetY = (int)(fCurrentGlobalScale * 4);

		final int mNextButtonOffsetX = (int)(fCurrentGlobalScale * 70);
		final int mNextButtonOffsetY = (int)(fCurrentGlobalScale * 4);

		final int mRepeatButtonOffsetX = (int)(fCurrentGlobalScale * 112);
		final int mRepeatButtonOffsetY = (int)(fCurrentGlobalScale * 4);

		final int mProgressBar2BgImageWidth = (int)(fCurrentGlobalScale * 246);
		final int mProgressBar2BgImageHeight = (int)(fCurrentGlobalScale * 48);
		final int mProgressBar2BgImageVerticalOffsetY = (int)(fCurrentGlobalScale * 25);
		final int mProgressBar2BgImageHorizontalOffsetY = (int)(fCurrentGlobalScale * 15);

		final int mProgressBar2ImageWidth = (int)(fCurrentGlobalScale * 224);
		final int mProgressBar2ImageHeight = (int)(fCurrentGlobalScale * 25);
		final int mProgressBar2ImageVerticalOffsetY = (int)(fCurrentGlobalScale * 34);
		final int mProgressBar2ImageHorizontalOffsetY = (int)(fCurrentGlobalScale * 24);

		final int mCurrTitleTextSize = (int)(fCurrentGlobalScale * 14);
		final int mCurrTitleMaxWidth = (int)(fCurrentGlobalScale * 198);
		final int mCurrTitleVerticalOffsetY = (int)(fCurrentGlobalScale * 36);
		final int mCurrTitleHorizontalOffsetY = (int)(fCurrentGlobalScale * 26);

		final int mlistVerticalOffsetX = (int)(fCurrentGlobalScale * 12);
		final int mlistVerticalOffsetY = (int)(fCurrentGlobalScale * 85);
		int mlistVerticalHeight = (int)(fCurrentGlobalScale * 228);
		final int mlistHorizontalOffsetX = (int)(fCurrentGlobalScale * 63);
		final int mlistHorizontalOffsetY = (int)(fCurrentGlobalScale * 57);
		final int mlistHorizontalHeight = (int)(fCurrentGlobalScale * 168);

		final int mLibraryButtonWidth = (int)(fCurrentGlobalScale * 105);
		final int mLibraryButtonHeight = (int)(fCurrentGlobalScale * 38);
		int mLibraryButtonVerticalOffsetY = (int)(fCurrentGlobalScale * 331);
		final int mLibraryButtonHorizontalOffsetY = (int)(fCurrentGlobalScale * 228);

		final int mLibraryButtonTxtTextSize = (int)(fCurrentGlobalScale * 16);
		int mLibraryButtonTxtVerticalOffsetY = (int)(fCurrentGlobalScale * 335);
		final int mLibraryButtonTxtHorizontalOffsetY = (int)(fCurrentGlobalScale * 232);

		if (288 == nViewHeight) {
			mlistVerticalHeight = mlistVerticalHeight - 30;
			mLibraryButtonVerticalOffsetY = mLibraryButtonVerticalOffsetY - 30;
			mLibraryButtonTxtVerticalOffsetY = mLibraryButtonTxtVerticalOffsetY - 30;
		}

		nBackButtonVerticalOffsetX = nCenterX - (int)(fCurrentGlobalScale * (123-21));
		nBackButtonVerticalOffsetY = (int)(fCurrentGlobalScale * 32);
		nBackButtonHorizontalOffsetX = nCenterX - (int)(fCurrentGlobalScale * (123-21));
		nBackButtonHorizontalOffsetY = (int)(fCurrentGlobalScale * 22);
		nBackButtonRadius = nBackButtonRadiusLists[nCurrentGraphMode];
		nBackButtonCheckRange = (int)(fCurrentGlobalScale * 20);

		nCDCenterImageRadius = (int)(fCurrentGlobalScale * 36);
		nAnimMinHalfPanelWidth = (int)(fCurrentGlobalScale * nAnimDefaultMinHalfPanelWidth);
		nAnimMaxHalfPanelWidth = (int)(fCurrentGlobalScale * nAnimDefaultMaxHalfPanelWidth);
		nAnimMaxHalfPanelHeight = (int)(fCurrentGlobalScale * nAnimDefaultMaxHalfPanelHeight);
		nAnimCurrentPanelMoveSpeed = (int)(fCurrentGlobalScale * nAnimDefaultPanelMoveSpeed);
		nAnimCurrentHalfPanelWidth = nAnimMinHalfPanelWidth;

		nAnimation0Left = (int)(fCurrentGlobalScale * 8);
		nAnimation0ImgOffsetX = (int)(fCurrentGlobalScale * 48);
		nAnimation0ImgOffsetY = (int)(fCurrentGlobalScale * 50);
		nAnimation0ImgHeight = (int)(fCurrentGlobalScale * 100);
		nAnimation0HalfImgWidth = (int)(fCurrentGlobalScale * 153);
		nAnimation0ImgWidth = (int)(fCurrentGlobalScale * 306);


		nAnimation3CDScaleSpeed = (int)(fCurrentGlobalScale * nAnimation3DefaultCDScaleSpeed);
		
		nAnimation4MaxCDOffsetX = (int)(fCurrentGlobalScale * nAnimation4DefaultCDOffsetX);
		nAnimation4CDOffsetSpeed = (int)(fCurrentGlobalScale * nAnimation4DefaultCDOffsetSpeed);
		
		nAnimation5MaxCDOffsetY = (int)(fCurrentGlobalScale * nAnimation5DefaultCDOffsetY);
		nAnimation5CDOffsetSpeed = (int)(fCurrentGlobalScale * nAnimation5DefaultCDOffsetSpeed);			

		fCircleProgressBarWidth = fCurrentGlobalScale * 5f;
		fCircleProgressBarRadius = fCurrentGlobalScale * 40.5f;
		if (1==nCurrentGraphMode) {
			fCircleProgressBarRadius = fCircleProgressBarRadius - 0.5f;
			fCircleProgressBarWidth = fCircleProgressBarWidth + 0.5f;
		}


		mTitle.setTextSize(mTitleTextSize);
		mTitle.setTextColor(Color.BLACK);
		mTitle.setText("No songs");
		mTitle.setMaxWidth((int)(Math.sin(Math.acos((double)Math.abs(mTitleOffsetY)/nCurrentCDRadius))*nCurrentCDRadius*2*0.9));
		mTitle.setPos(nCenterX, nCenterY + mTitleOffsetY);
		mTitle.initData();

		mArtist.setTextSize(mArtistTextSize);
		mArtist.setTextColor(Color.BLACK);
		mArtist.setText("No artist");
		mArtist.setMaxWidth((int)(Math.sin(Math.acos((double)Math.abs(mArtistOffsetY)/nCurrentCDRadius))*nCurrentCDRadius*2*0.9));
		mArtist.setPos(nCenterX, nCenterY + mArtistOffsetY);
		mArtist.initData();

		mPlayHelperMsg.setTextSize(mPlayHelperMsgTextSize);
		mPlayHelperMsg.setTextColor(Color.WHITE);
		mPlayHelperMsg.setText("Tap to pause!");
		mPlayHelperMsg.setMaxWidth(nViewWidth);
		mPlayHelperMsg.setPos(nCenterX, nCenterY + mPlayHelperMsgOffsetY);
		mPlayHelperMsg.setPadding(14, 4, 14, 12);
		mPlayHelperMsg.initData();

		mProgressMsg.setTextSize(mProgressMsgTextSize);
		mProgressMsg.setTextColor(Color.WHITE);
		mProgressMsg.setMinWidth(mProgressMsgMinWidth);
		mProgressMsg.setMaxWidth(nViewWidth);

		mPlayPanelImage.setPos(nCenterX, nCenterY);
		mPlayPanelImage.initData();

		mPlayCtrlImage.setPos(nCenterX, nCenterY);
		mPlayCtrlImage.initData();

		mProgressBarImage.setPos(nCenterX, nCenterY);
		mProgressBarImage.initData();

		mPlayCtrlButton.setPos(nCenterX, nCenterY);
		mPlayCtrlButton.initData();

		mPlayListButton.setPos(nCenterX, nCenterY);
		mPlayListButton.initData();

		mShuffleButton.setPos(nCenterX+mShuffleButtonOffsetX, nCenterY+mShuffleButtonOffsetY);
		mShuffleButton.initData();

		mPreviousButton.setPos(nCenterX+mPreviousButtonOffsetX, nCenterY+mPreviousButtonOffsetY);
		mPreviousButton.initData();

		mNextButton.setPos(nCenterX+mNextButtonOffsetX, nCenterY+mNextButtonOffsetY);
		mNextButton.initData();

        mRepeatButton.setPos(nCenterX+mRepeatButtonOffsetX, nCenterY+mRepeatButtonOffsetY);
        mRepeatButton.initData();

		mProgressBar2BgImage.setWH(mProgressBar2BgImageWidth, mProgressBar2BgImageHeight);
		if (bIsHorizontal) {
			mProgressBar2BgImage.setPos(nCenterX, mProgressBar2BgImageHorizontalOffsetY);
		} else {
			mProgressBar2BgImage.setPos(nCenterX, mProgressBar2BgImageVerticalOffsetY);

		}
		mProgressBar2BgImage.initData();

		mProgressBar2Image.setWH(mProgressBar2ImageWidth, mProgressBar2ImageHeight);
		if (bIsHorizontal) {
			mProgressBar2Image.setPos(nCenterX, mProgressBar2ImageHorizontalOffsetY);
		} else {
			mProgressBar2Image.setPos(nCenterX, mProgressBar2ImageVerticalOffsetY);
		}
		mProgressBar2Image.initData();


		mCurrTitle.setTextSize(mCurrTitleTextSize);
		mCurrTitle.setTextColor(Color.WHITE);
		mCurrTitle.setMaxWidth((int)(mCurrTitleMaxWidth * 0.9));
		if (bIsHorizontal) {
			mCurrTitle.setPos(nCenterX, mCurrTitleHorizontalOffsetY);
		} else {
			mCurrTitle.setPos(nCenterX, mCurrTitleVerticalOffsetY);
		}
		mCurrTitle.initData();

		if (bIsHorizontal) {
			mBackButton.setPos(nBackButtonHorizontalOffsetX, nBackButtonHorizontalOffsetY);
		} else {
			mBackButton.setPos(nBackButtonVerticalOffsetX, nBackButtonVerticalOffsetY);
		}
		mBackButton.initData();

		if (bIsHorizontal) {
			mlist.setPos(mlistHorizontalOffsetX, mlistHorizontalOffsetY);
			mlist.setHeight(mlistHorizontalHeight);
		} else {
			mlist.setPos(mlistVerticalOffsetX, mlistVerticalOffsetY);
			mlist.setHeight(mlistVerticalHeight);
		}
		mlist.initData();

		mLibraryButton.setWH(mLibraryButtonWidth, mLibraryButtonHeight);
		if (bIsHorizontal) {
			mLibraryButton.setPos(nCenterX, mLibraryButtonHorizontalOffsetY);
		} else {
        	mLibraryButton.setPos(nCenterX, mLibraryButtonVerticalOffsetY);
		}
        mLibraryButton.initData();

		mLibraryButtonTxt.setTextSize(mLibraryButtonTxtTextSize);
		mLibraryButtonTxt.setTextColor(Color.WHITE);
		mLibraryButtonTxt.setText("Library");
		mLibraryButtonTxt.setMaxWidth(nViewWidth);
		if (bIsHorizontal) {
			mLibraryButtonTxt.setPos(nCenterX, mLibraryButtonTxtHorizontalOffsetY);
		} else {
        	mLibraryButtonTxt.setPos(nCenterX, mLibraryButtonTxtVerticalOffsetY);
		}
        mLibraryButtonTxt.initData();

		myPlayingInterface.initData();
		myPanelInterface.initData();
		myLibraryInterface.initData();
	}

	private void resetProgressMsgInfo() {
		final int posX = nCenterX + (int)(Math.sin(dCurrentProgress * dTwoPI) * (nCDCenterImageRadius + 2));
		final int posY = nCenterY - (int)(Math.cos(dCurrentProgress * dTwoPI) * (nCDCenterImageRadius + 2));
		final int duration = (int)(dCurrentProgress * nMaxProgress / 1000);
		final int minute = duration / 60;
		final int second = duration % 60;
		mProgressMsg.setPos(posX+3, posY + 3);
		mProgressMsg.setText(""+minute+"'"+second+"\"");	
	}
	
	public String getAdjustedText(String srcText, Paint paint, float maxlen) {
		String destText;

		if (null == srcText) {
			return null;
		}
				
		String tmp = "...";
		int nTmpTextLength = tmp.length();

		float[] tmpwidths = new float[nTmpTextLength];
		int nTmpCount = paint.getTextWidths(tmp, 0, nTmpTextLength, tmpwidths);

		int nTextLength = srcText.length();			
		float[] widths = new float[nTextLength];
		int nCount = paint.getTextWidths(srcText, 0, nTextLength, widths);

		float fRealTextWidth = 0;
		float fTmpMaxLength = maxlen;
		int i;
		for (i=0; i<nCount; i++ ) {
			fRealTextWidth += widths[i];
		}

		if (fRealTextWidth > maxlen)
		{
			for (i=0; i<nTmpCount; i++ ) {
				fTmpMaxLength = fTmpMaxLength - tmpwidths[i];
			}

			for (i=nTextLength-1; i>=0; i-- ) {
				fRealTextWidth = fRealTextWidth - widths[i];
				if (fRealTextWidth<fTmpMaxLength) {
					break;
				}
			}
			destText = srcText.substring(0, i+1);
			destText = destText + tmp;

			for (i=0; i<nTmpCount; i++ ) {
				fRealTextWidth = fRealTextWidth + tmpwidths[i];
			}
		}
		else {
			destText = srcText;
		}

		return destText;
	}

    private Bitmap createScaleBitmap(Bitmap b) {
		Bitmap bitmap = b;
		
		if (null != bitmap && 0 != nCurrentScaleCDRadius) {
			final int nBitmapWidth = bitmap.getWidth();
			final int nBitmapHeight = bitmap.getHeight();
			final int nBitmapShort = nBitmapWidth > nBitmapHeight ? nBitmapHeight
					: nBitmapWidth;

			final int nNewShort = 2 * nCurrentScaleCDRadius;
			final float f = (float) nNewShort / nBitmapShort;
			Matrix mMatrix = new Matrix();
			mMatrix.setScale(f, f);
			Bitmap tmpBmp = Bitmap.createBitmap(bitmap, 
				(nBitmapWidth - nBitmapShort)/2,
				(nBitmapHeight - nBitmapShort)/2,
					nBitmapShort, nBitmapShort, mMatrix, false);
			bitmap = tmpBmp;
		}

		return bitmap;
    }

    private Bitmap createSelScaleCDBitmap(Bitmap b) {
		Bitmap bitmap = b;
		Bitmap mask = mMaskBitmap;
		Bitmap light = mLightBitmap;
		
		if (null != bitmap && 0 != nCurrentSelScaleCDRadius) {
			final int nBitmapWidth = bitmap.getWidth();
			final int nBitmapHeight = bitmap.getHeight();
			final int nBitmapShort = nBitmapWidth > nBitmapHeight ? nBitmapHeight
					: nBitmapWidth;
			final int nShort= nCurrentSelScaleCDRadius * 2;
			final int nSrcLeft = (nBitmapWidth - nBitmapShort) / 2;
			final int nSrcTop = (nBitmapHeight - nBitmapShort) / 2;
			mAdjustSrcRect.set(nSrcLeft, nSrcTop, nSrcLeft+nBitmapShort, nSrcTop+nBitmapShort);
			mAdjustDstRectF.set(0, 0, nShort, nShort);
			
			Bitmap tmpBmp = Bitmap.createBitmap(nShort, nShort, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas();
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
			canvas.setBitmap(tmpBmp);

			if (null != mask) {
				canvas.drawBitmap(bitmap, mAdjustSrcRect, mAdjustDstRectF, paint);
				mAdjustSrcRect.set(0, 0, mask.getWidth(), mask.getHeight());
				canvas.drawBitmap(mask, mAdjustSrcRect, mAdjustDstRectF, paint);
				paint.setXfermode(mModes);
				canvas.drawBitmap(mask, mAdjustSrcRect, mAdjustDstRectF, paint);
				paint.setXfermode(null);		
			} else {
				int saveCount = canvas.getSaveCount();
				canvas.save();
				canvas.clipPath(mSelScalePath);
				canvas.drawBitmap(bitmap, mAdjustSrcRect, mAdjustDstRectF, paint);
				canvas.restoreToCount(saveCount);
			}

			if (null != light) {
				mAdjustSrcRect.set(0, 0, light.getWidth(), light.getHeight());
				canvas.drawBitmap(light, mAdjustSrcRect, mAdjustDstRectF, paint);
			}
			
			bitmap = tmpBmp;
		}

		return bitmap;

    }

	private Bitmap clipPadding(Bitmap b) {
		Bitmap bitmap = b;

		if (null != bitmap) {
			final int nBitmapWidth = bitmap.getWidth();
			final int nBitmapHeight = bitmap.getHeight();
			
			if (0 == nCurrentCDRadius) {
				return bitmap;
			}

			int nShort = 2 * nCurrentCDRadius;
			Bitmap tmpBmp = Bitmap.createBitmap(bitmap,
					(nBitmapWidth - nShort)/2, 
					(nBitmapHeight - nShort)/2, 
					nShort, nShort);
			bitmap.recycle();
			bitmap = tmpBmp;
		}
		return bitmap;		
	}

	private Bitmap adjustToCurrentView(Bitmap b) {
		synchronized (this) {
			Bitmap bitmap = b;

			if (null != bitmap) {
				final int nBitmapWidth = bitmap.getWidth();
				final int nBitmapHeight = bitmap.getHeight();
				final int nBitmapShort = nBitmapWidth > nBitmapHeight ? nBitmapHeight
						: nBitmapWidth;
				final int nBgShort = 2*nCurrentCDRadius;

				if (nBitmapShort > nBgShort){
					final float f = (float) nBgShort / nBitmapShort;
					Matrix matrix = new Matrix();
					matrix.postScale(f, f);
					Bitmap tmpBmp = Bitmap.createBitmap(bitmap,
							(nBitmapWidth - nBitmapShort) / 2,
							(nBitmapHeight - nBitmapShort) / 2, nBitmapShort, nBitmapShort,
							matrix, false);
					bitmap.recycle();
					bitmap = tmpBmp;
					
				}
			}
			return bitmap;			
		}
	}

	private Bitmap adjustScaleCDBitmap(Bitmap b) {
		Bitmap bitmap = b;
		Bitmap mask = mMaskScaleBitmap;
		Bitmap light = mLightScaleBitmap;
		
		Log.v(TAG, "adjustScaleCDBitmap; Thread =["+Thread.currentThread()+"]");	
		if (null != bitmap) {
			final int nBitmapWidth = bitmap.getWidth();
			final int nBitmapHeight = bitmap.getHeight();
			final int nBitmapShort = nBitmapWidth > nBitmapHeight ? nBitmapHeight
					: nBitmapWidth;
			final int nShort= nCurrentScaleCDRadius * 2;
			final int nSrcLeft = (nBitmapWidth - nBitmapShort) / 2;
			final int nSrcTop = (nBitmapHeight - nBitmapShort) / 2;
			mAdjustSrcRect.set(nSrcLeft, nSrcTop, nSrcLeft+nBitmapShort, nSrcTop+nBitmapShort);
			mAdjustDstRectF.set(0, 0, nShort, nShort);
			
			Bitmap tmpBmp = Bitmap.createBitmap(nShort, nShort, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas();
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
			canvas.setBitmap(tmpBmp);

			if (null != mask) {
				canvas.drawBitmap(bitmap, mAdjustSrcRect, mAdjustDstRectF, paint);
				canvas.drawBitmap(mask, 0, 0, paint);
				paint.setXfermode(mModes);
				canvas.drawBitmap(mask, 0, 0, paint);
				paint.setXfermode(null);		
			} else {
				int saveCount = canvas.getSaveCount();
				canvas.save();
				canvas.clipPath(mScalePath);
				canvas.drawBitmap(bitmap, mAdjustSrcRect, mAdjustDstRectF, paint);
				canvas.restoreToCount(saveCount);
			}

			if (null != light) {
				canvas.drawBitmap(light, 0, 0, paint);
			}
			
			bitmap.recycle();
			bitmap = tmpBmp;
		}

		return bitmap;
	}

	private Bitmap adjustCDCover(Bitmap b){
		Bitmap bitmap = b;
		Bitmap mask = mMaskBitmap;
			
		if (null != bitmap) {	
			final int nBitmapWidth = bitmap.getWidth();
			final int nBitmapHeight = bitmap.getHeight();
			final int nBitmapShort = nBitmapWidth > nBitmapHeight ? nBitmapHeight : nBitmapWidth;
			final int nShort = nCurrentCDRadius * 2;
			if (nBitmapShort != nShort || nBitmapWidth != nBitmapHeight) {
				final int nSrcLeft = (nBitmapWidth - nBitmapShort) / 2;
				final int nSrcTop = (nBitmapHeight - nBitmapShort) / 2;
				mAdjustSrcRect.set(nSrcLeft, nSrcTop, nSrcLeft+nBitmapShort, nSrcTop+nBitmapShort);
				mAdjustDstRectF.set(0, 0, nShort, nShort);

				Bitmap tmpBmp = Bitmap.createBitmap(nShort, nShort, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas();
				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
				canvas.setBitmap(tmpBmp);
				canvas.drawBitmap(bitmap, mAdjustSrcRect, mAdjustDstRectF, paint);

				bitmap.recycle();
				bitmap = tmpBmp;
			}
		}
		
		return bitmap;
	}

	private void drawCurrCDCover(Canvas canvas) {
		Bitmap mCurrCDBitmap = (null==mCurrCDInfo ? null : mCurrCDInfo.getBitmap());
		if (null != mCurrCDBitmap) {
			mMatrix.reset();
			mMatrix.setTranslate(nCenterX - mCurrCDBitmap.getWidth() / 2,
					nCenterY - mCurrCDBitmap.getHeight() / 2);
			mMatrix.postRotate(nCurrentBgAngle, nCenterX, nCenterY);
		
			int saveCount = canvas.getSaveCount();
			canvas.save();
			canvas.clipPath(mPath);
			canvas.drawBitmap(mCurrCDBitmap, mMatrix, mPaint);
			canvas.restoreToCount(saveCount);
		
			if (null != mMaskBitmap) {
				final int left = nCenterX - mMaskBitmap.getWidth() / 2;
				final int top = nCenterY - mMaskBitmap.getHeight() / 2;
				
				canvas.drawBitmap(mMaskBitmap, left, top, mPaint);
				mPaint.setXfermode(mModes);
				canvas.drawBitmap(mMaskBitmap, left, top, mPaint);
				mPaint.setXfermode(null);
			}
			if (null != mLightBitmap) {
				canvas.drawBitmap(mLightBitmap, (nCenterX - mLightBitmap.getWidth() / 2), 
					(nCenterY - mLightBitmap.getHeight() / 2), mPaint);
			}
		} else {
			if (null != mAlbumBackgroundBitmap) {
				mMatrix.reset();
				mMatrix.setTranslate(nCenterX - mAlbumBackgroundBitmap.getWidth() / 2,
						nCenterY - mAlbumBackgroundBitmap.getHeight() / 2);
				canvas.drawBitmap(mAlbumBackgroundBitmap, mMatrix, mPaint);
			}
		}
	}

	private void drawCircleProgressBarShadow(Canvas canvas) {
		int fCurrentProgressDegress = (int) (dCurrentProgress * 360);
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(fCircleProgressBarWidth);
		mDrawDstRectF.set(nCenterX - fCircleProgressBarRadius, nCenterY - fCircleProgressBarRadius,
				nCenterX + fCircleProgressBarRadius, nCenterY + fCircleProgressBarRadius);
		
		canvas.drawArc(mDrawDstRectF, fCurrentProgressDegress - 90, 360 - fCurrentProgressDegress, false, mPaint);
	}

	private void initMyViewGroup() {
		ViewParent parent = this.getParent();
		mOnLongClickListener = null;
		mDefaultOnLongClickListener = null;
		
		while (null != parent) {
			if (strMyLauncherAppWidgetHostViewClassName.equals(parent.getClass().toString())) {
				mLauncherApp = (ViewGroup) parent;
				//Log.v(TAG, "mLauncherApp:w="+mLauncherApp.getWidth()+"; h="+mLauncherApp.getHeight());
			}
			if (strMyCellLayoutClassName.equals(parent.getClass().toString())) {
				mCellLayout = (ViewGroup) parent;

				//Log.v(TAG, "mCellLayout:w="+mCellLayout.getWidth()+"; h="+mCellLayout.getHeight());
			}
			if (strMyWorkspaceClassName.equals(parent.getClass().toString())) {
				mWorkspace = (ViewGroup) parent;
			}
			if (strMyDragLayerClassName.equals(parent.getClass().toString())) {
				mDragLayer = (ViewGroup) parent;
			}
			if (null == mDefaultOnLongClickListener) {
				if (parent instanceof View) {
					Context c = ((View)parent).getContext();
					if (strMyLauncherClassName.equals(c.getClass().toString()) && (c instanceof OnLongClickListener)) {
						mDefaultOnLongClickListener = (OnLongClickListener)c;
					}
				}
			}
			parent = parent.getParent();
		}
	}

	private void init() {
		initData();
		initMyViewGroup();

		disallowWorkspaceInterceptTouchEvent();
	}
	
	private void recycleLastCDBitmap(){
		if (null != mLastCDInfo) {
			mLastCDInfo.recycleBitmap();
			mLastCDInfo = null;
		}
	}

	private void clearResource() {
		if (null != mWorkspace) {
			mWorkspace.requestDisallowInterceptTouchEvent(false);
		}
		mDelayedClosePanelHandler.removeCallbacksAndMessages(null);
		mAlbumArtHandler.removeCallbacksAndMessages(null);
		
		recycleLastCDBitmap();

		if (null != mCurrCDInfo) {
			mCurrCDInfo.recycleBitmap();
			mCurrCDInfo = null;
		}
		
		clearMusicList(0);

		Bitmap tmpBmp;

		if (null != mSCBitmap) {
			tmpBmp = mSCBitmap;
			mSCBitmap = null;
			tmpBmp.recycle();
		}

		if (null != mAlbumBackgroundBitmap) {
			tmpBmp = mAlbumBackgroundBitmap;
			mAlbumBackgroundBitmap = null;
			tmpBmp.recycle();
		}

		/*if (null != mAlbumBackgroundScaleBitmap) {
			tmpBmp = mAlbumBackgroundScaleBitmap;
			mAlbumBackgroundScaleBitmap = null;
			tmpBmp.recycle();
		}*/

		if (null != mDefaultCDBitmap) {
			tmpBmp = mDefaultCDBitmap;
			mDefaultCDBitmap = null;
			tmpBmp.recycle();
		}

		if (null != mDefaultCDScaleBitmap) {
			tmpBmp = mDefaultCDScaleBitmap;
			mDefaultCDScaleBitmap = null;
			tmpBmp.recycle();
		}
		
		if (null != mLightBitmap) {
			tmpBmp = mLightBitmap;
			mLightBitmap = null;
			tmpBmp.recycle();
		}
		
		if (null != mLightScaleBitmap) {
			tmpBmp = mLightScaleBitmap;
			mLightScaleBitmap = null;
			tmpBmp.recycle();
		}

		if (null != mMaskBitmap) {
			tmpBmp = mMaskBitmap;
			mMaskBitmap = null;
			tmpBmp.recycle();
		}

		if (null != mMaskScaleBitmap) {
			tmpBmp = mMaskScaleBitmap;
			mMaskScaleBitmap = null;
			tmpBmp.recycle();
		}

		if (null != mSelScaleCDBitmap) {
			tmpBmp = mSelScaleCDBitmap;
			mSelScaleCDBitmap = null;
			tmpBmp.recycle();
		}

		Hashtable<Long, Bitmap> tmpTable = mAlbumTable;
		mAlbumTable = new Hashtable<Long, Bitmap>();
		Enumeration e = tmpTable.elements();
		while(e.hasMoreElements()){
			tmpBmp = (Bitmap)e.nextElement();
			tmpBmp.recycle();
			//Log.v(TAG, "clear hashtable artwork");
		}
		tmpTable.clear();
		tmpTable = null;
	}
	
	private void disallowWorkspaceInterceptTouchEvent() {
		if (null != mWorkspace) {
			mWorkspace.requestDisallowInterceptTouchEvent(true);
		}

		if (null != mDefaultOnLongClickListener && null != mLauncherApp) {
			mLauncherApp.setOnLongClickListener(null);
			setOnLongClickListener(mOnLongClickListener);
		}
		
		if (null != mDragLayer) {
			mDragLayer.requestDisallowInterceptTouchEvent(false);
		}

	}

	private boolean checkInRangeForAlbum(int x1, int y1) {
		int x = x1 > nCenterX ? x1 - nCenterX : nCenterX - x1;
		int y = y1 > nCenterY ? y1 - nCenterY : nCenterY - y1;

		if (x * x + y * y < nCurrentCDRadius * nCurrentCDRadius) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkInRangeForSlip(int x1, int y1) {
		int nAnimationStatus = ANIMATION_MASK & nCurrentStatus;

		if (0 == nAnimationStatus) {
			switch (nCurrentStatus) {
			case STATUS_PLAYING:
				if (checkInRangeForAlbum(x1, y1)) {
					return false;
				} else {
					return true;
				}
			case STATUS_PANEL:
				if (checkInRangeForAlbum(x1, y1)
						|| myPanelInterface.checkInRangeForControlPanel(x1, y1)) {
					return false;
				} else {
					return true;
				}
			case STATUS_LIBRARY:
				break;
			case STATUS_CHOOSE:
				break;
			default:
				break;
			}
		}
		
		return false;
	}

	private MyInterface getCurrentInterface() {
		MyInterface myInterface = null;
		int nAnimationStatus = ANIMATION_MASK & nCurrentStatus;

		if (0 == nAnimationStatus) {
			switch (nCurrentStatus) {
			case STATUS_PLAYING:
				myInterface = myPlayingInterface;
				break;
			case STATUS_PANEL:
				myInterface = myPanelInterface;
				break;
			case STATUS_LIBRARY:
				myInterface = myLibraryInterface;
				break;
			case STATUS_CHOOSE:
				myInterface = myChooseInterface;
				break;
			default:
				break;
			}
		} else {
			switch (nAnimationStatus) {
			case ANIMATION_0:
				myInterface = myAnimation0;
				break;
			case ANIMATION_1:
				myInterface = myAnimation1;
				break;
			case ANIMATION_2:
				myInterface = myAnimation2;
				break;
			case ANIMATION_3:
				myInterface = myAnimation3;
				break;
			case ANIMATION_4:
				myInterface = myAnimation4;
				break;
			case ANIMATION_5:
				myInterface = myAnimation5;
				break;
			default:
				break;
			}
		}
		
		return myInterface;
	}


	
	/*
	private SurfaceHolder mHolder; 
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		if (!bInit) {
			init();
			bInit = true;
		}
	}
	  
	public void surfaceDestroyed(SurfaceHolder holder) {
		mThread.interrupt(); 
		mThread = null; 
		stopDelayedClosePanelHandler();
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

	}
	*/
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		nViewWidth = w;
		nViewHeight = h;
		bIsHorizontal = nViewWidth > nViewHeight;
		Log.v(TAG, "onSizeChanged:w="+w+"; h="+h);
		requestUpdate(getContext());
	} 

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null != mSCBitmap) {
			canvas.drawBitmap(mSCBitmap, 0, 0, mPaint);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final boolean superResult = super.onTouchEvent(event);
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();

		if (MotionEvent.ACTION_DOWN == action && checkInRangeForSlip(x, y)) {
			bSlip = true;
			if (null != mWorkspace) {
				nLastScrollX = getScrollX();
			}
		}

		if (bSlip) {
			if (null != mWorkspace) {
				mWorkspace.onTouchEvent(event);
			}
		}

		if (MotionEvent.ACTION_UP == action && bSlip) {
			bSlip = false;
			if (null != mWorkspace) {
				int nCurrScrollX = mWorkspace.getScrollX();
				nScrollDirection = (nCurrScrollX == nLastScrollX ? 0 : (nCurrScrollX > nLastScrollX ? 1 : -1));
			}
		}

		if (null != mWorkspace) {
			mWorkspace.onInterceptTouchEvent(event);
		}

		if (!bSlip) {
			if (bPlayingAnimation)
				return true;

			MyInterface myInterface = getCurrentInterface();
			if (null != myInterface) {
				myInterface.onTouchEvent(event);
			}
		}

		disallowWorkspaceInterceptTouchEvent();

		return true;

	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (!pressed) {
			MyInterface myInterface = getCurrentInterface();
			if (null != myInterface) {
				myInterface.cancleCurrentPressed();
			}
		}
	}

	@Override
	public boolean performLongClick() {
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);

		boolean handled = false;
		if (mOnLongClickListener != null) {
		 	handled = mOnLongClickListener.onLongClick(MyTestView.this);
		} else if (mDefaultOnLongClickListener != null) {
		 	handled = mDefaultOnLongClickListener.onLongClick(mLauncherApp);
		}

		if (!handled) {
		 	handled = showContextMenu();
		}
		if (handled) {
		 	performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}
		return handled;
	}

	public void myDraw(Canvas canvas) {
		synchronized (this) {
			MyInterface myInterface = getCurrentInterface();
			if (null != myInterface) {
				myInterface.draw(canvas);
			}
		}
	}
	
	private Hashtable<Long, Bitmap> mAlbumTable = new Hashtable<Long, Bitmap>();

	private Handler mAlbumArtHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ADD_MUSIC_INFO:
					{
						MusicInfo param = ((MusicInfo) msg.obj);		
						myUtils.loadMusicInfo(param, true);
						mMusicList.add(param);
						break;
					}
				case LOAD_CURRENT_MUSIC_INFO:	
					{
						Long param = ((Long) msg.obj);		
						long songid = param.longValue();
						myUtils.loadCurrentMusicInfo(songid);
						break;
					}
				case SET_CD_BG:
					{
						if (null == mAlbumBackgroundBitmap) { //set once
							Long param = ((Long) msg.obj);		
							int resid = param.intValue();
							mAlbumBackgroundBitmap = clipPadding(loadBitmap(resid));
							//mAlbumBackgroundScaleBitmap  = createScaleBitmap(mAlbumBackgroundScaleBitmap);
							if (bCreatedWindow) {
								mAlbumBackgroundBitmap = adjustToCurrentView(mAlbumBackgroundBitmap);
							}
							Log.v(TAG, "set CD background finish");
						}
						break;
					}
				case SET_CD_DEFAULT:
					{
						if (null == mDefaultCDBitmap) { //set once
							Long param = ((Long) msg.obj);	
							int resid = param.intValue();
							mDefaultCDBitmap = clipPadding(loadBitmap(resid));
							mDefaultCDScaleBitmap = createScaleBitmap(mDefaultCDBitmap);
							if (bCreatedWindow) {
								mDefaultCDBitmap = adjustToCurrentView(mDefaultCDBitmap);
							}
							if(null!=mLightScaleBitmap && null!=mDefaultCDScaleBitmap) {
								Canvas canvas = new Canvas();
								Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
								canvas.setBitmap(mDefaultCDScaleBitmap);
								canvas.drawBitmap(mLightScaleBitmap, 0, 0, paint);
							}
							Log.v(TAG, "set CD default finish");
						}
						break;
					}
				case SET_CD_LIGHT:
					{
						if (null == mLightBitmap) { //set once
							Long param = ((Long) msg.obj);	
							int resid = param.intValue();
							mLightBitmap = clipPadding(loadBitmap(resid));
							mLightScaleBitmap = createScaleBitmap(mLightBitmap);
							if (bCreatedWindow) {
								mLightBitmap = adjustToCurrentView(mLightBitmap);
							}
							Log.v(TAG, "set CD light finish");
						}
						break;
					}
				
				case SET_CD_MASK:
					{
						if (null == mMaskBitmap) { //set once	
							Long param = ((Long) msg.obj);	
							int resid = param.intValue();
							Bitmap tmpBmp = loadBitmap(resid);

							if (null == tmpBmp) {
								Log.v(TAG, "set CD mask fail");
								return ;
							}
							int nBitmapWidth = tmpBmp.getWidth();
							for (int i=0; i<4; i++) {
								if (nBitmapWidth == nRadiusLists[2*i]) {
									nCurrentGraphMode = i;
									nCurrentCDRadius = nRadiusLists[2*i+1];
									fCurrentGlobalScale = (float)nCurrentCDRadius / nDefaultCDRadius;
									nCurrentScaleCDRadius = (int)(fCurrentGlobalScale * nDefaultScaleCDRadius);
									nCurrentSelScaleCDRadius = (int)(fCurrentGlobalScale * nDefaultSelScaleCDRadius);
									break;
								}	
							}
							mMaskBitmap = clipPadding(tmpBmp);
							mMaskScaleBitmap = createScaleBitmap(mMaskBitmap);
							if (bCreatedWindow) {
								mMaskBitmap = adjustToCurrentView(mMaskBitmap);
							}
							Log.v(TAG, "set CD mask finish, BitmapWidth="+nBitmapWidth);
						}
						break;
					}

				case CLEAR_ARTWORK:
					mlist.setFocusIndex(-1);
					synchronized (mMusicList) {
						if (null != mMusicList) {
							int size = mMusicList.size();
							for (int i=0; i<size; i++) {
								//Log.v(TAG, "clear list artwork");
								mMusicList.get(i).recycleBitmap();
							}
						    mMusicList.clear();
						}
					}
					break;
				
				case SET_PLAY_POS:
					{
						Long param = ((Long) msg.obj);	
						int index = param.intValue();
						long currentSongId = -1;
						mlist.setFocusIndex(index);
						if (index>=0 && index < mMusicList.size()) {
							currentSongId = mMusicList.get(index).id;
						}
						if (-1 != currentSongId && nLastSongId == currentSongId) {
							mlist.resetSelImageInitFlag();
						}
						nLastSongId = currentSongId;
						break;
					}
			}		
		}
	};

	@Override
	protected void onAttachedToWindow() {
		Log.v(TAG, "onAttachedToWindow");
		mThreadStopped = false;
		super.onAttachedToWindow();
		mThread = new Thread(new myThread());
		mThread.start();

		if (null == mInstances) {
			mInstances = new ArrayList<MyTestView>();
		}
		mInstances.add(this);

		if (null != mStopServiceAction && !mStopServiceAction.equals("")) {
			Intent stopIntent = new Intent(mStopServiceAction);
			stopIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
			stopIntent.putExtra("stop", false);
			getContext().sendBroadcast(stopIntent);
		}

	}
	
	@Override
	protected void onDetachedFromWindow() {
		Log.v(TAG, "onDetachedFromWindow");
		super.onDetachedFromWindow();
		mThreadStopped = true;
		
		if (null != mInstances) {
			mInstances.remove(this);
			if (mInstances.isEmpty()) {
				if (null !=mStopServiceAction && !mStopServiceAction.equals("")) {
					Intent stopIntent = new Intent(mStopServiceAction);
					stopIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
					stopIntent.putExtra("stop", true);
					getContext().sendBroadcast(stopIntent);
				}
				mInstances = null;
			}
		}
	}

	private boolean mThreadStopped;
	private Thread mThread;

	class myThread implements Runnable {
		public void run() {
			long nStartTime;
			long nEndTime;
			long nCostTime;
			int nCurrScrollX;
			int nScreenWidth;
			
			while (!Thread.currentThread().isInterrupted()) {

				try {
					if (mThreadStopped) break;
					
					nStartTime = SystemClock.elapsedRealtime();

					if ( !bInit && 0 != fCurrentGlobalScale 
						&& 0 != nViewWidth && 0 != nViewHeight) {
						init();
						bInit = true;
					}
					
					if (0 != nScrollDirection) {
						if (null != mWorkspace && null != mCellLayout) {
							nCurrScrollX = mWorkspace.getScrollX();
							nScreenWidth = mCellLayout.getWidth();
							if (0 ==  (nCurrScrollX % nScreenWidth)) {
								nScrollDirection = 0;
								if (nCurrScrollX != nLastScrollX) {
									mWorkspace.requestDisallowInterceptTouchEvent(false);
								}
							}
						}
					}
					
					int nAnimationStatus = ANIMATION_MASK & nCurrentStatus;
					if (true == bPlaying && false == bStartDrag) {
						nCurrentBgAngle = (nCurrentBgAngle + nCurrentRotationalSpeed) % 360;
					}
					
					MyInterface myInterface = getCurrentInterface();
					if (null != myInterface && 0 != nAnimationStatus) {
						myInterface.run();
					}

					if (bInit) {
						if (bMustDraw  != (bPlaying || bPlayingAnimation)) {
							if (bMustDraw) {
								nDrawCount = 1;
							} else {
								nDrawCount = 2;
							}
							bMustDraw = (bPlaying || bPlayingAnimation);
						}

							int saveCount = mCanvas.saveLayer(null, mPaint, Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
							mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

							myDraw(mCanvas);
							mCanvas.restoreToCount(saveCount);
							postInvalidate();
						if (nDrawCount > 0) {

							if (1 == nDrawCount) {
								nDrawCount = 0;
							}
						}
						// Canvas canvas = mHolder.lockCanvas();
						// canvas.save();
						// myDraw(canvas);
						// canvas.restore();
						// mHolder.unlockCanvasAndPost(canvas);

						nEndTime = SystemClock.elapsedRealtime();

					} else {
						nEndTime = SystemClock.elapsedRealtime();
					}
					nCostTime = nEndTime - nStartTime;
					//Log.v("Rick", "nCostTime="+nCostTime);
					
					if (nCostTime < 100) {
						Thread.sleep((100 - nCostTime));
					}
				} 
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			clearResource();
		}
	}


	private abstract class MyInterface extends MyInternalView {
		private boolean mIsLongPress = false;
		private MyImageButton mCurrentActivateButton = null;		
		private ArrayList<MyInternalView> mChilds;
		public void addView(MyInternalView v) {
	        if (mChilds == null) {
	            mChilds = new ArrayList<MyInternalView>();
	        }
	        mChilds.add(v);
	    }

		public boolean getIsLongPress() {return this.mIsLongPress;}
		
		public void cancleCurrentPressed() {
			if (null != mCurrentActivateButton) {
				mCurrentActivateButton.setActivate(false);
				mCurrentActivateButton.setHasPerformedLongPress(false);
				mCurrentActivateButton = null;
				mOnLongClickListener = null;
				mIsLongPress = false;
			}
		}

		public void draw(Canvas canvas) {
			if (mChilds != null) {
	            final int count = mChilds.size();
	            for (int i = 0; i < count; i++) {
	                MyInternalView v = mChilds.get(i);
	                v.draw(canvas);
	            }
	        }
		}

		public boolean onTouchEvent(MotionEvent event) {
			if (mChilds != null) {
	            final int count = mChilds.size();
				final int action = event.getAction();
				final int x = (int) event.getX();
				final int y = (int) event.getY();
				MyInternalView myInternalView = null;
				MyImageButton myImageButton = null;

				switch (action) {
				case MotionEvent.ACTION_CANCEL:
					if (null != mCurrentActivateButton) {
						mCurrentActivateButton.setActivate(false);
						mCurrentActivateButton.setHasPerformedLongPress(false);
						mCurrentActivateButton = null;
						mOnLongClickListener = null;
						mIsLongPress = false;
						return false;
					}
					break;					
				case MotionEvent.ACTION_UP:
					if (null != mCurrentActivateButton) {
						if (mCurrentActivateButton.checkInRange(x, y)) {
							mCurrentActivateButton.onClick(MyTestView.this);
						}
						mCurrentActivateButton.setActivate(false);
						mCurrentActivateButton.setHasPerformedLongPress(false);
						mCurrentActivateButton = null;
						mOnLongClickListener = null;
						mIsLongPress = false;
						return true;
					}
					break;

				case MotionEvent.ACTION_DOWN:
					if (null != mCurrentActivateButton) {
						mCurrentActivateButton.setActivate(false);
						mCurrentActivateButton = null;
						mOnLongClickListener = null;
					}
					for (int i = 0; i < count; i++) {
						myInternalView = mChilds.get(i);					
						if ( myInternalView instanceof MyImageButton) {
							myImageButton = (MyImageButton)myInternalView;
							if (myImageButton.checkInRange(x, y)) {
								mCurrentActivateButton = myImageButton;
								mCurrentActivateButton.setActivate(true);
								mCurrentActivateButton.setHasPerformedLongPress(false);
								mOnLongClickListener = mCurrentActivateButton.mOnLongClickListener;
								mIsLongPress = true;
								return true;
							}
						}
					}
					break;
				}				
			}
				
			return false;
		}

		public void run() {
		}
	}

	private class MyPlayingInterface extends MyInterface {
		private int moveX;
		private int moveY;
		private	int	clickX;
		private int	clickY;
		private double dLastAngle;
		public boolean checkInRangeForAlbumCover(int x1, int y1) {
			int x = x1 > nCenterX ? x1 - nCenterX : nCenterX - x1;
			int y = y1 > nCenterY ? y1 - nCenterY : nCenterY - y1;

			if (x * x + y * y < nCurrentCDRadius * nCurrentCDRadius
					&& x * x + y * y > nCDCenterImageRadius * nCDCenterImageRadius) {
				return true;
			} else {
				return false;
			}
		}

		private void finishSetProgress(double dCurrAngle) {
			double ddAngle =  dCurrAngle - dLastAngle;
			ddAngle = ddAngle < 0 - Math.PI ? ddAngle + dTwoPI : ddAngle;
			ddAngle = ddAngle > Math.PI ? ddAngle - dTwoPI : ddAngle;
			if ((ddAngle>0 && dCurrentProgress<1) || (ddAngle<0 && dCurrentProgress>0)) {
				nCurrentBgAngle = (int)(ddAngle / dTwoPI * 360) + nCurrentBgAngle;
				nCurrentBgAngle = nCurrentBgAngle > 360 ? nCurrentBgAngle - 360 : nCurrentBgAngle;
				nCurrentBgAngle = nCurrentBgAngle < 0   ? nCurrentBgAngle + 360 : nCurrentBgAngle;
				dCurrentProgress = ddAngle / dTwoPI * dRotatePer + dCurrentProgress;
				dCurrentProgress = dCurrentProgress > 1 ? 1 : dCurrentProgress;
				dCurrentProgress = dCurrentProgress < 0 ? 0 : dCurrentProgress;
			}
			resetProgressMsgInfo();
		}

		public void cancleCurrentPressed() {
			super.cancleCurrentPressed();
			mProgressMsg.setVisible(false);
		}
		
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub

			if (bPlaybackComplete) {
				if (null != mAlbumBackgroundBitmap) {
					mMatrix.reset();
					mMatrix.setTranslate(nCenterX - mAlbumBackgroundBitmap.getWidth() / 2,
							nCenterY - mAlbumBackgroundBitmap.getHeight() / 2);
					canvas.drawBitmap(mAlbumBackgroundBitmap, mMatrix, mPaint);
				} else {
					return ;
				}
			} else {
				drawCurrCDCover(canvas);
			}
			
			super.draw(canvas);

			drawCircleProgressBarShadow(canvas);
			
			mProgressMsg.draw(canvas);
		}

		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			double dAngle;
			
			super.onTouchEvent(event);

			dAngle = Math.atan((double) (x - nCenterX) / (y - nCenterY));
			dAngle = y < nCenterY ? dTwoPI - dAngle : Math.PI - dAngle;
			dAngle = dAngle > dTwoPI ? dAngle - dTwoPI : dAngle;

			switch (action) {
			case MotionEvent.ACTION_CANCEL:
				bStartDrag = false;
				mProgressMsg.setVisible(false);
				break;
				
			case MotionEvent.ACTION_UP:
				if (checkInRangeForAlbumCover(x, y)) {
					if (bStartDrag) {
						bStartDrag = false;
						finishSetProgress(dAngle);
						mProgressMsg.setVisible(false);
						if (mOnProgressChangeListener != null) {
							mOnProgressChangeListener.onProgressChanged(
									MyTestView.this,
									(int) (dCurrentProgress * nMaxProgress),
									false);
							mOnProgressChangeListener
									.onStopTrackingTouch(MyTestView.this);
						}

					} else {
						if (null != mOnPlayPauseClickListener) {
							mOnPlayPauseClickListener.onClick(MyTestView.this);
						}
					}
				}
				break;

			case MotionEvent.ACTION_DOWN:
				bStartDrag = false;
				dLastAngle = dAngle;
				moveX = x;
				moveY = y;
				clickX = x;
				clickY = y;
				break;

			case MotionEvent.ACTION_MOVE:
				if (bAllowDrag) {
					if (!checkInRangeForAlbumCover(x, y)) {
						if (bStartDrag) {
							bStartDrag = false;
							mProgressMsg.setVisible(false);
							if (mOnProgressChangeListener != null) {
								mOnProgressChangeListener.onProgressChanged(
										MyTestView.this,
										(int) (dCurrentProgress * nMaxProgress),
										false);
								mOnProgressChangeListener
										.onStopTrackingTouch(MyTestView.this);

							}
						}
					} else {
						if (!bStartDrag 
							&& ((Math.abs(y - moveY) > nTouchSlop || Math.abs(x - moveX) > nTouchSlop)
							|| (Math.abs(y - clickY) > nTouchSlop || Math.abs(x - clickX) > nTouchSlop))) {
							bStartDrag = true;
							mProgressMsg.setVisible(true);
							if (mOnProgressChangeListener != null) {
								mOnProgressChangeListener
										.onStartTrackingTouch(MyTestView.this);
								
							}
							//Log.v(TAG, "x="+x+"; y="+y+"; moveX="+moveX+"; moveY="+moveY+"; clickX="+clickX+"; clickY="+clickY);
						}
					}
					if (bStartDrag) {
						finishSetProgress(dAngle);
						dLastAngle = dAngle;
					}
				}
				moveX = x;
				moveY = y;
				break;
			}

			return true;
		}

		public void run() {
			// TODO Auto-generated method stub

		}

	}

	private MyPlayingInterface myPlayingInterface = new MyPlayingInterface();

	private class MyPanelInterface extends MyInterface {
		private int nControlPanelLeft;
		private int nControlPanelTop;
		private int nControlPanelRight;
		private int nControlPanelBottom;

		public void initData() {
			nControlPanelLeft = nCenterX - nAnimMaxHalfPanelWidth;
			nControlPanelTop = nCenterY - nAnimMaxHalfPanelHeight;
			nControlPanelRight = nCenterX + nAnimMaxHalfPanelWidth;
			nControlPanelBottom = nCenterY + nAnimMaxHalfPanelHeight;
		}

		public boolean checkInRangeForControlPanel(int x1, int y1) {
			if (x1 > nControlPanelLeft && x1 < nControlPanelRight
					&& y1 > nControlPanelTop && y1 < nControlPanelBottom) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean checkInRangeForAlbumCover(int x1, int y1) {
			int x = x1 > nCenterX ? x1 - nCenterX : nCenterX - x1;
			int y = y1 > nCenterY ? y1 - nCenterY : nCenterY - y1;

			if (x * x + y * y < nCurrentCDRadius * nCurrentCDRadius
					&& x * x + y * y > nCDCenterImageRadius * nCDCenterImageRadius) {
				return true;
			} else {
				return false;
			}
		}
		
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			drawCurrCDCover(canvas);
			
			super.draw(canvas);

			drawCircleProgressBarShadow(canvas);
			
			mProgressMsg.draw(canvas);
		}

		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			if (super.onTouchEvent(event) && (STATUS_PANEL == nCurrentStatus)) {
				resetDelayedClosePanelHandler();
			}

			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();

			switch (action) {
			case MotionEvent.ACTION_UP:
				if (checkInRangeForAlbumCover(x, y) && !checkInRangeForControlPanel(x, y)) {
					if (null != mOnPlayPauseClickListener) {
						mOnPlayPauseClickListener.onClick(MyTestView.this);
						resetDelayedClosePanelHandler();
					}
				}
				break;
			}
			
			return true;
		}

		public void run() {
			// TODO Auto-generated method stub

		}
	}
	
	private MyPanelInterface myPanelInterface = new MyPanelInterface();

	private Handler mDelayedClosePanelHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (myPanelInterface.getIsLongPress()) {
				resetDelayedClosePanelHandler();
			} else {
				nCurrentStatus = STATUS_PLAYING | ANIMATION_1;
				bPlayingAnimation = true;
			}
		}
	};

	private void startDelayedClosePanelHandler() {
        Message msg = mDelayedClosePanelHandler.obtainMessage();
        mDelayedClosePanelHandler.sendMessageDelayed(msg, PANEL_IDLE_DELAY);
	}

	private void stopDelayedClosePanelHandler() {
        mDelayedClosePanelHandler.removeCallbacksAndMessages(null);
		mDelayedClosePanelHandler.removeCallbacksAndMessages(null);
	}
	private void resetDelayedClosePanelHandler() {
		stopDelayedClosePanelHandler();
        startDelayedClosePanelHandler();
	}
	
	private class MyLibraryInterface extends MyInterface {
		private Path mPath = new Path();
		private RectF ova = new RectF();
		private int nBgColor = Color.BLACK;
		private int nShaderTop;
		private int nShaderBottom;
		private int nShaderRight;
		private int nShaderOffsetX;


		public void initData() {
			int top;
			if (bIsHorizontal) {
				top = (int)(fCurrentGlobalScale * 24);
			} else {
				top = (int)(fCurrentGlobalScale * 34);
			}	
			int left = nCenterX - (int)(fCurrentGlobalScale * (114));	// 101 = 224 / 2 + 2;
			int right = nCenterX + (int)(fCurrentGlobalScale * (114));
			int bottom = top + (int)(fCurrentGlobalScale * 25);
			ova.set(left, top, right, bottom);
			mPath.addRoundRect(ova, (fCurrentGlobalScale * 12.5f), (fCurrentGlobalScale * 12.5f), Path.Direction.CW);;

			nShaderTop = top;
			nShaderBottom = bottom;
			nShaderRight = nCenterX + (int)(fCurrentGlobalScale * 112);
			nShaderOffsetX = (int)(fCurrentGlobalScale *(-30));
		}
		public void draw(Canvas canvas) {
			canvas.drawColor(nBgColor);		
			super.draw(canvas);
			
			int nShaderWidth = (int)(fCurrentGlobalScale * (224 - 15) * (1-dCurrentProgress)); // 15 = 29 / 2 + 1;
			int nShaderLeft = nShaderRight - nShaderWidth + nShaderOffsetX;
			ova.set(nShaderLeft, nShaderTop, nShaderRight, nShaderBottom);

			int saveCount = canvas.getSaveCount();			
			canvas.save();
			canvas.clipPath(this.mPath);	
			if (dCurrentProgress > 0) {
				Shader s = new LinearGradient(nShaderLeft, nShaderTop, nShaderLeft+(int)(fCurrentGlobalScale*40), nShaderTop, 0x1ccd12, Color.BLACK, Shader.TileMode.CLAMP);  
				mPaint.setShader(s);
			}
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(Color.BLACK);
			canvas.drawRect(ova, mPaint);
			mPaint.setShader(null);
			canvas.restoreToCount(saveCount);
			
			mBackButton.draw(canvas);
			mCurrTitle.draw(canvas);
		}
		
		public boolean onTouchEvent(MotionEvent event) {
		    mlist.onTouchEvent(event);
		    return super.onTouchEvent(event);
		}
	}

	private MyLibraryInterface myLibraryInterface = new MyLibraryInterface();

	private class MyChooseInterface extends MyInterface {
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			super.draw(canvas);
		}

		public boolean onTouchEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			return true;
		}

		public void run() {
			// TODO Auto-generated method stub

		}
	}

	private MyChooseInterface myChooseInterface = new MyChooseInterface();

	private MyInterface myAnimation0 = new MyInterface() {
		private	Rect src = new Rect();
		private	RectF dst = new RectF();

		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			drawCurrCDCover(canvas);

			Bitmap b = mPlayPanelImage.getBitmap();
			if (null != b) {
				src.set(0, 0, nAnimation0Left+nAnimCurrentHalfPanelWidth-nAnimation0ImgOffsetX, nAnimation0ImgHeight);
				dst.set(nCenterX-nAnimCurrentHalfPanelWidth-nAnimation0Left, nCenterY-nAnimation0ImgOffsetY, nCenterX-nAnimation0ImgOffsetX, nCenterY+nAnimation0ImgOffsetY);
				canvas.drawBitmap(b, src, dst, mPaint);

				src.set(nAnimation0HalfImgWidth-nAnimation0ImgOffsetX, 0, nAnimation0HalfImgWidth+nAnimation0ImgOffsetX, nAnimation0ImgHeight );
				dst.set(nCenterX-nAnimation0ImgOffsetX, nCenterY-nAnimation0ImgOffsetY, nCenterX+nAnimation0ImgOffsetX, nCenterY+nAnimation0ImgOffsetY);
				canvas.drawBitmap(b, src, dst, mPaint);

				src.set(nAnimation0ImgWidth-nAnimation0Left-nAnimCurrentHalfPanelWidth+nAnimation0ImgOffsetX, 0, nAnimation0ImgWidth, nAnimation0ImgHeight);
				dst.set(nCenterX+nAnimation0ImgOffsetX, nCenterY-nAnimation0ImgOffsetY, nCenterX+nAnimCurrentHalfPanelWidth+nAnimation0Left, nCenterY+nAnimation0ImgOffsetY);
				canvas.drawBitmap(b, src, dst, mPaint);
			}
		
			super.draw(canvas);
		
			drawCircleProgressBarShadow(canvas);
		}

		public void run() {
			// TODO Auto-generated method stub
			if (nAnimCurrentHalfPanelWidth < nAnimMaxHalfPanelWidth) {
				nAnimCurrentHalfPanelWidth = nAnimCurrentHalfPanelWidth + nAnimCurrentPanelMoveSpeed;
			}

			if (nAnimCurrentHalfPanelWidth > nAnimMaxHalfPanelWidth) {
				nAnimCurrentHalfPanelWidth = nAnimMaxHalfPanelWidth;
			}

			if (nAnimCurrentHalfPanelWidth == nAnimMaxHalfPanelWidth) {
				startDelayedClosePanelHandler();
				nCurrentStatus &= ~ANIMATION_MASK;
				bPlayingAnimation = false;
			}
		}
	};

	private MyInterface myAnimation1 = new MyInterface() {
		private	Rect src = new Rect();
		private	RectF dst = new RectF();
		
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			myAnimation0.draw(canvas);
		}

		public void run() {
			// TODO Auto-generated method stub
			if (nAnimCurrentHalfPanelWidth > nAnimMinHalfPanelWidth) {
				nAnimCurrentHalfPanelWidth = nAnimCurrentHalfPanelWidth - nAnimCurrentPanelMoveSpeed;
			}

			if (nAnimCurrentHalfPanelWidth < nAnimMinHalfPanelWidth) {
				nAnimCurrentHalfPanelWidth = nAnimMinHalfPanelWidth;
			}

			if (nAnimCurrentHalfPanelWidth == nAnimMinHalfPanelWidth) {
				nCurrentStatus &= ~ANIMATION_MASK;
				bPlayingAnimation = false;
			}
		}
	};

	private MyInterface myAnimation2 = new MyInterface() {
		private static final int nContinueTime = 5;
		private int nCurrentTime;
		private boolean bDrawPlaying;

		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			if (nAnimCurrentHalfPanelWidth > nAnimMinHalfPanelWidth) {
				myAnimation0.draw(canvas);
			} else {
				myPlayingInterface.draw(canvas);
			}
		}

		public void run() {
			// TODO Auto-generated method stub
			if (nAnimCurrentHalfPanelWidth > nAnimMinHalfPanelWidth) {
				bDrawPlaying = false;
			}

			if (false == bDrawPlaying) {
				if (nAnimCurrentHalfPanelWidth > nAnimMinHalfPanelWidth) {
					nAnimCurrentHalfPanelWidth = nAnimCurrentHalfPanelWidth - nAnimCurrentPanelMoveSpeed;
				}
				if (nAnimCurrentHalfPanelWidth < nAnimMinHalfPanelWidth) {
					nAnimCurrentHalfPanelWidth = nAnimMinHalfPanelWidth;
				}
				if (nAnimCurrentHalfPanelWidth == nAnimMinHalfPanelWidth) {
					nCurrentTime = 0;
					bDrawPlaying = true;
				}
			} else {
				if (nCurrentTime < nContinueTime) {
					nCurrentTime++;
				}
				if (nCurrentTime > nContinueTime) {
					nCurrentTime = nContinueTime;
				}
				if (nCurrentTime == nContinueTime) {
					fAnimation3CDRadiusScale = 1.0f;
					nAnimation3CDRadius = nCurrentCDRadius;
					nCurrentStatus &= ~ANIMATION_MASK;
					nCurrentStatus = nCurrentStatus | ANIMATION_3;
					bPlayingAnimation = true;
				}
			}
		}

	};

	private MyInterface myAnimation3 = new MyInterface() {
		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			Bitmap mCurrCDBitmap = (null==mCurrCDInfo ? null : mCurrCDInfo.getBitmap());
			if (null != mCurrCDBitmap) {
				float dx = nCenterX - mCurrCDBitmap.getWidth() / 2
						* fAnimation3CDRadiusScale;
				float dy = nCenterY - mCurrCDBitmap.getHeight() / 2
						* fAnimation3CDRadiusScale;
				mMatrix.reset();
				mMatrix.setTranslate(dx, dy);			
				mMatrix.postScale(fAnimation3CDRadiusScale,
						fAnimation3CDRadiusScale, dx, dy);
				int saveCount = canvas.getSaveCount();
				canvas.save();
				canvas.clipPath(mPath);
				canvas.drawBitmap(mCurrCDBitmap, mMatrix, mPaint);
				canvas.restoreToCount(saveCount);
				
				if (null != mMaskBitmap) {					
					canvas.drawBitmap(mMaskBitmap, mMatrix, mPaint);
					mPaint.setXfermode(mModes);
					canvas.drawBitmap(mMaskBitmap, mMatrix, mPaint);
					mPaint.setXfermode(null);
				}
				if (null != mLightBitmap) {
					canvas.drawBitmap(mLightBitmap, mMatrix, mPaint);
				}
			}
			
			super.draw(canvas);
		}

		public void run() {
			if (nAnimation3CDRadius > nCurrentSelScaleCDRadius) {
				nAnimation3CDRadius = nAnimation3CDRadius - nAnimation3CDScaleSpeed;
			}

			if (nAnimation3CDRadius < nCurrentSelScaleCDRadius) {
				nAnimation3CDRadius = nCurrentSelScaleCDRadius;
			}

			fAnimation3CDRadiusScale = (float) nAnimation3CDRadius
					/ nCurrentCDRadius;

			if (nAnimation3CDRadius == nCurrentSelScaleCDRadius) {
				nAnimation4CDOffsetX = 0;
				nCurrentStatus &= ~ANIMATION_MASK;
				nCurrentStatus = nCurrentStatus | ANIMATION_4;
				bPlayingAnimation = true;
			}
		}
	};

	private MyInterface myAnimation4 = new MyInterface() {
		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			Bitmap mCurrCDBitmap = (null==mCurrCDInfo ? null : mCurrCDInfo.getBitmap());
			if (null != mCurrCDBitmap) {
				float dx = nCenterX - nAnimation4CDOffsetX
						- nCurrentSelScaleCDRadius;
				float dy = nCenterY - nCurrentSelScaleCDRadius;
				mMatrix.reset();
				mMatrix.setTranslate(dx, dy);
				mMatrix.postScale(fAnimation3CDRadiusScale, fAnimation3CDRadiusScale, dx, dy);			
				canvas.drawBitmap(mCurrCDBitmap, mMatrix, mPaint);

				if (null != mMaskBitmap) {					
					canvas.drawBitmap(mMaskBitmap, mMatrix, mPaint);
					mPaint.setXfermode(mModes);
					canvas.drawBitmap(mMaskBitmap, mMatrix, mPaint);
					mPaint.setXfermode(null);
				}				
				if (null != mLightBitmap) {
					canvas.drawBitmap(mLightBitmap, mMatrix, mPaint);
				}
			}
			super.draw(canvas);
		}

		public void run() {
			if (nAnimation4CDOffsetX < nAnimation4MaxCDOffsetX) {
				nAnimation4CDOffsetX = nAnimation4CDOffsetX + nAnimation4CDOffsetSpeed;
			}

			if (nAnimation4CDOffsetX > nAnimation4MaxCDOffsetX) {
				nAnimation4CDOffsetX = nAnimation4MaxCDOffsetX;
			}

			if (nAnimation4CDOffsetX == nAnimation4MaxCDOffsetX) {
				nAnimation5CDOffsetY = 0;
				nCurrentStatus &= ~ANIMATION_MASK;
				nCurrentStatus = nCurrentStatus | ANIMATION_5;
				bPlayingAnimation = true;
			}
		}
	};

	private MyInterface myAnimation5 = new MyInterface() {

		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			float dx;
			float dy;
			
			Bitmap mPrevCDScaleBitmap = mlist.getIndexBitmap(mlist.getFocusIndex()-1);
			//if (null == mPrevCDScaleBitmap) {
			//	mPrevCDScaleBitmap = mAlbumBackgroundScaleBitmap;
			//}

			if (null != mPrevCDScaleBitmap) {
				dx = nCenterX - nAnimation4CDOffsetX
						- mPrevCDScaleBitmap.getWidth() / 2;
				dy = nCenterY - nAnimation5CDOffsetY
						- mPrevCDScaleBitmap.getHeight() / 2;
				mMatrix.reset();
				mMatrix.setTranslate(dx, dy);
				canvas.drawBitmap(mPrevCDScaleBitmap, mMatrix, mPaint);
			}
			Bitmap mNextCDScaleBitmap = mlist.getIndexBitmap(mlist.getFocusIndex()+1);
			//if (null == mNextCDScaleBitmap) {
			//	mNextCDScaleBitmap = mAlbumBackgroundScaleBitmap;
			//}

			if (null != mNextCDScaleBitmap) {
				dx =  nCenterX - nAnimation4CDOffsetX - mNextCDScaleBitmap.getWidth()
						/ 2;
				dy = nCenterY + nAnimation5CDOffsetY
						- mNextCDScaleBitmap.getHeight() / 2;
				mMatrix.reset();
				mMatrix.setTranslate(dx, dy);
				canvas.drawBitmap(mNextCDScaleBitmap, mMatrix, mPaint);
			}
			
			dx =  nCenterX - nAnimation4CDOffsetX - nCurrentSelScaleCDRadius;
			dy = nCenterY - nCurrentSelScaleCDRadius;
			//Log.v(TAG, "myListView:selleft="+dx+"; seltop="+dy );
			if (null != mSelScaleCDBitmap) {
				canvas.drawBitmap(mSelScaleCDBitmap, dx, dy, mPaint);
			} else {
				if (null != mDefaultCDBitmap) {
					final int nShort = 2*nCurrentSelScaleCDRadius;
					mDrawSrcRect.set(0, 0, mDefaultCDBitmap.getWidth(), mDefaultCDBitmap.getHeight());
					mDrawDstRectF.set(dx, dy, dx+nShort, dy+nShort);
					canvas.drawBitmap(mDefaultCDBitmap, mDrawSrcRect, mDrawDstRectF, mPaint);
					if (null != mLightBitmap) {
						mAdjustSrcRect.set(0, 0, mLightBitmap.getWidth(), mLightBitmap.getHeight());
						canvas.drawBitmap(mLightBitmap, mDrawSrcRect, mDrawDstRectF, mPaint);
					}
				}
			}
			
			super.draw(canvas);
		}

		public void run() {
			if (nAnimation5CDOffsetY < nAnimation5MaxCDOffsetY) {
				nAnimation5CDOffsetY = nAnimation5CDOffsetY + nAnimation5CDOffsetSpeed;
			}

			if (nAnimation5CDOffsetY > nAnimation5MaxCDOffsetY) {
				nAnimation5CDOffsetY = nAnimation5MaxCDOffsetY;
			}

			if (nAnimation5CDOffsetY == nAnimation5MaxCDOffsetY) {
				nCurrentStatus &= ~ANIMATION_MASK;
				bPlayingAnimation = false;
			}
		}
	};

	private MyInterface myAnimation6 = new MyInterface() {
		private final int nMaxOffsetY = 180;
		private int nCurrentOffsetY;
		private int nCurrentAlpha;
		private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);;

		private int nDirection = 0;
		public void setDirection(int direction) {
			this.nDirection = direction;
		}

		public void initData() {
			this.nCurrentOffsetY = 0;
			this.nCurrentAlpha = 0;
		}

		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			int nOffsetY;
			Bitmap mLastCDBitmap = (null==mLastCDInfo ? null : mLastCDInfo.getBitmap());
			Bitmap mCurrCDBitmap = (null==mCurrCDInfo ? null : mCurrCDInfo.getBitmap());
			int x = nCenterX - mCurrCDBitmap.getWidth() / 2;
			int y = nCenterY - mCurrCDBitmap.getHeight() / 2; 

			if (null != mLastCDBitmap) {
				nOffsetY = (0 == nDirection ? -this.nCurrentOffsetY : this.nCurrentOffsetY);
				mMatrix.reset();
				mMatrix.setTranslate(x, y+nOffsetY);			
				mMatrix.postRotate(nCurrentBgAngle, nCenterX, nCenterY);
				mPaint.setAlpha(this.nCurrentAlpha);
				canvas.drawBitmap(mLastCDBitmap, mMatrix, this.mPaint);				
			}
			
			if (null != mCurrCDBitmap) { 
				nOffsetY = (0 == nDirection ? nMaxOffsetY-this.nCurrentOffsetY : this.nCurrentOffsetY-nMaxOffsetY);
				mMatrix.reset();
 				mMatrix.setTranslate(x,y+nOffsetY);
				this.mPaint.setAlpha(255 - this.nCurrentAlpha);
				canvas.drawBitmap(mCurrCDBitmap, mMatrix, this.mPaint);
			}
		}

		public void run() {
			if (this.nCurrentOffsetY < this.nMaxOffsetY) {
				this.nCurrentOffsetY = this.nCurrentOffsetY + 10;
			}

			if (this.nCurrentOffsetY > this.nMaxOffsetY) {
				this.nCurrentOffsetY = this.nMaxOffsetY;
			}

			if (this.nCurrentOffsetY == this.nMaxOffsetY) {
				nCurrentStatus &= ~ANIMATION_MASK;
				recycleLastCDBitmap();
			}
			this.nCurrentAlpha = (int)(((float)this.nCurrentOffsetY / this.nMaxOffsetY) * 255);
		}
	};

	private class MusicInfo {
        public long id;
        public long albumid;
        public String title;
		public String adjustedTitle;
		public boolean mIsScaleBitmap;
		public boolean mIsBmpRecyclebyOut = true;
        private Bitmap bitmap;

		
        public void setBitmap(Bitmap bitmap){
            this.bitmap = bitmap;
        }

        public Bitmap getBitmap() {
            if (bitmap == null){
				if (mIsScaleBitmap) {
					return MyTestView.this.mDefaultCDScaleBitmap;
					
				} else {
					return MyTestView.this.mDefaultCDBitmap;
				}
            }
            return bitmap;
        }

		public void recycleBitmap(){
			if (null != bitmap && false == mIsBmpRecyclebyOut) {
				Bitmap tmpBmp = this.bitmap;
				this.bitmap = null;				
				tmpBmp.recycle();
			}
        }
        
    }

	private class MyMusicUtils {
		private final String[] ccols = new String[] { 
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.TITLE
				};
		 
		private final String SQL = MediaStore.Audio.Media._ID + "= @ AND " + MediaStore.Audio.Media.IS_MUSIC + "=1";
		private final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		private final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

		private long nCurrentSuccessLoadAlbumId = -1;
		public void loadMusicInfo(MusicInfo info, boolean bIsScaleBitmap){
			if (null == info) {
				return ;
			}

			final long id = info.id;
			String where = SQL.replace("@", Long.toString(id));
			Cursor cursor = query(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				   ccols, where, null, null);
			info.albumid = -1;
			if (cursor != null && cursor.getCount() > 0) {	 
				cursor.moveToFirst();
				info.albumid = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				info.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				cursor.close();
			}
			if (null == info.title) {
				info.title = "";
			}
			if (bIsScaleBitmap) {
				if (info.albumid<0) {
					info.bitmap = adjustScaleCDBitmap(getArtwork(getContext(), info.id, info.albumid));
					info.mIsBmpRecyclebyOut = false;
				} else {
					info.bitmap = mAlbumTable.get(new Long(info.albumid));
					if (null == info.bitmap) {
						info.bitmap = adjustScaleCDBitmap(getArtwork(getContext(), info.id, info.albumid));
						if (nCurrentSuccessLoadAlbumId == info.albumid && null != info.bitmap) {
							mAlbumTable.put(new Long(info.albumid), info.bitmap);
						} else {
							info.mIsBmpRecyclebyOut = false;
						}
						nCurrentSuccessLoadAlbumId = -1;
					}
				}
			} else {
				info.bitmap = adjustCDCover(getArtwork(getContext(), info.id, info.albumid));
				info.mIsBmpRecyclebyOut = false;
			}
			info.mIsScaleBitmap = bIsScaleBitmap;
		}
		
		public void loadCurrentMusicInfo(long id){
			if (null != mLastCDInfo) {
				mLastCDInfo.recycleBitmap();
				mLastCDInfo = mCurrCDInfo;
			}
			
			String where = SQL.replace("@", Long.toString(id));
			Cursor cursor = query(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				   ccols, where, null, null);
			MusicInfo tmpInfo = new MusicInfo();
			tmpInfo.albumid = -1;
			tmpInfo.id = id;
			if (cursor != null && cursor.getCount() > 0) {	 
				cursor.moveToFirst();
				tmpInfo.albumid = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				tmpInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				cursor.close();
			}
		
			if (null != mLastCDInfo) {
				if (tmpInfo.albumid > 0 && tmpInfo.albumid == mLastCDInfo.albumid) {
					tmpInfo.bitmap = mLastCDInfo.bitmap;
					mLastCDInfo.mIsBmpRecyclebyOut = true;
				}
			} else {
				tmpInfo.bitmap = adjustCDCover(getArtwork(getContext(), tmpInfo.id, tmpInfo.albumid));
				Bitmap tmpBmp = mSelScaleCDBitmap;
				mSelScaleCDBitmap = createSelScaleCDBitmap(tmpInfo.bitmap);
				if (null != tmpBmp) {
					tmpBmp.recycle();
				}
				mlist.resetSelImageInitFlag();
			}
			tmpInfo.mIsScaleBitmap = false;
			tmpInfo.mIsBmpRecyclebyOut = false;
			
			mCurrCDInfo = tmpInfo;
		}
		
		public Bitmap getArtwork(Context context, long song_id, long album_id) {
			return getArtwork(context, song_id, album_id, true);
		}
		 
		public Bitmap getArtwork(Context context, long song_id, long album_id,
				boolean allowdefault) {
		 
			 if (album_id < 0) {
				 // This is something that is not in the database, so get the album art directly
				 // from the file.
				 if (song_id >= 0) {
					 Bitmap bm = getArtworkFromFile(context, song_id, -1);
					 if (bm != null) {
						 return bm;
					 }
				 }
				 if (allowdefault) {
					 return null;
				 }
				 return null;
			 }
		 
			 ContentResolver res = context.getContentResolver();
			 Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
			 if (uri != null) {
				 InputStream in = null;
				 try {
					 in = res.openInputStream(uri);
					 nCurrentSuccessLoadAlbumId = album_id;
					 return BitmapFactory.decodeStream(in, null, sBitmapOptions);
				 } catch (FileNotFoundException ex) {
					 // The album art thumbnail does not actually exist. Maybe the user deleted it, or
					 // maybe it never existed to begin with.
					 Bitmap bm = getArtworkFromFile(context, song_id, album_id);
					 if (bm != null) {
						 if (bm.getConfig() == null) {
							 bm = bm.copy(Bitmap.Config.RGB_565, false);
							 if (bm == null && allowdefault) {
								 return null;
							 }
						 }
					 } else if (allowdefault) {
						 bm = null;
					 }
					 return bm;
				 } finally {
					 try {
						 if (in != null) {
							 in.close();
						 }
					 } catch (IOException ex) {
					 }
				 }
			 }
			 
			 return null;
		 }
		 
		 private Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
			Bitmap bm = null;
			byte[] art = null;
			String path = null;
		
			if (albumid < 0 && songid < 0) {
				//throw new IllegalArgumentException("Must specify an album or a song id");
				return null;
			}
		
			try {
				if (albumid < 0) {
					Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
					ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
					if (pfd != null) {
						FileDescriptor fd = pfd.getFileDescriptor();
						bm = BitmapFactory.decodeFileDescriptor(fd);
					}
				} else {
					Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
					ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
					if (pfd != null) {
						FileDescriptor fd = pfd.getFileDescriptor();
						bm = BitmapFactory.decodeFileDescriptor(fd);
					}
				}
			} catch (FileNotFoundException ex) {
				//
			} catch (IllegalStateException ex) {
				Log.w(TAG, "getArtworkFromFile:catch IllegalStateException");
			}
			return bm;
		}
		private Cursor query(Context context, Uri uri, String[] projection,
				String selection, String[] selectionArgs, String sortOrder) {
			try {
				ContentResolver resolver = context.getContentResolver();
				if (resolver == null) {
					return null;
				}
				return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (UnsupportedOperationException ex) {
				return null;
			}
		}
	}

	public void setOnProgressChangeListener(OnProgressChangeListener l) {
		mOnProgressChangeListener = l;
	}

	@android.view.RemotableViewMethod
	public void setOnLibraryButtonClickListener(OnClickListener l) {
		mLibraryButton.setOnClickListener(l);
	}

	@android.view.RemotableViewMethod
	public void setOnPlayPauseClickListener(OnClickListener l) {
		mOnPlayPauseClickListener = l;
	}


	@android.view.RemotableViewMethod
	public void setOnShuffleClickListener(OnClickListener l) {
		mShuffleButton.setOnClickListener(l);
	}

	@android.view.RemotableViewMethod
	public void setOnPreviousClickListener(OnClickListener l) {
		mPreviousButton.setOnClickListener(l);
	}

	@android.view.RemotableViewMethod
	public void setOnNextClickListener(OnClickListener l) {
		mNextButton.setOnClickListener(l);
	}

	@android.view.RemotableViewMethod
	public void setOnRepeatClickListener(OnClickListener l) {
		mRepeatButton.setOnClickListener(l);
	}
	
	@android.view.RemotableViewMethod
	public void setPreviousRepeatListener(RepeatListener l) {
		mPreviousButton.setRepeatListener(l, 500);
	}
	
	@android.view.RemotableViewMethod
	public void setNextRepeatListener(RepeatListener l) {
		mNextButton.setRepeatListener(l, 500);
	}

	@android.view.RemotableViewMethod
	public void setShuffleBtnRes(int resId) {mShuffleButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setShuffleBtnSelRes(int resId) {mShuffleButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setRepeatBtnRes(int resId) {mRepeatButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setRepeatBtnSelRes(int resId) {mRepeatButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setCDBackgroundBmpRes(int resId) {
		mAlbumArtHandler.removeMessages(SET_CD_BG);
		mAlbumArtHandler.obtainMessage(SET_CD_BG, new Long(resId)).sendToTarget();
	}

	@android.view.RemotableViewMethod
	public void setDefaultCDBmpRes(int resId){
		mAlbumArtHandler.removeMessages(SET_CD_DEFAULT);
		mAlbumArtHandler.obtainMessage(SET_CD_DEFAULT, new Long(resId)).sendToTarget();
	}

	@android.view.RemotableViewMethod
	public void setCDLightBmpRes(int resId) {
		mAlbumArtHandler.removeMessages(SET_CD_LIGHT);
		mAlbumArtHandler.obtainMessage(SET_CD_LIGHT, new Long(resId)).sendToTarget();
	}

	@android.view.RemotableViewMethod
	public void setCDMaskBmpRes(int resId) {
		mAlbumArtHandler.removeMessages(SET_CD_MASK);
		mAlbumArtHandler.obtainMessage(SET_CD_MASK, new Long(resId)).sendToTarget();
	}
	
	@android.view.RemotableViewMethod
	public void setCurrentSongId(long id) {
		mAlbumArtHandler.removeMessages(LOAD_CURRENT_MUSIC_INFO);
		mAlbumArtHandler.obtainMessage(LOAD_CURRENT_MUSIC_INFO, new Long(id)).sendToTarget();
	}

	@android.view.RemotableViewMethod
	public void setPlayCtrlBmpRes(int resId) {mPlayCtrlImage.setImageResource(resId);}
	
	@android.view.RemotableViewMethod
	public void setPlayPanelBmpRes(int resId) {mPlayPanelImage.setImageResource(resId);}
	
	@android.view.RemotableViewMethod
	public void setProgressBarRes(int resId) {
		mProgressBarImage.setImageResource(resId);
	}

	@android.view.RemotableViewMethod
	public void setProgressBar2BgRes(int resId) {
		mProgressBar2BgImage.setImageResource(resId);
	}

	@android.view.RemotableViewMethod
	public void setProgressBar2Res(int resId) {
		mProgressBar2Image.setImageResource(resId);
	}

	@android.view.RemotableViewMethod
	public void setPlayCtrlBtnRes(int resId) {mPlayCtrlButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setPlayCtrlBtnSelRes(int resId) {mPlayCtrlButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setPreviousBtnRes(int resId) {mPreviousButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setPreviousBtnSelRes(int resId) {mPreviousButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setNextBtnRes(int resId) {mNextButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setNextSelBtnRes(int resId) {mNextButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setPlayListBtnRes(int resId) {mPlayListButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setPlayListBtnSelRes(int resId) {mPlayListButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setLibraryBtnRes(int resId) {mLibraryButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setLibraryBtnSelRes(int resId) {mLibraryButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setBackBtnRes(int resId) {mBackButton.setImageResource(resId);}
	@android.view.RemotableViewMethod
	public void setBackBtnSelRes(int resId) {mBackButton.setSelectImageResource(resId);}

	@android.view.RemotableViewMethod
	public void setPlayHelperMsgRes(int resId) {
		mPlayHelperMsg.setImageResource(resId);
	}

	@android.view.RemotableViewMethod
	public void setIndeterminate(boolean bFlag) {
	}

	@android.view.RemotableViewMethod
	public void setMax(int nMax) {
		this.nMaxProgress = nMax;
	}

	@android.view.RemotableViewMethod
	public void setProgress(int nProgress) {
		if (!bStartDrag) {
			this.dCurrentProgress = (double)nProgress / this.nMaxProgress;
			if (mProgressMsg.bVisible) {
				resetProgressMsgInfo();
			}
			if (!this.bAllowDrag && (0 != nProgress)) {
				this.bAllowDrag = true;
			}
		}
	}

	@android.view.RemotableViewMethod
	public void setStatus(int nStatus) {
		if (STATUS_PLAYING == nStatus || STATUS_PANEL == nStatus
			|| STATUS_LIBRARY == nStatus || STATUS_CHOOSE == nStatus) {
			bPlayingAnimation = false;
			nCurrentStatus = nStatus;
		}
	}	

	@android.view.RemotableViewMethod
	public void setTitle(String title) {
		mTitle.setText(title);
	}	

	@android.view.RemotableViewMethod
	public void setArtist(String artist) {
		mArtist.setText(artist);
	}

	@android.view.RemotableViewMethod
	public void setCurrTitle(String text) {
		mCurrTitle.setText(text);
	}	

	@android.view.RemotableViewMethod
	public void setUpdateAction(String text) {
		mUpdateAction = text;
	}	

	@android.view.RemotableViewMethod
	public void setStopServiceAction(String text) {
		mStopServiceAction = text;
	}	

	@android.view.RemotableViewMethod
	public void setPlayHelperVisible(boolean bFlag) {
		mPlayHelperMsg.setVisible(bFlag);
	}	

	@android.view.RemotableViewMethod
	public void setCurrentRotateAngle(int nAngle) {
		this.nCurrentBgAngle = nAngle;
	}

	@android.view.RemotableViewMethod
	public void setPlayHelperMsgTxtRes(int resId) {
		mPlayHelperMsg.setText(resId);
	}

	@android.view.RemotableViewMethod
	public void setLibraryBtnTxtRes(int resId) {
		mLibraryButtonTxt.setText(resId);
	}

	@android.view.RemotableViewMethod
	public void setPlaying(boolean bFlag) {
		this.bPlaying = bFlag;
		mPlayHelperMsg.setText(bFlag ? "Tap to pause!" : "Tap to play!");
	}

	@android.view.RemotableViewMethod
	public void setPlaybackComplete(boolean bFlag) {
		this.bPlaybackComplete = bFlag;
	}

	@android.view.RemotableViewMethod
	public void setAllowDrag(boolean bFlag) {
		this.bAllowDrag = bFlag;
	}

	@android.view.RemotableViewMethod
	public void clearMusicList(int id){
		mAlbumArtHandler.removeMessages(ADD_MUSIC_INFO);
		mAlbumArtHandler.obtainMessage(CLEAR_ARTWORK, new Long(0)).sendToTarget();
	}

	@android.view.RemotableViewMethod
	public void resetListScroll(int id){
	    mlist.resetScroll();
	}
	
	@android.view.RemotableViewMethod
	public void addMusicId(long id){
		MusicInfo info = new MusicInfo();
		info.id = id;
		info.bitmap = null;
	   	mAlbumArtHandler.obtainMessage(ADD_MUSIC_INFO, info).sendToTarget();
	}
	
	@android.view.RemotableViewMethod
	public void setSelectItemBack(int res) {
	    mSelectListBackRes = res;
    }    

    @android.view.RemotableViewMethod
    public void setPlayPos(int index){
		mAlbumArtHandler.obtainMessage(SET_PLAY_POS, new Long(index)).sendToTarget();
    }
		
	@android.view.RemotableViewMethod
	public void setListSelectPendingIntent(PendingIntent p) {
		final PendingIntent pendingIntent =  p;
		if (pendingIntent != null) {
			OnListItemClickListener l = new OnListItemClickListener() {
				public void onItemClick(int index) {
					Intent intent = new Intent();
					intent.putExtra("pos", index);
					try {
						getContext().startIntentSender(
								pendingIntent.getIntentSender(),
								intent, Intent.FLAG_ACTIVITY_NEW_TASK,
								Intent.FLAG_ACTIVITY_NEW_TASK, 0);
					} catch (IntentSender.SendIntentException e) {
						Log.e(TAG, "Cannot send pending intent: ", e);
					}
				}
			};
			mlist.setOnListItemClickListener(l);
		}
	}
}

