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
public final class ActionReceiver_MembersInjector implements MembersInjector<ActionReceiver> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private final Provider<GeofenceManager> geofenceManagerProvider;

  public ActionReceiver_MembersInjector(Provider<MedicationRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<GeofenceManager> geofenceManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
    this.geofenceManagerProvider = geofenceManagerProvider;
  }

  public static MembersInjector<ActionReceiver> create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<GeofenceManager> geofenceManagerProvider) {
    return new ActionReceiver_MembersInjector(repositoryProvider, notificationHelperProvider, alarmSchedulerProvider, geofenceManagerProvider);
  }

  @Override
  public void injectMembers(ActionReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectNotificationHelper(instance, notificationHelperProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
    injectGeofenceManager(instance, geofenceManagerProvider.get());
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.ActionReceiver.repository")
  public static void injectRepository(ActionReceiver instance, MedicationRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.ActionReceiver.notificationHelper")
  public static void injectNotificationHelper(ActionReceiver instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.ActionReceiver.alarmScheduler")
  public static void injectAlarmScheduler(ActionReceiver instance, AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.ActionReceiver.geofenceManager")
  public static void injectGeofenceManager(ActionReceiver instance,
      GeofenceManager geofenceManager) {
    instance.geofenceManager = geofenceManager;
  }
}
