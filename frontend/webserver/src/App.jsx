import { useState } from 'react'
import './App.css'
import LineGraph from './AnalyticsPage/LineGraph'
import BarGraph from './AnalyticsPage/BarGraph'
import PieGraph from './AnalyticsPage/PieChart'
import AreaChartComponent from './AnalyticsPage/AreaChart'

function App() {
  return (
    <>
    <h1>This is a sample Analytical Page</h1>
      <div className='container'>
        <div className='box'>
          <LineGraph/>
          <p>Sample Line Graph</p>  
        </div>
        <div className='box'>
          <BarGraph/>
          <p>Sample Bar Graph</p>
        </div>
      </div>
      <div className='container'>
        <div className='box'>
          <PieGraph/> 
          <p>Sample Pie Chart</p>
        </div>
        <div className='box'>
          <AreaChartComponent/>
          <p>Sample Area Chart</p>
        </div>
      </div>
    </>
  )
}

export default App
