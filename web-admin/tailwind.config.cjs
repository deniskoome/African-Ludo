module.exports = {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          primary: '#1E2148',
          'primary-foreground': '#FFFFFF',
          secondary: '#F59E0B',
          'secondary-foreground': '#1C1300',
          surface: '#F7F7FB',
          'surface-dark': '#161836',
        },
        neutral: {
          50: '#F7F7FB',
          100: '#E1E2EC',
          200: '#C7C8D6',
          500: '#44475B',
          900: '#111426',
        },
      },
      fontFamily: {
        sans: ['"Inter"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        sm: '8px',
        md: '16px',
        lg: '24px',
      },
      spacing: {
        1: '4px',
        2: '8px',
        3: '12px',
        4: '16px',
        5: '20px',
        6: '24px',
      },
    },
  },
  plugins: [],
};
