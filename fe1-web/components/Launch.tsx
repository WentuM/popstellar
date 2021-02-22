// TODO remove the line above when console will not be use
import React from 'react';
import {
  StyleSheet, View, Text, Button, TextInput, TextStyle, ViewStyle,
} from 'react-native';

import { Spacing, Typography } from 'styles';
import STRINGS from 'res/strings';
import PROPS_TYPE, { INavigation } from 'res/Props';
import { requestCreateLao } from 'network/MessageApi';
import { dispatch } from 'store';
import { getNetworkManager } from 'network';

/**
 * Manage the Launch screen: a description string, a LAO name text input, a launch LAO button,
 * and cancel button
 *
 * The Launch button does nothing
 * The cancel button clear the LAO name field and redirect to the Home screen
 *
 * TODO implement the launch button action
*/
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'space-around',
  },
  text: {
    ...Typography.base,
  } as TextStyle,
  textInput: {
    ...Typography.base,
    borderBottomWidth: 2,
  } as TextStyle,
  button: {
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.s,
  },
  viewTop: {
    justifyContent: 'flex-start',
  } as ViewStyle,
  viewBottom: {
    justifyContent: 'flex-end',
  } as ViewStyle,
});

const onButtonLaunchPress = (inputLaoName: React.RefObject<TextInput>) => {
  // FIXME old version : "inputLaoName.current?.props.value" but props is undefined here
  if (inputLaoName.current?.value) {
    requestCreateLao(inputLaoName.current.value);
  } else {
    console.error('empty lao name');
  }
};

interface IPropTypes {
  navigation: INavigation;
}

const Launch = ({ navigation }: IPropTypes) => {
  const inputLaoName = React.createRef<TextInput>();

  const cancelAction = () => {
    inputLaoName.current?.clear();
    navigation.navigate('Home');
  };

  return (
    <View style={styles.container}>
      <View style={styles.viewTop}>
        <Text style={styles.text}>{STRINGS.launch_description}</Text>
        <View style={styles.button}>
          <TextInput
            ref={inputLaoName}
            style={styles.textInput}
            placeholder={STRINGS.launch_organization_name}
          />
        </View>
      </View>
      <View style={styles.viewBottom}>
        <View style={styles.button}>
          <Button
            title={STRINGS.launch_button_launch}
            onPress={() => onButtonLaunchPress(inputLaoName)}
          />
        </View>
        { /* FIXME remove these two buttons used for testing */ }
        <View style={styles.button}>
          <Button title="--- OPEN CONNECTION to LocalMockServer.js (use 'npm run startServer') ---" onPress={() => getNetworkManager().connect('127.0.0.1')} />
        </View>
        <View style={styles.button}>
          <Button title="CLEAR STORAGE" onPress={() => dispatch({ type: 'CLEAR_STORAGE', value: {} })} />
        </View>
        <View style={styles.button}>
          <Button
            title={STRINGS.general_button_cancel}
            onPress={() => cancelAction()}
          />
        </View>
      </View>
    </View>
  );
};

Launch.propTypes = {
  navigation: PROPS_TYPE.navigation.isRequired,
};

export default Launch;