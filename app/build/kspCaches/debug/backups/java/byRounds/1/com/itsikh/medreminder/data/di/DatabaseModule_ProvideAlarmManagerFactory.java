package com.itsikh.medreminder.data.di;

import android.app.AlarmManager;
import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideAlarmManagerFactory implements Factory<AlarmManager> {
  private final Provider<Context> ctxProvider;

  public DatabaseModule_ProvideAlarmManagerFactory(Provider<Context> ctxProvider) {
    this.ctxProvider = ctxProvider;
  }

  @Override
  public AlarmManager get() {
    return provideAlarmManager(ctxProvider.get());
  }

  public static DatabaseModule_ProvideAlarmManagerFactory create(Provider<Context> ctxProvider) {
    return new DatabaseModule_ProvideAlarmManagerFactory(ctxProvider);
  }

  public static AlarmManager provideAlarmManager(Context ctx) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAlarmManager(ctx));
  }
}
