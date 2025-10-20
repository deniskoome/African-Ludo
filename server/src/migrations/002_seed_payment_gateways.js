import { GATEWAY_DEFINITIONS } from '../constants/gateways.js';

export default function seedPaymentGateways(db) {
  const insert = db.prepare(`
    INSERT INTO payment_gateways (name, display_name, enabled)
    VALUES (@name, @displayName, @enabled)
    ON CONFLICT(name) DO UPDATE SET display_name=excluded.display_name;
  `);

  const transaction = db.transaction((gateways) => {
    gateways.forEach((gateway) => {
      insert.run({
        name: gateway.name,
        displayName: gateway.displayName,
        enabled: gateway.name === 'paytm' ? 1 : 0
      });
    });
  });

  transaction(GATEWAY_DEFINITIONS);
}
