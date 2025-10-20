export const GATEWAY_DEFINITIONS = [
  {
    name: 'paytm',
    displayName: 'PayTM',
    supportedActions: ['deposit'],
    logo: '/logos/paytm.svg',
    configFields: [
      { key: 'merchantId', label: 'Merchant ID', secret: false },
      { key: 'merchantKey', label: 'Merchant Key', secret: true }
    ]
  },
  {
    name: 'payu',
    displayName: 'PayU Money',
    supportedActions: ['deposit'],
    logo: '/logos/payu.svg',
    configFields: [
      { key: 'merchantId', label: 'Merchant ID', secret: false },
      { key: 'merchantKey', label: 'Merchant Key', secret: true }
    ]
  },
  {
    name: 'razorpay',
    displayName: 'Razorpay',
    supportedActions: ['deposit'],
    logo: '/logos/razorpay.svg',
    configFields: [
      { key: 'keyId', label: 'Key ID', secret: false },
      { key: 'keySecret', label: 'Key Secret', secret: true }
    ]
  },
  {
    name: 'mpesa',
    displayName: 'M-Pesa',
    supportedActions: ['deposit', 'withdraw'],
    logo: '/logos/mpesa.svg',
    configFields: [
      { key: 'consumerKey', label: 'Consumer Key', secret: true },
      { key: 'consumerSecret', label: 'Consumer Secret', secret: true },
      { key: 'shortcode', label: 'Shortcode', secret: false },
      { key: 'passkey', label: 'Passkey', secret: true },
      { key: 'callbackUrl', label: 'Callback URL', secret: false }
    ]
  },
  {
    name: 'mastercard',
    displayName: 'Mastercard',
    supportedActions: ['deposit'],
    logo: '/logos/mastercard.svg',
    configFields: [
      { key: 'merchantId', label: 'Merchant ID', secret: false },
      { key: 'apiKey', label: 'API Key', secret: true },
      { key: 'apiSecret', label: 'API Secret', secret: true }
    ]
  },
  {
    name: 'flutterwave',
    displayName: 'Flutterwave',
    supportedActions: ['deposit'],
    logo: '/logos/flutterwave.svg',
    configFields: [
      { key: 'publicKey', label: 'Public Key', secret: false },
      { key: 'secretKey', label: 'Secret Key', secret: true },
      { key: 'encryptionKey', label: 'Encryption Key', secret: true }
    ]
  },
  {
    name: 'paystack',
    displayName: 'Paystack',
    supportedActions: ['deposit'],
    logo: '/logos/paystack.svg',
    configFields: [
      { key: 'publicKey', label: 'Public Key', secret: false },
      { key: 'secretKey', label: 'Secret Key', secret: true }
    ]
  },
  {
    name: 'stripe',
    displayName: 'Stripe',
    supportedActions: ['deposit'],
    logo: '/logos/stripe.svg',
    configFields: [
      { key: 'publishableKey', label: 'Publishable Key', secret: false },
      { key: 'secretKey', label: 'Secret Key', secret: true },
      { key: 'webhookSecret', label: 'Webhook Secret', secret: true }
    ]
  }
];

export function getGatewayDefinition(name) {
  return GATEWAY_DEFINITIONS.find((gateway) => gateway.name === name);
}
