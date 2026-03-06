package com.itsikh.medreminder.ui.screens.home;

import com.itsikh.medreminder.data.repository.MedicationRepository;
import com.itsikh.medreminder.notification.AlarmScheduler;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public HomeViewModel_Factory(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new HomeViewModel_Factory(repositoryProvider, alarmSchedulerProvider);
  }

  public static HomeViewModel newInstance(MedicationRepository repository,
      AlarmScheduler alarmScheduler) {
    return new HomeViewModel(repository, alarmScheduler);
  }
}
