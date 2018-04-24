package com.lab.dxy.bracelet.core;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/9/22
 */
public interface DeviceToAppOperator {

    //空操作
    byte OP_NULL = 0x00;

    //通用单次按键
    byte OP_ONCE = 0x01;

    //通用多次按键(2-16)
    byte OP_TWICE = 0x02;

    //查找手机
    byte OP_SECLTE_PHONE = 0x10;

    //拍照
    byte OP_SHAKE_PHOTO = 0x11;

    //播放音乐
    byte OP_PLAY_MUSIC = 0x12;

    //暂停播放音乐
    byte OP_STOP_MUSIC = 0x13;

    //上一首音乐
    byte OP_LAST_MUSIC = 0x14;

    //下一首音乐
    byte OP_NEXT_MUSIC = 0x15;

    //拨打亲情号码
    byte OP_FAMILY_PHONE = 0x16;

    //音量加
    byte OP_VOLUME_ADD = 0x17;

    //音量减
    byte OP_VOLUME_DEL = 0x18;

    //从左向右划动
    //从右向左划动
    //从上向下划动
    //从下向上划动
    //久坐提醒
}
