import { useNavigate } from "react-router-dom";

function NotFound() {
  const navigate = useNavigate();

  const handleButtonClick = () => {
    navigate("/");
  };
  return (
    <div>
      <h1>ERROR</h1>
      <h2>Page Not Found, Try Again</h2>
      <button onClick={handleButtonClick} type="button">
        Return
      </button>
    </div>
  );
}

export default NotFound;
