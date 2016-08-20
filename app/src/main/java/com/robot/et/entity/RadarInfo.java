package com.robot.et.entity;

/**
 * Created by houdeming on 2016/8/20.
 * 接收激光雷达
 */
public class RadarInfo {
    private String category;
    private int left;
    private int middle;
    private int right;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getMiddle() {
        return middle;
    }

    public void setMiddle(int middle) {
        this.middle = middle;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public RadarInfo() {
        super();
    }
}
