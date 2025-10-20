// Centralized configuration values for the backend messaging stack.
// Feature flags and secrets are expected to be injected through environment variables
// so that we can switch between legacy and v1 senders at runtime without redeploying.

const FCM_SENDER_VERSION = process.env.FCM_SENDER_VERSION || 'v1';
const FCM_PROJECT_ID = process.env.FCM_PROJECT_ID || '';
const ENCRYPTION_KEY = process.env.FCM_CREDENTIALS_KEY || 'change-me-in-prod';
const RETRY_ATTEMPTS = Number(process.env.FCM_MAX_RETRIES || 3);

export default {
  FCM_SENDER_VERSION,
  FCM_PROJECT_ID,
  ENCRYPTION_KEY,
  RETRY_ATTEMPTS
};
