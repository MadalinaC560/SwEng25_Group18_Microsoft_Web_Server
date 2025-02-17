// Replace hardcoded data values with dynaimc server ones through api's

function UserInfoBoxes() {
  return (
    <div className="user-page">
      {/* Floating Information Blocks */}
      <div className="info-blocks">
        <div className="info-block">
          <h3>Total Online Users</h3>
          <p>1,234</p>
        </div>
        <div className="info-block">
          <h3>Current Uptime</h3>
          <p>99.9%</p>
        </div>
        <div className="info-block">
          <h3>Current Costs</h3>
          <p>$1,200</p>
        </div>
        <div className="info-block">
          <h3>Active Sessions</h3>
          <p>567</p>
        </div>
      </div>
    </div>
  );
}

export default UserInfoBoxes;
