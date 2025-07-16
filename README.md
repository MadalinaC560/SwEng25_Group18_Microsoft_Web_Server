# ☁️ CLOUDLE - From Vision to Reality  
**Host. Scale. Grow.**

---

## 🚀 What Is This Project About?

CLOUDLE is a **modular, scalable, and secure cloud-based web hosting platform** built using **Azure**. It empowers users to host web applications with ease while ensuring enterprise-grade performance, fault tolerance, and observability.

We built our own **custom web server from scratch** (no 3rd-party frameworks) and extended its capabilities through Azure to meet real-world demands for hosting.

---

## 🔍 Core Features

- 🌐 **Static + Dynamic Web Hosting**
- 🔐 **User/Admin Authentication & Authorisation**
- 📂 **Secure File Uploads**
- 📊 **User & Admin Telemetry & Analytics**
- 🔄 **Zip File Handling and Extraction**
- ☁️ **Deployed on Microsoft Azure**
- ♻️ **Built with Green Computing Practices in Mind**

---

## 🧠 Architecture Overview
```
Client Layer (User/Admin) → HTTPS Azure Load Balancer (Traffic Distributor) 
→ Backend Server (Authentication | API requests) 
    ↳ API Azure Infrastructure 
    ⇅
    ↳ AzureSQL Database URL
⤴ Returns request to user
```

---

## 🧰 Tech Stack

### 🖥️ Frontend
- React, HTML, CSS, Tailwind CSS, JavaScript

### ⚙️ Backend
- Java (Custom-built HTTP Server)
- Node.js (Server-side scripting)
- Azure Cloud Services (Load Balancer, Blob Storage, SQL, TLS, Monitoring)

---

## ♻️ Green Computing

We leverage **Azure’s sustainability-focused infrastructure** to reduce our carbon footprint. Green computing was a core consideration in the design and deployment process — aligning with our module’s goals and Microsoft's climate initiatives.

---

## ✅ Project Management

- 🗂️ **Agile Workflow** using Kanban Boards
- 📌 GitLab Issues + Branch Naming Conventions (feature/task-specific)
- 🔄 Continuous Integration & Testing via GitLab Pipelines

---


## 👨‍💻 Team Members

| Name                     | Role                    |
|--------------------------|-------------------------|
| Abdul Wadood Rehan       | Backend Developer       |
| Eniola Olumenyan         | Backend / Documentation |
| Dylan Gallagher          | Backend Lead            |
| Daniel Fitzgerald        | Backend / Documentation |
| Ethan Duffy              | Backend Developer       |
| Anastasia O’Donnell      | Backend Developer       |
| Andy Yu                  | Frontend Developer      |
| Madalina Costovici       | Team / Frontend Lead    |

---

## 📘 Project Description

This is **Project 23: Microsoft Cloud Web Server**, guided by mentors **Dillan De Langen** and **Lei Xue**.

### 📌 Goal:
To design and implement a **modern, cloud-first web hosting platform** using Azure — including our own fully functional custom-built HTTP server.

### 🧱 Key Outcomes:
- HTTP spec implementation
- Systems & cloud architecture
- Fault tolerance & scalability
- Encryption, telemetry, TLS support
- UI and developer observability
- Secure static & dynamic hosting

---

## 🧪 Testing & Validation

- ✅ JUnit for backend components
- ✅ Gradle pipeline integration
- ✅ Manual testing with tools like Postman and browser
- 🔜 _Integration test suite coming soon_ (tracked under issue `#LS`)

---

📁 Project Directory Structure 

```
.
├── README.md
├── app
│   ├── bin
│   │   ├── com
│   │   ├── main
│   │   └── test
│   ├── build
│   │   ├── classes
│   │   ├── distributions
│   │   ├── generated
│   │   ├── libs
│   │   ├── reports
│   │   ├── resources
│   │   ├── scripts
│   │   ├── test-results
│   │   └── tmp
│   ├── build.gradle.kts
│   ├── server.properties
│   ├── src
│   │   ├── main
│   │   └── test
│   ├── test_webroot
│   │   ├── index.html
│   │   ├── test.txt
│   │   └── testfile.txt
│   └── webserver.log
├── build
│   ├── reports
│   │   ├── configuration-cache
│   │   └── problems
│   └── tmp
│       ├── buildEnvironment
│       ├── components
│       ├── dependencies
│       ├── tasks
│       └── wrapper
├── frontend
│   └── webserver
│       ├── README.md
│       ├── index.html
│       ├── package.json
│       ├── vite.config.js
│       └── src
├── lib
│   └── junit-jupiter-api-5.8.1.jar
├── placeholder_web_server
│   ├── server.py
├── settings.gradle.kts
└── webserver.log

```

---

## 🌐 Hosted On Azure

We leveraged multiple Azure services for deployment and scalability:

- Azure Load Balancer  
- Azure Blob Storage  
- Azure SQL Database  
- Azure Monitoring & TLS

---

> 🧠 **Fun Fact:** Everything — from HTTP parsing, routing, file handling, telemetry, and server logic — was **written from scratch in Java**.

---

## 🔗 License




