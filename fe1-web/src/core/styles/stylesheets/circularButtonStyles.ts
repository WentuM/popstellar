import { StyleSheet, ViewStyle } from 'react-native';

import { Color } from '../index';

const circularButtonStyles = StyleSheet.create({
  button: {
    width: 80,
    height: 80,
    backgroundColor: Color.blue,
    borderRadius: 80,
  } as ViewStyle,
  roundButton: {
    height: 65,
    width: 65,
    shadowColor: Color.gray,
    shadowOffset: {
      width: 2,
      height: 2,
    },
    borderRadius: 30,
    backgroundColor: Color.blue,
  } as ViewStyle,
});

export default circularButtonStyles;
