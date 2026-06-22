import { create } from "zustand";
import { persist } from "zustand/middleware";
import { AppStorageData, Client, Dia, Concepte, RangHorari } from "../types";
import { generateId, roundToNearest15Min, formatTimeHHmm, createEmptyDia } from "../lib/utils";
import { format } from "date-fns";

interface AppState extends AppStorageData {
  theme: 'light' | 'dark';
  setTheme: (theme: 'light' | 'dark') => void;
  addClient: (client: Client) => void;
  updateClient: (client: Client) => void;
  deleteClient: (id: string) => void;
  saveDia: (dia: Dia) => void;
  deleteDia: (id: string) => void;
  deleteConcepte: (diaId: string, concepteId: string) => void;
  startClockIn: () => void;
  stopClockIn: (defaultClientId?: string) => string | null; // Returns dayId if created
  cancelClockIn: () => void;
  loadBackup: (jsonString: string) => void;
  syncClientRates: (clientId: string, newRate: number) => void;
}

export const useStore = create<AppState>()(
  persist(
    (set, get) => ({
      theme: 'light' as const,
      setTheme: (theme) => set({ theme }),
      clients: [],
      dies: [],
      activeClockIn: null,

      addClient: (client) =>
        set((state) => ({ clients: [...state.clients, client] })),

      updateClient: (client) => {
        set((state) => ({
          clients: state.clients.map((c) => (c.id === client.id ? client : c)),
        }));
        get().syncClientRates(client.id, client.tarifaHoraria);
      },

      deleteClient: (id) =>
        set((state) => ({
          clients: state.clients.filter((c) => c.id !== id),
        })),

      saveDia: (dia) =>
        set((state) => {
          const index = state.dies.findIndex((d) => d.data === dia.data);
          let newDies = [...state.dies];
          if (index >= 0) {
            newDies[index] = dia;
          } else {
            newDies.push(dia);
          }
          return { dies: newDies };
        }),

      deleteDia: (id) =>
        set((state) => ({
          dies: state.dies.filter((d) => d.id !== id && d.data !== id),
        })),

      deleteConcepte: (diaId, concepteId) =>
        set((state) => {
          return {
            dies: state.dies.map(d => {
              if (d.data === diaId || d.id === diaId) {
                return { ...d, conceptes: d.conceptes.filter(c => c.id !== concepteId) }
              }
              return d;
            })
          }
        }),

      startClockIn: () =>
        set({ activeClockIn: roundToNearest15Min(new Date()).toISOString() }),
        
      cancelClockIn: () => set({ activeClockIn: null }),

      stopClockIn: (defaultClientId) => {
        const { activeClockIn, dies } = get();
        if (!activeClockIn) return null;

        const startDate = new Date(activeClockIn);
        const endDate = roundToNearest15Min(new Date());
        
        // Safety: if end is same as start (clocked out immediately), add 15 mins to make sense
        if (endDate.getTime() <= startDate.getTime()) {
          endDate.setMinutes(endDate.getMinutes() + 15);
        }

        const dateStr = format(startDate, "yyyy-MM-dd");
        const iniciStr = formatTimeHHmm(startDate);
        const fiStr = formatTimeHHmm(endDate);

        const newRang: RangHorari = {
          id: generateId(),
          inici: iniciStr,
          fi: fiStr,
        };

        const newConcepte: Concepte = {
          id: generateId(),
          nom: "Bolo Ràpid",
          clientId: defaultClientId || "",
          preuFix: false,
          importFix: 0,
          importDespeses: 0,
          notesDespeses: "",
          estatFacturacio: "Pendent",
          rangs: [newRang],
        };

        const existingDia = dies.find((d) => d.data === dateStr);
        let updatedDia: Dia;
        if (existingDia) {
          updatedDia = {
            ...existingDia,
            conceptes: [...existingDia.conceptes, newConcepte],
          };
        } else {
          updatedDia = createEmptyDia(dateStr);
          updatedDia.conceptes = [newConcepte];
        }

        get().saveDia(updatedDia);
        set({ activeClockIn: null });
        
        return updatedDia.id;
      },

      loadBackup: (jsonString) => {
        try {
          const parsed = JSON.parse(jsonString) as Partial<AppStorageData>;
          if (parsed.clients && parsed.dies) {
            set({
              clients: parsed.clients,
              dies: parsed.dies,
              activeClockIn: parsed.activeClockIn || null,
            });
          }
        } catch (e) {
          console.error("Failed to restore backup", e);
          alert("El format del fitxer no és vàlid.");
        }
      },

      syncClientRates: (clientId, newRate) => {
        set((state) => {
          const updatedDies = state.dies.map((dia) => ({
            ...dia,
            conceptes: dia.conceptes.map((c) => {
              // Only sync "Pendent" and non-fixed price items for this client
              if (c.clientId === clientId && c.estatFacturacio === "Pendent" && !c.preuFix) {
                 // The view dynamically calculates based on current client list, 
                 // but we'll structure this to fit if we ever decoupled.
                 // Actually MVVM strictly says we just use the reference,
                 // but doing the logic here is safe.
                 return c;
              }
              return c;
            })
          }));
          return { dies: updatedDies };
        });
      }
    }),
    {
      name: "hores-freelance-storage",
    }
  )
);
