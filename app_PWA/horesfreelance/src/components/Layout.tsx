import { useEffect } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import { Calendar, PieChart, Users, Sun, Moon } from "lucide-react";
import { cn } from "../lib/utils";
import { useStore } from "../store/useStore";
import { useTranslation } from "react-i18next";
import i18n from "../i18n";

const LANGS = [
  { code: "ca", label: "CA" },
  { code: "es", label: "ES" },
  { code: "en", label: "EN" },
];

export default function Layout() {
  const location = useLocation();
  const { theme, setTheme } = useStore();
  const { t } = useTranslation();

  useEffect(() => {
    if (theme === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [theme]);

  const navItems = [
    { to: "/calendar", icon: Calendar, labelKey: "nav_calendari" },
    { to: "/resum",    icon: PieChart,  labelKey: "nav_resum" },
    { to: "/clients",  icon: Users,     labelKey: "nav_clients" },
  ];

  const currentLang = i18n.language?.slice(0, 2) || "ca";

  const handleLangChange = (code: string) => {
    i18n.changeLanguage(code);
  };

  return (
    <div className="flex flex-col h-screen bg-slate-50 dark:bg-[#1e293b] text-slate-900 dark:text-slate-100 font-sans transition-colors duration-200">
      <main className="flex-1 overflow-y-auto pb-20 safe-area-bottom">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 w-full bg-white dark:bg-[#0f172a] border-t border-slate-200 dark:border-slate-800 safe-area-padding flex justify-around items-center p-2 z-50 transition-colors">

        {/* Navegació principal */}
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive =
            location.pathname === item.to ||
            (item.to !== "/" && location.pathname.startsWith(item.to));

          return (
            <Link
              key={item.to}
              to={item.to}
              className={cn(
                "flex flex-col items-center justify-center p-2 rounded-xl min-w-[64px] transition-colors",
                isActive
                  ? "text-indigo-600 dark:text-indigo-400"
                  : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
              )}
            >
              <div
                className={cn(
                  "p-1 rounded-full",
                  isActive && "bg-indigo-100 dark:bg-indigo-500/20"
                )}
              >
                <Icon size={24} strokeWidth={isActive ? 2.5 : 2} />
              </div>
              <span className="text-xs mt-1 font-medium">{t(item.labelKey)}</span>
            </Link>
          );
        })}

        {/* Separador visual */}
        <div className="w-px h-8 bg-slate-200 dark:bg-slate-700 mx-1" />

        {/* Selector d'idioma */}
        <div className="flex flex-col items-center gap-0.5">
          <div className="flex gap-0.5">
            {LANGS.map((lang) => (
              <button
                key={lang.code}
                onClick={() => handleLangChange(lang.code)}
                className={cn(
                  "text-[10px] font-semibold px-1.5 py-0.5 rounded transition-colors",
                  currentLang === lang.code
                    ? "bg-indigo-600 text-white"
                    : "text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                )}
                aria-label={`Canviar idioma a ${lang.label}`}
              >
                {lang.label}
              </button>
            ))}
          </div>
          <span className="text-[9px] text-slate-400 dark:text-slate-500 mt-0.5">Idioma</span>
        </div>

        {/* Separador visual */}
        <div className="w-px h-8 bg-slate-200 dark:bg-slate-700 mx-1" />

        {/* Botó tema clar/fosc */}
        <button
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          className="flex flex-col items-center justify-center p-2 rounded-xl min-w-[48px] text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          aria-label="Canviar tema"
        >
          <div className="p-1 rounded-full">
            {theme === "dark" ? <Sun size={22} strokeWidth={2} /> : <Moon size={22} strokeWidth={2} />}
          </div>
          <span className="text-xs mt-1 font-medium">
            {theme === "dark" ? t("tema_clar") : t("tema_fosc")}
          </span>
        </button>
      </nav>
    </div>
  );
}
