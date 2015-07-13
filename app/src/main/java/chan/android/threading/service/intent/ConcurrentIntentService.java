package chan.android.threading.service.intent;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.Executor;

public abstract class ConcurrentIntentService extends Service {

  private static final String TAG = ConcurrentIntentService.class.getSimpleName();
  /**
   * Executor service thread pool
   */
  private final Executor executor;
  private final CompletionHandler handler = new CompletionHandler();
  /**
   * The total number of task
   */
  private int counter;

  public ConcurrentIntentService(Executor executor) {
    this.executor = executor;
    this.counter = 0;
  }

  /**
   * The onHandleIntent method will be invoked in the background using the Executor
   * and triggered from the onStartCommand method of our Service, which is invoked
   * by the platform when we call startService from our Activity.
   *
   * @param intent
   */
  protected abstract void onHandleIntent(Intent intent);

  @Override
  public int onStartCommand(final Intent intent, int flags, int startId) {
    counter++;
    executor.execute(
      // Execute a task on different thread
      new Runnable() {
        @Override
        public void run() {
          try {
            onHandleIntent(intent);
          } finally {
            handler.sendMessage(Message.obtain(handler));
          }
        }
      });

    /**
     * Note that we're returning START_REDELIVER_INTENT from onStartCommand, which tells
     * the system that if it must kill our Service, for example, to free up memory for a
     * foreground application, it should be scheduled to restart when the system is under
     * less pressure and the last Intent object sent to the Service should be redelivered to it.
     */
    return START_REDELIVER_INTENT;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private class CompletionHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      if (--counter == 0) {
        Log.e(TAG, "No more tasks, stop service!");
        stopSelf();
      } else {
        Log.e(TAG, counter + " active tasks.");
      }
    }
  }
}
