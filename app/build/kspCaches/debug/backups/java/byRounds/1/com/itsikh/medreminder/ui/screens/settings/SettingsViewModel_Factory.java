package com.itsikh.medreminder.ui.screens.settings;

import android.content.Context;
import com.itsikh.medreminder.logging.DebugSettings;
import com.itsikh.medreminder.security.SecureKeyManager;
import com.itsikh.medreminder.update.AppUpdateManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<DebugSettings> debugSettingsProvider;

  private final Provider<SecureKeyManager> secureKeyManagerProvider;

  private final Provider<AppUpdateManager> updateManagerProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<DebugSettings> debugSettingsProvider,
      Provider<SecureKeyManager> secureKeyManagerProvider,
      Provider<AppUpdateManager> updateManagerProvider, Provider<Context> contextProvider) {
    this.debugSettingsProvider = debugSettingsProvider;
    this.secureKeyManagerProvider = secureKeyManagerProvider;
    this.updateManagerProvider = updateManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(debugSettingsProvider.get(), secureKeyManagerProvider.get(), updateManagerProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<DebugSettings> debugSettingsProvider,
      Provider<SecureKeyManager> secureKeyManagerProvider,
      Provider<AppUpdateManager> updateManagerProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(debugSettingsProvider, secureKeyManagerProvider, updateManagerProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(DebugSettings debugSettings,
      SecureKeyManager secureKeyManager, AppUpdateManager updateManager, Context context) {
    return new SettingsViewModel(debugSettings, secureKeyManager, updateManager, context);
  }
}
