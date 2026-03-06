package com.itsikh.medreminder.ui.screens.bugreport;

import android.content.Context;
import com.itsikh.medreminder.bugreport.GitHubIssuesClient;
import com.itsikh.medreminder.logging.CrashLogRepository;
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
public final class BugReportViewModel_Factory implements Factory<BugReportViewModel> {
  private final Provider<GitHubIssuesClient> gitHubIssuesClientProvider;

  private final Provider<CrashLogRepository> crashLogRepositoryProvider;

  private final Provider<Context> contextProvider;

  public BugReportViewModel_Factory(Provider<GitHubIssuesClient> gitHubIssuesClientProvider,
      Provider<CrashLogRepository> crashLogRepositoryProvider, Provider<Context> contextProvider) {
    this.gitHubIssuesClientProvider = gitHubIssuesClientProvider;
    this.crashLogRepositoryProvider = crashLogRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public BugReportViewModel get() {
    return newInstance(gitHubIssuesClientProvider.get(), crashLogRepositoryProvider.get(), contextProvider.get());
  }

  public static BugReportViewModel_Factory create(
      Provider<GitHubIssuesClient> gitHubIssuesClientProvider,
      Provider<CrashLogRepository> crashLogRepositoryProvider, Provider<Context> contextProvider) {
    return new BugReportViewModel_Factory(gitHubIssuesClientProvider, crashLogRepositoryProvider, contextProvider);
  }

  public static BugReportViewModel newInstance(GitHubIssuesClient gitHubIssuesClient,
      CrashLogRepository crashLogRepository, Context context) {
    return new BugReportViewModel(gitHubIssuesClient, crashLogRepository, context);
  }
}
