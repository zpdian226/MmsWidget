
package android.widget;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    TextView mNumber;

    TextView mDate;

    TextView mReadmms;

    TextView mTextnum;

    TextView mMsgtext;

    ImageView mMmsicon;

    ImageView mTopdivider;

    ImageView mButtomdivider;

    ImageView mTiview;

    ImageView mBiview;

    ImageView mUnread;

    ImageButton mPhoto;

    ImageButton mReplay;

    ImageButton mDelete;

    int mThread;

    int readColumn;

    int dateColumn;

    int smsbodyColumn;

    int phoneNumberColumn;

    int nameColumn;

    public static final int DEFALT = -1;

    public static final int REPLY = 0;

    public static final int DELETE = 1;

    public static final int VIEW_MESSAGE = 2;

    public String number;

    RelativeLayout.LayoutParams mLayoutWrap_Wrap = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    RelativeLayout.LayoutParams mLayoutFill_Wrap = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    public LayoutParams mLayoutGallery = new Gallery.LayoutParams(60, 50);

    private Context mContext;

    private Integer[] mImages;

    Intent mIntent = new Intent();

    public static final Uri CONTENT_URI = Uri.parse("content://sms/inbox");

    private Cursor mCursor;

    public Cursor mContactCursor;

    private Cursor mPhotoCursor;

    public ImageAdapter(Context context, Integer[] imageIds) {
        this.mContext = context;
        this.mImages = imageIds;
        getSmsInPhone();
    }

    public int getCount() {
        // TODO Auto-generated method stub
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public void getSmsInPhone() {
        ContentResolver cr = this.mContext.getContentResolver();
        String[] projection = new String[] {
                "_id", "thread_id", "address", "person", "body", "date", "read"
        };

        mCursor = cr.query(CONTENT_URI, projection, "1=1) group by " + "thread_id" + " -- (", null,
                "date desc");
    }

    public Cursor getMmsCursor() {
        return mCursor;
    }

    public int parameter(int id) {
        return mContext.getResources().getDimensionPixelSize(id);
    }

    public String parameters(int id) {
        return mContext.getResources().getString(id);

    }

    public View messagelayout(int position, int rect) {
        mCursor.moveToPosition(position);
        RelativeLayout relat = new RelativeLayout(mContext);
        relat.setBackgroundResource(MmsWidgetUtils.card_background_drawable);
        relat.layout(parameter(MmsWidgetUtils.relatl_dimen),
                parameter(MmsWidgetUtils.relatt_dimen), parameter(MmsWidgetUtils.relatr_dimen),
                parameter(MmsWidgetUtils.relatb_dimen));
        relat.setPadding(25, 23, 25, 38);

        mNumber = new TextView(mContext);
        number = mCursor.getString(mCursor.getColumnIndex("address"));
        ContentResolver cr = this.mContext.getContentResolver();
        String[] projectionContact = new String[] {
                "_id", "contact_id", "display_name", "data1"
        };
        mContactCursor = cr.query(ContactsContract.Data.CONTENT_URI, projectionContact, "data1 = "
                + number, null, null);
        Bitmap bp = null;
        if (mContactCursor.getCount() > 0) {
            mContactCursor.moveToFirst();
            String name = mContactCursor.getString(mContactCursor
                    .getColumnIndex(PhoneLookup.DISPLAY_NAME));
            mNumber.setText(name);
            String id = mContactCursor.getString(mContactCursor.getColumnIndex("contact_id"));
            Uri photoUri = Uri.withAppendedPath(
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                            Long.parseLong(id)), ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            mPhotoCursor = cr.query(photoUri, new String[] {
                ContactsContract.CommonDataKinds.Photo.PHOTO
            }, null, null, null);
            mPhotoCursor.moveToFirst();
            if (mPhotoCursor.getCount() > 0) {
                byte[] bt = mPhotoCursor.getBlob(mPhotoCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
                if (bt != null) {
                    bp = BitmapFactory.decodeByteArray(bt, 0, bt.length);
                }
            }
        } else {
            mNumber.setText(number);
        }
        mNumber.setTextColor(0xFF000000);
        mNumber.setTypeface(null, Typeface.BOLD);
        mNumber.setTextSize(20);
        mNumber.setBackgroundColor(0xffff000);
        mNumber.layout(parameter(MmsWidgetUtils.numberl_dimen),
                parameter(MmsWidgetUtils.numbert_dimen), parameter(MmsWidgetUtils.numberr_dimen),
                parameter(MmsWidgetUtils.numberb_dimen));
        relat.addView(mNumber, mLayoutWrap_Wrap);

        mDate = new TextView(mContext);
        int dateColumn = mCursor.getColumnIndex("date");
        String date;
        SimpleDateFormat dateFormat = new SimpleDateFormat(mContext.getResources().getString(
                MmsWidgetUtils.date_format_string));
        Date d = new Date(Long.parseLong(mCursor.getString(dateColumn)));
        date = dateFormat.format(d);
        mDate.setText(date);
        mDate.setTextSize(12);
        mDate.setTextColor(0xFF000000);
        mDate.layout(parameter(MmsWidgetUtils.datel_dimen), parameter(MmsWidgetUtils.datet_dimen),
                parameter(MmsWidgetUtils.dater_dimen), parameter(MmsWidgetUtils.dateb_dimen));
        relat.addView(mDate, mLayoutWrap_Wrap);

        mMmsicon = new ImageView(mContext);
        mMmsicon.setBackgroundResource(MmsWidgetUtils.icon_sms_drawable);
        mMmsicon.layout(parameter(MmsWidgetUtils.mmsiconl_dime),
                parameter(MmsWidgetUtils.mmsicont_dime), parameter(MmsWidgetUtils.mmsiconr_dime),
                parameter(MmsWidgetUtils.mmsiconb_dime));
        relat.addView(mMmsicon, mLayoutWrap_Wrap);

        Cursor cursor = cr.query(CONTENT_URI, new String[] {
            "read"
        }, "read=0", null, null);
        cursor.moveToFirst();
        int readstate = mCursor.getInt(mCursor.getColumnIndex("read"));
        int readsms = cursor.getCount();
        String unRead = readsms + " "
                + parameters(MmsWidgetUtils.unread_string);
        mReadmms = new TextView(mContext);
        if (readsms > 0) {
            mReadmms.setText(unRead);
        } else {
            mReadmms.setText(null);
        }
        mReadmms.setTextSize(12);
        mReadmms.setTextColor(0xFF555555);
        mReadmms.layout(parameter(MmsWidgetUtils.readmmsl_dimen),
                parameter(MmsWidgetUtils.readmmst_dimen), parameter(MmsWidgetUtils.readmmsr_dimen),
                parameter(MmsWidgetUtils.readmmsb_dimen));
        relat.addView(mReadmms, mLayoutWrap_Wrap);

        mPhoto = new ImageButton(mContext);
        mPhoto.setBackgroundResource(MmsWidgetUtils.head_background_drawable);
        mPhoto.setPadding(5, 5, 5, 5);
        mPhoto.setScaleType(ScaleType.CENTER_INSIDE);
        if (bp != null) {
            mPhoto.setImageBitmap(bp);
        } else {
            mPhoto.setImageResource(MmsWidgetUtils.head_default_drawable);
        }
        mPhoto.layout(parameter(MmsWidgetUtils.photol_dimen),
                parameter(MmsWidgetUtils.photot_dimen), parameter(MmsWidgetUtils.photor_dimen),
                parameter(MmsWidgetUtils.photob_dimen));
        relat.addView(mPhoto, mLayoutWrap_Wrap);

        mUnread = new ImageView(mContext);
        if (readsms > 0 && readstate == 0) {
            mUnread.setBackgroundResource(MmsWidgetUtils.msg_uread_drawable);
        }
        mUnread.layout(parameter(MmsWidgetUtils.unreadl_dimen),
                parameter(MmsWidgetUtils.unreadt_dimen), parameter(MmsWidgetUtils.unreadr_dimen),
                parameter(MmsWidgetUtils.unreadb_dimen));
        relat.addView(mUnread, mLayoutFill_Wrap);

        mTopdivider = new ImageView(mContext);
        mTopdivider.setBackgroundResource(MmsWidgetUtils.msg_divider_drawable);
        mTopdivider.layout(parameter(MmsWidgetUtils.topdividerl_dimen),
                parameter(MmsWidgetUtils.topdividert_dimen),
                parameter(MmsWidgetUtils.topdividerr_dimen),
                parameter(MmsWidgetUtils.topdividerb_dimen));
        relat.addView(mTopdivider, mLayoutFill_Wrap);

        mDelete = new ImageButton(mContext);
        mReplay = new ImageButton(mContext);
        if (rect == DEFALT) {
            mReplay.setBackgroundResource(MmsWidgetUtils.replay_rest_drawable);
        } else if (rect == REPLY) {
            mReplay.setBackgroundResource(MmsWidgetUtils.replay_press_drawable);
            mDelete.setBackgroundResource(MmsWidgetUtils.common_delete_drawable);
            String threadId = mCursor.getString(mCursor.getColumnIndex("thread_id"));
            Uri uri = Uri.parse("content://mms-sms/conversationsta/" + threadId);
            mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.setData(uri);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(mIntent);
        }
        mReplay.layout(parameter(MmsWidgetUtils.replayl_dimen),
                parameter(MmsWidgetUtils.replayt_dimen), parameter(MmsWidgetUtils.replayr_dimen),
                parameter(MmsWidgetUtils.replayb_dimen));
        relat.addView(mReplay, mLayoutWrap_Wrap);

        mTiview = new ImageView(mContext);
        mTiview.setBackgroundResource(MmsWidgetUtils.msg_seprators_drawable);
        mTiview.layout(parameter(MmsWidgetUtils.tiviewl_dimen),
                parameter(MmsWidgetUtils.tiviewt_dimen), parameter(MmsWidgetUtils.tiviewr_dimen),
                parameter(MmsWidgetUtils.tiviewb_dimen));
        relat.addView(mTiview, mLayoutWrap_Wrap);
        if (rect == DEFALT) {
            mDelete.setBackgroundResource(MmsWidgetUtils.common_delete_drawable);
        } else if (rect == DELETE) {
            mDelete.setBackgroundResource(MmsWidgetUtils.common_delete_press_drawable);
            mReplay.setBackgroundResource(MmsWidgetUtils.replay_rest_drawable);
//            Dialog dialog = new AlertDialog.Builder(mContext)
//                    .setTitle(MmsWidgetUtils.title_string)
//                    .setMessage(MmsWidgetUtils.delete_string)
//                    .setPositiveButton(MmsWidgetUtils.yes_string,
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // TODO Auto-generated method stub
//                                    Log.d("tag", "yes");
//                                }
//                            })
//                    .setNegativeButton(MmsWidgetUtils.no_string,
//                            new DialogInterface.OnClickListener() {
//
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // TODO Auto-generated method stub
//
//                                    Log.d("tag", "no");
//                                }
//                            }).create();
//            dialog.show();

        }
        mDelete.layout(parameter(MmsWidgetUtils.deletel_dimen),
                parameter(MmsWidgetUtils.deletet_dimen), parameter(MmsWidgetUtils.deleter_dimen),
                parameter(MmsWidgetUtils.deleteb_dimen));
        relat.addView(mDelete, mLayoutWrap_Wrap);

        mBiview = new ImageView(mContext);
        mBiview.setBackgroundResource(MmsWidgetUtils.msg_seprators_drawable);
        mBiview.layout(parameter(MmsWidgetUtils.biviewl_dimen),
                parameter(MmsWidgetUtils.biviewt_dimen), parameter(MmsWidgetUtils.biviewr_dimen),
                parameter(MmsWidgetUtils.biviewb_dimen));
        relat.addView(mBiview, mLayoutWrap_Wrap);

        mTextnum = new TextView(mContext);
        mTextnum.setText(position + 1 + " / " + getCount());
        mTextnum.setTextColor(0xFF000000);
        mTextnum.setGravity(Gravity.CENTER_HORIZONTAL);
        mTextnum.setTextSize(13);
        mLayoutWrap_Wrap.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mTextnum.layout(parameter(MmsWidgetUtils.textnuml_dimen),
                parameter(MmsWidgetUtils.textnumt_dimen), parameter(MmsWidgetUtils.textnumr_dimen),
                parameter(MmsWidgetUtils.textnumb_dimen));
        relat.addView(mTextnum, mLayoutWrap_Wrap);

        mButtomdivider = new ImageView(mContext);
        mButtomdivider.setBackgroundResource(MmsWidgetUtils.msg_divider_drawable);
        mButtomdivider.layout(parameter(MmsWidgetUtils.buttomdividerl_dimen),
                parameter(MmsWidgetUtils.buttomdividert_dimen),
                parameter(MmsWidgetUtils.buttomdividerr_dimen),
                parameter(MmsWidgetUtils.buttomdividerb_dimen));
        relat.addView(mButtomdivider, mLayoutFill_Wrap);

        String smsbody = mCursor.getString(mCursor.getColumnIndex("body"));
        mMsgtext = new TextView(mContext);
        mMsgtext.setText(smsbody);
        mMsgtext.setTextColor(0xFF000000);
        mMsgtext.layout(parameter(MmsWidgetUtils.msgtextl_dimen),
                parameter(MmsWidgetUtils.msgtextt_dimen), parameter(MmsWidgetUtils.msgtextr_dimen),
                parameter(MmsWidgetUtils.msgtextb_dimen));
        relat.addView(mMsgtext, mLayoutFill_Wrap);

        if (rect == VIEW_MESSAGE) {
            String threadId = mCursor.getString(mCursor.getColumnIndex("thread_id"));
            Uri uri = Uri.parse("content://mms-sms/conversationsta/" + threadId);
            mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.setData(uri);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(mIntent);
        }
        relat.buildDrawingCache();
        return relat;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ImageView imageView = new ImageView(mContext);
            Bitmap newBmp = null;
            View v = messagelayout(position, DEFALT);
            newBmp = getViewBitmap((View) v);
            imageView.setImageBitmap(newBmp);
            imageView.setLayoutParams(mLayoutGallery);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        } else {
            return convertView;
        }
    }

    public Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e("myTag", "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

}
