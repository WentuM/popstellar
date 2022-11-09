import { createStackNavigator } from '@react-navigation/stack';
import React from 'react';
import { useSelector } from 'react-redux';

import { stackScreenOptionsWithHeader } from 'core/navigation/ScreenOptions';
import { LaoEventsParamList } from 'core/navigation/typing/LaoEventsParamList';
import STRINGS from 'resources/strings';

import { LaoHooks } from '../hooks';
import { selectIsLaoOrganizer } from '../reducer';
import { EventsScreen } from '../screens';

/**
 * Define the Organizer stack navigation
 * four different screen (OrganizerScreen, CreateEvent, RollCallScanning)
 *
 * The app are not use in the stack order, only organizer to one of the other screen
 */

const Stack = createStackNavigator<LaoEventsParamList>();

export default function EventsNavigation() {
  const screens = LaoHooks.useEventsNavigationScreens();
  const isOrganizer = useSelector(selectIsLaoOrganizer);
  const CreateEventButton = LaoHooks.useCreateEventButtonComponent();

  return (
    <Stack.Navigator screenOptions={stackScreenOptionsWithHeader}>
      <Stack.Screen
        name={STRINGS.navigation_lao_events_home}
        component={EventsScreen}
        options={{
          title: STRINGS.navigation_lao_events_home_title,
          /* do not show the back button */
          headerLeft: () => null,
          headerRight: isOrganizer ? CreateEventButton : undefined,
        }}
      />
      {screens.map(
        ({ id, title, headerTitle, headerLeft, headerRight, headerShown, Component }) => (
          <Stack.Screen
            name={id}
            key={id}
            component={Component}
            options={{
              title: title || id,
              headerTitle: headerTitle || title || id,
              headerLeft,
              headerRight,
              headerShown,
            }}
          />
        ),
      )}
    </Stack.Navigator>
  );
}
