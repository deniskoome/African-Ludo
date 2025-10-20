export default function createPaymentGatewaysTable(db) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS payment_gateways (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL UNIQUE,
      display_name TEXT NOT NULL,
      enabled INTEGER NOT NULL DEFAULT 0,
      config TEXT,
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      updated_at TEXT NOT NULL DEFAULT (datetime('now'))
    );
  `);

  db.exec(`
    CREATE TRIGGER IF NOT EXISTS trg_payment_gateways_updated_at
    AFTER UPDATE ON payment_gateways
    FOR EACH ROW
    BEGIN
      UPDATE payment_gateways
      SET updated_at = datetime('now')
      WHERE id = OLD.id;
    END;
  `);
}
