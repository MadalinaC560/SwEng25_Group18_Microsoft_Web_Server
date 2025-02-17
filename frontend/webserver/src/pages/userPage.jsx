import UserInfoBoxes from "../components/userComponents/userInfoBoxes";
import UserAnalyticsBox from "../components/userComponents/userAnalyticsBox";
import "../css/user.css";

function UserPage() {
  return (
    <div className="info-container">
      <UserInfoBoxes />
      <UserAnalyticsBox />
    </div>
  );
}

export default UserPage;
