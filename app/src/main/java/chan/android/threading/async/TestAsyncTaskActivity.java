package chan.android.threading.async;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import chan.android.threading.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class TestAsyncTaskActivity extends Activity {

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.async_task);


    final Button start = (Button) findViewById(R.id.async_task_button);
    start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MyAsyncTask async = new Hello(start);
        async.execute(1);
      }
    });
  }

  public static class Hello extends MyAsyncTask<Integer, Integer, Integer> {

    private WeakReference<View> view;

    public Hello(View v) {
      view = new WeakReference<View>(v);
    }

    @Override
    protected void onProgressUpdate(Integer... integer) {
      Log.e("Hello", "progress update...");
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return 5;
    }

    @Override
    protected void onPostExecute(Integer result) {
      Button b = (Button) view.get();
      if (b != null) {
        b.setText("done");
      }
    }
  }
}
