import React from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

const sampleData = [
    { name: 'User 1', siteInteractions: 5 },
    { name: 'User 2', siteInteractions: 2 },
    { name: 'User 3', siteInteractions: 6 },
    { name: 'User 4', siteInteractions: 7 },
    { name: 'User 5', siteInteractions: 1 },
];

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
