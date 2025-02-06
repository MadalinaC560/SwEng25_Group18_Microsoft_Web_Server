import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import LineGraph from './Analytics'

function App() {
  return (
    <>
      <div>
        <LineGraph/>
        <p>Sample Line Graph</p>  
      </div>
    </>
  )
}

export default App
