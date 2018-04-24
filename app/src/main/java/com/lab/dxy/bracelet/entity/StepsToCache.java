package com.lab.dxy.bracelet.entity;

import com.lab.dxy.bracelet.entity.spl.DayStepsTab;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/8
 */
public class StepsToCache implements Serializable {


    private List<List<DayStepsTab>> allSteps;
    private List<String[]> label;
    private String[] days;
    private String[] labels;

    public StepsToCache(List<List<DayStepsTab>> allSteps, List<String[]> label, String[] days) {
        this.allSteps = allSteps;
        this.label = label;
        this.days = days;
    }

    public StepsToCache(List<List<DayStepsTab>> allSteps, String[] labels, String[] days) {
        this.allSteps = allSteps;
        this.days = days;
        this.labels = labels;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public List<String[]> getLabel() {
        return label;
    }

    public void setLabel(List<String[]> label) {
        this.label = label;
    }

    public String[] getDays() {
        return days;
    }

    public void setDays(String[] days) {
        this.days = days;
    }

    public List<List<DayStepsTab>> getAllSteps() {
        return allSteps;
    }

    public void setAllSteps(List<List<DayStepsTab>> allSteps) {
        this.allSteps = allSteps;
    }

    @Override
    public String toString() {
        return "StepsToCache{" +
                "allSteps=" + allSteps +
                ", label=" + label +
                ", days=" + Arrays.toString(days) +
                ", labels=" + Arrays.toString(labels) +
                '}';
    }
}
