import { useParams, useNavigate } from "react-router-dom";
import { useStore } from "../store/useStore";
import { format } from "date-fns";
import { ca } from "date-fns/locale";
import { ArrowLeft, Trash2, Edit } from "lucide-react";
import { calculateRangHours } from "../lib/utils";

export default function DayDetailScreen() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { dies, clients, deleteDia, deleteConcepte } = useStore();

  const dia = dies.find(d => d.id === id || d.data === id);

  if (!dia) {
    return (
      <div className="flex flex-col h-full items-center justify-center bg-slate-50 p-6">
        <p className="text-slate-500 mb-4">No hi ha dades per aquest dia.</p>
        <button onClick={() => navigate(-1)} className="text-indigo-600 font-medium">Tornar</button>
      </div>
    );
  }

  const handleDeleteDia = () => {
    if (window.confirm("Eliminar tot el dia? Aquesta acció no es pot desfer.")) {
      deleteDia(dia.id);
      navigate("/calendar");
    }
  };

  return (
    <div className="flex flex-col h-full bg-slate-50 dark:bg-slate-800 transition-colors">
      <header className="flex items-center justify-between p-4 bg-white dark:bg-slate-900 border-b dark:border-slate-700 sticky top-0 z-10 shadow-sm transition-colors">
        <button onClick={() => navigate("/calendar")} className="p-2 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full">
          <ArrowLeft />
        </button>
        <h1 className="text-lg font-medium text-slate-800 dark:text-slate-100 capitalize">
          {format(new Date(dia.data), "EEEE, d MMM yyyy", { locale: ca })}
        </h1>
        <button onClick={handleDeleteDia} className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-full" title="Eliminar Dia">
          <Trash2 size={20} />
        </button>
      </header>

      <div className="p-4 flex-1 overflow-y-auto">
        {dia.notes && (
          <div className="bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 p-3 rounded-lg mb-4 text-amber-900 dark:text-amber-200 text-sm">
            <span className="font-semibold block mb-1">Notes del dia:</span>
            {dia.notes}
          </div>
        )}

        <div className="flex justify-between items-center mb-4">
          <h2 className="font-medium text-slate-700 dark:text-slate-300">Registres ({dia.conceptes.length})</h2>
          <button 
            onClick={() => navigate(`/registre/${dia.data}`)} 
            className="text-indigo-600 dark:text-indigo-400 flex items-center text-sm font-medium hover:bg-indigo-50 dark:hover:bg-indigo-500/10 px-2 py-1 rounded-md transition"
          >
            <Edit size={16} className="mr-1" /> Editar
          </button>
        </div>

        <div className="space-y-4">
          {dia.conceptes.map(concepte => {
            const client = clients.find(c => c.id === concepte.clientId);
            const totalHours = concepte.rangsHoraris.reduce((acc, r) => acc + calculateRangHours(r.horaInici, r.horaFi), 0);
            
            // Calc earnings
            const earnings = concepte.preuFix 
              ? concepte.importFix 
              : totalHours * (concepte.preuHora || client?.preuHoraDefecte || 0);
            
            const estatColors: Record<string, string> = {
              "PENDENT": "bg-amber-100 dark:bg-amber-500/20 text-amber-800 dark:text-amber-300",
              "FACTURAT": "bg-blue-100 dark:bg-blue-500/20 text-blue-800 dark:text-blue-300",
              "COBRAT": "bg-emerald-100 dark:bg-emerald-500/20 text-emerald-800 dark:text-emerald-300"
            };

            return (
              <div key={concepte.id} className="bg-white dark:bg-slate-700 rounded-xl border border-slate-200 dark:border-slate-600 shadow-sm overflow-hidden transition-colors">
                <div className="p-4 border-b border-slate-100 dark:border-slate-600/50 flex justify-between items-start">
                  <div>
                    <h3 className="font-semibold text-lg text-slate-800 dark:text-slate-100">{concepte.nom}</h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400">{client?.nom || "Client Desconegut"}</p>
                  </div>
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${estatColors[concepte.estat] || estatColors["PENDENT"]}`}>
                    {concepte.estat}
                  </span>
                </div>
                
                <div className="px-4 py-3 bg-slate-50 dark:bg-slate-800/50 grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="block text-slate-500 dark:text-slate-400 text-xs uppercase tracking-wide">Temps</span>
                    <span className="font-medium dark:text-slate-200">{totalHours.toFixed(2)}h</span>
                  </div>
                  <div>
                    <span className="block text-slate-500 dark:text-slate-400 text-xs uppercase tracking-wide">Ingressos</span>
                    <span className="font-medium text-emerald-600 dark:text-emerald-400">{earnings.toFixed(2)} € {concepte.preuFix ? "(Fix)" : ""}</span>
                  </div>
                </div>
                
                {concepte.rangsHoraris.length > 0 && (
                  <div className="px-4 py-2 text-xs text-slate-600 dark:text-slate-400 border-t border-slate-100 dark:border-slate-600/50">
                    <span className="font-medium mr-2">Rangs:</span>
                    {concepte.rangsHoraris.map(r => `${r.horaInici} - ${r.horaFi}`).join(', ')}
                  </div>
                )}

                {concepte.despeses > 0 && (
                  <div className="px-4 py-2 text-xs bg-red-50 dark:bg-red-500/10 text-red-700 dark:text-red-300 border-t border-red-100 dark:border-red-500/20">
                    <span className="font-medium mr-1">Despeses:</span> {concepte.despeses.toFixed(2)} €
                    {concepte.despesesNotes && <span className="opacity-75"> ({concepte.despesesNotes})</span>}
                  </div>
                )}
                
                <div className="px-4 py-2 flex justify-end">
                   <button 
                     onClick={() => {
                        if (window.confirm("Eliminar aquest registre?")) {
                          deleteConcepte(dia.id, concepte.id);
                        }
                     }}
                     className="text-red-500 dark:text-red-400 flex items-center text-xs font-medium px-2 py-1 rounded hover:bg-red-50 dark:hover:bg-red-500/10"
                   >
                     <Trash2 size={14} className="mr-1" /> Esborrar
                   </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
