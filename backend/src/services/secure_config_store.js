import crypto from 'crypto';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import config from '../config/index.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const STORE_PATH = path.join(__dirname, '..', 'admin', 'secure-store.json');

// Simple AES-256-GCM wrapper around an on-disk JSON blob. In production this would be swapped
// with a managed secret manager or database column with envelope encryption.
class SecureConfigStore {
  constructor() {
    this.algorithm = 'aes-256-gcm';
    this.key = crypto.createHash('sha256').update(config.ENCRYPTION_KEY).digest();
  }

  // Persist an encrypted JSON payload to disk so that service restarts retain credentials.
  async saveJson(json) {
    const iv = crypto.randomBytes(12);
    const cipher = crypto.createCipheriv(this.algorithm, this.key, iv);
    const jsonString = typeof json === 'string' ? json : JSON.stringify(json);
    const encrypted = Buffer.concat([cipher.update(jsonString, 'utf8'), cipher.final()]);
    const authTag = cipher.getAuthTag();
    const payload = {
      iv: iv.toString('base64'),
      data: encrypted.toString('base64'),
      tag: authTag.toString('base64')
    };
    await fs.promises.mkdir(path.dirname(STORE_PATH), { recursive: true });
    await fs.promises.writeFile(STORE_PATH, JSON.stringify(payload));
    return true;
  }

  // Read and decrypt the service account JSON. Returns null when no credentials are stored yet.
  async loadJson() {
    try {
      const content = await fs.promises.readFile(STORE_PATH, 'utf8');
      const payload = JSON.parse(content);
      const decipher = crypto.createDecipheriv(
        this.algorithm,
        this.key,
        Buffer.from(payload.iv, 'base64')
      );
      decipher.setAuthTag(Buffer.from(payload.tag, 'base64'));
      const decrypted = Buffer.concat([
        decipher.update(Buffer.from(payload.data, 'base64')),
        decipher.final()
      ]);
      return JSON.parse(decrypted.toString('utf8'));
    } catch (error) {
      if (error.code === 'ENOENT') {
        return null;
      }
      throw error;
    }
  }
}

export default new SecureConfigStore();
