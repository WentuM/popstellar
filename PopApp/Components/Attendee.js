import React from 'react';
import { StyleSheet, View, Text } from 'react-native';

import STRINGS from '../res/strings';
import { Views, Typography } from '../Styles';

/**
* The Attendee component
*
* Manage the Attendee screen
*/
const styles = StyleSheet.create({
  container: {
    flex: 1,
    ...Views.base,
  },
  text: {
    ...Typography.base,
  },
});

const Attendee = () => (
  <View style={styles.container}>
    <Text style={styles.text}>{STRINGS.attendee_description}</Text>
  </View>
);

export default Attendee;
