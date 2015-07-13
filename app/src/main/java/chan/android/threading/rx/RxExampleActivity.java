package chan.android.threading.rx;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import chan.android.threading.R;
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class RxExampleActivity extends Activity {

  static final String TAG = RxExampleActivity.class.getSimpleName();

  private RxApi api = new RxApiImpl();

  private TextView text;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.rx);
    text = (TextView) findViewById(R.id.rx_text_view);
    final long begin = System.currentTimeMillis();
    Observable.zip(getInteger(), getString(), new Func2<Integer, String, Pair<Integer, String>>() {
      @Override
      public Pair<Integer, String> call(Integer i, String s) {
        return Pair.of(i, s);
      }
    })
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Subscriber<Pair<Integer, String>>() {
      @Override
      public void onCompleted() {
        Log.e(TAG, "onCompleted");
      }

      @Override
      public void onError(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
      }

      @Override
      public void onNext(Pair<Integer, String> p) {
        Log.e(TAG, "onNext: " + Thread.currentThread().getName());
        Log.e(TAG, "onNext: " + p.toString());
        Log.e(TAG, "onNext: elapsed: " + (System.currentTimeMillis() - begin) + " (ms)");
        text.setText(p.getLeft() + p.getRight());
      }
    });
  }

  Observable<Integer> getInteger() {
    return Observable.create(new Observable.OnSubscribe<Integer>() {
      @Override
      public void call(Subscriber<? super Integer> subscriber) {
        subscriber.onNext(api.getInteger());
        subscriber.onCompleted();
      }
    })
      .onBackpressureBuffer()
      .subscribeOn(Schedulers.io());
  }

  Observable<String> getString() {
    return Observable.create(new Observable.OnSubscribe<String>() {
      @Override
      public void call(Subscriber<? super String> subscriber) {
        subscriber.onNext(api.getString());
        subscriber.onCompleted();
      }
    })
      .onBackpressureBuffer()
      .subscribeOn(Schedulers.io());
  }
}
