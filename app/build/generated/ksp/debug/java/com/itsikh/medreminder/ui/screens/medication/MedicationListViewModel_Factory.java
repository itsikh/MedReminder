package com.itsikh.medreminder.ui.screens.medication;

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
public final class MedicationListViewModel_Factory implements Factory<MedicationListViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public MedicationListViewModel_Factory(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public MedicationListViewModel get() {
    return newInstance(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static MedicationListViewModel_Factory create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new MedicationListViewModel_Factory(repositoryProvider, alarmSchedulerProvider);
  }

  public static MedicationListViewModel newInstance(MedicationRepository repository,
      AlarmScheduler alarmScheduler) {
    return new MedicationListViewModel(repository, alarmScheduler);
  }
}
