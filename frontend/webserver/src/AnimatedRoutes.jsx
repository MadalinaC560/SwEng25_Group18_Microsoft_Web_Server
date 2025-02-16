import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import Landing from "./pages/landing.jsx";
import AboutUs from "./pages/aboutUs.jsx";
import HomePage from "./pages/home.jsx";
import UserPage from "./pages/userPage.jsx";
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
        <Route
          path="/user"
          element={
            <PageWrapper>
              <UserPage />
            </PageWrapper>
          }
        />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </AnimatePresence>
  );
}

export default AnimatedRoutes;
