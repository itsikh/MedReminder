package com.itsikh.medreminder.data.preferences;

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
public final class SnoozePrefs_Factory implements Factory<SnoozePrefs> {
  private final Provider<Context> contextProvider;

  public SnoozePrefs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SnoozePrefs get() {
    return newInstance(contextProvider.get());
  }

  public static SnoozePrefs_Factory create(Provider<Context> contextProvider) {
    return new SnoozePrefs_Factory(contextProvider);
  }

  public static SnoozePrefs newInstance(Context context) {
    return new SnoozePrefs(context);
  }
}
