package ru.spb.miwm64.moviemanager.server;

import ru.spb.miwm64.moviemanager.common.net.JsonRpcRequest;

public class RequestHandler {

    private final RequestRouter router;

    public RequestHandler(RequestRouter router) {
        this.router = router;
    }

    public Object handle(JsonRpcRequest request) throws Exception {
        return router.route(request.method, request.params);
    }
}
