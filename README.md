# MangaWatch 

## What is MangaWatch?

Made in the vein of something like Letterboxd and MAL but focused on Mangas, MangaWatch is a personal Manga Tracker. 
Choose from a massive selection of Mangas in the Discover section, pick what titles you like and add them to your library. 
Track your progress of that title in the Library.

## Structure

The Repo/Directory is conveniently divided into /frontend and /backend for their respective roles.

## Tech Stack

Briefly, the frontend is React and Vite with Tailwind, uses Axios and NPM of course. Lucide-React for the icons/svgs.
On the backend is Spring Boot (The starter) with PostgreSQL running in a docker container. JSON Dependencies and flywaydb for migrations.

## Features

The application has 3 main pages, Home (A page with popular genres and a search bar), Discover and Library. It also has Light and Dark modes, and supports/works on Mobile as well.

Discover is the page which houses Manga entries (displays by Pagination), filters, genres (All genres form all mangas, none left behind), sorting (Alphabetically, by Author, by Date) etc.
The titles are fetched from the DB which has around 87,000+ individual manga titles. They've been imported sequentially from MangaDex's database using its API.
So it has a wide variety ot titles and you'll 99/100 times find what you're looking for.

Library is your personal list/storage of titles you are reading, plan to read or have read before, it's protected so you must log in to access the Library functionality.
Creating an account is simple, just a Mail ID, Username and Password. The password is hashed of course, and the JSON Web Token is issued each time you login. 
For security, the routes require a valid Token to add or delete entries as well so ONLY you can mess with your Library. 

The application has fluent communication between frontend and backend. It may load some things optimistically but you'll get valid error logs if something breaks or fails.
The backend has defined routes for importing the MangaDex database, it imports around 10,000 titles at a time, and updates those what already exist to accomodate for changes in status or description.
All the Data be it the User, their Library or the Manga Entries, are stored in the PostgreSQL database running in a Docker container.

## Snippets

**PC View**

![PC View](https://github.com/Rjv-RY/MangaWatch/blob/main/Screenshot%202025-11-12%20182735.png)

**Mobile View (Navbar items become dowpdown)**

![Mobile View](https://github.com/Rjv-RY/MangaWatch/blob/main/Screenshot%202025-11-12%20182656.png)

## Run Instrcutions


