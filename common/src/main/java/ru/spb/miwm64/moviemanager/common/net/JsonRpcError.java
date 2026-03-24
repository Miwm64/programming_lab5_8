package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;

public class JsonRpcError {
    public Integer code;
    public String message;
    public Object data;

    public JsonRpcError(Integer code, String message, Object data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // JSON-RPC 2.0 Standard Errors
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    public static final int INVALID_VALUE = -32003;
    public static final int NOT_FOUND = -32001;
}