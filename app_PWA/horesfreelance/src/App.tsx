import { HashRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import FitxarScreen from "./screens/FitxarScreen";
import CalendarScreen from "./screens/CalendarScreen";
import DayDetailScreen from "./screens/DayDetailScreen";
import RegistreScreen from "./screens/RegistreScreen";
import ResumScreen from "./screens/ResumScreen";
import ClientsScreen from "./screens/ClientsScreen";
import DatabaseSelectorScreen from "./screens/DatabaseSelectorScreen";

export default function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/" element={<DatabaseSelectorScreen />} />
        <Route element={<Layout />}>
          <Route path="fitxar" element={<FitxarScreen />} />
          <Route path="calendar" element={<CalendarScreen />} />
          <Route path="dia/:id" element={<DayDetailScreen />} />
          <Route path="registre/:id" element={<RegistreScreen />} />
          <Route path="resum" element={<ResumScreen />} />
          <Route path="clients" element={<ClientsScreen />} />
        </Route>
      </Routes>
    </HashRouter>
  );
}
