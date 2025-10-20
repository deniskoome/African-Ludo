import { jest } from '@jest/globals';
import request from 'supertest';

const registerMock = jest.fn();
const getTokensMock = jest.fn().mockReturnValue(['token-1']);

jest.unstable_mockModule('../src/models/device_token_store.js', () => ({
  default: {
    register: registerMock,
    invalidate: jest.fn(),
    getTokensForUser: getTokensMock,
    getAllTokens: jest.fn(),
    tokensByUser: new Map()
  }
}));

const sendToTokensMock = jest.fn().mockResolvedValue([{ status: 'fulfilled' }]);

jest.unstable_mockModule('../src/services/fcm_sender_factory.js', () => ({
  default: { getSender: () => ({ sendToTokens: sendToTokensMock }) }
}));

const { default: app } = await import('../src/server.js');

describe('Gameplay push route', () => {
  beforeEach(() => {
    sendToTokensMock.mockClear();
  });

  it('sends pushes to registered tokens', async () => {
    const res = await request(app)
      .post('/api/push/send')
      .send({ userId: 'user-1', type: 'match_found', title: 'Match', body: 'Join now' });
    expect(res.status).toBe(200);
    expect(sendToTokensMock).toHaveBeenCalled();
  });
});
