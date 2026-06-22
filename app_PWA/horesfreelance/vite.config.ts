import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig } from 'vite';

export default defineConfig(() => {
  // Quan s'executa dins d'Electron, la ruta base ha de ser relativa ('./')
  // per tal que l'arxiu index.html trobi els assets a dist/assets/
  const isElectron = process.env.ELECTRON === 'true';

  return {
    // IMPORTANT: base './' perquè Electron carregui l'index.html com a fitxer local
    base: isElectron ? './' : '/',
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '.'),
      },
    },
    server: {
      hmr: process.env.DISABLE_HMR !== 'true',
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});
