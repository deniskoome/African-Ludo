import config from '../config/index.js';
import fcmV1Sender from './fcm_v1_sender.js';
import fcmLegacySender from './fcm_legacy_sender.js';

// Selects which sender implementation to use based on the runtime feature flag.
class FcmSenderFactory {
  // Return the appropriate sender instance so that callers do not need to know about feature flags.
  getSender() {
    return config.FCM_SENDER_VERSION === 'legacy' ? fcmLegacySender : fcmV1Sender;
  }
}

export default new FcmSenderFactory();
