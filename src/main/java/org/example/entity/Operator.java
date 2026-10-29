package org.example.entity;

public class Operator extends User {

    /** 负责区域 */
    private String area;

    /** 职务 */
    private String duty;

    @Override
    public String[] hasPermission() {
        return new String[]{
                "addBook", "listBook", "discardBook", "editBook", "editCopy",
                "borrow", "return", "myLoan"
        };
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }
}
