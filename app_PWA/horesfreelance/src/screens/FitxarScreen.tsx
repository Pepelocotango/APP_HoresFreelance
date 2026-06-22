import { useEffect, useState } from "react";
import { format } from "date-fns";
import { ca } from "date-fns/locale";
import { useStore } from "../store/useStore";
import { Play, Square, Sun, Moon } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function FitxarScreen() {
  const { activeClockIn, startClockIn, stopClockIn, clients, theme, setTheme } = useStore();
  const [now, setNow] = useState(new Date());
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const handleToggle = () => {
    if (activeClockIn) {
      if (clients.length === 0) {
        alert("Atenció: Crearàs el bolo, però no tens clients. Pots afegir-lo editant el registre.");
      }
      const initialClient = clients.length > 0 ? clients[0].id : undefined;
      const diaId = stopClockIn(initialClient);
      if (diaId) navigate(`/registre/${diaId}`);
    } else {
      startClockIn();
    }
  };

  const isActive = !!activeClockIn;

  return (
    <div className="flex flex-col items-center p-6 h-full justify-center bg-slate-50 dark:bg-slate-800 transition-colors duration-300">
      <button 
        onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')} 
        className="absolute top-6 right-6 p-2 rounded-full bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-300 dark:hover:bg-slate-600 transition"
      >
        {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
      </button>

      <div className="text-center mb-16">
        <h1 className="text-4xl font-light text-slate-800 dark:text-slate-200 tracking-tight capitalize">
          {format(now, "EEEE", { locale: ca })}
        </h1>
        <p className="text-xl text-slate-500 dark:text-slate-400 mt-2">
          {format(now, "d 'de' MMMM 'de' yyyy", { locale: ca })}
        </p>
        <p className="text-6xl font-medium mt-6 text-slate-900 dark:text-slate-100 tabular-nums tracking-tighter">
          {format(now, "HH:mm")}
        </p>
      </div>

      <div className="relative">
        {isActive && (
          <div className="absolute inset-0 bg-red-100 rounded-full animate-ping opacity-75"></div>
        )}
        
        <button
          onClick={handleToggle}
          className={`relative z-10 flex flex-col items-center justify-center w-64 h-64 rounded-full shadow-xl transition-all active:scale-95 ${
            isActive
              ? "bg-red-500 hover:bg-red-600 text-white shadow-red-200"
              : "bg-indigo-600 hover:bg-indigo-700 text-white shadow-indigo-200"
          }`}
        >
          {isActive ? (
            <>
              <Square size={48} className="mb-4" fill="currentColor" />
              <span className="text-2xl font-semibold tracking-wider">STOP</span>
              <span className="text-sm mt-2 opacity-80">Finalitzar Sessió</span>
            </>
          ) : (
            <>
              <Play size={48} className="mb-4 ml-2" fill="currentColor" />
              <span className="text-2xl font-semibold tracking-wider">START</span>
              <span className="text-sm mt-2 opacity-80">Iniciar Torn</span>
            </>
          )}
        </button>
      </div>
      
      {isActive && activeClockIn && (
        <div className="mt-12 text-slate-500 dark:text-slate-400 animate-pulse">
          Sessió iniciada a les {format(new Date(activeClockIn), "HH:mm")}
        </div>
      )}
    </div>
  );
}
