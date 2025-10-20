import express from 'express';
import fcmSenderFactory from '../services/fcm_sender_factory.js';
import deviceTokenStore from '../models/device_token_store.js';

const router = express.Router();

// Accept gameplay push events and fan them out to the registered device tokens.
router.post('/api/push/send', async (req, res) => {
  const { userId, type, title, body, metadata = {} } = req.body;
  if (!userId || !type) {
    return res.status(400).json({ error: 'userId and type are required' });
  }
  const tokens = deviceTokenStore.getTokensForUser(userId);
  if (!tokens.length) {
    return res.status(404).json({ error: 'No tokens registered' });
  }
  try {
    const sender = fcmSenderFactory.getSender();
    const notification = title && body ? { title, body } : undefined;
    const results = await sender.sendToTokens(tokens, { type, title, body, ...metadata }, notification);
    res.json({ results });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
