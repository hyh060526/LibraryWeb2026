package org.example.util;

/**
 * 统一 JSON 响应结构。
 * code == 0 表示成功，其余表示失败；msg 为提示信息，data 为业务数据。
 */
public class Result {

    private int code;
    private String msg;
    private Object data;

    public Result() {
    }

    public Result(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Result ok() {
        return new Result(0, "success", null);
    }

    public static Result ok(Object data) {
        return new Result(0, "success", data);
    }

    public static Result error(String msg) {
        return new Result(500, msg, null);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
