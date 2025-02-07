import React from "react";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";
import sampleData from "./SampleData";

// Helper function to generate random hex color
const generateRandomHexColor = () => {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  };

const PieGraph = () => {
  // Generate random colors for each slice
  const randomColors = sampleData.map(() => generateRandomHexColor());
  return (
    <ResponsiveContainer width={600} height={300}>
      <PieChart>
        <Pie
          data={sampleData}
          dataKey="siteInteractions"
          nameKey="name"
          cx="50%" // Position the pie chart at the center horizontally
          cy="50%" // Position the pie chart at the center vertically
          outerRadius={100} // Adjust the size of the pie chart
          fill="#8884d8"
          label
        >
          {sampleData.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={randomColors[index]} />
          ))}
        </Pie>
        <Tooltip />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
};

export default PieGraph;
