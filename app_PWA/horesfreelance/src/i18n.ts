import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import ca from './locales/ca.json';
import es from './locales/es.json';
import en from './locales/en.json';

i18n
  .use(LanguageDetector)   // Detecta l'idioma del navegador
  .use(initReactI18next)
  .init({
    resources: {
      ca: { translation: ca },
      es: { translation: es },
      en: { translation: en },
    },
    // Intentarà el idioma del navegador; si no és CA/ES/EN, cau a CA
    fallbackLng: 'ca',
    supportedLngs: ['ca', 'es', 'en'],
    // Guarda l'elecció de l'usuari a localStorage
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
      lookupLocalStorage: 'horesfreelance-lang',
    },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
