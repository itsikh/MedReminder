package com.itsikh.medreminder.ui.screens.log;

import com.itsikh.medreminder.data.repository.MedicationRepository;
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
public final class LogViewModel_Factory implements Factory<LogViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  public LogViewModel_Factory(Provider<MedicationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public LogViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static LogViewModel_Factory create(Provider<MedicationRepository> repositoryProvider) {
    return new LogViewModel_Factory(repositoryProvider);
  }

  public static LogViewModel newInstance(MedicationRepository repository) {
    return new LogViewModel(repository);
  }
}
