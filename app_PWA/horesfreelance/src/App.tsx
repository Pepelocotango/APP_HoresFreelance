import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import FitxarScreen from "./screens/FitxarScreen";
import CalendarScreen from "./screens/CalendarScreen";
import DayDetailScreen from "./screens/DayDetailScreen";
import RegistreScreen from "./screens/RegistreScreen";
import ResumScreen from "./screens/ResumScreen";
import ClientsScreen from "./screens/ClientsScreen";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<FitxarScreen />} />
          <Route path="calendar" element={<CalendarScreen />} />
          <Route path="dia/:id" element={<DayDetailScreen />} />
          <Route path="registre/:id" element={<RegistreScreen />} />
          <Route path="resum" element={<ResumScreen />} />
          <Route path="clients" element={<ClientsScreen />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
