import { jest } from '@jest/globals';
import request from 'supertest';

jest.unstable_mockModule('../src/services/secure_config_store.js', () => ({
  default: {
    loadJson: jest.fn().mockResolvedValue({ project_id: 'demo-project' }),
    saveJson: jest.fn().mockResolvedValue(true)
  }
}));

const sendToTokenMock = jest.fn().mockResolvedValue({ name: 'messages/1' });

jest.unstable_mockModule('../src/services/fcm_sender_factory.js', () => ({
  default: { getSender: () => ({ sendToToken: sendToTokenMock }) }
}));

const { default: app } = await import('../src/server.js');

const secureStore = (await import('../src/services/secure_config_store.js')).default;

describe('Admin push endpoints', () => {
  beforeEach(() => {
    sendToTokenMock.mockClear();
  });

  it('returns masked project id', async () => {
    const res = await request(app).get('/admin/push-settings');
    expect(res.status).toBe(200);
    expect(res.body.projectId).toContain('*');
  });

  it('uploads credentials', async () => {
    const res = await request(app)
      .post('/admin/push-settings')
      .field('serviceAccount', JSON.stringify({ foo: 'bar' }));
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('stored');
  });

  it('sends test push', async () => {
    const res = await request(app)
      .post('/admin/test-push')
      .send({ token: 'abc' });
    expect(res.status).toBe(200);
    expect(sendToTokenMock).toHaveBeenCalled();
  });
});
