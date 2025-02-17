import React, { useState } from "react";
import AreaChartComponent from "../analyticsComponents/AreaChart";
import BarGraph from "../analyticsComponents/BarGraph";
import LineGraph from "../analyticsComponents/LineGraph";
import PieGraph from "../analyticsComponents/PieChart";

function UserAnalyticsBox() {
  const [selectedOption, setSelectedOption] = useState("AreaChart");

  const handleOptionChange = (event) => {
    setSelectedOption(event.target.value);
  };

  const renderContent = () => {
    switch (selectedOption) {
      case "AreaChart":
        return (
          <div className="graph">
            <AreaChartComponent />
          </div>
        );
      case "BarGraph":
        return (
          <div className="graph">
            <BarGraph />
          </div>
        );
      case "LineGraph":
        return (
          <div className="graph">
            <LineGraph />
          </div>
        );
      case "PieChart":
        return (
          <div className="graph">
            <PieGraph />
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="container">
      {/* Radio Buttons */}
      <div className="radio-inputs">
        <label className="radio">
          <input
            type="radio"
            name="radio"
            value="AreaChart"
            checked={selectedOption === "AreaChart"}
            onChange={handleOptionChange}
          />
          <span className="name">Area</span>
        </label>
        <label className="radio">
          <input
            type="radio"
            name="radio"
            value="BarGraph"
            checked={selectedOption === "BarGraph"}
            onChange={handleOptionChange}
          />
          <span className="name">Bar</span>
        </label>
        <label className="radio">
          <input
            type="radio"
            name="radio"
            value="LineGraph"
            checked={selectedOption === "LineGraph"}
            onChange={handleOptionChange}
          />
          <span className="name">Line</span>
        </label>
        <label className="radio">
          <input
            type="radio"
            name="radio"
            value="PieChart"
            checked={selectedOption === "PieChart"}
            onChange={handleOptionChange}
          />
          <span className="name">Pie</span>
        </label>
      </div>

      {/* Content Container */}
      <div className="content-container">{renderContent()}</div>
    </div>
  );
}

export default UserAnalyticsBox;
