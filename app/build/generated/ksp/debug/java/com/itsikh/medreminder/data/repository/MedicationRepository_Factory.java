package com.itsikh.medreminder.data.repository;

import com.itsikh.medreminder.data.db.MedicationDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MedicationRepository_Factory implements Factory<MedicationRepository> {
  private final Provider<MedicationDao> daoProvider;

  public MedicationRepository_Factory(Provider<MedicationDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public MedicationRepository get() {
    return newInstance(daoProvider.get());
  }

  public static MedicationRepository_Factory create(Provider<MedicationDao> daoProvider) {
    return new MedicationRepository_Factory(daoProvider);
  }

  public static MedicationRepository newInstance(MedicationDao dao) {
    return new MedicationRepository(dao);
  }
}
