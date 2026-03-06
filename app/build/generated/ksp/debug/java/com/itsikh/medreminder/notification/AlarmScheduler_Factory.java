package com.itsikh.medreminder.notification;

import android.app.AlarmManager;
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
public final class AlarmScheduler_Factory implements Factory<AlarmScheduler> {
  private final Provider<Context> contextProvider;

  private final Provider<AlarmManager> alarmManagerProvider;

  public AlarmScheduler_Factory(Provider<Context> contextProvider,
      Provider<AlarmManager> alarmManagerProvider) {
    this.contextProvider = contextProvider;
    this.alarmManagerProvider = alarmManagerProvider;
  }

  @Override
  public AlarmScheduler get() {
    return newInstance(contextProvider.get(), alarmManagerProvider.get());
  }

  public static AlarmScheduler_Factory create(Provider<Context> contextProvider,
      Provider<AlarmManager> alarmManagerProvider) {
    return new AlarmScheduler_Factory(contextProvider, alarmManagerProvider);
  }

  public static AlarmScheduler newInstance(Context context, AlarmManager alarmManager) {
    return new AlarmScheduler(context, alarmManager);
  }
}
