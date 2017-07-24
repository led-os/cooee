package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

import java.io.Serializable;
import java.util.List;

public class Weather implements Serializable {

    private String weatherid;
    private String weathercity;
    private String weatherResultCity;
    private String weatherweek;
    private String weatherdate;
    private String weathercondition;
    private String weathercode;
    private String hightmp;
    private String lowtmp;
    private String currtmp;
    private String shidu;

    public String getShidu() {
        return shidu;
    }

    public void setShidu(
        String shidu) {
        this.shidu = shidu;
    }

    public String getCurrtmp() {
        return currtmp;
    }

    public void setCurrtmp(
        String currtmp) {
        this.currtmp = currtmp;
    }

    private List<Weather> list;

    public String getWeatherweek() {
        return weatherweek;
    }

    public void setWeatherweek(
        String weatherweek) {
        this.weatherweek = weatherweek;
    }

    public String getWeatherid() {
        return weatherid;
    }

    public void setWeatherid(
        String weatherid) {
        this.weatherid = weatherid;
    }

    /**
     * 搜索结果list中的city名称
     */
    public void setResultCity(String resultCity) {
        this.weatherResultCity = resultCity;
    }

    /**
     * 搜索结果list中的city名称
     */
    public String getResultCity() {
        return this.weatherResultCity;
    }

    public String getWeathercity() {
        return weathercity;
    }

    public void setWeathercity(
        String weathercity) {
        this.weathercity = weathercity;
    }

    public String getWeatherdate() {
        return weatherdate;
    }

    public void setWeatherdate(
        String weatherdate) {
        this.weatherdate = weatherdate;
    }

    public String getWeathercondition() {
        return weathercondition;
    }

    public void setWeathercondition(
        String weathercondition) {
        this.weathercondition = weathercondition;
    }

    public String getWeathercode() {
        return weathercode;
    }

    public void setWeathercode(
        String weathercode) {
        this.weathercode = weathercode;
    }

    public String getHightmp() {
        return hightmp;
    }

    public void setHightmp(
        String hightmp) {
        this.hightmp = hightmp;
    }

    public String getLowtmp() {
        return lowtmp;
    }

    public void setLowtmp(
        String lowtmp) {
        this.lowtmp = lowtmp;
    }

    public List<Weather> getList() {
        return list;
    }

    public void setList(
        List<Weather> list) {
        this.list = list;
    }

}
