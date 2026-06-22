import { useState } from "react";
import { useStore } from "../store/useStore";
import { Client } from "../types";
import { generateId } from "../lib/utils";
import { Plus, Edit2, Trash2, X, Check } from "lucide-react";

export default function ClientsScreen() {
  const { clients, addClient, updateClient, deleteClient } = useStore();
  const [editingClient, setEditingClient] = useState<Client | null>(null);
  const [isAdding, setIsAdding] = useState(false);

  const [nom, setNom] = useState("");
  const [tarifa, setTarifa] = useState("");

  const startEdit = (c: Client) => {
    setEditingClient(c);
    setNom(c.nom);
    setTarifa(c.preuHoraDefecte.toString());
    setIsAdding(false);
  };

  const startAdd = () => {
    setIsAdding(true);
    setEditingClient(null);
    setNom("");
    setTarifa("");
  };

  const cancelEdit = () => {
    setEditingClient(null);
    setIsAdding(false);
  };

  const handleSave = () => {
    if (!nom.trim() || !tarifa) return alert("Posa un nom i una tarifa.");
    
    if (isAdding) {
      addClient({ id: generateId(), nom, preuHoraDefecte: parseFloat(tarifa) });
    } else if (editingClient) {
      if(window.confirm("Això actualitzarà la tarifa per a tots els bolos Tancats o Pendents d'aquest client. Estàs d'acord?")) {
        updateClient({ ...editingClient, nom, preuHoraDefecte: parseFloat(tarifa) });
      }
    }
    cancelEdit();
  };

  const handleDelete = (id: string, nom: string) => {
    if (window.confirm(`Vols eliminar el client ${nom}? No s'esborraran els seus bolos passats.`)) {
      deleteClient(id);
    }
  };

  return (
    <div className="flex flex-col h-full bg-slate-50 dark:bg-slate-800 transition-colors">
      <header className="p-4 bg-indigo-600 dark:bg-indigo-900 text-white shadow-md sticky top-0 z-10 flex justify-between items-center">
        <h1 className="text-xl font-medium">Clients</h1>
      </header>

      <div className="p-4 flex-1 overflow-y-auto">
        
        {(isAdding || editingClient) && (
          <div className="bg-white dark:bg-slate-700 p-4 rounded-xl shadow-md border border-slate-200 dark:border-slate-600 mb-6 drop-shadow-sm animate-in fade-in slide-in-from-top-2">
            <h2 className="text-lg font-medium text-slate-800 dark:text-slate-100 mb-4">{isAdding ? 'Nou Client' : 'Editar Client'}</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Nom del Client</label>
                <input 
                  type="text" 
                  value={nom} 
                  onChange={e => setNom(e.target.value)} 
                  className="w-full bg-transparent dark:text-slate-100 border border-slate-300 dark:border-slate-500 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent" 
                  placeholder="Ex: Empresa S.L." 
                  autoFocus
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Tarifa Horària Base (€)</label>
                <input 
                  type="number" 
                  value={tarifa} 
                  onChange={e => setTarifa(e.target.value)} 
                  className="w-full bg-transparent dark:text-slate-100 border border-slate-300 dark:border-slate-500 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent" 
                  placeholder="25.00" 
                />
              </div>
              <div className="flex justify-end gap-2 pt-2 mt-2 border-t border-slate-100 dark:border-slate-600/50">
                <button onClick={cancelEdit} className="px-4 py-2 text-slate-600 dark:text-slate-300 font-medium text-sm hover:bg-slate-100 dark:hover:bg-slate-600 rounded-lg flex items-center">
                  <X size={16} className="mr-1" /> Cancel·lar
                </button>
                <button onClick={handleSave} className="px-4 py-2 bg-indigo-600 dark:bg-indigo-500 text-white font-medium text-sm hover:bg-indigo-700 dark:hover:bg-indigo-600 rounded-lg flex items-center shadow-sm">
                  <Check size={16} className="mr-1" /> Desar
                </button>
              </div>
            </div>
          </div>
        )}

        <div className="space-y-3">
          {clients.length === 0 && !isAdding && (
             <div className="text-center p-8 bg-white dark:bg-slate-700 border border-dashed border-slate-300 dark:border-slate-600 rounded-xl">
               <p className="text-slate-500 dark:text-slate-400 text-sm">No tens cap client registrat.</p>
               <button onClick={startAdd} className="bg-indigo-50 dark:bg-indigo-500/10 text-indigo-700 dark:text-indigo-400 px-4 py-2 rounded-full text-sm font-medium mt-4 hover:bg-indigo-100 dark:hover:bg-indigo-500/20 transition">Afegeix-ne un</button>
             </div>
          )}
          {clients.map(c => (
            <div key={c.id} className="bg-white dark:bg-slate-700 p-4 rounded-xl border border-slate-200 dark:border-slate-600 shadow-sm flex items-center justify-between transition-colors">
              <div>
                <h3 className="font-semibold text-slate-800 dark:text-slate-100 text-lg">{c.nom}</h3>
                <p className="text-slate-500 dark:text-slate-400 text-sm">{c.preuHoraDefecte.toFixed(2)} €/h</p>
              </div>
              <div className="flex gap-1">
                <button onClick={() => startEdit(c)} className="p-2 text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-500/10 rounded-full transition" title="Editar">
                  <Edit2 size={18} />
                </button>
                <button onClick={() => handleDelete(c.id, c.nom)} className="p-2 text-slate-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-full transition" title="Esborrar">
                  <Trash2 size={18} />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {!isAdding && !editingClient && (
        <button 
          onClick={startAdd}
          className="fixed bottom-24 right-6 bg-indigo-600 text-white p-4 rounded-2xl shadow-lg hover:bg-indigo-700 hover:scale-105 transition-all text-xl"
          title="Afegir Client"
        >
          <Plus size={28} />
        </button>
      )}
    </div>
  );
}
