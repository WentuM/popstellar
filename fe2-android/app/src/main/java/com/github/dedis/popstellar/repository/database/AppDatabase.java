package com.github.dedis.popstellar.repository.database;

import androidx.room.*;

import com.github.dedis.popstellar.repository.database.lao.LAODao;
import com.github.dedis.popstellar.repository.database.lao.LAOEntity;
import com.github.dedis.popstellar.repository.database.message.MessageDao;
import com.github.dedis.popstellar.repository.database.message.MessageEntity;
import com.github.dedis.popstellar.repository.database.subscriptions.SubscriptionsDao;
import com.github.dedis.popstellar.repository.database.subscriptions.SubscriptionsEntity;
import com.github.dedis.popstellar.repository.database.wallet.WalletDao;
import com.github.dedis.popstellar.repository.database.wallet.WalletEntity;

import javax.inject.Singleton;

@Singleton
@Database(
    entities = {
      MessageEntity.class,
      LAOEntity.class,
      WalletEntity.class,
      SubscriptionsEntity.class
    },
    version = 1)
@TypeConverters(CustomTypeConverters.class)
public abstract class AppDatabase extends RoomDatabase {
  public abstract MessageDao messageDao();

  public abstract LAODao laoDao();

  public abstract WalletDao walletDao();

  public abstract SubscriptionsDao subscriptionsDao();
}
