package com.itsikh.medreminder.data.di;

import com.itsikh.medreminder.data.db.MedicationDao;
import com.itsikh.medreminder.data.db.MedicationDatabase;
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
public final class DatabaseModule_ProvideDaoFactory implements Factory<MedicationDao> {
  private final Provider<MedicationDatabase> dbProvider;

  public DatabaseModule_ProvideDaoFactory(Provider<MedicationDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MedicationDao get() {
    return provideDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDaoFactory create(Provider<MedicationDatabase> dbProvider) {
    return new DatabaseModule_ProvideDaoFactory(dbProvider);
  }

  public static MedicationDao provideDao(MedicationDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDao(db));
  }
}
