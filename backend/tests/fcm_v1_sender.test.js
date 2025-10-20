import { jest } from '@jest/globals';

jest.unstable_mockModule('google-auth-library', () => ({
  GoogleAuth: jest.fn().mockImplementation(() => ({
    getAccessToken: jest.fn().mockResolvedValue('test-token')
  }))
}));

const axiosPost = jest.fn().mockResolvedValue({ status: 200, data: { name: 'messages/1' } });

jest.unstable_mockModule('axios', () => ({
  default: { post: axiosPost }
}));

jest.unstable_mockModule('../src/services/secure_config_store.js', () => ({
  default: { loadJson: jest.fn().mockResolvedValue({ project_id: 'demo-project' }) }
}));

const { default: fcmV1Sender } = await import('../src/services/fcm_v1_sender.js');

describe('FCM V1 sender', () => {
  beforeEach(() => {
    axiosPost.mockClear();
  });

  it('sends data to the v1 endpoint', async () => {
    const response = await fcmV1Sender.sendToToken('token', { type: 'match_found' });
    expect(response).toEqual({ name: 'messages/1' });
    expect(axiosPost).toHaveBeenCalledWith(
      'https://fcm.googleapis.com/v1/projects/demo-project/messages:send',
      expect.objectContaining({ message: expect.any(Object) }),
      expect.objectContaining({ headers: expect.any(Object) })
    );
  });
});
