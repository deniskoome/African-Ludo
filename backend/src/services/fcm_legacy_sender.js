import axios from 'axios';
import telemetryLogger from './telemetry_logger.js';

// Thin wrapper around the legacy HTTP API so that we can fall back if v1 credentials fail.
class FcmLegacySender {
  constructor(serverKey) {
    this.serverKey = serverKey || process.env.FCM_SERVER_KEY || '';
  }

  // Send a payload directly to the legacy endpoint. Only kept for rollback purposes.
  async sendToToken(token, data) {
    const body = {
      to: token,
      data
    };
    const response = await axios.post('https://fcm.googleapis.com/fcm/send', body, {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `key=${this.serverKey}`
      }
    });
    await telemetryLogger.log({
      target: token,
      type: data?.type,
      version: 'legacy',
      status: response.status
    });
    return response.data;
  }
}

export default new FcmLegacySender();
