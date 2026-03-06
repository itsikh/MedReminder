package com.itsikh.medreminder.update;

import android.content.Context;
import com.itsikh.medreminder.security.SecureKeyManager;
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
public final class AppUpdateManagerModule_ProvideAppUpdateManagerFactory implements Factory<AppUpdateManager> {
  private final Provider<Context> contextProvider;

  private final Provider<SecureKeyManager> secureKeyManagerProvider;

  public AppUpdateManagerModule_ProvideAppUpdateManagerFactory(Provider<Context> contextProvider,
      Provider<SecureKeyManager> secureKeyManagerProvider) {
    this.contextProvider = contextProvider;
    this.secureKeyManagerProvider = secureKeyManagerProvider;
  }

  @Override
  public AppUpdateManager get() {
    return provideAppUpdateManager(contextProvider.get(), secureKeyManagerProvider.get());
  }

  public static AppUpdateManagerModule_ProvideAppUpdateManagerFactory create(
      Provider<Context> contextProvider, Provider<SecureKeyManager> secureKeyManagerProvider) {
    return new AppUpdateManagerModule_ProvideAppUpdateManagerFactory(contextProvider, secureKeyManagerProvider);
  }

  public static AppUpdateManager provideAppUpdateManager(Context context,
      SecureKeyManager secureKeyManager) {
    return Preconditions.checkNotNullFromProvides(AppUpdateManagerModule.INSTANCE.provideAppUpdateManager(context, secureKeyManager));
  }
}
