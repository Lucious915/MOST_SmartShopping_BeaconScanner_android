package com.example.android_beacon_scanner.btlescan.util;

/**
 * Created by Lambert on 2016/8/12.
 */

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;

public class CountObj {
    ArrayList<Integer> array = new ArrayList<Integer>();
    float pre_avg = 70;

    public void initial() {
        pre_avg = this.getAvg();
        array = new ArrayList<Integer>();
    }


    public int getindex(int index) {
        return array.get(index);
    }

    public void inputArray(int t) {
        if(array.size()<=20){
            array.add(t);
        }
        else{
            array.remove(0);
            array.add(t);
        }
    }

    public void inputArray_10item(int t) {
        if(array.size()<=10){
            array.add(t);
        }
        else{
            array.remove(0);
            array.add(t);
        }
    }

    public int getMost(){
        if(array.size()>0){
            int[] item = new int[this.getMax()+1];
            for(int i = 0; i < item.length ; i++){
                item[i] = 0;
            }
            for (int i = 0; i < array.size(); i++) {
                item[array.get(i)]++;
            }
            int max = 0;
            for(int i = 0; i < item.length ; i++){
                if(item[i]>max){
                    max = i;
                }
            }
            return max;
        }
        return 0;
    }

    public int getSize() {
        return array.size();
    }

    public float getAvg() {
        if (array.size() == 0) return pre_avg;//pre_avg預設為70 , -70dB為極低的訊號值 ,每次initial都會更新
        float sum = 0;
        for (int i = 0; i < array.size(); i++) {
            sum += array.get(i);
        }
        float avg = sum / array.size();

        return avg;
    }

    public float getAvg_Top10() {
        if (array.size() == 0) return pre_avg;
        float Avg = this.getAvg();
        ArrayList<Float> array_subavg = new ArrayList<Float>();
        ArrayList<Float> array_subavg_sort = new ArrayList<Float>();
        float top_range = 0;
        for (int i = 0; i < array.size(); i++) {
            array_subavg.add(abs(array.get(i)-Avg));
            array_subavg_sort.add(abs(array.get(i)-Avg));
        }
        Collections.sort(array_subavg_sort);
        top_range = array_subavg_sort.get(array_subavg_sort.size()/2);
        float sum = 0;
        float avg_size = 0;
        for (int i = 0; i < array.size(); i++){
            if(array_subavg.get(i)<=top_range){
                sum += array.get(i);
                avg_size++;
            }
        }

        float Top10_avg = (avg_size==0) ? 0 : (sum/avg_size);
        return Top10_avg;
    }

    public double getStandardD() {
        double StandardD = 0;
        double avg = getAvg();
        double sum = 0;
        for (int i = 0; i < array.size(); i++) {
            sum += Math.pow(array.get(i) - avg, 2);
        }
        StandardD = Math.sqrt(sum / array.size());

        return StandardD;
    }

    public int getMax() {
        int max = -100;
        for (int i = 0; i < array.size(); i++) {
            if (max < array.get(i)) {
                max = array.get(i);
            }
        }
        return max;
    }

    public int getMin() {
        int min = -1;
        for (int i = 0; i < array.size(); i++) {
            if (min > array.get(i)) {
                min = array.get(i);
            }
        }
        return min;
    }

    public void sortreverse() {
        Collections.sort(array);
        Collections.reverse(array);
    }

    public double average25() {
        double sum = 0;
        int temp = 0;
        for (int i = array.size() * (3 / 4); i < array.size(); i++) {//後面25%總和
            sum = sum + array.get(i);
            temp++;
        }
        return sum / temp;
    }

}