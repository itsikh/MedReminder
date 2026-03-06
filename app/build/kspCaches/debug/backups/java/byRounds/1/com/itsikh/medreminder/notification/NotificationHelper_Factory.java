package com.itsikh.medreminder.notification;

import android.content.Context;
import com.itsikh.medreminder.data.preferences.SnoozePrefs;
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
public final class NotificationHelper_Factory implements Factory<NotificationHelper> {
  private final Provider<Context> contextProvider;

  private final Provider<SnoozePrefs> snoozePrefsProvider;

  public NotificationHelper_Factory(Provider<Context> contextProvider,
      Provider<SnoozePrefs> snoozePrefsProvider) {
    this.contextProvider = contextProvider;
    this.snoozePrefsProvider = snoozePrefsProvider;
  }

  @Override
  public NotificationHelper get() {
    return newInstance(contextProvider.get(), snoozePrefsProvider.get());
  }

  public static NotificationHelper_Factory create(Provider<Context> contextProvider,
      Provider<SnoozePrefs> snoozePrefsProvider) {
    return new NotificationHelper_Factory(contextProvider, snoozePrefsProvider);
  }

  public static NotificationHelper newInstance(Context context, SnoozePrefs snoozePrefs) {
    return new NotificationHelper(context, snoozePrefs);
  }
}
