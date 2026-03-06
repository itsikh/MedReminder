package com.itsikh.medreminder.ui.screens.snooze;

import android.content.Context;
import com.itsikh.medreminder.data.preferences.SnoozePrefs;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SnoozeSettingsViewModel_Factory implements Factory<SnoozeSettingsViewModel> {
  private final Provider<SnoozePrefs> snoozePrefsProvider;

  private final Provider<Context> contextProvider;

  public SnoozeSettingsViewModel_Factory(Provider<SnoozePrefs> snoozePrefsProvider,
      Provider<Context> contextProvider) {
    this.snoozePrefsProvider = snoozePrefsProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SnoozeSettingsViewModel get() {
    return newInstance(snoozePrefsProvider.get(), contextProvider.get());
  }

  public static SnoozeSettingsViewModel_Factory create(Provider<SnoozePrefs> snoozePrefsProvider,
      Provider<Context> contextProvider) {
    return new SnoozeSettingsViewModel_Factory(snoozePrefsProvider, contextProvider);
  }

  public static SnoozeSettingsViewModel newInstance(SnoozePrefs snoozePrefs, Context context) {
    return new SnoozeSettingsViewModel(snoozePrefs, context);
  }
}
