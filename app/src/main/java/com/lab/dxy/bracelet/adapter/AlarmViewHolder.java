package com.lab.dxy.bracelet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/5/9
 */
public class AlarmViewHolder extends RecyclerView.ViewHolder{
    public TextView time;
    public TextView time_am;
    public TextView mode;
    public TextView week;
    public ImageView img_enable;
    public TextView type;


    public AlarmViewHolder(View itemView) {
        super(itemView);

        time= (TextView) itemView.findViewById(R.id.time);
        time_am= (TextView) itemView.findViewById(R.id.time_am);
        mode= (TextView) itemView.findViewById(R.id.mode);
        week= (TextView) itemView.findViewById(R.id.week);
        img_enable= (ImageView) itemView.findViewById(R.id.img_enable);
        type= (TextView) itemView.findViewById(R.id.type);
    }
}
