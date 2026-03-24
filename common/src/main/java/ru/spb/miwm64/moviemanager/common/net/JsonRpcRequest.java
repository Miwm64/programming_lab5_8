package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    @JsonProperty("jsonrpc")
    public String version = "2.0";
    public Integer id;
    public String method;
    public Object params;

    public JsonRpcRequest(String method, Integer id, Object params) {
        this.method = method;
        this.id = id;
        this.params = params;
    }
}
