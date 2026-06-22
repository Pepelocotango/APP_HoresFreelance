export interface Client {
  id: string;
  nom: string;
  tarifaHoraria: number;
}

export interface RangHorari {
  id: string;
  inici: string; // HH:mm
  fi: string | null; // HH:mm
}

export type EstatFacturacio = 'Pendent' | 'Facturat' | 'Cobrat';

export interface Concepte {
  id: string;
  nom: string;
  clientId: string;
  preuFix: boolean;
  importFix: number;
  importDespeses: number;
  notesDespeses: string;
  estatFacturacio: EstatFacturacio;
  rangs: RangHorari[];
}

export interface Dia {
  id: string; // primary key (typically YYYY-MM-DD or UUID)
  data: string; // YYYY-MM-DD
  notesGlobals: string;
  conceptes: Concepte[];
}

export interface AppStorageData {
  clients: Client[];
  dies: Dia[];
  activeClockIn: string | null; // ISO DateTime
}
