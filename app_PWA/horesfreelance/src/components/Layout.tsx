import { useEffect } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import { Clock, Calendar, PieChart, Users } from "lucide-react";
import { cn } from "../lib/utils";
import { useStore } from "../store/useStore";

export default function Layout() {
  const location = useLocation();
  const { theme } = useStore();

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  const navItems = [
    { to: "/", icon: Clock, label: "Fitxar" },
    { to: "/calendar", icon: Calendar, label: "Calendari" },
    { to: "/resum", icon: PieChart, label: "Resum" },
    { to: "/clients", icon: Users, label: "Clients" },
  ];

  return (
    <div className="flex flex-col h-screen bg-slate-50 dark:bg-[#1e293b] text-slate-900 dark:text-slate-100 font-sans transition-colors duration-200">
      <main className="flex-1 overflow-y-auto pb-20 safe-area-bottom">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 w-full bg-white dark:bg-[#0f172a] border-t border-slate-200 dark:border-slate-800 safe-area-padding flex justify-around p-2 z-50 transition-colors">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.to || 
                           (item.to !== "/" && location.pathname.startsWith(item.to));

          return (
            <Link
              key={item.to}
              to={item.to}
              className={cn(
                "flex flex-col items-center justify-center p-2 rounded-xl min-w-[64px] transition-colors",
                isActive ? "text-indigo-600 dark:text-indigo-400" : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
              )}
            >
              <div className={cn("p-1 rounded-full", isActive && "bg-indigo-100 dark:bg-indigo-500/20")}>
                <Icon size={24} strokeWidth={isActive ? 2.5 : 2} />
              </div>
              <span className="text-xs mt-1 font-medium">{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
