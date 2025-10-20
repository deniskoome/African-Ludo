import express from 'express';
import db from '../db.js';
import GatewayConfigService from '../services/GatewayConfigService.js';
import { config } from '../config.js';
import { getGatewayDefinition } from '../constants/gateways.js';

const router = express.Router();
const configService = new GatewayConfigService(config.masterKey);

router.get('/active', (req, res) => {
  const rows = db.prepare('SELECT * FROM payment_gateways WHERE enabled = 1 ORDER BY display_name').all();
  const gateways = rows.map((row) => {
    const definition = getGatewayDefinition(row.name) || {};
    let decrypted = {};
    try {
      decrypted = row.config ? configService.decrypt(row.config) : {};
    } catch (error) {
      decrypted = {};
    }

    return {
      id: row.id,
      name: row.name,
      displayName: row.display_name,
      logo: definition.logo || null,
      supportedActions: definition.supportedActions || [],
      metadata: {
        configured: Object.keys(decrypted).length > 0
      }
    };
  });

  res.json({ gateways });
});

export default router;
