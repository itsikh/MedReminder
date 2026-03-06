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
public final class DebugSettings_Factory implements Factory<DebugSettings> {
  private final Provider<Context> contextProvider;

  public DebugSettings_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DebugSettings get() {
    return newInstance(contextProvider.get());
  }

  public static DebugSettings_Factory create(Provider<Context> contextProvider) {
    return new DebugSettings_Factory(contextProvider);
  }

  public static DebugSettings newInstance(Context context) {
    return new DebugSettings(context);
  }
}
