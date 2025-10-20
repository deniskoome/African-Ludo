import express from 'express';
import deviceTokenStore from '../models/device_token_store.js';

const router = express.Router();

// Register or update the FCM token for the authenticated user.
router.post('/api/devices', (req, res) => {
  const { userId, token } = req.body;
  if (!userId || !token) {
    return res.status(400).json({ error: 'userId and token are required' });
  }
  const tokens = deviceTokenStore.register(userId, token);
  res.json({ tokens });
});

// Remove the token when the user logs out or the device is deactivated.
router.delete('/api/devices', (req, res) => {
  const { userId, token } = req.body;
  if (!userId || !token) {
    return res.status(400).json({ error: 'userId and token are required' });
  }
  deviceTokenStore.invalidate(userId, token);
  res.json({ status: 'deleted' });
});

router.post('/api/devices/delete', (req, res) => {
  const { userId, token } = req.body;
  if (!userId || !token) {
    return res.status(400).json({ error: 'userId and token are required' });
  }
  deviceTokenStore.invalidate(userId, token);
  res.json({ status: 'deleted' });
});

export default router;
