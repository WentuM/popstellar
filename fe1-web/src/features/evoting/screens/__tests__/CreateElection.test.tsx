import { fireEvent, render } from '@testing-library/react-native';
import React from 'react';
import { act } from 'react-test-renderer';

import MockNavigator from '__tests__/components/MockNavigator';
import {
  mockLao,
  mockLaoId,
  messageRegistryInstance,
  mockReduxAction,
  mockKeyPair,
} from '__tests__/utils';
import FeatureContext from 'core/contexts/FeatureContext';
import { EvotingReactContext, EVOTING_FEATURE_IDENTIFIER } from 'features/evoting/interface';

import CreateElection from '../CreateElection';

const contextValue = {
  [EVOTING_FEATURE_IDENTIFIER]: {
    getCurrentLao: () => mockLao,
    useCurrentLaoId: () => mockLaoId,
    useConnectedToLao: () => true,
    useCurrentLao: () => mockLao,
    addEvent: () => mockReduxAction,
    updateEvent: () => mockReduxAction,
    getEventFromId: () => undefined,
    messageRegistry: messageRegistryInstance,
    onConfirmEventCreation: () => undefined,
    getEventById: () => undefined,
    useLaoOrganizerBackendPublicKey: () => mockKeyPair.publicKey,
  } as EvotingReactContext,
};

describe('CreateElection', () => {
  it('renders correctly when question is empty', () => {
    const { getByTestId, toJSON } = render(
      <FeatureContext.Provider value={contextValue}>
        <MockNavigator component={CreateElection} />
      </FeatureContext.Provider>,
    );

    const nameInput = getByTestId('election_name_selector');
    fireEvent.changeText(nameInput, 'myElection');
    expect(toJSON()).toMatchSnapshot();
  });

  it('renders correctly when name and question are empty', () => {
    const component = render(
      <FeatureContext.Provider value={contextValue}>
        <MockNavigator component={CreateElection} />
      </FeatureContext.Provider>,
    ).toJSON();
    expect(component).toMatchSnapshot();
  });
});
