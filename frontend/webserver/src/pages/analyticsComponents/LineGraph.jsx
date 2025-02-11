import React from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"; 
import sampleData from "./SampleData";

const LineGraph = () => {
    return (
        <div style={{ width: "100%", height: "100%" }}>
          <ResponsiveContainer width="100%" height="100%">
              <LineChart data={sampleData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis domain={[0, "auto"]} />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="siteInteractions" stroke="#8884d8" strokeWidth={2} />
              </LineChart>
          </ResponsiveContainer>
        </div>
    );
};

export default LineGraph;

