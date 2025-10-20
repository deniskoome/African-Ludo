import { GoogleAuth } from 'google-auth-library';
import axios from 'axios';
import config from '../config/index.js';
import secureStore from './secure_config_store.js';
import telemetryLogger from './telemetry_logger.js';
import retryQueue from './retry_queue.js';

const FCM_SCOPE = 'https://www.googleapis.com/auth/firebase.messaging';

// Adapter around the FCM HTTP v1 API that understands device token, topic and batch sends.
class FcmV1Sender {
  constructor() {
    this.authClient = null;
  }

  // Lazy-initialize the GoogleAuth client so that credentials are only loaded when required.
  async initClient() {
    if (!this.authClient) {
      const serviceAccount = await secureStore.loadJson();
      if (!serviceAccount) {
        throw new Error('Service account JSON not configured');
      }
      this.authClient = new GoogleAuth({
        credentials: serviceAccount,
        scopes: [FCM_SCOPE]
      });
      this.projectId = serviceAccount.project_id || config.FCM_PROJECT_ID;
    }
    return this.authClient;
  }

  // Build the request payload using the common metadata schema shared by the Android client.
  buildMessagePayload({ token, topic, data, notification }) {
    const message = {
      android: {
        priority: 'HIGH',
        ttl: '10s',
        notification: notification || undefined
      },
      data: {
        ...data
      }
    };
    if (token) {
      message.token = token;
    }
    if (topic) {
      message.topic = topic;
    }
    return { message };
  }

  // Send a single message to either a token or topic and handle the documented error cases.
  async sendMessage(options, attempt = 0) {
    const client = await this.initClient();
    const accessToken = await client.getAccessToken();
    const url = `https://fcm.googleapis.com/v1/projects/${this.projectId}/messages:send`;
    try {
      const response = await axios.post(url, this.buildMessagePayload(options), {
        headers: {
          Authorization: `Bearer ${accessToken.token || accessToken}`,
          'Content-Type': 'application/json'
        }
      });
      await telemetryLogger.log({
        target: options.token || options.topic,
        type: options.data?.type,
        version: 'v1',
        status: response.status
      });
      return response.data;
    } catch (error) {
      const status = error.response?.status;
      await telemetryLogger.log({
        target: options.token || options.topic,
        type: options.data?.type,
        version: 'v1',
        status: status || 'ERR',
        message: error.message
      });
      if (status === 401) {
        throw new Error('Invalid service account credentials');
      }
      if (status === 404 || status === 410) {
        const err = new Error('Unregistered device token');
        err.code = 'UNREGISTERED';
        throw err;
      }
      if (status >= 500 && attempt < config.RETRY_ATTEMPTS) {
        retryQueue.enqueue(() => this.sendMessage(options, attempt + 1), attempt);
        return { enqueued: true };
      }
      throw error;
    }
  }

  // Convenience helper to send to a single device token.
  async sendToToken(token, data, notification) {
    return this.sendMessage({ token, data, notification });
  }

  // Convenience helper to broadcast to a topic such as "match_found".
  async sendToTopic(topic, data, notification) {
    return this.sendMessage({ topic, data, notification });
  }

  // Fan out the payload to multiple registration tokens using Promise.allSettled.
  async sendToTokens(tokens, data, notification) {
    const results = await Promise.allSettled(
      tokens.map(token => this.sendToToken(token, data, notification))
    );
    return results;
  }
}

export default new FcmV1Sender();
