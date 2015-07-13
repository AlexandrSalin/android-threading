package chan.android.threading.async;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import java.util.ArrayDeque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MyAsyncTask<Params, Progress, Result> {

  /**
   * Log tag
   */
  private static final String TAG = MyAsyncTask.class.getSimpleName();

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
  private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
  private static final int KEEP_ALIVE = 1;

  /**
   * Handler messages
   */
  private static final int MESSAGE_POST_RESULT = 0x1;
  private static final int MESSAGE_POST_PROGRESS = 0x2;

  private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "MyAsyncTask #" + counter.getAndIncrement());
    }
  };

  private static final BlockingDeque<Runnable> POOL_WORK_QUEUE = new LinkedBlockingDeque<Runnable>();
  public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, POOL_WORK_QUEUE, THREAD_FACTORY);
  private static final Executor SERIAL_EXECUTOR = new SerialExecutor();

  private static final InternalHandler HANDLER = new InternalHandler();
  private static volatile Executor defaultExecutor = SERIAL_EXECUTOR;
  private final FutureTask<Result> futureTask;
  private final WorkerRunnable<Params, Result> worker;
  private final AtomicBoolean cancelled = new AtomicBoolean();
  private final AtomicBoolean taskInvoked = new AtomicBoolean();
  private volatile Status status = Status.PENDING;

  public MyAsyncTask() {

    worker = new WorkerRunnable<Params, Result>() {
      @Override
      public Result call() throws Exception {
        taskInvoked.set(true);
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        return postResult(doInBackground(params));
      }
    };

    futureTask = new FutureTask<Result>(worker) {
      @Override
      protected void done() {
        try {
          postResultIfNotInvoked(get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
        } catch (CancellationException e) {
          postResultIfNotInvoked(null);
        }
      }
    };
  }

  public static void init() {

  }

  public static void setDefaultExecutor(Executor executor) {

  }

  private void postResultIfNotInvoked(Result result) {
    final boolean wasTaskInvoked = taskInvoked.get();
    if (!wasTaskInvoked) {
      postResult(result);
    }
  }

  private Result postResult(Result result) {
    Message message = HANDLER.obtainMessage(MESSAGE_POST_RESULT, new MyAsyncTaskResult<Result>(this, result));
    message.sendToTarget();
    return result;
  }

  public final MyAsyncTask<Params, Progress, Result> execute(Params... params) {
    return executeOnExecutor(defaultExecutor, params);
  }

  public final MyAsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
    if (status != Status.PENDING) {
      switch (status) {
        case RUNNING:
          throw new IllegalStateException("Cannot execute task: the task is already running.");
        case FINISHED:
          throw new IllegalStateException("Cannot execute task:"
            + " the task has already been executed "
            + "(a task can be executed only once)");
      }
    }
    status = Status.RUNNING;
    onPreExecute();
    worker.params = params;
    exec.execute(futureTask);
    return this;
  }

  public final Result get() throws InterruptedException, ExecutionException {
    return futureTask.get();
  }

  public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return futureTask.get(timeout, unit);
  }

  protected abstract Result doInBackground(Params... params);

  protected void onPreExecute() {
  }

  protected void onPostExecute(Result result) {
  }

  protected void onProgressUpdate(Progress... values) {
  }

  protected final void publishProgress(Progress... values) {
    if (!isCancelled()) {
      HANDLER.obtainMessage(MESSAGE_POST_PROGRESS, new MyAsyncTaskResult<Progress>(this, values)).sendToTarget();
    }
  }

  protected void onCancelled(Result result) {
    onCancelled();
  }

  protected void onCancelled() {
  }

  public final Status getStatus() {
    return status;
  }

  public final boolean isCancelled() {
    return cancelled.get();
  }

  public final boolean cancel(boolean mayInterruptIfRunning) {
    cancelled.set(true);
    return futureTask.cancel(mayInterruptIfRunning);
  }

  private void finish(Result result) {
    if (isCancelled()) {
      onCancelled(result);
    } else {
      onPostExecute(result);
    }
    status = Status.FINISHED;
  }

  /**
   * Indicates the current status of the task. Each status will be set only once
   * during the lifetime of a task.
   */
  public enum Status {
    /**
     * Indicates that the task has not been executed yet.
     */
    PENDING,
    /**
     * Indicates that the task is running.
     */
    RUNNING,
    /**
     * Indicates that task has finished.
     */
    FINISHED,
  }

  private static class SerialExecutor implements Executor {

    final ArrayDeque<Runnable> tasks = new ArrayDeque<Runnable>();
    Runnable activeTask;

    @Override
    public void execute(final Runnable r) {
      tasks.offer(new Runnable() {
        @Override
        public void run() {
          try {
            r.run();
          } finally {
            scheduleNext();
          }
        }
      });
    }

    protected synchronized void scheduleNext() {
      if ((activeTask = tasks.poll()) != null) {
        THREAD_POOL_EXECUTOR.execute(activeTask);
      }
    }
  }

  /**
   * To send result to main thread
   */
  private static class InternalHandler extends Handler {

    @Override
    public void handleMessage(Message message) {
      MyAsyncTaskResult result = (MyAsyncTaskResult) message.obj;
      switch (message.what) {
        case MESSAGE_POST_RESULT:
          result.task.finish(result.data[0]);
          break;
        case MESSAGE_POST_PROGRESS:
          result.task.onProgressUpdate(result.data);
          break;
      }
    }
  }

  private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
    Params[] params;
  }

  private static class MyAsyncTaskResult<T> {
    final MyAsyncTask task;
    final T[] data;

    MyAsyncTaskResult(MyAsyncTask task, T... data) {
      this.task = task;
      this.data = data;
    }
  }

}
