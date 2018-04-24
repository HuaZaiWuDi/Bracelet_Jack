package com.lab.dxy.bracelet.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;

/**
 * 项目名称：MeshLed
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/3/17 16:16
 */

public class DialogMutiChoiceFragment extends DialogFragment implements View.OnClickListener {

    private RelativeLayout rl_everyday;
    private RelativeLayout rl_never;
    private RelativeLayout rl_mon;
    private RelativeLayout rl_tue;
    private RelativeLayout rl_wed;
    private RelativeLayout rl_thu;
    private RelativeLayout rl_fri;
    private RelativeLayout rl_sat;
    private RelativeLayout rl_sun;

    private CheckBox ck_everyday;
    private CheckBox ck_never;
    private CheckBox ck_mon;
    private CheckBox ck_tue;
    private CheckBox ck_wed;
    private CheckBox ck_thu;
    private CheckBox ck_fri;
    private CheckBox ck_sat;
    private CheckBox ck_sun;

    private Button btn_cancle;
    private Button btn_ok;

    private RadioButton once;
    private RadioButton every_time;

    private byte repeatData;
    private byte type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_dialog_mutichoice, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repeatData = getArguments().getByte("repeatData");
        type = getArguments().getByte("type");
        L.d("getrepeatData:" + repeatData);
    }

    @Override
    public void onStart() {
        super.onStart();
        intiView();
    }

    void intiView() {
        rl_everyday = (RelativeLayout) getView().findViewById(R.id.rl_everyday);
        rl_never = (RelativeLayout) getView().findViewById(R.id.rl_never);
        rl_mon = (RelativeLayout) getView().findViewById(R.id.rl_mon);
        rl_tue = (RelativeLayout) getView().findViewById(R.id.rl_tue);
        rl_wed = (RelativeLayout) getView().findViewById(R.id.rl_wed);
        rl_thu = (RelativeLayout) getView().findViewById(R.id.rl_thu);
        rl_fri = (RelativeLayout) getView().findViewById(R.id.rl_fri);
        rl_sat = (RelativeLayout) getView().findViewById(R.id.rl_sat);
        rl_sun = (RelativeLayout) getView().findViewById(R.id.rl_sun);

        ck_everyday = (CheckBox) getView().findViewById(R.id.ck_everyday);
        ck_never = (CheckBox) getView().findViewById(R.id.ck_never);
        ck_mon = (CheckBox) getView().findViewById(R.id.ck_mon);
        ck_tue = (CheckBox) getView().findViewById(R.id.ck_tue);
        ck_wed = (CheckBox) getView().findViewById(R.id.ck_wed);
        ck_thu = (CheckBox) getView().findViewById(R.id.ck_thu);
        ck_fri = (CheckBox) getView().findViewById(R.id.ck_fri);
        ck_sat = (CheckBox) getView().findViewById(R.id.ck_sat);
        ck_sun = (CheckBox) getView().findViewById(R.id.ck_sun);

        btn_cancle = (Button) getView().findViewById(R.id.btn_cancle);
        btn_ok = (Button) getView().findViewById(R.id.btn_ok);

        once = (RadioButton) getView().findViewById(R.id.once);
        every_time = (RadioButton) getView().findViewById(R.id.every_time);

        rl_everyday.setOnClickListener(this);
        rl_never.setOnClickListener(this);
        rl_mon.setOnClickListener(this);
        rl_tue.setOnClickListener(this);
        rl_wed.setOnClickListener(this);
        rl_thu.setOnClickListener(this);
        rl_fri.setOnClickListener(this);
        rl_sat.setOnClickListener(this);
        rl_sun.setOnClickListener(this);

        btn_cancle.setOnClickListener(this);
        btn_ok.setOnClickListener(this);

        initCheckBoxStatue();

        switch (type) {
            case 0x00:
                break;
            case 0x01:
                once.setChecked(true);
                break;
            case 0x02:
                every_time.setChecked(true);
                break;
        }

        once.setVisibility(View.GONE);
        every_time.setVisibility(View.GONE);

    }


    private void initCheckBoxStatue() {
        if (type == (byte) 0x00) {
            ck_never.setChecked(true);
        } else {
            if ((repeatData & 0x02) == 0x02) {
                ck_mon.setChecked(true);
            }
            if ((repeatData & 0x04) == 0x04) {
                ck_tue.setChecked(true);
            }
            if ((repeatData & 0x08) == 0x08) {
                ck_wed.setChecked(true);
            }
            if ((repeatData & 0x10) == 0x10) {
                ck_thu.setChecked(true);
            }
            if ((repeatData & 0x20) == 0x20) {
                ck_fri.setChecked(true);
            }
            if ((repeatData & 0x40) == 0x40) {
                ck_sat.setChecked(true);
            }
            if ((repeatData & 0x01) == 0x01) {
                ck_sun.setChecked(true);
            }
            setEverydayCheckBox();
        }
    }


    private void getRepeatDataResult() {
        repeatData = 0x00;
        if (ck_never.isChecked()) {
            type = (byte) 0x00;
        } else {
            if (ck_mon.isChecked()) {
                repeatData = (byte) (repeatData | (0x01 << 1));
            }
            if (ck_tue.isChecked()) {
                repeatData = (byte) (repeatData | (0x01 << 2));
            }
            if (ck_wed.isChecked()) {
                repeatData = (byte) (repeatData | (0x01 << 3));
            }
            if (ck_thu.isChecked()) {
                repeatData = (byte) (repeatData | (0x01 << 4));
            }
            if (ck_fri.isChecked()) {
                repeatData = (byte) (repeatData | (0x01 << 5));
            }
            if (ck_sat.isChecked()) {
                repeatData = (byte) (repeatData | (0x01 << 6));
            }
            if (ck_sun.isChecked()) {
                repeatData = (byte) (repeatData | 0x01);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_everyday:
                ck_everyday.setChecked(!ck_everyday.isChecked());
                if (ck_everyday.isChecked()) {
                    ck_never.setChecked(false);
                    setEveryday(true);
                }
                break;
            case R.id.rl_never:
                ck_never.setChecked(!ck_never.isChecked());
                if (ck_never.isChecked()) {
                    ck_everyday.setChecked(false);
                    setEveryday(false);
                }
                break;
            case R.id.rl_mon:
                setCheckBox(ck_mon);
                break;
            case R.id.rl_tue:
                setCheckBox(ck_tue);
                break;
            case R.id.rl_wed:
                setCheckBox(ck_wed);
                break;
            case R.id.rl_thu:
                setCheckBox(ck_thu);
                break;
            case R.id.rl_fri:
                setCheckBox(ck_fri);
                break;
            case R.id.rl_sat:
                setCheckBox(ck_sat);
                break;
            case R.id.rl_sun:
                setCheckBox(ck_sun);
                break;
            case R.id.btn_cancle:
                this.dismiss();
                break;
            case R.id.btn_ok:
                this.dismiss();
                getRepeatDataResult();

                onDialogFragmentListener.sendMes(type, repeatData);
                L.d("type:" + type + "-----repeatData:" + repeatData);
                break;
        }
    }

    public interface OnDialogFragmentListener {
        void sendMes(byte type, byte repeatData);
    }

    private OnDialogFragmentListener onDialogFragmentListener;

    public void setOnDialogFragmentListener(OnDialogFragmentListener onDialogFragmentListener) {
        this.onDialogFragmentListener = onDialogFragmentListener;
    }

    private void setCheckBox(CheckBox ck) {
        ck.setChecked(!ck.isChecked());
        if (ck.isChecked()) {
            ck_never.setChecked(false);
        } else {
            ck_everyday.setChecked(false);
        }
        setEverydayCheckBox();
    }

    private void setEverydayCheckBox() {
        if (ck_mon.isChecked() && ck_tue.isChecked() && ck_wed.isChecked() && ck_thu.isChecked()
                && ck_fri.isChecked() && ck_sat.isChecked() && ck_sun.isChecked()) {
            ck_everyday.setChecked(true);
        }
    }

    private void setEveryday(boolean enable) {
        ck_mon.setChecked(enable);
        ck_tue.setChecked(enable);
        ck_wed.setChecked(enable);
        ck_thu.setChecked(enable);
        ck_fri.setChecked(enable);
        ck_sat.setChecked(enable);
        ck_sun.setChecked(enable);
    }


}
