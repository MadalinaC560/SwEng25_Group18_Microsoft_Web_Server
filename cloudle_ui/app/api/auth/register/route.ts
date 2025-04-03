import { NextResponse } from "next/server";
import fs from "fs/promises";
import path from "path";

export async function POST(req: Request) {
  try {
    const { email, password } = await req.json();

    if (!email || !password) {
      return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
    }

    // Define the path to the user file
    const usersFilePath = path.join(process.cwd(), "supersecureusers.json");

    let users = [];

    try {
      // Check if the file exists and is not empty
      const usersData = await fs.readFile(usersFilePath, "utf-8");
      users = usersData.trim() ? JSON.parse(usersData) : []; // Handle empty file
    } catch (error) {
      console.warn("User file missing or empty, initializing...");
      users = [];
    }

    // Check if user already exists
    if (users.find((u: any) => u.email === email)) {
      return NextResponse.json({ error: "User already exists" }, { status: 400 });
    }

    // Add new user
    const newUser = { email, password };
    users.push(newUser);

    // Save the new user list to the file
    await fs.writeFile(usersFilePath, JSON.stringify(users, null, 2));

    return NextResponse.json({ message: "User registered successfully" }, { status: 201 });
  } catch (error) {
    console.error("Register Error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
