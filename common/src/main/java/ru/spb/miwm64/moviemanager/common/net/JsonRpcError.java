package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;

public class JsonRpcError {
    public Integer code;
    public String message;
    public Object data;

    public static final int INVALID_VALUE = -32003;
    public static final int NOT_FOUND = -32001;
}