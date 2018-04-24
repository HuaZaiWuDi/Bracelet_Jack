package com.lab.dxy.bracelet.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.entity.CardItem;
import com.lab.dxy.bracelet.ui.recyclerview.cardstack.RxAdapterStack;
import com.lab.dxy.bracelet.ui.recyclerview.cardstack.RxCardStackView;


/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/12/6
 */
public class HelpCardAdapter extends RxAdapterStack<CardItem> {
    public HelpCardAdapter(Context context) {
        super(context);
    }

    @Override
    public void bindView(CardItem data, int position, RxCardStackView.ViewHolder holder) {
        ColorItemViewHolder h = (ColorItemViewHolder) holder;
        h.onBind(data, position);
    }


    @Override
    protected RxCardStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        return new ColorItemViewHolder(getLayoutInflater().inflate(R.layout.list_card_item, parent, false));
    }


    static class ColorItemViewHolder extends RxCardStackView.ViewHolder {
        View mLayout;
        View mContainerContent;
        TextView mTextTitle;
        TextView text;
        TextView title;

        public ColorItemViewHolder(View view) {
            super(view);
            mLayout = view.findViewById(R.id.frame_list_card_item);
            mContainerContent = view.findViewById(R.id.container_list_content);
            mTextTitle = (TextView) view.findViewById(R.id.text_list_card_title);
            text = (TextView) view.findViewById(R.id.text);
            title = (TextView) view.findViewById(R.id.title);
        }

        @Override
        public void onItemExpand(boolean b) {
            mContainerContent.setVisibility(b ? View.VISIBLE : View.GONE);
        }

        public void onBind(CardItem data, int position) {
            mLayout.getBackground().setColorFilter(data.getCardColor(), PorterDuff.Mode.SRC_IN);
            mTextTitle.setText(String.valueOf(position + 1));
            text.setText(data.getData());
            title.setText(data.getTitle());
        }
    }
}
