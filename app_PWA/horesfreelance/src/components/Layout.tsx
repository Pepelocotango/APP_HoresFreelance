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
    { to: "/resum",    icon: PieChart,  labelKey: "nav_resum" },
    { to: "/calendar", icon: Calendar, labelKey: "nav_calendari" },
    { to: "/clients",  icon: Users,     labelKey: "nav_clients" },
  ];

  const currentLang = i18n.language?.slice(0, 2) || "ca";

  const handleLangChange = (code: string) => {
    i18n.changeLanguage(code);
  };

  return (
    <div className="flex flex-col h-screen bg-slate-50 dark:bg-[#1e293b] text-slate-900 dark:text-slate-100 font-sans transition-colors duration-200">
      <header className="bg-white dark:bg-[#0f172a] border-b border-slate-200 dark:border-slate-800 p-2 z-50 flex items-center justify-between sticky top-0 transition-colors">
        <h2 className="font-bold text-lg text-indigo-600 dark:text-indigo-400 px-2">HoresFreelance</h2>

        {/* Navegació principal */}
        <div className="flex items-center gap-1">
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
                  "flex items-center gap-2 p-2 rounded-lg transition-colors text-sm font-medium",
                  isActive
                    ? "bg-indigo-100 dark:bg-indigo-500/20 text-indigo-700 dark:text-indigo-300"
                    : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                )}
              >
                <Icon size={18} strokeWidth={isActive ? 2.5 : 2} />
                {t(item.labelKey)}
              </Link>
            );
          })}
        </div>

        <div className="flex items-center gap-2 px-2">
            {/* Selector d'idioma */}
            <div className="flex gap-1">
                {LANGS.map((lang) => (
                <button
                    key={lang.code}
                    onClick={() => handleLangChange(lang.code)}
                    className={cn(
                    "text-xs font-semibold px-2 py-1 rounded transition-colors",
                    currentLang === lang.code
                        ? "bg-indigo-600 text-white"
                        : "text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                    )}
                >
                    {lang.label}
                </button>
                ))}
            </div>

            {/* Botó tema clar/fosc */}
            <button
                onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
                className="p-2 rounded-full text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                aria-label="Canviar tema"
            >
                {theme === "dark" ? <Sun size={20} /> : <Moon size={20} />}
            </button>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
