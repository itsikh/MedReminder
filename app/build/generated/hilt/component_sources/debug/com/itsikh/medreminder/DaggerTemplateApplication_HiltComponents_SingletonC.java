package com.itsikh.medreminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.itsikh.medreminder.bugreport.CrashAutoReporter;
import com.itsikh.medreminder.bugreport.GitHubIssuesClient;
import com.itsikh.medreminder.bugreport.GitHubIssuesClientModule_ProvideGitHubIssuesClientFactory;
import com.itsikh.medreminder.data.db.MedicationDao;
import com.itsikh.medreminder.data.db.MedicationDatabase;
import com.itsikh.medreminder.data.di.DatabaseModule_ProvideAlarmManagerFactory;
import com.itsikh.medreminder.data.di.DatabaseModule_ProvideDaoFactory;
import com.itsikh.medreminder.data.di.DatabaseModule_ProvideDatabaseFactory;
import com.itsikh.medreminder.data.preferences.SnoozePrefs;
import com.itsikh.medreminder.data.repository.MedicationRepository;
import com.itsikh.medreminder.logging.CrashLogRepository;
import com.itsikh.medreminder.logging.DebugSettings;
import com.itsikh.medreminder.notification.ActionReceiver;
import com.itsikh.medreminder.notification.ActionReceiver_MembersInjector;
import com.itsikh.medreminder.notification.AlarmReceiver;
import com.itsikh.medreminder.notification.AlarmReceiver_MembersInjector;
import com.itsikh.medreminder.notification.AlarmScheduler;
import com.itsikh.medreminder.notification.BootReceiver;
import com.itsikh.medreminder.notification.BootReceiver_MembersInjector;
import com.itsikh.medreminder.notification.GeofenceManager;
import com.itsikh.medreminder.notification.GeofenceReceiver;
import com.itsikh.medreminder.notification.GeofenceReceiver_MembersInjector;
import com.itsikh.medreminder.notification.NotificationHelper;
import com.itsikh.medreminder.security.SecureKeyManager;
import com.itsikh.medreminder.security.SecureKeyManagerModule_ProvideSecureKeyManagerFactory;
import com.itsikh.medreminder.ui.components.DebugOverlayViewModel;
import com.itsikh.medreminder.ui.components.DebugOverlayViewModel_HiltModules;
import com.itsikh.medreminder.ui.components.DebugOverlayViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.components.DebugOverlayViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.bugreport.BugReportViewModel;
import com.itsikh.medreminder.ui.screens.bugreport.BugReportViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.bugreport.BugReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.bugreport.BugReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.home.HomeViewModel;
import com.itsikh.medreminder.ui.screens.home.HomeViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.log.LogViewModel;
import com.itsikh.medreminder.ui.screens.log.LogViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.log.LogViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.log.LogViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.medication.AddEditViewModel;
import com.itsikh.medreminder.ui.screens.medication.AddEditViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.medication.AddEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.medication.AddEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.medication.MedicationListViewModel;
import com.itsikh.medreminder.ui.screens.medication.MedicationListViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.medication.MedicationListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.medication.MedicationListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.settings.SettingsViewModel;
import com.itsikh.medreminder.ui.screens.settings.SettingsViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.ui.screens.snooze.SnoozeSettingsViewModel;
import com.itsikh.medreminder.ui.screens.snooze.SnoozeSettingsViewModel_HiltModules;
import com.itsikh.medreminder.ui.screens.snooze.SnoozeSettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.itsikh.medreminder.ui.screens.snooze.SnoozeSettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.itsikh.medreminder.update.AppUpdateManager;
import com.itsikh.medreminder.update.AppUpdateManagerModule_ProvideAppUpdateManagerFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerTemplateApplication_HiltComponents_SingletonC {
  private DaggerTemplateApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TemplateApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TemplateApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TemplateApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TemplateApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TemplateApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TemplateApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TemplateApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TemplateApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TemplateApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TemplateApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TemplateApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends TemplateApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TemplateApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(8).put(AddEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditViewModel_HiltModules.KeyModule.provide()).put(BugReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BugReportViewModel_HiltModules.KeyModule.provide()).put(DebugOverlayViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DebugOverlayViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(LogViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LogViewModel_HiltModules.KeyModule.provide()).put(MedicationListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MedicationListViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(SnoozeSettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SnoozeSettingsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectCrashAutoReporter(instance, singletonCImpl.crashAutoReporterProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends TemplateApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddEditViewModel> addEditViewModelProvider;

    private Provider<BugReportViewModel> bugReportViewModelProvider;

    private Provider<DebugOverlayViewModel> debugOverlayViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<LogViewModel> logViewModelProvider;

    private Provider<MedicationListViewModel> medicationListViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SnoozeSettingsViewModel> snoozeSettingsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.bugReportViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.debugOverlayViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.logViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.medicationListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.snoozeSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(8).put(AddEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) addEditViewModelProvider)).put(BugReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) bugReportViewModelProvider)).put(DebugOverlayViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) debugOverlayViewModelProvider)).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) homeViewModelProvider)).put(LogViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) logViewModelProvider)).put(MedicationListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) medicationListViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(SnoozeSettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) snoozeSettingsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.itsikh.medreminder.ui.screens.medication.AddEditViewModel 
          return (T) new AddEditViewModel(singletonCImpl.medicationRepositoryProvider.get(), singletonCImpl.alarmSchedulerProvider.get());

          case 1: // com.itsikh.medreminder.ui.screens.bugreport.BugReportViewModel 
          return (T) new BugReportViewModel(singletonCImpl.provideGitHubIssuesClientProvider.get(), singletonCImpl.crashLogRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.itsikh.medreminder.ui.components.DebugOverlayViewModel 
          return (T) new DebugOverlayViewModel(singletonCImpl.debugSettingsProvider.get());

          case 3: // com.itsikh.medreminder.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.medicationRepositoryProvider.get(), singletonCImpl.alarmSchedulerProvider.get());

          case 4: // com.itsikh.medreminder.ui.screens.log.LogViewModel 
          return (T) new LogViewModel(singletonCImpl.medicationRepositoryProvider.get());

          case 5: // com.itsikh.medreminder.ui.screens.medication.MedicationListViewModel 
          return (T) new MedicationListViewModel(singletonCImpl.medicationRepositoryProvider.get(), singletonCImpl.alarmSchedulerProvider.get());

          case 6: // com.itsikh.medreminder.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.debugSettingsProvider.get(), singletonCImpl.provideSecureKeyManagerProvider.get(), singletonCImpl.provideAppUpdateManagerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.itsikh.medreminder.ui.screens.snooze.SnoozeSettingsViewModel 
          return (T) new SnoozeSettingsViewModel(singletonCImpl.snoozePrefsProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends TemplateApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends TemplateApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends TemplateApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<MedicationDatabase> provideDatabaseProvider;

    private Provider<MedicationDao> provideDaoProvider;

    private Provider<MedicationRepository> medicationRepositoryProvider;

    private Provider<SnoozePrefs> snoozePrefsProvider;

    private Provider<NotificationHelper> notificationHelperProvider;

    private Provider<AlarmManager> provideAlarmManagerProvider;

    private Provider<AlarmScheduler> alarmSchedulerProvider;

    private Provider<GeofenceManager> geofenceManagerProvider;

    private Provider<CrashLogRepository> crashLogRepositoryProvider;

    private Provider<SecureKeyManager> provideSecureKeyManagerProvider;

    private Provider<GitHubIssuesClient> provideGitHubIssuesClientProvider;

    private Provider<CrashAutoReporter> crashAutoReporterProvider;

    private Provider<DebugSettings> debugSettingsProvider;

    private Provider<AppUpdateManager> provideAppUpdateManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<MedicationDatabase>(singletonCImpl, 2));
      this.provideDaoProvider = DoubleCheck.provider(new SwitchingProvider<MedicationDao>(singletonCImpl, 1));
      this.medicationRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MedicationRepository>(singletonCImpl, 0));
      this.snoozePrefsProvider = DoubleCheck.provider(new SwitchingProvider<SnoozePrefs>(singletonCImpl, 4));
      this.notificationHelperProvider = DoubleCheck.provider(new SwitchingProvider<NotificationHelper>(singletonCImpl, 3));
      this.provideAlarmManagerProvider = DoubleCheck.provider(new SwitchingProvider<AlarmManager>(singletonCImpl, 6));
      this.alarmSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<AlarmScheduler>(singletonCImpl, 5));
      this.geofenceManagerProvider = DoubleCheck.provider(new SwitchingProvider<GeofenceManager>(singletonCImpl, 7));
      this.crashLogRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CrashLogRepository>(singletonCImpl, 9));
      this.provideSecureKeyManagerProvider = DoubleCheck.provider(new SwitchingProvider<SecureKeyManager>(singletonCImpl, 11));
      this.provideGitHubIssuesClientProvider = DoubleCheck.provider(new SwitchingProvider<GitHubIssuesClient>(singletonCImpl, 10));
      this.crashAutoReporterProvider = DoubleCheck.provider(new SwitchingProvider<CrashAutoReporter>(singletonCImpl, 8));
      this.debugSettingsProvider = DoubleCheck.provider(new SwitchingProvider<DebugSettings>(singletonCImpl, 12));
      this.provideAppUpdateManagerProvider = DoubleCheck.provider(new SwitchingProvider<AppUpdateManager>(singletonCImpl, 13));
    }

    @Override
    public void injectTemplateApplication(TemplateApplication templateApplication) {
    }

    @Override
    public void injectActionReceiver(ActionReceiver actionReceiver) {
      injectActionReceiver2(actionReceiver);
    }

    @Override
    public void injectAlarmReceiver(AlarmReceiver alarmReceiver) {
      injectAlarmReceiver2(alarmReceiver);
    }

    @Override
    public void injectBootReceiver(BootReceiver bootReceiver) {
      injectBootReceiver2(bootReceiver);
    }

    @Override
    public void injectGeofenceReceiver(GeofenceReceiver geofenceReceiver) {
      injectGeofenceReceiver2(geofenceReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private ActionReceiver injectActionReceiver2(ActionReceiver instance) {
      ActionReceiver_MembersInjector.injectRepository(instance, medicationRepositoryProvider.get());
      ActionReceiver_MembersInjector.injectNotificationHelper(instance, notificationHelperProvider.get());
      ActionReceiver_MembersInjector.injectAlarmScheduler(instance, alarmSchedulerProvider.get());
      ActionReceiver_MembersInjector.injectGeofenceManager(instance, geofenceManagerProvider.get());
      return instance;
    }

    private AlarmReceiver injectAlarmReceiver2(AlarmReceiver instance2) {
      AlarmReceiver_MembersInjector.injectRepository(instance2, medicationRepositoryProvider.get());
      AlarmReceiver_MembersInjector.injectNotificationHelper(instance2, notificationHelperProvider.get());
      AlarmReceiver_MembersInjector.injectAlarmScheduler(instance2, alarmSchedulerProvider.get());
      return instance2;
    }

    private BootReceiver injectBootReceiver2(BootReceiver instance3) {
      BootReceiver_MembersInjector.injectRepository(instance3, medicationRepositoryProvider.get());
      BootReceiver_MembersInjector.injectAlarmScheduler(instance3, alarmSchedulerProvider.get());
      return instance3;
    }

    private GeofenceReceiver injectGeofenceReceiver2(GeofenceReceiver instance4) {
      GeofenceReceiver_MembersInjector.injectRepository(instance4, medicationRepositoryProvider.get());
      GeofenceReceiver_MembersInjector.injectNotificationHelper(instance4, notificationHelperProvider.get());
      GeofenceReceiver_MembersInjector.injectSnoozePrefs(instance4, snoozePrefsProvider.get());
      return instance4;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.itsikh.medreminder.data.repository.MedicationRepository 
          return (T) new MedicationRepository(singletonCImpl.provideDaoProvider.get());

          case 1: // com.itsikh.medreminder.data.db.MedicationDao 
          return (T) DatabaseModule_ProvideDaoFactory.provideDao(singletonCImpl.provideDatabaseProvider.get());

          case 2: // com.itsikh.medreminder.data.db.MedicationDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.itsikh.medreminder.notification.NotificationHelper 
          return (T) new NotificationHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.snoozePrefsProvider.get());

          case 4: // com.itsikh.medreminder.data.preferences.SnoozePrefs 
          return (T) new SnoozePrefs(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.itsikh.medreminder.notification.AlarmScheduler 
          return (T) new AlarmScheduler(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAlarmManagerProvider.get());

          case 6: // android.app.AlarmManager 
          return (T) DatabaseModule_ProvideAlarmManagerFactory.provideAlarmManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.itsikh.medreminder.notification.GeofenceManager 
          return (T) new GeofenceManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.snoozePrefsProvider.get());

          case 8: // com.itsikh.medreminder.bugreport.CrashAutoReporter 
          return (T) new CrashAutoReporter(singletonCImpl.crashLogRepositoryProvider.get(), singletonCImpl.provideGitHubIssuesClientProvider.get());

          case 9: // com.itsikh.medreminder.logging.CrashLogRepository 
          return (T) new CrashLogRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.itsikh.medreminder.bugreport.GitHubIssuesClient 
          return (T) GitHubIssuesClientModule_ProvideGitHubIssuesClientFactory.provideGitHubIssuesClient(singletonCImpl.provideSecureKeyManagerProvider.get());

          case 11: // com.itsikh.medreminder.security.SecureKeyManager 
          return (T) SecureKeyManagerModule_ProvideSecureKeyManagerFactory.provideSecureKeyManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.itsikh.medreminder.logging.DebugSettings 
          return (T) new DebugSettings(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.itsikh.medreminder.update.AppUpdateManager 
          return (T) AppUpdateManagerModule_ProvideAppUpdateManagerFactory.provideAppUpdateManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideSecureKeyManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
