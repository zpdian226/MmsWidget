
package android.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.QuickContact;
import android.provider.Telephony.Mms;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class CoverFlow extends VerticalGallery {

    private Camera mCamera = new Camera();

    private int mMaxRotationAngle = 70;

    private int mMaxZoom = -500;

    private int mCoveflowCenter;

    private boolean mAlphaMode = true;

    private boolean mCircleMode = true;

    private Context mContext;

    private ImageAdapter mImageAdapter;

    private Handler mHandler = new Handler();

    private ViewGroup mParent;

    private ImageButton mDisplay;

    private ImageButton mWrite;

    private Bundle mBundle;

    private Integer[] imageIds = new Integer[] {};

    private OnClickListener mDisplayClickListener = new OnClickListener() {

        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType("vnd.android-dir/mms-sms");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            Log.d("myTag", "left button");
        }
    };

    private OnClickListener mWriteClickListener = new OnClickListener() {

        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType("vnd.android-dir/mms-sms");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            Log.d("myTag", "right button");
        }
    };

    public CoverFlow(Context context) {
        super(context);
    }

    public CoverFlow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CoverFlow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getMaxRotationAngle() {
        return mMaxRotationAngle;
    }

    public void setMaxRotationAngle(int maxRotationAngle) {
        mMaxRotationAngle = maxRotationAngle;
    }

    public boolean getCircleMode() {
        return mCircleMode;
    }

    public void setCircleMode(boolean isCircle) {
        mCircleMode = isCircle;
    }

    public boolean getAlphaMode() {
        return mAlphaMode;
    }

    public void setAlphaMode(boolean isAlpha) {
        mAlphaMode = isAlpha;
    }

    public int getMaxZoom() {
        return mMaxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        mMaxZoom = maxZoom;
    }

    private int getCenterOfCoverflow() {
        return (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();
    }

    private static int getCenterOfView(View view) {
        return view.getTop() + view.getHeight() / 2;
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        final int childCenter = getCenterOfView(child);
        float rotationAngle = 0;
        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        if (childCenter == mCoveflowCenter) {
            transformImageBitmap((ImageView) child, t, 0, 0);
        } else {
            rotationAngle = (((float) (mCoveflowCenter - childCenter) / 170) * mMaxRotationAngle);
            transformImageBitmap((ImageView) child, t, rotationAngle, 0);
        }
        return true;
    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     * 
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCoveflowCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Transform the Image Bitmap by the Angle passed
     * 
     * @param imageView ImageView the ImageView whose bitmap we want to rotate
     * @param t transformation
     * @param rotationAngle the Angle by which to rotate the Bitmap
     */
    private void transformImageBitmap(ImageView child, Transformation t, float rotationAngle, int d) {
        child.setVisibility(View.VISIBLE);
        mCamera.save();
        final Matrix imageMatrix = t.getMatrix();
        final int imageHeight = child.getLayoutParams().height;
        final int imageWidth = child.getLayoutParams().width;
        final int rotation = Math.abs((int) rotationAngle);
        float zoomAmount = (float) (mMaxZoom + (rotation * 1.0));
        if (mAlphaMode) {
            if (rotationAngle > 0) {
                int alpha = (int) (255 - rotation * 1.5);
                if (rotation < 120) {
                     ((ImageView) (child)).setAlpha(alpha);
                } else {
                    // ((ImageView) (child)).setAlpha(0);
                }
            } else if (rotationAngle < 0) {
                int alpha = (int) (255 - rotation * 1.5);
                if (rotation < 120) {
                     ((ImageView) (child)).setAlpha(alpha);
                } else {
                    // ((ImageView) (child)).setAlpha(0);
                }
            }
            mCamera.translate(0.0f, -(float) (rotationAngle * rotation * rotation * 0.001 * 0.03),
                    zoomAmount);
        }
        mCamera.getMatrix(imageMatrix);
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2.0f));
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2.0f));
        mCamera.restore();
    }

    public void setImageAdapter(ImageAdapter adapter) {
        setAdapter(adapter);
        mImageAdapter = adapter;
    }

    private void reVert() {
        ImageView sd = (ImageView) getSelectedView();
        View v;
        Bitmap bp;
        v = mImageAdapter.messagelayout(getSelectedItemPosition(), -1);
        bp = mImageAdapter.getViewBitmap(v);
        sd.setImageBitmap(bp);
        sd.setLayoutParams(mImageAdapter.mLayoutGallery);
        sd.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        ImageView sd = (ImageView) getSelectedView();
        View v;
        Bitmap bp;
        Runnable runnable = new Runnable() {

            public void run() {
                reVert();
            }

        };
        ViewGroup parent = null;
        parent = (ViewGroup)this.getParent();
        if (e.getX() > mImageAdapter.parameter(MmsWidgetUtils.sreplyx_dimen)
                && e.getX() < mImageAdapter.parameter(MmsWidgetUtils.breplyx_dimen)
                && e.getY() > mImageAdapter.parameter(MmsWidgetUtils.sreplyy_dimen)
                && e.getY() < mImageAdapter.parameter(MmsWidgetUtils.breplyy_dimen)) {
            v = mImageAdapter.messagelayout(getSelectedItemPosition(), ImageAdapter.REPLY);
            bp = mImageAdapter.getViewBitmap(v);
            sd.setImageBitmap(bp);
            sd.setLayoutParams(mImageAdapter.mLayoutGallery);
            sd.setScaleType(ImageView.ScaleType.FIT_XY);
            mHandler.postDelayed(runnable, 1000);
        } else if (e.getX() > mImageAdapter.parameter(MmsWidgetUtils.sdeletex_dimen)
                && e.getX() < mImageAdapter.parameter(MmsWidgetUtils.bdeletex_dimen)
                && e.getY() > mImageAdapter.parameter(MmsWidgetUtils.sdeletey_dimen)
                && e.getY() < mImageAdapter.parameter(MmsWidgetUtils.bdeletey_dimen)) {
            v = mImageAdapter.messagelayout(getSelectedItemPosition(), ImageAdapter.DELETE);
            new AlertDialog.Builder( parent.getContext().getApplicationContext())
                    .setTitle(MmsWidgetUtils.title_string)
                    .setMessage(MmsWidgetUtils.delete_string)
                    .setPositiveButton(MmsWidgetUtils.yes_string,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    Log.e("tag", "yes");
                                }
                            })
                    .setNegativeButton(MmsWidgetUtils.no_string,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub

                                    Log.e("tag", "no");
                                }
                            }).create().show();
            bp = mImageAdapter.getViewBitmap(v);
            sd.setImageBitmap(bp);
            sd.setLayoutParams(mImageAdapter.mLayoutGallery);
            sd.setScaleType(ImageView.ScaleType.FIT_XY);
            mHandler.postDelayed(runnable, 1000);
        } else if (e.getX() > mImageAdapter.parameter(MmsWidgetUtils.sphotox_dimen)
                && e.getX() < mImageAdapter.parameter(MmsWidgetUtils.bphotox_dimen)
                && e.getY() > mImageAdapter.parameter(MmsWidgetUtils.sphotoy_dimen)
                && e.getY() < mImageAdapter.parameter(MmsWidgetUtils.bphotoy_dimen)) {

            Cursor cursor = mImageAdapter.getMmsCursor();
            cursor.moveToPosition(getSelectedItemPosition());
            String number = cursor.getString(cursor.getColumnIndex("address"));

            MmsQuickContactBadge qbg = (MmsQuickContactBadge) parent
                    .findViewById(MmsWidgetUtils.popup_icon_id);
            qbg.setMode(QuickContact.MODE_SMALL);
            qbg.assignContactFromPhone(number, true);
            qbg.onClick(null);
            Log.e("onSingleTapUp", "phone==" + number);
        } else {
            mImageAdapter.messagelayout(getSelectedItemPosition(), ImageAdapter.VIEW_MESSAGE);
        }
        // }if(e.getX()>47 && e.getY() >157 && e.getX() < 213 && e.getY()<213 ){
        // mImageAdapter.messagelayout(getSelectedItemPosition(),ImageAdapter.VIEW_MESSAGE);
        // }if(e.getX()>48 && e.getY() >214 && e.getX() < 274 && e.getY()<284){
        // mImageAdapter.messagelayout(getSelectedItemPosition(),ImageAdapter.VIEW_MESSAGE);
        // }
        return super.onSingleTapUp(e);
    }

    @android.view.RemotableViewMethod
    public void setResBundle(Bundle bundle) {
        mBundle = bundle;
    }

    @android.view.RemotableViewMethod
    public void init(int i) {
        ExtraBundle();
        mParent = (ViewGroup) this.getParent();
        mDisplay = (ImageButton) mParent.findViewById(MmsWidgetUtils.btn_display_id);
        mWrite = (ImageButton) mParent.findViewById(MmsWidgetUtils.btn_write_id);
        mDisplay.setOnClickListener(mDisplayClickListener);
        mWrite.setOnClickListener(mWriteClickListener);
        setGravity(Gravity.CENTER_HORIZONTAL);
        ImageAdapter adapter = new ImageAdapter(mContext, imageIds);
        // cf.setAdapter(adapter);
        setImageAdapter(adapter);
        this.setStaticTransformationsEnabled(true);
        this.setSpacing(45);
        setChildrenDrawingOrderEnabled(true);
    }

    private void ExtraBundle() {
        MmsWidgetUtils.popup_icon_id = mBundle.getInt("popup_icon_id");
        MmsWidgetUtils.card_background_drawable = mBundle.getInt("card_background_drawable");
        MmsWidgetUtils.date_format_string = mBundle.getInt("date_format_string");
        MmsWidgetUtils.icon_sms_drawable = mBundle.getInt("icon_sms_drawable");
        MmsWidgetUtils.unread_string = mBundle.getInt("unread_string");
        MmsWidgetUtils.head_background_drawable = mBundle.getInt("head_background_drawable");
        MmsWidgetUtils.head_default_drawable = mBundle.getInt("head_default_drawable");
        MmsWidgetUtils.msg_uread_drawable = mBundle.getInt("msg_uread_drawable");
        MmsWidgetUtils.msg_divider_drawable = mBundle.getInt("msg_divider_drawable");
        MmsWidgetUtils.replay_rest_drawable = mBundle.getInt("replay_rest_drawable");
        MmsWidgetUtils.replay_press_drawable = mBundle.getInt("replay_press_drawable");
        MmsWidgetUtils.common_delete_drawable = mBundle.getInt("common_delete_drawable");
        MmsWidgetUtils.msg_seprators_drawable = mBundle.getInt("msg_seprators_drawable");
        MmsWidgetUtils.common_delete_press_drawable = mBundle
                .getInt("common_delete_press_drawable");
        MmsWidgetUtils.title_string = mBundle.getInt("title_string");
        MmsWidgetUtils.delete_string = mBundle.getInt("delete_string");
        MmsWidgetUtils.yes_string = mBundle.getInt("yes_string");
        MmsWidgetUtils.no_string = mBundle.getInt("no_string");
        MmsWidgetUtils.btn_display_id = mBundle.getInt("btn_display_id");
        MmsWidgetUtils.btn_write_id = mBundle.getInt("btn_write_id");
        MmsWidgetUtils.relatl_dimen = mBundle.getInt("relatl_dimen");
        MmsWidgetUtils.relatt_dimen = mBundle.getInt("relatt_dimen");
        MmsWidgetUtils.relatr_dimen = mBundle.getInt("relatr_dimen");
        MmsWidgetUtils.relatb_dimen = mBundle.getInt("relatb_dimen");
        MmsWidgetUtils.numberl_dimen = mBundle.getInt("numberl_dimen");
        MmsWidgetUtils.numbert_dimen = mBundle.getInt("numbert_dimen");
        MmsWidgetUtils.numberr_dimen = mBundle.getInt("numberr_dimen");
        MmsWidgetUtils.numberb_dimen = mBundle.getInt("numberb_dimen");
        MmsWidgetUtils.datel_dimen = mBundle.getInt("datel_dimen");
        MmsWidgetUtils.datet_dimen = mBundle.getInt("datet_dimen");
        MmsWidgetUtils.dater_dimen = mBundle.getInt("dater_dimen");
        MmsWidgetUtils.dateb_dimen = mBundle.getInt("dateb_dimen");
        MmsWidgetUtils.mmsiconl_dime = mBundle.getInt("mmsiconl_dimen");
        MmsWidgetUtils.mmsicont_dime = mBundle.getInt("mmsicont_dimen");
        MmsWidgetUtils.mmsiconr_dime = mBundle.getInt("mmsiconr_dimen");
        MmsWidgetUtils.mmsiconb_dime = mBundle.getInt("mmsiconb_dimen");
        MmsWidgetUtils.readmmsl_dimen = mBundle.getInt("readmmsl_dimen");
        MmsWidgetUtils.readmmst_dimen = mBundle.getInt("readmmst_dimen");
        MmsWidgetUtils.readmmsr_dimen = mBundle.getInt("readmmsr_dimen");
        MmsWidgetUtils.readmmsb_dimen = mBundle.getInt("readmmsb_dimen");
        MmsWidgetUtils.photol_dimen = mBundle.getInt("photol_dimen");
        MmsWidgetUtils.photot_dimen = mBundle.getInt("photot_dimen");
        MmsWidgetUtils.photor_dimen = mBundle.getInt("photor_dimen");
        MmsWidgetUtils.photob_dimen = mBundle.getInt("photob_dimen");
        MmsWidgetUtils.unreadl_dimen = mBundle.getInt("unreadl_dimen");
        MmsWidgetUtils.unreadt_dimen = mBundle.getInt("unreadt_dimen");
        MmsWidgetUtils.unreadr_dimen = mBundle.getInt("unreadr_dimen");
        MmsWidgetUtils.unreadb_dimen = mBundle.getInt("unreadb_dimen");
        MmsWidgetUtils.topdividerl_dimen = mBundle.getInt("topdividerl_dimen");
        MmsWidgetUtils.topdividert_dimen = mBundle.getInt("topdividert_dimen");
        MmsWidgetUtils.topdividerr_dimen = mBundle.getInt("topdividerr_dimen");
        MmsWidgetUtils.topdividerb_dimen = mBundle.getInt("topdividerb_dimen");
        MmsWidgetUtils.replayl_dimen = mBundle.getInt("replayl_dimen");
        MmsWidgetUtils.replayt_dimen = mBundle.getInt("replayt_dimen");
        MmsWidgetUtils.replayr_dimen = mBundle.getInt("replayr_dimen");
        MmsWidgetUtils.replayb_dimen = mBundle.getInt("replayb_dimen");
        MmsWidgetUtils.deletel_dimen = mBundle.getInt("deletel_dimen");
        MmsWidgetUtils.deletet_dimen = mBundle.getInt("deletet_dimen");
        MmsWidgetUtils.deleter_dimen = mBundle.getInt("deleter_dimen");
        MmsWidgetUtils.deleteb_dimen = mBundle.getInt("deleteb_dimen");
        MmsWidgetUtils.tiviewl_dimen = mBundle.getInt("tiviewl_dimen");
        MmsWidgetUtils.tiviewt_dimen = mBundle.getInt("tiviewt_dimen");
        MmsWidgetUtils.tiviewr_dimen = mBundle.getInt("tiviewr_dimen");
        MmsWidgetUtils.tiviewb_dimen = mBundle.getInt("tiviewb_dimen");
        MmsWidgetUtils.biviewl_dimen = mBundle.getInt("biviewl_dimen");
        MmsWidgetUtils.biviewt_dimen = mBundle.getInt("biviewt_dimen");
        MmsWidgetUtils.biviewr_dimen = mBundle.getInt("biviewr_dimen");
        MmsWidgetUtils.biviewb_dimen = mBundle.getInt("biviewb_dimen");
        MmsWidgetUtils.textnuml_dimen = mBundle.getInt("textnuml_dimen");
        MmsWidgetUtils.textnumt_dimen = mBundle.getInt("textnumt_dimen");
        MmsWidgetUtils.textnumr_dimen = mBundle.getInt("textnumr_dimen");
        MmsWidgetUtils.textnumb_dimen = mBundle.getInt("textnumb_dimen");
        MmsWidgetUtils.msgtextl_dimen = mBundle.getInt("msgtextl_dimen");
        MmsWidgetUtils.msgtextt_dimen = mBundle.getInt("msgtextt_dimen");
        MmsWidgetUtils.msgtextr_dimen = mBundle.getInt("msgtextr_dimen");
        MmsWidgetUtils.msgtextb_dimen = mBundle.getInt("msgtextb_dimen");
        MmsWidgetUtils.buttomdividerl_dimen = mBundle.getInt("buttomdividerl_dimen");
        MmsWidgetUtils.buttomdividert_dimen = mBundle.getInt("buttomdividert_dimen");
        MmsWidgetUtils.buttomdividerr_dimen = mBundle.getInt("buttomdividerr_dimen");
        MmsWidgetUtils.buttomdividerb_dimen = mBundle.getInt("buttomdividerb_dimen");
        MmsWidgetUtils.sreplyx_dimen = mBundle.getInt("sreplyx_dimen");
        MmsWidgetUtils.breplyx_dimen = mBundle.getInt("breplyx_dimen");
        MmsWidgetUtils.sreplyy_dimen = mBundle.getInt("sreplyy_dimen");
        MmsWidgetUtils.breplyy_dimen = mBundle.getInt("breplyy_dimen");
        MmsWidgetUtils.sdeletex_dimen = mBundle.getInt("sdeletex_dimen");
        MmsWidgetUtils.bdeletex_dimen = mBundle.getInt("bdeletex_dimen");
        MmsWidgetUtils.sdeletey_dimen = mBundle.getInt("sdeletey_dimen");
        MmsWidgetUtils.bdeletey_dimen = mBundle.getInt("bdeletey_dimen");
        MmsWidgetUtils.sphotox_dimen = mBundle.getInt("sphotox_dimen");
        MmsWidgetUtils.bphotox_dimen = mBundle.getInt("bphotox_dimen");
        MmsWidgetUtils.sphotoy_dimen = mBundle.getInt("sphotoy_dimen");
        MmsWidgetUtils.bphotoy_dimen = mBundle.getInt("bphotoy_dimen");

    }
}
