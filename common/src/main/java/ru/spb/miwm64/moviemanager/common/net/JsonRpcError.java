package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;

public class JsonRpcError {
    public Integer code;
    public String message;
    public Object data;

    // Standard JSON-RPC 2.0 error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    // Your custom codes (from -32000 to -32099)
    public static final int MOVIE_NOT_FOUND = -32001;
    public static final int MOVIE_EXISTS = -32002;
    public static final int INVALID_VALUE = -32003;
    public static final int NOT_ENOUGH_PARAMETERS = -32004;
    public static final int NON_EXISTENT_COMMAND = -32005;
}
