package com.lab.dxy.bracelet.entity;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/9/12
 */
public class StepsItem {

    String title;
    String text;
    int img;
    boolean isOpen;


    public StepsItem() {
    }

    public StepsItem(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public StepsItem(String title, String text, int img) {
        this.title = title;
        this.text = text;
        this.img = img;
    }

    public StepsItem(String text, int img, boolean isOpen) {
        this.text = text;
        this.img = img;
        this.isOpen = isOpen;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getImg() {
        return img;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public String toString() {
        return "StepsItem{" +
                "title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", img=" + img +
                '}';
    }
}
