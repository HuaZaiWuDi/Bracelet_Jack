package com.lab.dxy.bracelet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.RxToast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/9
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmViewHolder> {
    private Context context;
    private List<UserAlarmTab> data;


    public AlarmAdapter(Context context, List<UserAlarmTab> data) {
        this.context = context;
        this.data = data;
    }

    public interface RecyclerOnClickLisener {
        void OnClickitemLisener(int i);

        void OnLongClickItemLisener(int i);
    }

    public RecyclerOnClickLisener recyclerOnClickLisener;

    public void setRecyclerOnClickLisener(RecyclerOnClickLisener recyclerOnClickLisener) {
        this.recyclerOnClickLisener = recyclerOnClickLisener;
    }

    public void removeData(int position) {
        data.remove(position);
//        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void addData(UserAlarmTab UserAlarmTab) {
        data.add(UserAlarmTab);
        notifyDataSetChanged();
    }

    public void clear() {
        if (data != null) {
            data.clear();
            notifyDataSetChanged();
        }
    }


    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlarmViewHolder(LayoutInflater.from(context).inflate(R.layout.addalarm_item, null, false));
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, final int position) {
        final UserAlarmTab userAlarmTab = data.get(position);
        holder.time.setText(intFormat(userAlarmTab.getHour()) + ":" + intFormat(userAlarmTab.getMin()));
//        holder.type.setText(Utils.setTypeString((byte) userAlarmTab.getType()));
        holder.week.setText(Utils.setRepeatStringToDay((byte) userAlarmTab.getWeek()));
//        holder.mode.setText(Utils.setModeString((byte) userAlarmTab.getMode()));

        //判断是否过期
        if (userAlarmTab.getWeek() == 0x00) {
            L.d("判断是否过期：" + Utils.setFormat(userAlarmTab.getAddTime(), "dd", Utils.DATE));
            L.d("判断是否过期：" + Utils.setFormat(new Date().getTime(), "dd", Utils.DATE));
            if (Utils.setFormat(userAlarmTab.getAddTime(), "dd", Utils.DATE).equals(Utils.setFormat(new Date().getTime(), "dd", Utils.DATE))) {
                L.d("添加时间:" + Utils.setFormat(userAlarmTab.getAddTime(), "dd-mm", Utils.DATE));
                String s = Utils.setFormat(new Date().getTime(), "HH-mm", Utils.DATE);
                String[] split = s.split("-");
                int nowHour = Integer.parseInt(split[0]);
                int nowMin = Integer.parseInt(split[1]);
                int hour = userAlarmTab.getHour();
                int min = userAlarmTab.getMin();
                if (hour < nowHour) {
                    userAlarmTab.setEnable(false);
                } else if (hour == nowHour && min <= nowMin) {
                    userAlarmTab.setEnable(false);
                }
            }
        }

        if (userAlarmTab.isEnable()) {
            Glide.with(context)
                    .load(R.mipmap.slip_switch_on)
                    .into(holder.img_enable);

        } else {
            Glide.with(context)
                    .load(R.mipmap.slip_switch_off)
                    .into(holder.img_enable);
        }

        holder.img_enable.setOnClickListener(v -> {
            if (BleService.isConnected) {
                if (data.get(position).isEnable()) {
                    Glide.with(context)
                            .load(R.mipmap.slip_switch_off)
                            .into(holder.img_enable);

                    updateAddTime(false, userAlarmTab);
                    data.get(position).setEnable(false);

                    MyBle.getInstance().sendAlarmPush(false, userAlarmTab, null);

                    L.d("UserAlarmTab1:" + UserAlarmTab.getaddTime(data.get(position).getAddTime()));
                } else {
                    Glide.with(context)
                            .load(R.mipmap.slip_switch_on)
                            .into(holder.img_enable);
                    updateAddTime(true, userAlarmTab);
                    data.get(position).setEnable(true);
                    MyBle.getInstance().sendAlarmPush(true, userAlarmTab, null);

                    L.d("UserAlarmTab2:" + UserAlarmTab.getaddTime(data.get(position).getAddTime()));
                }
            } else {
                RxToast.warning(context.getString(R.string.barDisConnect));
            }
        });


        holder.itemView.setOnClickListener(v -> recyclerOnClickLisener.OnClickitemLisener(position));

        holder.itemView.setOnLongClickListener(v -> {
            recyclerOnClickLisener.OnLongClickItemLisener(position);

            return true;
        });

    }


    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    private String intFormat(int i) {
        return new DecimalFormat("00").format(i);
    }


    private void updateAddTime(boolean enable, UserAlarmTab userAlarmTab) {
        List<UserAlarmTab> all = UserAlarmTab.getAll();

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getAddTime() == userAlarmTab.getAddTime()) {
                UserAlarmTab.deletByAddedTime(all.get(i).getAddTime());

                UserAlarmTab tab = new UserAlarmTab();
                tab.setEnable(enable);
                tab.setAddTime(userAlarmTab.getAddTime());
                tab.setHour(userAlarmTab.getHour());
                tab.setMin(userAlarmTab.getMin());
                tab.setMode(userAlarmTab.getMode());
                tab.setType(userAlarmTab.getType());
                tab.setWeek(userAlarmTab.getWeek());
                tab.save();
            }
        }

    }


    public static long getDateMillis(String dateString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        long millionSeconds = 0;
        try {
            millionSeconds = sdf.parse(dateString).getTime();
        } catch (ParseException e) {
            return millionSeconds;
        }// 毫秒

        return millionSeconds;
    }

    /**
     * 返回一定格式的当前时间
     *
     * @param pattern "yyyy-MM-dd HH:mm:ss E"
     * @return
     */
    public static String getCurrentDate(String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = new Date(System.currentTimeMillis());
        String dateString = simpleDateFormat.format(date);
        return dateString;

    }

    /**
     * 返回一定格式的当前时间
     *
     * @param pattern "yyyy-MM-dd HH:mm:ss E"
     * @return
     */
    public static String getCurrentDate(long value, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = new Date(value);
        String dateString = simpleDateFormat.format(date);
        return dateString;
    }

}
