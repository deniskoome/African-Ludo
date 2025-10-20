import express from 'express';
import bodyParser from 'body-parser';
import path from 'path';
import { fileURLToPath } from 'url';
import routes from './routes/index.js';
import config from './config/index.js';
import secureStore from './services/secure_config_store.js';

const app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(routes);

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
app.use('/admin', express.static(path.join(__dirname, 'admin')));

// Basic health endpoint to ensure the service is online.
app.get('/health', (req, res) => {
  res.json({ status: 'ok', senderVersion: config.FCM_SENDER_VERSION });
});

// Preload credentials on startup so operational issues surface early.
secureStore.loadJson().catch(() => {
  // Intentionally ignored; the admin panel can upload credentials later.
});

const port = process.env.PORT || 4000;
if (process.env.NODE_ENV !== 'test') {
  app.listen(port, () => {
    console.log(`Backend listening on port ${port}`);
  });
}

export default app;
