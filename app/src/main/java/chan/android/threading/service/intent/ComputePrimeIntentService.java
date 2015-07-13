package chan.android.threading.service.intent;


import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ComputePrimeIntentService extends ConcurrentIntentService {

  public static final String PARAM = "prime_to_find";
  public static final String MESSENGER = "messenger";
  public static final int INVALID = "invalid".hashCode();
  public static final int RESULT = "nth_prime".hashCode();
  private static final int N_THREADS = 5;

  public ComputePrimeIntentService() {
    super(Executors.newFixedThreadPool(
      N_THREADS,
      new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          Thread t = new Thread(r);
          t.setPriority(Thread.MIN_PRIORITY);
          t.setName("Primes intent thread");
          return t;
        }
      }));
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    int primeToFind = intent.getIntExtra(PARAM, -1);
    Messenger messenger = intent.getParcelableExtra(MESSENGER);
    try {
      if (primeToFind < 2) {
        messenger.send(Message.obtain(null, INVALID));
      } else {
        messenger.send(Message.obtain(null, RESULT, primeToFind, 0, calculateNthPrime(primeToFind)));
      }
    } catch (RemoteException e) {
      Log.e(PrimeActivity.TAG, "Unable to send message", e);
    }
  }

  private BigInteger calculateNthPrime(int primeToFind) {
    BigInteger prime = new BigInteger("2");
    for (int i = 0; i < primeToFind; i++) {
      prime = prime.nextProbablePrime();
    }
    return prime;
  }
}
