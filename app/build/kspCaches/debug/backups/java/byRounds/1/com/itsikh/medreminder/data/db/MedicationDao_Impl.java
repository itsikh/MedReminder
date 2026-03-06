package com.itsikh.medreminder.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.itsikh.medreminder.data.model.LogStatus;
import com.itsikh.medreminder.data.model.Medication;
import com.itsikh.medreminder.data.model.MedicationLog;
import com.itsikh.medreminder.data.model.MedicationSchedule;
import com.itsikh.medreminder.data.model.MedicationWithSchedules;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MedicationDao_Impl implements MedicationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Medication> __insertionAdapterOfMedication;

  private final EntityInsertionAdapter<MedicationSchedule> __insertionAdapterOfMedicationSchedule;

  private final EntityInsertionAdapter<MedicationLog> __insertionAdapterOfMedicationLog;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<MedicationSchedule> __deletionAdapterOfMedicationSchedule;

  private final EntityDeletionOrUpdateAdapter<Medication> __updateAdapterOfMedication;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateMedication;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSchedulesForMedication;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLogStatus;

  public MedicationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMedication = new EntityInsertionAdapter<Medication>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medications` (`id`,`name`,`dosage`,`color`,`isActive`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Medication entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDosage());
        statement.bindLong(4, entity.getColor());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfMedicationSchedule = new EntityInsertionAdapter<MedicationSchedule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medication_schedules` (`id`,`medicationId`,`timeHour`,`timeMinute`,`daysOfWeek`,`isEnabled`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationSchedule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicationId());
        statement.bindLong(3, entity.getTimeHour());
        statement.bindLong(4, entity.getTimeMinute());
        statement.bindLong(5, entity.getDaysOfWeek());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__insertionAdapterOfMedicationLog = new EntityInsertionAdapter<MedicationLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medication_logs` (`id`,`medicationId`,`scheduleId`,`medicationName`,`dosage`,`scheduledTimeMillis`,`takenTimeMillis`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicationId());
        statement.bindLong(3, entity.getScheduleId());
        statement.bindString(4, entity.getMedicationName());
        statement.bindString(5, entity.getDosage());
        statement.bindLong(6, entity.getScheduledTimeMillis());
        if (entity.getTakenTimeMillis() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getTakenTimeMillis());
        }
        final String _tmp = __converters.fromLogStatus(entity.getStatus());
        statement.bindString(8, _tmp);
      }
    };
    this.__deletionAdapterOfMedicationSchedule = new EntityDeletionOrUpdateAdapter<MedicationSchedule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `medication_schedules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationSchedule entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMedication = new EntityDeletionOrUpdateAdapter<Medication>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `medications` SET `id` = ?,`name` = ?,`dosage` = ?,`color` = ?,`isActive` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Medication entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDosage());
        statement.bindLong(4, entity.getColor());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeactivateMedication = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE medications SET isActive = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSchedulesForMedication = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM medication_schedules WHERE medicationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLogStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE medication_logs SET status = ?, takenTimeMillis = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMedication(final Medication medication,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMedication.insertAndReturnId(medication);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSchedule(final MedicationSchedule schedule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMedicationSchedule.insertAndReturnId(schedule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSchedules(final List<MedicationSchedule> schedules,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMedicationSchedule.insert(schedules);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLog(final MedicationLog log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMedicationLog.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSchedule(final MedicationSchedule schedule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMedicationSchedule.handle(schedule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMedication(final Medication medication,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMedication.handle(medication);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateMedication(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateMedication.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeactivateMedication.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSchedulesForMedication(final int medId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSchedulesForMedication.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, medId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSchedulesForMedication.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLogStatus(final int id, final LogStatus status, final Long takenMs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLogStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromLogStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
        if (takenMs == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, takenMs);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateLogStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationWithSchedules>> getMedicationsWithSchedules() {
    final String _sql = "SELECT * FROM medications WHERE isActive = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"medication_schedules",
        "medications"}, new Callable<List<MedicationWithSchedules>>() {
      @Override
      @NonNull
      public List<MedicationWithSchedules> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
            final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
            final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final LongSparseArray<ArrayList<MedicationSchedule>> _collectionSchedules = new LongSparseArray<ArrayList<MedicationSchedule>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionSchedules.containsKey(_tmpKey)) {
                _collectionSchedules.put(_tmpKey, new ArrayList<MedicationSchedule>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipmedicationSchedulesAscomItsikhMedreminderDataModelMedicationSchedule(_collectionSchedules);
            final List<MedicationWithSchedules> _result = new ArrayList<MedicationWithSchedules>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final MedicationWithSchedules _item;
              final Medication _tmpMedication;
              final int _tmpId;
              _tmpId = _cursor.getInt(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final String _tmpDosage;
              _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
              final int _tmpColor;
              _tmpColor = _cursor.getInt(_cursorIndexOfColor);
              final boolean _tmpIsActive;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsActive);
              _tmpIsActive = _tmp != 0;
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              _tmpMedication = new Medication(_tmpId,_tmpName,_tmpDosage,_tmpColor,_tmpIsActive,_tmpCreatedAt);
              final ArrayList<MedicationSchedule> _tmpSchedulesCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpSchedulesCollection = _collectionSchedules.get(_tmpKey_1);
              _item = new MedicationWithSchedules(_tmpMedication,_tmpSchedulesCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMedicationById(final int id,
      final Continuation<? super Medication> $completion) {
    final String _sql = "SELECT * FROM medications WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Medication>() {
      @Override
      @Nullable
      public Medication call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Medication _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final int _tmpColor;
            _tmpColor = _cursor.getInt(_cursorIndexOfColor);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Medication(_tmpId,_tmpName,_tmpDosage,_tmpColor,_tmpIsActive,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSchedulesForMedication(final int medId,
      final Continuation<? super List<MedicationSchedule>> $completion) {
    final String _sql = "SELECT * FROM medication_schedules WHERE medicationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, medId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MedicationSchedule>>() {
      @Override
      @NonNull
      public List<MedicationSchedule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "timeHour");
          final int _cursorIndexOfTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinute");
          final int _cursorIndexOfDaysOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeek");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<MedicationSchedule> _result = new ArrayList<MedicationSchedule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationSchedule _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpMedicationId;
            _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
            final int _tmpTimeHour;
            _tmpTimeHour = _cursor.getInt(_cursorIndexOfTimeHour);
            final int _tmpTimeMinute;
            _tmpTimeMinute = _cursor.getInt(_cursorIndexOfTimeMinute);
            final int _tmpDaysOfWeek;
            _tmpDaysOfWeek = _cursor.getInt(_cursorIndexOfDaysOfWeek);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new MedicationSchedule(_tmpId,_tmpMedicationId,_tmpTimeHour,_tmpTimeMinute,_tmpDaysOfWeek,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getScheduleById(final int id,
      final Continuation<? super MedicationSchedule> $completion) {
    final String _sql = "SELECT * FROM medication_schedules WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MedicationSchedule>() {
      @Override
      @Nullable
      public MedicationSchedule call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "timeHour");
          final int _cursorIndexOfTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinute");
          final int _cursorIndexOfDaysOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeek");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final MedicationSchedule _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpMedicationId;
            _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
            final int _tmpTimeHour;
            _tmpTimeHour = _cursor.getInt(_cursorIndexOfTimeHour);
            final int _tmpTimeMinute;
            _tmpTimeMinute = _cursor.getInt(_cursorIndexOfTimeMinute);
            final int _tmpDaysOfWeek;
            _tmpDaysOfWeek = _cursor.getInt(_cursorIndexOfDaysOfWeek);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _result = new MedicationSchedule(_tmpId,_tmpMedicationId,_tmpTimeHour,_tmpTimeMinute,_tmpDaysOfWeek,_tmpIsEnabled);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllActiveSchedules(
      final Continuation<? super List<MedicationSchedule>> $completion) {
    final String _sql = "SELECT ms.* FROM medication_schedules ms INNER JOIN medications m ON ms.medicationId = m.id WHERE ms.isEnabled = 1 AND m.isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MedicationSchedule>>() {
      @Override
      @NonNull
      public List<MedicationSchedule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfTimeHour = CursorUtil.getColumnIndexOrThrow(_cursor, "timeHour");
          final int _cursorIndexOfTimeMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "timeMinute");
          final int _cursorIndexOfDaysOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeek");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<MedicationSchedule> _result = new ArrayList<MedicationSchedule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationSchedule _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpMedicationId;
            _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
            final int _tmpTimeHour;
            _tmpTimeHour = _cursor.getInt(_cursorIndexOfTimeHour);
            final int _tmpTimeMinute;
            _tmpTimeMinute = _cursor.getInt(_cursorIndexOfTimeMinute);
            final int _tmpDaysOfWeek;
            _tmpDaysOfWeek = _cursor.getInt(_cursorIndexOfDaysOfWeek);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new MedicationSchedule(_tmpId,_tmpMedicationId,_tmpTimeHour,_tmpTimeMinute,_tmpDaysOfWeek,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationLog>> getAllLogs() {
    final String _sql = "SELECT * FROM medication_logs ORDER BY scheduledTimeMillis DESC LIMIT 300";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medication_logs"}, new Callable<List<MedicationLog>>() {
      @Override
      @NonNull
      public List<MedicationLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfScheduleId = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfScheduledTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTimeMillis");
          final int _cursorIndexOfTakenTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimeMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<MedicationLog> _result = new ArrayList<MedicationLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationLog _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpMedicationId;
            _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
            final int _tmpScheduleId;
            _tmpScheduleId = _cursor.getInt(_cursorIndexOfScheduleId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final long _tmpScheduledTimeMillis;
            _tmpScheduledTimeMillis = _cursor.getLong(_cursorIndexOfScheduledTimeMillis);
            final Long _tmpTakenTimeMillis;
            if (_cursor.isNull(_cursorIndexOfTakenTimeMillis)) {
              _tmpTakenTimeMillis = null;
            } else {
              _tmpTakenTimeMillis = _cursor.getLong(_cursorIndexOfTakenTimeMillis);
            }
            final LogStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toLogStatus(_tmp);
            _item = new MedicationLog(_tmpId,_tmpMedicationId,_tmpScheduleId,_tmpMedicationName,_tmpDosage,_tmpScheduledTimeMillis,_tmpTakenTimeMillis,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MedicationLog>> getLogsForDay(final long startMs, final long endMs) {
    final String _sql = "SELECT * FROM medication_logs WHERE scheduledTimeMillis >= ? AND scheduledTimeMillis < ? ORDER BY scheduledTimeMillis ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medication_logs"}, new Callable<List<MedicationLog>>() {
      @Override
      @NonNull
      public List<MedicationLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfScheduleId = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfScheduledTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTimeMillis");
          final int _cursorIndexOfTakenTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimeMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<MedicationLog> _result = new ArrayList<MedicationLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationLog _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpMedicationId;
            _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
            final int _tmpScheduleId;
            _tmpScheduleId = _cursor.getInt(_cursorIndexOfScheduleId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final long _tmpScheduledTimeMillis;
            _tmpScheduledTimeMillis = _cursor.getLong(_cursorIndexOfScheduledTimeMillis);
            final Long _tmpTakenTimeMillis;
            if (_cursor.isNull(_cursorIndexOfTakenTimeMillis)) {
              _tmpTakenTimeMillis = null;
            } else {
              _tmpTakenTimeMillis = _cursor.getLong(_cursorIndexOfTakenTimeMillis);
            }
            final LogStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toLogStatus(_tmp);
            _item = new MedicationLog(_tmpId,_tmpMedicationId,_tmpScheduleId,_tmpMedicationName,_tmpDosage,_tmpScheduledTimeMillis,_tmpTakenTimeMillis,_tmpStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLogById(final int id, final Continuation<? super MedicationLog> $completion) {
    final String _sql = "SELECT * FROM medication_logs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MedicationLog>() {
      @Override
      @Nullable
      public MedicationLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfScheduleId = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleId");
          final int _cursorIndexOfMedicationName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationName");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfScheduledTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTimeMillis");
          final int _cursorIndexOfTakenTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "takenTimeMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final MedicationLog _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpMedicationId;
            _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
            final int _tmpScheduleId;
            _tmpScheduleId = _cursor.getInt(_cursorIndexOfScheduleId);
            final String _tmpMedicationName;
            _tmpMedicationName = _cursor.getString(_cursorIndexOfMedicationName);
            final String _tmpDosage;
            _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            final long _tmpScheduledTimeMillis;
            _tmpScheduledTimeMillis = _cursor.getLong(_cursorIndexOfScheduledTimeMillis);
            final Long _tmpTakenTimeMillis;
            if (_cursor.isNull(_cursorIndexOfTakenTimeMillis)) {
              _tmpTakenTimeMillis = null;
            } else {
              _tmpTakenTimeMillis = _cursor.getLong(_cursorIndexOfTakenTimeMillis);
            }
            final LogStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toLogStatus(_tmp);
            _result = new MedicationLog(_tmpId,_tmpMedicationId,_tmpScheduleId,_tmpMedicationName,_tmpDosage,_tmpScheduledTimeMillis,_tmpTakenTimeMillis,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipmedicationSchedulesAscomItsikhMedreminderDataModelMedicationSchedule(
      @NonNull final LongSparseArray<ArrayList<MedicationSchedule>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipmedicationSchedulesAscomItsikhMedreminderDataModelMedicationSchedule(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`medicationId`,`timeHour`,`timeMinute`,`daysOfWeek`,`isEnabled` FROM `medication_schedules` WHERE `medicationId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "medicationId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfMedicationId = 1;
      final int _cursorIndexOfTimeHour = 2;
      final int _cursorIndexOfTimeMinute = 3;
      final int _cursorIndexOfDaysOfWeek = 4;
      final int _cursorIndexOfIsEnabled = 5;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<MedicationSchedule> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final MedicationSchedule _item_1;
          final int _tmpId;
          _tmpId = _cursor.getInt(_cursorIndexOfId);
          final int _tmpMedicationId;
          _tmpMedicationId = _cursor.getInt(_cursorIndexOfMedicationId);
          final int _tmpTimeHour;
          _tmpTimeHour = _cursor.getInt(_cursorIndexOfTimeHour);
          final int _tmpTimeMinute;
          _tmpTimeMinute = _cursor.getInt(_cursorIndexOfTimeMinute);
          final int _tmpDaysOfWeek;
          _tmpDaysOfWeek = _cursor.getInt(_cursorIndexOfDaysOfWeek);
          final boolean _tmpIsEnabled;
          final int _tmp;
          _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
          _tmpIsEnabled = _tmp != 0;
          _item_1 = new MedicationSchedule(_tmpId,_tmpMedicationId,_tmpTimeHour,_tmpTimeMinute,_tmpDaysOfWeek,_tmpIsEnabled);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
