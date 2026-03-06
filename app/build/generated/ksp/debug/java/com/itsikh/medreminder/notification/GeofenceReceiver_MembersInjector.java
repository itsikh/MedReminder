package com.itsikh.medreminder.notification;

import com.itsikh.medreminder.data.preferences.SnoozePrefs;
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
public final class GeofenceReceiver_MembersInjector implements MembersInjector<GeofenceReceiver> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<SnoozePrefs> snoozePrefsProvider;

  public GeofenceReceiver_MembersInjector(Provider<MedicationRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<SnoozePrefs> snoozePrefsProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.snoozePrefsProvider = snoozePrefsProvider;
  }

  public static MembersInjector<GeofenceReceiver> create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider,
      Provider<SnoozePrefs> snoozePrefsProvider) {
    return new GeofenceReceiver_MembersInjector(repositoryProvider, notificationHelperProvider, snoozePrefsProvider);
  }

  @Override
  public void injectMembers(GeofenceReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectNotificationHelper(instance, notificationHelperProvider.get());
    injectSnoozePrefs(instance, snoozePrefsProvider.get());
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.GeofenceReceiver.repository")
  public static void injectRepository(GeofenceReceiver instance, MedicationRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.GeofenceReceiver.notificationHelper")
  public static void injectNotificationHelper(GeofenceReceiver instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }

  @InjectedFieldSignature("com.itsikh.medreminder.notification.GeofenceReceiver.snoozePrefs")
  public static void injectSnoozePrefs(GeofenceReceiver instance, SnoozePrefs snoozePrefs) {
    instance.snoozePrefs = snoozePrefs;
  }
}
