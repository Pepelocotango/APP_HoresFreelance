import { useState, useMemo } from "react";
import { useStore } from "../store/useStore";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { startOfWeek, endOfWeek, startOfMonth, endOfMonth, subMonths, isWithinInterval, format } from "date-fns";
import { ca } from "date-fns/locale";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from "recharts";
import { FileText, Filter, Download } from "lucide-react";
import { calculateRangHours } from "../lib/utils";

type DateRange = "setmana" | "mes" | "anterior" | "tots" | "lliure";

export default function ResumScreen() {
  const { dies, clients } = useStore();
  const { t } = useTranslation();
  const navigate = useNavigate();
  
  const [range, setRange] = useState<DateRange>("mes");
  const [statusFilter, setStatusFilter] = useState("Tots");
  const [clientFilter, setClientFilter] = useState("Tots");
  
  // Estats per al DatePicker de rang lliure
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

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
    } else if (range === "lliure") {
      start = startDate;
      end = endDate;
    }

    let flatRecords: any[] = [];
    
    dies.forEach(dia => {
      const dDate = new Date(dia.data);
      // Si hi ha rang de dates configurat, filtem. Si és "tots", no filtem per data.
      if (start && end && !isWithinInterval(dDate, { start, end })) return;

      dia.conceptes.forEach(concepte => {
        if (statusFilter !== "Tots" && concepte.estat !== statusFilter) return;
        if (clientFilter !== "Tots" && concepte.clientId !== clientFilter) return;

        const client = clients.find(c => c.id === concepte.clientId);
        const hours = concepte.rangsHoraris.reduce((acc, r) => acc + calculateRangHours(r.horaInici, r.horaFi), 0);
        const earnings = concepte.preuFix ? concepte.importFix : hours * (concepte.preuHora || client?.preuHoraDefecte || 0);

        flatRecords.push({
          diaId: dia.id,
          data: dia.data,
          concepte,
          clientNom: client?.nom || t('desconegut'),
          hours,
          earnings,
          despeses: concepte.despeses
        });
      });
    });

    return flatRecords;
  }, [dies, clients, range, statusFilter, clientFilter, startDate, endDate, t]);

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
    const header = `${t('data')},${t('client')},${t('bolo')},${t('hores')},${t('ingressos')},${t('despeses')},${t('estat')}\n`;
    const body = filteredData.map(r => {
      return `${r.data},"${r.clientNom}","${r.concepte.nom}",${r.hours.toFixed(2)},${r.earnings.toFixed(2)},${r.despeses.toFixed(2)},${t(r.concepte.estat.toLowerCase())}`;
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

  const exportPDF = () => {
    const doc = new jsPDF();
    
    doc.setFontSize(18);
    doc.text(t('export_pdf_title'), 14, 22);
    
    doc.setFontSize(10);
    const rangeLabel = t(`filtre_${range}`);
    doc.text(`${t('filtres')}: ${rangeLabel} | ${t('estat')}: ${statusFilter === 'Tots' ? t('tots') : statusFilter} | ${t('client')}: ${clientFilter === 'Tots' ? t('tots') : clients.find(c=>c.id===clientFilter)?.nom}`, 14, 30);

    const tableData = filteredData.map(r => [
      format(new Date(r.data), "dd/MM/yyyy"),
      r.clientNom,
      r.concepte.nom,
      r.hours.toFixed(2) + 'h',
      r.earnings.toFixed(2) + '€',
      r.despeses.toFixed(2) + '€',
      t(r.concepte.estat.toLowerCase())
    ]);

    autoTable(doc, {
      startY: 35,
      head: [[t('data'), t('client'), t('bolo'), t('hores'), t('ingressos'), t('despeses'), t('estat')]],
      body: tableData,
      headStyles: { fillColor: [79, 70, 229] }, // Indigo color
      styles: { fontSize: 8 }
    });

    const finalY = (doc as any).lastAutoTable.finalY + 10;
    doc.setFontSize(12);
    doc.text(`${t('resum_hores')}: ${totals.hours.toFixed(1)}h`, 14, finalY);
    doc.text(`${t('resum_ingressos')}: ${totals.earnings.toFixed(2)}€`, 14, finalY + 7);
    if (totals.despeses > 0) {
       doc.text(`${t('despeses')}: ${totals.despeses.toFixed(2)}€`, 14, finalY + 14);
    }

    doc.save(`hores_export_${format(new Date(), "yyyyMMdd")}.pdf`);
  };

  const chartColors = ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

  return (
    <div className="flex flex-col h-full bg-slate-50 dark:bg-slate-800 transition-colors overflow-hidden">
      <header className="p-4 bg-white dark:bg-slate-900 border-b dark:border-slate-700 shadow-sm sticky top-0 z-10 flex justify-between items-center transition-colors">
        <h1 className="text-xl font-medium text-slate-800 dark:text-slate-100">{t('resum_informes')}</h1>
        <div className="flex gap-2">
          <button onClick={exportCSV} className="text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-500/10 p-2 text-sm font-medium rounded-lg flex items-center hover:bg-indigo-100 dark:hover:bg-indigo-500/20 transition">
            <FileText size={18} className="mr-2" /> CSV
          </button>
          <button onClick={exportPDF} className="text-white bg-indigo-600 dark:bg-indigo-500 p-2 text-sm font-medium rounded-lg flex items-center hover:bg-indigo-700 dark:hover:bg-indigo-600 transition">
            <Download size={18} className="mr-2" /> PDF
          </button>
        </div>
      </header>

      <div className="p-4 flex-1 overflow-y-auto scrollbar-thin scrollbar-thumb-slate-300 dark:scrollbar-thumb-slate-600 space-y-4">
        
        {/* 1. FILTRES (Molt més compacte) */}
        <div className="bg-white dark:bg-slate-700 p-3 rounded-xl shadow-sm border border-slate-200 dark:border-slate-600 transition-colors">
          <div className="flex items-center gap-1.5 mb-2.5 text-xs font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">
            <Filter size={14} /> {t('filtres')}
          </div>
          
          <div className="flex flex-wrap gap-1.5 mb-3">
            {[
              {id: "setmana", label: t('filtre_setmana')}, 
              {id: "mes", label: t('filtre_mes')}, 
              {id: "anterior", label: t('filtre_mes_anterior')}, 
              {id: "lliure", label: t('filtre_lliure')},
              {id: "tots", label: t('filtre_tots')}
            ].map(r => (
              <button 
                key={r.id} 
                onClick={() => setRange(r.id as DateRange)}
                className={`px-2.5 py-1 rounded-full text-[11px] font-semibold transition ${range === r.id ? 'bg-indigo-600 text-white shadow-sm' : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-600'}`}
              >
                {r.label}
              </button>
            ))}
          </div>

          {range === "lliure" && (
            <div className="flex flex-wrap gap-3 mb-3 items-center bg-slate-50 dark:bg-slate-800/50 p-2.5 rounded-lg border border-slate-100 dark:border-slate-600/50">
              <div className="flex flex-col">
                <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1">{t('inici')}</label>
                <DatePicker
                  selected={startDate}
                  onChange={(date: Date | null) => setStartDate(date)}
                  selectsStart
                  startDate={startDate}
                  endDate={endDate}
                  isClearable
                  dateFormat="dd/MM/yyyy"
                  className="border border-slate-200 dark:border-slate-600 rounded p-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-slate-800 dark:text-slate-200 w-full"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1">{t('fi')}</label>
                <DatePicker
                  selected={endDate}
                  onChange={(date: Date | null) => setEndDate(date)}
                  selectsEnd
                  startDate={startDate}
                  endDate={endDate}
                  minDate={startDate}
                  isClearable
                  dateFormat="dd/MM/yyyy"
                  className="border border-slate-200 dark:border-slate-600 rounded p-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:bg-slate-800 dark:text-slate-200 w-full"
                />
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1">{t('estat')}</label>
              <select 
                value={statusFilter} 
                onChange={e => setStatusFilter(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded p-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:text-slate-200"
              >
                <option value="Tots">{t('tots_estats')}</option>
                <option value="PENDENT">{t('pendent')}</option>
                <option value="FACTURAT">{t('facturat')}</option>
                <option value="COBRAT">{t('cobrat')}</option>
              </select>
            </div>
            <div>
              <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-1">{t('client')}</label>
              <select 
                value={clientFilter} 
                onChange={e => setClientFilter(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded p-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:text-slate-200"
              >
                <option value="Tots">{t('tots_clients')}</option>
                {clients.map(c => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
            </div>
          </div>
        </div>

        {/* 2. DESGLOSSAMENT (Ara a dalt) */}
        <div className="bg-white dark:bg-slate-700 rounded-xl shadow-sm border border-slate-200 dark:border-slate-600 overflow-hidden transition-colors">
           <h3 className="text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider p-3 border-b border-slate-100 dark:border-slate-600 bg-slate-50 dark:bg-slate-800/50">{t('desglossament')}</h3>
           <div className="divide-y divide-slate-100 dark:divide-slate-600/50 max-h-60 overflow-y-auto scrollbar-thin">
             {filteredData.length === 0 && <div className="p-4 text-center text-slate-400 text-sm">{t('no_esdeveniments')}</div>}
             {filteredData.map((item, i) => (
               <div 
                 key={i} 
                 className="p-3 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-600 transition cursor-pointer"
                 onClick={() => navigate(`/registre/${item.diaId}`)}
               >
                 <div>
                   <div className="font-medium text-xs text-slate-800 dark:text-slate-100">{format(new Date(item.data), "dd/MM/yyyy")} - {item.concepte.nom}</div>
                   <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">{item.clientNom} · {t(item.concepte.estat.toLowerCase())}</div>
                 </div>
                 <div className="text-right">
                   <div className="font-bold text-xs text-indigo-600 dark:text-indigo-400">{item.earnings.toFixed(2)} €</div>
                   <div className="text-[11px] text-slate-400 dark:text-slate-500 mt-0.5">{item.hours.toFixed(1)} h</div>
                 </div>
               </div>
             ))}
           </div>
        </div>

        {/* 3. INGRESSOS I HORES (Molt més compactes) */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-indigo-600 dark:bg-indigo-700 text-white rounded-xl p-3 shadow-sm flex flex-col justify-center relative overflow-hidden transition-colors">
             <div className="absolute top-0 right-0 p-3 opacity-10"><FileText size={32}/></div>
             <span className="text-indigo-100 text-[10px] font-bold uppercase tracking-wider">{t('ingressos')}</span>
             <span className="text-xl font-bold mt-0.5">{totals.earnings.toFixed(2)} €</span>
          </div>
          <div className="bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 rounded-xl p-3 shadow-sm flex flex-col justify-center transition-colors">
             <span className="text-slate-500 dark:text-slate-400 text-[10px] font-bold uppercase tracking-wider">{t('hores')}</span>
             <span className="text-xl font-bold text-slate-800 dark:text-slate-100 mt-0.5">{totals.hours.toFixed(1)} <span className="text-xs">h</span></span>
          </div>
        </div>
        
        {totals.despeses > 0 && (
          <div className="p-2.5 rounded-xl border border-red-200 dark:border-red-500/20 bg-red-50 dark:bg-red-500/10 flex items-center justify-between transition-colors text-xs">
            <span className="text-red-800 dark:text-red-300 font-medium">{t('despeses_justificables')}</span>
            <span className="text-red-700 dark:text-red-200 font-bold">{totals.despeses.toFixed(2)} €</span>
          </div>
        )}

        {/* 4. INGRESSOS PER CLIENT (Llista de barres de progrés nativa, molt professional i informativa) */}
        <div className="bg-white dark:bg-slate-700 p-3 rounded-xl shadow-sm border border-slate-200 dark:border-slate-600 transition-colors">
          <h3 className="text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5">{t('ingressos_per_client')}</h3>
          {chartData.length > 0 ? (
            <div className="space-y-3 max-h-48 overflow-y-auto pr-1 scrollbar-thin">
              {chartData.map((client, idx) => {
                const percentage = totals.earnings > 0 ? (client.value / totals.earnings) * 100 : 0;
                return (
                  <div key={client.name} className="space-y-1">
                    <div className="flex justify-between items-center text-xs">
                      <span className="font-medium text-slate-600 dark:text-slate-300 truncate max-w-[60%]" title={client.name}>{client.name}</span>
                      <div className="flex items-center gap-1.5 shrink-0">
                        <span className="font-bold text-slate-800 dark:text-slate-100">{client.value.toFixed(2)} €</span>
                        <span className="text-[10px] text-slate-400 dark:text-slate-500 font-medium">({percentage.toFixed(0)}%)</span>
                      </div>
                    </div>
                    <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-1.5 overflow-hidden transition-colors">
                      <div 
                        className="h-full rounded-full transition-all duration-500" 
                        style={{ 
                          width: `${percentage}%`,
                          backgroundColor: chartColors[idx % chartColors.length]
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="h-16 flex items-center justify-center text-slate-400 text-xs">
              {t('sense_dades')}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}