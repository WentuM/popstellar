package com.github.dedis.popstellar.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.crypto.tink.signature.Ed25519PrivateKeyManager;
import com.google.crypto.tink.signature.PublicKeySignWrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class KeysetModule {

  private static final String KEYSET_NAME = "POP_KEYSET";
  private static final String SHARED_PREF_FILE_NAME = "POP_KEYSET_SP";
  private static final String MASTER_KEY_URI = "android-keystore://POP_MASTER_KEY";

  private KeysetModule() {}

  @Provides
  @Singleton
  public static AndroidKeysetManager provideAndroidKeysetManager(
      @ApplicationContext Context applicationContext) {

    try {
      SharedPreferences.Editor editor =
          applicationContext
              .getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
              .edit();
      editor.apply();

      Ed25519PrivateKeyManager.registerPair(true);
      PublicKeySignWrapper.register();

      // TODO: move to background thread

      return new AndroidKeysetManager.Builder()
          .withSharedPref(applicationContext, KEYSET_NAME, SHARED_PREF_FILE_NAME)
          .withKeyTemplate(Ed25519PrivateKeyManager.rawEd25519Template())
          .withMasterKeyUri(MASTER_KEY_URI)
          .build();
    } catch (IOException | GeneralSecurityException e) {
      throw new SecurityException("Could not retrieve the keyset from the app", e);
    }
  }
}
