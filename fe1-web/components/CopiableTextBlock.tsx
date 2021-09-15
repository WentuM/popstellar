import React from 'react';
import PropTypes from 'prop-types';
import {
  StyleSheet, View, ViewStyle,
} from 'react-native';
import CopyButton from 'components/CopyButton';
import TextBlock from './TextBlock';
import { Views } from '../styles';

/**
 * This is a TextBlock component which data is copiable
 * to clipboard by clicking the copy button.
 */

const styles = StyleSheet.create({
  view: {
    ...Views.base,
    flexDirection: 'row',
    zIndex: 3,
  } as ViewStyle,
});

const CopiableTextBlock = (props: IPropTypes) => {
  const { text } = props;
  const { visibility } = props;

  return (
    <View style={styles.view}>
      <CopyButton data={text} />
      <TextBlock text={text} visibility={visibility} />
    </View>
  );
};

const propTypes = {
  text: PropTypes.string,
  visibility: PropTypes.bool,
};

CopiableTextBlock.propTypes = propTypes;

CopiableTextBlock.defaultProps = {
  text: '',
  visibility: false,
};

type IPropTypes = {
  text: string,
  visibility: boolean,
};

export default CopiableTextBlock;
