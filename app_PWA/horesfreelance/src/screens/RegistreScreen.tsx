import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useStore } from "../store/useStore";
import { format } from "date-fns";
import { ca } from "date-fns/locale";
import { Dia, Concepte, Client, RangHorari } from "../types";
import { generateId, createEmptyDia, rangesOverlap } from "../lib/utils";
import { ArrowLeft, Save, Plus, Trash2 } from "lucide-react";

export default function RegistreScreen() {
  const { id } = useParams(); // Using date string "YYYY-MM-DD" as ID
  const navigate = useNavigate();
  const { dies, clients, saveDia, addClient } = useStore();

  const [diaState, setDiaState] = useState<Dia | null>(null);

  useEffect(() => {
    if (id) {
      const existing = dies.find(d => d.data === id || d.id === id);
      if (existing) {
        setDiaState(JSON.parse(JSON.stringify(existing))); // Deep copy
      } else {
        setDiaState(createEmptyDia(id));
      }
    }
  }, [id, dies]);

  if (!diaState) return <div className="p-4">Carregant...</div>;

  const handleUpdateNotes = (text: string) => {
    setDiaState({ ...diaState, notesGlobals: text });
  };

  const handleAddConcepte = () => {
    const newConcepte: Concepte = {
      id: generateId(),
      nom: "",
      clientId: clients.length > 0 ? clients[0].id : "",
      preuFix: false,
      importFix: 0,
      importDespeses: 0,
      notesDespeses: "",
      estatFacturacio: "Pendent",
      rangs: []
    };
    setDiaState({ ...diaState, conceptes: [newConcepte, ...diaState.conceptes] });
  };

  const checkOverlaps = () => {
    const allRangs = diaState.conceptes.flatMap(c => c.rangs);
    for (let i = 0; i < allRangs.length; i++) {
      for (let j = i + 1; j < allRangs.length; j++) {
        if (rangesOverlap(allRangs[i], allRangs[j])) {
          return true;
        }
      }
    }
    return false;
  };

  const handleSave = () => {
    if (diaState.conceptes.some(c => c.nom.trim() === "")) {
      return alert("Tots els bolos han de tenir un nom.");
    }
    if (diaState.conceptes.some(c => c.rangs.length === 0 && !c.preuFix)) {
        // Technically they might want 0 hours but fixed price
        if (diaState.conceptes.some(c => c.rangs.length === 0 && !c.preuFix)) {
           return alert("Els bolos per hores requereixen com a mínim un rang horari.");
        }
    }
    if (checkOverlaps()) {
      return alert("Hi ha solapament en els rangs horaris d'avui! Si us plau, revisa les hores.");
    }
    saveDia(diaState);
    navigate(-1);
  };

  // Internal component for Concept editor to keep things clean
  const ConcepteEditor = ({ concepte, idx }: { concepte: Concepte, idx: number, key?: any }) => {
    
    const updateConcepte = (updates: Partial<Concepte>) => {
      const newConceptes = [...diaState.conceptes];
      newConceptes[idx] = { ...concepte, ...updates };
      setDiaState({ ...diaState, conceptes: newConceptes });
    };

    const removeConcepte = () => {
      setDiaState({ 
        ...diaState, 
        conceptes: diaState.conceptes.filter((_, i) => i !== idx) 
      });
    };

    const addRang = () => {
      updateConcepte({ 
        rangs: [...concepte.rangs, { id: generateId(), inici: "09:00", fi: "10:00" }] 
      });
    };

    const removeRang = (rIdx: number) => {
      updateConcepte({ 
        rangs: concepte.rangs.filter((_, i) => i !== rIdx) 
      });
    };

    const updateRang = (rIdx: number, field: 'inici'|'fi', value: string) => {
      const newRangs = [...concepte.rangs];
      newRangs[rIdx] = { ...newRangs[rIdx], [field]: value };
      updateConcepte({ rangs: newRangs });
    };

    return (
      <div className="bg-white dark:bg-slate-700 border dark:border-slate-600 rounded-xl p-4 mb-4 shadow-sm transition-colors">
        <div className="flex justify-between items-start mb-4">
          <input 
            type="text" 
            placeholder="Nom del bolo (ex: Rodatge Matí)" 
            className="text-lg font-semibold dark:text-slate-100 border-b border-transparent dark:hover:border-slate-500 hover:border-slate-300 focus:border-indigo-500 focus:outline-none w-full bg-transparent px-1 py-0.5 transition-colors"
            value={concepte.nom}
            onChange={e => updateConcepte({ nom: e.target.value })}
          />
          <button onClick={removeConcepte} className="text-red-400 hover:text-red-600 dark:hover:text-red-300 p-1 ml-2"><Trash2 size={18} /></button>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 mb-4">
          <div>
            <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Client</label>
            <div className="flex gap-2">
              <select 
                className="w-full bg-slate-50 dark:bg-slate-800 dark:text-slate-200 border border-slate-200 dark:border-slate-600 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
                value={concepte.clientId}
                onChange={e => {
                  if (e.target.value === "NEW") {
                    const nom = prompt("Nom del nou client:");
                    if (nom) {
                      const newC: Client = { id: generateId(), nom, tarifaHoraria: 20 };
                      addClient(newC);
                      updateConcepte({ clientId: newC.id });
                    }
                  } else {
                    updateConcepte({ clientId: e.target.value });
                  }
                }}
              >
                <option value="" disabled>Selecciona Client</option>
                {clients.map(c => <option key={c.id} value={c.id}>{c.nom} ({c.tarifaHoraria}€/h)</option>)}
                <option value="NEW" className="font-bold text-indigo-600 dark:text-indigo-400">+ Nou Client...</option>
              </select>
            </div>
          </div>

          <div>
             <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Estat de Facturació</label>
             <select 
               className="w-full bg-slate-50 dark:bg-slate-800 dark:text-slate-200 border border-slate-200 dark:border-slate-600 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none transition-colors"
               value={concepte.estatFacturacio}
               onChange={e => updateConcepte({ estatFacturacio: e.target.value as any })}
             >
               <option value="Pendent">Pendent</option>
               <option value="Facturat">Facturat</option>
               <option value="Cobrat">Cobrat</option>
             </select>
          </div>
        </div>

        <div className="flex items-center gap-2 mb-4 p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg border border-slate-100 dark:border-slate-600/50 transition-colors">
           <input 
             type="checkbox" 
             id={`fix-${concepte.id}`}
             checked={concepte.preuFix} 
             onChange={e => updateConcepte({ preuFix: e.target.checked })}
             className="rounded text-indigo-600 focus:ring-indigo-500 h-4 w-4 bg-white dark:bg-slate-700 dark:border-slate-500"
           />
           <label htmlFor={`fix-${concepte.id}`} className="text-sm font-medium text-slate-700 dark:text-slate-300">Preu Fix (Tancat)</label>
           
           {concepte.preuFix && (
             <div className="ml-auto flex items-center gap-1">
               <input 
                 type="number" 
                 value={concepte.importFix} 
                 onChange={e => updateConcepte({ importFix: parseFloat(e.target.value) || 0 })}
                 className="w-24 text-right bg-transparent dark:text-slate-100 border border-slate-300 dark:border-slate-500 rounded px-2 py-1 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
               />
               <span className="text-sm text-slate-500 dark:text-slate-400">€</span>
             </div>
           )}
        </div>

        <div className="mb-4">
          <div className="flex items-center justify-between mb-2">
            <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">Rangs Horaris</label>
            <button onClick={addRang} className="text-xs text-indigo-600 dark:text-indigo-400 font-medium flex items-center bg-indigo-50 dark:bg-indigo-500/10 px-2 py-1 rounded hover:bg-indigo-100 dark:hover:bg-indigo-500/20"><Plus size={14} className="mr-1"/> Afegir</button>
          </div>
          {concepte.rangs.length === 0 ? (
            <p className="text-xs text-slate-400 dark:text-slate-500 italic">Cap rang associat.</p>
          ) : (
            <div className="space-y-2 text-sm">
              {concepte.rangs.map((rang, rIdx) => (
                <div key={rang.id} className="flex items-center gap-2">
                  <input 
                    type="time" 
                    value={rang.inici} 
                    onChange={e => updateRang(rIdx, 'inici', e.target.value)}
                    className="border border-slate-200 dark:border-slate-500 rounded px-2 py-1 flex-1 focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-slate-50 dark:bg-slate-800 dark:text-slate-200"
                  />
                  <span className="text-slate-400">-</span>
                  <input 
                    type="time" 
                    value={rang.fi || ""} 
                    onChange={e => updateRang(rIdx, 'fi', e.target.value)}
                    className="border border-slate-200 dark:border-slate-500 rounded px-2 py-1 flex-1 focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-slate-50 dark:bg-slate-800 dark:text-slate-200"
                  />
                  <button onClick={() => removeRang(rIdx)} className="text-slate-400 hover:text-red-500 dark:hover:text-red-400 p-1"><Trash2 size={16}/></button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="pt-3 border-t border-slate-100 dark:border-slate-600/50">
           <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-2">Despeses (Ex: Taxis, Dinars)</label>
           <div className="flex gap-2">
             <div className="relative w-24 shrink-0">
               <input 
                 type="number" 
                 placeholder="0.00"
                 value={concepte.importDespeses || ""}
                 onChange={e => updateConcepte({ importDespeses: parseFloat(e.target.value) || 0 })}
                 className="w-full border border-slate-200 dark:border-slate-600 rounded-lg pl-2 pr-6 py-1.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-slate-50 dark:bg-slate-800 dark:text-slate-200"
               />
               <span className="absolute right-2 top-1.5 text-slate-400 text-sm">€</span>
             </div>
             <input 
               type="text" 
               placeholder="Notes despeses..."
               value={concepte.notesDespeses}
               onChange={e => updateConcepte({ notesDespeses: e.target.value })}
               className="flex-1 border border-slate-200 dark:border-slate-600 rounded-lg px-3 py-1.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-slate-50 dark:bg-slate-800 dark:text-slate-200"
             />
           </div>
        </div>

      </div>
    );
  };

  return (
    <div className="flex flex-col h-full bg-slate-100 dark:bg-slate-800 transition-colors">
      <header className="flex items-center justify-between p-4 bg-white dark:bg-slate-900 border-b dark:border-slate-700 sticky top-0 z-10 shadow-sm transition-colors">
        <button onClick={() => navigate(-1)} className="p-2 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full">
          <ArrowLeft />
        </button>
        <h1 className="text-lg font-medium text-slate-800 dark:text-slate-100">Registre d'Hores</h1>
        <button onClick={handleSave} className="p-2 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 rounded-full flex items-center font-medium pr-3" title="Desar">
          <Save size={20} className="mr-1" /> <span className="hidden sm:inline">Desar</span>
        </button>
      </header>

      <div className="p-4 flex-1 overflow-y-auto">
        <div className="mb-6">
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Data</label>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 border-b-2 border-indigo-200 dark:border-indigo-500 inline-block pb-1 capitalize mt-1">
             {format(new Date(diaState.data), "EEEE, dd 'de' MMMM", { locale: ca })}
          </div>
        </div>

        <div className="mb-6">
           <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">Notes Generals</label>
           <textarea 
             className="w-full bg-transparent dark:text-slate-100 border border-slate-200 dark:border-slate-600 rounded-xl p-3 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none shadow-sm resize-none transition-colors"
             rows={2}
             placeholder="Alguna nota sobre avui..."
             value={diaState.notesGlobals}
             onChange={e => handleUpdateNotes(e.target.value)}
           ></textarea>
        </div>

        <div className="flex items-center justify-between mb-3">
          <h2 className="text-sm font-medium uppercase tracking-wider text-slate-500 dark:text-slate-400">Bolos del Dia ({diaState.conceptes.length})</h2>
          <button 
            onClick={handleAddConcepte} 
            className="text-xs bg-indigo-600 dark:bg-indigo-500 hover:bg-indigo-700 dark:hover:bg-indigo-600 text-white flex items-center px-3 py-1.5 rounded-full transition shadow-sm font-medium"
          >
            <Plus size={14} className="mr-1"/> Nou Bolo
          </button>
        </div>

        {diaState.conceptes.map((concepte, idx) => (
          <ConcepteEditor key={concepte.id} concepte={concepte} idx={idx} />
        ))}
        
        {diaState.conceptes.length === 0 && (
          <div className="text-center p-8 bg-white dark:bg-slate-700 rounded-xl border border-dashed border-slate-300 dark:border-slate-600 transition-colors">
             <p className="text-slate-500 dark:text-slate-400 text-sm">Cap bolo registrat encara.</p>
             <button onClick={handleAddConcepte} className="mt-2 text-indigo-600 dark:text-indigo-400 font-medium hover:underline text-sm">+ Afegeix el primer bolo</button>
          </div>
        )}

      </div>
    </div>
  );
}
