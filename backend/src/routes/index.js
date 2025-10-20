import express from 'express';
import adminPushRoutes from './admin_push.js';
import deviceRoutes from './device_routes.js';
import pushRoutes from './push_routes.js';

const router = express.Router();

router.use(adminPushRoutes);
router.use(deviceRoutes);
router.use(pushRoutes);

export default router;
