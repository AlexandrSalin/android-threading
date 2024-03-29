package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.request.CachedSpiceRequest;

import java.util.Set;

/**
 * Defines the behavior of a listener that will be notified of request
 * processing by the {@link com.octo.android.robospice.SpiceService}.
 *
 * @author sni
 */
public interface SpiceServiceServiceListener {
  void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, Set<RequestListener<?>> listeners);
}
