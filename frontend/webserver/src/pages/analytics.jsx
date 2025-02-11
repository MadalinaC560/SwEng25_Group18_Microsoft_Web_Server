import AreaChartComponent from "./analyticsComponents/AreaChart";
import LineGraph from "./analyticsComponents/LineGraph";
import BarGraph from "./analyticsComponents/BarGraph";
import PieGraph from "./analyticsComponents/PieChart";

function AnalyticsPage() {
  return (
    <>
      {/* <div className="greenBackground"> */}
      <h1 color="(231,0,42)">This is a sample Analytical Page</h1>
      <div className="container">
        <div className="box">
          <LineGraph />
          <p className="graph-title">Sample Line Graph</p>
        </div>
        <div className="box">
          <BarGraph />
          <p className="graph-title">Sample Bar Graph</p>
        </div>
      </div>
      <div className="container">
        <div className="box">
          <PieGraph />
          <p className="graph-title">Sample Pie Chart</p>
        </div>
        <div className="box">
          <AreaChartComponent />
          <p className="graph-title">Sample Area Chart</p>
        </div>
      </div>
      {/* </div> */}
    </>
  );
}

export default AnalyticsPage;
