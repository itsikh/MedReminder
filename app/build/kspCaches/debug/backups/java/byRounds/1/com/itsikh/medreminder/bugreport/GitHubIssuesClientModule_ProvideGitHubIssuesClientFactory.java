package com.itsikh.medreminder.bugreport;

import com.itsikh.medreminder.security.SecureKeyManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class GitHubIssuesClientModule_ProvideGitHubIssuesClientFactory implements Factory<GitHubIssuesClient> {
  private final Provider<SecureKeyManager> secureKeyManagerProvider;

  public GitHubIssuesClientModule_ProvideGitHubIssuesClientFactory(
      Provider<SecureKeyManager> secureKeyManagerProvider) {
    this.secureKeyManagerProvider = secureKeyManagerProvider;
  }

  @Override
  public GitHubIssuesClient get() {
    return provideGitHubIssuesClient(secureKeyManagerProvider.get());
  }

  public static GitHubIssuesClientModule_ProvideGitHubIssuesClientFactory create(
      Provider<SecureKeyManager> secureKeyManagerProvider) {
    return new GitHubIssuesClientModule_ProvideGitHubIssuesClientFactory(secureKeyManagerProvider);
  }

  public static GitHubIssuesClient provideGitHubIssuesClient(SecureKeyManager secureKeyManager) {
    return Preconditions.checkNotNullFromProvides(GitHubIssuesClientModule.INSTANCE.provideGitHubIssuesClient(secureKeyManager));
  }
}
