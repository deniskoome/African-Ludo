import express from 'express';
import multer from 'multer';
import secureStore from '../services/secure_config_store.js';
import config from '../config/index.js';
import fcmSenderFactory from '../services/fcm_sender_factory.js';
import deviceTokenStore from '../models/device_token_store.js';
import { mask } from '../utils/mask.js';

const upload = multer();
const router = express.Router();

// Return masked configuration so that admins know whether credentials exist without leaking them.
router.get('/admin/push-settings', async (req, res) => {
  try {
    const credentials = await secureStore.loadJson();
    res.json({
      projectId: mask(credentials?.project_id || config.FCM_PROJECT_ID || ''),
      senderVersion: config.FCM_SENDER_VERSION,
      hasCredentials: Boolean(credentials)
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Upload or update the service account JSON and persist it encrypted on disk.
router.post('/admin/push-settings', upload.single('serviceAccount'), async (req, res) => {
  try {
    const jsonPayload = req.file ? req.file.buffer.toString('utf8') : req.body.serviceAccount;
    if (!jsonPayload) {
      return res.status(400).json({ error: 'Missing service account JSON' });
    }
    await secureStore.saveJson(jsonPayload);
    res.json({ status: 'stored' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Trigger a test push to a single device token so that admins can verify connectivity.
router.post('/admin/test-push', async (req, res) => {
  const { token } = req.body;
  if (!token) {
    return res.status(400).json({ error: 'Token is required' });
  }
  try {
    const sender = fcmSenderFactory.getSender();
    const response = await sender.sendToToken(token, {
      type: 'admin_test',
      title: 'African Ludo Test',
      body: 'Test push from admin panel'
    }, {
      title: 'African Ludo Test',
      body: 'If you can read this, FCM v1 is configured correctly.'
    });
    deviceTokenStore.register('admin-test', token);
    res.json({ status: 'sent', response });
  } catch (error) {
    if (error.message === 'Invalid service account credentials') {
      return res.status(401).json({ error: error.message });
    }
    if (error.code === 'UNREGISTERED') {
      deviceTokenStore.invalidate('admin-test', token);
      return res.status(404).json({ error: 'Token invalid' });
    }
    res.status(500).json({ error: error.message });
  }
});

export default router;
