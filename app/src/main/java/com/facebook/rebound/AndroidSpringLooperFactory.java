/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.view.Choreographer;

/**
 * Android version of the spring looper that uses the most appropriate frame callback mechanism
 * available. It uses Android's {@link android.view.Choreographer} when available, otherwise it uses a
 * {@link android.os.Handler}.
 */
public abstract class AndroidSpringLooperFactory {

  /**
   * Create an Android {@link SpringLooper} for the detected Android platform.
   *
   * @return a SpringLooper
   */
  public static SpringLooper createSpringLooper() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      return ChoreographerAndroidSpringLooper.create();
    } else {
      return LegacyAndroidSpringLooper.create();
    }
  }

  /**
   * The base implementation of the Android spring looper, using a {@link android.os.Handler} for the
   * frame callbacks.
   */
  private static class LegacyAndroidSpringLooper extends SpringLooper {

    private final Handler mHandler;
    private final Runnable mLooperRunnable;
    private boolean mStarted;

    public LegacyAndroidSpringLooper(Handler handler) {
      mHandler = handler;
      mLooperRunnable = new Runnable() {
        @Override
        public void run() {
          if (!mStarted || mSpringSystem == null) {
            return;
          }
          mSpringSystem.loop();
          mHandler.post(mLooperRunnable);
        }
      };
    }

    /**
     * @return an Android spring looper using a new {@link android.os.Handler} instance
     */
    public static SpringLooper create() {
      return new LegacyAndroidSpringLooper(new Handler());
    }

    @Override
    public void start() {
      mStarted = true;
      mHandler.removeCallbacks(mLooperRunnable);
      mHandler.post(mLooperRunnable);
    }

    @Override
    public void stop() {
      mStarted = false;
      mHandler.removeCallbacks(mLooperRunnable);
    }
  }

  /**
   * The Jelly Bean and up implementation of the spring looper that uses Android's
   * {@link android.view.Choreographer} instead of a {@link android.os.Handler}
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private static class ChoreographerAndroidSpringLooper extends SpringLooper {

    private final Choreographer mChoreographer;
    private final Choreographer.FrameCallback mFrameCallback;
    private boolean mStarted;

    public ChoreographerAndroidSpringLooper(Choreographer choreographer) {
      mChoreographer = choreographer;
      mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
          if (!mStarted || mSpringSystem == null) {
            return;
          }
          mSpringSystem.loop();
          mChoreographer.postFrameCallback(mFrameCallback);
        }
      };
    }

    /**
     * @return an Android spring choreographer using the system {@link android.view.Choreographer}
     */
    public static ChoreographerAndroidSpringLooper create() {
      return new ChoreographerAndroidSpringLooper(Choreographer.getInstance());
    }

    @Override
    public void start() {
      mStarted = true;
      mChoreographer.removeFrameCallback(mFrameCallback);
      mChoreographer.postFrameCallback(mFrameCallback);
    }

    @Override
    public void stop() {
      mStarted = false;
      mChoreographer.removeFrameCallback(mFrameCallback);
    }
  }
}