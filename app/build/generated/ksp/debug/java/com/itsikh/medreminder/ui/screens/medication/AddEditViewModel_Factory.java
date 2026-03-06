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
public final class AddEditViewModel_Factory implements Factory<AddEditViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public AddEditViewModel_Factory(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public AddEditViewModel get() {
    return newInstance(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static AddEditViewModel_Factory create(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new AddEditViewModel_Factory(repositoryProvider, alarmSchedulerProvider);
  }

  public static AddEditViewModel newInstance(MedicationRepository repository,
      AlarmScheduler alarmScheduler) {
    return new AddEditViewModel(repository, alarmScheduler);
  }
}
