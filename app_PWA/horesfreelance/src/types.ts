export interface Client {
  id: string;
  nom: string;
  preuHoraDefecte: number;
}

export interface RangHorari {
  id: string;
  concepteId: string;
  horaInici: string; // HH:mm
  horaFi: string; // HH:mm
}

export type EstatFacturacio = 'PENDENT' | 'FACTURAT' | 'COBRAT';

export interface Concepte {
  id: string;
  diaId: string;
  nom: string;
  preuHora: number;
  clientId: string | null;
  clientNom?: string | null;
  rangsHoraris: RangHorari[];
  estat: EstatFacturacio;
  despeses: number;
  despesesNotes: string;
  preuFix: boolean;
  importFix: number;
}

export interface Dia {
  id: string;
  data: string; // YYYY-MM-DD
  notes: string;
  conceptes: Concepte[];
}

export interface AppStorageData {
  clients: Client[];
  dies: Dia[];
  activeClockIn: string | null; // ISO DateTime
}
