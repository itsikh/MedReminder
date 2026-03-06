package com.itsikh.medreminder;

import com.itsikh.medreminder.bugreport.CrashAutoReporter;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<CrashAutoReporter> crashAutoReporterProvider;

  public MainActivity_MembersInjector(Provider<CrashAutoReporter> crashAutoReporterProvider) {
    this.crashAutoReporterProvider = crashAutoReporterProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<CrashAutoReporter> crashAutoReporterProvider) {
    return new MainActivity_MembersInjector(crashAutoReporterProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectCrashAutoReporter(instance, crashAutoReporterProvider.get());
  }

  @InjectedFieldSignature("com.itsikh.medreminder.MainActivity.crashAutoReporter")
  public static void injectCrashAutoReporter(MainActivity instance,
      CrashAutoReporter crashAutoReporter) {
    instance.crashAutoReporter = crashAutoReporter;
  }
}
