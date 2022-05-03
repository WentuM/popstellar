import { describe } from '@jest/globals';

import { mockLaoId } from '__tests__/utils';

import {
  addNotification,
  discardNotifications,
  discardAllNotifications,
  markNotificationAsRead,
  notificationReduce,
  NotificationState,
  NotificationReducerState,
  makeUnreadNotificationCountSelector,
  NOTIFICATION_REDUCER_PATH,
  getNotification,
  makeUnreadNotificationsSelector,
  makeReadNotificationsSelector,
} from '../NotificationReducer';

const n0 = {
  id: 0,
  laoId: mockLaoId,
  hasBeenRead: true,
  timestamp: 20,
  title: 'some title',
  type: 'some-type',
} as NotificationState;

const n1 = {
  id: 1,
  laoId: mockLaoId,
  hasBeenRead: false,
  timestamp: 20,
  title: 'some title',
  type: 'some-type',
} as NotificationState;

const n3 = {
  id: 3,
  laoId: mockLaoId,
  hasBeenRead: true,
  timestamp: 20,
  title: 'some title',
  type: 'some-type',
} as NotificationState;

const n11 = {
  id: 11,
  laoId: mockLaoId,
  hasBeenRead: false,
  timestamp: 20,
  title: 'some title',
  type: 'some-type',
} as NotificationState;

describe('NotificationReducer', () => {
  describe('addNotification', () => {
    it('adds notifications to the store', () => {
      const notification: Omit<NotificationState, 'id' | 'hasBeenRead'> = {
        title: 'some title',
        laoId: mockLaoId,
        timestamp: 0,
        type: 'some-type',
      };

      const newState = notificationReduce({ byLaoId: {} }, addNotification(notification));

      expect(newState.byLaoId[mockLaoId].unreadIds).toEqual([0]);
      expect(newState.byLaoId[mockLaoId].readIds).toEqual([]);
      expect(newState.byLaoId[mockLaoId].byId).toHaveProperty('0', {
        id: 0,
        hasBeenRead: false,
        ...notification,
      });
      expect(newState.byLaoId[mockLaoId].nextId).toEqual(1);
    });
  });

  describe('discardNotifications', () => {
    it('removes a notification from the store', () => {
      const newState = notificationReduce(
        {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [0, 1],
              readIds: [],
              byId: {
                0: n0,
                1: n1,
              },
              nextId: 2,
            },
          },
        } as NotificationReducerState,
        discardNotifications({ laoId: mockLaoId, notificationIds: [0] }),
      );

      expect(newState.byLaoId[mockLaoId].unreadIds).toEqual([1]);
      expect(newState.byLaoId[mockLaoId].readIds).toEqual([]);
      expect(newState.byLaoId[mockLaoId].byId).toEqual({
        1: n1,
      });
      expect(newState.byLaoId[mockLaoId].nextId).toEqual(2);
    });

    it('removes multiple notifications from the store', () => {
      const newState = notificationReduce(
        {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [0, 1],
              readIds: [3],
              byId: {
                0: n0,
                1: n1,
                3: n3,
              },
              nextId: 4,
            },
          },
        } as NotificationReducerState,
        discardNotifications({ laoId: mockLaoId, notificationIds: [0, 3] }),
      );

      expect(newState.byLaoId[mockLaoId].unreadIds).toEqual([1]);
      expect(newState.byLaoId[mockLaoId].readIds).toEqual([]);
      expect(newState.byLaoId[mockLaoId].byId).toEqual({
        1: n1,
      });
      expect(newState.byLaoId[mockLaoId].nextId).toEqual(4);
    });

    it("doesn't do anything if the id does not exist in the store", () => {
      const newState = notificationReduce(
        {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [0],
              readIds: [],
              byId: {
                0: n0,
              },
              nextId: 1,
            },
          },
        } as NotificationReducerState,
        discardNotifications({ laoId: mockLaoId, notificationIds: [1] }),
      );

      expect(newState.byLaoId[mockLaoId].unreadIds).toEqual([0]);
      expect(newState.byLaoId[mockLaoId].readIds).toEqual([]);
      expect(newState.byLaoId[mockLaoId].byId).toEqual({
        0: n0,
      });
      expect(newState.byLaoId[mockLaoId].nextId).toEqual(1);
    });
  });

  describe('discardAllNotifications', () => {
    it('removes all notifications for a given lao from the store', () => {
      const otherMockLaoId = 'some other id';

      const newState = notificationReduce(
        {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [10],
              readIds: [0],
              byId: {
                0: n0,
                10: n11,
              },
              nextId: 12,
            },
            [otherMockLaoId]: {
              unreadIds: [],
              readIds: [0],
              byId: {
                0: {
                  id: 0,
                  laoId: otherMockLaoId,
                  hasBeenRead: false,
                  timestamp: 13,
                  title: 'some title',
                  type: 'some-type',
                },
              },
              nextId: 1,
            },
          },
        } as NotificationReducerState,
        discardAllNotifications(mockLaoId),
      );

      expect(newState.byLaoId).toEqual({
        [otherMockLaoId]: {
          unreadIds: [],
          readIds: [0],
          byId: {
            0: {
              id: 0,
              laoId: otherMockLaoId,
              hasBeenRead: false,
              timestamp: 13,
              title: 'some title',
              type: 'some-type',
            },
          },
          nextId: 1,
        },
      } as NotificationReducerState['byLaoId']);
    });
  });

  describe('markNotificationAsRead', () => {
    it('sets the hasBeenRead flag to true', () => {
      const notification: NotificationState = {
        id: 0,
        laoId: mockLaoId,
        hasBeenRead: false,
        timestamp: 20,
        title: 'some title',
        type: 'some-type',
      };
      const newState = notificationReduce(
        {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [0],
              readIds: [],
              byId: {
                0: notification,
              },
              nextId: 1,
            },
          },
        } as NotificationReducerState,
        markNotificationAsRead({ laoId: mockLaoId, notificationId: 0 }),
      );

      expect(newState.byLaoId[mockLaoId].unreadIds).toEqual([]);
      expect(newState.byLaoId[mockLaoId].readIds).toEqual([0]);
      expect(newState.byLaoId[mockLaoId].byId).toHaveProperty('0', {
        ...notification,
        hasBeenRead: true,
      });
      expect(newState.byLaoId[mockLaoId].nextId).toEqual(1);
    });

    it("doesn't do anything if the id does not exist in the store", () => {
      const newState = notificationReduce(
        {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [0],
              readIds: [],
              byId: {
                0: n0,
              },
              nextId: 1,
            },
          },
        } as NotificationReducerState,
        markNotificationAsRead({ laoId: mockLaoId, notificationId: 1 }),
      );

      expect(newState.byLaoId[mockLaoId].unreadIds).toEqual([0]);
      expect(newState.byLaoId[mockLaoId].readIds).toEqual([]);
      expect(newState.byLaoId[mockLaoId].byId).toEqual({
        0: n0,
      });
      expect(newState.byLaoId[mockLaoId].nextId).toEqual(1);
    });
  });
});

