import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler';
import React from 'react';
import { StatusBar, Platform } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { NavigationContainer, useNavigationContainerRef } from '@react-navigation/native';
import { useReduxDevToolsExtension } from '@react-navigation/devtools';
import { PersistGate } from 'redux-persist/integration/react';
import { ToastProvider } from 'react-native-toast-notifications';
import { Provider } from 'react-redux';
import { store, persist } from 'core/redux';

import AppNavigation from 'core/navigation/AppNavigation';
import { configureNetwork } from 'core/network';
import { configureFeatures } from 'features';
import { configureKeyPair } from 'core/keypair';

/*
 * The starting point of the app.
 *
 * It opens the navigation component in a safeAreaProvider to be able to use
 * SafeAreaView in order to resolve issue with status bar.
 * It initializes the message registry, configures the ingestion and message signatures.
 *
 * The Platform.OS is to put the statusBar in IOS in black, otherwise it is not readable
 */
function App() {
  const { messageRegistry, keyPairRegistry, navigationOpts } = configureFeatures();
  configureKeyPair();
  configureNetwork(messageRegistry, keyPairRegistry);

  const navigationRef = useNavigationContainerRef();
  useReduxDevToolsExtension(navigationRef);

  return (
    <Provider store={store}>
      <PersistGate loading={null} persistor={persist}>
        <NavigationContainer ref={navigationRef}>
          <SafeAreaProvider>
            {Platform.OS === 'ios' && <StatusBar barStyle="dark-content" backgroundColor="white" />}
            <ToastProvider>
              <AppNavigation screens={navigationOpts.screens} />
            </ToastProvider>
          </SafeAreaProvider>
        </NavigationContainer>
      </PersistGate>
    </Provider>
  );
}

export default registerRootComponent(App);