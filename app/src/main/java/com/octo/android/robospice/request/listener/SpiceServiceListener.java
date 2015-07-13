package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.request.CachedSpiceRequest;

import java.util.Set;

/**
 * Defines the behavior of a listener that will be notified of request
 * processing by the {@link com.octo.android.robospice.SpiceService}.
 *
 * @author sni
 */
public interface SpiceServiceListener {
  void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

  void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext);

  void onServiceStopped();

  // ----------------------------------
  //  INNER CLASS
  // ----------------------------------

  class RequestProcessingContext {
    private Thread executionThread;
    private RequestProgress requestProgress;
    private Set<RequestListener<?>> requestListeners;

    public Thread getExecutionThread() {
      return executionThread;
    }

    public void setExecutionThread(Thread executionThread) {
      this.executionThread = executionThread;
    }

    public RequestProgress getRequestProgress() {
      return requestProgress;
    }

    public void setRequestProgress(RequestProgress requestProgress) {
      this.requestProgress = requestProgress;
    }

    public Set<RequestListener<?>> getRequestListeners() {
      return requestListeners;
    }

    public void setRequestListeners(Set<RequestListener<?>> requestListeners) {
      this.requestListeners = requestListeners;
    }
  }


}