describe('makeUnreadNotificationCountSelector', () => {
  it('returns the correct number of notifications', () => {
    expect(
      makeUnreadNotificationCountSelector(mockLaoId)({
        [NOTIFICATION_REDUCER_PATH]: {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [1, 11],
              readIds: [0, 3],
              byId: {
                0: n0,
                1: n1,
                3: n3,
                11: n11,
              },
              nextId: 12,
            },
          },
        } as NotificationReducerState,
      }),
    ).toEqual(2);
  });
});

describe('makeUnreadNotificationsSelector', () => {
  it('returns all read notifications', () => {
    expect(
      makeUnreadNotificationsSelector(mockLaoId)({
        [NOTIFICATION_REDUCER_PATH]: {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [1, 11],
              readIds: [0, 3],
              byId: {
                0: n0,
                1: n1,
                3: n3,
                11: n11,
              },
              nextId: 12,
            },
          },
        } as NotificationReducerState,
      }),
    ).toEqual([n1, n11]);
  });
});

describe('makeReadNotificationsSelector', () => {
  it('returns all read notifications', () => {
    expect(
      makeReadNotificationsSelector(mockLaoId)({
        [NOTIFICATION_REDUCER_PATH]: {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [1, 11],
              readIds: [0, 3],
              byId: {
                0: n0,
                1: n1,
                3: n3,
                11: n11,
              },
              nextId: 12,
            },
          },
        } as NotificationReducerState,
      }),
    ).toEqual([n0, n3]);
  });
});

describe('getNotification', () => {
  it('returns the correct notification', () => {
    expect(
      getNotification(mockLaoId, 3, {
        [NOTIFICATION_REDUCER_PATH]: {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [1, 11],
              readIds: [0, 3],
              byId: {
                0: n0,
                1: n1,
                3: n3,
                11: n11,
              },
              nextId: 12,
            },
          },
        } as NotificationReducerState,
      }),
    ).toEqual(n3);
  });

  it('returns undefined if the notification is not in the store', () => {
    expect(
      getNotification(mockLaoId, 5, {
        [NOTIFICATION_REDUCER_PATH]: {
          byLaoId: {
            [mockLaoId]: {
              unreadIds: [1, 11],
              readIds: [0, 3],
              byId: {
                0: n0,
                1: n1,
                3: n3,
                11: n11,
              },
              nextId: 12,
            },
          },
        } as NotificationReducerState,
      }),
    ).toBeUndefined();
  });
});
