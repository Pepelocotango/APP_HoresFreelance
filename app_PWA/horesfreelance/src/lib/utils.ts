import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function generateId() {
  return crypto.randomUUID();
}

/**
 * Rounds a Date to the nearest 15 minutes.
 */
export function roundToNearest15Min(date: Date): Date {
  const ms = 1000 * 60 * 15;
  return new Date(Math.round(date.getTime() / ms) * ms);
}

/**
 * Formats a Date object as HH:mm
 */
export function formatTimeHHmm(date: Date): string {
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${hours}:${minutes}`;
}

/**
 * Parses HH:mm string to total minutes
 */
export function timeToMinutes(time: string): number {
  if (!time) return 0;
  const [h, m] = time.split(":").map(Number);
  return h * 60 + (m || 0);
}

/**
 * Duration in hours of a RangHorari
 */
export function calculateRangHours(inici: string, fi: string): number {
  if (!fi) return 0;
  let startMin = timeToMinutes(inici);
  let endMin = timeToMinutes(fi);
  if (endMin < startMin) {
    // Crosses midnight, assume it ends the next day
    endMin += 24 * 60;
  }
  return (endMin - startMin) / 60;
}

/**
 * Check if two time ranges overlap
 */
export function rangesOverlap(r1: {horaInici: string, horaFi: string}, r2: {horaInici: string, horaFi: string}): boolean {
  if (!r1.horaInici || !r1.horaFi || !r2.horaInici || !r2.horaFi) return false;
  
  const s1 = timeToMinutes(r1.horaInici);
  let e1 = timeToMinutes(r1.horaFi);
  if(e1 <= s1) e1 += 24 * 60;
  
  const s2 = timeToMinutes(r2.horaInici);
  let e2 = timeToMinutes(r2.horaFi);
  if(e2 <= s2) e2 += 24 * 60;

  return s1 < e2 && s2 < e1;
}

/**
 * Create a base Dia object
 */
export function createEmptyDia(dateStr: string): import("../types").Dia {
  return {
    id: generateId(),
    data: dateStr,
    notes: "",
    conceptes: []
  };
}
