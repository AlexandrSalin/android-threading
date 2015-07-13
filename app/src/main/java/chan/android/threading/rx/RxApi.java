package chan.android.threading.rx;

public interface RxApi {

  Integer getInteger();

  String getString();
}

class RxApiImpl implements RxApi {

  @Override
  public Integer getInteger() {
    sleep(3);
    return 1;
  }

  @Override
  public String getString() {
    sleep(5);
    return "RxAndroid";
  }

  public static void sleep(int sec) {
    try {
      Thread.sleep(1000L * sec);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
