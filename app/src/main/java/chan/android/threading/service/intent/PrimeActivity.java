package chan.android.threading.service.intent;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import chan.android.threading.R;

public class PrimeActivity extends Activity {

  public static final String TAG = PrimeActivity.class.getSimpleName();

  private static PrimesHandler handler = new PrimesHandler();

  private static Messenger messenger = new Messenger(handler);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.primes);
    final Button go = (Button) findViewById(R.id.button_go);
    go.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        triggerService(1000);
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    LinearLayout ll = (LinearLayout) findViewById(R.id.linearlayout_root);
    handler.attach(ll);
  }

  @Override
  public void onPause() {
    super.onPause();
    handler.detach();
  }

  private void triggerService(int primeToFind) {
    Intent intent = new Intent(this, ComputePrimeIntentService.class);
    intent.putExtra(ComputePrimeIntentService.PARAM, primeToFind);
    intent.putExtra(ComputePrimeIntentService.MESSENGER, messenger);
    startService(intent);
  }

  private static class PrimesHandler extends Handler {

    private LinearLayout view;

    /**
     * This method must run on main thread
     *
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == ComputePrimeIntentService.RESULT) {
        if (view != null) {
          TextView text = new TextView(view.getContext());
          text.setText(msg.arg1 + "th prime: " + msg.obj.toString());
          view.addView(text);
        } else {
          Log.e(PrimeActivity.TAG, "Ignore because view was detached");
        }
      } else if (msg.what == ComputePrimeIntentService.INVALID) {
        if (view != null) {
          TextView text = new TextView(view.getContext());
          text.setText("Invalid request");
          view.addView(text);
        }
      } else {
        super.handleMessage(msg);
      }
    }

    public void attach(LinearLayout view) {
      this.view = view;
    }

    public void detach() {
      view = null;
    }
  }
}
