package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse<T> {
    @JsonProperty("jsonrpc")
    public String version = "2.0";
    public T result;
    public JsonRpcError error;
    public Integer id;
    public UUID uuid;
}