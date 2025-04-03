import { NextResponse } from "next/server";

export function middleware(req) {
  const token = req.cookies.get("token");

  if (!token && req.nextUrl.pathname !== "/login" && req.nextUrl.pathname !== "/register_account") {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  return NextResponse.next();
}
