import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config({ path: path.join(__dirname, '..', '.env') });

dotenv.config();

export const config = {
  port: process.env.PORT ? parseInt(process.env.PORT, 10) : 4000,
  masterKey: process.env.PAYMENT_CONFIG_MASTER_KEY || process.env.PAYMENT_MASTER_KEY,
  stripeTestMode: process.env.STRIPE_TEST_MODE ? process.env.STRIPE_TEST_MODE === 'true' : true,
};

if (!config.masterKey) {
  console.warn('\u26a0\ufe0f  PAYMENT_CONFIG_MASTER_KEY is not set. Using development fallback key. Do not use this in production.');
  config.masterKey = 'development-only-master-key-change-me';
}
