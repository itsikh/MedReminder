package com.itsikh.medreminder.logging;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CrashLogRepository_Factory implements Factory<CrashLogRepository> {
  private final Provider<Context> contextProvider;

  public CrashLogRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CrashLogRepository get() {
    return newInstance(contextProvider.get());
  }

  public static CrashLogRepository_Factory create(Provider<Context> contextProvider) {
    return new CrashLogRepository_Factory(contextProvider);
  }

  public static CrashLogRepository newInstance(Context context) {
    return new CrashLogRepository(context);
  }
}
