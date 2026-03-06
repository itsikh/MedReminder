package com.itsikh.medreminder.notification;

import com.itsikh.medreminder.data.repository.MedicationRepository;
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
public final class AlarmReceiver_MembersInjector implements MembersInjector<AlarmReceiver> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public AlarmReceiver_MembersInjector(Provider<MedicationRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  public static MembersInjector<AlarmReceiver> create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new AlarmReceiver_MembersInjector(repositoryProvider, notificationHelperProvider, alarmSchedulerProvider);
  }

  @Override
  public void injectMembers(AlarmReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectNotificationHelper(instance, notificationHelperProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.AlarmReceiver.repository")
  public static void injectRepository(AlarmReceiver instance, MedicationRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.AlarmReceiver.notificationHelper")
  public static void injectNotificationHelper(AlarmReceiver instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.AlarmReceiver.alarmScheduler")
  public static void injectAlarmScheduler(AlarmReceiver instance, AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }
}
