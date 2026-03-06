package com.itsikh.medreminder.security;

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
public final class SecureKeyManagerModule_ProvideSecureKeyManagerFactory implements Factory<SecureKeyManager> {
  private final Provider<Context> contextProvider;

  public SecureKeyManagerModule_ProvideSecureKeyManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecureKeyManager get() {
    return provideSecureKeyManager(contextProvider.get());
  }

  public static SecureKeyManagerModule_ProvideSecureKeyManagerFactory create(
      Provider<Context> contextProvider) {
    return new SecureKeyManagerModule_ProvideSecureKeyManagerFactory(contextProvider);
  }

  public static SecureKeyManager provideSecureKeyManager(Context context) {
    return Preconditions.checkNotNullFromProvides(SecureKeyManagerModule.INSTANCE.provideSecureKeyManager(context));
  }
}
