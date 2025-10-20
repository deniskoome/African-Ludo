import express from 'express';
import Stripe from 'stripe';
import db from '../db.js';
import GatewayConfigService from '../services/GatewayConfigService.js';
import { config } from '../config.js';
import { HttpError, wrapAsync } from '../utils/httpErrors.js';

const router = express.Router();
const configService = new GatewayConfigService(config.masterKey);

function getStripeGateway() {
  const row = db.prepare("SELECT * FROM payment_gateways WHERE name = 'stripe'").get();
  if (!row) {
    throw new HttpError(404, 'Stripe gateway is not registered');
  }
  if (!row.enabled) {
    throw new HttpError(400, 'Stripe gateway is disabled');
  }
  return row;
}

router.post(
  '/payments/stripe/intent',
  wrapAsync(async (req, res) => {
    const gateway = getStripeGateway();
    const decrypted = configService.decrypt(gateway.config);
    const publishableKey = decrypted.publishableKey;
    const secretKey = decrypted.secretKey;
    const currency = (req.body.currency || 'KES').toLowerCase();
    const amount = parseInt(Number(req.body.amount) * 100, 10);

    if (!amount || amount <= 0) {
      throw new HttpError(400, 'A positive amount is required');
    }
    if (!publishableKey || !secretKey) {
      throw new HttpError(400, 'Stripe keys are missing');
    }

    if (config.stripeTestMode) {
      return res.json({
        clientSecret: `test_client_secret_${Date.now()}`,
        publishableKey,
        currency
      });
    }

    const stripe = new Stripe(secretKey);
    const intent = await stripe.paymentIntents.create({
      amount,
      currency,
      automatic_payment_methods: { enabled: true }
    });

    res.json({ clientSecret: intent.client_secret, publishableKey, currency });
  })
);

export default router;
