import React, { useState, useRef } from "react";
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth, isToday, addMonths, subMonths, getDay } from "date-fns";
import { ca } from "date-fns/locale";
import { useStore } from "../store/useStore";
import { useNavigate } from "react-router-dom";
import { ChevronLeft, ChevronRight, Save, Upload, Plus } from "lucide-react";
import { cn } from "../lib/utils";

export default function CalendarScreen() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const { dies, loadBackup } = useStore();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const prevMonth = () => setCurrentDate(subMonths(currentDate, 1));
  const nextMonth = () => setCurrentDate(addMonths(currentDate, 1));

  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const daysInMonth = eachDayOfInterval({ start: monthStart, end: monthEnd });

  // Padded days for grid
  const startDayOfWeek = getDay(monthStart);
  const paddedDays = Array.from({ length: startDayOfWeek === 0 ? 6 : startDayOfWeek - 1 }).fill(null);

  const handleBackup = () => {
    const data = window.localStorage.getItem("hores-freelance-storage");
    if (!data) return alert("Cap dada per exportar.");
    const blob = new Blob([data], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `hores_backup_${format(new Date(), "yyyyMMdd")}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleRestore = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (evt) => {
        const text = evt.target?.result as string;
        if (text) {
           if(window.confirm("Això sobreescriurà les dades actuals. Estàs segur?")) {
             loadBackup(text);
           }
        }
      };
      reader.readAsText(file);
    }
  };

  const dayHasRecords = (dateStr: string) => {
    const d = dies.find(day => day.data === dateStr);
    return d && d.conceptes.length > 0;
  };

  return (
    <div className="flex flex-col h-full bg-white dark:bg-slate-800 transition-colors">
      <header className="flex items-center justify-between p-4 bg-indigo-600 dark:bg-indigo-900 text-white shadow-md">
        <h1 className="text-xl font-medium">Calendari</h1>
        <div className="flex gap-2">
          <button onClick={handleBackup} className="p-2 hover:bg-white/20 rounded-full transition" title="Còpia de seguretat">
            <Save size={20} />
          </button>
          <button onClick={() => fileInputRef.current?.click()} className="p-2 hover:bg-white/20 rounded-full transition" title="Restaura còpia">
            <Upload size={20} />
            <input type="file" accept=".json" className="hidden" ref={fileInputRef} onChange={handleRestore} />
          </button>
        </div>
      </header>

      <div className="p-4 flex-1">
        <div className="flex items-center justify-between mb-6">
          <button onClick={prevMonth} className="p-2 hover:bg-slate-100 dark:hover:bg-slate-700/50 rounded-full">
            <ChevronLeft />
          </button>
          <h2 className="text-xl font-medium capitalize dark:text-slate-100">
            {format(currentDate, "MMMM yyyy", { locale: ca })}
          </h2>
          <button onClick={nextMonth} className="p-2 hover:bg-slate-100 dark:hover:bg-slate-700/50 rounded-full">
            <ChevronRight />
          </button>
        </div>

        <div className="grid grid-cols-7 gap-2 mb-2 text-center text-sm font-medium text-slate-500 dark:text-slate-400">
          {['dl', 'dt', 'dc', 'dj', 'dv', 'ds', 'dg'].map(day => (
            <div key={day} className="uppercase">{day}</div>
          ))}
        </div>

        <div className="grid grid-cols-7 gap-2">
          {paddedDays.map((_, i) => (
            <div key={`pad-${i}`} className="h-14"></div>
          ))}
          {daysInMonth.map(date => {
            const dateStr = format(date, "yyyy-MM-dd");
            const hasData = dayHasRecords(dateStr);
            const today = isToday(date);
            const current = isSameMonth(date, currentDate);

            return (
              <button
                key={dateStr}
                onClick={() => navigate(hasData ? `/dia/${dateStr}` : `/registre/${dateStr}`)}
                className={cn(
                  "h-14 rounded-xl flex items-center justify-center font-medium text-lg transition-transform active:scale-95",
                  !current && "opacity-30",
                  hasData ? "bg-indigo-100 dark:bg-indigo-500/20 text-indigo-700 dark:text-indigo-300 font-bold border border-indigo-200 dark:border-indigo-500/30" : "bg-slate-50 dark:bg-slate-700/30 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700/50",
                  today && "ring-2 ring-indigo-500 inset-0"
                )}
              >
                {format(date, "d")}
              </button>
            );
          })}
        </div>
      </div>

      <button 
        onClick={() => navigate(`/registre/${format(new Date(), "yyyy-MM-dd")}`)}
        className="fixed bottom-24 right-6 bg-indigo-600 text-white p-4 rounded-2xl shadow-lg hover:bg-indigo-700 hover:scale-105 transition-all text-xl"
      >
        <Plus size={28} />
      </button>
    </div>
  );
}
