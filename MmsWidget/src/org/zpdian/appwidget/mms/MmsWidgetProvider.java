package org.zpdian.appwidget.mms;

import org.zpdian.appwidget.mms.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class MmsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        // TODO Auto-generated method stub
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        // TODO Auto-generated method stub
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews views = new RemoteViews (context.getPackageName(), R.layout.main);
        Bundle bundle = new Bundle();
        bundle.putInt("popup_icon_id", R.id.avatar);
        bundle.putInt("card_background_drawable", R.drawable.message_card);
        bundle.putInt("date_format_string", R.string.date_format);
        bundle.putInt("icon_sms_drawable", R.drawable.icon_icon_sms);
        bundle.putInt("unread_string", R.string.unread);
        bundle.putInt("head_background_drawable", R.drawable.message_photo_base);
        bundle.putInt("head_default_drawable", R.drawable.msg_default_photo);
        bundle.putInt("msg_uread_drawable", R.drawable.message_unread);
        bundle.putInt("msg_divider_drawable", R.drawable.msg_divider);
        bundle.putInt("replay_rest_drawable", R.drawable.replay_icon_rest);
        bundle.putInt("replay_press_drawable", R.drawable.replay_icon_press);
        bundle.putInt("common_delete_drawable", R.drawable.common_icon_delete);
        bundle.putInt("msg_seprators_drawable", R.drawable.message_separators);
        bundle.putInt("common_delete_press_drawable", R.drawable.common_icon_delete_press);
        bundle.putInt("title_string", R.string.title);
        bundle.putInt("delete_string", R.string.delete);
        bundle.putInt("yes_string", R.string.yes);
        bundle.putInt("no_string", R.string.no);
        bundle.putInt("btn_display_id", R.id.display);
        bundle.putInt("btn_write_id", R.id.write);
        bundle.putInt("relatl_dimen",R.dimen.relat_l);
        bundle.putInt("relatt_dimen",R.dimen.relat_t);
        bundle.putInt("relatr_dimen",R.dimen.relat_r);
        bundle.putInt("relatb_dimen",R.dimen.relat_b);
        bundle.putInt("numberl_dimen",R.dimen.number_l);
        bundle.putInt("numbert_dimen",R.dimen.number_t);
        bundle.putInt("numberr_dimen",R.dimen.number_r);
        bundle.putInt("numberb_dimen",R.dimen.number_b);
        bundle.putInt("datel_dimen",R.dimen.date_l);
        bundle.putInt("datet_dimen",R.dimen.date_t);
        bundle.putInt("dater_dimen",R.dimen.date_r);
        bundle.putInt("dateb_dimen",R.dimen.date_b);
        bundle.putInt("mmsiconl_dimen", R.dimen.mmsicon_l);
        bundle.putInt("mmsicont_dimen", R.dimen.mmsicon_t);
        bundle.putInt("mmsiconr_dimen", R.dimen.mmsicon_r);
        bundle.putInt("mmsiconb_dimen", R.dimen.mmsicon_b);
        bundle.putInt("replayl_dimen",R.dimen.replay_l);
        bundle.putInt("replayt_dimen",R.dimen.replay_t);
        bundle.putInt("replayr_dimen",R.dimen.replay_r);
        bundle.putInt("replayb_dimen",R.dimen.replay_b);
        bundle.putInt("photol_dimen", R.dimen.photo_l);
        bundle.putInt("photot_dimen", R.dimen.photo_t);
        bundle.putInt("photor_dimen", R.dimen.photo_r);
        bundle.putInt("photob_dimen", R.dimen.photo_b);
        bundle.putInt("unreadl_dimen", R.dimen.unread_l);
        bundle.putInt("unreadt_dimen", R.dimen.unread_t);
        bundle.putInt("unreadr_dimen", R.dimen.unread_r);
        bundle.putInt("unreadb_dimen", R.dimen.unread_b);
        bundle.putInt("topdividerl_dimen", R.dimen.topdivider_l);
        bundle.putInt("topdividert_dimen", R.dimen.topdivider_t);
        bundle.putInt("topdividerr_dimen", R.dimen.topdivider_r);
        bundle.putInt("topdividerb_dimen", R.dimen.topdivider_b);
        bundle.putInt("readmmsl_dimen", R.dimen.readmms_l);
        bundle.putInt("readmmst_dimen", R.dimen.readmms_t);
        bundle.putInt("readmmsr_dimen", R.dimen.readmms_r);
        bundle.putInt("readmmsb_dimen", R.dimen.readmms_b);
        bundle.putInt("deletel_dimen", R.dimen.delete_l);
        bundle.putInt("deletet_dimen", R.dimen.delete_t);
        bundle.putInt("deleter_dimen", R.dimen.delete_r);
        bundle.putInt("deleteb_dimen", R.dimen.delete_b);
        bundle.putInt("tiviewl_dimen", R.dimen.tiview_l);
        bundle.putInt("tiviewt_dimen", R.dimen.tiview_t);
        bundle.putInt("tiviewr_dimen", R.dimen.tiview_r);
        bundle.putInt("tiviewb_dimen", R.dimen.tiview_b);
        bundle.putInt("biviewl_dimen", R.dimen.biview_l);
        bundle.putInt("biviewt_dimen", R.dimen.biview_t);
        bundle.putInt("biviewr_dimen", R.dimen.biview_r);
        bundle.putInt("biviewb_dimen", R.dimen.biview_b);
        bundle.putInt("textnuml_dimen", R.dimen.textnum_l);
        bundle.putInt("textnumt_dimen", R.dimen.textnum_t);
        bundle.putInt("textnumr_dimen", R.dimen.textnum_r);
        bundle.putInt("textnumb_dimen", R.dimen.textnum_b);
        bundle.putInt("msgtextl_dimen", R.dimen.msgtext_l);
        bundle.putInt("msgtextt_dimen", R.dimen.msgtext_t);
        bundle.putInt("msgtextr_dimen", R.dimen.msgtext_r);
        bundle.putInt("msgtextb_dimen", R.dimen.msgtext_b);
        bundle.putInt("buttomdividerl_dimen", R.dimen.buttomdivider_l);
        bundle.putInt("buttomdividert_dimen", R.dimen.buttomdivider_t);
        bundle.putInt("buttomdividerr_dimen", R.dimen.buttomdivider_r);
        bundle.putInt("buttomdividerb_dimen", R.dimen.buttomdivider_b);
        bundle.putInt("sreplyx_dimen", R.dimen.sreplyx);
        bundle.putInt("breplyx_dimen", R.dimen.breplyx);
        bundle.putInt("sreplyy_dimen", R.dimen.sreplyy);
        bundle.putInt("breplyy_dimen", R.dimen.breplyy);
        bundle.putInt("sdeletex_dimen", R.dimen.sdeletex);
        bundle.putInt("bdeletex_dimen", R.dimen.bdeletex);
        bundle.putInt("sdeletey_dimen", R.dimen.sdeletey);
        bundle.putInt("bdeletey_dimen", R.dimen.bdeletey);
        bundle.putInt("sphotox_dimen", R.dimen.sphotox);
        bundle.putInt("bphotox_dimen", R.dimen.bphotox);
        bundle.putInt("sphotoy_dimen", R.dimen.sphotoy);
        bundle.putInt("bphotoy_dimen", R.dimen.bphotoy);
        views.setBundle(R.id.cover_flow, "setResBundle", bundle);
        views.setInt(R.id.cover_flow, "init", 0);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

}
