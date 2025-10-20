export type Player = {
  name: string;
  email: string;
  status: 'online' | 'offline' | 'in-match';
  wallet: string;
  country: string;
};

export const players: Player[] = [
  { name: 'Amina Otieno', email: 'amina@ludo.africa', status: 'online', wallet: 'KES 3,420', country: 'Kenya' },
  { name: 'Kwame Mensah', email: 'kwame@ludo.africa', status: 'in-match', wallet: 'KES 1,210', country: 'Ghana' },
  { name: 'Zainab Bello', email: 'zainab@ludo.africa', status: 'online', wallet: 'KES 5,660', country: 'Nigeria' },
  { name: 'Peter Kamau', email: 'peter@ludo.africa', status: 'offline', wallet: 'KES 450', country: 'Kenya' },
  { name: 'Thandi Ndlovu', email: 'thandi@ludo.africa', status: 'online', wallet: 'KES 2,040', country: 'South Africa' },
  { name: 'Emeka Obi', email: 'emeka@ludo.africa', status: 'offline', wallet: 'KES 980', country: 'Nigeria' }
];

export type PaymentGateway = {
  id: string;
  name: string;
  enabled: boolean;
  description: string;
};

export const gateways: PaymentGateway[] = [
  { id: 'mpesa', name: 'M-Pesa', enabled: true, description: 'Safaricom mobile money payments' },
  { id: 'flutterwave', name: 'Flutterwave', enabled: true, description: 'Pan-African card & bank payments' },
  { id: 'razorpay', name: 'Razorpay', enabled: false, description: 'Credit card payments for diaspora players' }
];
