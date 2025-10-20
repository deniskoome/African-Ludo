// In-memory exponential backoff queue used to retry transient FCM failures. For production
// workloads this would be replaced with Redis or a message broker.
class RetryQueue {
  constructor() {
    this.queue = [];
  }

  // Schedule a retry attempt with exponential backoff so that the API is not overwhelmed.
  enqueue(task, attempt) {
    const delay = Math.pow(2, attempt) * 1000;
    const timeout = setTimeout(async () => {
      try {
        await task();
      } finally {
        this.queue = this.queue.filter(entry => entry.timeout !== timeout);
      }
    }, delay);
    this.queue.push({ timeout, attempt });
  }

  // Allow test suites to inspect queue length and clear scheduled retries.
  clear() {
    this.queue.forEach(entry => clearTimeout(entry.timeout));
    this.queue = [];
  }
}

export default new RetryQueue();
