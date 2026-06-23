import { useNavigate } from "react-router-dom";
import { useStore } from "../store/useStore";
import { Database, FileUp } from "lucide-react";

export default function DatabaseSelectorScreen() {
  const { loadBackup } = useStore();
  const navigate = useNavigate();

  const handleFileSelect = async () => {
    try {
      // @ts-expect-error - showOpenFilePicker està disponible a navegadors moderns i Electron
      const [handle] = await window.showOpenFilePicker({
        types: [{ description: 'JSON Database', accept: { 'application/json': ['.json'] } }],
        multiple: false
      });
      const file = await handle.getFile();
      const content = await file.text();
      loadBackup(content);
      navigate("/calendar");
    } catch (err) {
      console.error("Error selecting file:", err);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center h-screen bg-slate-50 dark:bg-slate-900 p-6">
      <div className="bg-white dark:bg-slate-800 p-8 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-700 max-w-md w-full text-center">
        <Database size={48} className="mx-auto text-indigo-600 dark:text-indigo-400 mb-6" />
        <h1 className="text-2xl font-bold text-slate-800 dark:text-slate-100 mb-2">Benvingut a HoresFreelance</h1>
        <p className="text-slate-500 dark:text-slate-400 mb-8">Selecciona un fitxer de base de dades JSON per començar a treballar.</p>
        
        <button 
          onClick={handleFileSelect}
          className="w-full flex items-center justify-center gap-3 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 px-6 rounded-xl transition"
        >
          <FileUp size={20} /> Carregar Base de Dades
        </button>
      </div>
    </div>
  );
}
