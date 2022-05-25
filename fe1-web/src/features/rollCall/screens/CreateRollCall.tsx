import 'react-datepicker/dist/react-datepicker.css';

import { CompositeScreenProps } from '@react-navigation/core';
import { useNavigation } from '@react-navigation/native';
import { StackScreenProps } from '@react-navigation/stack';
import React, { useState } from 'react';
import { Platform, ScrollView, Text, View } from 'react-native';
import { useToast } from 'react-native-toast-notifications';

import {
  ConfirmModal,
  DatePicker,
  DismissModal,
  ParagraphBlock,
  TextInputLine,
  Button,
} from 'core/components';
import { onChangeEndTime, onChangeStartTime } from 'core/components/DatePicker';
import { onConfirmEventCreation } from 'core/functions/UI';
import { AppParamList } from 'core/navigation/typing/AppParamList';
import { LaoOrganizerParamList } from 'core/navigation/typing/LaoOrganizerParamList';
import { LaoParamList } from 'core/navigation/typing/LaoParamList';
import { Timestamp } from 'core/objects';
import { Typography } from 'core/styles';
import { createEventStyles as styles } from 'core/styles/stylesheets/createEventStyles';
import { FOUR_SECONDS } from 'resources/const';
import STRINGS from 'resources/strings';

import { RollCallHooks } from '../hooks';
import { requestCreateRollCall } from '../network';

const DEFAULT_ROLL_CALL_DURATION = 3600;

type NavigationProps = CompositeScreenProps<
  StackScreenProps<
    LaoOrganizerParamList,
    typeof STRINGS.navigation_lao_organizer_creation_roll_call
  >,
  CompositeScreenProps<
    StackScreenProps<LaoParamList, typeof STRINGS.navigation_lao_events>,
    StackScreenProps<AppParamList, typeof STRINGS.navigation_app_lao>
  >
>;

/**
 * Screen to create a roll-call event
 *
 * TODO Send the Roll-call event in an open state to the organization server
 *  when the confirm button is press
 */
const CreateRollCall = () => {
  const navigation = useNavigation<NavigationProps['navigation']>();
  const toast = useToast();

  const laoId = RollCallHooks.useCurrentLaoId();

  const [proposedStartTime, setProposedStartTime] = useState(Timestamp.EpochNow());
  const [proposedEndTime, setProposedEndTime] = useState(
    Timestamp.EpochNow().addSeconds(DEFAULT_ROLL_CALL_DURATION),
  );

  const [rollCallName, setRollCallName] = useState('');
  const [rollCallLocation, setRollCallLocation] = useState('');
  const [rollCallDescription, setRollCallDescription] = useState('');
  const [modalEndIsVisible, setModalEndIsVisible] = useState(false);
  const [modalStartIsVisible, setModalStartIsVisible] = useState(false);

  const buildDatePickerWeb = () => {
    const startDate = proposedStartTime.toDate();
    const endDate = proposedEndTime.toDate();

    return (
      <View style={styles.viewVertical}>
        <View style={[styles.view, styles.padding]}>
          <ParagraphBlock text={STRINGS.roll_call_create_proposed_start} />
          <DatePicker
            selected={startDate}
            onChange={(date: Date) =>
              onChangeStartTime(
                date,
                setProposedStartTime,
                setProposedEndTime,
                DEFAULT_ROLL_CALL_DURATION,
              )
            }
          />
        </View>
        <View style={[styles.view, styles.padding, styles.zIndexInitial]}>
          <ParagraphBlock text={STRINGS.roll_call_create_proposed_end} />
          <DatePicker
            selected={endDate}
            onChange={(date: Date) => onChangeEndTime(date, proposedStartTime, setProposedEndTime)}
          />
        </View>
      </View>
    );
  };

  const buttonsVisibility: boolean = rollCallName !== '' && rollCallLocation !== '';

  const createRollCall = () => {
    const description = rollCallDescription === '' ? undefined : rollCallDescription;
    requestCreateRollCall(
      laoId,
      rollCallName,
      rollCallLocation,
      proposedStartTime,
      proposedEndTime,
      description,
    )
      .then(() => {
        navigation.navigate(STRINGS.navigation_lao_organizer_home);
      })
      .catch((err) => {
        console.error('Could not create roll call, error:', err);
        toast.show(`Could not create roll call, error: ${err}`, {
          type: 'danger',
          placement: 'top',
          duration: FOUR_SECONDS,
        });
      });
  };

  return (
    <ScrollView>
      {/* see archive branches for date picker used for native apps */}
      {Platform.OS === 'web' && buildDatePickerWeb()}

      <TextInputLine
        placeholder={STRINGS.roll_call_create_name}
        onChangeText={(text: string) => {
          setRollCallName(text);
        }}
      />
      <TextInputLine
        placeholder={STRINGS.roll_call_create_location}
        onChangeText={(text: string) => {
          setRollCallLocation(text);
        }}
      />
      <TextInputLine
        placeholder={STRINGS.roll_call_create_description}
        onChangeText={(text: string) => {
          setRollCallDescription(text);
        }}
      />

      <Button
        onPress={() =>
          onConfirmEventCreation(
            proposedStartTime,
            proposedEndTime,
            createRollCall,
            setModalStartIsVisible,
            setModalEndIsVisible,
          )
        }
        disabled={!buttonsVisibility}>
        <Text style={[Typography.base, Typography.centered, Typography.negative]}>
          {STRINGS.general_button_confirm}
        </Text>
      </Button>

      <Button onPress={navigation.goBack} disabled={!buttonsVisibility}>
        <Text style={[Typography.base, Typography.centered, Typography.negative]}>
          {STRINGS.general_button_cancel}
        </Text>
      </Button>

      <DismissModal
        visibility={modalEndIsVisible}
        setVisibility={setModalEndIsVisible}
        title={STRINGS.modal_event_creation_failed}
        description={STRINGS.modal_event_ends_in_past}
      />
      <ConfirmModal
        visibility={modalStartIsVisible}
        setVisibility={setModalStartIsVisible}
        title={STRINGS.modal_event_creation_failed}
        description={STRINGS.modal_event_starts_in_past}
        onConfirmPress={() => createRollCall()}
        buttonConfirmText={STRINGS.modal_button_start_now}
        buttonCancelText={STRINGS.modal_button_go_back}
      />
    </ScrollView>
  );
};

export default CreateRollCall;
