package com.samplus.smartrecrutare.analytics.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Port asincron pentru procesarea unei executii persistente. */
public interface ProcesatorAnaliticeFundal {
    CompletableFuture<Void> proceseaza(UUID executieId);
}
