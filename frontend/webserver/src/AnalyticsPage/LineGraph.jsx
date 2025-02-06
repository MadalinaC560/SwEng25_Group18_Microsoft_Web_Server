import React from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"; 
import sampleData from "./SampleData";

const LineGraph = () => {
    return (
        <ResponsiveContainer width={600} height={300}>
            <LineChart data={sampleData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="siteInteractions" stroke="#8884d8" strokeWidth={2} />
            </LineChart>
        </ResponsiveContainer>
    );
};

export default LineGraph;
