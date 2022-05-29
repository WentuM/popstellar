import PropTypes from 'prop-types';
import React, { FunctionComponent, useMemo } from 'react';
import { View } from 'react-native';
import { ListItem } from 'react-native-elements';
import { useSelector } from 'react-redux';
import ReactTimeago from 'react-timeago';

import ElectionIcon from 'core/components/icons/ElectionIcon';
import { Color, Icon, List, Typography } from 'core/styles';
import STRINGS from 'resources/strings';

import { EvotingInterface } from '../interface';
import { Election, ElectionStatus } from '../objects';
import { makeElectionSelector } from '../reducer';

const Subtitle = ({ election }: { election: Election }) => {
  if (election.electionStatus === ElectionStatus.NOT_STARTED) {
    return (
      <ListItem.Subtitle>
        {STRINGS.general_starting_at} <ReactTimeago date={election.start.valueOf() * 1000} />
      </ListItem.Subtitle>
    );
  }

  if (election.electionStatus === ElectionStatus.OPENED) {
    return (
      <ListItem.Subtitle>
        {STRINGS.general_ongoing}, {STRINGS.general_ending_at}{' '}
        <ReactTimeago date={election.end.valueOf() * 1000} />
      </ListItem.Subtitle>
    );
  }

  return <ListItem.Subtitle>{STRINGS.general_closed}</ListItem.Subtitle>;
};

const ElectionListItem = (props: IPropTypes) => {
  const { eventId: electionId } = props;

  const selectElection = useMemo(() => makeElectionSelector(electionId), [electionId]);
  const election = useSelector(selectElection);

  if (!election) {
    throw new Error(`Could not find an election with id ${electionId}`);
  }

  return (
    <>
      <View style={List.icon}>
        <ElectionIcon color={Color.primary} size={Icon.size} />
      </View>
      <ListItem.Content>
        <ListItem.Title style={Typography.base}>{election.name}</ListItem.Title>
        <Subtitle election={election} />
      </ListItem.Content>
      <ListItem.Chevron />
    </>
  );
};

const propTypes = {
  eventId: PropTypes.string.isRequired,
};
ElectionListItem.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default ElectionListItem;

export const ElectionEventType: EvotingInterface['eventTypes']['0'] = {
  eventType: Election.EVENT_TYPE,
  eventName: STRINGS.election_event_name,
  navigationNames: {
    createEvent: STRINGS.navigation_lao_events_create_election,
    screenSingle: STRINGS.navigation_lao_events_view_single_election,
  },
  ListItemComponent: ElectionListItem as FunctionComponent<{
    eventId: string;
    isOrganizer: boolean | null | undefined;
  }>,
};
