const { app, BrowserWindow, shell } = require('electron');
const path = require('path');
const fs = require('fs');

// Desactiva l'acceleració de hardware si cal (útil per a CI/CD)
if (process.env.ELECTRON_DISABLE_GPU) {
  app.disableHardwareAcceleration();
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1024,
    height: 768,
    minWidth: 375,
    minHeight: 600,
    title: 'HoresFreelance',
    // Icona: usa la que existeixi segons la plataforma
    icon: getIconPath(),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      // Permet localStorage (Zustand persist ho necessita)
      partition: 'persist:horesfreelance',
    },
    // Arrenca amb la finestra oculta fins que estigui llesta (evita flash blanc)
    show: false,
  });

  // Carrega el build de Vite (dist/index.html)
  const indexPath = path.join(__dirname, '..', 'dist', 'index.html');
  win.loadFile(indexPath);

  // Mostra la finestra quan estigui completament carregada
  win.once('ready-to-show', () => {
    win.show();
  });

  // Obre els enllaços externs al navegador del sistema, no dins d'Electron
  win.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });
}

function getIconPath() {
  const base = path.join(__dirname, '..', 'public');
  if (process.platform === 'win32') {
    const ico = path.join(base, 'icon.ico');
    return fs.existsSync(ico) ? ico : undefined;
  }
  if (process.platform === 'darwin') {
    const icns = path.join(base, 'icon.icns');
    return fs.existsSync(icns) ? icns : undefined;
  }
  // Linux: PNG
  const png = path.join(base, 'icon.png');
  return fs.existsSync(png) ? png : undefined;
}

app.whenReady().then(() => {
  createWindow();

  // macOS: torna a crear la finestra si es clica la icona del Dock sense finestres obertes
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

// Tanca l'app quan es tanquen totes les finestres (excepte macOS)
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
