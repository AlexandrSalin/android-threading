package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

import java.util.List;

public class GetAllDataFromCacheCommand<T> extends SpiceManager.SpiceManagerCommand<List<T>> {
  private Class<T> clazz;

  public GetAllDataFromCacheCommand(SpiceManager spiceManager, Class<T> clazz) {
    super(spiceManager);
    this.clazz = clazz;
  }

  @Override
  protected List<T> executeWhenBound(SpiceService spiceService) throws CacheLoadingException, CacheCreationException {
    return spiceService.loadAllDataFromCache(clazz);
  }
}
