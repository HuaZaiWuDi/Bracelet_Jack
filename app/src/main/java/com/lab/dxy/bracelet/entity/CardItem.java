package com.lab.dxy.bracelet.entity;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/6
 */
public class CardItem {

    private int cardColor;
    private String title;
    private String data;

    public CardItem(int cardColor, String title, String data) {
        this.cardColor = cardColor;
        this.title = title;
        this.data = data;
    }

    public int getCardColor() {
        return cardColor;
    }

    public void setCardColor(int cardColor) {
        this.cardColor = cardColor;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "CardItem{" +
                "cardColor=" + cardColor +
                ", title='" + title + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
