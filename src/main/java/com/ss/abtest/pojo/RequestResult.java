package com.ss.abtest.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author senn
 * @since 2023/4/2 19:50
 **/
@Data
@NoArgsConstructor
public class RequestResult {
    private int code;
    private String message;
    private Object obj;

    public RequestResult(HttpStatus status, Object obj) {
        this.code = status.value();
        this.message = status.getReasonPhrase();
        this.obj = obj;
    }

    public RequestResult(HttpStatus status, String message, Object obj) {
        this.code = status.value();
        this.message = message;
        this.obj = obj;
    }

    /**
     * 请求成功
     *
     * @return 200 OK
     */
    public static RequestResult successResult() {
        return new RequestResult(HttpStatus.OK, null);
    }

    /**
     * 请求成功
     *
     * @return 200 OK
     */
    public static RequestResult successResult(Object obj) {
        return new RequestResult(HttpStatus.OK, obj);
    }

    /**
     * 请求错误结果
     *
     * @return 400
     */
    public static RequestResult requestErrorResult(Object obj) {
        return new RequestResult(HttpStatus.INTERNAL_SERVER_ERROR, obj);
    }

    /**
     * 请求错误结果
     *
     * @return 400
     */
    public static RequestResult requestErrorResult() {
        return new RequestResult(HttpStatus.BAD_REQUEST, null);
    }

    /**
     * 服务器错误结果
     *
     * @return 500
     */
    public static RequestResult errorResult() {
        return new RequestResult(HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * 服务器错误结果
     *
     * @return 500
     */
    public static RequestResult errorResult(Object obj) {
        return new RequestResult(HttpStatus.INTERNAL_SERVER_ERROR, obj);
    }
}
