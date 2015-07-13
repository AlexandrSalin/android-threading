package com.path.android.jobqueue;

import android.content.Context;

/**
 * Interface to supply custom {@link com.path.android.jobqueue.JobQueue}s for JobManager
 */
public interface QueueFactory {
  public JobQueue createPersistentQueue(Context context, Long sessionId, String id);

  public JobQueue createNonPersistent(Context context, Long sessionId, String id);
}