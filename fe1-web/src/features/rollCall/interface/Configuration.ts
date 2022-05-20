import React from 'react';
import { AnyAction, Reducer } from 'redux';

import { MessageRegistry } from 'core/network/jsonrpc/messages';
import { Hash, PopToken } from 'core/objects';
import FeatureInterface from 'core/objects/FeatureInterface';
import STRINGS from 'resources/strings';

import { RollCall } from '../objects';
import { RollCallReducerState, ROLLCALL_REDUCER_PATH } from '../reducer';
import { RollCallFeature } from './Feature';

export const ROLLCALL_FEATURE_IDENTIFIER = 'rollCall';

export interface RollCallConfiguration {
  // objects
  messageRegistry: MessageRegistry;

  /* LAO related functions */

  /**
   * Gets the lao associated to the given id. Should be used outside react components
   */
  getLaoById: (id: string) => RollCallFeature.Lao | undefined;

  /**
   * Returns the currently active lao id. Should be used inside react components
   * @returns The current lao id
   */
  useCurrentLaoId: () => Hash | undefined;

  /**
   * An action cretor that sets the last roll call for a given lao
   */
  setLaoLastRollCall: (
    laoId: Hash | string,
    rollCallId: Hash | string,
    hasToken: boolean,
  ) => AnyAction;

  /* Event related functions */

  /**
   * Creates a redux action for adding an event to the event store
   * @param laoId - The lao id where to add the event
   * @param event - The event
   * @returns A redux action causing the state change
   */
  addEvent: (laoId: Hash | string, event: RollCallFeature.EventState) => AnyAction;

  /**
   * Creates a redux action for update the stored event state
   * @param event - The event
   * @returns A redux action causing the state change
   */
  updateEvent: (event: RollCallFeature.EventState) => AnyAction;

  /**
   * Given the redux state and an event id, this function looks in the active
   * lao for an event with a matching id, creates an instance of the corresponding type
   * and returns it
   * @param id - The id of the event
   * @returns The event or undefined if none was found
   */
  getEventById: (id: Hash) => RollCallFeature.EventState | undefined;

  /**
   * Creates a selector for a two-level map from laoIds to eventIds to events
   * where all returned events have type 'eventType'
   * @param eventType The type of the events that should be returned
   * @returns A selector for a map from laoIds to a map of eventIds to events
   */
  makeEventByTypeSelector: (
    eventType: string,
  ) => (state: unknown) => Record<string, Record<string, RollCallFeature.EventState>>;

  /**
   * Deterministically generates a pop token from given lao and rollCall ids
   * @param laoId The lao id to generate a token for
   * @param rollCallId The rollCall id to generate a token for
   * @returns The generated pop token
   */
  generateToken: (laoId: Hash, rollCallId: Hash | undefined) => Promise<PopToken>;

  /**
   * Checks whether a seed is present in the wallet store
   */
  hasSeed: () => boolean;
}

/**
 * The type of the context that is provided to react rollcall components
 */
export type RollCallReactContext = Pick<
  RollCallConfiguration,
  'useCurrentLaoId' | 'makeEventByTypeSelector' | 'generateToken' | 'hasSeed'
>;

/**
 * The interface the rollcall feature exposes
 */
export interface RollCallInterface extends FeatureInterface {
  screens: {
    CreateRollCall: React.ComponentType<any>;
    RollCallOpened: React.ComponentType<any>;
  };

  eventTypes: EventType[];

  functions: {
    getRollCallById: (rollCallId: Hash | string) => RollCall | undefined;
  };

  hooks: {
    useRollCallsByLaoId: () => {
      [laoId: string]: {
        [rollCallId: string]: RollCall;
      };
    };
  };

  context: RollCallReactContext;

  reducers: {
    [ROLLCALL_REDUCER_PATH]: Reducer<RollCallReducerState>;
  };
}

interface EventType {
  eventType: string;
  navigationNames: {
    createEvent:
      | typeof STRINGS.navigation_lao_organizer_creation_meeting
      | typeof STRINGS.navigation_lao_organizer_creation_roll_call
      | typeof STRINGS.navigation_lao_organizer_creation_election;
  };
  Component: React.ComponentType<{
    eventId: string;
    isOrganizer: boolean | null | undefined;
  }>;
}
