package com.itsikh.medreminder.ui.components;

import com.itsikh.medreminder.logging.DebugSettings;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class DebugOverlayViewModel_Factory implements Factory<DebugOverlayViewModel> {
  private final Provider<DebugSettings> debugSettingsProvider;

  public DebugOverlayViewModel_Factory(Provider<DebugSettings> debugSettingsProvider) {
    this.debugSettingsProvider = debugSettingsProvider;
  }

  @Override
  public DebugOverlayViewModel get() {
    return newInstance(debugSettingsProvider.get());
  }

  public static DebugOverlayViewModel_Factory create(
      Provider<DebugSettings> debugSettingsProvider) {
    return new DebugOverlayViewModel_Factory(debugSettingsProvider);
  }

  public static DebugOverlayViewModel newInstance(DebugSettings debugSettings) {
    return new DebugOverlayViewModel(debugSettings);
  }
}
