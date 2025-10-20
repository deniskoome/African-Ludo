import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const LOG_PATH = path.join(__dirname, '..', 'admin', 'message-telemetry.log');

// Lightweight append-only logger that captures delivery metadata for debugging and analytics.
class TelemetryLogger {
  // Append one row to the telemetry log so that support engineers can inspect delivery attempts.
  async log(entry) {
    const line = JSON.stringify({
      timestamp: new Date().toISOString(),
      ...entry
    });
    await fs.promises.mkdir(path.dirname(LOG_PATH), { recursive: true });
    await fs.promises.appendFile(LOG_PATH, `${line}\n`, 'utf8');
  }
}

export default new TelemetryLogger();
