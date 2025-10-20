// In-memory representation of device tokens keyed by user. Replace with a persistent store in production.
class DeviceTokenStore {
  constructor() {
    this.tokensByUser = new Map();
  }

  // Register a token for a user, de-duplicating so that we do not spam devices.
  register(userId, token) {
    if (!this.tokensByUser.has(userId)) {
      this.tokensByUser.set(userId, new Set());
    }
    this.tokensByUser.get(userId).add(token);
    return Array.from(this.tokensByUser.get(userId));
  }

  // Remove a specific token when the user logs out or FCM reports it as unregistered.
  invalidate(userId, token) {
    const tokens = this.tokensByUser.get(userId);
    if (tokens) {
      tokens.delete(token);
      if (!tokens.size) {
        this.tokensByUser.delete(userId);
      }
    }
    return true;
  }

  // Return all tokens in the system. This is helpful for topic/broadcast testing.
  getAllTokens() {
    return Array.from(this.tokensByUser.values()).flatMap(set => Array.from(set));
  }

  // Fetch tokens for a specific user so game services can direct targeted pushes.
  getTokensForUser(userId) {
    return Array.from(this.tokensByUser.get(userId) || []);
  }
}

export default new DeviceTokenStore();
