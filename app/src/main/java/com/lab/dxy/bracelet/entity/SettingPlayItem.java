package com.lab.dxy.bracelet.entity;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/9/1
 */
public class SettingPlayItem {

    boolean isTitle;
    boolean isFunction;
    String title;
    String text;
    int icon;
    boolean isOpen;
    byte[] img_icon;

    public SettingPlayItem(String title, boolean isTitle) {
        this.title = title;
        this.isTitle = isTitle;
    }

    public SettingPlayItem(boolean isTitle, boolean isFunction, String title, String text, int icon, boolean isOpen) {
        this.isTitle = isTitle;
        this.isFunction = isFunction;
        this.title = title;
        this.text = text;
        this.icon = icon;
        this.isOpen = isOpen;
    }

    public SettingPlayItem(String title, boolean isOpen, byte[] img_icon) {
        this.title = title;
        this.isOpen = isOpen;
        this.img_icon = img_icon;
    }

    public SettingPlayItem(String title, boolean isFunction, boolean isOpen, int icon) {
        this.title = title;
        this.isFunction = isFunction;
        this.isOpen = isOpen;
        this.icon = icon;
    }

    public SettingPlayItem(String title, String text, int icon, boolean isOpen) {
        this.title = title;
        this.text = text;
        this.icon = icon;
        this.isOpen = isOpen;
    }

    public SettingPlayItem(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public SettingPlayItem(String title, boolean isOpen, int icon) {
        this.title = title;
        this.icon = icon;
        this.isOpen = isOpen;
    }

    public byte[] getImg_icon() {
        return img_icon;
    }

    public void setImg_icon(byte[] img_icon) {
        this.img_icon = img_icon;
    }

    public boolean isTitle() {
        return isTitle;
    }

    public void setTitle(boolean title) {
        isTitle = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public void setFunction(boolean function) {
        isFunction = function;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "SettingPlayItem{" +
                "isTitle=" + isTitle +
                ", isFunction=" + isFunction +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", icon=" + icon +
                ", isOpen=" + isOpen +
                '}';
    }
}
