package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    @JsonProperty("jsonrpc")
    public String version = "2.0";
    public Integer id;
    public String method;
    public JsonNode params;

    public JsonRpcRequest(){}

    public JsonRpcRequest(Integer id, String method, JsonNode params) {
        this.method = method;
        this.id = id;
        this.params = params;
    }
}
