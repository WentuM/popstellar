import * as React from 'react';
import { StyleSheet, Text, View, ViewStyle } from 'react-native';

import ScreenWrapper from 'core/components/ScreenWrapper';
import { List, Spacing, Typography } from 'core/styles';
import STRINGS from 'resources/strings';

import { UserListItem } from '../components';
import { SocialHooks } from '../hooks';

/**
 * Component that will be used to allow users to search for other users or topics.
 * For now, it is used to show all the attendees of the last roll call so that everyone can follow
 * whoever they want.
 */

const styles = StyleSheet.create({
  titleTextView: {
    marginBottom: Spacing.x1,
  } as ViewStyle,
});

const SocialSearch = () => {
  const currentLao = SocialHooks.useCurrentLao();

  if (!currentLao) {
    throw new Error('Impossible to open Social media Search if you are not connected to a LAO');
  }

  const rollCallId = currentLao.last_tokenized_roll_call_id;
  const attendees = SocialHooks.useRollCallAttendeesById(rollCallId);

  return (
    <ScreenWrapper>
      <View style={styles.titleTextView}>
        <Text style={[Typography.base, Typography.important]}>
          {STRINGS.attendees_of_last_roll_call}
        </Text>
      </View>
      <View style={List.container}>
        {attendees.map((attendee, i) => (
          <UserListItem
            key={attendee.toString()}
            laoId={currentLao.id}
            publicKey={attendee}
            isFirstItem={i === 0}
            isLastItem={i === attendees.length - 1}
          />
        ))}
      </View>
    </ScreenWrapper>
  );
};

export default SocialSearch;
