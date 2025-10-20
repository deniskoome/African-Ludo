import deviceTokenStore from '../src/models/device_token_store.js';

describe('DeviceTokenStore', () => {
  beforeEach(() => {
    deviceTokenStore.tokensByUser.clear();
  });

  it('registers and deduplicates tokens per user', () => {
    const tokens = deviceTokenStore.register('user1', 'tokenA');
    expect(tokens).toContain('tokenA');
    deviceTokenStore.register('user1', 'tokenA');
    expect(deviceTokenStore.getAllTokens()).toEqual(['tokenA']);
  });

  it('invalidates tokens', () => {
    deviceTokenStore.register('user2', 'tokenB');
    deviceTokenStore.invalidate('user2', 'tokenB');
    expect(deviceTokenStore.getAllTokens()).toHaveLength(0);
  });
});
