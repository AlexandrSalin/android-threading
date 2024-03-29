package com.octo.android.robospice.persistence;

import android.app.Application;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

import java.util.List;

/**
 * Super class of all factories of {@link com.octo.android.robospice.persistence.ObjectPersisterFactory} classes. They
 * are responsible for creating {@link com.octo.android.robospice.persistence.ObjectPersister} classes that will
 * save/load data for a given class. Such factories are useful because we
 * sometimes need to create {@link com.octo.android.robospice.persistence.ObjectPersister} dynamically. For instance
 * when we serialize items as JSON, we need to create a new
 * {@link com.octo.android.robospice.persistence.ObjectPersister} class for every class of items saved/loaded into
 * cache. A unique {@link com.octo.android.robospice.persistence.ObjectPersister} would not be able to strongly type
 * the load/save method and we would have to cast the result of both operations,
 * leading to less robust and less convenient code.
 *
 * @author sni
 */
public abstract class ObjectPersisterFactory implements Persister {

  private Application mApplication;
  private boolean isAsyncSaveEnabled;
  private List<Class<?>> listHandledClasses;

  /**
   * Creates an {@link com.octo.android.robospice.persistence.ObjectPersisterFactory} given an Android application.
   * As no list of handled classes is specified, it will act has if it can
   * handle all classes.
   *
   * @param application the android context needed to access android file system or
   *                    databases to store.
   */
  public ObjectPersisterFactory(Application application) {
    this(application, null);
  }

  /**
   * Creates an {@link com.octo.android.robospice.persistence.ObjectPersisterFactory} given an Android application
   * and a list of handled classes.
   *
   * @param application        the android context needed to access android file system or
   *                           databases to store.
   * @param listHandledClasses the list of classes that is handled by the factory. The
   *                           factory will try to produce an {@link com.octo.android.robospice.persistence.ObjectPersister} for
   *                           each element of the list.
   */
  public ObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses) {
    this.mApplication = application;
    this.listHandledClasses = listHandledClasses;
  }

  protected final Application getApplication() {
    return mApplication;
  }

  /**
   * Wether or not this bus element can persist/unpersist objects of the given
   * class clazz.
   *
   * @param clazz the class of objets we are looking forward to persist.
   * @return true if this bus element can persist/unpersist objects of the
   * given class clazz. False otherwise.
   */
  @Override
  public boolean canHandleClass(Class<?> clazz) {
    if (listHandledClasses == null) {
      return true;
    } else {
      return listHandledClasses.contains(clazz);
    }
  }

  /**
   * Creates a {@link com.octo.android.robospice.persistence.ObjectPersister} for a given class.
   *
   * @param clazz the class of the items that need to be saved/loaded from
   *              cache.
   * @return a {@link com.octo.android.robospice.persistence.ObjectPersister} able to load/save instances of class
   * clazz.
   * @throws com.octo.android.robospice.persistence.exception.CacheCreationException if the persist fails to create its cache (it may also create
   *                                                                                 it later, but it is suggested to fail as fast as possible).
   */
  public abstract <DATA> ObjectPersister<DATA> createObjectPersister(Class<DATA> clazz) throws CacheCreationException;

  /**
   * Indicates whether this {@link com.octo.android.robospice.persistence.ObjectPersisterFactory} will set the
   * {@link com.octo.android.robospice.persistence.ObjectPersister} instances it produces to save data asynchronously
   * or not.
   *
   * @return true if the {@link com.octo.android.robospice.persistence.ObjectPersister} instances it produces to save
   * data asynchronously. False otherwise.
   */
  public boolean isAsyncSaveEnabled() {
    return isAsyncSaveEnabled;
  }

  /**
   * Set whether this {@link com.octo.android.robospice.persistence.ObjectPersisterFactory} will set the
   * {@link com.octo.android.robospice.persistence.ObjectPersister} instances it produces to save data asynchronously
   * or not.
   *
   * @param isAsyncSaveEnabled whether or not data will be saved asynchronously by
   *                           {@link com.octo.android.robospice.persistence.ObjectPersister} instances produced by this factory.
   */
  public void setAsyncSaveEnabled(boolean isAsyncSaveEnabled) {
    this.isAsyncSaveEnabled = isAsyncSaveEnabled;
  }

  /**
   * Return the list of classes persisted by this factory.
   *
   * @return the list of classes persisted by this factory.
   */
  protected List<Class<?>> getListHandledClasses() {
    return listHandledClasses;
  }
}
