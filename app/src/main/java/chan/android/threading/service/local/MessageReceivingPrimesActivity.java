package chan.android.threading.service.local;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import chan.android.threading.R;

public class MessageReceivingPrimesActivity extends Activity {

  public static final String TAG = MessageReceivingPrimesActivity.class.getSimpleName();

  private static PrimesHandler handler = new PrimesHandler();
  private static Messenger messenger = new Messenger(handler);

  private MessageSendingPrimesService service;
  private ServiceConnection serviceConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.primes);
    final Button go = (Button) findViewById(R.id.button_go);
    go.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (service == null) {
          Log.e(TAG, "Service not bound");
        } else {
          service.calculateNthPrime(10000, messenger);
        }
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    LinearLayout ll = (LinearLayout) findViewById(R.id.linearlayout_root);
    handler.attach(ll);
    serviceConnection = new Connection();
    bindService(new Intent(this, MessageSendingPrimesService.class), serviceConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    service = null;
    unbindService(serviceConnection);
    handler.detach();
  }

  private static class PrimesHandler extends Handler {
    private LinearLayout view;

    @Override
    public void handleMessage(Message message) {
      if (message.what == MessageSendingPrimesService.RESULT) {
        if (view != null) {
          TextView text = new TextView(view.getContext());
          text.setText(message.obj.toString());
          view.addView(text);
        } else {
          Log.e(TAG, "received a result, ignoring because we're detached");
        }
      }
    }

    public void attach(LinearLayout view) {
      this.view = view;
    }

    public void detach() {
      this.view = null;
    }

  }

  private class Connection implements ServiceConnection {

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
      service = ((MessageSendingPrimesService.PrimeBinder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      service = null;
    }
  }
}
