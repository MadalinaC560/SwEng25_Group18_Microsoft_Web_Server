import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import Landing from "./pages/landing.jsx";
import AnalyticsPage from "./pages/analytics.jsx";
import AboutUs from "./pages/aboutUs.jsx";
import HomePage from "./pages/home.jsx";
import NotFound from "./pages/notfoundpage.jsx";
import PageWrapper from "./components/animationComponents/PageWrapper.jsx";

function AnimatedRoutes() {
  const location = useLocation(); // Track the current route

  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route
          path="/"
          element={
            <PageWrapper>
              <Landing />
            </PageWrapper>
          }
        />
        <Route
          path="/analytics"
          element={
            <PageWrapper>
              <AnalyticsPage />
            </PageWrapper>
          }
        />
        <Route
          path="/aboutUs"
          element={
            <PageWrapper>
              <AboutUs />
            </PageWrapper>
          }
        />
        <Route
          path="/home"
          element={
            <PageWrapper>
              <HomePage />
            </PageWrapper>
          }
        />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </AnimatePresence>
  );
}

export default AnimatedRoutes;
