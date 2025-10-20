import express from 'express';
import db from '../db.js';
import GatewayConfigService from '../services/GatewayConfigService.js';
import { config } from '../config.js';
import { GATEWAY_DEFINITIONS, getGatewayDefinition } from '../constants/gateways.js';
import { HttpError, wrapAsync } from '../utils/httpErrors.js';

const router = express.Router();
const configService = new GatewayConfigService(config.masterKey);

function serializeGateway(row) {
  const definition = getGatewayDefinition(row.name) || {};
  let decrypted = {};
  try {
    decrypted = row.config ? configService.decrypt(row.config) : {};
  } catch (error) {
    decrypted = { error: 'Unable to decrypt' };
  }

  return {
    id: row.id,
    name: row.name,
    displayName: row.display_name,
    enabled: Boolean(row.enabled),
    supportedActions: definition.supportedActions || [],
    logo: definition.logo || null,
    configFields: definition.configFields || [],
    config: configService.maskConfig(decrypted)
  };
}

router.get(
  '/',
  wrapAsync(async (req, res) => {
    const gateways = db.prepare('SELECT * FROM payment_gateways ORDER BY display_name').all();
    res.json({ gateways: gateways.map(serializeGateway) });
  })
);

router.post(
  '/:id/toggle',
  wrapAsync(async (req, res) => {
    const gateway = db.prepare('SELECT * FROM payment_gateways WHERE id = ?').get(req.params.id);
    if (!gateway) {
      throw new HttpError(404, 'Gateway not found');
    }
    const enabled = req.body.enabled !== undefined ? req.body.enabled : !gateway.enabled;
    db.prepare('UPDATE payment_gateways SET enabled = ?, updated_at = datetime(\'now\') WHERE id = ?').run(
      enabled ? 1 : 0,
      gateway.id
    );
    res.json({
      message: `Gateway ${enabled ? 'enabled' : 'disabled'} successfully`,
      gateway: serializeGateway({ ...gateway, enabled: enabled ? 1 : 0 })
    });
  })
);

router.post(
  '/:id/config',
  wrapAsync(async (req, res) => {
    const gateway = db.prepare('SELECT * FROM payment_gateways WHERE id = ?').get(req.params.id);
    if (!gateway) {
      throw new HttpError(404, 'Gateway not found');
    }
    const payload = req.body.config;
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
      throw new HttpError(400, 'Invalid config payload. Expecting JSON object.');
    }
    const encrypted = configService.encrypt(payload);
    db.prepare('UPDATE payment_gateways SET config = ?, updated_at = datetime(\'now\') WHERE id = ?').run(
      encrypted,
      gateway.id
    );
    res.json({ message: 'Configuration updated successfully.' });
  })
);

router.post(
  '/:id/test',
  wrapAsync(async (req, res) => {
    const gateway = db.prepare('SELECT * FROM payment_gateways WHERE id = ?').get(req.params.id);
    if (!gateway) {
      throw new HttpError(404, 'Gateway not found');
    }
    const definition = getGatewayDefinition(gateway.name);
    if (!definition) {
      throw new HttpError(400, 'Unknown gateway definition.');
    }
    const configData = configService.decrypt(gateway.config);
    const missingFields = (definition.configFields || [])
      .filter((field) => !configData[field.key])
      .map((field) => field.key);

    if (missingFields.length > 0) {
      throw new HttpError(400, `Missing configuration values: ${missingFields.join(', ')}`);
    }

    if (gateway.name === 'stripe') {
      await performStripeSmokeTest(configData);
    } else if (gateway.name === 'mpesa') {
      performMpesaConfigValidation(configData);
    }

    res.json({ message: 'Gateway connection verified successfully.' });
  })
);

function performMpesaConfigValidation(configData) {
  const required = ['consumerKey', 'consumerSecret', 'shortcode', 'passkey', 'callbackUrl'];
  const missing = required.filter((key) => !configData[key]);
  if (missing.length) {
    throw new HttpError(400, `Missing Mpesa configuration fields: ${missing.join(', ')}`);
  }
}

async function performStripeSmokeTest(configData) {
  const stripeSecret = configData.secretKey;
  if (!stripeSecret) {
    throw new HttpError(400, 'Stripe secretKey is required.');
  }
  if (config.stripeTestMode) {
    return;
  }
  try {
    const stripeModule = await import('stripe');
    const stripe = new stripeModule.default(stripeSecret);
    await stripe.balance.retrieve({});
  } catch (error) {
    throw new HttpError(400, `Stripe verification failed: ${error.message}`);
  }
}

export default router;
