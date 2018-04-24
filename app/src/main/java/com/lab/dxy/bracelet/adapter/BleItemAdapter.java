package com.lab.dxy.bracelet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchResult;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.Utils;
import com.syd.oden.odenble.Utils.HexUtil;

import java.util.List;

/**
 * Created by 华 on 2017/5/4.
 */

public class BleItemAdapter extends BaseAdapter {
    private Context context;
    private List<SearchResult> bleScanItemList;

    public BleItemAdapter() {
    }

    public BleItemAdapter(Context context, List<SearchResult> bleScanItemList) {
        this.context = context;
        this.bleScanItemList = bleScanItemList;
    }

    @Override
    public int getCount() {
        return bleScanItemList == null ? 0 : bleScanItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return bleScanItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.bleitem, null, false);
            holder.bleName = (TextView) convertView.findViewById(R.id.bleName);
            holder.bleAddr = (TextView) convertView.findViewById(R.id.bleAddr);
            holder.img = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.bleName.setText(bleScanItemList.get(position).getName().replace(Contents.barFilterName, "").trim());
        holder.bleAddr.setText(bleScanItemList.get(position).getAddress());
        byte[] record = bleScanItemList.get(position).scanRecord;


        String s = HexUtil.encodeHexStr(record);
        String[] split = s.split("ff0201");
        String substring = split[1].substring(12, 14);

        if (substring.equals("01")) {
            holder.img.setImageDrawable(Utils.tintDrawableWithColor(context, R.drawable.bluetooth, R.color.default_color));
        } else {
            holder.img.setImageDrawable(Utils.tintDrawableWithColor(context, R.drawable.bluetooth, R.color.bleGreen));
        }
        return convertView;
    }
}

class ViewHolder {
    TextView bleName;
    TextView bleAddr;
    ImageView img;
}
