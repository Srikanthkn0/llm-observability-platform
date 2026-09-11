/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: { 950: '#0b1020', 900: '#111827', 800: '#1f2937' },
        accent: { DEFAULT: '#6366f1', soft: '#818cf8' },
      },
    },
  },
  plugins: [],
}
