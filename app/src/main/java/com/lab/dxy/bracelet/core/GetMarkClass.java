package com.lab.dxy.bracelet.core;

import java.math.BigDecimal;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/9/20
 */
public class GetMarkClass {

    private int steps;
    private int weight;
    private int height;


    public GetMarkClass(int steps, int weight, int height) {
        this.steps = steps;
        this.weight = weight;
        this.height = height;
    }


    public double getMark() {
        double km = getKm();
        km = Double.parseDouble(fromatDouble(km, 3));
        return 0.6 * weight * km;
    }

    public double getKm() {
        int stepLenght = getStepLenght();
        return ((double) (stepLenght * steps)) / 100000.0;
    }


    private int getStepLenght() {
        int tempHei = height;

        if (tempHei < 50) {
            tempHei = 50;
        } else if (tempHei > 190) {
            tempHei = 190;
        } else {
            if (tempHei % 10 != 0) {
                tempHei = (tempHei / 10 + 1) * 10;
            } else {
                tempHei = tempHei / 10 * 10;
            }
        }

        int stepLength = 0;
        switch (tempHei) {
            case 50: {
                stepLength = 20;
            }
            break;
            case 60: {
                stepLength = 22;
            }
            break;
            case 70: {
                stepLength = 25;
            }
            break;
            case 80: {
                stepLength = 29;
            }
            break;
            case 90: {
                stepLength = 33;
            }
            break;
            case 100: {
                stepLength = 37;
            }
            break;
            case 110: {
                stepLength = 40;
            }
            break;
            case 120: {
                stepLength = 44;
            }
            break;
            case 130: {
                stepLength = 48;
            }
            break;
            case 140: {
                stepLength = 51;
            }
            break;
            case 150: {
                stepLength = 55;
            }
            break;
            case 160: {
                stepLength = 59;
            }
            break;
            case 170: {
                stepLength = 62;
            }
            break;
            case 180: {
                stepLength = 66;
            }
            break;
            case 190: {
                stepLength = 70;
            }
            break;
            default:
                break;
        }
        return stepLength;
    }

    private String fromatDouble(double i, int scale) {
        BigDecimal bigDecimal = new BigDecimal(i);
        BigDecimal decimal = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);//保留小数点后2位，直接去掉值。
        return String.valueOf(decimal);
    }

}
