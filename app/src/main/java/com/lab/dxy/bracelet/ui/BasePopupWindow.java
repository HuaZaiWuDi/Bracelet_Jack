package com.lab.dxy.bracelet.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.Utils;

import java.util.Arrays;


/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/12/4
 */

public class BasePopupWindow extends PopupWindow implements View.OnClickListener, View.OnLongClickListener {
    Activity mContext;
    private String[] texts;

    public BasePopupWindow(Activity mContext) {
        this.mContext = mContext;
    }


    public void initPop(@NonNull String title, @Nullable String text, final String[] texts) {
        this.texts = texts;
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_basepop, null, false);
        LinearLayout textLayout = (LinearLayout) view.findViewById(R.id.Linear_text);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);//解决popup被输入法挡住的问题
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setContentView(view);
        this.setAnimationStyle(R.style.popAnim);
        this.setOnDismissListener(() -> setBackgroundAlpha(mContext, 1.0f));


        ((TextView) view.findViewById(R.id.title)).setText(title);
        TextView textV = (TextView) view.findViewById(R.id.text);
        if (!TextUtils.isEmpty(text))
            textV.setText(text);
        else
            textV.setVisibility(View.GONE);

        view.findViewById(R.id.cancel).setOnClickListener(view1 -> dismiss());

        for (int i = 0; i < texts.length; i++) {
            View line = new View(mContext);
            line.setBackgroundColor(Color.parseColor("#D9DEE1"));
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            line.setLayoutParams(lineParams);
            textLayout.addView(line);

            TextView textView = new TextView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.sp2px(mContext, 80));
            textView.setText(texts[i]);
            textView.setTextColor(Color.parseColor("#25b4b4"));
            textView.setTextSize(Utils.sp2px(mContext, 10));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(params);
            textView.setTag(texts[i]);
            textView.setOnClickListener(this);
            textView.setOnLongClickListener(this);
            textLayout.addView(textView);

        }
    }

    @Override
    public void onClick(View view) {
        dismissPop();
        String key = (String) view.getTag();
        int indexOf = Arrays.asList(texts).indexOf(key);
        if (indexOf != -1) {
            if (onItemClickLisetener != null)
                onItemClickLisetener.OnClick(indexOf, key);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        dismissPop();
        String key = (String) view.getTag();
        int indexOf = Arrays.asList(texts).indexOf(key);
        if (indexOf != -1) {
            if (onItemLongClickLisetener != null)
                onItemLongClickLisetener.OnLongClick(indexOf, key);

        }

        return false;
    }


    public interface OnItemClickLisetener {
        void OnClick(int position, String text);
    }

    public interface OnItemLongClickLisetener {
        void OnLongClick(int position, String text);
    }

    private OnItemClickLisetener onItemClickLisetener;
    private OnItemLongClickLisetener onItemLongClickLisetener;

    public void setOnItemLongClickLisetener(OnItemLongClickLisetener onItemLongClickLisetener) {
        this.onItemLongClickLisetener = onItemLongClickLisetener;
    }

    public void setOnItemClickLisetener(OnItemClickLisetener onItemClickLisetener) {
        this.onItemClickLisetener = onItemClickLisetener;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        setBackgroundAlpha(mContext, 0.6f);
    }



    public void dismissPop() {
        dismiss();
        setBackgroundAlpha(mContext, 1.0f);
    }

    //屏幕主题变暗
    private void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        activity.getWindow().setAttributes(lp);
    }


}
