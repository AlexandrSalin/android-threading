package chan.android.threading.service.local;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.math.BigInteger;

public class MessageSendingPrimesService extends Service {

  public static final String TAG = MessageSendingPrimesService.class.getSimpleName();

  public static final int RESULT = "nth_prime".hashCode();
  private final PrimeBinder binder = new PrimeBinder();

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  public void calculateNthPrime(final int n, final Messenger messenger) {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        BigInteger prime = new BigInteger("2");
        for (int i = 0; i < n; i++) {
          prime = prime.nextProbablePrime();
        }
        try {
          messenger.send(Message.obtain(null, RESULT, prime.toString()));
        } catch (RemoteException e) {
          Log.e(MessageSendingPrimesService.TAG, "unable to send msg", e);
        }
        return null;
      }
    }.execute(); // remember, execute() operates off a single-threaded executor on API levels >= 11
  }

  private void notifyUser(int primeToFind, String result) {
    String msg = String.format("The %sth prime is %s", primeToFind, result);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
      .setSmallIcon(android.R.drawable.stat_notify_chat)
      .setContentTitle("Prime service")
      .setContentText(msg);
    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(primeToFind, builder.build());
  }

  public class PrimeBinder extends Binder {
    public MessageSendingPrimesService getService() {
      return MessageSendingPrimesService.this;
    }
  }
}
