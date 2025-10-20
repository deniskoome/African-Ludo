import db from './db.js';
import createPaymentGatewaysTable from './migrations/001_create_payment_gateways.js';
import seedPaymentGateways from './migrations/002_seed_payment_gateways.js';

function runMigrations() {
  createPaymentGatewaysTable(db);
  seedPaymentGateways(db);
}

runMigrations();

console.log('Migrations executed successfully.');
