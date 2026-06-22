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
        get().syncClientRates(client.id, client.preuHoraDefecte);
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

        const diaId = generateId();
        const concepteId = generateId();

        const newRang: RangHorari = {
          id: generateId(),
          concepteId: concepteId,
          horaInici: iniciStr,
          horaFi: fiStr,
        };

        const newConcepte: Concepte = {
          id: concepteId,
          diaId: diaId,
          nom: "Bolo Ràpid",
          clientId: defaultClientId || null,
          preuHora: 0,
          estat: "PENDENT",
          rangsHoraris: [newRang],
          despeses: 0,
          despesesNotes: "",
          preuFix: false,
          importFix: 0,
        };

        const existingDia = dies.find((d) => d.data === dateStr);
        let updatedDia: Dia;
        if (existingDia) {
          updatedDia = {
            ...existingDia,
            conceptes: [...existingDia.conceptes, { ...newConcepte, diaId: existingDia.id }],
          };
        } else {
          updatedDia = createEmptyDia(dateStr);
          updatedDia.id = diaId;
          updatedDia.conceptes = [newConcepte];
        }

        get().saveDia(updatedDia);
        set({ activeClockIn: null });
        
        return updatedDia.id;
      },

      loadBackup: (jsonString) => {
        try {
          const parsed = JSON.parse(jsonString) as any;
          // Support both formats: PWA native (wrapped in state) and Android export (direct AppData)
          let clients: Client[] = [];
          let dies: Dia[] = [];

          if (parsed.state && parsed.state.clients && parsed.state.dies) {
            clients = parsed.state.clients;
            dies = parsed.state.dies;
          } else if (parsed.clients && parsed.dies) {
            clients = parsed.clients;
            dies = parsed.dies;
          }

          if (clients && dies) {
            set({
              clients: clients,
              dies: dies,
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
              // Only sync "PENDENT" and non-fixed price items for this client
              if (c.clientId === clientId && c.estat === "PENDENT" && !c.preuFix) {
                 return { ...c, preuHora: newRate };
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
