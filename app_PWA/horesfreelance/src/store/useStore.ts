import { create } from "zustand";
import { persist } from "zustand/middleware";
import { AppStorageData, Client, Dia, Concepte, RangHorari } from "../types";
import { generateId, roundToNearest15Min, formatTimeHHmm, createEmptyDia, isValidDate, isValidTime } from "../lib/utils";
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
}

// Helper function per generar el nom del bolo ràpid equivalent a Android
const generateBoloRapidName = (dies: Dia[]): string => {
  let maxN = 0;
  const regex = /^Bolo sense títol (\d+)$/;
  
  dies.forEach(dia => {
    dia.conceptes.forEach(concepte => {
      const match = concepte.nom.match(regex);
      if (match) {
        const num = parseInt(match[1], 10);
        if (num > maxN) maxN = num;
      }
    });
  });
  
  return `Bolo sense títol ${maxN + 1}`;
};

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
        // CORRECCIÓ: Només actualitzem el client. Ja NO es sincronitzen els preus dels bolos existents.
        set((state) => ({
          clients: state.clients.map((c) => (c.id === client.id ? client : c)),
        }));
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

        // CORRECCIÓ: Canviat de "Bolo Ràpid" a "Bolo sense títol N" per coincidir amb Android
        const nomBolo = generateBoloRapidName(dies);

        const newConcepte: Concepte = {
          id: concepteId,
          diaId: diaId,
          nom: nomBolo,
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
          let clients: Client[] = [];
          let dies: Dia[] = [];

          // Detectar format (PWA amb "state" o Android pla)
          if (parsed.state && parsed.state.clients && parsed.state.dies) {
            clients = parsed.state.clients;
            dies = parsed.state.dies;
          } else if (parsed.clients && parsed.dies) {
            clients = parsed.clients;
            dies = parsed.dies;
          } else {
            throw new Error("Format de JSON no reconegut. Esperava: {clients: [], dies: []}");
          }

          if (!clients || !dies) {
            throw new Error("Dades incompletes");
          }

          // ✅ SANITITZACIÓ DEFENSIVA COMPLETA
          const sanitizedClients = clients.map((client: any) => {
            if (!client.id || typeof client.id !== 'string') {
              throw new Error("Client sense ID vàlid");
            }
            return {
              id: client.id,
              nom: typeof client.nom === 'string'
                ? client.nom.trim() || "Client sense nom"
                : "Client sense nom",
              preuHoraDefecte: typeof client.preuHoraDefecte === 'number'
                ? Math.max(0, client.preuHoraDefecte)
                : 0,
            };
          });

          const sanitizedDies = dies.map((dia: any) => {
            if (!dia.id || typeof dia.id !== 'string') {
              throw new Error("Dia sense ID vàlid");
            }
            
            // Validar data
            if (!isValidDate(dia.data)) {
              throw new Error(`Data inválida: ${dia.data}. Format esperat: YYYY-MM-DD`);
            }

            const clientIds = sanitizedClients.map(c => c.id);

            const sanitizedConceptes = (dia.conceptes || []).map((concepte: any) => {
              if (!concepte.id || typeof concepte.id !== 'string') {
                throw new Error(`Concepte sense ID en dia ${dia.data}`);
              }

              // Validar que clientId existeix (si no és null)
              if (concepte.clientId && !clientIds.includes(concepte.clientId)) {
                throw new Error(`Concepte '${concepte.nom}' referencia un client que no existeix: ${concepte.clientId}`);
              }

              // Validar i sanitizar rangsHoraris
              const sanitizedRangs = (concepte.rangsHoraris || [])
                .filter((r: any) => {
                  if (!isValidTime(r.horaInici) || !isValidTime(r.horaFi)) {
                    console.warn(`RangHorari amb hores inválides en '${concepte.nom}': ${r.horaInici}-${r.horaFi}`);
                    return false;
                  }
                  return true;
                });

              return {
                id: concepte.id,
                diaId: concepte.diaId || dia.id,
                nom: typeof concepte.nom === 'string'
                  ? concepte.nom.trim() || "Bolo sense títol"
                  : "Bolo sense títol",
                preuHora: typeof concepte.preuHora === 'number'
                  ? Math.max(0, concepte.preuHora)
                  : 0,
                clientId: concepte.clientId ?? null,
                clientNom: concepte.clientNom ?? null,
                rangsHoraris: sanitizedRangs,
                estat: (['PENDENT', 'FACTURAT', 'COBRAT'].includes(concepte.estat)
                  ? concepte.estat
                  : 'PENDENT') as import("../types").EstatFacturacio,
                despeses: typeof concepte.despeses === 'number'
                  ? Math.max(0, concepte.despeses)
                  : 0,
                despesesNotes: typeof concepte.despesesNotes === 'string'
                  ? concepte.despesesNotes.trim()
                  : "",
                preuFix: typeof concepte.preuFix === 'boolean'
                  ? concepte.preuFix
                  : false,
                importFix: typeof concepte.importFix === 'number'
                  ? Math.max(0, concepte.importFix)
                  : 0,
              };
            });

            return {
              id: dia.id,
              data: dia.data,
              notes: typeof dia.notes === 'string' ? dia.notes.trim() : "",
              conceptes: sanitizedConceptes,
            };
          });

          set({
            clients: sanitizedClients,
            dies: sanitizedDies,
            activeClockIn: null,  // ✅ Sempre null en importar (no comparteix sessions amb Android)
          });
        } catch (e) {
          console.error("Error en loadBackup:", e);
          alert(`Error en importar: ${(e as Error).message}`);
        }
      }

    }),
    {
      name: "hores-freelance-storage",
    }
  )
);