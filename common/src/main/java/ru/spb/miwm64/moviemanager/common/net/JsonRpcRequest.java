package ru.spb.miwm64.moviemanager.common.net;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    @JsonProperty("jsonrpc")
    public String version = "2.0";
    public Integer id;
    public String method;
    public JsonNode params;
    public UUID uuid;

    public JsonRpcRequest(){}

    public JsonRpcRequest(Integer id, String method, JsonNode params, UUID uuid) {
        this.method = method;
        this.id = id;
        this.params = params;
        this.uuid = uuid;
    }
}
