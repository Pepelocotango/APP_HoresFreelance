import { useState, useMemo } from "react";
import { useStore } from "../store/useStore";
import { startOfWeek, endOfWeek, startOfMonth, endOfMonth, subMonths, isWithinInterval, format } from "date-fns";
import { ca } from "date-fns/locale";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from "recharts";
import { FileText, Filter } from "lucide-react";
import { calculateRangHours } from "../lib/utils";

type DateRange = "setmana" | "mes" | "anterior" | "tots";

export default function ResumScreen() {
  const { dies, clients } = useStore();
  const [range, setRange] = useState<DateRange>("mes");
  const [statusFilter, setStatusFilter] = useState("Tots");
  const [clientFilter, setClientFilter] = useState("Tots");

  const filteredData = useMemo(() => {
    let start: Date | null = null;
    let end: Date | null = null;
    const now = new Date();

    if (range === "setmana") {
      start = startOfWeek(now, { weekStartsOn: 1 });
      end = endOfWeek(now, { weekStartsOn: 1 });
    } else if (range === "mes") {
      start = startOfMonth(now);
      end = endOfMonth(now);
    } else if (range === "anterior") {
      const lastMonth = subMonths(now, 1);
      start = startOfMonth(lastMonth);
      end = endOfMonth(lastMonth);
    }

    let flatRecords: any[] = [];
    
    dies.forEach(dia => {
      const dDate = new Date(dia.data);
      if (start && end && !isWithinInterval(dDate, { start, end })) return;

      dia.conceptes.forEach(concepte => {
        if (statusFilter !== "Tots" && concepte.estatFacturacio !== statusFilter) return;
        if (clientFilter !== "Tots" && concepte.clientId !== clientFilter) return;

        const client = clients.find(c => c.id === concepte.clientId);
        const hours = concepte.rangs.reduce((acc, r) => acc + calculateRangHours(r.inici, r.fi), 0);
        const earnings = concepte.preuFix ? concepte.importFix : hours * (client?.tarifaHoraria || 0);

        flatRecords.push({
          diaId: dia.id,
          data: dia.data,
          concepte,
          clientNom: client?.nom || "Desconegut",
          hours,
          earnings,
          despeses: concepte.importDespeses
        });
      });
    });

    return flatRecords;
  }, [dies, clients, range, statusFilter, clientFilter]);

  const totals = useMemo(() => {
    return filteredData.reduce((acc, r) => {
      acc.hours += r.hours;
      acc.earnings += r.earnings;
      acc.despeses += r.despeses;
      return acc;
    }, { hours: 0, earnings: 0, despeses: 0 });
  }, [filteredData]);

  const chartData = useMemo(() => {
    const acc: Record<string, number> = {};
    filteredData.forEach(r => {
      acc[r.clientNom] = (acc[r.clientNom] || 0) + r.earnings;
    });
    return Object.entries(acc).map(([name, value]) => ({ name, value })).sort((a,b) => b.value - a.value);
  }, [filteredData]);

  const exportCSV = () => {
    const header = "Data,Client,Bolo,Hores,Ingressos,Despeses,Estat\n";
    const body = filteredData.map(r => {
      return `${r.data},"${r.clientNom}","${r.concepte.nom}",${r.hours.toFixed(2)},${r.earnings.toFixed(2)},${r.despeses.toFixed(2)},${r.concepte.estatFacturacio}`;
    }).join("\n");
    
    const blob = new Blob([header + body], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", `hores_export_${format(new Date(), "yyyyMMdd")}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const chartColors = ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

  return (
    <div className="flex flex-col h-full bg-slate-50 dark:bg-slate-800 transition-colors">
      <header className="p-4 bg-white dark:bg-slate-900 border-b dark:border-slate-700 shadow-sm sticky top-0 z-10 flex justify-between items-center transition-colors">
        <h1 className="text-xl font-medium text-slate-800 dark:text-slate-100">Resum i Informes</h1>
        <button onClick={exportCSV} className="text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-500/10 p-2 text-sm font-medium rounded-lg flex items-center hover:bg-indigo-100 dark:hover:bg-indigo-500/20 transition">
          <FileText size={18} className="mr-2" /> Exportar CSV
        </button>
      </header>

      <div className="p-4 flex-1 overflow-y-auto">
        <div className="bg-white dark:bg-slate-700 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-600 mb-6 transition-colors">
          <div className="flex items-center gap-2 mb-4 text-sm font-medium text-slate-700 dark:text-slate-300">
            <Filter size={16} /> Filtres
          </div>
          
          <div className="flex flex-wrap gap-2 mb-4">
            {[{id: "setmana", label: "Aquesta Setmana"}, {id: "mes", label: "Aquest Mes"}, {id: "anterior", label: "Mes Anterior"}, {id: "tots", label: "Tots"}].map(r => (
              <button 
                key={r.id} 
                onClick={() => setRange(r.id as DateRange)}
                className={`px-3 py-1.5 rounded-full text-xs font-semibold transition ${range === r.id ? 'bg-indigo-600 text-white shadow-md' : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-600'}`}
              >
                {r.label}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Estat</label>
              <select 
                value={statusFilter} 
                onChange={e => setStatusFilter(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded p-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:text-slate-200"
              >
                <option value="Tots">Tots els estats</option>
                <option value="Pendent">Pendent</option>
                <option value="Facturat">Facturat</option>
                <option value="Cobrat">Cobrat</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Client</label>
              <select 
                value={clientFilter} 
                onChange={e => setClientFilter(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded p-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:text-slate-200"
              >
                <option value="Tots">Tots els clients</option>
                {clients.map(c => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="bg-indigo-600 dark:bg-indigo-700 text-white rounded-xl p-4 shadow-md flex flex-col justify-center relative overflow-hidden transition-colors">
             <div className="absolute top-0 right-0 p-4 opacity-10"><FileText size={48}/></div>
             <span className="text-indigo-100 text-sm font-medium uppercase tracking-wider">Ingressos</span>
             <span className="text-3xl font-bold mt-1">{totals.earnings.toFixed(2)} €</span>
          </div>
          <div className="bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-xl p-4 shadow-sm flex flex-col justify-center transition-colors">
             <span className="text-slate-500 dark:text-slate-400 text-sm font-medium uppercase tracking-wider">Hores</span>
             <span className="text-3xl font-bold text-slate-800 dark:text-slate-100 mt-1">{totals.hours.toFixed(1)} <span className="text-lg">h</span></span>
          </div>
        </div>
        
        {totals.despeses > 0 && (
          <div className="mb-6 p-4 rounded-xl border border-red-200 dark:border-red-500/20 bg-red-50 dark:bg-red-500/10 flex items-center justify-between transition-colors">
            <span className="text-red-800 dark:text-red-300 font-medium text-sm">Totals Despeses Justificables</span>
            <span className="text-red-700 dark:text-red-200 font-bold">{totals.despeses.toFixed(2)} €</span>
          </div>
        )}

        <div className="bg-white dark:bg-slate-700 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-600 mb-6 transition-colors">
          <h3 className="text-sm font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-4">Ingressos per Client</h3>
          {chartData.length > 0 ? (
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} layout="vertical" margin={{ top: 0, right: 0, left: 0, bottom: 0 }}>
                  <XAxis type="number" hide />
                  <YAxis dataKey="name" type="category" width={80} tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
                  <Tooltip cursor={{fill: '#f1f5f9'}} formatter={(value: number) => [`${value.toFixed(2)} €`, 'Ingressos']} />
                  <Bar dataKey="value" radius={[0, 4, 4, 0]} barSize={20}>
                    {chartData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={chartColors[index % chartColors.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-32 flex items-center justify-center text-slate-400 text-sm">
              Sense dades per analitzar
            </div>
          )}
        </div>

        <div className="bg-white dark:bg-slate-700 rounded-xl shadow-sm border border-slate-200 dark:border-slate-600 overflow-hidden transition-colors">
           <h3 className="text-sm font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider p-4 border-b border-slate-100 dark:border-slate-600 bg-slate-50 dark:bg-slate-800/50">Desglossament</h3>
           <div className="divide-y divide-slate-100 dark:divide-slate-600/50">
             {filteredData.length === 0 && <div className="p-6 text-center text-slate-400 text-sm">No s'han trobat esdeveniments.</div>}
             {filteredData.map((item, i) => (
               <div key={i} className="p-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-600 transition">
                 <div>
                   <div className="font-medium text-sm text-slate-800 dark:text-slate-100">{format(new Date(item.data), "dd/MM/yyyy")} - {item.concepte.nom}</div>
                   <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{item.clientNom} · {item.concepte.estatFacturacio}</div>
                 </div>
                 <div className="text-right">
                   <div className="font-bold text-sm text-indigo-600 dark:text-indigo-400">{item.earnings.toFixed(2)} €</div>
                   <div className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">{item.hours.toFixed(1)} h</div>
                 </div>
               </div>
             ))}
           </div>
        </div>

      </div>
    </div>
  );
}
