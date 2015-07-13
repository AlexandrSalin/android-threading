package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Interface used to deal with request result. Two cases : request failed or
 * succeed. Implement this interface to retrieve request result or to manage
 * error
 *
 * @param <RESULT>
 * @author jva
 */
public interface RequestListener<RESULT> {

  void onRequestFailure(SpiceException spiceException);

  void onRequestSuccess(RESULT result);
}
