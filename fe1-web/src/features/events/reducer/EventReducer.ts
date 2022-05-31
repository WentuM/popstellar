/**
 * This error is disabled since reducers use the createSlice function, which requires the user to
 * param-reassign. Please do not disable other errors.
 */
/* eslint-disable no-param-reassign */
import { createSelector, createSlice, PayloadAction } from '@reduxjs/toolkit';

import { Hash } from 'core/objects';

import { EventState } from '../objects';

/**
 * This is the root state for the Events Reducer
 * We can store all events together since the event ids are hashes that include the laoId.
 * Assuming the hash function is collision resistant, the event ids for different laos will be different
 * (We rely for more important things on the collision resistance, so it is safe to do this here as well)
 */
export interface EventReducerState {
  // only store the list of the eventIds per lao so that they can be retrieved by lao
  byLaoId: {
    [laoId: string]: {
      /**
       * allIds stores the list of all known event IDs
       */
      allIds: string[];
    };
  };

  /**
   * byId maps an event ID to the event state itself
   */
  byId: Record<string, EventState>;
}

export const EVENT_REDUCER_PATH = 'event';

const initialState: EventReducerState = {
  byLaoId: {},
  byId: {},
};

const eventSlice = createSlice({
  name: EVENT_REDUCER_PATH,
  initialState,
  reducers: {
    // Add a Event to the list of known Events
    addEvent: {
      prepare(laoId: Hash | string, event: EventState) {
        return {
          payload: {
            laoId: laoId.valueOf(),
            event,
          },
        };
      },
      reducer(state, action: PayloadAction<{ laoId: string; event: EventState }>) {
        const { laoId, event } = action.payload;

        if (!(laoId in state.byLaoId)) {
          state.byLaoId[laoId] = {
            allIds: [],
          };
        }

        if (event.id in state.byId) {
          throw new Error(
            `Tried to store event with id ${event.id} but there already exists one with the same id`,
          );
        }

        state.byLaoId[laoId].allIds.push(event.id);
        state.byId[event.id] = event;
      },
    },

    // Update an Event in the list of known Events
    updateEvent: {
      prepare(event: EventState) {
        return {
          payload: event,
        };
      },
      reducer(state, action: PayloadAction<EventState>) {
        const event = action.payload;

        if (!(event.id in state.byId)) {
          throw new Error(`Tried to update inexistent event with id ${event.id}`);
        }

        state.byId[event.id] = event;
      },
    },

    // Remove a Event to the list of known Events
    removeEvent: {
      prepare(laoId: Hash | string, eventId: Hash | string): any {
        return { payload: { laoId: laoId.valueOf(), eventId: eventId.valueOf() } };
      },
      reducer(
        state,
        action: PayloadAction<{
          laoId: string;
          eventId: string;
        }>,
      ) {
        const { laoId, eventId } = action.payload;

        if (!(laoId in state.byLaoId)) {
          throw new Error(`Tried to remove event from inexistent lao with id ${laoId}`);
        }

        if (!(eventId in state.byId)) {
          throw new Error(`Tried to remove inexistent event with id ${eventId}`);
        }

        delete state.byId[eventId];
        state.byLaoId[laoId].allIds = state.byLaoId[laoId].allIds.filter((e) => e !== eventId);
      },
    },

    // Empty the list of known Events ("reset")
    clearAllEvents: (state) => {
      state.byLaoId = {};
      state.byId = {};
    },
  },
});

export const { addEvent, updateEvent, removeEvent, clearAllEvents } = eventSlice.actions;

export const getEventState = (state: any): EventReducerState => state[EVENT_REDUCER_PATH];

/**
 * Creates a selector that returns a list of all events for a given lao id
 * @param laoId The id of the lao the events should be retrieved for
 */
export const makeEventListSelector = (laoId: string) =>
  createSelector(
    // First input: Get all events across all LAOs
    (state) => getEventState(state).byLaoId[laoId]?.allIds,
    // First input: Get all events across all LAOs
    (state) => getEventState(state).byId,
    // Selector: returns an array of EventStates -- should it return an array of Event objects?
    (allIds: string[] | undefined, byId): EventState[] => {
      if (!allIds) {
        return [];
      }

      return allIds.map((id) => byId[id]);
    },
  );

/**
 * Creates a selector that returns a specific event
 * @param eventId - The id of the event
 * @returns The selector
 */
export const makeEventSelector = (eventId: Hash | string | undefined) => {
  const eventIdString = eventId?.valueOf() || 'undefined';

  return createSelector(
    // First input: Get all events for a given lao
    (state) => getEventState(state).byId,
    // Selector: returns the state of a given event
    (eventsById): EventState | undefined => {
      if (!eventIdString || !(eventIdString in eventsById)) {
        return undefined;
      }

      return eventsById[eventIdString];
    },
  );
};

/**
 * Gets a specific event within a lao
 * @param eventId - The id of the event
 * @param state - The redux state
 * @returns The event
 */
export const getEvent = (eventId: Hash | string | undefined, state: unknown) => {
  const eventIdString = eventId?.valueOf() || 'undefined';
  const eventState = getEventState(state);
  const eventsById = eventState.byId;

  if (!eventIdString || !(eventIdString in eventsById.byId)) {
    return undefined;
  }

  return eventsById[eventIdString];
};

/**
 * Returns all events of a certain type.
 *
 * @param eventType
 */
export const makeEventByTypeSelector = (eventType: string) =>
  createSelector(
    // First input: Get all events across all LAOs
    (state) => getEventState(state),
    // Selector: returns a map of lao ids to a map of event its to event states
    (eventMap: EventReducerState): Record<string, Record<string, EventState>> => {
      const eventByLao: Record<string, Record<string, EventState>> = {};

      Object.entries(eventMap.byLaoId).forEach(([laoId, eventList]) => {
        eventByLao[laoId] = eventList.allIds
          .map((i) => eventMap.byId[i])
          .filter((e): e is EventState => e !== undefined && e.eventType === eventType)
          .reduce((eventById, e) => {
            eventById[e.id.valueOf()] = e;
            return eventById;
          }, {} as Record<string, EventState>);
      });

      return eventByLao;
    },
  );

export const eventReduce = eventSlice.reducer;

export default {
  [EVENT_REDUCER_PATH]: eventSlice.reducer,
};