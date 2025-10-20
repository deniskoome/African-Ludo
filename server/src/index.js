import express from 'express';
import helmet from 'helmet';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import { config } from './config.js';
import './migrate.js';
import adminGatewaysRouter from './routes/adminGateways.js';
import publicGatewaysRouter from './routes/publicGateways.js';
import { HttpError } from './utils/httpErrors.js';
import stripePaymentsRouter from './routes/stripePayments.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '2mb' }));
app.use(express.urlencoded({ extended: true }));

app.use('/logos', express.static(path.join(__dirname, '..', 'public', 'logos')));
app.use('/admin', express.static(path.join(__dirname, '..', 'public', 'admin')));

app.use('/admin/payment-gateways', adminGatewaysRouter);
app.use('/gateways', publicGatewaysRouter);
app.use('/', stripePaymentsRouter);

app.use((err, req, res, next) => {
  if (err instanceof HttpError) {
    return res.status(err.status).json({ message: err.message });
  }
  console.error(err);
  return res.status(500).json({ message: 'Unexpected server error' });
});

app.listen(config.port, () => {
  console.log(`African Ludo backend listening on port ${config.port}`);
});
