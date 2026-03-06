package com.itsikh.medreminder.bugreport;

import com.itsikh.medreminder.logging.CrashLogRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CrashAutoReporter_Factory implements Factory<CrashAutoReporter> {
  private final Provider<CrashLogRepository> crashLogRepositoryProvider;

  private final Provider<GitHubIssuesClient> gitHubIssuesClientProvider;

  public CrashAutoReporter_Factory(Provider<CrashLogRepository> crashLogRepositoryProvider,
      Provider<GitHubIssuesClient> gitHubIssuesClientProvider) {
    this.crashLogRepositoryProvider = crashLogRepositoryProvider;
    this.gitHubIssuesClientProvider = gitHubIssuesClientProvider;
  }

  @Override
  public CrashAutoReporter get() {
    return newInstance(crashLogRepositoryProvider.get(), gitHubIssuesClientProvider.get());
  }

  public static CrashAutoReporter_Factory create(
      Provider<CrashLogRepository> crashLogRepositoryProvider,
      Provider<GitHubIssuesClient> gitHubIssuesClientProvider) {
    return new CrashAutoReporter_Factory(crashLogRepositoryProvider, gitHubIssuesClientProvider);
  }

  public static CrashAutoReporter newInstance(CrashLogRepository crashLogRepository,
      GitHubIssuesClient gitHubIssuesClient) {
    return new CrashAutoReporter(crashLogRepository, gitHubIssuesClient);
  }
}
