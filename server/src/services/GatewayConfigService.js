import crypto from 'crypto';

function ensureObject(input) {
  if (!input || typeof input !== 'object' || Array.isArray(input)) {
    throw new Error('Gateway config must be a non-null object.');
  }
}

export default class GatewayConfigService {
  constructor(masterKey) {
    this.key = crypto.createHash('sha256').update(masterKey).digest();
  }

  encrypt(config) {
    ensureObject(config);
    const iv = crypto.randomBytes(12);
    const cipher = crypto.createCipheriv('aes-256-gcm', this.key, iv);
    const json = JSON.stringify(config);
    const encrypted = Buffer.concat([cipher.update(json, 'utf8'), cipher.final()]);
    const authTag = cipher.getAuthTag();
    return `${iv.toString('base64')}:${encrypted.toString('base64')}:${authTag.toString('base64')}`;
  }

  decrypt(payload) {
    if (!payload) {
      return {};
    }
    const [ivB64, dataB64, tagB64] = payload.split(':');
    if (!ivB64 || !dataB64 || !tagB64) {
      throw new Error('Invalid encrypted gateway config payload');
    }
    const decipher = crypto.createDecipheriv('aes-256-gcm', this.key, Buffer.from(ivB64, 'base64'));
    decipher.setAuthTag(Buffer.from(tagB64, 'base64'));
    const decrypted = Buffer.concat([
      decipher.update(Buffer.from(dataB64, 'base64')),
      decipher.final()
    ]);
    return JSON.parse(decrypted.toString('utf8'));
  }

  maskConfig(config) {
    return Object.keys(config || {}).reduce((acc, key) => {
      acc[key] = config[key] ? '********' : '';
      return acc;
    }, {});
  }
}
