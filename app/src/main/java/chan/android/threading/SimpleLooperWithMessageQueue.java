package chan.android.threading;

import android.app.Activity;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

public class SimpleLooperWithMessageQueue extends Activity {

  private static final String TAG = "HelloThreading";

  private LooperThread lt;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    Button b = (Button) findViewById(R.id.button_click);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (lt.handler != null) {
          // Initialize a Message-object with the what argument arbitrarily set to 0
          Message msg = lt.handler.obtainMessage(0);
          // Insert the message in the queue
          lt.handler.sendMessage(msg);
        }
      }
    });
    lt = new LooperThread();
    lt.start();
  }

  @Override
  protected void onDestroy() {
    lt.handler.getLooper().quit();
    super.onDestroy();
  }

  private static class LooperThread extends Thread {

    private Handler handler;

    public void run() {
      // Associate a Looper—and implicitly a MessageQueue—with the thread.
      Looper.prepare();

      // Set up a Handler to be used by the producer for inserting messages in the queue. Here we use the
      // default constructor so it will bind to the Looper of the current thread. Hence, this Handler
      // can created only after Looper.prepare(), or it will have nothing to bind to.
      handler = new Handler() {

        // Callback that runs when the message has been dispatched to the worker thread.
        // It checks the what parameter and then executes the long operation.
        public void handleMessage(Message msg) {
          if (msg.what == 0) {
            doWork();
          }
        }
      };

      // Start dispatching messages from the message queue to the consumer thread.
      // This is a blocking call, so the worker thread will not finish.
      Looper.loop();
    }

    private void doWork() {
      Log.e(TAG, "start doWork()");
      SystemClock.sleep(TimeUnit.SECONDS.toMillis(5));
      Log.e(TAG, "done doWork()");
    }
  }
}
