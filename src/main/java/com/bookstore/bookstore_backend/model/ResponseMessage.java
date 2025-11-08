package com.bookstore.bookstore_backend.model;

public class ResponseMessage <T>{
    private int code;
    private String message;
    private T data;
    private long total;
    public long getTotal() {return total;    }

    public ResponseMessage(int i, String error, T da) {
        this.code = i;
        this.message = error;
        this.data = da;
    }

    public ResponseMessage() {
        this.code = 500;
        this.message = "error";
        this.data = null;
    }

    public static <T> ResponseMessage<T> success(T data){
        ResponseMessage message = new ResponseMessage();
        message.setCode(200);
        message.setMessage("success");
        message.setData(data);
        return message;
    }
    public ResponseMessage<T> setTotal(long total) {
        this.total = total;
        return this;
    }


    public static <T> ResponseMessage<T> error(String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setCode(500); // 或其他错误码
        responseMessage.setMessage(message);
        return responseMessage;
    }
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
